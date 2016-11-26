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
package samples.ogc.filter.xml;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import com.luciad.format.gml31.model.TLcdGML31AbstractFeature;
import com.luciad.format.xml.TLcdXMLName;
import com.luciad.io.ILcdInputStreamFactory;
import com.luciad.io.TLcdInputStreamFactory;
import com.luciad.model.ILcdModel;
import com.luciad.ogc.filter.ILcdOGCFeatureIDRetriever;
import com.luciad.ogc.filter.evaluator.ILcdEvaluatorFunction;
import com.luciad.ogc.filter.evaluator.ILcdPropertyRetrieverProvider;
import com.luciad.ogc.filter.evaluator.TLcdOGCFilterContext;
import com.luciad.ogc.filter.evaluator.TLcdOGCFilterEvaluator;
import com.luciad.ogc.filter.evaluator.TLcdPropertyRetrieverUtil;
import com.luciad.ogc.filter.model.TLcdOGCFilter;
import com.luciad.ogc.filter.xml.TLcdOGCFilterDecoder;
import com.luciad.reference.ILcdGeoReference;
import com.luciad.util.ILcdFilter;
import com.luciad.view.gxy.TLcdGXYLayer;
import com.luciad.view.gxy.TLcdGXYPainterColorStyle;

import samples.gxy.common.GXYSample;
import samples.gxy.common.TitledPanel;
import samples.gxy.common.layers.GXYDataUtil;
import samples.gxy.common.layers.factories.GXYUnstyledLayerFactory;

/**
 * This sample demonstrates the ability to filter GML data using the OGC Filter XML decoding and evaluation facilities.
 */
public class MainPanel extends GXYSample {

  private static final String FILTER_SOURCE = "http://www.luciad.com/sample/filter.xml";

  private static final String[] FILTERS = new String[]{
      "And.xml", "BBOX.xml", "IsEqualUpper.xml", "IsGreaterThan.xml", "IsGreaterThan2.xml", "IsLike.xml", "ObjectId.xml"
  };

  private TLcdGXYLayer fGXYLayer;
  private JTextArea fFilterArea = new JTextArea(10, 50);
  private TLcdOGCFilterContext fFilterContext;
  private TLcdOGCFilterEvaluator fFilterEvaluator;
  private TLcdOGCFilterDecoder fFilterDecoder;

  private JButton fApplyButton;

  @Override
  public void addData() {
    // Make the grid layer invisible for better visibility of sample layers.
    getView().getGridLayer().setVisible(false);
    // Initialize decoders before loading the data.
    initializeDecoders();

    GXYUnstyledLayerFactory factory = new GXYUnstyledLayerFactory();
    factory.setLineStyle(new TLcdGXYPainterColorStyle(Color.gray, Color.red));
    factory.setFillStyle(new TLcdGXYPainterColorStyle(new Color(222, 205, 139), Color.red));
    fGXYLayer = (TLcdGXYLayer) GXYDataUtil.instance()
                                          .model("Data/Gml/world.gml")
                                          .layer(factory)
                                          .label("Filtered")
                                          .addToView(getView()).getLayer();
    fFilterContext = createFilterContext(fGXYLayer.getModel());

    SwingUtilities.invokeLater(new Runnable() {

      public void run() {
        fApplyButton.setEnabled(true);
      }

    });
  }

  private void initializeDecoders() {
    // Creates the filter evaluator
    fFilterEvaluator = createFilterEvaluator();
    // Creates the filter decoder
    fFilterDecoder = createFilterDecoder();
  }

