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
package samples.ogc.wcs.client;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.*;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;

import com.luciad.format.raster.TLcdGeoTIFFModelDecoder;
import samples.common.LuciadFrame;
import com.luciad.io.TLcdIOUtil;
import com.luciad.model.ILcdModel;
import com.luciad.shape.ILcdBounds;
import com.luciad.shape.shape2D.TLcdLonLatBounds;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.ILcdGXYLayerFactory;
import com.luciad.view.gxy.TLcdCompositeGXYLayerFactory;
import com.luciad.view.map.TLcdMapJPanel;

import samples.common.SampleData;
import samples.common.layerControls.swing.LayerControlPanel;
import samples.common.serviceregistry.ServiceRegistry;
import samples.common.SamplePanel;
import samples.gxy.common.ProgressUtil;
import samples.gxy.common.SampleMapJPanelFactory;
import samples.gxy.common.TitledPanel;
import samples.common.layerControls.swing.LayerControlPanelFactory2D;
import samples.gxy.common.layers.GXYDataUtil;
import samples.gxy.common.layers.GXYLayerUtil;
import samples.gxy.common.toolbar.ToolBar;

/**
 * This sample provides a simple demonstration of how to query a Web Coverage
 * Service (WCS). In the lower right corner of the window, you will find a text
 * field in which you can type the URL of a WCS. Clicking the "Query WCS
 * service" button will retrieve a list of available coverage offerings from
 * the specified WCS. The "Query coverages" combo box will be populated
 * with the names of these coverages. Next, you will find two text fields that
 * allow you to specify the desired output resolution of the coverage data.
 * Finally, clicking the "Get coverages" button will send a GetCoverage request
 * to the server, and the requested data will be added to the map.
 */
public class MainPanel extends SamplePanel {

  private TLcdMapJPanel fMapJPanel = SampleMapJPanelFactory.createMapJPanel(new TLcdLonLatBounds(-125, 25, 60.00, 30.00));

  private ILcdGXYLayer fWCSLayer = null;
  private MyLayerFactory fLayerFactory = new MyLayerFactory();
  private EditableBoundsLayer fBoundsLayer = null;

  private Vector fCoverages = new Vector();
  private String fSelectedCoverage = "";

  private JComboBox fCoverageChooser;
  private JTextField fURL, fResX, fResY;
  private JButton fGetCoverageButton;

