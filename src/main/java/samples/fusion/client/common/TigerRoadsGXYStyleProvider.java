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
package samples.fusion.client.common;

import static java.awt.Color.YELLOW;

import static com.luciad.gui.TLcdSymbol.FILLED_CIRCLE;
import static com.luciad.util.TLcdDistanceUnit.METRE_UNIT;

import java.awt.Color;

import com.luciad.datamodel.ILcdDataObject;
import com.luciad.datamodel.TLcdDataType;
import com.luciad.fusion.client.view.gxy.ALfnVectorGXYStyleProvider;
import com.luciad.fusion.tilestore.model.vector.ILfnTiledSurface;
import com.luciad.gui.ILcdIcon;
import com.luciad.gui.TLcdSymbol;
import com.luciad.shape.ILcdCurve;
import com.luciad.shape.ILcdPoint;
import com.luciad.view.gxy.ILcdGXYPainterStyle;
import com.luciad.view.gxy.TLcdGXYPainterColorStyle;

public class TigerRoadsGXYStyleProvider extends ALfnVectorGXYStyleProvider {

  private static final String MTFCC = "MTFCC"; // MAF/TIGER feature class code

  private static final String RTTYP = "RTTYP"; // Route type code

  private final ILcdIcon fPointIcon = new TLcdSymbol(FILLED_CIRCLE, 5, Color.decode("#5555FF"));

  private final ILcdIcon fSelectedPointIcon = new TLcdSymbol(FILLED_CIRCLE, 5, Color.decode("#5555FF"), YELLOW);

  private final TLcdGXYPainterColorStyle fFillStyle = new TLcdGXYPainterColorStyle(Color.decode("#5555FF"), YELLOW);

  /**
   * Anonymous: black.
   */
  final ILcdGXYPainterStyle fLineStyle = newLineStyle(3, Color.decode("#000000"));

  /**
   * Primary road: red.
   */
  final ILcdGXYPainterStyle fS1100_LineStyle = newLineStyle(24, Color.decode("#FF2A2A"));

  /**
   * Interstate: red (official interstate colors {@code 0x003f87ff} and {@code 0x00af1e2d}.
   */
  final ILcdGXYPainterStyle fS1100_I_LineStyle = fS1100_LineStyle; //newLineStyle(24, Color.decode("#3F87FF"));

  /**
   * State route: red.
   */
  final ILcdGXYPainterStyle fS1100_S_LineStyle = fS1100_LineStyle;

  /**
   * Secondary road: yellow.
   */
  final ILcdGXYPainterStyle fS1200_LineStyle = newLineStyle(18, Color.decode("#FFDD55"));

  /**
   * State highway: yellow.
   */
  final ILcdGXYPainterStyle fS1200_S_LineStyle = fS1200_LineStyle;

  /**
   * US highway: yellow.
   */
  final ILcdGXYPainterStyle fS1200_U_LineStyle = fS1200_LineStyle;

  /**
   * Local road: almost white.
   */
  final ILcdGXYPainterStyle fS1400_LineStyle = newLineStyle(9, Color.decode("#E0E0E0"));

  /**
   * Trail: brown.
   */
  final ILcdGXYPainterStyle fS1500_LineStyle = newLineStyle(3, Color.decode("#DEAA87"));

  /**
   * Ramp: pale orange.
   */
  final ILcdGXYPainterStyle fS1630_LineStyle = newLineStyle(15, Color.decode("#FF9955"));

  /**
   * Service drive: orange.
   */
  final ILcdGXYPainterStyle fS1640_LineStyle = newLineStyle(3, Color.decode("#FFB380"));

  /**
   * Walkway: green.
   */
  final ILcdGXYPainterStyle fS1710_LineStyle = newLineStyle(1, Color.decode("#AADE87"));

  /**
   * Alley: see walkway.
   */
  final ILcdGXYPainterStyle fS1730_LineStyle = fS1710_LineStyle;

  /**
   * Stairway: see walkway.
   */
  final ILcdGXYPainterStyle fS1720_LineStyle = fS1710_LineStyle;

  /**
   * Private road: purple-ish grey.
   */
  final ILcdGXYPainterStyle fS1740_LineStyle = newLineStyle(6, Color.decode("#AC9D93"));

  /**
   * Driveway: see private road.
   */
  final ILcdGXYPainterStyle fS1750_LineStyle = fS1740_LineStyle;

  /**
   * Parking lot road: pink.
   */
  final ILcdGXYPainterStyle fS1780_LineStyle = newLineStyle(6, Color.decode("#FF80E5"));

  /**
   * Bike path: green.
   */
  final ILcdGXYPainterStyle fS1820_LineStyle = newLineStyle(2, Color.decode("#C6E9AF"));

