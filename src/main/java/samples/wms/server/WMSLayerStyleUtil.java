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
package samples.wms.server;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;

import com.luciad.view.gxy.ILcdGXYContext;
import com.luciad.view.gxy.ILcdGXYPainterStyle;
import com.luciad.view.gxy.TLcdGXYPainterColorStyle;
import com.luciad.wms.server.model.ALcdWMSLayer;

/**
 * Utility class to convert WMS layer properties to painter and label style objects.
 * <p/>
 * An ALcdWMSLayer object can store key-value pairs, which can be used to associate
 * additional information with the layer, apart from the predefined properties.
 * This functionality is used by the sample XML capabilities format: the
 * capabilities decoder samples.wms.server.config.xml.WMSCapabilitiesXMLDecoder
 * supports a set of XML elements that define painter and label styles, which
 * are automatically stored in newly created ALcdWMSLayer objects as key-value pairs.
 * These properties are converted to painter and label style objects by this utility class,
 * so that they can be used to configure painters and label painters in a layer
 * factory; see samples.wms.server.WMSSHPLayerFactory for an example.
 */
final class WMSLayerStyleUtil {

  private static final Color DEFAULT_LINE_COLOR = new Color(64, 64, 128);
  private static final Color DEFAULT_FILL_COLOR = Color.DARK_GRAY;

  private WMSLayerStyleUtil() {
    // No need to instantiate this class, as all utility methods are static.
  }

  /**
   * Returns an ILcdGXYPainterStyle object for the given WMS layer.
   * <p/>
   * The properties to initialize the ILcdGXYPainterStyle are
   * retrieved from the key-value pairs in ALcdWMSLayer.
   * Supported key-value pairs:
   * - 'linestyle.color' -> Color : used as painter color if defined,
   * otherwise color (64, 64, 128) is used.
   * - 'linestyle.width' -> Integer : used as plain stroke width if defined,
   * otherwise no explicit stroke information is applied.
   *
   * @param aWMSLayer An ALcdWMSLayer object.
   * @return an ILcdGXYPainterStyle object for the given WMS layer.
   */
  static ILcdGXYPainterStyle getLineStyle(ALcdWMSLayer aWMSLayer) {
    Color lineColor = (Color) aWMSLayer.getProperty("linestyle.color", DEFAULT_LINE_COLOR);
    Integer width = (Integer) aWMSLayer.getProperty("linestyle.width");
    ILcdGXYPainterStyle painterStyle = null;
    if (width != null) {
      painterStyle = new MyLineGXYPainterStyle(width.floatValue(), lineColor);
    } else {
      painterStyle = new TLcdGXYPainterColorStyle(lineColor);
    }
    return painterStyle;
  }

  /**
   * Returns an ILcdGXYPainterStyle object for the given WMS layer.
   * <p/>
   * The properties to initialize the ILcdGXYPainterStyle are
   * retrieved from the key-value pairs in ALcdWMSLayer.
   * Supported key-value pairs:
   * - 'fillstyle.color' -> Color : used as painter color if defined,
   * otherwise color (64, 64, 64) is used.
   *
   * @param aWMSLayer An ALcdWMSLayer object.
   * @return an ILcdGXYPainterStyle object for the given WMS layer.
   */
  static ILcdGXYPainterStyle getFillStyle(ALcdWMSLayer aWMSLayer) {
    Color fillColor = (Color) aWMSLayer.getProperty("fillstyle.color", DEFAULT_FILL_COLOR);
    return new TLcdGXYPainterColorStyle(fillColor);
  }

  /**
   * Returns a MyLabelStyle object for the given WMS layer.
   * <p/>
   * The properties to initialize the MyLabelStyle object are
   * retrieved from the key-value pairs in ALcdWMSLayer.
   * Supported key-value pairs:
   * - 'labeled' -> Boolean : defines whether the layer must be labeled, false by default.
   * - 'label.background' -> Color : used as background label color if defined,
   * otherwise color (255, 255, 255) is used.
   * - 'label.foreground' -> Color : used as foreground label color if defined,
   * otherwise color (0, 0, 0) is used.
   * - 'label.framed' -> Boolean : defines whether the label must be painted within a frame,
   * false by default.
   * - 'label.filled' -> Boolean : defines whether the background of the label must be filled,
   * false by default.
   * - 'label.withPin' -> Boolean : defines whether the label must be painted with a pin,
   * false by default.
   * - 'label.font' -> Font : defines the font to be used for the label.
   * - 'label.feature_names' -> String[] : defines the features to be displayed in the label.
   *
   * @param aWMSLayer An ALcdWMSLayer object.
   * @return a MyLabelStyle object for the given WMS layer.
   */
  static MyLabelStyle getLabelStyle(ALcdWMSLayer aWMSLayer) {
    Boolean labeled = (Boolean) aWMSLayer.getProperty("labeled", Boolean.FALSE);
    if (!labeled.booleanValue()) {
      return null;
    }
    MyLabelStyle labelStyle = new MyLabelStyle();
    labelStyle.fBackground = (Color) aWMSLayer.getProperty("label.background", Color.white);
    labelStyle.fForeground = (Color) aWMSLayer.getProperty("label.foreground", Color.black);
    labelStyle.fFramed = ((Boolean) aWMSLayer.getProperty("label.framed", Boolean.FALSE)).booleanValue();
    labelStyle.fFilled = ((Boolean) aWMSLayer.getProperty("label.filled", Boolean.FALSE)).booleanValue();
    labelStyle.fWithPin = ((Boolean) aWMSLayer.getProperty("label.withPin", Boolean.FALSE)).booleanValue();
    labelStyle.fFont = (Font) aWMSLayer.getProperty("label.font");

    String[] featureNames = (String[]) aWMSLayer.getProperty("label.feature_names");
    if (featureNames != null && featureNames.length > 0) {
      labelStyle.fExpressions = featureNames;
    }

    return labelStyle;
  }

  /**
   * Container class to store properties related to label rendering.
   */
  public static class MyLabelStyle {
    boolean fFilled;
    boolean fFramed;
    boolean fWithPin;
    Color fBackground;
    Color fForeground;
    Font fFont;
    String[] fExpressions;
  }

  /**
   * Implementation of ILcdGXYPainterStyle that is used
   * to apply a color and a plain stroke width on
   * a Graphics instance.
   */
  private static class MyLineGXYPainterStyle implements ILcdGXYPainterStyle {

    private Stroke fStroke;
    private Color fColor;

    public MyLineGXYPainterStyle(float aWidth, Color aColor) {
      fStroke = new BasicStroke(aWidth);
      fColor = aColor;
    }

    public void setupGraphics(Graphics aGraphics,
                              Object aObject,
                              int aMode,
                              ILcdGXYContext aGXYContext) {
      if (fStroke != null && aGraphics instanceof Graphics2D) {
        Graphics2D g2d = (Graphics2D) aGraphics;
        g2d.setStroke(fStroke);
      }
      aGraphics.setColor(fColor);
    }
  }
}
