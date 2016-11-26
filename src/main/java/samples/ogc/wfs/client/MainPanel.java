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
package samples.ogc.wfs.client;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import javax.swing.*;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;

import com.luciad.format.gml31.xml.TLcdGML31ModelDecoder;
import com.luciad.io.TLcdIOUtil;
import com.luciad.model.ILcdModel;
import com.luciad.shape.ILcdBounds;
import com.luciad.shape.shape2D.TLcdLonLatBounds;
import com.luciad.view.gxy.ILcdGXYLayer;

import samples.gxy.common.GXYSample;
import samples.gxy.common.ProgressUtil;
import samples.gxy.common.TitledPanel;
import samples.gxy.common.layers.GXYLayerUtil;
import samples.gxy.common.layers.factories.GXYUnstyledLayerFactory;
import samples.gxy.concurrent.painting.AsynchronousLayerFactory;

/**
 * This sample provides a simple demonstration of how to query a Web Feature
 * Service (WFS). In the lower right corner of the window, you will find a text
 * field in which you can type the URL of a WFS. Clicking the "Query"
 * button will retrieve a list of available feature types from the specified WFS.
 * The "Query features" combo box will be populated with these feature types.
 * Selecting a type from the list will trigger a GetFeature request, and the
 * selected feature will be displayed on the map. Each GetFeature request is
 * equipped with a default {@link #MAX_FEATURES} parameter value, to avoid
 * overloading a WFS with too large data requests,
 */
public class MainPanel extends GXYSample {

  // Default maximum amount of features to be retrieved in a GetFeature request,
  // to avoid requesting too much data at once.
  private static final int MAX_FEATURES = 3500;

  private ILcdGXYLayer fWFSLayer = null;

  private Vector<String> fFeatureTypes = new Vector<>();

  private JTextField fURL;
  private JComboBox<String> fFeatureTypeListComboBox;

  @Override
  protected ILcdBounds getInitialBounds() {
    return new TLcdLonLatBounds(-125, 25, 60.00, 30.00);
  }

  @Override
  protected Component[] createToolBars() {
    List<Component> toolBars = new ArrayList<>();
    toolBars.addAll(Arrays.asList(super.createToolBars()));
    toolBars.add(createWFSURLPanel());
    return toolBars.toArray(new Component[toolBars.size()]);
  }

  private JPanel createWFSURLPanel() {
    // Create a text field in which the user can type the WFS URL. Default to localhost.
    fURL = new JTextField("http://localhost:8080/LuciadLightspeedOGC/wfs");

    // Create a button that queries the specified WFS for its list of available feature types.
    JButton querybutton = new JButton("Query");
    querybutton.addActionListener(new QueryButtonClickListener(fURL));
    JPanel urlpanel = new JPanel(new BorderLayout(5, 0));
    urlpanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
    urlpanel.add(new JLabel("Server URL"), BorderLayout.WEST);
    urlpanel.add(fURL, BorderLayout.CENTER);
    urlpanel.add(querybutton, BorderLayout.EAST);
    return TitledPanel.createTitledPanel("WFS service", urlpanel);
  }

  @Override
  protected JPanel createSettingsPanel() {
    // Create a combo box with all available feature types. Selecting one will retrieve it from
    // the server and display it on the map.
    fFeatureTypeListComboBox = new JComboBox<>(fFeatureTypes);
    fFeatureTypeListComboBox.addItemListener(new FeatureTypeListChangeListener());

    JPanel serverpanel = new JPanel(new BorderLayout());
    serverpanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
    serverpanel.add(fFeatureTypeListComboBox, BorderLayout.CENTER);

    return TitledPanel.createTitledPanel("Query features", serverpanel);
  }

  public void addData() throws IOException {
    super.addData();

    JDialog progress = ProgressUtil.createProgressDialog(this, "Querying WFS service...");
    ProgressUtil.showDialog(progress);
    try {
      buildFeatureTypeList(fURL.getText());
      if (fFeatureTypes.size() > 0) {
        createWFSLayer(fURL.getText(), fFeatureTypes.get(0));
        fFeatureTypeListComboBox.setSelectedItem(fFeatureTypes.get(0));
      }
      ProgressUtil.hideDialog(progress);
    } catch (IOException e) {
      ProgressUtil.hideDialog(progress);
      String[] message = new String[]{
          e.getMessage(),
          "An exception occurred while connecting to the WFS.",
          "Please check if the WFS is up and running.",
          "To start the LuciadLightspeed WFS server sample, ",
          "run the samples.ogc.server.StartOGCServices sample."
      };
      JOptionPane.showMessageDialog(this, message);
    }
  }

