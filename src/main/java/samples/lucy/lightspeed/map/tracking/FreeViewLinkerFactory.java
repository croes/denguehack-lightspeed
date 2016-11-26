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
package samples.lucy.lightspeed.map.tracking;

import java.util.List;

import com.luciad.lucy.map.ILcyGenericMapComponent;
import com.luciad.lucy.map.cameralinking.ALcyCameraLinker;
import com.luciad.lucy.map.cameralinking.ALcyCameraLinkerFactory;
import com.luciad.lucy.map.cameralinking.lightspeed.ALcyLspCameraLinker3D;
import com.luciad.lucy.map.lightspeed.ILcyLspMapComponent;
import com.luciad.model.ILcdModel;
import com.luciad.view.ILcdLayer;
import com.luciad.view.ILcdView;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.camera.ALspCameraConstraint;
import com.luciad.view.lightspeed.camera.TLspViewXYZWorldTransformation3D;
import com.luciad.view.lightspeed.camera.tracking.ALspTrackingPointProvider;
import com.luciad.view.lightspeed.camera.tracking.TLspLookFromTrackingCameraConstraint3D;

/**
 * A linker factory used to create custom <code>ALcyLspCameraLinker</code> instances.
 *
 */
public class FreeViewLinkerFactory extends ALcyCameraLinkerFactory {

  @Override
  public boolean canCreateCameraLinker(ILcyGenericMapComponent<? extends ILcdView, ? extends ILcdLayer> aMapComponent,
                                       String aType, List<Object> aObjects, List<ILcdModel> aModels) {
    return aMapComponent instanceof ILcyLspMapComponent && "FreeView".equals(aType);
  }

  @Override
  public ALcyCameraLinker createCameraLinker(ILcyGenericMapComponent<? extends ILcdView, ? extends ILcdLayer> aMapComponent,
                                             String aType, List<Object> aObjects,
                                             List<ILcdModel> aModels) {
    return new FreeViewLinker(aObjects.toArray(),
                              aModels.toArray(new ILcdModel[aModels.size()]),
                              (ILcyLspMapComponent) aMapComponent);
  }

  /**
   * <p>This class is an extension of <code>ALcyLspCameraLinker3D</code> for a <code>TLspLookFromTrackingCameraConstraint3D</code> camera constraint.<br />
   * This means that this class should do two things:
   * <ul>
   * <li>It should be able to create <code>ALspViewXYZWorldTransformation3DConstraint</code> camera constraints.</li>
   * <li>It should be able to check whether a given environment allows to install such camera constraints.</li>
   * </ul>
   *
   */
  private static class FreeViewLinker extends ALcyLspCameraLinker3D {

    public FreeViewLinker(Object[] aObjectsToTrack, ILcdModel[] aModelsOfObjects, ILcyLspMapComponent aMapComponent) {
      super(aMapComponent, aObjectsToTrack, aModelsOfObjects);
    }

    @Override
    protected ALspCameraConstraint<TLspViewXYZWorldTransformation3D> createCameraConstraint(ALspTrackingPointProvider aTrackingPointProvider,
                                                                                            ILspView aView) {
      TLspLookFromTrackingCameraConstraint3D constraint = new TLspLookFromTrackingCameraConstraint3D(false);
      constraint.setTrackingPointProvider(aTrackingPointProvider);
      return constraint;
    }

  }

}
