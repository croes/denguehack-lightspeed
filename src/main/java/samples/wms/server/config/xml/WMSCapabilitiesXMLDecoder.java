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
package samples.wms.server.config.xml;

import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;

import com.luciad.gui.TLcdImageIcon;
import com.luciad.gui.TLcdSymbol;
import com.luciad.io.TLcdIOUtil;
import com.luciad.ogc.ows.model.TLcdOWSOnlineResource;
import com.luciad.util.logging.ILcdLogger;
import com.luciad.util.logging.TLcdLoggerFactory;
import com.luciad.wms.server.ILcdModelProvider;
import com.luciad.wms.server.ILcdWMSCapabilitiesDecoder;
import com.luciad.wms.server.model.ALcdWMSCapabilities;
import com.luciad.wms.server.model.ALcdWMSLayer;
import com.luciad.wms.server.model.TLcdWMSCapabilities;
import com.luciad.wms.server.model.TLcdWMSDimension;
import com.luciad.wms.server.model.TLcdWMSDimensionExtent;
import com.luciad.wms.server.model.TLcdWMSLayer;
import com.luciad.wms.server.model.TLcdWMSLayerStyle;
import com.luciad.wms.server.model.TLcdWMSServiceMetaData;
import com.luciad.wms.server.model.TLcdWMSURL;

/**
 * Decodes the WMS server sample XML configuration file into a <code>ALcdWMSCapabilities</code> object.
 */
public class WMSCapabilitiesXMLDecoder implements ILcdWMSCapabilitiesDecoder {

  private static ILcdLogger sLogger = TLcdLoggerFactory.getLogger(WMSCapabilitiesXMLDecoder.class.getName());

  private ILcdModelProvider fModelProvider;

  /**
   * Creates a new <code>WMSCapabilitiesXMLDecoder</code> instance.
   */
  public WMSCapabilitiesXMLDecoder() {
    this(null);
  }

  /**
   * Creates a new <code>WMSCapabilitiesXMLDecoder</code> instance with a
   * given model provider. This model provider is used to retrieve the bounding boxes
   * of the served data, to be able to set accurate bounding box information
   * on the advertised WMS layers.
   */
  public WMSCapabilitiesXMLDecoder(ILcdModelProvider aModelProvider) {
    fModelProvider = aModelProvider;
  }

  @Override
  public ALcdWMSCapabilities decodeWMSCapabilities(String aConfigurationSourceName) throws IOException {
    // Parse the XML file.
    SAXBuilder saxBuilder = new SAXBuilder();
    Document document;
    try {
      TLcdIOUtil ioUtil = new TLcdIOUtil();
      ioUtil.setSourceName(aConfigurationSourceName);
      document = saxBuilder.build(ioUtil.retrieveInputStream());
    } catch (JDOMException e) {
      throw new IOException("Error parsing " + aConfigurationSourceName);
    }

    // Iterate over the XML elements, parse them and populate a capabilities instance.
    Element xmlRootElement = document.getRootElement();
    if ("WMS_Capabilities_config".equals(xmlRootElement.getName())) {
      Attribute xmlVersion = xmlRootElement.getAttribute("version");
      if (xmlVersion == null || xmlVersion.getValue() == null) {
        throw new IOException("Missing version number in configuration file.");
      }
      if (!xmlVersion.getValue().contains("1.0")) {
        throw new IOException("Version number should be 1.0 !");
      }

      return decodeWMSCapabilities(xmlRootElement);
    } else {
      throw new IOException("Unsupported XML format: unrecognized XML root element " + xmlRootElement.getName());
    }
  }

