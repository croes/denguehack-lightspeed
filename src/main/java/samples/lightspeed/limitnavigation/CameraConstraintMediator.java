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
package samples.lightspeed.limitnavigation;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import com.luciad.util.ILcdDisposable;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.camera.ALspCameraConstraint;
import com.luciad.view.lightspeed.camera.ALspViewXYZWorldTransformation;
import com.luciad.view.lightspeed.camera.TLspViewXYZWorldTransformation2D;
import com.luciad.view.lightspeed.camera.TLspViewXYZWorldTransformation3D;

/**
 * Keeps the correct navigation constraint configured on the view's view-to-world transformation.
 */
public class CameraConstraintMediator implements ILcdDisposable {

  private final MyPropertyChangeListener fPropertyChangeListener = new MyPropertyChangeListener();
  private final ILspView fView;
  private boolean fConstrainNavigation = true;

  private ALspCameraConstraint<TLspViewXYZWorldTransformation2D> fConstraint2D;
  private ALspCameraConstraint<TLspViewXYZWorldTransformation3D> fConstraint3D;

  /**
   * Installs the given camera constraints on the given view.
   * If the view's reference changes, the appropriate constraint is installed.
   */
  public CameraConstraintMediator(ILspView aView,
                                  ALspCameraConstraint<TLspViewXYZWorldTransformation2D> aConstraint2D,
                                  ALspCameraConstraint<TLspViewXYZWorldTransformation3D> aConstraint3D) {
    fView = aView;
    fConstraint2D = aConstraint2D;
    fConstraint3D = aConstraint3D;
    aView.addPropertyChangeListener(fPropertyChangeListener);
    addConstraint(fView.getViewXYZWorldTransformation());
  }

  public boolean isConstrain() {
    return fConstrainNavigation;
  }

  /**
   * Toggles whether the constraints are active or not.
   */
  public void setConstrain(boolean aConstrainNavigation) {
    boolean oldValue = fConstrainNavigation;
    fConstrainNavigation = aConstrainNavigation;
    if (oldValue != aConstrainNavigation) {
      ALspViewXYZWorldTransformation transformation = fView.getViewXYZWorldTransformation();
      removeConstraint(transformation);
      if (aConstrainNavigation) {
        addConstraint(transformation);
      }
    }
  }

  private void addConstraint(ALspViewXYZWorldTransformation aTransformation) {
    removeConstraint(aTransformation);
    if (fConstraint2D != null && aTransformation instanceof TLspViewXYZWorldTransformation2D) {
      TLspViewXYZWorldTransformation2D transformation = (TLspViewXYZWorldTransformation2D) aTransformation;
      transformation.addConstraint(fConstraint2D);
    } else if (fConstraint3D != null && aTransformation instanceof TLspViewXYZWorldTransformation3D) {
      TLspViewXYZWorldTransformation3D transformation = (TLspViewXYZWorldTransformation3D) aTransformation;
      transformation.addConstraint(fConstraint3D);
    }
  }

  private void removeConstraint(ALspViewXYZWorldTransformation aTransformation) {
    if (fConstraint2D != null && aTransformation instanceof TLspViewXYZWorldTransformation2D) {
      TLspViewXYZWorldTransformation2D transformation = (TLspViewXYZWorldTransformation2D) aTransformation;
      transformation.removeConstraint(fConstraint2D);
    } else if (fConstraint3D != null && aTransformation instanceof TLspViewXYZWorldTransformation3D) {
      TLspViewXYZWorldTransformation3D transformation = (TLspViewXYZWorldTransformation3D) aTransformation;
      transformation.removeConstraint(fConstraint3D);
    }
  }

  @Override
  public void dispose() {
    removeConstraint(fView.getViewXYZWorldTransformation());
    fView.removePropertyChangeListener(fPropertyChangeListener);
  }

  private class MyPropertyChangeListener implements PropertyChangeListener {
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
      if ("viewXYZWorldTransformation".equals(evt.getPropertyName()) && fConstrainNavigation) {
        addConstraint((ALspViewXYZWorldTransformation) evt.getNewValue());
      }
    }
  }
}
