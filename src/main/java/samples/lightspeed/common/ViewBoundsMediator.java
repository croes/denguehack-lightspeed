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
package samples.lightspeed.common;

import java.awt.Point;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;

import com.luciad.shape.ILcdPoint;
import com.luciad.view.ILcdView;
import com.luciad.view.gxy.ILcdRotationCapableGXYView;
import com.luciad.view.lightspeed.ILspAWTView;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.camera.ALspViewXYZWorldTransformation;
import com.luciad.view.lightspeed.camera.TLspViewXYZWorldTransformation2D;

/**
 * Keeps 2D view bounds synchronized.
 *
 * @since 2013.1
 */
public class ViewBoundsMediator {

  private final Collection<ILspView> fLspViews;
  private final Collection<ILcdRotationCapableGXYView> fGXYViews;

  private final PropertyChangeListener fGXYViewNavigationListener = new PropertyChangeListener() {
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
      apply((ILcdRotationCapableGXYView) evt.getSource());
    }
  };

  private final PropertyChangeListener fLspViewNavigationListener = new PropertyChangeListener() {
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
      ALspViewXYZWorldTransformation source = (ALspViewXYZWorldTransformation) evt.getSource();
      apply(source);
    }
  };

  /**
   * Start mediating the bounds between the given views.
   * <p> The views must be either {@link ILcdRotationCapableGXYView} or {@link ILspAWTView}. </p>
   * <p> Use {@link #stop()} to remove the listeners. </p>
   *
   * @param aViews The views to synchronize
   *
   * @return The mediator
   */
  public static ViewBoundsMediator start(ILcdView... aViews) {
    Collection<ILspView> lspViews = new ArrayList<ILspView>();
    Collection<ILcdRotationCapableGXYView> aGXYViews = new ArrayList<ILcdRotationCapableGXYView>();
    for (ILcdView view : aViews) {
      if (view instanceof ILspView) {
        lspViews.add((ILspView) view);
      } else if (view instanceof ILcdRotationCapableGXYView) {
        aGXYViews.add((ILcdRotationCapableGXYView) view);
      } else {
        throw new UnsupportedOperationException("Cannot handle view " + view);
      }
    }
    return new ViewBoundsMediator(lspViews, aGXYViews);
  }

  private ViewBoundsMediator(Collection<ILspView> aViews, Collection<ILcdRotationCapableGXYView> aGXYViews) {
    fLspViews = aViews;
    fGXYViews = aGXYViews;

    for (ILcdRotationCapableGXYView gxyView : fGXYViews) {
      gxyView.addPropertyChangeListener(fGXYViewNavigationListener);
    }
    for (ILspView lspView : fLspViews) {
      lspView.getViewXYZWorldTransformation().addPropertyChangeListener(fLspViewNavigationListener);
    }

    apply(fLspViews.iterator().next().getViewXYZWorldTransformation());
  }

  public void stop() {
    for (ILcdRotationCapableGXYView gxyView : fGXYViews) {
      gxyView.removePropertyChangeListener(fGXYViewNavigationListener);
    }
    for (ILspView lspView : fLspViews) {
      lspView.getViewXYZWorldTransformation().removePropertyChangeListener(fLspViewNavigationListener);
    }
  }

  private void apply(ILcdPoint aWorldOrigin, Point aViewOrigin, double aScale, double aRotation, Object aSource) {
    for (ILspView lspView : fLspViews) {
      ALspViewXYZWorldTransformation v2w = lspView.getViewXYZWorldTransformation();
      if (aSource == v2w) {
        continue;
      }
      if (v2w instanceof TLspViewXYZWorldTransformation2D) {
        v2w.removePropertyChangeListener(fLspViewNavigationListener);
        ((TLspViewXYZWorldTransformation2D) v2w).lookAt(aWorldOrigin, aViewOrigin, aScale, aRotation);
        v2w.addPropertyChangeListener(fLspViewNavigationListener);
      }
    }

    for (ILcdRotationCapableGXYView gxyView : fGXYViews) {
      if (aSource == gxyView) {
        continue;
      }
      gxyView.removePropertyChangeListener(fGXYViewNavigationListener);
      gxyView.setWorldOrigin(aWorldOrigin);
      gxyView.setViewOrigin(aViewOrigin);
      gxyView.setScale(aScale);
      gxyView.setRotation(aRotation);
      gxyView.addPropertyChangeListener(fGXYViewNavigationListener);
    }
  }

  private void apply(ALspViewXYZWorldTransformation aSource) {
    if (aSource instanceof TLspViewXYZWorldTransformation2D) {
      TLspViewXYZWorldTransformation2D v2w = (TLspViewXYZWorldTransformation2D) aSource;
      apply(v2w.getWorldOrigin(), v2w.getViewOrigin(), aSource.getScale(), v2w.getRotation(), aSource);
    }
  }

  private void apply(ILcdRotationCapableGXYView aSource) {
    apply(aSource.getWorldOrigin(), aSource.getViewOrigin(), aSource.getScale(), aSource.getRotation(), aSource);
  }
}