  private ALcdWMSCapabilities decodeWMSCapabilities(Element aXmlCapabilitiesElement) {
    TLcdWMSCapabilities capabilities = new TLcdWMSCapabilities();

    for (Object xmlElementObject : aXmlCapabilitiesElement.getChildren()) {
      Element xmlElement = (Element) xmlElementObject;
      if ("Service".equals(xmlElement.getName())) {
        decodeWMSServiceSFCT(xmlElement, capabilities);
      } else if ("Output".equals(xmlElement.getName())) {
        decodeWMSOutputSFCT(xmlElement, capabilities);
      } else if ("MapData".equals(xmlElement.getName())) {
        decodeWMSMapDataSFCT(xmlElement, capabilities);
      } else if ("Root".equals(xmlElement.getName())) {
        decodeWMSRootLayerSFCT(xmlElement, capabilities);
      }
    }

    return capabilities;
  }

  private void decodeWMSServiceSFCT(Element aXmlServiceElement, TLcdWMSCapabilities aWMSCapabilities) {
    TLcdWMSServiceMetaData serviceMetaData = new TLcdWMSServiceMetaData();

    for (Object xmlElementObject : aXmlServiceElement.getChildren()) {
      Element xmlElement = (Element) xmlElementObject;
      if ("Name".equals(xmlElement.getName())) {
        if (!((xmlElement.getValue()).equals("OGC:WMS") || (xmlElement.getValue()).equals("WMS"))) {
          sLogger.warn("The WMS specification enforces the name to be used dependent on the version of the protocol.\nReplacing [" + xmlElement.getValue() + "] by this value.");
        }
      } else if ("Title".equals(xmlElement.getName())) {
        serviceMetaData.setServiceTitle(xmlElement.getValue());
      } else if ("Abstract".equals(xmlElement.getName())) {
        serviceMetaData.setServiceAbstract(xmlElement.getValue());
      } else if ("LayerLimit".equals(xmlElement.getName())) {
        serviceMetaData.setLayerLimit(Integer.parseInt(xmlElement.getValue()));
      } else if ("MaxWidth".equals(xmlElement.getName())) {
        serviceMetaData.setMaxWidth(Integer.parseInt(xmlElement.getValue()));
      } else if ("MaxHeight".equals(xmlElement.getName())) {
        serviceMetaData.setMaxHeight(Integer.parseInt(xmlElement.getValue()));
      } else if ("KeywordList".equals(xmlElement.getName())) {
        for (Object xmlKeywordElement : xmlElement.getChildren()) {
          serviceMetaData.addKeyword(((Element) xmlKeywordElement).getValue());
        }
      } else if ("OnlineResource".equals(xmlElement.getName())) {
        serviceMetaData.setOnlineResource(new TLcdWMSURL(new TLcdOWSOnlineResource(xmlElement.getAttributeValue("href", Namespace.getNamespace("http://www.w3.org/1999/xlink"))), null, null, null));
      }
    }

    aWMSCapabilities.setWMSServiceMetaData(serviceMetaData);
  }

  private void decodeWMSOutputSFCT(Element aXmlOutputElement, TLcdWMSCapabilities aWMSCapabilities) {
    for (Object xmlElementObject : aXmlOutputElement.getChildren()) {
      Element xmlElement = (Element) xmlElementObject;
      if ("Format".equals(xmlElement.getName())) {
        aWMSCapabilities.addMapFormat(xmlElement.getValue());
      } else if ("FeatureInfoFormat".equals(xmlElement.getName())) {
        aWMSCapabilities.addFeatureInfoFormat(xmlElement.getValue());
      } else if ("LegendGraphicFormat".equals(xmlElement.getName())) {
        aWMSCapabilities.addLegendGraphicFormat(xmlElement.getValue());
      } else if ("DescribeLayerFormat".equals(xmlElement.getName())) {
        aWMSCapabilities.addDescribeLayerFormat(xmlElement.getValue());
      }
    }
  }

  private void decodeWMSMapDataSFCT(Element aXmlMapDataElement, TLcdWMSCapabilities aWMSCapabilities) {
    String mapDataFolder = aXmlMapDataElement.getValue();
    if (mapDataFolder == null || mapDataFolder.trim().length() == 0) {
      throw new IllegalArgumentException("Missing MapData -> Folder value in configuration file.");
    }

    aWMSCapabilities.setMapDataFolder(mapDataFolder.trim());
  }

