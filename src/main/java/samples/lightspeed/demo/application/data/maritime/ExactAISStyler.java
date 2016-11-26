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
package samples.lightspeed.demo.application.data.maritime;

import static com.luciad.util.expression.TLcdExpressionFactory.*;

import static samples.lightspeed.demo.application.data.maritime.ExactAISModelDescriptor.*;

import java.awt.Color;
import java.awt.image.IndexColorModel;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.lang.ref.SoftReference;
import java.util.Collection;

import com.luciad.gui.ILcdIcon;
import com.luciad.gui.TLcdImageIcon;
import com.luciad.util.expression.ILcdExpression;
import com.luciad.util.expression.ILcdParameter;
import com.luciad.view.animation.ALcdAnimation;
import com.luciad.view.animation.ALcdAnimationManager;
import com.luciad.view.lightspeed.TLspContext;
import com.luciad.view.lightspeed.style.ALspStyle;
import com.luciad.view.lightspeed.style.ILspWorldElevationStyle;
import com.luciad.view.lightspeed.style.TLspPlotStyle;
import com.luciad.view.lightspeed.style.styler.ALspStyleCollector;
import com.luciad.view.lightspeed.style.styler.ALspStyler;

;

/**
 * @author tomn
 * @since 2012.1
 */
public class ExactAISStyler extends ALspStyler {

  public static final int NO_MMSI_SELECTED = -1;
  public static final float unknown_value = -777777f;

  private final PropertyChangeSupport fPropertyChangeSupport = new PropertyChangeSupport(this);

  private IndexColorModel fCustomColorModel = null;

  /**
   * Important note: always add all attributes to {@link #getAttributes}
   * TODO
   */
  private final ILcdExpression<Float> fMMSIAttr = attribute("mmsi", Float.class, new AttributeValueProvider<Float>() {
    @Override
    public Float getValue(Object aObject, Object aGeometry) {
      int id = ((AISPlot) aObject).getID();
      return (float) id;
    }
  });
  private final ILcdExpression<Float> fTimeStampAttr = attribute("timeStamp", Float.class, new AttributeValueProvider<Float>() {
    @Override
    public Float getValue(Object aObject, Object aGeometry) {
      long timestamp = ((AISPlot) aObject).getTimeStamp();
      return (float) ((timestamp - fMinTime) / 1000.0);
    }
  });
  private final ILcdExpression<Float> fNavStatusAttr = attribute("navigationalStatus", Float.class, new AttributeValueProvider<Float>() {
    @Override
    public Float getValue(Object aObject, Object aGeometry) {
      int status = ((AISPlot) aObject).getNavigationalStatus();
      return (float) status;
    }
  });

  private final ILcdExpression<Float> fShipTypeAttr = attribute("shipType", Float.class, new AttributeValueProvider<Float>() {
    @Override
    public Float getValue(Object aObject, Object aGeometry) {
      int id = ((AISPlot) aObject).getID();
      ExactAISModelDescriptor md = fModelDescriptorRef.get();
      if (md == null) {
        return (float) ShipType.OTHER_CATEGORY;
      } else {
        ShipDescriptor shipDescriptor = md.getShipDescriptor(id);
        return shipDescriptor == null ? ShipType.OTHER_CATEGORY : (float) shipDescriptor.getShipType();
      }
    }
  });

  private final ILcdExpression<Float> fShipDepthAttr = attribute("shipDepthType", Float.class, new AttributeValueProvider<Float>() {
    @Override
    public Float getValue(Object aObject, Object aGeometry) {
      double oceanFloorDepth = ((AISPlot) aObject).getOceanFloorDepth();
      int id = ((AISPlot) aObject).getID();
      ExactAISModelDescriptor md = fModelDescriptorRef.get();
      if (md != null) {
        ShipDescriptor shipDescriptor = md.getShipDescriptor(id);
        double draught = shipDescriptor == null ? Double.NaN : shipDescriptor.getDraught();
        if (Double.isNaN(draught) || Double.isNaN(oceanFloorDepth)) {
          return unknown_value;
        }
        return (float) (oceanFloorDepth - draught);
      }
      return unknown_value;
    }
  });
  private final ILcdExpression<Float> fShipLengthAttr = attribute("shipLength", Float.class, new AttributeValueProvider<Float>() {
    @Override
    public Float getValue(Object aObject, Object aGeometry) {
      int id = ((AISPlot) aObject).getID();
      ExactAISModelDescriptor md = fModelDescriptorRef.get();
      if (md != null) {
        ShipDescriptor shipDescriptor = md.getShipDescriptor(id);
        int length = shipDescriptor == null ? -1 : shipDescriptor.getLength();
        if (length != -1) {
          return (float) length;
        }
      }
      return 10.f;
    }
  });