  public void createGUI() {
    // initialize GUI components

    // Create a text field in which the user can type the WCS URL. Default to localhost.
    fURL = new JTextField("http://localhost:8080/LuciadLightspeedOGC/wcs");

    // Create a button that queries the specified WCS for its list of available coverages.
    JButton querybutton = new JButton("Query WCS service");
    querybutton.addActionListener(new QueryButtonClickListener());

    // Create a combo box with all available coverages. Selecting one will retrieve it from
    // the server and display it on the map.
    fCoverageChooser = new JComboBox(fCoverages);
    fCoverageChooser.addItemListener(new CoverageListChangeListener());

    fResX = new JTextField("512", 6);
    fResY = new JTextField("512", 6);
    fGetCoverageButton = new JButton("Get coverage");
    fGetCoverageButton.addActionListener(new GetCoverageButtonClickListener());

    JPanel urlpanel = new JPanel(new BorderLayout(5, 0));
    urlpanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
    urlpanel.add(new JLabel("Server URL"), BorderLayout.WEST);
    urlpanel.add(fURL, BorderLayout.CENTER);
    urlpanel.add(querybutton, BorderLayout.EAST);

    JPanel serverpanel = new JPanel(new GridBagLayout());
    serverpanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
    GridBagConstraints gbc = new GridBagConstraints(
        0, 0, 1, 1, 0, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0
    );

    gbc.gridx = 0;
    gbc.weightx = 0;
    serverpanel.add(new JLabel("Coverage"), gbc);
    gbc.gridx = 1;
    serverpanel.add(Box.createHorizontalStrut(5), gbc);
    gbc.gridx = 2;
    gbc.weightx = 1;
    serverpanel.add(fCoverageChooser, gbc);
    gbc.gridx = 0;

    gbc.gridy++;
    serverpanel.add(Box.createVerticalStrut(5), gbc);

    gbc.gridy++;
    gbc.gridx = 0;
    gbc.weightx = 0;
    serverpanel.add(new JLabel("Width"), gbc);
    gbc.gridx = 1;
    serverpanel.add(Box.createHorizontalStrut(5), gbc);
    gbc.gridx = 2;
    gbc.weightx = 1;
    serverpanel.add(fResX, gbc);
    gbc.gridx = 0;

    gbc.gridy++;
    serverpanel.add(Box.createVerticalStrut(5), gbc);

    gbc.gridy++;
    gbc.gridx = 0;
    gbc.weightx = 0;
    serverpanel.add(new JLabel("Height"), gbc);
    gbc.gridx = 1;
    serverpanel.add(Box.createHorizontalStrut(5), gbc);
    gbc.gridx = 2;
    gbc.weightx = 1;
    serverpanel.add(fResY, gbc);
    gbc.gridx = 0;

    gbc.gridy++;
    serverpanel.add(Box.createVerticalStrut(5), gbc);

    gbc.gridy++;
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    serverpanel.add(fGetCoverageButton, gbc);
    gbc.gridwidth = 1;

    // Create the default toolbar and layer control.
    ToolBar tool_bar = new ToolBar(fMapJPanel, true, this);
    LayerControlPanel layer_control = LayerControlPanelFactory2D.createDefaultGXYLayerControlPanel(fMapJPanel);

    // Create a titled panel around the map panel
    TitledPanel map_panel = TitledPanel.createTitledPanel(
        "Map", fMapJPanel, TitledPanel.NORTH | TitledPanel.EAST
    );

    JPanel url_panel = TitledPanel.createTitledPanel(
        "WCS service settings", urlpanel
    );

    JPanel coverage_panel = TitledPanel.createTitledPanel(
        "Query coverage", serverpanel
    );

    JPanel east_panel = new JPanel(new BorderLayout());
    east_panel.add(BorderLayout.NORTH, coverage_panel);
    east_panel.add(BorderLayout.CENTER, layer_control);

    setLayout(new BorderLayout());
    add(BorderLayout.NORTH, tool_bar);
    add(BorderLayout.CENTER, map_panel);
    add(BorderLayout.EAST, east_panel);
    add(BorderLayout.SOUTH, url_panel);
  }

  /**
   * Loads the world data.
   */
  public void addData() {
    ServiceRegistry.getInstance().register(fLayerFactory, ServiceRegistry.HIGH_PRIORITY);

    // Add a background layer
    GXYDataUtil.instance().model(SampleData.COUNTRIES).layer().label("Countries").selectable(false).addToView(fMapJPanel);

    fBoundsLayer = new EditableBoundsLayer(-90, 30, 20, 20);
    GXYLayerUtil.addGXYLayer(fMapJPanel, fBoundsLayer, true, false);

    JDialog progress = ProgressUtil.createProgressDialog(this, "Querying WCS service...");
    ProgressUtil.showDialog(progress);
    try {
      buildCoverageOfferingList(fURL.getText());
      ProgressUtil.hideDialog(progress);
    } catch (IOException e) {
      ProgressUtil.hideDialog(progress);
      String[] message = new String[]{
          e.getMessage(),
          "An exception occurred while connecting to the WCS.",
          "Please check if the WCS is up and running.",
          "To start the LuciadLightspeed WCS server sample, ",
          "run the samples.ogc.server.StartOGCServices sample."
      };
      JOptionPane.showMessageDialog(null, message);
    }
  }

  private String createGetCapabilities() {
    return "?SERVICE=WCS&VERSION=1.0.0&REQUEST=GetCapabilities";
  }

