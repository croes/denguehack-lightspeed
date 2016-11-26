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
package samples.wms.client.lightweight;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Properties;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;

import com.luciad.gui.TLcdImageIcon;
import com.luciad.gui.swing.TLcdSWIcon;
import com.luciad.io.TLcdInputStreamFactory;

import samples.common.SamplePanel;

import samples.common.LuciadFrame;

/**
 * This sample demonstrates how to use the Luciad Web Map Server using Open GIS queries.
 * <p/>
 * The wmssettings_lw.cfg file should contain the url of the running version of
 * the Luciad Web Map Server with a default version of the configuration files.
 * To use this sample, adjust the wms.capabilities.cfg.xml file that configures
 * the Web Map Server so that it uses the Data directory as 'MapData Folder'.
 * <p/>
 * When these changes are made, the map in the center of the sample will be loaded
 * with the states and the rivers of the USA and with a shp file and a raster file of Washington DC.
 * This map is retrieved from the Luciad Web Map Server.
 */
public class MainPanel extends SamplePanel {

  private static final short ZOOM_MODE = 0;
  private static final short SELECT_MODE = 1;

  private static final String SERVLET_PROPERTY = "servlet.url";

  private static String PROPERTIES_FILE_NAME = "samples/wms/client/lightweight/wmssettings_lw.cfg";

  private Map fMap = new Map();
  private short fMode = ZOOM_MODE;

  private JPanel fMainPanel = new JPanel();
  private JToolBar fToolBar = new JToolBar();
  private JToggleButton fZoomButton = new JToggleButton();
  private JToggleButton fSelectButton = new JToggleButton();
  private MyToolbarActionListener fMyToolbarActionListener = new MyToolbarActionListener();
  private MyMapMouseListener fMyMapMouseListener = new MyMapMouseListener();

  public void createGUI() {
    fMainPanel.setLayout(new BorderLayout());

    String servlet_url = retrieveServletURL();
    if (isCorrectServletURL(servlet_url)) {
      fMap.setServletURL(servlet_url);
      fMainPanel.add(fMap, BorderLayout.CENTER);
    }
    initPanButtons();
    initToolBar();

    setLayout(new BorderLayout());
    add(fToolBar, BorderLayout.NORTH);
    add(fMainPanel, BorderLayout.CENTER);
  }

  private InputStream retrieveInputStream(String aSource) throws IOException {
    return new TLcdInputStreamFactory().createInputStream(aSource);
  }

  /**
   * Retrieves the url of the servlet from the properties file.
   *
   * @return a <code>String</code> representation of the url of the servlet
   *         running the Web Map Server.
   */
  private String retrieveServletURL() {
    try {
      Properties settings_properties = new Properties();
      settings_properties.load(retrieveInputStream(PROPERTIES_FILE_NAME));
      return settings_properties.getProperty(SERVLET_PROPERTY);
    } catch (IOException e) {
      System.out.println("Could not load propertiesFile [" + PROPERTIES_FILE_NAME + "].");
      e.printStackTrace();
    }
    return null;
  }

  private boolean isCorrectServletURL(String aServletPath) {
    URL test_url = null;

    String message = null;
    Exception exception = null;

    if (aServletPath == null) {
      message = "The server URL is not defined in your configuration file (" + PROPERTIES_FILE_NAME + ").\nCheck if the file contains a line like \n" + SERVLET_PROPERTY + "=http://host:port/servletname?";
    } else {
      try {
        new URL(aServletPath);
        if (aServletPath.endsWith("?")) {
          test_url = new URL(aServletPath + "REQUEST=GetCapabilities&SERVICE=WMS");
        } else {
          test_url = new URL(aServletPath + "?REQUEST=GetCapabilities&SERVICE=WMS");
        }
      } catch (MalformedURLException e) {
        message = "The server URL specified in the configuration file is malformed.";
        message += "\n  " + aServletPath;
        exception = e;
      }
      try {
        if (test_url != null) {
          InputStream test_stream = test_url.openStream();
          test_stream.close();
        }
      } catch (IOException ioe) {
        exception = ioe;
        if (ioe instanceof FileNotFoundException) {
          message = "The server URL specified in " + PROPERTIES_FILE_NAME + " is incorrect.";
          message += "\n" + aServletPath;
        } else if (ioe instanceof ConnectException) {
          message = "A problem occurred while connecting to the server. \nCheck whether your server is started. \nIf the server is up and running, the problem may be due to an error in the server URL specified in " + PROPERTIES_FILE_NAME + ":";
          message += "\n" + aServletPath;
          message += "\nTo start the LuciadLightspeed WMS server sample, run the samples.ogc.server.StartOGCServices sample.";
        } else if (ioe instanceof UnknownHostException) {
          message = "The server URL specified in " + PROPERTIES_FILE_NAME + " is incorrect, the host is unknown.";
          message += "\n" + aServletPath;
        }
      } catch (SecurityException se) {
        exception = se;
        message = "A security exception occurred while connecting to the server.\n Adapt your security settings to allow the connection.";
      }
    }
    if (message == null) {
      return true;
    } else {
      message = message + "\nNothing will be displayed on the map.";
      if (exception != null) {
        message = message + "\nCheck the console for more details.";
        System.out.println("An exception occurred while testing the URL in the sample configuration file:");
        exception.printStackTrace(System.out);
      }
      JOptionPane.showMessageDialog(this, message, "Error connecting to the LuciadLightspeed Web Map Server", JOptionPane.ERROR_MESSAGE);
      return false;
    }
  }

