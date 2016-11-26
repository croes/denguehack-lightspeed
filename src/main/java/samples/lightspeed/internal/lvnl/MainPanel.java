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
package samples.lightspeed.internal.lvnl;

import static com.luciad.gui.TLcdIconFactory.GLOBE_ICON;
import static com.luciad.gui.TLcdIconFactory.create;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.luciad.format.object3d.ILcd3DMesh;
import com.luciad.format.object3d.obj.TLcdOBJMeshDecoder;
import com.luciad.gui.ALcdAction;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelListener;
import com.luciad.model.TLcdModelChangedEvent;
import com.luciad.model.TLcdModelDescriptor;
import com.luciad.model.TLcdVectorModel;
import com.luciad.reference.TLcdGeodeticReference;
import com.luciad.view.lightspeed.ILspAWTView;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.TLspOffscreenView;
import com.luciad.view.lightspeed.TLspViewBuilder;
import com.luciad.view.lightspeed.camera.ALspViewXYZWorldTransformation;
import com.luciad.view.lightspeed.controller.ILspController;
import com.luciad.view.lightspeed.editor.TLspShapeEditor;
import com.luciad.view.lightspeed.layer.ILspInteractivePaintableLayer;
import com.luciad.view.lightspeed.layer.TLspLayer;
import com.luciad.view.lightspeed.layer.TLspPaintRepresentation;
import com.luciad.view.lightspeed.layer.TLspPaintState;
import com.luciad.view.lightspeed.painter.mesh.ILsp3DIcon;
import com.luciad.view.lightspeed.painter.mesh.TLspMesh3DIcon;
import com.luciad.view.lightspeed.painter.shape.TLspShapePainter;
import com.luciad.view.lightspeed.style.TLsp3DIconStyle;
import com.luciad.view.lightspeed.style.TLspVerticalLineStyle;
import com.luciad.view.lightspeed.style.styler.TLspStyler;
import com.luciad.view.lightspeed.util.TLspViewTransformationUtil;

import samples.common.formatsupport.OpenAction;
import samples.common.formatsupport.OpenSupport;
import samples.common.serviceregistry.ServiceRegistry;
import samples.lightspeed.common.CreateAndEditToolBar;
import samples.lightspeed.common.FitUtil;
import samples.lightspeed.common.LightspeedSample;
import samples.lightspeed.common.LspOpenSupport;
import samples.lightspeed.common.ToolBar;
import samples.lightspeed.icons3d.OrientedPoint;
import samples.lightspeed.internal.lvnl.airspaces.AirspaceLayerFactory;
import samples.lightspeed.internal.lvnl.airspaces.AirspaceModelDecoder;
import samples.lightspeed.internal.lvnl.buildings.model.LvnlObjectsModelDecoder;
import samples.lightspeed.internal.lvnl.buildings.view.LvnlBuildingsLayerFactory;
import samples.lightspeed.internal.lvnl.procedures.ProcedureLayerFactory;

/**
 * The decoder sample illustrates the use of <code>ILcdModelDecoder</code> and
 * <code>ILspLayerFactory</code> to add models to an <code>ILspView</code>.
 * <p/>
 * The subpackages of this sample contain layer factory implementations specific to various
 * file formats. The sample itself uses <code>TLspCompositeLayerFactory</code> to aggregate these
 * implementations, and <code>TLcdOpenAction</code> to aggregate the corresponding model decoders.
 * The open action can be invoked via a toolbar button or by dragging and dropping files onto the
 * {@code ILspView}.
 */
public class MainPanel extends LightspeedSample {

  private OpenSupport fOpenSupport;

  // Custom toolbar which adds editing and creation controllers to the default Lightspeed sample toolbar
  private CreateAndEditToolBar fCreateAndEditToolBar;

  @Override
  protected ILspAWTView createView() {
    return super.createView(ILspView.ViewType.VIEW_3D);
  }

  @Override
  protected ToolBar[] createToolBars(final ILspAWTView aView) {
    final ToolBar regularToolBar = new ToolBar(aView, this, true, true);

    regularToolBar.addAction(new ALcdAction("Screenshot", create(GLOBE_ICON)) {
      @Override
      public void actionPerformed(ActionEvent e) {
        System.out.println("MainPanel.actionPerformed");

        TLspOffscreenView view = TLspViewBuilder.newBuilder()
                                                .size(1024, 1024)
                                                .executorThreadCount(0)
                                                .buildOffscreenView();
        TLspViewTransformationUtil.setup3DView(view, false);
        for (int i = 0; i < getView().layerCount(); i++) {
          view.addLayer(getView().getLayer(i));
        }
        ALspViewXYZWorldTransformation w2v = getView().getViewXYZWorldTransformation().clone();
        w2v.setSize(view.getWidth(), view.getHeight());
        view.setViewXYZWorldTransformation(w2v);
        view.display(true);
        BufferedImage screenshot = view.getImage();
        view.destroy();

        System.out.println("Saving...");
        try {
          String name = System.currentTimeMillis() + ".png";
          ImageIO.write(screenshot, "PNG", new File(name));
          System.out.println("Saved " + name);
        } catch (IOException e1) {
          e1.printStackTrace();
        }
      }
    }, 0);

    if (fCreateAndEditToolBar == null) {
      fCreateAndEditToolBar = new CreateAndEditToolBar(aView, this,
                                                       regularToolBar.getButtonGroup()
      ) {
        @Override
        protected ILspController createDefaultController() {
          return regularToolBar.getDefaultController();
        }
      };
    }
    return new ToolBar[]{regularToolBar, fCreateAndEditToolBar};
  }

