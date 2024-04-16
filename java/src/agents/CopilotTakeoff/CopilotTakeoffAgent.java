package agents.CopilotTakeoff;

import agents.XPlaneAgent;
import data.FlightData;
//import jdk.internal.util.xml.impl.Input;
import graph1.Node;
import org.jsoar.kernel.*;
import org.jsoar.kernel.events.OutputEvent;
import org.jsoar.kernel.io.InputBuilder;
import org.jsoar.kernel.io.InputWme;
import org.jsoar.kernel.memory.Wme;
import org.jsoar.kernel.symbols.SymbolFactory;
import org.jsoar.util.commands.SoarCommands;

import xplane.WaypointController;
import xplane.XPCUserInterface;
import xplane.XPlaneConnector;

import java.io.*;
import java.util.Iterator;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static xplane.WaypointController.currentPolygonIndex;
import static xplane.WaypointController.currentWaypointIndex;
import static xplane.XPlaneConnector.getFlightData;

public class CopilotTakeoffAgent extends XPlaneAgent
{
    boolean DISPLAYDETAILS = true;
     private SymbolFactory syms;
    Agent soar_agent = getAgent();
    InputBuilder builder;
    double batteryLevel = 0.0;
    double heading = 0.0;
    double latitude = 0.0;
    double longitude = 0.0;
    double pitch = 0.0;
    double targetAltitude = 3000;
    int elevation = 0;
    int desRate = 0;
    double throttle = 0.0;
    double targetHeading = 0.0;
    double errorInSensor = 0.0;
    int timeTag = 0, timeTagClear =0;
    boolean allEnginesOK = true;
    DecisionCycle decisionCycle;
    private boolean wheelBrakesON;
    private boolean airBrakesON;
    private boolean reversersON;

    private java.lang.String realTime;
    private PipedReader filterOutput;
    private XPCUserInterface UIobj;
    private WaypointController waypointControl;
    private XPlaneConnector xpcobj;

    private int cycleCount = 0, trial_count = 0,timeOffset = 0;;
    private String pilotAlertResponse = "yes";
    private boolean trialActive = false;
    private PrintWriter rlWriter, resultWriter;

    @Override
    public java.lang.String name() {
        return "Copilot_Takeoff";
    }

    @Override
    public boolean runOnStartup() {
        return true;
    }

    @Override
    public void start()  {
        soar_agent = setupSoarAgent();
        xpcobj = new XPlaneConnector();
        xpcobj.setAutopilot(162);
        soar_agent.initialize();

        //wait on GUI to be set up before loading in soar
        setupGUI();

        try {
            soar_agent.getInterpreter().eval("trace --rl");
        } catch (SoarException e) {
            e.printStackTrace();
        }
        addPropertiesFromXplaneToAgent();
        readInSOARFile();
        updateThrottleandRotorPositions();

        //push flight data every 500ms to update
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(pushFlightData(), 500, 200, TimeUnit.MILLISECONDS);
    }

    private void updateThrottleandRotorPositions(){
        soar_agent.getEvents().addListener(OutputEvent.class,
                soarEvent ->
                {
                    // Contains details of the OutputEvent
                    Executors.newSingleThreadExecutor().execute(() ->
                    {
                        CopilotTakeoffAgent.this.throttleWmes((OutputEvent) soarEvent);  // Update throttle
                        CopilotTakeoffAgent.this.VTOLModeWmes((OutputEvent) soarEvent);  // Update the VTOL rotor position
                    });
                });
    }
    private void readInSOARFile(){
        // read the soar file
        try
        {
            String pathToSoar =  "C:\\Users\\assist-lab\\Documents\\adnav-pop-weather\\SOARAgent\\load.soar";
            SoarCommands.source(soar_agent.getInterpreter(), pathToSoar);
            System.out.println("There are now " + soar_agent.getProductions().getProductionCount() + " productions loaded");

        }
        catch (SoarException e) {
            e.printStackTrace();
        }
    }

    private WaypointController getWaypoints() {
        waypointControl = WaypointController.getSingle_instance();
        //waypoints = get_instance
        if (UIobj.displayLOCButton == false && UIobj.startedLOC == true){
            waypointControl.updateWaypointList(UIobj.cities[UIobj.selectedCityIndex], UIobj.startCoordinates);
            UIobj.startedLOC = false;
        }
        waypointControl.updateNextWaypoints(UIobj.getIMULat(),UIobj.getIMULon());
        return waypointControl;
    }

    private void setupGUI(){
        try {
            UIobj = new XPCUserInterface();
        }catch(Exception e){
            e.printStackTrace();
        }

        //don't start pushing data until start flight button is clicked on UI
        while(UIobj.startedTakeOffProcedure == false){
            System.out.println("");
        }
    }