  private void setButtonIcon(AbstractButton aButton, String aImageName) {
    TLcdImageIcon imageIcon = new TLcdImageIcon(aImageName);
    aButton.setIcon(new TLcdSWIcon(imageIcon));
  }

  private void initToolBar() {
    // Initialize toolbar button icons.
    setButtonIcon(fZoomButton, "images/icons/zoom_16.png");
    setButtonIcon(fSelectButton, "images/icons/arrow_16.png");

    // Initialize toolbar button tooltips.
    fZoomButton.setToolTipText("Zoom: left click to zoom in, right click to zoom out");
    fSelectButton.setToolTipText("Feature info: click on a point to retrieve feature info");

    // Construct the toolbar.
    ButtonGroup buttonGroup = new ButtonGroup();
    buttonGroup.add(fZoomButton);
    buttonGroup.add(fSelectButton);

    fToolBar.add(fZoomButton);
    fToolBar.add(fSelectButton);

    fZoomButton.addActionListener(fMyToolbarActionListener);
    fSelectButton.addActionListener(fMyToolbarActionListener);

    fZoomButton.setSelected(true);

    fMap.addMouseListener(fMyMapMouseListener);

    add(fToolBar, BorderLayout.NORTH);
  }

  private void initPanButtons() {
    JButton panNorthButton = new JButton();
    setButtonIcon(panNorthButton, "images/icons/move_up_16.png");
    panNorthButton.setToolTipText("Pan the map to the north");
    JButton panEastButton = new JButton();
    setButtonIcon(panEastButton, "images/icons/move_right_16.png");
    panEastButton.setToolTipText("Pan the map to the east");
    JButton panSouthButton = new JButton();
    setButtonIcon(panSouthButton, "images/icons/move_down_16.png");
    panSouthButton.setToolTipText("Pan the map to the south");
    JButton panWestButton = new JButton();
    setButtonIcon(panWestButton, "images/icons/move_left_16.png");
    panWestButton.setToolTipText("Pan the map to the west");

    panNorthButton.setBorderPainted(false);
    panEastButton.setBorderPainted(false);
    panSouthButton.setBorderPainted(false);
    panWestButton.setBorderPainted(false);

    panNorthButton.setPreferredSize(new Dimension(25, 25));
    panEastButton.setPreferredSize(new Dimension(25, 25));
    panSouthButton.setPreferredSize(new Dimension(25, 25));
    panWestButton.setPreferredSize(new Dimension(25, 25));

    panNorthButton.setBackground(new Color(68, 71, 196));
    panEastButton.setBackground(new Color(68, 71, 196));
    panSouthButton.setBackground(new Color(68, 71, 196));
    panWestButton.setBackground(new Color(68, 71, 196));

    fMainPanel.add(panNorthButton, BorderLayout.NORTH);
    fMainPanel.add(panEastButton, BorderLayout.EAST);
    fMainPanel.add(panSouthButton, BorderLayout.SOUTH);
    fMainPanel.add(panWestButton, BorderLayout.WEST);

    panNorthButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        fMap.pan(0, fMap.getHeight() - 10);
      }
    });

    panEastButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        fMap.pan((-1) * (fMap.getWidth() - 10), 0);
      }
    });

    panSouthButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        fMap.pan(0, (-1) * (fMap.getHeight() - 10));
      }
    });

    panWestButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        fMap.pan(fMap.getWidth() - 10, 0);
      }
    });
  }

  private class MyToolbarActionListener implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      if (fZoomButton.isSelected()) {
        fMode = ZOOM_MODE;
      } else if (fSelectButton.isSelected()) {
        fMode = SELECT_MODE;
      }
    }
  }

  private class MyMapMouseListener extends MouseAdapter {
    public void mouseClicked(MouseEvent e) {
      if (fMode == ZOOM_MODE) {
        if (SwingUtilities.isLeftMouseButton(e)) {
          fMap.zoom(e.getPoint(), 2);
        } else if (SwingUtilities.isRightMouseButton(e)) {
          fMap.zoom(e.getPoint(), 0.5);
        }
      } else if (fMode == SELECT_MODE) {
        fMap.selectAt(e.getX(), e.getY());
      }
    }
  }

  // Main method
  public static void main(final String[] aArgs) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        MainPanel sample = new MainPanel();
        String map_format = null;
        String feature_info_format = null;
        for (int i = 0; i < aArgs.length; i++) {
          if (aArgs[i].equalsIgnoreCase("-mapFormat")) {
            map_format = aArgs[++i];
          } else if (aArgs[i].equalsIgnoreCase("-featureInfoFormat")) {
            feature_info_format = aArgs[++i];
          } else if (aArgs[i].equalsIgnoreCase("-config")) {
            PROPERTIES_FILE_NAME = aArgs[++i];
          }
        }

        if (map_format != null) {
          sample.fMap.setMapFormat(map_format);
        }
        if (feature_info_format != null) {
          sample.fMap.setFeatureInfoFormat(feature_info_format);
        }

        new LuciadFrame(sample, "Lightweight WMS client");
      }
    });
  }
}
