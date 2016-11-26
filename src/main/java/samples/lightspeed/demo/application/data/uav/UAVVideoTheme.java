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

import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.IntBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.WeakHashMap;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.FormLayout;
import com.luciad.geometry.cartesian.TLcdCartesian;
import com.luciad.gui.ILcdAction;
import com.luciad.gui.ILcdUndoableListener;
import com.luciad.gui.TLcdAWTUtil;
import com.luciad.gui.TLcdIconFactory;
import com.luciad.gui.swing.TLcdSWIcon;
import com.luciad.model.ILcdModel;
import com.luciad.view.lightspeed.ILspAWTView;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.camera.ALspViewXYZWorldTransformation;
import com.luciad.view.lightspeed.camera.TLspViewXYZWorldTransformation3D;
import com.luciad.view.lightspeed.controller.ALspController;
import com.luciad.view.lightspeed.controller.ILspController;
import com.luciad.view.lightspeed.controller.selection.TLspSelectController;
import com.luciad.view.lightspeed.editor.snapping.ILspSnappable;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.TLspLayer;
import com.luciad.view.lightspeed.layer.imageprojection.ILspImageProjectionLayer;

import samples.common.HaloLabel;
import samples.common.action.ShowPopupAction;
import samples.common.action.ShowPropertiesAction;
import samples.lightspeed.common.ToolBar;
import samples.lightspeed.common.controller.ControllerFactory;
import samples.lightspeed.demo.application.data.support.los.LOSSupport;
import samples.lightspeed.demo.framework.application.Framework;
import samples.lightspeed.demo.framework.data.IOUtil;
import samples.lightspeed.demo.framework.data.themes.AbstractTheme;
import samples.lightspeed.demo.framework.util.video.stream.GStreamerVideoStream;
import samples.lightspeed.demo.framework.util.video.stream.VirtualCamera;

/**
 * Theme that displays an Unmanned aerial vehicle (UAV) from the perspective of the UAV.
 */
public class UAVVideoTheme extends AbstractTheme {

  private final WeakHashMap<ILspView, ILspController> fViewToController = new WeakHashMap<ILspView, ILspController>();
  private GStreamerVideoStream fVideoStream;
  private JSlider fSlider;

  private Properties fProps;
  private ILcdModel fUavRegionModel;
  private UAVVideoLayerFactory fUavVideoLayerFactory;
  private GStreamerVideoStream.VideoStreamListener fVideoStreamListener;

  private LOSSupport fLOSSupport;

  public UAVVideoTheme() {
    setName("UAV");
    setCategory("Terrain");
    fLOSSupport = new LOSSupport();
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

      // UAV layer
      ILspImageProjectionLayer regionLayer = fUavVideoLayerFactory.createRegionLayer(fUavRegionModel);
      if (regionLayer instanceof ILspSnappable) {
        ((ILspSnappable) regionLayer).setSnapTarget(false);
      }
      layers.add(regionLayer);
      framework.registerLayers("layer.id.uav.region", view, Collections.<ILspLayer>singletonList(regionLayer));

      // Los layer
      ILspLayer losLayer = fLOSSupport.createLayer(view, "UAV LOS");
      framework.registerLayers("layer.id.uav.los", view, Collections.singleton(losLayer));
      layers.add(losLayer);

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

    fUavVideoLayerFactory = new UAVVideoLayerFactory();
    fUavVideoLayerFactory.configure(fProps);

    fVideoStream = (GStreamerVideoStream) Framework.getInstance().getSharedValue("videoStream");

    fVideoStreamListener = new GStreamerVideoStream.VideoStreamListener() {
      @Override
      public void frame(final VirtualCamera aCamera, int aWidth, int aHeight, IntBuffer aImage) {
        TLcdAWTUtil.invokeLater(new Runnable() {
          public void run() {
            // Update sliders
            double relValue = (double) fVideoStream.getCurrentFrame() /
                              (double) fVideoStream.getFrameCount();
            int value = (int) Math.floor(relValue * 100.0);
            if (fSlider != null && !fSlider.isEnabled()) {
              fSlider.setValue(value);
            }
            // Update camera
            for (ILspView view : getViews()) {
              view.setAutoUpdate(false);
              ALspViewXYZWorldTransformation v2w = view.getViewXYZWorldTransformation();
              if (v2w instanceof TLspViewXYZWorldTransformation3D) {
                TLspViewXYZWorldTransformation3D v2w3D = (TLspViewXYZWorldTransformation3D) v2w;
                v2w3D.lookAt(aCamera.getRef(),
                             TLcdCartesian.distance3D(aCamera.getEye(), aCamera.getRef()),
                             aCamera.getYaw(), aCamera.getPitch(), aCamera.getRoll());
                v2w3D.setFieldOfView(aCamera.getFov());
              }
              view.setAutoUpdate(true);
            }
          }
        });
      }
    };
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
      // Fit view on video
      if (view instanceof ILspAWTView) {
        int viewWidth = view.getWidth();
        int viewHeight = view.getHeight();
        double oldAspectRatio = (double) viewWidth / (double) viewHeight;
        double aspectRatio = fVideoStream.getCurrentCamera().getAspectRatio();
        if (oldAspectRatio < aspectRatio) {
          viewHeight = (int) Math.round(viewWidth / aspectRatio);
        } else {
          viewWidth = (int) Math.round(viewHeight * aspectRatio);
        }

        Frame frame = TLcdAWTUtil.findParentFrame(((ILspAWTView) view).getHostComponent());
        frame.setSize(frame.getWidth() + viewWidth - view.getWidth(),
                      frame.getHeight() + viewHeight - view.getHeight());
      }
      // Disable navigation
      fViewToController.put(view, view.getController());
      view.setController(createController(Framework.getInstance().getUndoManager(), view));
    }

