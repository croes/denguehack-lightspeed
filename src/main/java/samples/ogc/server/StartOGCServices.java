/*
 *
 * Copyright (c) 1999-2016 Luciad All Rights Reserved.
 *
 * Luciad grants you ("Licensee") a non-exclusive, royalty free, license to use,
 * modify and redistribute this software in source and binary code form,
 * provided that i) this copyright notice and license appear on all copies of
 * the software; and ii) Licensee does not utilize the software in a manner
 * which is disparaging to Luciad.
 *
 * This software is provided "AS IS," without a warranty of any kind. ALL
 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING ANY
 * IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 * NON-INFRINGEMENT, ARE HEREBY EXCLUDED. LUCIAD AND ITS LICENSORS SHALL NOT BE
 * LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING
 * OR DISTRIBUTING THE SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL LUCIAD OR ITS
 * LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT,
 * INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER
 * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF
 * OR INABILITY TO USE SOFTWARE, EVEN IF LUCIAD HAS BEEN ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGES.
 */
package samples.ogc.server;

import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.jetty.xml.XmlConfiguration;

/**
 * Sample to start the Luciad OGC server web application, consisting of an OGC WMS, WFS-T and WCS.
 *
 * Without arguments, it runs the pre-built OGC server web application available in the 'distrib' folder,
 * using the Jetty servlet engine. The main goal of this use case is to be able to quickly start an OGC
 * WMS, WFS-T and WCS server to test the client samples, without the need to open an IDE.
 *
 * When using the argument '-fromsource', it runs the OGC server web application directly from the sample source code,
 * using the Jetty servlet engine. The main goal of this use case is to ease the implementation of an
 * OGC WMS, WFS-T and/or WCS server in an IDE, as it allows you to run the services directly from the source code.
 * Please make sure to add 'build/ogc/resources' to your classpath upfront for this use case, as this folder
 * contains OGC configuration files and XML Schema's required by the services.
 *
 * Additionally, the sample can also be configured to display tray icon while the service is running,
 * by means of the property 'samples.server.trayIcon'. If this is set to true (default), the tray icon is
 * displayed.
 */
public class StartOGCServices {

  private static final String TRAY_ICON_PROPERTY = "samples.server.trayIcon";

  public static void main(String[] args) {
    boolean fromSource = false;

    if (args == null || args.length == 0) {
      System.out.println("INFO: Starting the OGC server web application available in distrib/ogc...");
      fromSource = false;
    } else if ("-fromsource".equals(args[0])) {
      System.out.println("INFO: Starting the OGC server web application from the source code available in samples/src...");
      fromSource = true;
    }

    // Jetty server initialization.
    try {
      if (fromSource) {
        // Create a Jetty server for a given port.
        Server server = new Server(8080);

        // Define a Jetty web app context for the OGC servlets.
        WebAppContext context = new WebAppContext("build/ogc/resources", "/LuciadLightspeedOGC");
        context.setSystemClasses(new String[]{"java.", "javax.servlet.", "javax.xml.", "javax.media.", "javax.el.", "org.eclipse.", "org.xml.", "org.w3c.", "org.apache.commons.logging.", "org.apache.log4j.", "com.sun."});

        // Register the web app context on the server.
        server.setHandler(context);

        // Start the server.
        server.start();

        System.out.println("INFO: The Luciad OGC server has been started. The capabilities of the various services can be requested via the following URLs:");
        String url = "http://localhost:" + server.getConnectors()[0].getPort() + context.getContextPath() + "/";
        System.out.println("- OGC WFS: " + url + "wfs?REQUEST=GetCapabilities&SERVICE=WFS");
        System.out.println("- OGC WCS: " + url + "wcs?REQUEST=GetCapabilities&SERVICE=WCS");
        System.out.println("- OGC WMS: " + url + "wms?REQUEST=GetCapabilities&SERVICE=WMS");
      } else {
        // Create a Jetty server.
        Server server = new Server();

        // Configure the server based on an XML configuration file, which uses port 8080 by default
        // and which points to the distrib/ogc folder to load the pre-built OGC server web application.
        InputStream serviceInputStream = StartOGCServices.class.getClassLoader().getResourceAsStream("samples/ogc/server/jetty_ogc_service.xml");
        XmlConfiguration configuration = new XmlConfiguration(serviceInputStream);
        configuration.configure(server);

        // Start the server.
        server.start();

        System.out.println("INFO: The Luciad OGC server has been started. You can navigate to http://localhost:" + server.getConnectors()[0].getPort() + " to access the services.");
      }

      if (Boolean.getBoolean(TRAY_ICON_PROPERTY)) {
        // Add a tray icon to stop the server from the system tray.
        createTrayIcon();
      }

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private static void createTrayIcon() {
    try {
      if (!GraphicsEnvironment.isHeadless() && SystemTray.isSupported()) {
        final TrayIcon trayIcon = new TrayIcon(createLuciadIcon());
        final SystemTray tray = SystemTray.getSystemTray();
        final PopupMenu popup = new PopupMenu();
        MenuItem stop = new MenuItem("Stop");
        stop.addActionListener(new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent e) {
            System.exit(0);
          }
        });
        popup.add(stop);
        trayIcon.setPopupMenu(popup);
        trayIcon.setToolTip("OGC Server");
        tray.add(trayIcon);
      }
    } catch (Exception e) {
      System.out.println("INFO: TrayIcon could not be added.");
    }
  }

  //Obtain the image URL
  protected static Image createLuciadIcon() throws IOException {
    return ImageIO.read(ClassLoader.getSystemResource("samples/images/luciad_tray_icon16.png"));
  }
}
