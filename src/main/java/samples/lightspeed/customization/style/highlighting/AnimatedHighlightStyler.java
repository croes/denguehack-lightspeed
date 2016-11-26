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

import static java.awt.Color.*;
import static java.util.Collections.synchronizedMap;

import java.awt.Color;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

import com.luciad.datamodel.ILcdDataObject;
import com.luciad.geometry.cartesian.TLcdCartesian;
import com.luciad.gui.TLcdSymbol;
import com.luciad.model.ILcdModel;
import com.luciad.shape.ILcdComplexPolygon;
import com.luciad.shape.ILcdPoint;
import com.luciad.shape.ILcdShape;
import com.luciad.shape.ILcdShapeList;
import com.luciad.shape.shape2D.ILcd2DEditablePoint;
import com.luciad.shape.shape3D.TLcdLonLatHeightPoint;
import com.luciad.util.collections.TLcdWeakIdentityHashMap;
import com.luciad.view.animation.ALcdAnimation;
import com.luciad.view.animation.ALcdAnimationManager;
import com.luciad.view.lightspeed.TLspContext;
import com.luciad.view.lightspeed.layer.TLspPaintRepresentationState;
import com.luciad.view.lightspeed.layer.TLspPaintState;
import com.luciad.view.lightspeed.painter.label.TLspLabelID;
import com.luciad.view.lightspeed.style.ALspStyle;
import com.luciad.view.lightspeed.style.TLspFillStyle;
import com.luciad.view.lightspeed.style.TLspIconStyle;
import com.luciad.view.lightspeed.style.TLspLineStyle;
import com.luciad.view.lightspeed.style.TLspPinLineStyle;
import com.luciad.view.lightspeed.style.TLspTextStyle;
import com.luciad.view.lightspeed.style.styler.ALspStyler;
import com.luciad.view.lightspeed.style.styler.ILspStyler;

/**
 * Styler that adapts the style of highlighted objects over time.
 */
public abstract class AnimatedHighlightStyler extends ALspStyler implements HighlightController.HighlightListener {

  //Color and style constants for the countries
  protected static final Color COUNTRY_BG_FILL_COLOR = new Color(0.3f, 0.3f, 0.3f, 0.01f);
  protected static final Color COUNTRY_BG_LINE_COLOR = new Color(0.7f, 0.7f, 0.7f, 1f);
  protected static final Color COUNTRY_HL_FILL_COLOR = new Color(0.6f, 0.6f, 0.6f, 0.6f);
  protected static final Color COUNTRY_HL_LINE_COLOR = new Color(1f, 1f, 1f, 0.9f);
  protected static final TLspTextStyle COUNTRY_TEXT_STYLE = TLspTextStyle.newBuilder().font("Default-BOLD-16").textColor(white).haloColor(black).build();

  //Color and style constants for the pie charts
  protected static final float CHART_ALPHA = 1.0f;

  protected static final Map<PopulationGroup, Color> CHART_FILL_COLORS;

  static {
    CHART_FILL_COLORS = new HashMap<PopulationGroup, Color>();
    CHART_FILL_COLORS.put(PopulationGroup.MALE_0_14, new Color(0xA64B00));
    CHART_FILL_COLORS.put(PopulationGroup.MALE_15_64, new Color(0xBF7130));
    CHART_FILL_COLORS.put(PopulationGroup.MALE_65PLUS, new Color(0xFF7400));
    CHART_FILL_COLORS.put(PopulationGroup.FEMALE_0_14, new Color(0x006363));
    CHART_FILL_COLORS.put(PopulationGroup.FEMALE_15_64, new Color(0x1D7373));
    CHART_FILL_COLORS.put(PopulationGroup.FEMALE_65PLUS, new Color(0x009999));
  }

