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
package samples.lightspeed.internal.lvnl.buildings.view;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Properties;

import com.luciad.format.object3d.obj.TLcdOBJMeshDecoder;
import com.luciad.format.object3d.openflight.TLcdOpenFlightFileDecoder;
import com.luciad.format.object3d.openflight.lightspeed.TLspOpenFlight3DIcon;
import com.luciad.format.object3d.openflight.model.TLcdOpenFlightHeaderNode;
import com.luciad.io.ILcdInputStreamFactory;
import com.luciad.io.TLcdInputStreamFactory;
import com.luciad.model.ILcdModel;
import com.luciad.view.lightspeed.TLspContext;
import com.luciad.view.lightspeed.TLspPaintPass;
import com.luciad.view.lightspeed.layer.ALspSingleLayerFactory;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.TLspLayer;
import com.luciad.view.lightspeed.layer.TLspPaintRepresentation;
import com.luciad.view.lightspeed.layer.TLspPaintState;
import com.luciad.view.lightspeed.painter.mesh.ILsp3DIcon;
import com.luciad.view.lightspeed.painter.mesh.TLspMesh3DIcon;
import com.luciad.view.lightspeed.painter.shape.TLspShapePainter;
import com.luciad.view.lightspeed.style.TLsp3DIconStyle;
import com.luciad.view.lightspeed.style.styler.ALspStyleCollector;
import com.luciad.view.lightspeed.style.styler.ALspStyler;
import com.luciad.view.lightspeed.util.opengl.glstate.ILspGLState;
import com.luciad.view.opengl.binding.ILcdGL;
import com.luciad.view.opengl.binding.ILcdGLDrawable;

import samples.lightspeed.internal.lvnl.buildings.model.LvnlObjectsModelDecoder;
import samples.lightspeed.internal.lvnl.buildings.model.LvnlPositionedObject;
import samples.lightspeed.internal.lvnl.buildings.model.ObjectRepository;

/**
 * @author tomn
 * @since 2012.0
 */
public class LvnlBuildingsLayerFactory extends ALspSingleLayerFactory {

  private ObjectRepository fObjectRepository;
  private ILcdInputStreamFactory fInputStreamFactory;

  public LvnlBuildingsLayerFactory() throws IOException {
    Properties props = new Properties();
    props.load(new FileInputStream(new File("Data/internal.data/lvnl/objects/object_repository.properties")));
    fObjectRepository = new ObjectRepository(
        props,
        new File("Data/internal.data/lvnl/objects/"),
        false
    );
    fInputStreamFactory = new TLcdInputStreamFactory();
  }

  @Override
  public ILspLayer createLayer(ILcdModel aModel) {
    BuildingStyler styler = new BuildingStyler(fObjectRepository, fInputStreamFactory);
    TLspShapePainter painter = new TLspShapePainter();
    painter.setStyler(TLspPaintState.REGULAR, styler);

    TLspLayer layer = new TLspLayer(aModel, "Buildings", ILspLayer.LayerType.BACKGROUND);
    layer.setPainter(TLspPaintRepresentation.BODY, painter);
    return layer;
  }

  @Override
  public boolean canCreateLayers(ILcdModel aModel) {
    return aModel.getModelDescriptor().getTypeName().endsWith(LvnlObjectsModelDecoder.OBJECTS_TYPE_NAME);
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////

  private static class BuildingStyler extends ALspStyler {

    private ObjectRepository fObjectRepository;
    private ILcdInputStreamFactory fInputStreamFactory;

    private BuildingStyler(ObjectRepository aObjectRepository,
                           ILcdInputStreamFactory aInputStreamFactory) {
      fObjectRepository = aObjectRepository;
      fInputStreamFactory = aInputStreamFactory;
    }

    @Override
    public void style(Collection<?> aObjects, ALspStyleCollector aStyleCollector, TLspContext aContext) {
      for (Object o : aObjects) {
        if (o instanceof LvnlPositionedObject) {
          LvnlPositionedObject lvnl = (LvnlPositionedObject) o;
          String iconSource = fObjectRepository.getIconSource(lvnl.getObjectName());
          if (iconSource != null) {
            TLsp3DIconStyle style = TLsp3DIconStyle.newBuilder().
                icon(loadIcon(iconSource)).
                                                       recenterIcon(false).
                                                       verticalOffsetFactor(0).
                                                       build();
            aStyleCollector.
                               object(lvnl).
                               geometry(lvnl).
                               style(style).
                               submit();
          }
        }
      }
    }

    private ILsp3DIcon loadIcon(String aIconSource) {
      try {
        String extension = aIconSource.substring(aIconSource.length() - 4);
        if (extension.equalsIgnoreCase(".obj")) {
          TLcdOBJMeshDecoder decoder = new TLcdOBJMeshDecoder();
          decoder.setInputStreamFactory(fInputStreamFactory);
          return new TLspMesh3DIcon(decoder.decodeMesh(aIconSource));
        } else if (extension.equalsIgnoreCase(".flt")) {
          TLcdOpenFlightFileDecoder decoder = new TLcdOpenFlightFileDecoder();
          decoder.setInputStreamFactory(fInputStreamFactory);
          TLcdOpenFlightHeaderNode scene = decoder.decode(aIconSource);
          return new TLspOpenFlight3DIcon(scene) {
            @Override
            public void paint(ILspGLState aGLState, ILcdGLDrawable aGLDrawable, TLspPaintPass aPass, TLspContext aContext) {
              ILcdGL gl = aGLDrawable.getGL();
              gl.glEnable(ILcdGL.GL_LIGHTING);
              gl.glEnable(ILcdGL.GL_LIGHT0);
              gl.glLightfv(ILcdGL.GL_LIGHT0, ILcdGL.GL_AMBIENT, new float[]{0.3f, 0.3f, 0.3f, 1f});

              super.paint(aGLState, aGLDrawable, aPass, aContext);

              gl.glDisable(ILcdGL.GL_LIGHTING);
              gl.glDisable(ILcdGL.GL_LIGHT0);
            }
          };
        } else {
          throw new IllegalArgumentException("Could not load icon, unsupported file format: " + extension);
        }
      } catch (IOException e) {
        throw new IllegalArgumentException("Could not load icon", e);
      }
    }

  }
}
