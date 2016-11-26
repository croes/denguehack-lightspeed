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
package samples.wms.server.config.editor;

import java.awt.Color;
import java.awt.Font;
import java.io.FileWriter;
import java.io.IOException;

import com.luciad.gui.TLcdImageIcon;
import com.luciad.gui.TLcdSymbol;
import com.luciad.wms.server.model.ALcdWMSCapabilities;
import com.luciad.wms.server.model.ALcdWMSDimension;
import com.luciad.wms.server.model.ALcdWMSLayer;
import com.luciad.wms.server.model.ALcdWMSServiceMetaData;
import com.luciad.wms.server.model.TLcdWMSDimensionExtent;

/**
 * Encoder class for saving WMS configuration files to disk.
 */
class WMSCapabilitiesXMLEncoder {

  private final String fFontStyles[] = {
      "plain",
      "bold",
      "italic",
      "bold_italic"
  };

  private final String fSymbolNames[] = {
      "circle",
      "filled_circle",
      "rect",
      "filled_rect",
      "plus",
      "plus_rect",
      "cross",
      "cross_rect",
      "triangle",
      "filled_triangle",
      "polyline",
      "polygon",
      "area",
      "points",
      "outlined_area",
      "text"
  };

  private FileWriter fOutput;
  private String fMapDataFolder;

  private static String escapeCharacters(String aString) {
    StringBuffer result = null;
    for (int i = 0, max = aString.length(), delta = 0; i < max; i++) {
      char c = aString.charAt(i);
      String replacement = null;

      if (c == '&') {
        replacement = "&amp;";
      } else if (c == '<') {
        replacement = "&lt;";
      } else if (c == '>') {
        replacement = "&gt;";
      } else if (c == '"') {
        replacement = "&quot;";
      } else if (c == '\'') {
        replacement = "&apos;";
      }

      if (replacement != null) {
        if (result == null) {
          result = new StringBuffer(aString);
        }
        result.replace(i + delta, i + delta + 1, replacement);
        delta += (replacement.length() - 1);
      }
    }
    if (result == null) {
      return aString;
    }
    return result.toString();
  }

  private void writeServiceMetadata(ALcdWMSServiceMetaData aData) throws IOException {

    fOutput.write("  <Service>\n");
    fOutput.write("    <Name>OGC:WMS</Name>\n");
    fOutput.write("    <Title>" + escapeCharacters(aData.getServiceTitle()) + "</Title>\n");
    fOutput.write("    <Abstract>" + escapeCharacters(aData.getServiceAbstract()) + "</Abstract>\n");
    fOutput.write("    <OnlineResource xmlns:xlink=\"http://www.w3.org/1999/xlink\"\n");
    fOutput.write("      xlink:type=\"simple\"\n");
    fOutput.write("      xlink:href=\"" + escapeCharacters(aData.getOnlineResource().getOnlineResource().getHref()) + "\" />\n");
    fOutput.write("  </Service>\n");
  }

  private void writeOutputFormats(ALcdWMSCapabilities aCaps) throws IOException {

    fOutput.write("  <Output>\n");
    for (int i = 0; i < aCaps.getMapFormatCount(); i++) {
      fOutput.write("    <Format>" + escapeCharacters(aCaps.getMapFormat(i)) + "</Format>\n");
    }
    fOutput.write("  </Output>\n");
  }

  private void writeMapDataPath() throws IOException {
    fOutput.write("  <MapData>\n");
    fOutput.write("    <Folder>" + escapeCharacters(fMapDataFolder.replace('\\', '/')) + "</Folder>\n");
    fOutput.write("  </MapData>\n");
  }

  private void writeIndentation(int spaces) throws IOException {
    for (int i = 0; i < spaces; i++) {
      fOutput.write(' ');
    }
  }

  private String colorToHexString(Color aColor) {
    String hex = Integer.toHexString(aColor.getRGB() & 0xFFFFFF).toUpperCase();
    while (hex.length() < 6) {
      hex = "0" + hex;
    }
    return "#" + hex;
  }