  private void decodeWMSRootLayerSFCT(Element aXmlLayerElement, TLcdWMSCapabilities aWMSCapabilities) {
    TLcdWMSLayer rootLayer = new TLcdWMSLayer();
    for (Object xmlElementObject : aXmlLayerElement.getChildren()) {
      Element xmlElement = (Element) xmlElementObject;
      if ("Title".equals(xmlElement.getName())) {
        rootLayer.setTitle(xmlElement.getValue());
      } else if ("Layer".equals(xmlElement.getName())) {
        decodeWMSLayerSFCT(xmlElement, rootLayer, aWMSCapabilities);
      }
    }

    adjustBoundingBoxSFCT(rootLayer);
    aWMSCapabilities.addRootWMSLayer(rootLayer);
  }

  private void decodeWMSLayerSFCT(Element aXmlLayerElement, TLcdWMSLayer aParentLayer, TLcdWMSCapabilities aWMSCapabilities) {
    TLcdWMSLayer childLayer = new TLcdWMSLayer();

    childLayer.setNameVisible(Boolean.valueOf(aXmlLayerElement.getAttributeValue("namevisible")));
    childLayer.setQueryable(Boolean.valueOf(aXmlLayerElement.getAttributeValue("queryable")));
    childLayer.setOpaque(Boolean.valueOf(aXmlLayerElement.getAttributeValue("opaque")));

    for (Object xmlElementObject : aXmlLayerElement.getChildren()) {
      Element xmlElement = (Element) xmlElementObject;
      if ("Name".equals(xmlElement.getName())) {
        childLayer.setName(xmlElement.getValue());
      } else if ("Title".equals(xmlElement.getName())) {
        childLayer.setTitle(xmlElement.getValue());
      } else if ("Layer".equals(xmlElement.getName())) {
        decodeWMSLayerSFCT(xmlElement, childLayer, aWMSCapabilities);
      } else if ("Style".equals(xmlElement.getName())) {
        decodeWMSLayerStyleSFCT(xmlElement, childLayer);
      } else if ("Dimension".equals(xmlElement.getName())) {
        decodeWMSLayerDimensionSFCT(xmlElement, childLayer);
      } else if ("File".equals(xmlElement.getName())) {
        decodeWMSLayerFileSFCT(xmlElement, aWMSCapabilities.getMapDataFolder(), childLayer);
      } else if ("Abstract".equals(xmlElement.getName())) {
        childLayer.setAbstract(xmlElement.getValue());
      } else if ("MinScaleDenominator".equals(xmlElement.getName())) {
        childLayer.setMinScaleDenominator(Double.valueOf(xmlElement.getValue()));
      } else if ("MaxScaleDenominator".equals(xmlElement.getName())) {
        childLayer.setMaxScaleDenominator(Double.valueOf(xmlElement.getValue()));
      } else if ("KeywordList".equals(xmlElement.getName())) {
        for (Object xmlKeywordElement : xmlElement.getChildren()) {
          childLayer.addKeyword(((Element) xmlKeywordElement).getValue());
        }
      } else if ("PaintStyle".equals(xmlElement.getName())) {
        decodeWMSPaintStyleSFCT(xmlElement, childLayer);
      } else if ("Label".equals(xmlElement.getName())) {
        decodeWMSLabelStyleSFCT(xmlElement, childLayer);
      }
    }

    adjustBoundingBoxSFCT(childLayer);
    aParentLayer.addChildWMSLayer(childLayer);
  }