  protected static final TLspFillStyle[] CHART_FILL_STYLES = new TLspFillStyle[]{
      getChartFillStyle(PopulationGroup.MALE_0_14),
      getChartFillStyle(PopulationGroup.MALE_15_64),
      getChartFillStyle(PopulationGroup.MALE_65PLUS),
      getChartFillStyle(PopulationGroup.FEMALE_65PLUS),
      getChartFillStyle(PopulationGroup.FEMALE_15_64),
      getChartFillStyle(PopulationGroup.FEMALE_0_14)
  };
  protected static final TLspLineStyle[] CHART_LINE_STYLES = new TLspLineStyle[]{
      getChartLineStyle(PopulationGroup.MALE_0_14),
      getChartLineStyle(PopulationGroup.MALE_15_64),
      getChartLineStyle(PopulationGroup.MALE_65PLUS),
      getChartLineStyle(PopulationGroup.FEMALE_65PLUS),
      getChartLineStyle(PopulationGroup.FEMALE_15_64),
      getChartLineStyle(PopulationGroup.FEMALE_0_14)
  };

  protected static final TLspTextStyle CHART_MALE_TEXT_STYLE = TLspTextStyle.newBuilder().font("Default-BOLD-13").textColor(new Color(0xA64B00).darker()).haloColor(white).build();

  protected static final TLspTextStyle CHART_FEMALE_TEXT_STYLE = TLspTextStyle.newBuilder().font("Default-BOLD-13").textColor(new Color(0x006363).darker()).haloColor(white).build();
  protected static final TLspPinLineStyle CHART_PIN_STYLE = TLspPinLineStyle.newBuilder().color(Color.darkGray).width(1.5f).build();
  protected static final TLspIconStyle CHART_ANCHOR_ICON_STYLE = TLspIconStyle.newBuilder().icon(new TLcdSymbol(TLcdSymbol.FILLED_CIRCLE, 7, darkGray, lightGray)).build();
  protected static final String[] CHART_CAT_LABELS = new String[]{
      "\u2642 -15",
      "\u2642 15-64",
      "\u2642 65+",
      "\u2640 65+",
      "\u2640 15-64",
      "\u2640 -15",
  };

  //General constants
  protected static final double FADE_IN_DURATION = 0.5;

  protected static final double FADE_OUT_DURATION = 0.5;
  protected static final double ELEVATION_OFFSET = 100000;

  protected HashMap<Integer, ILspStyler> fStylerIndex = new HashMap<Integer, ILspStyler>();

  // The object that is currently under the mouse cursor (there can be only one object which is fading in)
  protected Object fFadeInObject;

  protected ILcdModel fFadeInModel;

  // All the objects being animated (i.e. 1 fading in, all the others fading out)
  protected Map<Object, Integer> fObject2Alpha = synchronizedMap(new IdentityHashMap<Object, Integer>());

  /**
   * Population groups represented in the pie chart
   */
  public enum PopulationGroup {
    MALE_0_14,
    MALE_15_64,
    MALE_65PLUS,
    FEMALE_0_14,
    FEMALE_15_64,
    FEMALE_65PLUS
  }

  /**
   * The default constructor for the AnimatedHighlightStyler
   */
  protected AnimatedHighlightStyler() {
  }

  /**
   * Returns a fill style belonging to the given population group
   * @param aGroup the population group for which to return the fill style
   * @return the fill style belonging to the given population group
   */
  private static TLspFillStyle getChartFillStyle(PopulationGroup aGroup) {
    return TLspFillStyle.newBuilder()
                        .color(CHART_FILL_COLORS.get(aGroup))
                        .opacity(CHART_ALPHA)
                        .zOrder(1).build();
  }

  /**
   * Returns a line style belonging to the given population group
   * @param aGroup the population group for which to return the line style
   * @return the line style belonging to the given population group
   */
  private static TLspLineStyle getChartLineStyle(PopulationGroup aGroup) {
    return TLspLineStyle.newBuilder()
                        .color(CHART_FILL_COLORS.get(aGroup).darker())
                        .opacity(CHART_ALPHA)
                        .zOrder(1).build();
  }

  @Override
  public void objectHighlighted(Object aObject, TLspPaintRepresentationState aPrs, TLspContext aContext) {
    if (fFadeInObject != aObject) {
      playFadeOutAnimation(fFadeInObject, fFadeInModel);
      ILcdModel model = aContext == null ? null : aContext.getModel();
      playFadeInAnimation(aObject, model);
      fFadeInObject = aObject;
      fFadeInModel = model;
    }
  }

