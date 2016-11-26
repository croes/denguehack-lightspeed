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
package samples.lightspeed.internal.azimuthalequidistant;

import static com.luciad.util.concurrent.TLcdLockUtil.*;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import javax.swing.*;

import com.luciad.geodesy.TLcdEllipsoid;
import com.luciad.geodesy.TLcdGeodeticDatum;
import com.luciad.gui.swing.TLcdOverlayLayout;
import com.luciad.model.ILcdModel;
import com.luciad.model.TLcdModelDescriptor;
import com.luciad.model.TLcdVectorModel;
import com.luciad.projection.TLcdAzimuthalEquidistant;
import com.luciad.reference.TLcdGeodeticReference;
import com.luciad.reference.TLcdGridReference;
import com.luciad.shape.ILcdPoint;
import com.luciad.shape.shape2D.TLcdXYLine;
import com.luciad.shape.shape2D.TLcdXYPoint;
import com.luciad.shape.shape3D.TLcdLonLatHeightPoint;
import com.luciad.shape.shape3D.TLcdXYZPoint;
import com.luciad.transformation.TLcdDefaultModelXYZWorldTransformation;
import com.luciad.util.TLcdDistanceUnit;
import com.luciad.util.TLcdOutOfBoundsException;
import com.luciad.view.lightspeed.ILspAWTView;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.TLspAWTView;
import com.luciad.view.lightspeed.TLspViewBuilder;
import com.luciad.view.lightspeed.camera.ALspViewXYZWorldTransformation;
import com.luciad.view.lightspeed.camera.TLspViewXYZWorldTransformation2D;
import com.luciad.view.lightspeed.camera.TLspViewXYZWorldTransformation3D;
import com.luciad.view.lightspeed.camera.tracking.TLspModelElementTrackingPointProvider;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.TLspPaintState;
import com.luciad.view.lightspeed.layer.shape.TLspShapeLayerBuilder;
import com.luciad.view.lightspeed.style.TLspLineStyle;
import com.luciad.view.lightspeed.style.TLspRasterStyle;
import com.luciad.view.lightspeed.swing.TLspScaleIndicator;

import samples.common.SampleData;
import samples.lightspeed.common.LspDataUtil;

/**
 * Compares a constrained 3D geocentric view with a 2D azimuthal equidistant projection.
 *
 * The sample allows to center the views on a moving ship position. The ship position
 * is updated using a Timer thread, by default every 1 second.
 *
 * The left view shows the 2D view, the right view shows the 3D view.
 *
 * Below the 2D view is the distance shown between the corresponding world positions
 * at the mouse location in both views. It gives an indication of the deviation from
 * the perfect 2D azimuthal equidistant projection (left view) when using the constrained
 * 3D view (right view).
 *
 * When the camera is constrained to be centered at the ship, the data in the 2D view
 * needs to be re-discretized on every ship movement. The data in the right view does not.
 *
 * You can run the sample with only the 3D view by changing the ADD_2D_VIEW field.
 *
 * @see TrackingCameraConstraint3D
 */
public class Compare3DGeocentricWithAzimuthalCylindrical implements Runnable {

  private static final boolean ADD_2D_VIEW = true;

  private static final int SIZE = 500;
  static final double NAVIGATION_LIMIT = TLcdDistanceUnit.NM_UNIT.convertToStandard(400);

  private NumberFormat fFormat = new DecimalFormat("#.000");
  private OrientedLonLatPoint fShip;
  private ILcdModel fShipModel;
  private TLspAWTView f2DView;
  private TLspAWTView f3DView;
  private Timer fTimer;
  private TrackingCameraConstraint3D fLookAtCameraConstraint3D;
  private TrackingCameraConstraint2D fConstraint2D;