  private String createGetCoverage(String aCoverage, int aWidth, int aHeight, ILcdBounds aBBOX) {

    return "?SERVICE=WCS&VERSION=1.0.0&REQUEST=GetCoverage&FORMAT=GeoTIFF&COVERAGE=" + aCoverage +
           "&CRS=EPSG:4326&WIDTH=" + aWidth + "&HEIGHT=" + aHeight + "&BBOX=" +
           aBBOX.getLocation().getX() + "," + aBBOX.getLocation().getY() + "," +
           (aBBOX.getLocation().getX() + aBBOX.getWidth()) + "," +
           (aBBOX.getLocation().getY() + aBBOX.getHeight());
  }

  private InputStream doGET(String aURL) throws IOException {
    TLcdIOUtil io_util = new TLcdIOUtil();
    io_util.setSourceName(aURL);
    return io_util.retrieveInputStream();
  }

  private void buildCoverageOfferingList(String aServletURL) throws IOException {
    fCoverages.clear();
    doLayout();

    SAXBuilder builder = new SAXBuilder();
    Document doc = null;
    try {
      // Issue a GetCapabilities request to retrieve a list of available coverages from the WCS.
      doc = builder.build(doGET(aServletURL + createGetCapabilities()));

      Element root = doc.getRootElement();
      Namespace wcs_ns = Namespace.getNamespace("wcs", "http://www.opengis.net/wcs");

      // Extract the coverage list from the returned XML document.
      Element coverageElement = root.getChild("ContentMetadata", wcs_ns);
      if (coverageElement != null) {
        List coverages = coverageElement.getChildren("CoverageOfferingBrief", wcs_ns);
        for (int i = 0; i < coverages.size(); i++) {
          Element coverage = (Element) coverages.get(i);
          fCoverages.add(coverage.getChildText("name", wcs_ns));
        }
      } else {
        throw new IOException("Can't get coverage offering list from " + aServletURL);
      }

      fCoverageChooser.setEnabled(true);
      fResX.setEnabled(true);
      fResY.setEnabled(true);
      fGetCoverageButton.setEnabled(true);

      // Redo the layout so the coverage list combo box gets updated.
      fCoverageChooser.setModel(new DefaultComboBoxModel(fCoverages));
      fCoverageChooser.setSelectedIndex(-1);

      if (fCoverages.size() > 0) {
        fSelectedCoverage = (String) fCoverages.get(0);
        fCoverageChooser.setSelectedItem(fCoverages.get(0));
      }

      doLayout();
    } catch (Exception e) {
      fCoverageChooser.setEnabled(false);
      fResX.setEnabled(false);
      fResY.setEnabled(false);
      fGetCoverageButton.setEnabled(false);

      System.err.println(e.getMessage());
      throw new IOException(aServletURL + " does not appear to point to a valid WCS server");
    }
  }

