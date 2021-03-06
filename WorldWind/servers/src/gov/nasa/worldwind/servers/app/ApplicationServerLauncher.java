/*
Copyright (C) 2001, 2011 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/

package gov.nasa.worldwind.servers.app;

import gov.nasa.worldwind.Version;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.util.Logging;

import java.io.File;

/**
 * @author Lado Garakanidze
 * @version $Id$
 */

public class ApplicationServerLauncher
{
    /**
     * This is a starting point of the NASA World Wind Application Server.
     * This launcher reads a WEB-INF/web.xml applications configuration file,
     * instantiates web applications, and waits forever until the application server is terminated.
     * <p/>
     * There are two specifics when running this launcher as a windows service.
     * We recommend to use Apache Commons Daemon framework to run this launcher as a windows service.
     * 1. When a windows service starts, the launcher is invoked with the "start" argument.
     * The launcher must start application and never terminate. Otherwise, if terminated,
     * Apache Commons Daemon will consider that the service failed to start.
     * 2. This is very important! When the windows service is stopped, this launcher will be running
     * with the "stop" argument and in the SAME JVM instance as when started.
     * Therefore, since the ApplicationServer is singleton class (per JVM, of course),
     * invoking ApplicationServer.getInstance(); will return the same ApplicationServer instance,
     * as when started. Remember, this is not true, when you run this launcher as a regular application.
     *
     * @param args These startup parameters are used only when running as a windows service
     *             and could be only "start" or "stop".
     */
    public static void main(String[] args)
    {
        String cmd = (null != args && args.length > 0) ? args[0] : "start";

        try
        {
            gov.nasa.worldwind.Configuration.setValue(AVKey.LOGGER_NAME, Logging.class.getPackage().getName());

            ApplicationServer appServer = ApplicationServer.getInstance();

            if ("start".equalsIgnoreCase(cmd))
            {
                String userHomeFolder = System.getProperties().getProperty("user.dir");
                File webAppConfigFile = new File(userHomeFolder + File.separator + "WEB-INF/web.xml");

                // log version of World Wind Java SDK
                Logging.logger().info(Logging.getMessage("WMS.Server.StartingUp", Version.getVersion() ));

                appServer.loadApplications(webAppConfigFile);
                appServer.waitToFinish();
            }
            else if ("stop".equalsIgnoreCase(cmd))
            {
                appServer.stop();
            }
        }
        catch (Exception e)
        {
            String message = Logging.getMessage("WMS.Server.ShuttingDown", e.getMessage());
            Logging.logger().severe(message);
        }
        finally
        {
            String message = Logging.getMessage("WMS.Server.ShuttingDown", "OK");
            Logging.logger().info(message);
        }
    }
}