  public void run() {
    createShipModel();

    //*** 3D VIEW *******************************************************************************

    f3DView = createView( ILspView.ViewType.VIEW_3D);
    TLspViewXYZWorldTransformation3D vw3D = new TLspViewXYZWorldTransformation3D(f3DView);
    fLookAtCameraConstraint3D = createLookAtCameraConstraint3D(f3DView, fShip, fShipModel);
    vw3D.addConstraint(fLookAtCameraConstraint3D);
    vw3D.setFieldOfView(60);
    f3DView.getServices().getTerrainSupport().setElevationEnabled(false);
    f3DView.setViewXYZWorldTransformation(vw3D);

    try {
      addData(f3DView);
    } catch (IOException e) {
      e.printStackTrace();
      System.exit(1);
    }

    //*** 2D VIEW *******************************************************************************
    if (ADD_2D_VIEW) {
      f2DView = createView( ILspView.ViewType.VIEW_2D);
      f2DView.setXYZWorldReference(new TLcdGridReference(
          new TLcdGeodeticDatum(),
          new TLcdAzimuthalEquidistant( fShip.getLon(), fShip.getLat() )
      ));
      final TLspViewXYZWorldTransformation2D vw2D = new TLspViewXYZWorldTransformation2D( f2DView );
      fConstraint2D = new TrackingCameraConstraint2D( fShip, f3DView, f2DView );
      vw2D.addConstraint( fConstraint2D );
      f2DView.setViewXYZWorldTransformation(vw2D);
      vw3D.addPropertyChangeListener(new PropertyChangeListener() {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
          // Force constraint to be applied
          vw2D.lookAt(
              vw2D.getWorldOrigin(),
              vw2D.getViewOrigin(),
              vw2D.getScale(),
              vw2D.getRotation()
          );
        }
      });
      f2DView.setController(null);

      try {
        addData( f2DView );
      } catch (IOException e) {
        e.printStackTrace();
        System.exit(1);
      }
    }

    //*** TOOLBAR *******************************************************************************

    JToolBar bar = new JToolBar();

