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
package samples.lightspeed.paintinview;

import static com.luciad.view.lightspeed.layer.TLspPaintRepresentation.BODY;
import static com.luciad.view.lightspeed.layer.TLspPaintRepresentationState.REGULAR_BODY;
import static com.luciad.view.lightspeed.style.ILspWorldElevationStyle.ElevationMode.ON_TERRAIN;

import java.awt.Color;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import com.luciad.geodesy.TLcdEllipsoid;
import com.luciad.gui.TLcdIconFactory;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelReference;
import com.luciad.model.TLcdModelDescriptor;
import com.luciad.model.TLcdVectorModel;
import com.luciad.reference.ILcdGeoReference;
import com.luciad.reference.TLcdGeodeticReference;
import com.luciad.shape.ILcdPoint;
import com.luciad.shape.shape2D.TLcdLonLatEllipse;
import com.luciad.shape.shape2D.TLcdXYEllipse;
import com.luciad.shape.shape2D.TLcdXYPoint;
import com.luciad.shape.shape2D.TLcdXYPolygon;
import com.luciad.view.lightspeed.TLspContext;
import com.luciad.view.lightspeed.editor.TLspShapeEditor;
import com.luciad.view.lightspeed.layer.ILspInteractivePaintableLayer;
import com.luciad.view.lightspeed.layer.TLspLayer;
import com.luciad.view.lightspeed.layer.TLspPaintRepresentation;
import com.luciad.view.lightspeed.layer.TLspPaintRepresentationState;
import com.luciad.view.lightspeed.layer.TLspPaintState;
import com.luciad.view.lightspeed.layer.shape.TLspShapeLayerBuilder;
import com.luciad.view.lightspeed.painter.shape.TLspShapePainter;
import com.luciad.view.lightspeed.style.TLspFillStyle;
import com.luciad.view.lightspeed.style.TLspIconStyle;
import com.luciad.view.lightspeed.style.TLspLineStyle;
import com.luciad.view.lightspeed.style.TLspPinLineStyle;
import com.luciad.view.lightspeed.style.TLspTextStyle;
import com.luciad.view.lightspeed.style.styler.ALspStyleCollector;
import com.luciad.view.lightspeed.style.styler.ALspStyleTargetProvider;
import com.luciad.view.lightspeed.style.styler.ALspStyler;
import com.luciad.view.lightspeed.style.styler.ILspStyler;

import samples.lightspeed.common.LightspeedSample;
import samples.lightspeed.labels.util.FixedTextProviderStyle;

/**
 * This sample demonstrates painting and editing objects in screen coordinates:
 * <ul>
 *   <li>A layer with data defined in screen coordinates.</li>
 *   <li>A layer with data defined on the earth, but styled to draw an icon and mini-data in screen coordinates.</li>
 * </ul>
 * Both layers are editable.
 */
public class MainPanel extends LightspeedSample {

  @Override
  protected void addData() throws IOException {
    super.addData();

    // Add a layer with data defined in screen coordinates
    getView().addLayer(createScreenCoordinatesLayer());

    // Add a layer with data defined on the earth, but with aspects drawn in view coordinates
    getView().addLayer(createGeoreferencedLayer());
  }

