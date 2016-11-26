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
package samples.lightspeed.decoder.object3d.openflight;

import java.io.File;

import com.luciad.format.object3d.openflight.TLcdOpenFlightModelDescriptor;
import com.luciad.format.object3d.openflight.lightspeed.TLspOpenFlightPainter;
import com.luciad.model.ILcdModel;
import com.luciad.util.service.LcdService;
import com.luciad.view.lightspeed.layer.ALspSingleLayerFactory;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.ILspLayerFactory;
import com.luciad.view.lightspeed.layer.shape.TLspShapeLayerBuilder;

/**
 * Layer factor that creates layers for models that contain OpenFlight meshes.
 */
@LcdService(service = ILspLayerFactory.class)
public class FLTLayerFactory extends ALspSingleLayerFactory {

  @Override
  public boolean canCreateLayers(ILcdModel aModel) {
    return (aModel.getModelDescriptor() instanceof TLcdOpenFlightModelDescriptor);
  }

  @Override
  public ILspLayer createLayer(ILcdModel aModel) {
    if (aModel.getModelDescriptor() instanceof TLcdOpenFlightModelDescriptor) {
      TLspOpenFlightPainter painter = new TLspOpenFlightPainter();
      File sourceFile = new File(aModel.getModelDescriptor().getSourceName());
      painter.addTextureSearchPath(sourceFile.getAbsoluteFile().getParent());

      String fileName = sourceFile.getName();
      int index_extension = fileName.toLowerCase().lastIndexOf(".");
      String label = fileName.substring(0, index_extension);

      return TLspShapeLayerBuilder.newBuilder()
                                  .label(label)
                                  .model(aModel)
                                  .selectable(true)
                                  .bodyPainter(painter)
                                  .build();
    }
    return null;
  }
}
