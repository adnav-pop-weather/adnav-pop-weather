package main;

import agents.XPlaneAgent;
import gov.nasa.xpc.XPlaneConnect;
import org.reflections.Reflections;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 * Created by mstafford on 10/3/2016.
 */
public class StartAgents
{
    static XPlaneConnect xpc = null;
    public static void main(String[] args)
    {
        Reflections reflect = new Reflections("agents");
        Set<Class<? extends XPlaneAgent>> agentClasses = reflect.getSubTypesOf(XPlaneAgent.class);

        ExecutorService agentThreadPool = Executors.newCachedThreadPool();

        for(Class<? extends XPlaneAgent> agentClass : agentClasses)
        {
            try (XPlaneAgent agent = agentClass.newInstance())
            {
                if (agent.runOnStartup())
                {
                    System.err.println(agent.getClass());
                    agentThreadPool.submit(agent);
                }
            }
            catch (Exception ignored) {
                System.err.println(ignored.getMessage());
                ignored.printStackTrace();
            }
        }
    }

    public static XPlaneConnect getXPlaneConnector()
    {
        if ( xpc == null )
        {
            try
            {
                xpc = new XPlaneConnect();
                // Ensure connection established.
                xpc.getDREF("sim/test/test_float");
                return xpc;
            }
            catch (IOException e) {
                System.err.println(e.getStackTrace()[0] + e.getMessage() + "  Is X-Plane running?");
            }
            System.exit(-1);
            return null;
        }
        else
        {
            return xpc;
        }
    }
}