  @Override
  protected JPanel createBottomPanel() {
    // The load button will allow the user to load a predefined xml in the text area.
    JButton loadButton = new JButton("Load");
    loadButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        loadFilter();
      }
    });

    // The apply button will allow to apply the filter contained in the text area.
    fApplyButton = new JButton("Apply");
    fApplyButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        applyFilter();
      }
    });
    fApplyButton.setEnabled(false);

    // Create the bottom right panel the apply button and the filter list.
    JPanel filter_right_top_panel = new JPanel(new GridLayout(2, 1, 0, 5));
    filter_right_top_panel.add(loadButton);
    filter_right_top_panel.add(fApplyButton);

    JPanel filter_right_panel = new JPanel(new BorderLayout());
    filter_right_panel.add(BorderLayout.NORTH, filter_right_top_panel);

    JPanel filter_panel = new JPanel(new BorderLayout(5, 0));
    filter_panel.add(BorderLayout.CENTER, new JScrollPane(fFilterArea));
    filter_panel.add(BorderLayout.EAST, filter_right_panel);

    return TitledPanel.createTitledPanel("Filter settings", filter_panel);
  }


  private TLcdOGCFilterEvaluator createFilterEvaluator() {
    // Create the filter evaluator
    TLcdOGCFilterEvaluator filterEvaluator = new TLcdOGCFilterEvaluator();

    // Register a custom function : UPPER
    filterEvaluator.registerFunction(
        TLcdXMLName.getInstance(null, "UPPER"),
        new ILcdEvaluatorFunction() {
          public Object apply(Object[] aArguments,
                              Object aCurrentObject,
                              TLcdOGCFilterContext aOGCFilterContext) {
            return (aArguments.length > 0 && aArguments[0] != null) ?
                   aArguments[0].toString().toUpperCase() : null;
          }

          public int getArgumentCount() {
            return 1;
          }
        }
    );
    return filterEvaluator;
  }


  private TLcdOGCFilterContext createFilterContext(ILcdModel aModel) {
    // Create the feature ID retriever.
    ILcdOGCFeatureIDRetriever featureIDRetriever = new MyFeatureIDRetriever();

    // Get the default property retriever provider for a GML model.
    ILcdPropertyRetrieverProvider propertyRetrieverProvider = createPropertyRetrieverProvider(aModel);

    // Create the filter context.
    return new TLcdOGCFilterContext(
        (ILcdGeoReference) aModel.getModelReference(),
        featureIDRetriever,
        propertyRetrieverProvider
    );
  }

  /**
   * Gets the filter evaluator.
   *
   * @return the filter evaluator.
   */
  private TLcdOGCFilterEvaluator getFilterEvaluator() {
    return fFilterEvaluator;
  }

  /**
   * Gets the filter context.
   *
   * @return the filter context.
   */
  private TLcdOGCFilterContext getFilterContext() {
    return fFilterContext;
  }

  /**
   * Gets the filter decoder.
   *
   * @return the filter decoder.
   */
  private TLcdOGCFilterDecoder getFilterDecoder() {
    return fFilterDecoder;
  }

  /**
   * Converts the given <code>InputStream<code/> to a String.
   */
  public static String convertToString(InputStream aInputStream) throws IOException {
    BufferedReader reader = new BufferedReader(new InputStreamReader(aInputStream));
    StringBuffer stringBuffer = new StringBuffer();
    String line = null;
    try {
      while ((line = reader.readLine()) != null) {
        stringBuffer.append(line + "\n");
      }
    } finally {
      aInputStream.close();
    }
    return stringBuffer.toString();
  }

  /**
   * Asks the user to select a predefined xml filter.
   */
  private void loadFilter() {
    Object object = JOptionPane.showInputDialog(
        this,
        null,
        "Select a predefined XML filter.",
        JOptionPane.QUESTION_MESSAGE,
        null,
        FILTERS,
        null
    );
    if (object instanceof String) {
      try {
        InputStream is = new TLcdInputStreamFactory().createInputStream("" + "Data/filter/" + object);
        fFilterArea.setText(convertToString(is));
      } catch (IOException e) {
        throw new RuntimeException(e.getMessage(), e);
      }
    }
  }

  /**
   * Applies the filter to the country layer.
   */
  private void applyFilter() {
    // get the filter text from the text area
    String text = fFilterArea.getText();

    ILcdFilter objectFilter;
    if (text.trim().length() > 0) {

      try {
        // we use a URL for the xml source name to make it work in a browser (otherwise the XML parser will try to
        // get an absolute file path and as such access the user.dir system property which is
        // not allowed for an applet)
        objectFilter = createObjectFilter(FILTER_SOURCE);
      } catch (Exception e) {
        JOptionPane.showMessageDialog(
            this,
            "The XML Filter cannot be applied : " + e.getMessage(),
            "Error",
            JOptionPane.ERROR_MESSAGE
        );
        throw new RuntimeException(e);
      }

      // applies the ILcdFilter to the layer.
      fGXYLayer.setFilter(objectFilter);
    }
  }

  /**
   * Creates a filter decoder that can decode XML document stored in Strings.
   *
   * @return the filter decoder.
   */
  private TLcdOGCFilterDecoder createFilterDecoder() {
    final TLcdOGCFilterDecoder filterDecoder = new TLcdOGCFilterDecoder();

    filterDecoder.setInputStreamFactory(

        new ILcdInputStreamFactory() {
          ILcdInputStreamFactory fDelegate = filterDecoder.getInputStreamFactory();

          public InputStream createInputStream(String aString) throws IOException {
            if (FILTER_SOURCE.equals(aString)) {
              return new ByteArrayInputStream(MainPanel.this.fFilterArea.getText().getBytes("UTF-8")); //If no encoding is specified, the XML specification requires the parser to assume UTF-8.
            } else {
              return fDelegate.createInputStream(aString);
            }
          }
        });
    return filterDecoder;
  }


  private ILcdFilter createObjectFilter(String aXMLSource) throws IOException {

    // Get the filter decoder
    TLcdOGCFilterDecoder filterDecoder = getFilterDecoder();

    // Decode the XML source into an instance of the OGC filter API.
    TLcdOGCFilter ogcFilter = (TLcdOGCFilter) filterDecoder.decode(aXMLSource);

    // Get the filter evaluation context.
    TLcdOGCFilterContext filterContext = getFilterContext();

    // Get the filter evaluator.
    TLcdOGCFilterEvaluator filterEvaluator = getFilterEvaluator();

    // Build the ILcdFilter instance from the OGC filter.
    return filterEvaluator.buildFilter(ogcFilter, filterContext);
  }

  /**
   * Creates a property retreiver provider for a given model.
   *
   * @param aModel the model for which to create a property retriever provider.
   * @return a property retreiver provider for a given model.
   */
  private ILcdPropertyRetrieverProvider createPropertyRetrieverProvider(ILcdModel aModel) {
    return TLcdPropertyRetrieverUtil.getDefaultPropertyRetrieverProvider(aModel);
  }

  /**
   * This class is responsible for retrieving a feature ID from a given object.
   * In this case the given object is assumed to be a GML object and the feature ID is obviously the GML object ID.
   */
  private static class MyFeatureIDRetriever implements ILcdOGCFeatureIDRetriever {
    public String retrieveFeatureID(Object aObject) {
      TLcdGML31AbstractFeature gmlObject = (TLcdGML31AbstractFeature) aObject;
      return gmlObject.getId();
    }
  }

  public static void main(final String[] args) {
    startSample(MainPanel.class, "Filtering GML model");
  }
}