  /**
   * Creates a geodetic model with three lon-lat ellipses.
   *
   * Also creates an {@link ILspStyler} that draws the original shapes in blue, but also an
   * icon defined in screen coordinates.
   *
   * @return An editable layer based on this model.
   */
  private ILspInteractivePaintableLayer createGeoreferencedLayer() {
    // The data is defined in lon-lat
    TLcdGeodeticReference modelReference = new TLcdGeodeticReference();

    TLcdVectorModel model = new TLcdVectorModel(modelReference);
    model.setModelDescriptor(new TLcdModelDescriptor("geo", "geo", "Georeferenced data"));

    model.addElement(new MyEllipse(-100, -30, 1000000, 4000000, -20, 400, 30), ILcdModel.NO_EVENT);
    model.addElement(new MyEllipse(0, 0, 3000000, 3000000, 0, 450, 30), ILcdModel.NO_EVENT);
    model.addElement(new MyEllipse(100, 30, 2000000, 4000000, 20, 500, 30), ILcdModel.NO_EVENT);

    ALspStyler bodyStyler = new ALspStyler() {
      @Override
      public void style(Collection<?> aObjects, ALspStyleCollector aStyleCollector, TLspContext aContext) {
        aStyleCollector.objects(aObjects)
                       .styles(TLspFillStyle.newBuilder().color(new Color(0.5f, 0.5f, 1.0f, 0.5f)).elevationMode(ON_TERRAIN).build(),
                               TLspLineStyle.newBuilder().color(Color.blue).elevationMode(ON_TERRAIN).build())
                       .submit();
      }
    };

    ALspStyler screenStyler = new ALspStyler() {
      private ALspStyleTargetProvider fIconTargetProvider = new ALspStyleTargetProvider() {
        @Override
        public void getStyleTargetsSFCT(Object aObject, TLspContext aContext, List<Object> aResultSFCT) {
          aResultSFCT.add(((MyEllipse) aObject).getScreenLocation());
        }

        @Override
        public ILcdModelReference getStyleTargetReference(TLspContext aContext) {
          // The geometry is specified in screen coordinates
          return null;
        }
      };

      private ALspStyleTargetProvider fMiniTargetProvider = new ALspStyleTargetProvider() {
        @Override
        public void getStyleTargetsSFCT(Object aObject, TLspContext aContext, List<Object> aResultSFCT) {
          aResultSFCT.add(((MyEllipse) aObject).getMiniEllipse());
        }

        @Override
        public ILcdModelReference getStyleTargetReference(TLspContext aContext) {
          // The geometry is specified in screen coordinates
          return null;
        }
      };

      @Override
      public void style(Collection<?> aObjects, ALspStyleCollector aStyleCollector, TLspContext aContext) {
        aStyleCollector.objects(aObjects)
                       .geometry(fIconTargetProvider)
                       .styles(TLspIconStyle.newBuilder().icon(TLcdIconFactory.create(TLcdIconFactory.EDIT_ICON, TLcdIconFactory.getDefaultTheme(), TLcdIconFactory.Size.SIZE_32)).build())
                       .submit();
        aStyleCollector.objects(aObjects)
                       .geometry(fMiniTargetProvider)
                       .styles(TLspFillStyle.newBuilder().color(new Color(0.5f, 0.5f, 1.0f, 0.5f)).elevationMode(ON_TERRAIN).build(),
                               TLspLineStyle.newBuilder().color(Color.blue).elevationMode(ON_TERRAIN).build())
                       .submit();
      }
    };

    TLspLayer layer = new TLspLayer(model);
    layer.setCulling(false);
    layer.setSelectable(true);
    layer.setEditable(true);
    layer.setEditable(BODY, true);
    layer.setEditor(BODY, new TLspShapeEditor());
    layer.setPainter(BODY, new TLspShapePainter());
    layer.setStyler(REGULAR_BODY, bodyStyler);
    TLspPaintRepresentation SCREEN = TLspPaintRepresentation.getInstance("SCREEN", 150);
    layer.addPaintRepresentation(SCREEN);
    layer.setPainter(SCREEN, new TLspShapePainter());
    layer.setStyler(TLspPaintRepresentationState.getInstance(SCREEN, TLspPaintState.REGULAR), screenStyler);
    layer.setVisible(SCREEN, true);
    return layer;
  }

  private static class MyEllipse extends TLcdLonLatEllipse {

    private final ILcdPoint fScreenLocation;

    public MyEllipse(double aLon, double aLat, double aA, double aB, double aR, int aScreenX, int aScreenY) {
      super(aLon, aLat, aA, aB, aR, new TLcdEllipsoid());
      fScreenLocation = new TLcdXYPoint(aScreenX, aScreenY);
    }

    public ILcdPoint getScreenLocation() {
      return fScreenLocation;
    }

    public TLcdXYEllipse getMiniEllipse() {
      double factor = Math.max(getA(), getB()) / 20.0;
      return new TLcdXYEllipse(fScreenLocation.getX(), fScreenLocation.getY() + 40,
                               getA() / factor, getB() / factor, -getRotAngle());
    }
  }

  /**
   * Creates a model with three shapes (a point, polygon and ellipse).
   * By using a non-{@link ILcdGeoReference} model reference, we indicate the data
   * is specified in screen coordinates.
   *
   * @return An editable layer based on this model, with the default styling.
   */
  private ILspInteractivePaintableLayer createScreenCoordinatesLayer() {
    // Use a null model reference to indicates the data is specified in screen coordinates.
    ILcdModelReference modelReference = null;

    TLcdVectorModel model = new TLcdVectorModel(modelReference);
    model.setModelDescriptor(new TLcdModelDescriptor("screen", "screen", "Screen data"));

    TLcdXYPoint point = new TLcdXYPoint(60, 60);
    model.addElement(point, ILcdModel.NO_EVENT);

    TLcdXYPolygon line = new TLcdXYPolygon();
    line.insert2DPoint(0, 100, 30);
    line.insert2DPoint(1, 180, 30);
    line.insert2DPoint(2, 110, 130);
    model.addElement(line, ILcdModel.NO_EVENT);

    TLcdXYEllipse ellipse = new TLcdXYEllipse(250, 70, 30, 50, 15);
    model.addElement(ellipse, ILcdModel.NO_EVENT);

    return TLspShapeLayerBuilder.newBuilder()
                                .model(model)
                                .selectable(true)
                                .bodyEditable(true)
                                .labelStyles(TLspPaintState.REGULAR, TLspTextStyle.newBuilder().build(),
                                             FixedTextProviderStyle.newBuilder().text("Label").build(),
                                             TLspPinLineStyle.newBuilder().build())
                                .labelEditable(true)
                                .build();
  }

  public static void main(final String[] aArgs) {
    startSample(MainPanel.class, "Painting In View Coordinates");
  }

}