  private float getFloatOpacity(Color aColor) {
    return aColor.getAlpha() / 255.0f;
  }

  private void writeLabels(ALcdWMSLayer aLayer, int aLevel) throws IOException {

    Boolean labeled = (Boolean) aLayer.getProperty("labeled");
    if ((labeled != null) && (labeled.booleanValue())) {
      writeIndentation(6 + aLevel * 2);
      fOutput.write("<Label ");
      Color c = (Color) aLayer.getProperty("label.foreground");
      if (c != null) {
        fOutput.write("foreground=\"" + colorToHexString(c) + "\" ");
      }
      c = (Color) aLayer.getProperty("label.background");
      if (c != null) {
        fOutput.write("background=\"" + colorToHexString(c) + "\" ");
      }
      Boolean b = (Boolean) aLayer.getProperty("label.filled", Boolean.FALSE);
      fOutput.write("filled=\"" + b.toString() + "\" ");
      b = (Boolean) aLayer.getProperty("label.framed", Boolean.FALSE);
      fOutput.write("framed=\"" + b.toString() + "\" ");
      b = (Boolean) aLayer.getProperty("label.withPin", Boolean.FALSE);
      fOutput.write("withPin=\"" + b.toString() + "\" ");
      Font f = (Font) aLayer.getProperty("label.font");
      if (f != null) {
        fOutput.write("fontname=\"" + escapeCharacters(f.getName()) + "\" ");
        fOutput.write("fontsize=\"" + f.getSize() + "\" ");
        fOutput.write("fontstyle=\"" + fFontStyles[f.getStyle()] + "\" ");
      }
      fOutput.write(">\n");

      Object[] features = (Object[]) aLayer.getProperty("label.feature_names");
      if (features != null) {
        for (int i = 0; i < features.length; i++) {
          writeIndentation(8 + aLevel * 2);
          fOutput.write("<Feature name=\"" + escapeCharacters(features[i].toString()) + "\"/>\n");
        }
      }

      writeIndentation(6 + aLevel * 2);
      fOutput.write("</Label>\n");
    }
  }

  private void writePaintStyle(ALcdWMSLayer aLayer, int aLevel) throws IOException {

    Boolean haspaintstyle = (Boolean) aLayer.getProperty("haspaintstyle");
    if ((haspaintstyle != null) && (haspaintstyle.booleanValue())) {
      Color color;

      writeIndentation(6 + aLevel * 2);
      fOutput.write("<PaintStyle");
      String mode = (String) aLayer.getProperty("mode");
      if (mode != null) {
        fOutput.write(" mode=\"" + mode + "\"");
      }
      fOutput.write(">\n");

      color = (Color) aLayer.getProperty("fillstyle.color");
      if (color != null) {
        writeIndentation(8 + aLevel * 2);
        fOutput.write("<FillStyle");
        fOutput.write(" color=\"" + colorToHexString(color) + "\"");
        fOutput.write(" opacity=\"" + getFloatOpacity(color) + "\"");
        fOutput.write(" pattern=\"filled\" />\n");
      }

      color = (Color) aLayer.getProperty("linestyle.color");
      Integer w = (Integer) aLayer.getProperty("linestyle.width");
      if ((color != null) || (w != null)) {
        writeIndentation(8 + aLevel * 2);
        fOutput.write("<LineStyle");
        if (color != null) {
          fOutput.write(" color=\"" + colorToHexString(color) + "\"");
          fOutput.write(" opacity=\"" + getFloatOpacity(color) + "\"");
        }
        if (w != null) {
          fOutput.write(" width=\"" + w.intValue() + "\"");
        }
        fOutput.write(" />\n");
      }

      Object pointstyle = aLayer.getProperty("pointstyle.icon");
      if (pointstyle != null) {
        writeIndentation(8 + aLevel * 2);
        if (pointstyle instanceof TLcdImageIcon) {
          String src = (String) aLayer.getProperty("pointstyle.icon_src");
          fOutput.write("<PointStyle imagesrc=\"" + escapeCharacters(src) + "\"/>\n");
        } else if (pointstyle instanceof TLcdSymbol) {
          TLcdSymbol symbol = (TLcdSymbol) pointstyle;
          fOutput.write("<PointStyle");
          fOutput.write(" color=\"" + colorToHexString(symbol.getFillColor()) + "\"");
          fOutput.write(" size=\"" + symbol.getSize() + "\"");
          fOutput.write(" shape=\"" + fSymbolNames[symbol.getShape()] + "\"");
          fOutput.write("/>\n");
        }
      }

      writeIndentation(6 + aLevel * 2);
      fOutput.write("</PaintStyle>\n");
    }
  }