  public ILspInteractivePaintableLayer getCreationLayer() {
    if (fCreateAndEditToolBar == null) {
      createToolBars(getView());
    }
    return fCreateAndEditToolBar.getCreationLayer();
  }

  @Override
  protected void createGUI() {
    super.createGUI();
    fOpenSupport = new LspOpenSupport(getView());
    fOpenSupport.addStatusListener(getStatusBar());

    getToolBars()[0].addAction(new OpenAction(fOpenSupport), ToolBar.FILE_GROUP);
  }

  @Override
  protected void addData() throws IOException {
    super.addData();

    ServiceRegistry.getInstance().register(new AirspaceLayerFactory());
    ServiceRegistry.getInstance().register(new ProcedureLayerFactory());
    ServiceRegistry.getInstance().register(new LvnlBuildingsLayerFactory());
    ServiceRegistry.getInstance().register(new LvnlObjectsModelDecoder());

    getView().addLayer(fCreateAndEditToolBar.getCreationLayer());

    TLspLayer layer = createAircraftLayer();
    getView().addLayer(layer);

    fOpenSupport.openSource(
        "Data/internal.data/lvnl/TrueMarble.250m.21600x21600.E1_processed_gamma.tif"
    );
    fOpenSupport.openSource(
        "Data/internal.data/lvnl/schiphol.tif"
    );
    fOpenSupport.openSource(
        "Data/internal.data/lvnl/objects/object_placement.properties"
    );

    try {
      ILcdModel ctr = new AirspaceModelDecoder().decode("Airspaces");
      getView().addModel(ctr);
    } catch (IOException e) {
      e.printStackTrace();
    }

    FitUtil.fitOnLayers(this, layer);
  /*
    try {
      ILcdModel sids = new ProcedureModelDecoder().decode( "Procedures" );
      getView().addModel( sids );
    } catch ( IOException e ) {
      e.printStackTrace();
    }
*/
  }

  private TLspLayer createAircraftLayer() {
    TLcdVectorModel planes = new TLcdVectorModel(
        new TLcdGeodeticReference(),
        new TLcdModelDescriptor()
    );
    planes.addModelListener(new ILcdModelListener() {
      @Override
      public void modelChanged(TLcdModelChangedEvent aEvent) {
        OrientedPoint p = (OrientedPoint) aEvent.elements().nextElement();
        System.out.println(p);
      }
    });
    planes.addElement(
        new OrientedPoint(
            4.805942393492885,
            52.31970491214158,
            256.2465797640069,
            10.0,
            0.0,
            0.0
        ),
        ILcdModel.NO_EVENT
    );

    TLspLayer layer = new TLspLayer(planes, "Aircraft");

    TLspShapePainter painter = new TLspShapePainter();
    TLsp3DIconStyle iconStyle = TLsp3DIconStyle.newBuilder()
                                               .icon(loadIcon())
                                               .worldSize(100)
                                               .verticalOffsetFactor(1.0)
                                               .iconSizeMode(TLsp3DIconStyle.ScalingMode.WORLD_SCALING)
                                               .build();
    TLspVerticalLineStyle lineStyle = TLspVerticalLineStyle.newBuilder().
        width(6f).
                                                               color(new Color(0.33f, 1f, 0.33f, 0.8f)).
                                                               build();
    painter.setStyler(
        TLspPaintState.REGULAR,
        new TLspStyler(iconStyle, lineStyle)
    );
    painter.setStyler(
        TLspPaintState.SELECTED,
        new TLspStyler(iconStyle, lineStyle)
    );
    painter.setStyler(
        TLspPaintState.EDITED,
        new TLspStyler(iconStyle, lineStyle)
    );

    layer.setPainter(TLspPaintRepresentation.BODY, painter);
    layer.setEditable(TLspPaintRepresentation.BODY, true);
    layer.setEditor(TLspPaintRepresentation.BODY, new TLspShapeEditor());
    layer.setSelectable(true);
    layer.setEditable(true);
    return layer;
  }

  private ILsp3DIcon loadIcon() {
    try {
      TLcdOBJMeshDecoder decoder = new TLcdOBJMeshDecoder();
      ILcd3DMesh mesh = decoder.decodeMesh("Data/3d_icons/plane.obj");
      return new TLspMesh3DIcon(mesh);
    } catch (IOException e) {
      throw new RuntimeException("Could load icon.", e);
    }
  }


}