  private final ILcdParameter<Float> fHighlightedMMSIParam = parameter("highlightedMMSI", -1f);
  private final ILcdParameter<Float> fSelectedMMSIParam = parameter("selectedMMSI", -1f);
  private final ILcdParameter<Float> fMinTimeParam = parameter("timeMin", 0f);
  private final ILcdParameter<Float> fMaxTimeParam = parameter("timeMax", 1f);
  // 0 = no highlight, 1 = full highlight
  private ILcdParameter<Float> fHighlightTimeParam = parameter("highlightTime", 0f);
  private ILcdParameter<Float> fSelectTimeParam = parameter("selectTime", 0f);

  private final TLcdImageIcon fVesselIcon;
  private final ILcdIcon[] fIconMap;

  private TLspPlotStyle fTrackStyle;

  private SoftReference<ExactAISModelDescriptor> fModelDescriptorRef;
  private long fMinTime = -1;
  private long fMaxTime = -1;
  private int fHighlightedMMSI = NO_MMSI_SELECTED;
  private int fSelectedMMSI = NO_MMSI_SELECTED;
  //If true, paints density. Specific style depends on fOceanDepthBased.
  private boolean fPaintDensity = false;
  private double fDensityIconSize = 50e3;
  //If false, paints based on ship type. Otherwise paints based on ocean depth (green=valid, blue=unknown, red=invalid)
  private boolean fStyleBasedOnOceanDepth = false;

  public ExactAISStyler(String aIconDir) {
    fIconMap = new ILcdIcon[16];
    fVesselIcon = new TLcdImageIcon((aIconDir == null ? "" : aIconDir) + "/vessel2.png");

    for (int i = 0; i < 16; i++) {
      if (i == NavigationalStatus.ANCHORED) {
        fIconMap[i] = new TLcdImageIcon((aIconDir == null ? "" : aIconDir) + "/" + i + ".png");
      } else {
        fIconMap[i] = fVesselIcon;
      }
    }
  }

  public ILcdExpression[] getAttributes() {
    return new ILcdExpression[]{fMMSIAttr, fTimeStampAttr, fNavStatusAttr, fShipTypeAttr, fShipLengthAttr/*, fShipDepthAttr*/};
  }

  @Override
  public void style(Collection<?> aObjects, ALspStyleCollector aStyleCollector, TLspContext aContext) {
    aStyleCollector.objects(aObjects).style(getStyle(aContext)).submit();
  }