  private void decodeWMSLayerStyleSFCT(Element aXmlLayerStyleElement, TLcdWMSLayer aLayer) {
    TLcdWMSLayerStyle layerStyle = new TLcdWMSLayerStyle();

    for (Object xmlElementObject : aXmlLayerStyleElement.getChildren()) {
      Element xmlElement = (Element) xmlElementObject;
      if ("StyleName".equals(xmlElement.getName())) {
        layerStyle.setName(xmlElement.getValue());
      } else if ("StyleTitle".equals(xmlElement.getName())) {
        layerStyle.setTitle(xmlElement.getValue());
      }
    }

    aLayer.addStyle(layerStyle);
  }

  private void decodeWMSLayerDimensionSFCT(Element aXmlLayerDimensionElement, TLcdWMSLayer aLayer) {
    TLcdWMSDimension layerDimension = new TLcdWMSDimension();

    layerDimension.setName(aXmlLayerDimensionElement.getAttributeValue("name"));
    layerDimension.setUnits(aXmlLayerDimensionElement.getAttributeValue("units"));
    layerDimension.setUnitSymbol(aXmlLayerDimensionElement.getAttributeValue("unitSymbol"));
    layerDimension.setMultipleValues(Boolean.valueOf(aXmlLayerDimensionElement.getAttributeValue("multipleValues")));
    layerDimension.setNearestValue(Boolean.valueOf(aXmlLayerDimensionElement.getAttributeValue("nearestValue")));
    layerDimension.setCurrent(Boolean.valueOf(aXmlLayerDimensionElement.getAttributeValue("current")));

    decodeWMSLayerDimensionExtentSFCT(aXmlLayerDimensionElement.getAttributeValue("default"), true, layerDimension);
    decodeWMSLayerDimensionExtentSFCT(aXmlLayerDimensionElement.getValue(), false, layerDimension);

    aLayer.addDimension(layerDimension);
  }

  private void decodeWMSLayerDimensionExtentSFCT(String aDimensionExtent, boolean aIsDefault, TLcdWMSDimension aDimension) {
    if (aDimensionExtent != null) {
      StringTokenizer valueTokenizer = new StringTokenizer(aDimensionExtent, ",");
      TLcdWMSDimensionExtent extent = new TLcdWMSDimensionExtent();

      while (valueTokenizer.hasMoreTokens()) {
        String extentValue = valueTokenizer.nextToken().trim();
        StringTokenizer intervalTokenizer = new StringTokenizer(extentValue, "/");

        if (intervalTokenizer.countTokens() == 1) {
          if (!extent.canAddValue(extentValue)) {
            throw new IllegalArgumentException("Dimension " + aDimension.getName() + " contains an invalid extent value: values and intervals must not be mixed.");
          }
          extent.addValue(extentValue);
        } else if (intervalTokenizer.countTokens() == 3) {
          String[] interval = new String[3];
          interval[0] = intervalTokenizer.nextToken();
          interval[1] = intervalTokenizer.nextToken();
          interval[2] = intervalTokenizer.nextToken();
          if (!extent.canAddInterval(interval)) {
            throw new IllegalArgumentException("Dimension " + aDimension.getName() + " contains an invalid extent value: values and intervals must not be mixed.");
          }
          extent.addInterval(interval);
        } else {
          throw new IllegalArgumentException("Dimension " + aDimension.getName() + " contains an invalid extent value: " + extentValue);
        }
      }

      if (aIsDefault) {
        aDimension.setDefaultValue(extent);
      } else {
        aDimension.setExtent(extent);
      }
    }
  }

  private void decodeWMSLayerFileSFCT(Element aXmlFileElement, String aMapDataFolder, TLcdWMSLayer aLayer) {
    String sourceName;
    if (fModelProvider != null) {
      File sourceFile = new File(aMapDataFolder, aXmlFileElement.getValue());
      if (!sourceFile.exists()) {
        throw new IllegalArgumentException("Specified source file " + aXmlFileElement.getValue() + " does not exist.");
      }
      sourceName = sourceFile.getAbsolutePath();
    } else {
      sourceName = aXmlFileElement.getValue();
    }

    aLayer.setSourceName(sourceName);
  }

