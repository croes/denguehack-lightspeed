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

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.IntBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.FormLayout;
import com.luciad.gui.TLcdAWTUtil;
import com.luciad.gui.TLcdIconFactory;
import com.luciad.gui.swing.TLcdSWIcon;
import com.luciad.model.ILcdModel;
import com.luciad.shape.shape2D.TLcdXYBounds;
import com.luciad.util.ILcdFireEventMode;
import com.luciad.view.lightspeed.ILspAWTView;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.TLspLayer;
import com.luciad.view.lightspeed.layer.imageprojection.ILspImageProjectionLayer;

import samples.common.HaloLabel;
import samples.lightspeed.demo.framework.application.Framework;
import samples.lightspeed.demo.framework.data.IOUtil;
import samples.lightspeed.demo.framework.data.themes.AbstractTheme;
import samples.lightspeed.demo.framework.util.video.stream.GStreamerVideoStream;
import samples.lightspeed.demo.framework.util.video.stream.VirtualCamera;

/**
 * Theme that displays an Unmanned aerial vehicle (UAV).
 */
public class UAVTheme extends AbstractTheme {

  private GStreamerVideoStream fVideoStream;
  private JSlider fSlider;
  private UAVScreenOverlayLayerFactory fUAVScreenOverlayLayerFactory;
  private ILcdModel fOverlayModel;
  private ILcdModel fProjectedPointModel;
  private OverlayMouseMotionListener fOverlayMouseMotionListener;

  private Properties fProps;
  private ILcdModel fUavRegionModel;
  private ILcdModel fUavIconModel;
  private ILcdModel fUavFrustumModel;
  private UAVVideoLayerFactory fUavVideoLayerFactory;
  private UAVIconLayerFactory fUavIconLayerFactory;

  public UAVTheme() {
    setName("UAV");
    setCategory("Terrain");
  }

  @Override
  public void initialize(List<ILspView> aViews, Properties aProps) {
    initAttributes(aProps);
    super.initialize(aViews, aProps);
  }

  @Override
  protected List<ILspLayer> createLayers(List<ILspView> aViews) {
    Framework framework = Framework.getInstance();
    List<ILspLayer> allLayers = new ArrayList<ILspLayer>();
    for (ILspView view : aViews) {
      List<ILspLayer> layers = new ArrayList<ILspLayer>();
      ILspImageProjectionLayer regionLayer = fUavVideoLayerFactory.createRegionLayer(fUavRegionModel);
      layers.add(regionLayer);
      framework.registerLayers("layer.id.uav.region", view, Collections.<ILspLayer>singletonList(regionLayer));

      UAVFrustumLayerFactory uavFrustumLayerFactory = new UAVFrustumLayerFactory(regionLayer);
      uavFrustumLayerFactory.configure(fProps);
      ILspLayer frustumLayer = uavFrustumLayerFactory.createFrustumLayer(fUavFrustumModel);
      layers.add(frustumLayer);
      framework.registerLayers("layer.id.uav.frustum", view, Collections.<ILspLayer>singletonList(frustumLayer));

      ILspLayer iconLayer = fUavIconLayerFactory.createIconLayer(fUavIconModel);
      layers.add(iconLayer);
      framework.registerLayers("layer.id.uav.icon", view, Collections.<ILspLayer>singletonList(iconLayer));

      UAVProjectedPointLayerFactory uavProjectedPointLayerFactory = new UAVProjectedPointLayerFactory(regionLayer);
      uavProjectedPointLayerFactory.configure(fProps);
      ILspLayer projectedPointLayer = uavProjectedPointLayerFactory.createLayer(fProjectedPointModel);
      layers.add(projectedPointLayer);
      framework.registerLayers("layer.id.uav.projectedPoint", view, Collections.<ILspLayer>singletonList(projectedPointLayer));

      ILspLayer screenOverlayLayer = fUAVScreenOverlayLayerFactory.createOverlayLayer(fOverlayModel);
      layers.add(screenOverlayLayer);
      framework.registerLayers("layer.id.uav.screenOverlay", view, Collections.<ILspLayer>singletonList(screenOverlayLayer));

      for (ILspLayer layer : layers) {
        view.addLayer(layer);
      }
      allLayers.addAll(layers);
    }

    return allLayers;
  }

