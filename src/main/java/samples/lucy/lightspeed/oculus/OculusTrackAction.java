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
package samples.lucy.lightspeed.oculus;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import com.luciad.lucy.ILcyLucyEnv;
import com.luciad.lucy.gui.ILcyActiveSettable;
import com.luciad.lucy.gui.TLcyActionBarUtil;
import com.luciad.lucy.map.lightspeed.ILcyLspMapComponent;
import com.luciad.model.ILcdModel;
import com.luciad.shape.ILcdPoint;
import com.luciad.util.ILcdDisposable;
import com.luciad.util.ILcdFilter;
import com.luciad.view.ALcdObjectSelectionAction;
import com.luciad.view.TLcdDomainObjectContext;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.camera.ALspCameraConstraint;
import com.luciad.view.lightspeed.camera.TLspViewXYZWorldTransformation3D;
import com.luciad.view.lightspeed.camera.tracking.TLspLookFromTrackingCameraConstraint3D;
import com.luciad.view.lightspeed.camera.tracking.TLspModelElementTrackingPointProvider;
import com.luciad.view.lightspeed.layer.ILspLayer;

import samples.lucy.util.LayerUtil;

/**
 * This action will open a new Oculus view. In the Oculus view the camera will be placed so
 * that it looks from the selected objects form which the action was triggered. In the main view a look-at constraint
 * is enabled, which will make sure the object stays in the view.
 */
class OculusTrackAction extends ALcdObjectSelectionAction implements ILcdDisposable {

  private ILspView fOculusView;
  private final ILcyLucyEnv fLucyEnv;
  private final ILcyLspMapComponent fMapComponent;

  // This active settable is used to enable the look-at camera constraint to keep the object that is being followed
  // in the view.
  private final ILcyActiveSettable fLookAtActiveSettable;

  public OculusTrackAction(ILcyLspMapComponent aMapComponent, ILcyLucyEnv aLucyEnv) {
    super(aMapComponent.getMainView(), new ILcdFilter<TLcdDomainObjectContext>() {
      @Override
      public boolean accept(TLcdDomainObjectContext aObject) {
        return aObject.getLayer() instanceof ILspLayer &&
               aObject.getDomainObject() instanceof ILcdPoint;
      }
    });

    fMapComponent = aMapComponent;
    fLucyEnv = aLucyEnv;
    fLookAtActiveSettable = TLcyActionBarUtil.findActiveSettable("TLcyLspCameraLinkAddOn.linkCameraActiveSettable.LookAt",
                                                                 "popupMenu",
                                                                 fMapComponent,
                                                                 fLucyEnv.getUserInterfaceManager().getActionBarManager());
  }

  @Override
  protected void actionPerformed(ActionEvent aActionEvent, List<TLcdDomainObjectContext> aSelection) {
    // Create the Oculus view and copy all layers to it.
    fOculusView = OculusViewManager.getOculusView(this);
    LayerUtil.copyLayers(fMapComponent.getMainView(), fOculusView, fLucyEnv);

    final TLspViewXYZWorldTransformation3D v2w = (TLspViewXYZWorldTransformation3D) fOculusView.getViewXYZWorldTransformation();
    final ALspCameraConstraint<TLspViewXYZWorldTransformation3D> oculusConstraint = createLookFromConstraint(aSelection);
    v2w.addConstraint(oculusConstraint);

    fLookAtActiveSettable.setActive(true);
    fLookAtActiveSettable.addPropertyChangeListener(new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        if ("active".equals(evt.getPropertyName()) && evt.getNewValue() == Boolean.FALSE) {
          fLookAtActiveSettable.removePropertyChangeListener(this);
          v2w.removeConstraint(oculusConstraint);
          OculusViewManager.destroyOculusView();
          fOculusView = null;
        }
      }
    });

  }

  /**
   * Creates a look from camera constraint based on the currently selected objects in the view.
   *
   * @return the look from camera constraint
   */
  private ALspCameraConstraint<TLspViewXYZWorldTransformation3D> createLookFromConstraint(List<TLcdDomainObjectContext> aSelection) {
    ArrayList<Object> objects = new ArrayList<>();
    ArrayList<ILcdModel> models = new ArrayList<>();

    for (TLcdDomainObjectContext domainObjectContext : aSelection) {
      objects.add(domainObjectContext.getDomainObject());
      models.add(domainObjectContext.getModel());
    }

    TLspModelElementTrackingPointProvider provider = new TLspModelElementTrackingPointProvider();
    provider.setTrackedObjects(fMapComponent.getMainView(), objects.toArray(), models.toArray(new ILcdModel[models.size()]));

    TLspLookFromTrackingCameraConstraint3D constraint = new TLspLookFromTrackingCameraConstraint3D(true);
    constraint.setTrackingPointProvider(provider);

    return constraint;
  }

  @Override
  public void dispose() {
    fLookAtActiveSettable.setActive(false);
  }
}