  private void decodeWMSPaintStyleSFCT(Element aXmlStyleElement, TLcdWMSLayer aLayer) {
    // Parse and set painting properties.

    // 1. the paint mode
    String mode = aXmlStyleElement.getAttributeValue("mode");
    if (mode != null && mode.trim().length() > 0) {
      aLayer.putProperty("mode", mode);
    }
    // 2. the style settings
    for (Object xmlElementObject : aXmlStyleElement.getChildren()) {
      Element xmlElement = (Element) xmlElementObject;
      if ("PointStyle".equals(xmlElement.getName())) {
        String imagesrc = xmlElement.getAttributeValue("imagesrc");
        if (imagesrc != null && imagesrc.trim().length() > 0) {
          TLcdImageIcon imageIcon = new TLcdImageIcon(imagesrc);
          aLayer.putProperty("pointstyle.icon", imageIcon);
          aLayer.putProperty("pointstyle.icon_src", imagesrc);
        } else {
          TLcdSymbol symbol = new TLcdSymbol(TLcdSymbol.FILLED_RECT, 5, new Color(200, 150, 190));

          // symbol color
          Color symbolColor = decodeColor(xmlElement.getAttributeValue("color"), null, null);
          if (symbolColor != null) {
            symbol.setFillColor(symbolColor);
            symbol.setBorderColor(symbolColor);
          }

          // size
          String sizeStr = xmlElement.getAttributeValue("size");
          if (sizeStr != null && sizeStr.trim().length() > 0) {
            int size = Integer.parseInt(sizeStr.trim());
            symbol.setSize(size);
          }
          String shapeStr = xmlElement.getAttributeValue("shape");
          if (shapeStr != null && shapeStr.trim().length() > 0) {
            shapeStr = shapeStr.trim();
            if (shapeStr.equalsIgnoreCase("circle")) {
              symbol.setShape(TLcdSymbol.CIRCLE);
            } else if (shapeStr.equalsIgnoreCase("filled_circle")) {
              symbol.setShape(TLcdSymbol.FILLED_CIRCLE);
            } else if (shapeStr.equalsIgnoreCase("rectangle")) {
              symbol.setShape(TLcdSymbol.RECT);
            } else if (shapeStr.equalsIgnoreCase("filled_rectangle")) {
              symbol.setShape(TLcdSymbol.FILLED_RECT);
            } else if (shapeStr.equalsIgnoreCase("cross")) {
              symbol.setShape(TLcdSymbol.CROSS);
            } else if (shapeStr.equalsIgnoreCase("cross_rectangle")) {
              symbol.setShape(TLcdSymbol.CROSS_RECT);
            }
          }
          // put symbol
          aLayer.putProperty("pointstyle.icon", symbol);
        }
      } else if ("LineStyle".equals(xmlElement.getName())) {
        Color lineStyleColor = decodeColor(xmlElement.getAttributeValue("color"),
                                           xmlElement.getAttributeValue("opacity"),
                                           new Color(64, 64, 128));
        aLayer.putProperty("linestyle.color", lineStyleColor);

        String lineWidthAsString = xmlElement.getAttributeValue("width");
        if (lineWidthAsString != null && lineWidthAsString.trim().length() > 0) {
          Integer width = Integer.valueOf(lineWidthAsString.trim());
          aLayer.putProperty("linestyle.width", width);
        }
      } else if ("FillStyle".equals(xmlElement.getName())) {
        Color fillStyleColor = decodeColor(xmlElement.getAttributeValue("color"),
                                           xmlElement.getAttributeValue("opacity"),
                                           Color.DARK_GRAY);
        aLayer.putProperty("fillstyle.color", fillStyleColor);
      }
    }
  }