    final JButton pause = new JButton("Start");
    pause.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        if (fTimer.isRunning()) {
          fTimer.stop();
          pause.setText( "Start" );
        } else {
          fTimer.start();
          pause.setText( "Stop" );
        }
      }
    });
    bar.add(pause);
    bar.addSeparator();

    final JTextField interval = new JTextField("" + fTimer.getDelay());
    interval.setMaximumSize(new Dimension(150, 150));
    interval.addKeyListener(new KeyListener() {
      @Override
      public void keyTyped(KeyEvent e) {
      }

      @Override
      public void keyPressed(KeyEvent e) {
      }

      @Override
      public void keyReleased(KeyEvent e) {
        try {
          int msec = Integer.parseInt(interval.getText());
          fTimer.setDelay(msec);
        } catch (NumberFormatException ex) {
        }
      }
    });
    bar.add(new JLabel("Interval (msec): "));
    bar.add(interval);
    bar.addSeparator();

    final JCheckBox constrain = new JCheckBox("Follow ship", true);
    constrain.addItemListener(new ItemListener() {
      @Override
      public void itemStateChanged(ItemEvent e) {
        setCameraConstrained(constrain.isSelected());
        fireChangeEvents();
      }
    });
    bar.add(constrain);
    bar.addSeparator();

    final JCheckBox northUp = new JCheckBox("North up", false);
    northUp.addItemListener(new ItemListener() {
      @Override
      public void itemStateChanged(ItemEvent e) {
        setNorthUp( northUp.isSelected() );
        fireChangeEvents();
      }
    });
    bar.add(northUp);
    bar.addSeparator();

    //***

    final JLabel accuracyReadOut = new JLabel("###");
    accuracyReadOut.setHorizontalAlignment(SwingConstants.CENTER);
    accuracyReadOut.setFont(new Font("dialog", Font.BOLD, 16));

    MouseMotionAdapter accuracyMeter = new MouseMotionAdapter() {
      @Override
      public void mouseMoved(MouseEvent e) {
        double accuracy = computeAccuracy(e.getPoint());
        double accuracyM = accuracy;
        String unit;
        if (accuracy >= 1000) {
          accuracy /= 1000;
          unit = "km";
        } else if (accuracy >= 1) {
          unit = "m";
        } else if (accuracy >= 0.01) {
          accuracy *= 100;
          unit = "cm";
        } else {
          accuracy *= 1000;
          unit = "mm";
        }
        accuracyReadOut.setText(
            "ACCURACY: " +
            fFormat.format(accuracyM * f2DView.getViewXYZWorldTransformation().getScale()) + " pixels"
            + " (" + fFormat.format(accuracy) + " " + unit + ")"
        );
      }
    };

    final JLabel rangeReadOut = new JLabel("###");
    if ( f2DView != null) {
      f3DView.getHostComponent().addMouseMotionListener(accuracyMeter);
      f2DView.getHostComponent().addMouseMotionListener(accuracyMeter);

      //***

      rangeReadOut.setHorizontalAlignment(SwingConstants.CENTER);
      rangeReadOut.setFont(new Font("dialog", Font.BOLD, 16));

      f2DView.getHostComponent().addComponentListener(new ComponentAdapter() {
        @Override
        public void componentResized(ComponentEvent e) {
          updateRangeLabel(rangeReadOut);
        }
      });
      f3DView.getViewXYZWorldTransformation().addPropertyChangeListener(new PropertyChangeListener() {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
          updateRangeLabel(rangeReadOut);
        }
      });

    }

    //***

    JPanel views = new JPanel(new GridLayout(1, 2, 4, 4));
    if ( f2DView != null) {
      views.add( f2DView.getHostComponent());
    }
    views.add(f3DView.getHostComponent());

    JPanel readOuts = new JPanel(new GridLayout(1, 2, 4, 4));
    if ( f2DView != null) {
      readOuts.add(accuracyReadOut);
      readOuts.add(rangeReadOut);
    }

    JFrame frame = new JFrame("Compares 3D top-down Geocentric with Azimuthal Equidistant");
    frame.getContentPane().setLayout(new BorderLayout(4, 4));
    frame.getContentPane().add(views, BorderLayout.CENTER);
    frame.getContentPane().add(bar, BorderLayout.NORTH);
    if ( f2DView != null) {
      frame.getContentPane().add(readOuts, BorderLayout.SOUTH);
    }
    frame.pack();
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.setVisible(true);
  }

  private void updateRangeLabel(JLabel aLabel) {
    ALspViewXYZWorldTransformation v2d = f2DView.getViewXYZWorldTransformation();
    double rangeX = f2DView.getWidth() / v2d.getScale();
    double rangeY = f2DView.getHeight() / v2d.getScale();
    String unit;
    if (rangeX >= 1000) {
      rangeX /= 1000;
      rangeY /= 1000;
      unit = "km";
    } else if (rangeX >= 1) {
      unit = "m";
    } else if (rangeX >= 0.01) {
      rangeX *= 100;
      rangeY *= 100;
      unit = "cm";
    } else {
      rangeX *= 1000;
      rangeY *= 1000;
      unit = "mm";
    }
    aLabel.setText(
        "VIEW RANGE: " +
        fFormat.format(rangeX) + " x " + fFormat.format(rangeY) + " " + unit
    );
  }

  private void setCameraConstrained(boolean aConstrained) {
    fLookAtCameraConstraint3D.setConstrained(aConstrained);
  }
    
  private void setNorthUp(boolean aNorthUp) {
    if (fConstraint2D!=null) {
      fConstraint2D.setNorthUp( aNorthUp );
    }
    fLookAtCameraConstraint3D.setNorthUp( aNorthUp );
  }

  private TLspAWTView createView(ILspView.ViewType aViewType) {
    TLspAWTView view = TLspViewBuilder.newBuilder()
                                      .viewType(aViewType)
                                      .executorThreadCount(0)
                                      .size(SIZE, SIZE)
                                      .addAtmosphere(false)
                                      .background(Color.lightGray)
                                      .buildAWTView();

    view.getOverlayComponent().add(
        new TLspScaleIndicator(view).getLabel(),
        TLcdOverlayLayout.Location.SOUTH
    );
    return view;
  }

  private double computeAccuracy(Point aPoint) {
    ILcdPoint ppi = getLonLat(aPoint, f2DView );
    ILcdPoint geoc = getLonLat(aPoint, f3DView);
    return TLcdEllipsoid.DEFAULT.geodesicDistance(geoc, ppi);
  }

  private ILcdPoint getLonLat(Point aPoint, ILspView aView) {
    ALspViewXYZWorldTransformation v2w = aView.getViewXYZWorldTransformation();

    TLcdXYZPoint wp = new TLcdXYZPoint();
    try {
      v2w.viewAWTPoint2worldSFCT(aPoint, ALspViewXYZWorldTransformation.LocationMode.ELLIPSOID, wp);
    } catch (TLcdOutOfBoundsException ignored) {
    }

    TLcdLonLatHeightPoint mp = new TLcdLonLatHeightPoint();
    TLcdDefaultModelXYZWorldTransformation w2m = new TLcdDefaultModelXYZWorldTransformation();
    w2m.setXYZWorldReference(aView.getXYZWorldReference());
    w2m.setModelReference(new TLcdGeodeticReference());
    try {
      w2m.worldPoint2modelSFCT(wp, mp);
    } catch (TLcdOutOfBoundsException e) {
    }
    mp.move3D(mp.getX(), mp.getY(), 0);

    return mp;
  }

  private void createShipModel() {
    fShipModel = new TLcdVectorModel(
        new TLcdGeodeticReference(),
        new TLcdModelDescriptor("Ship", "Ship", "Ship")
    );

    fShip = new OrientedLonLatPoint(-123.45, 37.75, Math.random() * 360);
    fShipModel.addElement(fShip, ILcdModel.NO_EVENT);

    final int delay = 1000;
    fTimer = new Timer(delay, new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        try (Lock autoUnlock = writeLock(fShipModel)) {
          fShip.setOrientation(fShip.getOrientation() + 0.001 * delay);
          TLcdEllipsoid.DEFAULT.geodesicPointSFCT(fShip, delay, fShip.getOrientation(), fShip);
        }
        fireChangeEvents();
      }
    });
  }

  private void fireChangeEvents() {
    fShipModel.elementChanged(fShip, ILcdModel.FIRE_NOW);
    if ( f2DView != null) {
      try (Lock autoUnlock = readLock(fShipModel)) {
      f2DView.setXYZWorldReference(new TLcdGridReference(
            new TLcdGeodeticDatum(),
          new TLcdAzimuthalEquidistant( fShip.getLon(), fShip.getLat() )
        ));
      }
      // Force constraint to be applied
      TLspViewXYZWorldTransformation2D vw2D = (TLspViewXYZWorldTransformation2D) f2DView.getViewXYZWorldTransformation();
      vw2D.lookAt(
          vw2D.getWorldOrigin(),
          vw2D.getViewOrigin(),
          vw2D.getScale(),
          vw2D.getRotation()
      );
    }
  }

  private void addData(ILspAWTView aView) throws IOException {
    LspDataUtil.instance().model(SampleData.SAN_FRANCISCO).layer().addToView(aView);
    LspDataUtil.instance().grid().addToView(aView);

    ILspLayer shipLayer = TLspShapeLayerBuilder.newBuilder()
                                               .model(fShipModel)
                                               .synchronizePainters(false)
                                               .bodyStyler(
                                                   TLspPaintState.REGULAR,
                                                   new ShipStyler()
                                               )
                                               .selectable(false)
                                               .culling(false)
                                               .build();
    aView.addLayer(shipLayer);

    aView.getServices().getTerrainSupport().setBackgroundStyler(
        TLspRasterStyle.newBuilder()
                       .brightness(0.66f)
                       .build()
    );

    TLcdVectorModel grid = new TLcdVectorModel(
        null,
        new TLcdModelDescriptor("Grid", "Grid", "Grid")
    );
    for (int x = 50; x < 2000; x += 100) {
      TLcdXYLine line1 = new TLcdXYLine(
          new TLcdXYPoint(x, 0),
          new TLcdXYPoint(x, 2000)
      );
      grid.addElement(line1, ILcdModel.NO_EVENT);
      TLcdXYLine line2 = new TLcdXYLine(
          new TLcdXYPoint(0, x),
          new TLcdXYPoint(2000, x)
      );
      grid.addElement(line2, ILcdModel.NO_EVENT);
    }

    if (ADD_2D_VIEW) {
      ILspLayer gridLayer = TLspShapeLayerBuilder.newBuilder()
                                                 .model(grid)
                                                 .bodyStyler(
                                                     TLspPaintState.REGULAR,
                                                     TLspLineStyle.newBuilder()
                                                                  .color(Color.cyan)
                                                                  .opacity(0.3f)
                                                                  .build()
                                                 )
                                                 .culling(false)
                                                 .synchronizePainters(false)
                                                 .selectable(false)
                                                 .build();
      aView.addLayer(gridLayer);
    }
  }

  /**
   * Creates a 3D look at tracking constraint.
   */
  private TrackingCameraConstraint3D createLookAtCameraConstraint3D(
      ILspView aView,
      OrientedLonLatPoint aShip,
      ILcdModel aShipModel
  ) {
    TLspModelElementTrackingPointProvider trackingPointProvider = new TLspModelElementTrackingPointProvider();
    trackingPointProvider.setTrackedObjects(
        aView,
        new Object[]{aShip},
        new ILcdModel[]{aShipModel}
    );
    TrackingCameraConstraint3D constraint = new TrackingCameraConstraint3D(f3DView, fShipModel, aShip);
    constraint.setTrackingPointProvider(trackingPointProvider);
    constraint.setMinDistance(1000);
    return constraint;
  }
}