  @Override
  public void labelHighlighted(TLspLabelID aLabelID, TLspPaintState aPaintState, TLspContext aContext) {
    // Do nothing
  }

  /**
   * Plays a fade out animation for the given object.
   *
   * @param aFadeOutObject the object for which the animation is played
   * @param aFadeOutModel  the model that contains the given object
   */
  protected void playFadeOutAnimation(Object aFadeOutObject, ILcdModel aFadeOutModel) {
    if (aFadeOutObject != null) {
      Integer currentAlpha = fObject2Alpha.get(aFadeOutObject);
      if (currentAlpha == null) {
        currentAlpha = 255;
      }
      ALcdAnimationManager.getInstance().putAnimation(getAnimationKey(aFadeOutObject),
                                                      new MyAnimation(false, aFadeOutObject, aFadeOutModel, ((double) currentAlpha) / 255.0, FADE_OUT_DURATION));
    }
  }

  /**
   * Plays a fade in animation for the given object.
   *
   * @param aFadeInObject the object for which the animation is played
   * @param aFadeInModel  the model that contains the given object
   */
  protected void playFadeInAnimation(Object aFadeInObject, ILcdModel aFadeInModel) {
    if (aFadeInObject != null) {
      Integer currentAlpha = fObject2Alpha.get(aFadeInObject);
      if (currentAlpha == null) {
        currentAlpha = FADE_IN_DURATION == 0 ? 1 : 0;
      }
      ALcdAnimationManager.getInstance().putAnimation(getAnimationKey(aFadeInObject),
                                                      new MyAnimation(true, aFadeInObject, aFadeInModel, ((double) currentAlpha) / 255.0, FADE_IN_DURATION));
    }
  }

  /**
   * Returns an animation key for the given domain object
   * @param aDomainObject the domain object for which to return an animation key
   * @return an animation key for the given domain object
   */
  private Object[] getAnimationKey(Object aDomainObject) {
    return new Object[]{this, aDomainObject};
  }

  /**
   * Returns the current alpha value of the given object
   *
   * @param object
   * @return the opacity value for the given object (0-255)
   */
  protected int getCurrentAlpha(Object object) {
    Integer alpha = fObject2Alpha.get(object);
    if (alpha == null) {
      alpha = 0;
    }
    return alpha;
  }

  /**
   * Returns the population statistics for each slice of the pie chart associated with the given domain object
   * @param aObject the domain object for which to return the population statistics
   * @return the population statistics for each slice of the pie chart associated with the given domain object
   */
  protected int[] getSliceStatistics(Object aObject) {
    ILcdDataObject dataObject = (ILcdDataObject) aObject;
    return new int[]{
        (Integer) dataObject.getValue("MALE_0_14"),
        (Integer) dataObject.getValue("MALE_15_64"),
        (Integer) dataObject.getValue("65PLUS"),
        (Integer) dataObject.getValue("FEM_65PLUS"),
        (Integer) dataObject.getValue("FEM_15_64"),
        (Integer) dataObject.getValue("FEM_0_14"),
    };
  }

  /**
   * Sets the current alpha value of the given object, of the given model to the given value.
   * If the object isn't an object that is currently being animated, it is added to the list of currently animated objects.
   * @param aAnimatedObject the animated object for which to set the alpha value
   * @param aModel the model of the animated object
   * @param aAlpha the new alpha value for the given animated object
   */
  protected void setAnimatedStyle(Object aAnimatedObject, ILcdModel aModel, double aAlpha) {
    // Calculate integer value for alpha (ranges from 0 - 255)
    Integer alpha = (int) Math.round(aAlpha * 255.0);

    // Store new alpha value
    fObject2Alpha.put(aAnimatedObject, alpha);

    // fire a styleChangeEvent to update the view
    fireStyleChangeEvent(
        aModel,
        Collections.singleton(aAnimatedObject),
        Collections.<ALspStyle>emptySet()
    );
  }

