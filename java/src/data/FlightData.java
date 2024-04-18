package data;

import org.jsoar.util.events.SoarEvent;

/**
 * Created by icislab on 10/13/2016.
 */
public class FlightData implements SoarEvent
{
    public int airspeed;
    public int altitude;
    public double lat;
    public double lon;
    public double throttle;
    public boolean allEningesOK = true;
    public boolean wheelBrakesON;
    public boolean airBrakesON;
    public boolean reversersON;
    public int elevation;
    public int desRate;


    public FlightData(int desRate, int elevation, int airspeed, int altitude, double lat, double lon, double throttle, boolean allEnginesOK, boolean wBrakes, boolean aBrakes, boolean reversers)
    {
        this.airspeed = airspeed;
        this.altitude = altitude;
        this.lat = lat;
        this.lon = lon;
        this.throttle = throttle;
        this.allEningesOK = allEnginesOK;
        this.wheelBrakesON = wBrakes;
        this.airBrakesON = aBrakes;
        this.reversersON = reversers;
        this.elevation = elevation;
        this.desRate = desRate;
    }

}