  private String getRelativePath(String aAbsoluteFileName) {
    String absSlash = aAbsoluteFileName.replace('\\', '/');
    String rootSlash = fMapDataFolder.replace('\\', '/');

    if (!absSlash.toLowerCase().startsWith(rootSlash.toLowerCase())) {
      return absSlash;
      // throw new IllegalArgumentException(aAbsoluteFileName + " is not located beneath the map data root folder!");
    } else {
      return absSlash.substring(fMapDataFolder.length());
    }
  }

  private void writeLayer(ALcdWMSLayer aLayer, int aLevel) throws IOException {

    writeIndentation(4 + aLevel * 2);
    String layerAttributes = " namevisible=\"" + aLayer.isNameVisible() + "\"";
    if (aLayer.isQueryable()) {
      layerAttributes = layerAttributes.concat(" queryable=\"" + aLayer.isQueryable() + "\"");
    }
    if (Boolean.TRUE.equals(aLayer.isOpaque())) {
      layerAttributes = layerAttributes.concat(" opaque=\"true\"");
    }

    fOutput.write("<Layer" + layerAttributes + ">\n");

    if (aLayer.getName() != null) {
      writeIndentation(4 + aLevel * 2);
      fOutput.write("  <Name>" + escapeCharacters(aLayer.getName()) + "</Name>\n");
    }
    if (aLayer.getTitle() != null) {
      writeIndentation(4 + aLevel * 2);
      fOutput.write("  <Title>" + escapeCharacters(aLayer.getTitle()) + "</Title>\n");
    }
    if (aLayer.getAbstract() != null) {
      writeIndentation(4 + aLevel * 2);
      fOutput.write("  <Abstract>" + escapeCharacters(aLayer.getAbstract()) + "</Abstract>\n");
    }
    if (aLayer.getSourceName() != null) {
      writeIndentation(4 + aLevel * 2);
      fOutput.write("  <File>" + escapeCharacters(getRelativePath(aLayer.getSourceName())) + "</File>\n");
    }

    writeLabels(aLayer, aLevel);
    writePaintStyle(aLayer, aLevel);

    if (aLayer.getMinScaleDenominator() > 0.0) {
      writeIndentation(4 + aLevel * 2);
      fOutput.write("  <MinScaleDenominator>" + escapeCharacters(Double.toString(aLayer.getMinScaleDenominator())) + "</MinScaleDenominator>\n");
    }

    if (aLayer.getMaxScaleDenominator() > 0.0 && !Double.isInfinite(aLayer.getMaxScaleDenominator())) {
      writeIndentation(4 + aLevel * 2);
      fOutput.write("  <MaxScaleDenominator>" + escapeCharacters(Double.toString(aLayer.getMaxScaleDenominator())) + "</MaxScaleDenominator>\n");
    }

    for (int i = 0; i < aLayer.getDimensionCount(); i++) {
      writeDimension(aLayer.getDimension(i));
    }

    for (int i = 0; i < aLayer.getChildWMSLayerCount(); i++) {
      writeLayer(aLayer.getChildWMSLayer(i), aLevel + 1);
    }
    writeIndentation(4 + aLevel * 2);
    fOutput.write("</Layer>\n");
  }

