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
package samples.lightspeed.demo.application.gui.menu;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.IndexColorModel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.ConstantSize;
import com.jgoodies.forms.layout.FormLayout;
import com.luciad.tea.lightspeed.hypsometry.ALspHypsometric2DDirectionShader;
import com.luciad.tea.lightspeed.hypsometry.ALspHypsometric3DDirectionShader;
import com.luciad.tea.lightspeed.hypsometry.ALspHypsometricShader;
import com.luciad.tea.lightspeed.hypsometry.TLspHypsometricCreaseShader;
import com.luciad.tea.lightspeed.hypsometry.TLspHypsometricHillShadingShader;
import com.luciad.tea.lightspeed.hypsometry.TLspHypsometricOrientationAngleShader;
import com.luciad.tea.lightspeed.hypsometry.TLspHypsometricOrientationShader;
import com.luciad.tea.lightspeed.hypsometry.TLspHypsometricShadingStyle;
import com.luciad.tea.lightspeed.hypsometry.TLspHypsometricSlopeAngleShader;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.layer.ILspEditableStyledLayer;
import com.luciad.view.lightspeed.layer.TLspPaintRepresentationState;
import com.luciad.view.lightspeed.style.ALspStyle;
import com.luciad.view.lightspeed.style.styler.ILspStyler;
import com.luciad.view.lightspeed.style.styler.TLspEditableStyler;

import samples.common.HaloLabel;
import samples.lightspeed.common.AngleControlComponent;
import samples.lightspeed.demo.framework.application.Framework;
import samples.lightspeed.demo.framework.data.themes.AbstractTheme;
import samples.tea.lightspeed.hypsometry.HypsometryColorModelFactory;

/**
 */
public class HypsometryPanelFactory implements IThemePanelFactory {

  /**
   * Display names for the various hypsometric shaders.
   */
  public static final String[] HYPSOMETRY_NAMES = {
      "Shading",
      "Azimuth",
      "Orientation",
      "Slope",
      "Ridges & Valleys"
  };

  /**
   * Instances of the various hypsometric shaders.
   */
  public static final ALspHypsometricShader[] HYPSOMETRY_SHADERS = {
      TLspHypsometricHillShadingShader.newBuilder().build(),
      TLspHypsometricOrientationAngleShader.newBuilder().build(),
      TLspHypsometricOrientationShader.newBuilder().build(),
      TLspHypsometricSlopeAngleShader.newBuilder().build(),
      TLspHypsometricCreaseShader.newBuilder().build()
  };

  /**
   * Color models for the various hypsometric shaders.
   */
  public static final IndexColorModel[] HYPSOMETRY_COLORMODELS = {
      HypsometryColorModelFactory.createShadingColorModel(),
      HypsometryColorModelFactory.createAzimuthColorModel(),
      HypsometryColorModelFactory.createOrientationColorModel(),
      HypsometryColorModelFactory.createSlopeAngleColorModel(),
      HypsometryColorModelFactory.createRidgeValleyColorModel()
  };

  // Index to keep track of which hypsometry shader was selected last
  private static int sHypsometryIndex = 0;

  private ShaderControls fShaderControls;

  public HypsometryPanelFactory() {
    // Default constructor
  }

  @Override
  public List<JPanel> createThemePanels(AbstractTheme aTheme) {
    return Arrays.asList(createShadersPanel(), createShaderControlsPanel());
  }

