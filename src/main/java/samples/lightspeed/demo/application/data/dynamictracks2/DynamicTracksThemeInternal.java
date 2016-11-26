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
package samples.lightspeed.demo.application.data.dynamictracks2;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.ConstantSize;
import com.jgoodies.forms.layout.FormLayout;
import com.luciad.util.collections.TLcdIdentityHashSet;
import com.luciad.util.logging.ILcdLogger;
import com.luciad.util.logging.TLcdLoggerFactory;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.layer.ILspLayer;

import samples.common.HaloLabel;
import samples.lightspeed.demo.framework.application.Framework;
import samples.lightspeed.demo.framework.data.themes.AbstractTheme;

/**
 * Theme that displays trajectories as polylines and tracks as points that move along
 * the trajectories.
 * <p>
 * When the theme is activated, two layers (one for the trajectories and one for the tracks)
 * are retrieved from the demo framework and are made visible. The following keys are used
 * to retrieve those layers:
 * <ul>
 * <li><code>layer.id.enroute.trajectory</code> for the trajectories layer</li>
 * <li><code>layer.id.enroute.track</code> for the tracks layer</li>
 * </ul>
 * Note that this implies that those layers must have been created by the demo framework
 * in order for this theme to work.
 */
public class DynamicTracksThemeInternal extends AbstractTheme {

  private static ILcdLogger sLogger = TLcdLoggerFactory.getLogger(DynamicTracksThemeInternal.class);

  private static final String DYNAMIC_LAYER_ID = "layer.id.density.dynamic";
  private static final String STATIC_LAYER_ID = "layer.id.density.static";
  private static final String TRAJECTORY_LAYER_ID = "layer.id.enroute.trajectory";
  private static final String TRACK_LAYER_ID = "layer.id.enroute.track";

  private static enum Mode {
    TRACKS("Tracks"),
    STATIC_DENSITY("Trajectory density"),
    DYNAMIC_DENSITY("Track density");
    private String fDisplayName;

    Mode(String s) {
      fDisplayName = s;
    }

    @Override
    public String toString() {
      return fDisplayName;
    }
  }

  private Map<Mode, Set<ILspLayer>> fMode2Layer = new HashMap<Mode, Set<ILspLayer>>();
  private Mode fMode = Mode.TRACKS;

  /**
   * Default constructor.
   */
  public DynamicTracksThemeInternal() {
    setName("Air Tracks");
    setCategory("Tracks");
  }

  @Override
  protected List<ILspLayer> createLayers(List<ILspView> aViews) {
    Framework framework = Framework.getInstance();

    List<ILspLayer> allLayers = new ArrayList<ILspLayer>();

    Set<ILspLayer> tracksLayers = new TLcdIdentityHashSet<ILspLayer>();
    tracksLayers.addAll(framework.getLayersWithID(TRAJECTORY_LAYER_ID));

    try {
      // Since the track layer depends on the (optional) realtime package, we need
      // to perform a check before trying to add the layer to this theme
      tracksLayers.addAll(framework.getLayersWithID(TRACK_LAYER_ID));
    } catch (Exception e) {
      e.printStackTrace();
      sLogger.warn("Track layer could not be added to Air Tracks theme.");
    }
    fMode2Layer.put(Mode.TRACKS, tracksLayers);
    allLayers.addAll(tracksLayers);

    Set<ILspLayer> staticDensityLayers = new TLcdIdentityHashSet<ILspLayer>();
    staticDensityLayers.addAll(framework.getLayersWithID(STATIC_LAYER_ID));
    fMode2Layer.put(Mode.STATIC_DENSITY, staticDensityLayers);
    allLayers.addAll(staticDensityLayers);

    Set<ILspLayer> dynamicDensityLayers = new TLcdIdentityHashSet<ILspLayer>();
    try {
      // Since the dynamic density layer depends on the (optional) realtime package,
      // we need to perform a check before trying to add the layer to this theme
      dynamicDensityLayers.addAll(framework.getLayersWithID(DYNAMIC_LAYER_ID));
    } catch (Exception e) {
      e.printStackTrace();
      sLogger.warn("Dynamic density layer could not be added to Air Tracks theme.");
    }
    fMode2Layer.put(Mode.DYNAMIC_DENSITY, dynamicDensityLayers);
    allLayers.addAll(dynamicDensityLayers);

    return allLayers;
  }

  @Override
  public void activate() {
    super.activate();
    setMode(Mode.TRACKS);
  }

  @Override
  public void deactivate() {
    super.deactivate();
  }

  @Override
  public boolean isSimulated() {
    return true;
  }

  @Override
  public List<JPanel> getThemePanels() {
    List<JPanel> layersPanel = super.getThemePanels();
    ArrayList<JPanel> result = new ArrayList<JPanel>();
    result.add(createModePanel());
    result.addAll(layersPanel);
    return result;
  }

  private JPanel createModePanel() {
    // Create buttons to choose between different modes.
    DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout("p,5dlu,p"));
    builder.border(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    boolean isTouchUI = Boolean.parseBoolean(Framework.getInstance().getProperty("ui.touch", "false"));

    if (isTouchUI) {
      builder.lineGapSize(new ConstantSize(5, ConstantSize.DLUY));
    }

    // Add title to content panel
    HaloLabel titleLabel = new HaloLabel("Display Mode", 15, true);
    builder.append(titleLabel, 3);
    builder.nextLine();

    ButtonGroup bg = new ButtonGroup();
    for (final Mode mode : Mode.values()) {
      final AbstractButton button = isTouchUI ?
                                    new JButton(mode.toString()) :
                                    new JRadioButton();

      button.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
          setMode(mode);
        }
      });

      if (!isTouchUI) {
        if (mode == fMode) {
          button.setSelected(true);
        }
        bg.add(button);
      }

      builder.append(button);

      if (!isTouchUI) {
        HaloLabel label = new HaloLabel(mode.toString());
        builder.append(label);
        label.addMouseListener(new MouseAdapter() {
          @Override
          public void mouseClicked(MouseEvent e) {
            button.doClick();
          }
        });
      }

      builder.nextLine();
    }

    // Retrieve panel from builder and set its size (not doing the latter will cause the panel to be invisible)
    JPanel contentPanel = builder.getPanel();
    contentPanel.setSize(contentPanel.getLayout().minimumLayoutSize(contentPanel));

    // Create slide menu
    return (contentPanel);
  }

  private void setMode(Mode mode) {
    Set<ILspLayer> modeLayers = fMode2Layer.get(mode);
    for (ILspLayer layer : getLayers()) {
      layer.setVisible(modeLayers.contains(layer));
    }
  }
}