  /**
   * Bridle path: brown.
   */
  final ILcdGXYPainterStyle fS1830_LineStyle = newLineStyle(2, Color.decode("#DEAA87"));

  /**
   * Railroad: almost black.
   */
  final ILcdGXYPainterStyle fR1011_LineStyle = newLineStyle(18, Color.decode("#202020"));

  /**
   * Mass transportation (other than railroad): maroon.
   */
  final ILcdGXYPainterStyle fR1051_LineStyle = newLineStyle(18, Color.decode("#782121"));

  private TigerRoadsGXYStyleProvider() {
  }

  @Override
  public ILcdGXYPainterStyle getFillStyle(ILcdDataObject aDataObject, ILfnTiledSurface aArea) {
    return fFillStyle;
  }

  @Override
  public ILcdGXYPainterStyle getOutlineStyle(ILcdDataObject aDataObject, ILfnTiledSurface aArea) {
    return getLineStyle(aDataObject);
  }

  private ILcdGXYPainterStyle getLineStyle(ILcdDataObject aDataObject) {
    String mtfcc = String.valueOf(aDataObject.getValue(MTFCC));
    if ("S1100".equals(mtfcc)) {
      String rttyp = String.valueOf(aDataObject.getValue(RTTYP));
      if ("I".equals(rttyp)) // Interstate
      {
        return fS1100_I_LineStyle;
      }
      if ("S".equals(rttyp)) // State route
      {
        return fS1100_S_LineStyle;
      }
      return fS1100_LineStyle;
    }
    if ("S1200".equals(mtfcc)) {
      String rttyp = String.valueOf(aDataObject.getValue(RTTYP));
      if ("S".equals(rttyp)) // State highway
      {
        return fS1200_S_LineStyle;
      }
      if ("U".equals(rttyp)) // US highway
      {
        return fS1100_I_LineStyle;
      }
      return fS1100_LineStyle;
    }
    if ("S1400".equals(mtfcc)) {
      return fS1400_LineStyle;
    }
    if ("S1500".equals(mtfcc)) {
      return fS1500_LineStyle;
    }
    if ("S1630".equals(mtfcc)) {
      return fS1630_LineStyle;
    }
    if ("S1640".equals(mtfcc)) {
      return fS1640_LineStyle;
    }
    if ("S1710".equals(mtfcc)) {
      return fS1710_LineStyle;
    }
    if ("S1720".equals(mtfcc)) {
      return fS1720_LineStyle;
    }
    if ("S1730".equals(mtfcc)) {
      return fS1730_LineStyle;
    }
    if ("S1740".equals(mtfcc)) {
      return fS1740_LineStyle;
    }
    if ("S1750".equals(mtfcc)) {
      return fS1750_LineStyle;
    }
    if ("S1780".equals(mtfcc)) {
      return fS1780_LineStyle;
    }
    if ("S1820".equals(mtfcc)) {
      return fS1820_LineStyle;
    }
    if ("S1830".equals(mtfcc)) {
      return fS1830_LineStyle;
    }
    if ("R1011".equals(mtfcc)) {
      return fR1011_LineStyle;
    }
    if ("R1051".equals(mtfcc)) {
      return fR1051_LineStyle;
    }
    return fLineStyle;
  }

  @Override
  public ILcdGXYPainterStyle getLineStyle(ILcdDataObject aDataObject, ILcdCurve aLine) {
    return getLineStyle(aDataObject);
  }

  @Override
  public ILcdIcon getIcon(ILcdDataObject aDataObject, ILcdPoint aPoint) {
    return fPointIcon;
  }

  @Override
  public ILcdIcon getSelectionIcon(ILcdDataObject aDataObject, ILcdPoint aPoint) {
    return fSelectedPointIcon;
  }

  @Override
  public String getName() {
    return "TIGER";
  }

  /**
   * @param aWidth line width in meters
   * @param aColor line color
   * @return a line style
   */
  static ILcdGXYPainterStyle newLineStyle(int aWidth, Color aColor) {
    PronouncingLineStyle style = new PronouncingLineStyle();
    style.setLineWidth(aWidth);
    style.setColor(aColor);
    style.setSelectedLineWidth(aWidth * 2);
    style.setSelectedColor(YELLOW);
    style.setAntiAliasing(true);
    style.setLineWidthUnit(METRE_UNIT);
    return style;
  }

  public static TigerRoadsGXYStyleProvider newInstance() {
    return new TigerRoadsGXYStyleProvider();
  }

  public static boolean isApplicableFor(TLcdDataType aDataType) {
    return aDataType.getDeclaredProperty(MTFCC) != null;
  }
}
