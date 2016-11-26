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
package samples.lightspeed.mapoverview;

import java.awt.Color;
import java.awt.Point;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;

import javax.swing.JPanel;

import com.luciad.geodesy.ILcdEllipsoid;
import com.luciad.gui.TLcdAWTUtil;
import com.luciad.model.ILcdModel;
import com.luciad.model.TLcdModelDescriptor;
import com.luciad.model.TLcdVectorModel;
import com.luciad.reference.TLcdGeodeticReference;
import com.luciad.shape.ILcdComplexPolygon;
import com.luciad.shape.ILcdPoint;
import com.luciad.shape.shape3D.TLcdLonLatHeightPoint;
import com.luciad.shape.shape3D.TLcdXYZPoint;
import com.luciad.transformation.TLcdDefaultModelXYZWorldTransformation;
import com.luciad.util.TLcdOutOfBoundsException;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.TLspContext;
import com.luciad.view.lightspeed.TLspSwingView;
import com.luciad.view.lightspeed.TLspViewBuilder;
import com.luciad.view.lightspeed.camera.ALspCameraConstraint;
import com.luciad.view.lightspeed.camera.ALspViewXYZWorldTransformation;
import com.luciad.view.lightspeed.camera.TLspViewXYZWorldTransformation2D;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.TLspPaintState;
import com.luciad.view.lightspeed.layer.shape.TLspShapeLayerBuilder;
import com.luciad.view.lightspeed.style.TLspFillStyle;
import com.luciad.view.lightspeed.style.TLspLineStyle;
import com.luciad.view.lightspeed.style.styler.ALspStyleCollector;
import com.luciad.view.lightspeed.style.styler.ALspStyler;

import samples.common.SampleData;
import samples.lightspeed.common.LspDataUtil;

/**
 * Uses a TLspSwingView to create a map overview for another ILspView. The map
 * overview shows the contours of the world area which is visible in the main
 * view. The overview fits continuously to this contour, and also enforces a
 * minimum and maximum zoom level.
 */
public class MapOverview extends JPanel {

  /**
   * The minimum zoom level enforced by the map overview.
   */
  private static final double MINIMUM_SCALE = 3e-6;

  /**
   * The maximum zoom level enforced by the map overview.
   */
  private static final double MAXIMUM_SCALE = 1e-3;

  private TLspSwingView fOverview;

  /**
   * Creates a map overview for the given main view.
   * @param aMainView the view for which to create an overview
   */
  public MapOverview(final ILspView aMainView) {
    // Create the TLspSwingView in which we will draw the map overview.
    fOverview = TLspViewBuilder.newBuilder()
                               .viewType(ILspView.ViewType.VIEW_2D)
                               .size(200, 200)
                               .background(Color.lightGray)
                               .buildSwingView();

    // Add a camera constraint to enforce the minimum and maximum zoom level.
    ((TLspViewXYZWorldTransformation2D) fOverview.getViewXYZWorldTransformation()).addConstraint(
        new ALspCameraConstraint<TLspViewXYZWorldTransformation2D>() {
          @Override
          public void constrain(TLspViewXYZWorldTransformation2D aSource, TLspViewXYZWorldTransformation2D aTargetSFCT) {
            aTargetSFCT.lookAt(
                aTargetSFCT.getWorldOrigin(),
                aTargetSFCT.getViewOrigin(),
                Math.max(Math.min(aTargetSFCT.getScale(), MAXIMUM_SCALE), MINIMUM_SCALE),
                0
            );
          }
        }
    );

    // Disable interactive navigation in the overview
    fOverview.setController(null);

    // Add layers to the overview
    LspDataUtil.instance().model(SampleData.SAN_FRANCISCO).layer().addToView(fOverview);
    LspDataUtil.instance().grid().addToView(fOverview);
    addOverviewLayer(aMainView, fOverview);

    // Add the overview to the component hierarchy
    add(fOverview.getHostComponent());
  }

  public void tearDown() {
    if (fOverview != null) {
      fOverview.removeAllLayers();
      fOverview.destroy();
    }
  }

  /**
   * Creates and adds the overview layer. The visible geographic area in
   * {@code aMainView} is visualized in {@code aOverview}.
   *
   * @param aMainView main view
   * @param aOverview view to which the overview layer is added
   */
  private void addOverviewLayer(ILspView aMainView, ILspView aOverview) {
    TLcdVectorModel model = new TLcdVectorModel(
        new TLcdGeodeticReference(),
        new TLcdModelDescriptor()
    );

    // The styler of the overview will output the visible area of the main view.
    model.addElement(aMainView, ILcdModel.NO_EVENT);

    // Create a layer with a map overview styler.
    ILspLayer overviewLayer = TLspShapeLayerBuilder.newBuilder()
                                                   .model(model)
                                                   .bodyStyler(TLspPaintState.REGULAR, new MapOverviewStyler(aMainView, aOverview))
                                                   .build();
    aOverview.addLayer(overviewLayer);
  }

  /**
   * Styler that visualizes the visible area of a given main view.
   */
  private static class MapOverviewStyler extends ALspStyler implements PropertyChangeListener {
    private static final TLcdGeodeticReference WGS84_REF = new TLcdGeodeticReference();

    private ILspView fMainView;
    private ILspView fOverview;
    private ALspViewXYZWorldTransformation fMainV2W;
    private Object fV2WIdentifier;

    // Styles used to visualize the visible area.
    private final TLspLineStyle fLineStyle = TLspLineStyle.newBuilder().width(3f).color(Color.orange).build();
    private final TLspFillStyle fFillStyle = TLspFillStyle.newBuilder().color(Color.white).opacity(0.4f).build();