    private void addPropertiesFromXplaneToAgent(){
        builder = InputBuilder.create(soar_agent.getInputOutput());
        builder.push("flightdata").markWme("fd").
                add("airspeed", 0.0).markWme("as").
                add("lat", latitude).markWme("lat").
                add("lon", longitude).markWme("lon").
                add("altitude", pitch).markWme("alt").
                add("elevation", elevation).markWme("elv").
                add("des-rate", elevation).markWme("dr").
                add("allEnginesOK", allEnginesOK).markWme("engOK").
                add("wheelbrakesON", wheelBrakesON).markWme("wBrakes").
                add("airbrakesON", airBrakesON).markWme("aBrakes").
                add("throttle", throttle).markWme("throttle").
                add("initiate-landing", UIobj.startedLandingProcedure).markWme("initiate-landing").
                add("distance-to-target", UIobj.distanceToTarget).markWme("distance-to-target").
                add("reversersON", reversersON).markWme("reverse");
    }

    private Agent setupSoarAgent(){
        System.setProperty("jsoar.agent.interpreter","tcl");
        System.err.println("Started");
        soar_agent = getAgent();
        PipedWriter agentWriter = new PipedWriter();
        filterOutput = new PipedReader();

        try
        {
            agentWriter.connect(filterOutput);
        }
        catch (IOException ignored) {}

        soar_agent.setName("SOAR_Agent");

        syms = soar_agent.getSymbols();

        return soar_agent;
    }