  private void decodeWMSLabelStyleSFCT(Element aXmlLabelElement, TLcdWMSLayer aLayer) {
    // Parse and set labeling properties.

    // 1. the layer should be labeled
    aLayer.putProperty("labeled", Boolean.TRUE);
    // 2. configure background color
    String backgroundColorStr = aXmlLabelElement.getAttributeValue("background");
    if (backgroundColorStr != null && backgroundColorStr.trim().length() == 7) {
      Color backgroundColor = WMSColorUtility.toColor(backgroundColorStr);
      aLayer.putProperty("label.background", backgroundColor);
    }
    // 3. configure foreground color
    String foregroundColorStr = aXmlLabelElement.getAttributeValue("foreground");
    if (foregroundColorStr != null && foregroundColorStr.trim().length() == 7) {
      Color foregroundColor = WMSColorUtility.toColor(foregroundColorStr);
      aLayer.putProperty("label.foreground", foregroundColor);
    }
    // 4. configure whether the label should be filled
    aLayer.putProperty("label.filled", Boolean.valueOf(aXmlLabelElement.getAttributeValue("filled")));
    // 5. configure whether the label should have a frame
    aLayer.putProperty("label.framed", Boolean.valueOf(aXmlLabelElement.getAttributeValue("framed")));
    // 6. configure whether the label should have a pin
    aLayer.putProperty("label.withPin", Boolean.valueOf(aXmlLabelElement.getAttributeValue("withPin")));
    // 7. configure the label font
    String fontnameStr = aXmlLabelElement.getAttributeValue("fontname");
    String fontstyleStr = aXmlLabelElement.getAttributeValue("fontstyle");
    String fontsizeStr = aXmlLabelElement.getAttributeValue("fontsize");
    String fontname = (fontnameStr == null ? "Dialog" : fontnameStr);
    int fontstyle = Font.PLAIN;
    if (fontstyleStr != null) {
      if (fontstyleStr.equalsIgnoreCase("bold")) {
        fontstyle = Font.BOLD;
      } else if (fontstyleStr.equalsIgnoreCase("italic")) {
        fontstyle = Font.ITALIC;
      } else if (fontstyleStr.equalsIgnoreCase("bold_italic")) {
        fontstyle = Font.BOLD | Font.ITALIC;
      }
    }
    int fontsize = (fontsizeStr == null ? 10 : Integer.parseInt(fontsizeStr));
    Font font = new Font(fontname, fontstyle, fontsize);
    aLayer.putProperty("label.font", font);
    // 8. configure the properties that should be used as label.
    ArrayList<String> properties = new ArrayList<>();
    for (Object xmlPropertyElement : aXmlLabelElement.getChildren()) {
      properties.add(((Element) xmlPropertyElement).getAttributeValue("name"));
    }
    aLayer.putProperty("label.feature_names", properties.toArray(new String[properties.size()]));
  }

  private Color decodeColor(String aColorAsString, String aOpacityAsString, Color aDefaultColor) {
    Color color = aDefaultColor;
    if (aColorAsString != null && aColorAsString.trim().length() == 7) {
      color = WMSColorUtility.toColor(aColorAsString);
    }

    if (aOpacityAsString != null && aOpacityAsString.trim().length() > 0) {
      double opacity = Double.parseDouble(aOpacityAsString.trim());
      color = WMSColorUtility.toTransparentColor(color, opacity);
    }

    return color;
  }

  private void adjustBoundingBoxSFCT(TLcdWMSLayer aLayer) {
    String sourceFileName = aLayer.getSourceName();
    if (sourceFileName != null) {
      aLayer.setLatLonBoundingBox(WMSBoundsUtility.calculateWGS84Bounds(sourceFileName, fModelProvider));
    } else if (aLayer.getChildWMSLayerCount() > 0) {
      List<ALcdWMSLayer> layers = new ArrayList<>();
      int childCount = aLayer.getChildWMSLayerCount();
      for (int i = 0; i < childCount; i++) {
        layers.add(aLayer.getChildWMSLayer(i));
      }
      aLayer.setLatLonBoundingBox(WMSBoundsUtility.calculateWGS84Bounds(layers));
    }
  }
}