  private void createWCSLayer(String aServletURL, String aCoverage, int aWidth, int aHeight, ILcdBounds aBBOX) {

    try {
      if (fWCSLayer != null) {
        GXYLayerUtil.removeGXYLayer(fMapJPanel, fWCSLayer, true);
        fWCSLayer = null;
      }

      // Open a connection to the WCS
      URL url = new URL(aServletURL + createGetCoverage(aCoverage, aWidth, aHeight, aBBOX));
      HttpURLConnection connection = (HttpURLConnection) url.openConnection();

      int response = connection.getResponseCode();
      String contentType = connection.getContentType();

      // If the HTTP response code is not ok (i.e., not equal to 200)
      // and if the content is not an OGC service exception,
      // we assume a transport-level issue.
      if (response != HttpURLConnection.HTTP_OK && !"application/vnd.ogc.se_xml".equals(contentType)) {
         throw new IOException("Unexpected response received from the server: response code " + response + ", content type " + contentType );
      }
      // In all other cases, we decode the content of the response and continue
      // with its interpretation.
      else {
        InputStream inputStream;

        // If the HTTP response code is not ok (i.e., not equal to 200),
        // the content of the response needs to be retrieved via the HTTP URL connection's error stream.
        if (response != HttpURLConnection.HTTP_OK) {
          inputStream = connection.getErrorStream();
        }
        else {
          inputStream = connection.getInputStream();
        }

        // Check the content type: the WCS may have returned a service exception
        if ("application/vnd.ogc.se_xml".equals(contentType)) {
        // A service exception is returned: show its content in a dialog
        List<String> exceptionReportAsString = new ArrayList<>();
        exceptionReportAsString.add("Could not retrieve " + aCoverage + ".");
        exceptionReportAsString.add("The WCS responded with the following message:");

        Document serviceExceptionReportDocument = new SAXBuilder().build(inputStream);
        Element serviceExceptionReport = serviceExceptionReportDocument.getRootElement();
        for (int i = 0; i < serviceExceptionReport.getChildren().size(); i++) {
          Element serviceException = (Element) serviceExceptionReport.getChildren().get(i);
          exceptionReportAsString.add(serviceException.getValue());
          }

        JOptionPane.showMessageDialog(null, exceptionReportAsString.toArray(new String[exceptionReportAsString.size()]));
        } else {
          // Otherwise, decode the result and add it to the map
          TLcdGeoTIFFModelDecoder decoder = new TLcdGeoTIFFModelDecoder();

          File temp = File.createTempFile("wcs_", ".tif");
          FileOutputStream f_out = new FileOutputStream(temp);
          byte buf[] = new byte[1024];
          int c = inputStream.read(buf);
          while (c != -1) {
            f_out.write(buf, 0, c);
            c = inputStream.read(buf);
          }
          f_out.flush();
          f_out.close();
          inputStream.close();

          ILcdModel model = decoder.decode(temp.getAbsolutePath());
          temp.deleteOnExit();

          fLayerFactory.setLayerLabel(aCoverage);
          TLcdCompositeGXYLayerFactory gxy_layer_factory = new TLcdCompositeGXYLayerFactory(ServiceRegistry.getInstance().query(ILcdGXYLayerFactory.class));
          fWCSLayer = gxy_layer_factory.createGXYLayer(model);
          GXYLayerUtil.addGXYLayer(fMapJPanel, fWCSLayer, true, false);
        }
      }
    } catch (Exception ex) {
      String[] message = new String[]{
          "An exception occurred while retrieving coverage " + aCoverage + ".",
          "Type of exception: " + ex.getClass().getName(),
          "Message: " + ex.getMessage(),
      };

      JOptionPane.showMessageDialog(null, message);
    }
  }

  /**
   * Listener for the "Query WCS service" button.
   */
  private class QueryButtonClickListener implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      JDialog progress = ProgressUtil.createProgressDialog(MainPanel.this, "Querying WCS service...");
      ProgressUtil.showDialog(progress);
      try {
        buildCoverageOfferingList(fURL.getText());
        ProgressUtil.hideDialog(progress);
      } catch (IOException ex) {
        ProgressUtil.hideDialog(progress);
        JOptionPane.showMessageDialog(null, ex.getMessage());
      }
    }
  }

  /**
   * Listener for the "Queryable coverages" combo box.
   */
  private class CoverageListChangeListener implements ItemListener {
    public void itemStateChanged(ItemEvent e) {
      if (e.getStateChange() == ItemEvent.SELECTED) {
        fSelectedCoverage = (String) e.getItem();
      }
    }
  }

  /**
   * Listener for the "Get coverages" button.
   */
  private class GetCoverageButtonClickListener implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      createWCSLayer(
          fURL.getText(),
          fSelectedCoverage,
          Integer.valueOf(fResX.getText()).intValue(),
          Integer.valueOf(fResY.getText()).intValue(),
          fBoundsLayer.getLonLatBounds()
      );
    }
  }

  // Main method
  public static void main(final String[] aArgs) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        new LuciadFrame(new MainPanel(), "WCS client");
      }
    });
  }
}