    private void throttleWmes(OutputEvent soarEvent)
    {

        Iterator<Wme> wmes = soarEvent.getWmes();
        while (wmes.hasNext())
        {
            Wme nextWME = wmes.next();
            printWme(nextWME);
            if (nextWME.getAttribute().asString().getValue().equals("throttle"))
            {
                System.out.println(nextWME.getValue());

                String txt = nextWME.getValue().toString();
                float throttle = Float.parseFloat(txt);
                xpcobj.setEngineThrottle(throttle);
                //speakText(txt);
            }else if (nextWME.getAttribute().asString().getValue().equals("target-altitude"))
            {
                System.err.println("HELLO");
                System.err.println(nextWME.getValue());

                String txt = nextWME.getValue().toString();
                int targetAlt = Integer.parseInt(txt);
                xpcobj.setTargetAltitude(targetAlt);
            }else if (nextWME.getAttribute().asString().getValue().equals("target-elevation"))
            {
                //System.err.println("HELLO");
                System.out.println(nextWME.getValue());

                String txt = nextWME.getValue().toString();
                int elevation = Integer.parseInt(txt);
                xpcobj.setTargetElevation(elevation);

            }else if (nextWME.getAttribute().asString().getValue().equals("target-des-rate"))
            {
                //System.err.println("HELLO");
                System.out.println(nextWME.getValue());

                String txt = nextWME.getValue().toString();
                int desRate = Integer.parseInt(txt);
                xpcobj.setDesRate(desRate);

            }else if (nextWME.getAttribute().asString().getValue().equals("target-speed"))
            {
                //System.err.println("HELLO");
                System.out.println(nextWME.getValue());

                String txt = nextWME.getValue().toString();
                int airspeed = Integer.parseInt(txt);
                xpcobj.setTargetAirspeed(airspeed);
                if(airspeed == 0 && xpcobj.getAutopilotState() != 170){
                    // xpcobj.setAutopilot(170);
                }

            }else if (nextWME.getAttribute().asString().getValue().equals("autoflaps"))
            {
                //System.err.println("HELLO");
                System.out.println(nextWME.getValue());

                String txt = nextWME.getValue().toString();
                if(txt.equalsIgnoreCase("on")){
                    xpcobj.setFlapsMode(1);
                }else{
                    xpcobj.setFlapsMode(0);
                }
            }else if (nextWME.getAttribute().asString().getValue().equals("air-brake"))
            {
//                System.err.println("HELLO");
                System.out.println(nextWME.getValue());

                String txt = nextWME.getValue().toString();
                float brakeDeployment = Float.parseFloat(txt);
                xpcobj.setAirBrake(brakeDeployment);

            }

        }
    }
    private void VTOLModeWmes(OutputEvent soarEvent)
    {

        Iterator<Wme> wmes = soarEvent.getWmes();
        while (wmes.hasNext())
        {
            Wme nextWME = wmes.next();
            if(DISPLAYDETAILS)
                System.out.println(nextWME.getAttribute().asString().getValue());
            if (nextWME.getAttribute().asString().getValue().equals("VTOLMode"))
            {


                String txt = nextWME.getValue().toString();
                //System.err.println(txt);
                if(txt.equalsIgnoreCase("vertical")){
                    xpcobj.setVTOLModeVertical();
                    UIobj.finishedTakeoff=false;
                    if(xpcobj.getAutopilotState() == 164)
                        xpcobj.setAutopilot(162);
                    ///xpcobj.setAutopilot(164);


                }else if(txt.equalsIgnoreCase("horizontal")){
                    xpcobj.setVTOLModeHorizontal();
                    UIobj.finishedTakeoff=true;
                    if(xpcobj.getAutopilotState() == 164)
                        xpcobj.setAutopilot(162);

                }else{
                    System.err.println("ERROR invalid VTOLMode");
                }

            }
        }
    }
    private Runnable pushFlightData()
    {
        return () ->
        {
            long bt = System.nanoTime()/1000000;
            FlightData data = getFlightData();
            try {
                if(DISPLAYDETAILS)
                    System.out.println(data.lat +","+data.lon);

                double latitude = 0.0;
                double longitude = 0.0;

                latitude = UIobj.getIMULat();
                longitude = UIobj.getIMULon();

                if(DISPLAYDETAILS)
                    System.out.println(xpcobj.getAutopilotState());

                if(xpcobj.getAutopilotState() != 162){
                    xpcobj.setAutopilot(162);
                }

                // calculate heading to target waypoint
                waypointControl = getWaypoints();

                // read in polygon text file (once and only once)
                waypointControl.setInitialPolygonFileLoaded();
                // determine new target waypoint
                targetHeading = waypointControl.calculateHeading(waypointControl.waypoints.get(currentPolygonIndex).get(currentWaypointIndex).getCoordinates()[0], waypointControl.waypoints.get(currentPolygonIndex).get(currentWaypointIndex).getCoordinates()[1], latitude, longitude);


                UIobj.setTargetWaypoint((float)waypointControl.waypoints.get(currentPolygonIndex).get(currentWaypointIndex).getCoordinates()[0],(float)waypointControl.waypoints.get(currentPolygonIndex).get(currentWaypointIndex).getCoordinates()[1]);
                UIobj.setDistanceToTarget(waypointControl.getDistanceToWaypoint(latitude,longitude));
                UIobj.displayLandingButton = waypointControl.waypoints.get(currentPolygonIndex).get(currentWaypointIndex).isLandingPoint;
                System.out.println("TARGET HEADING: " + targetHeading);
                xpcobj.setTargetHeading(targetHeading);

                UIobj.planeAirspeed = data.airspeed;
                UIobj.planeAltitude = data.altitude;
                UIobj.desRate = data.desRate;
                UIobj.planeElevation = data.elevation;
                UIobj.currentLat = data.lat;
                UIobj.currentLon = data.lon;

                InputWme bl = builder.getWme("as");
                bl.update(syms.createInteger(Math.abs(data.airspeed)));
                InputWme lat = builder.getWme("lat");
                lat.update(syms.createDouble(latitude));
                InputWme lon = builder.getWme("lon");
                lon.update(syms.createDouble(longitude));

                InputWme throttle = builder.getWme("throttle");
                throttle.update(syms.createDouble(data.throttle));
//                InputWme targetalt = builder.getWme("target-altitude");
//                targetalt.update(syms.createInteger(data.targetAlt));
////                xpcobj.setTargetAltitude(data.targetAlt);

                InputWme initiatedLandingWme = builder.getWme("initiate-landing");
                initiatedLandingWme.update(syms.createString(UIobj.startedLandingProcedure ? "yes":"no"));

                InputWme d = builder.getWme("distance-to-target");
                d.update(syms.createDouble(UIobj.distanceToTarget));
                InputWme p = builder.getWme("alt");
                p.update(syms.createInteger(data.altitude));
                InputWme ev = builder.getWme("elv");
                ev.update(syms.createInteger(data.elevation));
                InputWme dr = builder.getWme("dr");
                dr.update(syms.createInteger(data.desRate));
                InputWme e = builder.getWme("engOK");
                e.update(syms.createString(Boolean.toString(data.allEningesOK)));
                InputWme wb = builder.getWme("wBrakes");
                wb.update(syms.createString(Boolean.toString(data.wheelBrakesON)));
                InputWme ab = builder.getWme("aBrakes");
                ab.update(syms.createString(Boolean.toString(data.airBrakesON)));
                InputWme re = builder.getWme("reverse");
                re.update(syms.createString(Boolean.toString(data.reversersON)));

                // uncomment if you want to see the productions that matches
                MatchSet matchSet = soar_agent.getMatchSet();

                if (matchSet.getEntries().size() > 1) {
                    System.out.println("Found matching productions!!");
                    for (MatchSetEntry mse : matchSet.getEntries()) {
                        System.out.println("Production:" + mse.getProduction());
                    }
                }

                soar_agent.runFor(1, RunType.DECISIONS);

            }catch(Exception e){
                e.printStackTrace();
            }
            long at = System.nanoTime()/1000000;
            if(DISPLAYDETAILS)
                System.out.println("[INFO]time taken: "+(at-bt)+"ms");
        } ;
    }

    private void printWme(Wme wme)
    {
        System.out.println(wme);
        Iterator<Wme> children = wme.getChildren();
        while (children.hasNext())
        {
            Wme child = children.next();
            printWme(child);
        }
    }
}
