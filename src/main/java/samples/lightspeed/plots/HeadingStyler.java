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
package samples.lightspeed.plots;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.luciad.datamodel.ILcdDataObject;
import com.luciad.datamodel.TLcdDataProperty;
import com.luciad.shape.ILcdBounds;
import com.luciad.shape.ILcdPoint;
import com.luciad.shape.shape2D.ILcd2DEditablePoint;
import com.luciad.shape.shape3D.ILcd3DEditablePoint;
import com.luciad.util.ILcdOriented;
import com.luciad.view.lightspeed.TLspContext;
import com.luciad.view.lightspeed.style.ALspStyle;
import com.luciad.view.lightspeed.style.TLspPlotStyle;
import com.luciad.view.lightspeed.style.styler.ALspStyleCollector;
import com.luciad.view.lightspeed.style.styler.ALspStyleCollectorWrapper;
import com.luciad.view.lightspeed.style.styler.ALspStyleTargetProvider;
import com.luciad.view.lightspeed.style.styler.ALspStyler;
import com.luciad.view.lightspeed.style.styler.ILspStyleChangeListener;
import com.luciad.view.lightspeed.style.styler.TLspStyleChangeEvent;

import samples.lightspeed.plots.datamodelstyling.DataTypeStyler;

/**
 * This styler adds an "arrow" icon that is orientated.
 *
 * It delegates to another styler, so it can wrap it.
 */
public class HeadingStyler extends ALspStyler implements ILspStyleChangeListener {

  private static final int ARROW_SIZE = 25;

  private final DataTypeStyler fDelegate;
  private final OrientedPointProvider fStyleTargetProvider;
  private boolean fEnabled = false;

  public HeadingStyler(DataTypeStyler aDelegate, TLcdDataProperty aHeadingProperty) {
    fDelegate = aDelegate;
    fStyleTargetProvider = new OrientedPointProvider(aHeadingProperty);
    aDelegate.addStyleChangeListener(this);
  }

  public void setEnabled(boolean aEnabled) {
    fEnabled = aEnabled;
    fireStyleChangeEvent();
  }

  @Override
  public void styleChanged(TLspStyleChangeEvent aEvent) {
    fireStyleChangeEvent(aEvent.getAffectedModel(), aEvent.getAffectedObjects(), aEvent.getAffectedStyles());
  }

  @Override
  public void style(Collection<?> aObjects, ALspStyleCollector aStyleCollector, TLspContext aContext) {
    TLspPlotStyle delegateStyle = fDelegate.getStyle();
    List<ALspStyle> styles = Collections.emptyList();
    if (fEnabled && !delegateStyle.isPaintDensity()) {
      styles = Collections.singletonList((ALspStyle) TLspPlotStyle.newBuilder()
                                                                  .visibility(delegateStyle.getVisibility())
                                                                  .useOrientation(true)
                                                                  .icon(new ArrowIcon(ARROW_SIZE))
                                                                  .automaticScaling(800)
                                                                  .build());
    }

    aStyleCollector.objects(aObjects).styles(styles).geometry(fStyleTargetProvider).submit();

    fDelegate.style(aObjects, new ALspStyleCollectorWrapper(aStyleCollector) {
      @Override
      public void submit() {
        super.geometry(fStyleTargetProvider);
        super.submit();
      }
    }, aContext);
  }

  private static class OrientedPointProvider extends ALspStyleTargetProvider {
    private final TLcdDataProperty fOrientationProperty;

    public OrientedPointProvider(TLcdDataProperty aOrientationProperty) {
      fOrientationProperty = aOrientationProperty;
    }

    @Override
    public void getStyleTargetsSFCT(Object aObject, TLspContext aContext, List<Object> aResultSFCT) {
      aResultSFCT.add(new OrientedPoint((ILcdPoint) aObject, (ILcdDataObject) aObject, fOrientationProperty));
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof OrientedPointProvider)) {
        return false;
      }

      OrientedPointProvider that = (OrientedPointProvider) o;

      if (!fOrientationProperty.equals(that.fOrientationProperty)) {
        return false;
      }

      return true;
    }

    @Override
    public int hashCode() {
      return fOrientationProperty.hashCode();
    }
  }

  private static class OrientedPoint implements ILcdPoint, ILcdOriented {
    private final ILcdPoint fDelegate;
    private final ILcdDataObject fDataObject;
    private final TLcdDataProperty fDataProperty;

    private OrientedPoint(ILcdPoint aDelegate, ILcdDataObject aDataObject, TLcdDataProperty aDataProperty) {
      fDelegate = aDelegate;
      fDataObject = aDataObject;
      fDataProperty = aDataProperty;
    }

    @Override
    public double getOrientation() {
      return (Double) fDataObject.getValue(fDataProperty);
    }

    @Override
    public ILcd2DEditablePoint cloneAs2DEditablePoint() {
      return fDelegate.cloneAs2DEditablePoint();
    }

    @Override
    public ILcd3DEditablePoint cloneAs3DEditablePoint() {
      return fDelegate.cloneAs3DEditablePoint();
    }

    @Override
    public boolean equals(Object aObject) {
      return fDelegate.equals(aObject);
    }

    @Override
    public double getCosX() {
      return fDelegate.getCosX();
    }

    @Override
    public double getCosY() {
      return fDelegate.getCosY();
    }

    @Override
    public double getSinX() {
      return fDelegate.getSinX();
    }

    @Override
    public double getSinY() {
      return fDelegate.getSinY();
    }

    @Override
    public double getTanX() {
      return fDelegate.getTanX();
    }

    @Override
    public double getTanY() {
      return fDelegate.getTanY();
    }

    @Override
    public double getX() {
      return fDelegate.getX();
    }

    @Override
    public double getY() {
      return fDelegate.getY();
    }

    @Override
    public double getZ() {
      return fDelegate.getZ();
    }

    @Override
    public boolean contains2D(ILcdPoint aPoint) {
      return fDelegate.contains2D(aPoint);
    }

    @Override
    public boolean contains2D(double aX, double aY) {
      return fDelegate.contains2D(aX, aY);
    }

    @Override
    public boolean contains3D(ILcdPoint aPoint) {
      return fDelegate.contains3D(aPoint);
    }

    @Override
    public boolean contains3D(double aX, double aY, double aZ) {
      return fDelegate.contains3D(aX, aY, aZ);
    }

    @Override
    public ILcdPoint getFocusPoint() {
      return fDelegate.getFocusPoint();
    }

    @Override
    public ILcdBounds getBounds() {
      return fDelegate.getBounds();
    }

    @Override
    @SuppressWarnings({"CloneDoesntDeclareCloneNotSupportedException"})
    public Object clone() {
      return new OrientedPoint(fDelegate, fDataObject, fDataProperty);
    }
  }

}
