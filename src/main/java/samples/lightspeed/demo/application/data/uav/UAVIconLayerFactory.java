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
package samples.lightspeed.demo.application.data.uav;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import com.luciad.model.ILcdModel;
import com.luciad.view.lightspeed.TLspContext;
import com.luciad.view.lightspeed.TLspPaintPass;
import com.luciad.view.lightspeed.TLspPaintProgress;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.TLspPaintState;
import com.luciad.view.lightspeed.layer.paintgroup.TLspPaintGroup;
import com.luciad.view.lightspeed.layer.shape.TLspShapeLayerBuilder;
import com.luciad.view.lightspeed.painter.shape.TLspShapePainter;
import com.luciad.view.lightspeed.style.styler.ILspStyler;
import com.luciad.view.lightspeed.util.opengl.glstate.ILspGLState;
import com.luciad.view.opengl.binding.ILcdGL;
import com.luciad.view.opengl.binding.ILcdGLDrawable;

import samples.lightspeed.demo.framework.data.AbstractLayerFactory;

/**
 * Layer factory for layers with UAV objects.
 * <p>
 * This layer factory sets up a layer with a painter that visualizes the UAV objects as a 3D icon.
 * The user can specify the icon itself by setting the property "uav.icon.src". This property
 * specifies the filename of the 3D mesh that is used for the 3D icon.
 */
public class UAVIconLayerFactory extends AbstractLayerFactory {

  private float fIconScale;
  private String fIconSrc;

  public UAVIconLayerFactory() {
    fIconScale = 10.0f;
    fIconSrc = "";
  }

  @Override
  public Collection<ILspLayer> createLayers(ILcdModel aModel) {
    return Collections.singletonList(createIconLayer(aModel));
  }

  public ILspLayer createIconLayer(ILcdModel aModel) {
    return TLspShapeLayerBuilder
        .newBuilder()
        .model(aModel)
        .bodyStyler(TLspPaintState.REGULAR, new UAVStyler(fIconSrc, fIconScale))
        .selectable(false)
        .culling(false)
        .label("UAV 3D Icon")
        .build();
  }

  @Override
  public void configure(Properties aProperties) {
    super.configure(aProperties);
    fIconScale = Float.parseFloat(aProperties.getProperty("uav.icon.scale", "10000.0f"));
    fIconSrc = aProperties.getProperty("uav.icon.src");
  }

  @Override
  public boolean canCreateLayers(ILcdModel aModel) {
    return aModel.getModelDescriptor().getTypeName() != null && aModel.getModelDescriptor()
                                                                      .getTypeName().equals("UAVIcon");
  }
}
