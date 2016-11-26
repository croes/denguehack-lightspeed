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
package samples.lightspeed.demo.application.data.airplots;

import static com.luciad.util.expression.TLcdExpressionFactory.*;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.luciad.datamodel.ILcdDataObject;
import com.luciad.util.expression.ILcdExpression;
import com.luciad.util.expression.ILcdParameter;
import com.luciad.util.expression.TLcdExpressionFactory;
import com.luciad.view.animation.ALcdAnimationManager;
import com.luciad.view.lightspeed.TLspContext;
import com.luciad.view.lightspeed.style.ALspStyle;
import com.luciad.view.lightspeed.style.TLspPlotStyle;
import com.luciad.view.lightspeed.style.styler.ALspStyleCollector;
import com.luciad.view.lightspeed.style.styler.ALspStyler;
import com.luciad.view.lightspeed.style.styler.ILspStyleChangeListener;
import com.luciad.view.lightspeed.style.styler.ILspStyler;
import com.luciad.view.lightspeed.style.styler.TLspStyleChangeEvent;

import samples.lightspeed.demo.application.data.maritime.ExactAISStyler;

public class HighlightingStyler extends ALspStyler implements ILspStyleChangeListener {

  private final ILcdExpression<Integer> fIdAttribute = TLcdExpressionFactory.attribute("ID", Integer.class, new AttributeValueProvider<Integer>() {
    @Override
    public Integer getValue(Object aDomainObject, Object aGeometry) {
      return getId(aDomainObject);
    }
  });

  private final ILcdParameter<Integer> fHighlightedId = parameter("highlightedID", -1);
  private final ILcdParameter<Integer> fSelectedId = parameter("selectedID", -1);
  private final ILcdParameter<Float> fHighlightTime = parameter("highlightTime", 0f);
  private final ILcdParameter<Float> fSelectTime = parameter("selectTime", 0f);

  private final ILspStyler fDelegate;

  public HighlightingStyler(ILspStyler aDelegate) {
    fDelegate = aDelegate;
    aDelegate.addStyleChangeListener(this);
  }

  @Override
  public void styleChanged(TLspStyleChangeEvent aEvent) {
    fireStyleChangeEvent(aEvent.getAffectedModel(), aEvent.getAffectedObjects(), aEvent.getAffectedStyles());
  }

  @Override
  public void style(final Collection<?> aObjects, final ALspStyleCollector aStyleCollector, TLspContext aContext) {
    fDelegate.style(aObjects, new ALspStyleCollector(aObjects) {
      @Override
      protected void submitImpl() {
        aStyleCollector.objects(aObjects).styles(pimpStyles(getStyles())).submit();
      }
    }, aContext);
  }

  private List<? extends ALspStyle> pimpStyles(List<ALspStyle> aStyles) {
    List<ALspStyle> result = new ArrayList<ALspStyle>();

    for (ALspStyle style : aStyles) {
      TLspPlotStyle original = (TLspPlotStyle) style;
      if (original.isPaintDensity()) {
        result.add(original);
      } else {
        ILcdExpression<Boolean> isHighlighted = eq(fIdAttribute, fHighlightedId);
        ILcdExpression<Boolean> isSelected = eq(fIdAttribute, fSelectedId);

        TLspPlotStyle pimpedStyle = original.asBuilder()
                                            .modulationColor(ifThenElse(isSelected,
                                                                        constant(new Color(255, 190, 0)),
                                                                        ifThenElse(isHighlighted,
                                                                                   mix(original.getColor(), constant(Color.red), fHighlightTime),
                                                                                   scale(original.getColor(), sub(1f, div(fSelectTime, 2f)))
                                                                        )
                                            ))
                                            .visibility(or(original.getVisibility(), isSelected))
                                            .scale(mul(original.getScale(), ifThenElse(isSelected, add(1f, fSelectTime), constant(1f))))
                                            .opacity(mul(original.getOpacity(), ifThenElse(or(isSelected, isHighlighted), constant(1f), sub(0.75f, div(fSelectTime, 2f)))))
                                            .build();
        result.add(pimpedStyle);
      }
    }

    return result;
  }

  public void setHighlightedObject(Object aObject) {
    tweakAnimatedParam(aObject, fHighlightedId, fHighlightTime);
  }

  public void setSelectedObject(Object aObject) {
    tweakAnimatedParam(aObject, fSelectedId, fSelectTime);
  }

  private static int getId(Object aDomainObject) {
    if (aDomainObject == null) {
      return -1;
    }
    ILcdDataObject dataObject = (ILcdDataObject) aDomainObject;
    return dataObject.getValue("Flight_nr").hashCode();
  }

  private static void tweakAnimatedParam(Object aObject, final ILcdParameter<Integer> aId, ILcdParameter<Float> aTime) {
    final int id = getId(aObject);
    if (aId.getValue() == id) {
      return;
    }

    float duration = 0.33f;

    if (id == -1) {
      // Fade out
      ALcdAnimationManager.getInstance().putAnimation(
          aTime,
          new ExactAISStyler.ParamAnimation(
              aTime,
              aTime.getValue(),
              0f,
              aTime.getValue() * duration
          ) {
            @Override
            public void stop() {
              super.stop();
              aId.setValue(id);
            }
          }
      );
    } else {
      // Fade in
      ALcdAnimationManager.getInstance().putAnimation(
          aTime,
          new ExactAISStyler.ParamAnimation(
              aTime,
              aTime.getValue(),
              1f,
              (1f - aTime.getValue()) * duration
          ) {
            @Override
            public void start() {
              aId.setValue(id);
              super.start();
            }
          }
      );
    }
  }
}