  private void writeRootLayer(ALcdWMSLayer aLayer) throws IOException {
    fOutput.write("  <Root>\n");
    if (aLayer.getName() != null) {
      fOutput.write("    <Name>" + escapeCharacters(aLayer.getName()) + "</Name>\n");
    }
    if (aLayer.getTitle() != null) {
      fOutput.write("    <Title>" + escapeCharacters(aLayer.getTitle()) + "</Title>\n");
    }
    if (aLayer.getAbstract() != null) {
      fOutput.write("    <Abstract>" + escapeCharacters(aLayer.getAbstract()) + "</Abstract>\n");
    }
    if (aLayer.getSourceName() != null) {
      fOutput.write("    <File>" + escapeCharacters(getRelativePath(aLayer.getSourceName())) + "</File>\n");
    }
    for (int i = 0; i < aLayer.getDimensionCount(); i++) {
      writeDimension(aLayer.getDimension(i));
    }
    for (int i = 0; i < aLayer.getChildWMSLayerCount(); i++) {
      writeLayer(aLayer.getChildWMSLayer(i), 0);
    }
    fOutput.write("  </Root>\n");
  }

  private void writeDimension(ALcdWMSDimension aWMSDimension) throws IOException {
    fOutput.write("<Dimension ");
    fOutput.write("name=\"" + aWMSDimension.getName() + "\" ");
    fOutput.write("units=\"" + aWMSDimension.getUnits() + "\" ");
    if (aWMSDimension.getUnitSymbol() != null) {
      fOutput.write("unitSymbol=\"" + aWMSDimension.getUnitSymbol() + "\" ");
    }
    if (aWMSDimension.getDefaultValue() != null) {
      fOutput.write("default=\"");
      writeDimensionExtent(aWMSDimension.getDefaultValue());
      fOutput.write("\" ");
    }
    fOutput.write("multipleValues=\"" + (aWMSDimension.isMultipleValues() ? "1" : "0") + "\" ");
    fOutput.write("nearestValue=\"" + (aWMSDimension.isNearestValue() ? "1" : "0") + "\" ");
    fOutput.write("current=\"" + (aWMSDimension.isCurrent() ? "1" : "0") + "\" ");
    fOutput.write(">");
    writeDimensionExtent(aWMSDimension.getExtent());
    fOutput.write("</Dimension>\n");
  }

  private void writeDimensionExtent(TLcdWMSDimensionExtent aDimensionExtent) throws IOException {
    if (aDimensionExtent.getValueCount() > 0) {
      for (int i = 0; i < aDimensionExtent.getValueCount(); i++) {
        if (i > 0) {
          fOutput.write(",");
        }
        fOutput.write(escapeCharacters(aDimensionExtent.getValue(i)));
      }
    } else if (aDimensionExtent.getIntervalCount() > 0) {
      for (int i = 0; i < aDimensionExtent.getIntervalCount(); i++) {
        if (i > 0) {
          fOutput.write(",");
        }
        String[] interval = aDimensionExtent.getInterval(i);
        fOutput.write(escapeCharacters(interval[0]));
        fOutput.write("/" + escapeCharacters(interval[1]));
        fOutput.write("/" + escapeCharacters(interval[2]));
      }
    }
  }

  public void save(ALcdWMSCapabilities aCapabilities, String aDestination) throws IOException {

    fMapDataFolder = aCapabilities.getMapDataFolder();

    fOutput = new FileWriter(aDestination);

    fOutput.write("<WMS_Capabilities_config version=\"1.0\">\n");
    writeServiceMetadata(aCapabilities.getWMSServiceMetaData());
    writeOutputFormats(aCapabilities);
    writeMapDataPath();
    writeRootLayer(aCapabilities.getRootWMSLayer(0));
    fOutput.write("</WMS_Capabilities_config>\n");

    fOutput.close();
  }
}
