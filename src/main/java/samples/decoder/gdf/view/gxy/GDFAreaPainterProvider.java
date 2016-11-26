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
package samples.decoder.gdf.view.gxy;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import com.luciad.format.gdf.ILcdGDFAreaFeature;
import com.luciad.view.gxy.ILcdGXYPainter;
import com.luciad.view.gxy.ILcdGXYPainterProvider;
import com.luciad.view.gxy.ILcdGXYPainterStyle;
import com.luciad.view.gxy.TLcdGXYPainterColorStyle;
import com.luciad.view.gxy.painter.TLcdGXYPointListPainter;
import com.luciad.view.gxy.painter.TLcdGXYShapeListPainter;

/**
 * Painter provider for GDF area features.
 */
public class GDFAreaPainterProvider implements ILcdGXYPainterProvider {

  private Map<Integer, ILcdGXYPainterStyle> fAreaStyles = new HashMap<Integer, ILcdGXYPainterStyle>();

  private TLcdGXYPointListPainter fPointListPainter = new TLcdGXYPointListPainter(TLcdGXYPointListPainter.FILLED);
  private TLcdGXYShapeListPainter fShapeListPainter = new TLcdGXYShapeListPainter(fPointListPainter);

  public GDFAreaPainterProvider() {
    // Some default styles.
    registerStyle(3110, new TLcdGXYPainterColorStyle(new Color(200, 200, 200)));
    registerStyle(3120, new TLcdGXYPainterColorStyle(new Color(200, 200, 200)));
    registerStyle(4310, new TLcdGXYPainterColorStyle(new Color(150, 180, 200)));
    registerStyle(7110, new TLcdGXYPainterColorStyle(new Color(150, 150, 150)));
    registerStyle(7170, new TLcdGXYPainterColorStyle(new Color(180, 200, 150)));
  }

  /**
   * Registers the specified fill style for the given GDF feature class code.
   *
   * @param aFeatureClassCode the feature class code for which to register the style.
   * @param aFillStyle        the style to be registered.
   */
  public void registerStyle(int aFeatureClassCode,
                            ILcdGXYPainterStyle aFillStyle) {
    fAreaStyles.put(aFeatureClassCode, aFillStyle);
  }

  // Implementations for ILcdGXYPainterProvider.

  public ILcdGXYPainter getGXYPainter(Object aObject) {
    if (aObject instanceof ILcdGDFAreaFeature) {
      ILcdGDFAreaFeature feature = (ILcdGDFAreaFeature) aObject;
      int fcc = feature.getFeatureClass().getFeatureClassCode();

      ILcdGXYPainterStyle fillStyle = fAreaStyles.get(fcc);
      if (fillStyle != null) {
        fPointListPainter.setFillStyle(fillStyle);
        fShapeListPainter.setObject(aObject);
        return fShapeListPainter;
      }
    }

    return null;
  }

  public Object clone() {
    try {
      return super.clone();
    } catch (CloneNotSupportedException e) {
      e.printStackTrace();
      return null;
    }
  }

}