  public JPanel createShadersPanel() {
    // Set proper initial index
    List<ILspEditableStyledLayer> layers = (List<ILspEditableStyledLayer>) Framework.getInstance().getSharedValue("hypsometry.layers");
    if (layers != null && layers.size() > 0) {
      ILspEditableStyledLayer layer = layers.get(0);
      ILspStyler styler = layer.getStyler(TLspPaintRepresentationState.REGULAR_BODY);
      if (styler instanceof TLspHypsometricShadingStyle) {
        TLspHypsometricShadingStyle style = (TLspHypsometricShadingStyle) styler;
        sHypsometryIndex = Arrays.asList(HYPSOMETRY_SHADERS).indexOf(style.getShader());
      }
    }

    // Create buttons to choose between different hypsometric shaders.
    DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout("p,5dlu,p"));
    builder.border(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    boolean isTouchUI = Boolean.parseBoolean(Framework.getInstance().getProperty("ui.touch", "false"));

    if (isTouchUI) {
      builder.lineGapSize(new ConstantSize(5, ConstantSize.DLUY));
    }

    // Add title to content panel
    HaloLabel titleLabel = new HaloLabel("Shaders", 15, true);
    builder.append(titleLabel, 3);
    builder.nextLine();

    ButtonGroup bg = new ButtonGroup();
    for (int i = 0; i < HYPSOMETRY_NAMES.length; ++i) {
      final int index = i;
      AbstractButton button = isTouchUI ? new JButton(HYPSOMETRY_NAMES[i]) : new JRadioButton();

      button.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
          List<ILspEditableStyledLayer> layers = (List<ILspEditableStyledLayer>) Framework.getInstance().getSharedValue("hypsometry.layers");
          if (layers != null) {
            for (ILspEditableStyledLayer layer : layers) {
              // Each button should apply a different shader/color
              // model combination to the hypsometric layer.
              setHypsometryStyle(
                  layer,
                  TLspHypsometricShadingStyle.newBuilder().
                      shader(HYPSOMETRY_SHADERS[index]).
                                                 colorModel(HYPSOMETRY_COLORMODELS[index]).
                                                 build()
              );
              updateShaderControls(HYPSOMETRY_SHADERS[index]);
              sHypsometryIndex = index;
              for (ILspView view : layer.getCurrentViews()) {
                view.invalidate(true, this, "");
              }
            }
          }
        }
      });

      if (!isTouchUI) {
        if (i == sHypsometryIndex) {
          button.setSelected(true);
        }
        bg.add(button);
      }

      builder.append(button);

      if (!isTouchUI) {
        builder.append(new HaloLabel(HYPSOMETRY_NAMES[i]));
      }
      builder.nextLine();
    }

    // Retrieve panel from builder and set its size (not doing the latter will cause the panel to be invisible)
    JPanel contentPanel = builder.getPanel();
    contentPanel.setSize(contentPanel.getLayout().minimumLayoutSize(contentPanel));
    contentPanel.setCursor(Cursor.getDefaultCursor());

    // Create slide menu
    return contentPanel;
  }

  private void updateShaderControls(ALspHypsometricShader shader) {
    if (shader instanceof ALspHypsometric2DDirectionShader) {
      ALspHypsometric2DDirectionShader directionShader = (ALspHypsometric2DDirectionShader) shader;
      fShaderControls.setYawEnabled(true);
      fShaderControls.setPitchEnabled(false);
      fShaderControls.setReferenceDirection(directionShader.getReferenceDirectionX(),
                                            directionShader.getReferenceDirectionY());
    } else if (shader instanceof ALspHypsometric3DDirectionShader) {
      ALspHypsometric3DDirectionShader directionShader = (ALspHypsometric3DDirectionShader) shader;
      fShaderControls.setYawEnabled(true);
      fShaderControls.setPitchEnabled(true);
      fShaderControls.setReferenceDirection(directionShader.getReferenceDirectionX(),
                                            directionShader.getReferenceDirectionY(),
                                            directionShader.getReferenceDirectionZ());
    } else {
      fShaderControls.setYawEnabled(false);
      fShaderControls.setPitchEnabled(false);
    }
  }

  public JPanel createShaderControlsPanel() {

    // Create content panel
    DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout("p,5dlu,p"));
    builder.border(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    // Add title to content panel
    HaloLabel titleLabel = new HaloLabel("Shader Controls", 15, true);
    builder.append(titleLabel, 3);
    builder.nextLine();

    // Add controls
    fShaderControls = new ShaderControls();
    builder.append(fShaderControls.getYawControl());
    builder.append(fShaderControls.getPitchControl());
    builder.nextLine();

    // Create panel and set its size (not doing the latter will cause it to be invisible)
    JPanel contentPanel = builder.getPanel();
    contentPanel.setSize(contentPanel.getLayout().preferredLayoutSize(contentPanel));
    contentPanel.setCursor(Cursor.getDefaultCursor());

    updateShaderControls(HYPSOMETRY_SHADERS[0]);

    // Create slide menu
    return contentPanel;
  }

  //////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Manages the reference direction controls.
   */
  private class ShaderControls implements ChangeListener {

    // GUI components that control shader properties
    private AngleControlComponent fYawControl;
    private AngleControlComponent fPitchControl;

    private ShaderControls() {
      fYawControl = new AngleControlComponent(AngleControlComponent.Type.FULL, 0, 360) {
        @Override
        public Dimension getPreferredSize() {
          return new Dimension(80, 80);
        }
      };
      fYawControl.addChangeListener(this);
      fPitchControl = new AngleControlComponent(AngleControlComponent.Type.SEGMENT, 0, 90) {
        @Override
        public Dimension getPreferredSize() {
          return new Dimension(60, 60);
        }
      };
      fPitchControl.addChangeListener(this);

      fYawControl.setEnabled(false);
      fPitchControl.setEnabled(false);
    }

    public AngleControlComponent getYawControl() {
      return fYawControl;
    }

    public AngleControlComponent getPitchControl() {
      return fPitchControl;
    }

    public void setYawEnabled(boolean aEnabled) {
      fYawControl.setEnabled(aEnabled);
    }

    public void setPitchEnabled(boolean aEnabled) {
      fPitchControl.setEnabled(aEnabled);
    }

    public void setReferenceDirection(double aX, double aY) {
      double yaw = 90 - Math.toDegrees(Math.atan2(aY, aX));

      while (yaw < 0) {
        yaw += 360;
      }
      while (yaw > 360) {
        yaw -= 360;
      }
      fYawControl.setAngle(yaw);
    }

    public void setReferenceDirection(double aX, double aY, double aZ) {
      setReferenceDirection(aX, aY);
      double pitch = 90 - Math.toDegrees(Math.acos(aZ));
      while (pitch < 0) {
        pitch += 360;
      }

      while (pitch > 360) {
        pitch -= 360;
      }

      if (pitch > 90) {
        pitch = 90;
      }

      fPitchControl.setAngle(pitch);
    }

    @Override
    public void stateChanged(ChangeEvent e) {
      // Convert yaw and pitch to radians
      double yaw = Math.toRadians(90 - fYawControl.getAngle());
      double pitch = Math.toRadians(90 - fPitchControl.getAngle());

      // Apply the reference direction to the shaders that use it
      Framework app = Framework.getInstance();
      List<ILspEditableStyledLayer> layers = (List<ILspEditableStyledLayer>) app.getSharedValue("hypsometry.layers");
      if (layers != null) {
        for (ILspEditableStyledLayer layer : layers) {
          // Each button should apply a different shader/color model combination to the
          // hypsometric layer.
          TLspHypsometricShadingStyle style = getHypsometryStyle(layer);
          ALspHypsometricShader shader = style.getShader();
          if (shader instanceof ALspHypsometric2DDirectionShader) {
            // Convert spherical to cartesian coordinates
            double x = Math.cos(yaw);
            double y = Math.sin(yaw);

            ALspHypsometric2DDirectionShader directionShader = (ALspHypsometric2DDirectionShader) shader;
            shader = directionShader.asBuilder().referenceDirection(x, y).build();
          } else if (shader instanceof ALspHypsometric3DDirectionShader) {
            // Convert spherical to cartesian coordinates
            double x = Math.sin(pitch) * Math.cos(yaw);
            double y = Math.sin(pitch) * Math.sin(yaw);
            double z = Math.cos(pitch);

            ALspHypsometric3DDirectionShader directionShader = (ALspHypsometric3DDirectionShader) shader;
            shader = directionShader.asBuilder().referenceDirection(x, y, z).build();
          }
          setHypsometryStyle(
              layer,
              style.asBuilder().shader(shader).build()
          );

          for (ILspView view : layer.getCurrentViews()) {
            view.invalidate(true, this, "");
          }
        }
      }

    }
  }

  private TLspHypsometricShadingStyle getHypsometryStyle(ILspEditableStyledLayer aLayer) {
    ILspStyler styler = aLayer.getStyler(TLspPaintRepresentationState.REGULAR_BODY);
    if (styler instanceof TLspHypsometricShadingStyle) {
      return (TLspHypsometricShadingStyle) styler;
    }
    if (styler instanceof TLspEditableStyler) {
      List<ALspStyle> defaultStyle = ((TLspEditableStyler) styler).getDefaultStyle();
      for (ALspStyle style : defaultStyle) {
        if (style instanceof TLspHypsometricShadingStyle) {
          return (TLspHypsometricShadingStyle) style;
        }
      }
    }
    return null;
  }

  private void setHypsometryStyle(ILspEditableStyledLayer aLayer, TLspHypsometricShadingStyle aStyle) {
    ILspStyler styler = aLayer.getStyler(TLspPaintRepresentationState.REGULAR_BODY);
    if (styler instanceof TLspHypsometricShadingStyle) {
      aLayer.setStyler(TLspPaintRepresentationState.REGULAR_BODY, aStyle);
    }
    if (styler instanceof TLspEditableStyler) {
      TLspEditableStyler editableStyler = (TLspEditableStyler) styler;
      ArrayList<ALspStyle> defaultStyle = new ArrayList<ALspStyle>(editableStyler.getDefaultStyle());
      for (int i = 0; i < defaultStyle.size(); i++) {
        if (defaultStyle.get(i) instanceof TLspHypsometricShadingStyle) {
          defaultStyle.set(i, aStyle);
        }
      }
      editableStyler.setDefaultStyle(defaultStyle);
    }
  }

}