  /**
   * Initializes all the attributes of this theme based on the theme properties.
   */
  private void initAttributes(Properties aProperties) {
    fProps = aProperties;

    fUavRegionModel = loadModel("uavRegion", fProps.getProperty("uav.region.src"));
    fUavIconModel = loadModel("uavIcon", fProps.getProperty("uav.icon.src"));
    fOverlayModel = loadModel("uavOverlay", fProps.getProperty("uav.overlay.src"));
    fUavFrustumModel = loadModel("uavFrustum", "");
    fProjectedPointModel = UAVProjectedPointModelFactory.newModel();

    fUavVideoLayerFactory = new UAVVideoLayerFactory();
    fUavVideoLayerFactory.configure(fProps);
    fUavIconLayerFactory = new UAVIconLayerFactory();
    fUavIconLayerFactory.configure(fProps);
    fUAVScreenOverlayLayerFactory = new UAVScreenOverlayLayerFactory();
    fUAVScreenOverlayLayerFactory.configure(fProps);

    fOverlayMouseMotionListener = new OverlayMouseMotionListener(fOverlayModel, fProjectedPointModel);

    fVideoStream = (GStreamerVideoStream) Framework.getInstance()
                                                   .getSharedValue("videoStream");

    fVideoStream.addVideoStreamListener(new GStreamerVideoStream.VideoStreamListener() {
      @Override
      public void frame(VirtualCamera aCamera, int aWidth, int aHeight, IntBuffer aImage) {
        Runnable runnable = new Runnable() {
          public void run() {
            double relValue = (double) fVideoStream.getCurrentFrame() /
                              (double) fVideoStream.getFrameCount();
            int value = (int) Math.floor(relValue * 100.0);
            if (fSlider != null && !fSlider.isEnabled()) {
              fSlider.setValue(value);
            }
          }
        };
        TLcdAWTUtil.invokeLater(runnable);
        List<ILspView> views = getViews();
        for (ILspView view : views) {
          view.invalidate(true, this, "");
        }
      }
    });
  }

  private ILcdModel loadModel(String aModelFactory, String aSource) {
    try {
      File dir = IOUtil.getFile(Framework.getInstance().getDataPath(aSource));
      return Framework.getInstance().getRegisteredModelFactory(aModelFactory).createModel(dir.getAbsolutePath());
    } catch (IOException e) {
      throw new UnsupportedOperationException(e);
    }
  }

  private List<ILspView> getViews() {
    return Framework.getInstance().getFrameworkContext().getViews();
  }

  @Override
  public void activate() {

    for (ILspView view : getViews()) {
      if (view instanceof ILspAWTView) {
        ((ILspAWTView) view).getHostComponent().addMouseMotionListener(
            fOverlayMouseMotionListener
        );
      }
    }

    super.activate();
  }

  @Override
  public void deactivate() {
    if (fVideoStream != null) {
      fVideoStream.stop();
    }
    List<ILspLayer> layers = getLayers();
    for (ILspLayer layer : layers) {
      layer.clearSelection(ILcdFireEventMode.FIRE_NOW);
    }

    for (ILspView view : getViews()) {
      if (view instanceof ILspAWTView) {
        ILspAWTView awtView = (ILspAWTView) view;
        awtView.getHostComponent().removeMouseMotionListener(fOverlayMouseMotionListener);
      }
    }

    super.deactivate();
  }

  @Override
  public void destroy() {
    List<ILspView> views = getViews();
    for (ILspLayer layer : getLayers()) {
      for (ILspView view : views) {
        view.removeLayer(layer);
      }
      ((TLspLayer) layer).setModel(null);
    }
    super.destroy();
  }

  @Override
  public List<JPanel> getThemePanels() {
    return Collections.emptyList();
  }

  @Override
  public JComponent getSouthDockedComponent() {
    return createPlaybackPanel();
  }

  private JPanel createPlaybackPanel() {
    DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout("p, 5dlu, p, 5dlu, p, 5dlu, p"));
    builder.border(Borders.DIALOG);

    // Add title
    builder.append(new HaloLabel("UAV Video Controls", 15, true), 7);
    builder.nextLine();

