package xplane;

import java.awt.*;
import java.io.IOException;
import java.net.URL;
import javax.imageio.ImageIO;
import javax.swing.*;

import xplane.InterfacePanel.CommandPanel;
import xplane.InterfacePanel.MapPanel;
import xplane.InterfacePanel.mapBoxAPI.mapbox;

import static xplane.XPlaneConnector.fuelQuantity;

public class XPCUserInterface extends JFrame {
    public float[][] positions = new float[4][2];
    public double distanceToTarget = 0.0;
    public boolean displayLandingButton = true, startedLandingProcedure = false, finishedTakeoff = false, startedTakeOffProcedure = false;
    public static boolean displayLOCButton = true, startedLOC = false, showLocRoute = false;
    public static boolean updatePosition = true;
    public String[] cities = new String[]{"Dallas", "Wichita", "Oklahoma"};
    public static int selectedCityIndex = 0;
    public int planeAirspeed = 0, planeAltitude = 0;
    public double currentLat, currentLon;
    public double[] startCoordinates;
    public int planeElevation = 0, desRate = 0;

    public void setTargetWaypoint(float xPos, float yPos) {
        positions[3][0] = xPos;
        positions[3][1] = yPos;
    }

    public void setDistanceToTarget(double distance) {
        distanceToTarget = distance;
    }

    public double getIMULat() {
        return currentLat;
    }

    public double getIMULon() {
        return currentLon;
    }

    private final CommandPanel commandPanel;
    private final MapPanel mapPanel;

    public XPCUserInterface() {
        setTitle("Sensor Output:");
        setResizable(false);
        setLocation(400, 0);
        setSize(1000, 1010);
        setLayout(new GridLayout(2, 1));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);

        // Create the first JPanel
        commandPanel = new CommandPanel(new double[]{currentLat, currentLon}, new double[]{(double)positions[3][0], (double)positions[3][1]},
                distanceToTarget, new int[]{desRate, planeAirspeed, planeAltitude, planeElevation}, fuelQuantity);
        commandPanel.setLayout(null);

        mapPanel = new MapPanel(new double[]{currentLat, currentLon}, showLocRoute);

        // Add the JPanels to the frame
        add(commandPanel);
        add(mapPanel);

        // Start the thread
        Thread thread1 = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(10);
                        // Call the updateLabel method to update the label
                        ((CommandPanel) commandPanel).updateLabel(new double[]{currentLat, currentLon}, new double[]{(double)positions[3][0], (double)positions[3][1]},
                                distanceToTarget, new int[]{desRate, planeAirspeed, planeAltitude, planeElevation}, fuelQuantity);
                        startedTakeOffProcedure = commandPanel.getConditionFlag()[0];
                        startedLandingProcedure = commandPanel.getConditionFlag()[1];
                        displayLOCButton = commandPanel.getConditionFlag()[2];
                        startedLOC = commandPanel.getConditionFlag()[3];
                        showLocRoute = commandPanel.getConditionFlag()[4];


                        selectedCityIndex = commandPanel.getCityIndex();

                        if (startedLOC && updatePosition) {
                            startCoordinates = new double[]{currentLat, currentLon};
                            updatePosition = false;
                        }
                        revalidate();
                        repaint();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        thread1.start();

        Thread thread2 = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(10000);
                        ((MapPanel) mapPanel).updateMapPanel(new double[]{currentLat, currentLon}, showLocRoute);
                        revalidate();
                        repaint();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        thread2.start();
    }
}


