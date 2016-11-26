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
package samples.gxy.clustering;

import java.awt.Graphics;
import java.util.Set;

import com.luciad.model.ILcdModel;
import com.luciad.model.transformation.clustering.TLcdCluster;
import com.luciad.view.gxy.ILcdGXYContext;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.ILcdGXYLayerFactory;
import com.luciad.view.gxy.ILcdGXYPainter;
import com.luciad.view.gxy.TLcdGXYLayer;
import com.luciad.view.gxy.TLcdGXYShapePainter;
import com.luciad.view.gxy.asynchronous.TLcdGXYAsynchronousEditableLabelsLayerWrapper;

import samples.common.MapColors;

/**
 * Creates a layer that can paint clusters.
 *
 * @since 2016.0
 */
public class ClusterLayerFactory implements ILcdGXYLayerFactory {

  @Override
  public ILcdGXYLayer createGXYLayer(ILcdModel aModel) {
    TLcdGXYLayer clusteredLayer = new TLcdGXYLayer(aModel, "Clustered events");
    clusteredLayer.setLabeled(true);

    final TLcdGXYShapePainter regularPainter = new TLcdGXYShapePainter();
    regularPainter.setIcon(MapColors.createIcon(false));
    regularPainter.setSelectedIcon(MapColors.createIcon(true));

    TLcdGXYShapePainter clusterPainter = new TLcdGXYShapePainter() {

      @Override
      public void paint(Graphics aGraphics, int aMode, ILcdGXYContext aGXYContext) {
        if ((aMode & ILcdGXYPainter.SELECTED) != 0) {
          Object object = getObject();
          if (object instanceof TLcdCluster) {
            TLcdCluster cluster = (TLcdCluster) object;
            Object oldObject = regularPainter.getObject();
            for (Object element : cluster.getComposingElements()) {
              regularPainter.setObject(element);
              regularPainter.paint(aGraphics, aMode, aGXYContext);
            }
            regularPainter.setObject(oldObject);
          }
        }
        super.paint(aGraphics, aMode, aGXYContext);
      }

      /**
       * Clusters are not editable.
       * See {@link com.luciad.model.transformation.clustering.ILcdClusterShapeProvider#getShape(Set, ILcdModel)}.
       *
       */
      @Override
      public boolean edit(Graphics aGraphics, int aMode, ILcdGXYContext aGXYContext) {
        return false;
      }

    };
    clusterPainter.setIconProvider(new ClusterIconProvider(false));
    clusterPainter.setSelectedIconProvider(new ClusterIconProvider(true));

    ClusterAwarePaintingProvider clusterAwarePaintingProvider = new ClusterAwarePaintingProvider(clusterPainter, regularPainter);
    clusteredLayer.setGXYPainterProvider(clusterAwarePaintingProvider);
    clusteredLayer.setGXYEditorProvider(clusterAwarePaintingProvider);

    return new TLcdGXYAsynchronousEditableLabelsLayerWrapper(clusteredLayer);
  }

}