    // Create time slider + label
    final long startTime = 0;
    final long endTime = (long) (fVideoStream.getDuration() * 1000.0);
    fSlider = new JSlider(0, 100, JSlider.HORIZONTAL);
    fSlider.setOpaque(false);
    fSlider.setValue(0);

    try {
      Class ui = fSlider.getUI().getClass();
      Field field = ui.getDeclaredField("paintValue");
      field.setAccessible(true);
      field.set(fSlider.getUI(), false);
    } catch (Exception ignored) {
    }

    final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
    final HaloLabel label = new HaloLabel(dateFormat.format(startTime));

    fSlider.addChangeListener(new ChangeListener() {
      @Override
      public void stateChanged(ChangeEvent e) {
        double fraction = (double) fSlider.getValue() / fSlider.getMaximum();
        long time = (long) (fraction * (endTime - startTime));
        label.setText(dateFormat.format(startTime + time));

        // User is dragging
        if (fSlider.isEnabled()) {
          double frame = fraction * fVideoStream.getFrameCount();
          fVideoStream.seek((int) frame);
        }
      }
    });

    // Add time slider + label
    builder.append(fSlider, 5);
    builder.append(label);
    builder.nextLine();

    // Add controls
    final TLcdSWIcon playIcon = new TLcdSWIcon(TLcdIconFactory.create(TLcdIconFactory.PLAY_ICON));
    final TLcdSWIcon pauseIcon = new TLcdSWIcon(TLcdIconFactory.create(TLcdIconFactory.PAUSE_ICON));

    final boolean[] playing = new boolean[]{false};

    final JButton play = new JButton(playIcon);
    builder.append(play);
    play.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        if (!playing[0]) {
          play.setIcon(pauseIcon);
          fSlider.setEnabled(false);
          fVideoStream.play();
        } else {
          play.setIcon(playIcon);
          fSlider.setEnabled(true);
          fVideoStream.pause();
        }
        playing[0] = !playing[0];
      }
    });

    JButton stop = new JButton(new TLcdSWIcon(TLcdIconFactory.create(TLcdIconFactory.STOP_ICON)));
    stop.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        playing[0] = false;
        play.setIcon(playIcon);
        fSlider.setValue(0);
        fSlider.setEnabled(true);
        fVideoStream.stop();
      }
    });

    builder.append(stop);
    builder.nextLine();

    JPanel contentPanel = builder.getPanel();
    contentPanel.setOpaque(false);
    contentPanel.setSize(contentPanel.getLayout().preferredLayoutSize(contentPanel));
    return contentPanel;
  }

  private BufferedImage createImage(String aSourceName) {
    try {
      return IOUtil.readImage(aSourceName);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public GStreamerVideoStream getVideoStream() {
    return fVideoStream;
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Simple mouse listener class that highlights the associated position of the mouse
   * on the projected version of the UAV video on the map, when the mouse is moved over
   * the screen-space display of the movie.
   */
  private static class OverlayMouseMotionListener extends MouseAdapter {

    private final ILcdModel fOverlayModel;
    private final ILcdModel fProjectedPointModel;
    private final TLcdXYBounds fTempBounds = new TLcdXYBounds();

    public OverlayMouseMotionListener(ILcdModel aOverlayModel, ILcdModel aProjectedPointModel) {
      fOverlayModel = aOverlayModel;
      fProjectedPointModel = aProjectedPointModel;
    }

    @Override
    public void mouseMoved(MouseEvent e) {
      Component component = e.getComponent();
      if (component == null) {
        return;
      }

      ScreenSpaceBounds bounds = (ScreenSpaceBounds) fOverlayModel.elements().nextElement();
      bounds.retrieveAbsoluteBoundsSFCT(component.getWidth(), component.getHeight(), fTempBounds);
      if (fTempBounds.contains2D(e.getX(), e.getY())) {
        double relX = (e.getX() - fTempBounds.getLocation().getX()) / fTempBounds.getWidth();
        double relY = (e.getY() - fTempBounds.getLocation().getY()) / fTempBounds.getHeight();
        UAVProjectedPointModelFactory.setProjectedPoint(fProjectedPointModel, relX, relY);
      } else {
        UAVProjectedPointModelFactory.removeProjectedPoint(fProjectedPointModel);
      }
    }
  }
}