  private String createGetCapabilities() {
    return "?service=WFS&version=1.1.0&request=GetCapabilities";
  }

  private String createGetFeature(String aFeatureType) {
    return "?service=WFS&version=1.1.0&maxFeatures=" + MAX_FEATURES + "&request=GetFeature&outputFormat=text/xml;%20subtype=gml/3.1.1&typeName=" + aFeatureType;
  }

  private InputStream doGET(String aURL) throws IOException {
    TLcdIOUtil io_util = new TLcdIOUtil();
    io_util.setSourceName(aURL);
    return io_util.retrieveInputStream();
  }


  private void buildFeatureTypeList(String aServletURL) throws IOException {
    fFeatureTypes.clear();
    doLayout();

    SAXBuilder builder = new SAXBuilder();
    try {
      // Issue a GetCapabilities request to retrieve a list of available feature types from the WFS.
      Document doc = builder.build(doGET(aServletURL + createGetCapabilities()));

      Element root = doc.getRootElement();
      Namespace wfs_ns = Namespace.getNamespace("wfs", "http://www.opengis.net/wfs");

      // Extract the feature type list from the returned XML document.
      Element featuretypelist = root.getChild("FeatureTypeList", wfs_ns);
      if (featuretypelist != null) {
        List<?> featuretypes = featuretypelist.getChildren("FeatureType", wfs_ns);
        for (int i = 0; i < featuretypes.size(); i++) {
          Element featuretype = (Element) featuretypes.get(i);
          fFeatureTypes.add(featuretype.getChildText("Name", wfs_ns));
        }
      } else {
        throw new IOException("Can't get feature type list from " + aServletURL);
      }

      // Redo the layout so the feature list combo box gets updated.
      fFeatureTypeListComboBox.setModel(new DefaultComboBoxModel<>(fFeatureTypes));
      fFeatureTypeListComboBox.setSelectedIndex(-1);
      doLayout();
    } catch (Exception e) {
      System.err.println(e.getMessage());
      throw new IOException(aServletURL + " does not appear to point to a valid WFS server");
    }
  }

  private void createWFSLayer(String aServletURL, String aFeatureType) {

    try {
      // Remove the previous WFS layer on the AWT event dispatching thread.
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          if (fWFSLayer != null) {
            GXYLayerUtil.removeGXYLayer(getView(), fWFSLayer, false);
            fWFSLayer = null;
          }
        }
      });

      TLcdGML31ModelDecoder decoder = new TLcdGML31ModelDecoder();

      String url = aServletURL + createGetFeature(aFeatureType);
      ILcdModel model = decoder.decode(url);

      GXYUnstyledLayerFactory layerFactory = new GXYUnstyledLayerFactory();
      // Paint the layer asynchronously so it does not block the event thread.
      fWFSLayer = AsynchronousLayerFactory.createAsynchronousLayer(layerFactory.createGXYLayer(model));
      fWFSLayer.setLabel(aFeatureType);

      // Add the new WFS layer on the AWT event dispatching thread.
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          GXYLayerUtil.addGXYLayer(getView(), fWFSLayer, true, false);
        }
      });
    } catch (IOException ex) {
      System.out.println("Could not retrieve " + aFeatureType + ": " + ex.getMessage());
      ex.printStackTrace();
    }
  }

  /**
   * Listener for the "Query" button.
   */
  private class QueryButtonClickListener implements ActionListener {

    private JTextField fURL;

    public QueryButtonClickListener(JTextField aURL) {
      fURL = aURL;
    }

    public void actionPerformed(ActionEvent e) {
      JDialog progress = ProgressUtil.createProgressDialog(MainPanel.this, "Querying WFS service...");
      ProgressUtil.showDialog(progress);
      try {
        buildFeatureTypeList(fURL.getText());
        ProgressUtil.hideDialog(progress);
      } catch (IOException ex) {
        ProgressUtil.hideDialog(progress);
        JOptionPane.showMessageDialog(MainPanel.this, ex.getMessage());
      }
    }
  }

  /**
   * Listener for the "Queryable features" combo box.
   */
  private class FeatureTypeListChangeListener implements ItemListener {
    public void itemStateChanged(final ItemEvent e) {
      if (e.getStateChange() == ItemEvent.SELECTED) {
        // Load the WFS layer in a background thread.
        Thread createWFSLayer = new Thread() {
          @Override
          public void run() {
            String feature = (String) e.getItem();
            createWFSLayer(fURL.getText(), feature);
          }
        };
        createWFSLayer.start();
      }
    }
  }

  public static void main(final String[] aArgs) {
    startSample(MainPanel.class, "WFS client");
  }
}
