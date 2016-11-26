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
package samples.lightspeed.customization.style.highlighting;

import static java.util.Collections.synchronizedCollection;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.luciad.gui.TLcdColor;
import com.luciad.model.ILcdModel;
import com.luciad.shape.ILcdBounds;
import com.luciad.shape.ILcdPoint;
import com.luciad.shape.ILcdShape;
import com.luciad.shape.shape2D.TLcdLonLatArcBand;
import com.luciad.shape.shape3D.TLcdExtrudedShape;
import com.luciad.view.lightspeed.TLspContext;
import com.luciad.view.lightspeed.layer.TLspPaintRepresentationState;
import com.luciad.view.lightspeed.style.ILspWorldElevationStyle.ElevationMode;
import com.luciad.view.lightspeed.style.TLspFillStyle;
import com.luciad.view.lightspeed.style.TLspLineStyle;
import com.luciad.view.lightspeed.style.styler.ALspStyleCollector;

/**
 * A styler that generates 3D pie charts for countries in the highlighting
 * sample. The chart shows the population distribution per age group.
 */
public class AnimatedHighlightAreaShapeStyler extends AnimatedHighlightStyler {

  //All the selected objects for which the fade-out should be postponed
  private final Collection<Object> fSelectedObjects = synchronizedCollection(new ArrayList<>());
  private final Color fBaseColor;

  /**
   * The default constructor for this AnimatedHighLightStyler
   */
  public AnimatedHighlightAreaShapeStyler(Color aBaseColor) {
    fBaseColor = aBaseColor;
  }

  @Override
  public void style(Collection<?> aObjects, ALspStyleCollector aStyleCollector, TLspContext aContext) {
    //List of object on which default styling should be applied
    List<Object> defaultObjects = new ArrayList<Object>();

    for (Object object : aObjects) {
      //Retrieve the current alpha value for the object, and add it to the list of default objects if alpha <= 0
      int t = fObject2Alpha.containsKey(object) ? fObject2Alpha.get(object) : 0;
      if (t <= 0) {
        defaultObjects.add(object);
      } else {
        //Normalize the alpha value to the [0,1] interval
        float normalizedT = t / 255f;

        //Submit the countries background styles to the style collector
        aStyleCollector.object(object)
                       .styles(TLspFillStyle.newBuilder().color(TLcdColor.interpolate(fBaseColor, COUNTRY_HL_FILL_COLOR, normalizedT)).elevationMode(ElevationMode.ON_TERRAIN).build(),
                               TLspLineStyle.newBuilder().color(COUNTRY_BG_LINE_COLOR).width(1 + normalizedT).elevationMode(ElevationMode.ON_TERRAIN).build())
                       .submit();

        ILcdShape shape = (ILcdShape) object;
        //Retrieve the center point and radius of the shape
        ILcdBounds b = shape.getBounds();
        double radius = Math.sqrt(b.getWidth() * b.getWidth() + b.getHeight() * b.getHeight()) * 8e3;

        ILcdPoint center = AnimatedHighlightStyler.getFocusPoint(shape);

        //Retrieve and store the statistical information for each population group
        int[] slices = getSliceStatistics(object);

        int max = 0;
        int total = 0;
        boolean validPieChart = false;
        for (int slice : slices) {
          total += slice;
          max = Math.max(max, slice);
          validPieChart |= (slice != 0);
        }

        if (!validPieChart) {
          continue;
        }

        double alpha = 0;

        float tRadius = Math.min(1f, normalizedT * 2);
        float tHeight = Math.max(0f, normalizedT - 0.5f) * 2f;

        // This is the complete circle around the pie chart
        for (int i = 0; i < slices.length; i++) {
          double percentage = (double) slices[i] / (double) total;
          double angle = 360.0 * percentage;
          //Create an arc band for each piece of the pie chart
          TLcdLonLatArcBand arc = new TLcdLonLatArcBand(
              center,
              0, radius * tRadius,
              alpha + 90, angle
          );
          alpha += angle;

          double altitude = max / 200.0;
          //Submit the appropriate fill and line style for a custom geometry: An extruded shape based on the previously created arc band.
          aStyleCollector.
                             object(object).
                             geometry(new TLcdExtrudedShape(arc, ELEVATION_OFFSET, ELEVATION_OFFSET + tHeight * altitude)).
                             styles(CHART_FILL_STYLES[i],
                                    CHART_LINE_STYLES[i])
                         .submit();
        }
      }
    }

    //Apply default styling to all object in the default objects list
    if (!defaultObjects.isEmpty()) {
      aStyleCollector.
                         objects(defaultObjects)
                     .styles(TLspFillStyle.newBuilder().color(fBaseColor).elevationMode(ElevationMode.ON_TERRAIN).build(),
                             TLspLineStyle.newBuilder().color(COUNTRY_BG_LINE_COLOR).elevationMode(ElevationMode.ON_TERRAIN).build())
                     .submit();
    }
  }

  @Override
  public void objectHighlighted(Object aObject, TLspPaintRepresentationState aPrs, TLspContext aContext) {
    if (fFadeInObject != aObject) {
      //Only play a fade-out animation for an object if it is not still selected
      if (!fSelectedObjects.contains(fFadeInObject)) {
        playFadeOutAnimation(fFadeInObject, fFadeInModel);
      }
      ILcdModel model = aContext == null ? null : aContext.getModel();
      playFadeInAnimation(aObject, model);
      fFadeInObject = aObject;
      fFadeInModel = model;
    }
  }

  @Override
  protected void removeAnimatedObject(Object aAnimatedObject) {
    fSelectedObjects.remove(aAnimatedObject);
    super.removeAnimatedObject(aAnimatedObject);
  }

}