  private synchronized ALspStyle getStyle(TLspContext aContext) {
    ExactAISModelDescriptor actualMd = (ExactAISModelDescriptor) aContext.getModel().getModelDescriptor();
    if (fTrackStyle != null && fModelDescriptorRef != null && fModelDescriptorRef.get() == actualMd) {
      return fTrackStyle;
    }

    fModelDescriptorRef = new SoftReference<>(actualMd);

    fMinTime = actualMd.getMinTime();
    fMinTimeParam.setValue((float) (getMinTime() / 1000.0));
    fMaxTime = actualMd.getMaxTime();
    fMaxTimeParam.setValue((float) (getMaxTime() / 1000.0));

    fPropertyChangeSupport.firePropertyChange("time", null, null);

    ILcdExpression<Boolean> isHighlighted = eq(fMMSIAttr, fHighlightedMMSIParam);
    ILcdExpression<Boolean> isSelected = eq(fMMSIAttr, fSelectedMMSIParam);

    ILcdExpression<Color> coloringScheme;
    if (fStyleBasedOnOceanDepth) {
      coloringScheme = cases(new Color(255, 255, 255)).
                                                          when(eq(fShipDepthAttr, unknown_value), new Color(0, 0, 255)).
                                                          when(gt(fShipDepthAttr, 0.f), new Color(0, 255, 0)).
                                                          when(lte(fShipDepthAttr, 0.f), new Color(255, 0, 0));
    } else {
      coloringScheme = cases(new Color(217, 225, 212)).
                                                          when(eq(fShipTypeAttr, (float) ShipType.VESSEL_FISHING), new Color(189, 255, 160)).
                                                          when(lte(fShipTypeAttr, ShipType.VESSEL_CATEGORY + 9f), new Color(227, 179, 255)).
                                                          when(lte(fShipTypeAttr, ShipType.FUTURE_10_CATEGORY + 9f), new Color(255, 188, 138)).
                                                          when(lte(fShipTypeAttr, ShipType.WINGED_IN_GROUND_CATEGORY + 9f), new Color(197, 255, 172)).
                                                          when(lte(fShipTypeAttr, ShipType.UNNAMED_30_CATEGORY + 9f), new Color(206, 234, 255)).
                                                          when(lte(fShipTypeAttr, ShipType.HIGH_SPEED_CRAFT_CATEGORY + 9f), new Color(255, 211, 182)).
                                                          when(lte(fShipTypeAttr, ShipType.SPECIAL_CRAFT_CATEGORY + 9f), new Color(159, 255, 204)).
                                                          when(lte(fShipTypeAttr, ShipType.PASSENGER_SHIP_CATEGORY + 9f), new Color(156, 188, 235)).
                                                          when(lte(fShipTypeAttr, ShipType.CARGO_SHIP_CATEGORY + 9f), new Color(204, 204, 204)).
                                                          when(lte(fShipTypeAttr, ShipType.TANKER_SHIP_CATEGORY + 9f), new Color(208, 189, 120));
    }

    int iconSize = Math.max(fVesselIcon.getIconWidth(), fVesselIcon.getIconHeight());
    float smallShipSize = 8f / iconSize;
    ILcdExpression<Float> scaleBasedOnShipLength = cases(24f / iconSize).
                                                                            when(lt(fShipLengthAttr, 10.f), smallShipSize).
                                                                            when(lt(fShipLengthAttr, 50.f), smallShipSize * 1.5f).
                                                                            when(lt(fShipLengthAttr, 100.f), smallShipSize * 2.0f);

    ILcdExpression<Float> scaleBasedOnSelectedMMSI = ifThenElse(isSelected, add(1f, fSelectTimeParam), constant(1f));

    ILcdExpression<Boolean> visibilityFunction;
    ILcdExpression<Float> scaleFunction;
    ILcdExpression<Color> highlightModulationFunction;
    ILcdExpression<Color> selectionColorFunction = constant(new Color(255, 190, 0));
    if (fPaintDensity && fStyleBasedOnOceanDepth) {
      visibilityFunction = and(and(or(isSelected, between(fTimeStampAttr, fMinTimeParam, fMaxTimeParam)),
                                   lte(fShipDepthAttr, 0.f)), neq(fShipDepthAttr, unknown_value));
      scaleFunction = constant(10.0f * smallShipSize);
      highlightModulationFunction = coloringScheme;
    } else {
      visibilityFunction = or(isSelected, between(fTimeStampAttr, fMinTimeParam, fMaxTimeParam));
      if (!fPaintDensity) {
        scaleFunction = mul(scaleBasedOnShipLength, scaleBasedOnSelectedMMSI);
      } else {
        scaleFunction = constant(smallShipSize * 2.0f);
      }
      if (fStyleBasedOnOceanDepth) {
        selectionColorFunction = coloringScheme;
        highlightModulationFunction = mix(coloringScheme, constant(Color.white), mul(fHighlightTimeParam, 0.8f));
        visibilityFunction = and(visibilityFunction, neq(fShipDepthAttr, unknown_value));
      } else {
        highlightModulationFunction = mix(coloringScheme, constant(Color.red), fHighlightTimeParam);
      }
    }

    TLspPlotStyle.Builder plotStyleBuilder = TLspPlotStyle.newBuilder()
                                                          .elevationMode(ILspWorldElevationStyle.ElevationMode.ON_TERRAIN)
                                                          .useOrientation(true)
                                                          .visibility(visibilityFunction)
                                                          .scale(scaleFunction)
                                                          .modulationColor(
                                                              ifThenElse(
                                                                  isSelected,
                                                                  selectionColorFunction,
                                                                  ifThenElse(
                                                                      isHighlighted,
                                                                      highlightModulationFunction,
                                                                      scale(coloringScheme, sub(1f, div(fSelectTimeParam, 2f)))
                                                                  )
                                                              )
                                                          )
                                                          .opacity(ifThenElse(or(isSelected, isHighlighted), constant(1f), sub(0.75f, div(fSelectTimeParam, 2f))));
    if (!fPaintDensity) {
      plotStyleBuilder.icon(map(fNavStatusAttr, fIconMap, new TLcdImageIcon()));
      plotStyleBuilder.automaticScaling(fDensityIconSize);
    } else {
      if (fCustomColorModel == null) {
        plotStyleBuilder.density(true);
      } else {
        plotStyleBuilder.density(fCustomColorModel, 0.5);
      }
      if (!fStyleBasedOnOceanDepth) {
        plotStyleBuilder.automaticScaling(fDensityIconSize);
      }
    }
    fTrackStyle = plotStyleBuilder.build();
    return fTrackStyle;
  }

  public IndexColorModel getCustomColorModel() {
    return fCustomColorModel;
  }

  public void setCustomColorModel(IndexColorModel aCustomColorModel) {
    fCustomColorModel = aCustomColorModel;
  }

  public boolean isPaintDensity() {
    return fPaintDensity;
  }

