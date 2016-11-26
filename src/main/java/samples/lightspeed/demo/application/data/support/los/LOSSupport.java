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
package samples.lightspeed.demo.application.data.support.los;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;

import com.luciad.earth.tileset.ILcdEarthTileSet;
import com.luciad.earth.tileset.terrain.TLcdEarthTileSetElevationProvider;
import com.luciad.gui.ALcdAction;
import com.luciad.gui.ILcdIcon;
import com.luciad.gui.TLcdImageIcon;
import com.luciad.gui.swing.TLcdGreyIcon;
import com.luciad.gui.swing.TLcdSWAction;
import com.luciad.gui.swing.TLcdSWIcon;
import samples.common.SwingUtil;
import com.luciad.model.TLcdModelDescriptor;
import com.luciad.model.TLcdVectorModel;
import com.luciad.reference.ILcdGeoReference;
import com.luciad.reference.TLcdGeodeticReference;
import com.luciad.shape.ILcdPoint;
import com.luciad.tea.ILcdAltitudeProvider;
import com.luciad.tea.TLcdCoverageAltitudeMode;
import com.luciad.tea.lightspeed.los.TLspLOSCalculator;
import com.luciad.tea.lightspeed.los.view.TLspLOSCoveragePainter;
import com.luciad.tea.lightspeed.los.view.TLspLOSCoverageStyle;
import com.luciad.util.TLcdColorMap;
import com.luciad.util.TLcdInterval;
import com.luciad.view.lightspeed.ILspAWTView;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.TLspContext;
import com.luciad.view.lightspeed.controller.ILspController;
import com.luciad.view.TLcdAWTEventFilterBuilder;
import com.luciad.view.lightspeed.controller.manipulation.TLspCreateController;
import com.luciad.view.lightspeed.editor.TLspShapeEditor;
import com.luciad.view.lightspeed.layer.ILspInteractivePaintableLayer;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.TLspLayer;
import com.luciad.view.lightspeed.layer.TLspPaintRepresentation;
import com.luciad.view.lightspeed.layer.TLspPaintRepresentationState;
import com.luciad.view.lightspeed.layer.TLspPaintState;
import com.luciad.view.lightspeed.layer.paintgroup.TLspPaintGroup;
import com.luciad.view.lightspeed.painter.shape.TLspShapePainter;
import com.luciad.view.lightspeed.query.ALspPaintQuery;
import com.luciad.view.lightspeed.query.TLspPaintedObjectsTouchQuery;
import com.luciad.view.lightspeed.services.terrain.ILspTerrainSupport;
import com.luciad.view.lightspeed.style.ILspWorldElevationStyle;
import com.luciad.view.lightspeed.style.TLspFillStyle;
import com.luciad.view.lightspeed.style.TLspLineStyle;
import com.luciad.view.lightspeed.style.styler.ALspStyleCollector;
import com.luciad.view.lightspeed.style.styler.ALspStyleTargetProvider;
import com.luciad.view.lightspeed.style.styler.ALspStyler;

import samples.lightspeed.common.controller.LonLatCreateControllerModel;
import samples.lightspeed.demo.application.controller.TouchCreateController;
import samples.lightspeed.demo.framework.application.Framework;
import samples.lightspeed.demo.framework.application.FrameworkContext;
import samples.tea.lightspeed.los.EarthTerrainElevationAdapter;

/**
 * Provides support for painting and editing line-of-sight layers in a theme.
 */
public class LOSSupport {

  private static final TLspPaintRepresentation LOS_EDITING_PREP = TLspPaintRepresentation
      .getInstance("Control point", 150);

  private static final Color LOS_OUTLINE = new Color(0.8f, 0.8f, 0.8f, 0.8f);
  private static final Color VISIBLE = new Color(1f, 0f, 0f, 0.5f);
  private static final Color INVISIBLE = new Color(0f, 0f, 0f, 0.2f);

  private Map<ILspView, ILspLayer> fView2LOSLayer = new IdentityHashMap<ILspView, ILspLayer>();
  private TLspLOSCalculator fLOSCalculator;
  private ILcdAltitudeProvider fAltitudeProvider;
  private final TLcdVectorModel fLosModel;

