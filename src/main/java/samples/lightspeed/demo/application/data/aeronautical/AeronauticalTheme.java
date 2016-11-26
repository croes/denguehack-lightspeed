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
package samples.lightspeed.demo.application.data.aeronautical;

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
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.layer.ILspLayer;

import samples.common.HaloLabel;
import samples.lightspeed.demo.application.data.airspaces.AirspaceTheme;
import samples.lightspeed.demo.application.data.aixm5.AIXM5Theme;
import samples.lightspeed.demo.framework.application.Framework;
import samples.lightspeed.demo.framework.data.themes.AbstractTheme;
import samples.lightspeed.demo.framework.data.themes.ThemeAnimation;

/**
 * Merges the airspace theme and the aixm5 theme into a single Aeronautical theme.
 * The theme provides a slide panel to switch.
 */
public class AeronauticalTheme extends AbstractTheme {

  private static enum Mode {
    AIRSPACES("Airspaces"),
    Runways("Runways");

    private String fDisplayName;

    Mode(String s) {
      fDisplayName = s;
    }

    @Override
    public String toString() {
      return fDisplayName;
    }
  }

  private Mode fMode = Mode.AIRSPACES;

  private AirspaceTheme fAirspaceTheme;
  private AIXM5Theme fAIXM5Theme;

  private Map<Mode, Set<ILspLayer>> fMode2Layer = new HashMap<Mode, Set<ILspLayer>>();

  private List<ILspView> fViews;

  public AeronauticalTheme() {
    fAirspaceTheme = new AirspaceTheme();
    fAIXM5Theme = new AIXM5Theme();
    setName("Aeronautical");
    setCategory("Shapes");
  }

  @Override
  protected List<ILspLayer> createLayers(List<ILspView> aViews) {
    fViews = new ArrayList<ILspView>(aViews);

    ArrayList<ILspLayer> result = new ArrayList<ILspLayer>();
    List<ILspLayer> airspaceLayers = fAirspaceTheme.createLayers(aViews);
    List<ILspLayer> aixm5Layers = fAIXM5Theme.createLayers(aViews);
    result.addAll(airspaceLayers);
    result.addAll(aixm5Layers);

    fMode2Layer.put(Mode.AIRSPACES, new TLcdIdentityHashSet<ILspLayer>(airspaceLayers));
    fMode2Layer.put(Mode.Runways, new TLcdIdentityHashSet<ILspLayer>(aixm5Layers));
    return result;
  }

  @Override
  public boolean isSimulated() {
    return fAirspaceTheme.isSimulated() || fAIXM5Theme.isSimulated();
  }

  @Override
  public void activate() {
    super.activate();
    fAirspaceTheme.activate();
    // call AIXM5 last to activate the simulator
    fAIXM5Theme.activate();
  }

  @Override
  public void deactivate() {
    super.deactivate();
    fAirspaceTheme.deactivate();
    fAIXM5Theme.deactivate();
  }

  @Override
  public List<JPanel> getThemePanels() {
    List<JPanel> layersPanel = super.getThemePanels();
    ArrayList<JPanel> result = new ArrayList<JPanel>();
    result.add(createDataPanel());
    result.addAll(layersPanel);
    return result;
  }

  private JPanel createDataPanel() {
    // Create buttons to choose between different modes.
    DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout("p,5dlu,p"));
    builder.border(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    boolean isTouchUI = Boolean
        .parseBoolean(Framework.getInstance().getProperty("ui.touch", "false"));

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

      final ThemeAnimation[] animation = {null};
      button.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
          animation[0] = new ThemeAnimation(
              mode.toString(),
              new ArrayList<ILspView>(fViews)
          );
          animation[0].addAnimationListener(new ThemeAnimation.AnimationListener() {
            @Override
            public void animationStarted() {
              setMode(null);
            }

            @Override
            public void animationStopped() {
              setMode(mode);
              animation[0].removeAnimationListener(this);
            }
          });
          animation[0].doAnimation();
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
        HaloLabel label = new HaloLabel(mode.toString() + "      ");
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
    return contentPanel;
  }

  private void setMode(Mode mode) {
    if (mode == null) {
      for (ILspLayer layer : getLayers()) {
        layer.setVisible(false);
      }
    } else {
      Set<ILspLayer> modeLayers = fMode2Layer.get(mode);
      for (ILspLayer layer : getLayers()) {
        layer.setVisible(modeLayers.contains(layer));
      }
    }
  }

  public AirspaceTheme getAirspaceTheme() {
    return fAirspaceTheme;
  }

  public AIXM5Theme getAIXM5Theme() {
    return fAIXM5Theme;
  }
}