    private MapOverviewStyler(ILspView aMainView, ILspView aOverview) {
      fMainView = aMainView;
      fOverview = aOverview;
      fMainV2W = fMainView.getViewXYZWorldTransformation();
      fV2WIdentifier = null;
      // Register property change listeners so that changes to the main view can be tracked.
      fMainView.addPropertyChangeListener(this);
      fMainV2W.addPropertyChangeListener(this);
    }

    @Override
    public void style(Collection<?> aObjects, ALspStyleCollector aStyleCollector, TLspContext aContext) {
      for (Object o : aObjects) {
        // Verify that we're working with the view which was passed to the styler's constructor.
        if (o == fMainView) {
          // Obtain the visible area in the main view.
          ILcdComplexPolygon area = fMainV2W.getGeodeticVisibleArea();
          // Draw it in the map overview.
          if (area != null) {
            aStyleCollector
                .object(o)
                .geometry(area)
                .styles(fFillStyle, fLineStyle)
                .submit();
          }
        }
      }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
      // If the ALspViewXYZWorldTransformation of the main view is changed,
      // remove this styler as a listener from the old transformation and add
      // it to the new one.
      if (fMainV2W != fMainView.getViewXYZWorldTransformation()) {
        fMainV2W.removePropertyChangeListener(this);
        fMainV2W = fMainView.getViewXYZWorldTransformation();
        fMainV2W.addPropertyChangeListener(this);
      }

      // Only modify the visible area if the v2w identifier has actually changed
      Object v2wIdentifier = fMainV2W.getIdentifier();
      if (v2wIdentifier != fV2WIdentifier && (v2wIdentifier == null || !v2wIdentifier.equals(fV2WIdentifier))) {
        fV2WIdentifier = v2wIdentifier;

        // Fire a style change event to make sure the map overview gets updated.
        fireStyleChangeEvent();

        // Fit the map overview to the visible area.
        final ILcdComplexPolygon area = fMainV2W.getGeodeticVisibleArea();
        if (area != null) {
          TLcdAWTUtil.invokeAndWait(new Runnable() {
            @Override
            public void run() {
              try {
                fitOverviewToVisibleArea(area);
              } catch (TLcdOutOfBoundsException ignored) {
                // If fit failed for any reason, do nothing.
              }
            }
          });
        }
      }
    }

    /**
     * Fits the overview map to the visible area of the main map. This method centers the
     * overview on the same point as the main view, and then computes an appropriate scale
     * for the overview given the main view's visible area.
     * <p/>
     * It would be easier to use {@code TLspViewNavigationUtil} to simply fit the overview
     * to the bounds of the visible area. Doing so, however, would make the overview appear
     * more "jittery", especially in 3D where rotation of the camera can make the visible
     * area change quite rapidly. The approach used here results in a more visually stable
     * map overview.
     *
     * @param aArea the area to fit to
     * @throws TLcdOutOfBoundsException if the fit could not be performed, typically because
     *                                  the Earth is panned entirely outside the main view
     */
    private void fitOverviewToVisibleArea(ILcdComplexPolygon aArea) throws TLcdOutOfBoundsException {
      TLcdDefaultModelXYZWorldTransformation m2w = new TLcdDefaultModelXYZWorldTransformation();
      m2w.setModelReference(WGS84_REF);

      // Compute the lon/lat coordinates corresponding to the center of the main view
      Point centerView = new Point(fMainV2W.getWidth() / 2, fMainV2W.getHeight() / 2);
      TLcdXYZPoint centerWorld = new TLcdXYZPoint();
      TLcdLonLatHeightPoint centerLLH = new TLcdLonLatHeightPoint();

      fMainV2W.viewAWTPoint2worldSFCT(
          centerView,
          ALspViewXYZWorldTransformation.LocationMode.ELLIPSOID,
          centerWorld
      );

      m2w.setXYZWorldReference(fMainView.getXYZWorldReference());
      m2w.worldPoint2modelSFCT(centerWorld, centerLLH);

      // Transform those coordinates back to the world reference of the map overview
      m2w.setXYZWorldReference(fOverview.getXYZWorldReference());
      m2w.modelPoint2worldSFCT(centerLLH, centerWorld);

      // Compute an appropriate scale for the map overview. We base this on the
      // points of the visible area rather than on its bounds, because the
      // latter are not rotationally invariant. The scale we compute here is
      // based on the radius of a circle which encloses the visible area.
      ILcdEllipsoid ellipsoid = WGS84_REF.getGeodeticDatum().getEllipsoid();
      double radius = 0;
      for (int i = 0; i < aArea.getPointCount(); i++) {
        ILcdPoint p = aArea.getPoint(i);
        double distance = ellipsoid.geodesicDistance(p, centerLLH);
        radius = Math.max(radius, distance);
      }

      // The diameter of the circle is 2 * radius, obviously, but we add
      // another factor 2 to add an extra margin around the map overview. This
      // way the overview provides more context and is thus easier to read.
      TLspViewXYZWorldTransformation2D overviewW2V = (TLspViewXYZWorldTransformation2D) fOverview.getViewXYZWorldTransformation();
      double scale = Math.max(overviewW2V.getWidth(), overviewW2V.getHeight()) / (4.0 * radius);

      // Center the overview
      centerView.setLocation(overviewW2V.getWidth() / 2, overviewW2V.getHeight() / 2);
      overviewW2V.lookAt(centerWorld, centerView, scale, 0.0);
    }
  }
}