  public LOSSupport() {
    try {
      fLOSCalculator = new TLspLOSCalculator();
      fLOSCalculator.setCoverageAltitudeMode(TLcdCoverageAltitudeMode.ABOVE_GROUND_LEVEL);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    fLosModel = new TLcdVectorModel(new TLcdGeodeticReference(), new TLcdModelDescriptor("LOS", "LOS", "LOS"));
  }

  public ILspLayer createLayer(ILspView aView, String aLabel) {
    TLspLayer losLayer = new TLspLayer(fLosModel, aLabel);
    fView2LOSLayer.put(aView, losLayer);
    TLspLOSCoveragePainter coveragePainter = new TLspLOSCoveragePainter() {
      @Override
      public <T> boolean query(List<TLspPaintGroup> aPaintGroups, ALspPaintQuery<T> aQuery,
                               TLspPaintRepresentationState aPaintRepresentationState, TLspContext aContext) {
        if (aQuery instanceof TLspPaintedObjectsTouchQuery) {
          return false;
        }
        return super.query(aPaintGroups, aQuery, aPaintRepresentationState, aContext);
      }
    };
    coveragePainter.setStyler(
        TLspPaintState.REGULAR,
        new ALspStyler() {
          @Override
          public void style(Collection<?> aObjects, ALspStyleCollector aStyleCollector, TLspContext aContext) {
            aStyleCollector
                .objects(aObjects)
                .geometry(new ALspStyleTargetProvider() {
                  @Override
                  public synchronized void getStyleTargetsSFCT(Object aObject, TLspContext aContext,
                                                               List<Object> aResultSFCT) {
                    LOSCoverage coverage = (LOSCoverage) aObject;
                    coverage.computeCoverage(fLOSCalculator, getAltitudeProvider(aContext.getView()));
                    aResultSFCT.add(coverage.getMatrix());
                  }
                })
                .style(
                    TLspLOSCoverageStyle.newBuilder()
                                        .elevationMode(ILspWorldElevationStyle.ElevationMode.ON_TERRAIN)
                                        .colorMap(new TLcdColorMap(
                                            new TLcdInterval(-100, 1000),
                                            new double[]{2, 20},
                                            new Color[]{VISIBLE, INVISIBLE}
                                        ))
                                        .build()
                )
                .submit();
          }
        }
    );
    losLayer.setPainter(TLspPaintRepresentation.BODY, coveragePainter);
    losLayer.addPaintRepresentation(LOS_EDITING_PREP);
    TLspShapePainter painter = new TLspShapePainter();
    painter.setStyler(TLspPaintState.REGULAR, new ALspStyler() {
      @Override
      public void style(Collection<?> aObjects, ALspStyleCollector aStyleCollector, TLspContext aContext) {
        aStyleCollector
            .objects(aObjects)
            .styles(
                TLspLineStyle.newBuilder().color(LOS_OUTLINE).build(),
                TLspFillStyle.newBuilder().opacity(0f).build()
            )
            .submit();
      }
    });
    painter.setStyler(TLspPaintState.SELECTED, painter.getStyler(TLspPaintState.REGULAR));
    painter.setStyler(TLspPaintState.EDITED, painter.getStyler(TLspPaintState.REGULAR));
    losLayer.setPainter(LOS_EDITING_PREP, painter);
    losLayer.setEditor(LOS_EDITING_PREP, new TLspShapeEditor());
    losLayer.setEditable(LOS_EDITING_PREP, true);
    losLayer.setVisible(LOS_EDITING_PREP, true);
    losLayer.setEditable(true);
    losLayer.setScaleRange(TLspPaintRepresentation.BODY, new TLcdInterval(0.005, Double.MAX_VALUE));
    losLayer.setScaleRange(LOS_EDITING_PREP, new TLcdInterval(0.005, Double.MAX_VALUE));

    return losLayer;
  }

  protected ILcdAltitudeProvider getAltitudeProvider(ILspView aView) {
    if (fAltitudeProvider == null) {
      fAltitudeProvider = createAltitudeProvider(aView);
    }
    return fAltitudeProvider;
  }

  private ILcdAltitudeProvider createAltitudeProvider(ILspView aView) {
    ILspTerrainSupport terrainSupport = aView.getServices().getTerrainSupport();
    ILcdEarthTileSet elevationTileSet = terrainSupport.getElevationTileSet();
    final TLcdEarthTileSetElevationProvider elevationProvider = new TLcdEarthTileSetElevationProvider(elevationTileSet);
    elevationProvider.setForceAsynchronousTileRequests(false);
    int tileLevel = 10;
    elevationProvider.setMaxSynchronousLevel(tileLevel);
    elevationProvider.setMaxTileLevel(tileLevel);
    terrainSupport.addPropertyChangeListener(new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        elevationProvider.clearCache();
      }
    });
    return new EarthTerrainElevationAdapter(elevationProvider) {
      @Override
      public double retrieveAltitudeAt(ILcdPoint aPoint, ILcdGeoReference aPointReference) {
        double v = super.retrieveAltitudeAt(aPoint, aPointReference);
        return Double.isNaN(v) ? 0.0 : v;
      }

      @Override
      public double retrieveElevationAt(ILcdPoint aPoint, ILcdGeoReference aPointReference) {
        double v = super.retrieveElevationAt(aPoint, aPointReference);
        return Double.isNaN(v) ? 0.0 : v;
      }
    };
  }

  public JButton createLOSCreationButton() {
    return createLOSCreationButton(false);
  }

  public JButton createLOSCreationButton(boolean aTouch) {
    return createLOSCreationButton(
        fView2LOSLayer,
        aTouch ? new TLcdImageIcon("samples/lightspeed/demo/icons/los_64.png")
               : new TLcdImageIcon("samples/lightspeed/demo/icons/los_32.png"),
        "Create a LoS coverage"
    );
  }

  private JButton createLOSCreationButton(final Map<ILspView, ILspLayer> aView2Layer, final ILcdIcon aIcon,
                                          String aToolTipText) {
    final JButton button = new JButton();

    SwingUtil.makeSquare(button);
    final ALcdAction createAction = new CreateLOSAction(
        aView2Layer, aIcon, aToolTipText
    );
    TLcdSWAction swing_action = new TLcdSWAction(createAction);

    button.addActionListener(swing_action);
    button.setIcon(new TLcdSWIcon(aIcon));
    button.setDisabledIcon(new TLcdSWIcon(new TLcdGreyIcon(aIcon)));
    button.setToolTipText(aToolTipText);
    button.setEnabled(swing_action.isEnabled());
    //button.addMouseListener(this);

    createAction.addPropertyChangeListener(new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getSource() == createAction) {
          button.setEnabled(createAction.isEnabled());
        }
      }
    });

    return button;
  }

  private class CreateLOSAction extends ALcdAction {

    private final Map<ILspView, ILspLayer> fView2Layer;
    private final ILcdIcon fIcon;

    public CreateLOSAction(
        Map<ILspView, ILspLayer> aView2Layer,
        ILcdIcon aIcon,
        String aToolTipText
    ) {
      fView2Layer = aView2Layer;
      fIcon = aIcon;
      setShortDescription(aToolTipText);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      for (ILspView view : fView2Layer.keySet()) {
        ILspLayer layer = fView2Layer.get(view);
        if (layer instanceof ILspInteractivePaintableLayer) {
          ILspController controller = createLOSCreationController(
              (ILspInteractivePaintableLayer) layer,
              view,
              fIcon
          );
          view.setController(controller);
        }
      }
    }

    private ILspController createLOSCreationController(ILspInteractivePaintableLayer aLayer, final ILspView aView,
                                                       ILcdIcon aIcon) {
      // Each creation controller must have a controller model (in this case a lon-lat creation model)
      LonLatCreateControllerModel cm = new LonLatCreateControllerModel(LonLatCreateControllerModel.Type.CIRCLE,
                                                                       aLayer) {
        @Override
        public TLspPaintRepresentation getPaintRepresentation(ILspInteractivePaintableLayer aLayer, ILspView aView) {
          return LOS_EDITING_PREP;
        }

        @Override
        public Object create(ILspView aView, ILspLayer aLayer) {
          return new LOSCoverage((ILcdGeoReference) aLayer.getModel().getModelReference());
        }
      };
      cm.setCreateExtrudedShape(false);
      // Create and initialize creation controller
      TLspCreateController createController = null;
      Framework app = Framework.getInstance();
      boolean isTouchEnabled = Boolean.parseBoolean(app.getProperty("controllers.touch.enabled", "false"));

      final ILspController defaultController = aView.getController();
      if (isTouchEnabled) {
        FrameworkContext frameworkContext = app.getFrameworkContext();

        if (frameworkContext != null) {
          List<ILspView> views = frameworkContext.getViews();
          if (views != null && !views.isEmpty()) {
            ILspView view = views.get(0);
            if (view instanceof ILspAWTView) {
              createController = new TouchCreateController(new JButton(), new JButton(), cm);
            }
          }
        }

        if (createController == null) {
          throw new IllegalStateException("Could not initialize a touch create controller.");
        }
      } else {
        createController = new TLspCreateController(cm);
        createController.setAWTFilter(TLcdAWTEventFilterBuilder.newBuilder().
            leftMouseButton().or().
                                                                   rightMouseButton().or().
                                                                   keyEvents().build());
        createController.appendController(defaultController);
      }

//      TLspEditHandleStyler editHandleStyler = ( TLspEditHandleStyler ) createController.getHandleStyler();
//      editHandleStyler.setStyles( TLspHandleGeometryType.OUTLINE, ( List<ALspStyle> ) null ); // Removes existing styles
//      editHandleStyler.setStyles( TLspHandleGeometryType.OUTLINE, TLspLineStyle.newBuilder().color( new Color( 0f, 0f, 0f, 0f ) ).build() );

      createController.setShortDescription("Create LOS coverage");
      createController.setIcon(aIcon);
      createController.setActionToTriggerAfterCommit(
          new ALcdAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
              aView.setController(defaultController);
            }
          }
      );

      return createController;
    }
  }
}
