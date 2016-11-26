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
package samples.lightspeed.internal.havelsan.tactical;

import static com.luciad.util.concurrent.TLcdLockUtil.Lock;
import static com.luciad.util.concurrent.TLcdLockUtil.writeLock;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Enumeration;

import javax.swing.Timer;

import com.luciad.model.ILcdModel;
import com.luciad.model.TLcdVectorModel;
import com.luciad.view.lightspeed.layer.ALspSingleLayerFactory;
import com.luciad.view.lightspeed.layer.ILspInteractivePaintableLayer;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.TLspPaintState;
import com.luciad.view.lightspeed.layer.shape.TLspShapeLayerBuilder;

/**
 * @author tomn
 * @since 2012.0
 */
public class TacticalLayerFactory extends ALspSingleLayerFactory {
  @Override
  public ILspLayer createLayer(ILcdModel aModel) {
    final ILspInteractivePaintableLayer layer = TLspShapeLayerBuilder.newBuilder()
                                                                     .model(aModel)
                                                                     .layerType(ILspLayer.LayerType.REALTIME)
                                                                     .culling(false)
                                                                     .selectable(false)
                                                                     .bodyStyler(TLspPaintState.REGULAR, new TacticalObjectStyler())
                                                                     .build();

    final TLcdVectorModel model = (TLcdVectorModel) aModel;
    final Timer t = new Timer(1000, new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        if (layer.isVisible()) {
          try (Lock autoUnlock = writeLock(model)) {
            Enumeration elements = model.elements();
            while (elements.hasMoreElements()) {
              TacticalObject object = (TacticalObject) elements.nextElement();
              object.setTime(object.getTime() + 0.005);
            }
            model.allElementsChanged(ILcdModel.FIRE_NOW);
          }
        }
      }
    });
    t.start();

    return layer;
  }

  @Override
  public boolean canCreateLayers(ILcdModel aModel) {
    return aModel.getModelDescriptor() instanceof TacticalModelDescriptor;
  }
}