  /**
   * Removes the given object from the list of objects that need to be animated
   * @param aAnimatedObject the object to remove from the list of objects that need to be animated
   */
  protected void removeAnimatedObject(Object aAnimatedObject) {
    fObject2Alpha.remove(aAnimatedObject);
    fireStyleChangeEvent(
        fFadeInModel,
        Collections.singleton(aAnimatedObject),
        Collections.<ALspStyle>emptySet());
  }

  private static final Map<ILcdShape, ILcdPoint> FOCUS_POINTS = new TLcdWeakIdentityHashMap<ILcdShape, ILcdPoint>();

  /**
   * Returns the focus point of the given shape
   * @param aShape the shape to return the focus point of
   * @return the focus point of the given shape
   */
  public static ILcdPoint getFocusPoint(ILcdShape aShape) {
    ILcdPoint focusPoint;
    synchronized (FOCUS_POINTS) {
      // Retrieve the focus point form the cache if possible
      focusPoint = FOCUS_POINTS.get(aShape);
    }

    if (focusPoint == null) {
      focusPoint = aShape.getFocusPoint();
      if (aShape instanceof ILcdShapeList) {
        ILcdShapeList shapeList = (ILcdShapeList) aShape;
        if (shapeList.getShape(0) instanceof ILcdComplexPolygon) {
          ILcdComplexPolygon complexPolygon = (ILcdComplexPolygon) shapeList.getShape(0);
          focusPoint = new TLcdLonLatHeightPoint();
          TLcdCartesian.computeInsidePoint(
              complexPolygon.getPolygon(0),
              (ILcd2DEditablePoint) focusPoint
          );
        }
      }
      synchronized (FOCUS_POINTS) {
        // Cache the focus point, since this is a complex operation
        FOCUS_POINTS.put(aShape, focusPoint);
      }
    }
    return focusPoint;
  }

  /////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Animation used to update the scalar alpha value and to initiate firing of
   * style change events whenever the setTimeImpl method is called.
   */
  private class MyAnimation extends ALcdAnimation {

    // Total duration in seconds. The actual animation might take less time
    // if the start alpha is not 0 or 1, but something in between.
    private final double fTotalDuration;

    // The actual animation duration in seconds.
    private final double fDuration;

    // Indicates whether the animation should be fading in (true) or fading out (false)
    private final boolean fFadeIn;

    // The object for which the style is animated.
    private final Object fAnimatedObject;

    // Model associated to the animated object
    private final ILcdModel fAnimatedModel;

    // Start alpha, typically 0 or 1, but can be something in between.
    private final double fStartAlpha;

    private MyAnimation(boolean aFadeIn, Object aAnimatedObject, ILcdModel aAnimatedModel, double aStartAlpha, double aTotalDuration) {
      fAnimatedObject = aAnimatedObject;
      fAnimatedModel = aAnimatedModel;
      fTotalDuration = aTotalDuration;
      fFadeIn = aFadeIn;
      fStartAlpha = aStartAlpha;
      fDuration = fFadeIn ? (1 - fStartAlpha) * fTotalDuration : (fStartAlpha - 0) * fTotalDuration;
    }

    @Override
    protected void setTimeImpl(double aTime) {
      // Scale time between 0 and 1.
      double scaledTime = aTime / getDuration();

      // First half alpha increases, second half alpha decreases
      double alpha = fStartAlpha + (fFadeIn ? scaledTime * (1 - fStartAlpha) : -scaledTime * fStartAlpha);

      // Update the style
      if (!Double.isNaN(alpha)) {
        setAnimatedStyle(fAnimatedObject, fAnimatedModel, alpha);
      }
    }

    @Override
    public double getDuration() {
      return fDuration; // seconds
    }

    @Override
    public void start() {
      setAnimatedStyle(fAnimatedObject, fAnimatedModel, fStartAlpha);
    }

    @Override
    public void stop() {
      if (!fFadeIn) {
        // when fading out, the object should be removed
        // from the 'animated objects' list when animation is done.
        removeAnimatedObject(fAnimatedObject);
      }
    }
  }
}
