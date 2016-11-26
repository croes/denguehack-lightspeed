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
package samples.lightspeed.demo.application.data.milsym;

import com.luciad.model.transformation.clustering.TLcdCluster;
import com.luciad.shape.ALcdShape;
import com.luciad.shape.ILcdPoint;
import com.luciad.shape.shape2D.ILcd2DEditablePoint;
import com.luciad.symbology.app6a.model.TLcdEditableAPP6AObject;
import com.luciad.view.lightspeed.layer.TLspPaintState;
import com.luciad.view.lightspeed.style.styler.ILspStyler;

import samples.symbology.lightspeed.ClusterAwareMilitarySymbologyLabelStylerWrapper;

/**
 * Takes care of labeling clusters.
 */
public class ClusterAwareMilSymLabelStylerWrapper extends ClusterAwareMilitarySymbologyLabelStylerWrapper {

  public ClusterAwareMilSymLabelStylerWrapper(ILspStyler aStyler, TLspPaintState aPaintState) {
    super(aStyler, aPaintState, Option.ADD_CHILD_STYLE);
  }

  @Override
  protected TLcdEditableAPP6AObject getRepresentativeElement(TLcdCluster<?> aCluster) {
    TLcdEditableAPP6AObject representativeModelElement = (TLcdEditableAPP6AObject) aCluster.getComposingElements().iterator().next();
    //Find the element whose location is used for the cluster.
    for (Object symbol : aCluster.getComposingElements()) {
      if (symbol instanceof TLcdEditableAPP6AObject) {
        TLcdEditableAPP6AObject app6AObject = (TLcdEditableAPP6AObject) symbol;
        ILcdPoint point = app6AObject.getPointList().getPoint(0);
        ILcd2DEditablePoint clusterLocation = point.cloneAs2DEditablePoint();
        clusterLocation.move2D(ALcdShape.fromDomainObject(aCluster).getFocusPoint());
        if (point.equals(clusterLocation)) {
          representativeModelElement = app6AObject;
        }
      }
    }
    return representativeModelElement;
  }

}