  public void setPaintDensity(boolean aPaintDensity) {
    fPaintDensity = aPaintDensity;
    fTrackStyle = null;
    fireStyleChangeEvent();
  }

  public double getDensityIconSize() {
    return fDensityIconSize;
  }

  public void setDensityIconSize(double aDensityIconSize) {
    fDensityIconSize = aDensityIconSize;
  }

  public boolean isStyleBasedOnOceanDepth() {
    return fStyleBasedOnOceanDepth;
  }

  public void setStyleBasedOnOceanDepth(boolean aStyleBasedOnOceanDepth) {
    fStyleBasedOnOceanDepth = aStyleBasedOnOceanDepth;
    fTrackStyle = null;
    fireStyleChangeEvent();
  }

  public long getMinTime() {
    return 0;
  }

  public long getMaxTime() {
    return fMaxTime - fMinTime;
  }

  public double getTimeRangeMin() {
    return fMinTimeParam.getValue() * 1000.0;
  }

  public double getTimeRangeMax() {
    return fMaxTimeParam.getValue() * 1000.0;
  }

  public void setTimeRange(double aMin, double aMax) {
    fMinTimeParam.setValue((float) (aMin / 1000.0));
    fMaxTimeParam.setValue((float) (aMax / 1000.0));
  }

  public void addPropertyChangeListener(PropertyChangeListener listener) {
    fPropertyChangeSupport.addPropertyChangeListener(listener);
  }

  public void removePropertyChangeListener(PropertyChangeListener listener) {
    fPropertyChangeSupport.removePropertyChangeListener(listener);
  }

  public void setHighlightedMMSI(final int aMMSI) {
    if (aMMSI != fHighlightedMMSI) {
      fHighlightedMMSI = aMMSI;

      float duration = 0.33f;

      if (fHighlightedMMSI == NO_MMSI_SELECTED) {
        // Fade out
        ALcdAnimationManager.getInstance().putAnimation(
            fHighlightTimeParam,
            new ParamAnimation(
                fHighlightTimeParam,
                fHighlightTimeParam.getValue(),
                0f,
                fHighlightTimeParam.getValue() * duration
            ) {
              @Override
              public void stop() {
                super.stop();
                fHighlightedMMSIParam.setValue((float) fHighlightedMMSI);
              }
            }
        );
      } else {
        // Fade in
        ALcdAnimationManager.getInstance().putAnimation(
            fHighlightTimeParam,
            new ParamAnimation(
                fHighlightTimeParam,
                fHighlightTimeParam.getValue(),
                1f,
                (1f - fHighlightTimeParam.getValue()) * duration
            ) {
              @Override
              public void start() {
                fHighlightedMMSIParam.setValue((float) fHighlightedMMSI);
                super.start();
              }
            }
        );
      }
    }
  }

  public void setSelectedMMSI(final int aMMSI) {
    if (aMMSI != fSelectedMMSI) {
      fSelectedMMSI = aMMSI;

      float duration = 0.33f;

      if (fSelectedMMSI == NO_MMSI_SELECTED) {
        // Fade out
        ALcdAnimationManager.getInstance().putAnimation(
            fSelectTimeParam,
            new ParamAnimation(
                fSelectTimeParam,
                fSelectTimeParam.getValue(),
                0f,
                fSelectTimeParam.getValue() * duration
            ) {
              @Override
              public void stop() {
                super.stop();
                fSelectedMMSIParam.setValue((float) fSelectedMMSI);
              }
            }
        );
      } else {
        // Fade in
        ALcdAnimationManager.getInstance().putAnimation(
            fSelectTimeParam,
            new ParamAnimation(
                fSelectTimeParam,
                fSelectTimeParam.getValue(),
                1f,
                (1f - fSelectTimeParam.getValue()) * duration
            ) {
              @Override
              public void start() {
                fSelectedMMSIParam.setValue((float) fSelectedMMSI);
                super.start();
              }
            }
        );
      }
    }
  }

  public static class ParamAnimation extends ALcdAnimation {
    private final ILcdParameter<Float> fParameter;
    private final float fMin;
    private final float fMax;

    public ParamAnimation(ILcdParameter<Float> aParameter, float aMin, float aMax, float aDuration) {
      super(aDuration);
      fParameter = aParameter;
      fMin = aMin;
      fMax = aMax;
    }

    @Override
    protected void setTimeImpl(double aTime) {
      double v = ALcdAnimation.interpolate(fMin, fMax, aTime / getDuration());
      fParameter.setValue((float) v);
    }

    @Override
    public void start() {
      fParameter.setValue(fMin);
    }

    @Override
    public void stop() {
      fParameter.setValue(fMax);
    }
  }
}