    // Add video stream listener
    fVideoStream.addVideoStreamListener(fVideoStreamListener, true);

    super.activate();
  }

  private ILspController createController(ILcdUndoableListener aUndoableListener, ILspView aView) {
    ShowPropertiesAction propertiesAction = new ShowPropertiesAction(aView, ToolBar.getParentComponent(aView));
    ILcdAction doubleClickAction = propertiesAction;
    ILcdAction[] popupMenuActions = {propertiesAction};

    ALspController editController = ControllerFactory.createDefaultEditController(aUndoableListener, aView);
    TLspSelectController selectController = ControllerFactory.createDefaultSelectController();

    if (aView instanceof ILspAWTView && popupMenuActions != null && popupMenuActions.length > 0) {
      selectController.setContextAction(
          new ShowPopupAction(popupMenuActions, ((ILspAWTView) aView).getOverlayComponent()));
    }
    if (doubleClickAction != null) {
      selectController.setDoubleClickAction(doubleClickAction);
    }

    editController.appendController(selectController);

    return editController;
  }

  @Override
  public void deactivate() {
    fVideoStream.stop();
    fVideoStream.removeVideoStreamListener(fVideoStreamListener);

    for (ILspView view : getViews()) {
      // Restore navigation
      view.setController(fViewToController.get(view));
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

    final JButton play = new JButton(playIcon);
    builder.append(play);
    play.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        if (fVideoStream.getState() != GStreamerVideoStream.StreamState.PLAYING) {
          play.setIcon(pauseIcon);
          fSlider.setEnabled(false);
          fVideoStream.play();
        } else {
          play.setIcon(playIcon);
          fSlider.setEnabled(true);
          fVideoStream.pause();
        }
      }
    });

    JButton stop = new JButton(new TLcdSWIcon(TLcdIconFactory.create(TLcdIconFactory.STOP_ICON)));
    stop.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        play.setIcon(playIcon);
        fSlider.setValue(0);
        fSlider.setEnabled(true);
        fVideoStream.stop();
      }
    });

    fVideoStream.addPropertyChangeListener(new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        if ("state".equals(evt.getPropertyName())) {
          switch ((GStreamerVideoStream.StreamState) evt.getNewValue()) {
          case PLAYING:
            play.setIcon(pauseIcon);
            fSlider.setEnabled(false);
            break;
          case PAUSED:
            play.setIcon(playIcon);
            fSlider.setEnabled(true);
            break;
          case STOPPED:
            play.setIcon(playIcon);
            fSlider.setEnabled(true);
            fSlider.setValue(0);
            break;
          }
        }
      }
    });

    builder.append(stop);
    builder.nextLine();

//    builder.append(fLOSSupport.createLOSCreationButton());

    JPanel contentPanel = builder.getPanel();

    JPanel combinedPanel = new JPanel();
    combinedPanel.setLayout(new FlowLayout());
    combinedPanel.add(contentPanel);
    combinedPanel.add(fLOSSupport.createLOSCreationButton());
    combinedPanel.setSize(combinedPanel.getLayout().preferredLayoutSize(combinedPanel));
    return combinedPanel;
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

}
