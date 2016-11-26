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

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.luciad.gui.ALcdAction;
import com.luciad.gui.ILcdIcon;
import com.luciad.gui.TLcdIconFactory;
import com.luciad.gui.swing.TLcdGreyIcon;
import com.luciad.gui.swing.TLcdSWAction;
import com.luciad.gui.swing.TLcdSWIcon;
import com.luciad.util.ILcdSelectionListener;
import com.luciad.util.TLcdSelectionChangedEvent;
import com.luciad.view.lightspeed.ILspAWTView;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.controller.ILspController;
import com.luciad.view.TLcdAWTEventFilterBuilder;
import com.luciad.view.lightspeed.controller.manipulation.TLspCreateController;
import com.luciad.view.lightspeed.layer.ILspInteractivePaintableLayer;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.TLspPaintRepresentationState;
import com.luciad.view.lightspeed.style.styler.ILspStyler;
import com.luciad.view.lightspeed.style.styler.TLspEditableStyler;

import samples.common.HaloLabel;
import samples.lightspeed.common.controller.ControllerFactory;
import samples.lightspeed.common.controller.LonLatCreateControllerModel;
import samples.lightspeed.demo.application.controller.TouchCreateController;
import samples.lightspeed.demo.framework.application.Framework;
import samples.lightspeed.demo.framework.application.FrameworkContext;
import samples.lightspeed.demo.framework.data.themes.AbstractTheme;
import samples.lightspeed.demo.framework.gui.HueSlider;
import samples.lightspeed.style.editable.StyleEditorModel;

/**
 * Creates theme panels for the editing theme.
 */
public class EditingPanelFactory extends MouseAdapter implements IThemePanelFactory {

  private JButton fCancelButton;
  private JButton fFinishButton;
  private Dimension fPreferredButtonSize;
  private HaloLabel fLabel;
  private HueSlider fFillColorSlider;
  private HueSlider fLineColorSlider;
  private JSlider fLineWidthSlider;

  /**
   * Default constructor.
   */
  public EditingPanelFactory() {
    fLabel = new HaloLabel(" ", 12, true);
  }

  @Override
  public void mouseEntered(MouseEvent e) {
    if (e.getSource() instanceof JToggleButton) {
      fLabel.setText(((JToggleButton) e.getSource()).getToolTipText());
    }
  }

  @Override
  public void mouseExited(MouseEvent e) {
    fLabel.setText(" ");
  }

  @Override
  public List<JPanel> createThemePanels(AbstractTheme aTheme) {
    return Collections.emptyList();
  }

  public JComponent getSouthDockedComponent() {
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    panel.setOpaque(false);
    // Wrap both panels in a JPanel to force center alignment
    JPanel shapeCreationPanel = new JPanel();
    shapeCreationPanel.setOpaque(false);
    shapeCreationPanel.add(createShapeCreationPanel());
    JPanel styleEditingPanel = new JPanel();
    styleEditingPanel.setOpaque(false);
    styleEditingPanel.add(createStyleEditingPanel());
    panel.add(shapeCreationPanel);
    panel.add(styleEditingPanel);
    return panel;
  }

  private JPanel createStyleEditingPanel() {
    DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout("p, 5dlu, p, 5dlu, p"));
    builder.border(BorderFactory.createEmptyBorder(0, 10, 10, 10));

    // Add title
    builder.append(new HaloLabel("Style Settings", 15, true), 3);
    builder.nextLine();

    // Create sliders
    fFillColorSlider = new HueSlider();
    fFillColorSlider.setOpaque(false);
    fFillColorSlider.setValue(0);

    // Add slider
    builder.append(new HaloLabel("Fill Color", 12, true), 1);
    builder.append(fFillColorSlider, 1);
    builder.nextLine();

    fLineColorSlider = new HueSlider();
    fLineColorSlider.setOpaque(false);
    fLineColorSlider.setValue(0);

    // Add slider
    builder.append(new HaloLabel("Line Color", 12, true), 1);
    builder.append(fLineColorSlider, 1);
    builder.nextLine();

    fLineWidthSlider = new JSlider(0, 300, JSlider.HORIZONTAL);
    fLineWidthSlider.setOpaque(false);
    fLineWidthSlider.setValue(0);

    try {
      Class ui = fLineWidthSlider.getUI().getClass();
      Field field = ui.getDeclaredField("paintValue");
      field.setAccessible(true);
      field.set(fLineWidthSlider.getUI(), false);
    } catch (Exception ignored) {
    }

    // Add slider
    builder.append(new HaloLabel("Line Width", 12, true), 1);
    builder.append(fLineWidthSlider, 1);
    builder.nextLine();

    new StyleMediator(this, fFillColorSlider, fLineColorSlider, fLineWidthSlider);

    JPanel contentPanel = builder.getPanel();
    contentPanel.setSize(contentPanel.getLayout().preferredLayoutSize(contentPanel));
    contentPanel.setCursor(Cursor.getDefaultCursor());
    contentPanel.setOpaque(false);
    return contentPanel;
  }

  private JPanel createShapeCreationPanel() {
    boolean isTouchEnabled = Boolean.parseBoolean(Framework.getInstance().getProperty("ui.touch", "false"));

    fPreferredButtonSize = isTouchEnabled ? new Dimension(36, 36) : new Dimension(25, 25);

    HashMap<ILspView, ILspLayer> view2layer = getViewLayerMap();
    DefaultFormBuilder builder;
    if (isTouchEnabled) {
      builder = new DefaultFormBuilder(new FormLayout("p,10dlu,p,10dlu,p,10dlu,p,10dlu,p,10dlu,p,10dlu,p,10dlu,p,10dlu,p"));
      fCancelButton = new JButton("Cancel");
      fFinishButton = new JButton("Finish");
      fCancelButton.setEnabled(false);
      fFinishButton.setEnabled(false);
    } else {
      builder = new DefaultFormBuilder(new FormLayout("p,5dlu,p,5dlu,p,5dlu,p,5dlu,p,5dlu,p,5dlu,p,5dlu,p,5dlu,p"));
    }
    builder.border(BorderFactory.createEmptyBorder(10, 10, 0, 10));

    // Add title to content panel
    HaloLabel titleLabel = new HaloLabel("Shape Creation", 15, true);
    builder.append(titleLabel, 17);
    builder.nextLine();

    // Add 2D creation controllers to panel
    ButtonGroup bg = new ButtonGroup();

    ToggleExtrudeAction extrudeAction = new ToggleExtrudeAction();
    AbstractButton toggleExtrudeButton = new JToggleButton() {
      @Override
      public Dimension getPreferredSize() {
        return fPreferredButtonSize;
      }
    };

    toggleExtrudeButton.addActionListener(extrudeAction);
    toggleExtrudeButton.setIcon(new TLcdSWIcon(TLcdIconFactory.create(TLcdIconFactory.EXTRUDE_ICON)));
    toggleExtrudeButton.setToolTipText("Toggle extrusion");
    toggleExtrudeButton.setEnabled(extrudeAction.isEnabled());
    toggleExtrudeButton.addMouseListener(this);

    builder.append(toggleExtrudeButton);

    builder.append(createCreationButton(bg, extrudeAction, view2layer, LonLatCreateControllerModel.Type.POLYLINE, TLcdIconFactory.create(TLcdIconFactory.DRAW_POLYLINE_ICON), "Create polyline"));
    builder.append(createCreationButton(bg, extrudeAction, view2layer, LonLatCreateControllerModel.Type.POLYGON, TLcdIconFactory.create(TLcdIconFactory.DRAW_POLYGON_ICON), "Create polygon"));
    builder.append(createCreationButton(bg, extrudeAction, view2layer, LonLatCreateControllerModel.Type.ARC, TLcdIconFactory.create(TLcdIconFactory.DRAW_ELLIPTICAL_ARC_ICON), "Create arc"));
    builder.append(createCreationButton(bg, extrudeAction, view2layer, LonLatCreateControllerModel.Type.ARC_BY_3_POINTS, TLcdIconFactory.create(TLcdIconFactory.DRAW_ARC_BY_3_POINTS_ICON), "Create arc by 3 points"));
    builder.append(createCreationButton(bg, extrudeAction, view2layer, LonLatCreateControllerModel.Type.ARCBAND, TLcdIconFactory.create(TLcdIconFactory.DRAW_ARC_BAND_ICON), "Create arc band"));
    builder.append(createCreationButton(bg, extrudeAction, view2layer, LonLatCreateControllerModel.Type.BOUNDS, TLcdIconFactory.create(TLcdIconFactory.DRAW_BOUNDS_ICON), "Create bounds"));
    builder.append(createCreationButton(bg, extrudeAction, view2layer, LonLatCreateControllerModel.Type.VARIABLE_GEO_BUFFER, TLcdIconFactory.create(TLcdIconFactory.DRAW_VARIABLE_BUFFER_ICON), "Create buffer"));
    builder.append(createCreationButton(bg, extrudeAction, view2layer, LonLatCreateControllerModel.Type.ELLIPSE, TLcdIconFactory.create(TLcdIconFactory.DRAW_ELLIPSE_ICON), "Create ellipse"));
    builder.nextLine();
    builder.append(fLabel, 17);

    if (fCancelButton != null && fFinishButton != null) {
      builder.nextLine();
      builder.append(fCancelButton, 8);
      builder.append(fFinishButton, 8);
    }

    // Create panel and set its size
    JPanel contentPanel = builder.getPanel();
    contentPanel.setSize(contentPanel.getLayout().preferredLayoutSize(contentPanel));
    contentPanel.setCursor(Cursor.getDefaultCursor());
    contentPanel.setOpaque(false);
    return contentPanel;
  }

  private HashMap<ILspView, ILspLayer> getViewLayerMap() {
    List<ILspView> views = Framework.getInstance().getFrameworkContext().getViews();
    HashMap<ILspView, ILspLayer> view2layer = new HashMap<ILspView, ILspLayer>();

    for (ILspView view : views) {
      List<ILspLayer> layers = Framework.getInstance().getLayersWithID("layer.id.shapes", view);
      for (ILspLayer layer : layers) {
        if (view.containsLayer(layer)) {
          view2layer.put(view, layer);
        }
      }
    }
    return view2layer;
  }

  private static class ToggleExtrudeAction extends ALcdAction {

    private boolean fExtrude;

    public ToggleExtrudeAction() {
      fExtrude = false;
    }

    public boolean isExtrude() {
      return fExtrude;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      fExtrude = !fExtrude;
      firePropertyChange("extrude", !fExtrude, fExtrude);
    }
  }

  private class CreateAction extends ALcdAction implements PropertyChangeListener {

    private final Map<ILspView, ILspLayer> fView2Layer;
    private final LonLatCreateControllerModel.Type f2DType;
    private final ButtonGroup fButtonGroup;
    private boolean fExtruded = false;
    private boolean fActive = false;
    private final ILcdIcon fIcon;
    private LonLatCreateControllerModel.Type fActiveType;

    public CreateAction(Map<ILspView, ILspLayer> aView2Layer,
                        ButtonGroup aButtonGroup,
                        ILcdIcon aIcon,
                        LonLatCreateControllerModel.Type a2DType,
                        String aToolTipText,
                        ToggleExtrudeAction aToggleExtrudeAction) {
      fView2Layer = aView2Layer;
      fButtonGroup = aButtonGroup;
      fIcon = aIcon;
      f2DType = a2DType;
      setShortDescription(aToolTipText);
      aToggleExtrudeAction.addPropertyChangeListener(this);
      updateActiveType(aToggleExtrudeAction.isExtrude());
    }

    private void updateActiveType(boolean aExtrude) {
      fActiveType = f2DType;
      fExtruded = aExtrude;
      setEnabled(fActiveType != null);
      if (fActive) {
        actionPerformed(null);
      }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
      updateActiveType((Boolean) evt.getNewValue());
      firePropertyChange("enabled", !isEnabled(), isEnabled());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      fActive = true;
      for (ILspView view : fView2Layer.keySet()) {
        ILspLayer layer = fView2Layer.get(view);
        if (layer instanceof ILspInteractivePaintableLayer) {
          ILspController controller = createCreationController(
              (ILspInteractivePaintableLayer) layer,
              fActiveType,
              fIcon);
          view.setController(controller);
        }
      }
    }

    private ILspController createCreationController(ILspInteractivePaintableLayer aLayer, LonLatCreateControllerModel.Type aType, ILcdIcon aIcon) {
      // Each creation controller must have a controller model (in this case a lon-lat creation model)
      LonLatCreateControllerModel cm = new LonLatCreateControllerModel(aType, aLayer);
      cm.setCreateExtrudedShape(fExtruded);
      // Create and initialize creation controller
      TLspCreateController createController = null;
      Framework app = Framework.getInstance();
      boolean isTouchEnabled = Boolean.parseBoolean(app.getProperty("controllers.touch.enabled", "false"));

      if (isTouchEnabled) {
        FrameworkContext frameworkContext = app.getFrameworkContext();

        if (frameworkContext != null) {
          List<ILspView> views = frameworkContext.getViews();
          if (views != null && !views.isEmpty()) {
            ILspView view = views.get(0);
            if (view instanceof ILspAWTView) {
              createController = new TouchCreateController(fCancelButton, fFinishButton, cm);
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
        createController.appendController(ControllerFactory.createNavigationController());
      }

      createController.setShortDescription(aType.toString());
      createController.setIcon(aIcon);
      createController.setActionToTriggerAfterCommit(
          new ALcdAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
              Framework app = Framework.getInstance();
              Map<ILspView, ILspController> view2controller = (Map<ILspView, ILspController>) app
                  .getSharedValue("view.default.controllers");
              for (ILspView view : view2controller.keySet()) {
                view.setController(view2controller.get(view));
              }
              fButtonGroup.clearSelection();
              fActive = false;
            }
          }
      );

      return createController;
    }
  }

  private AbstractButton createCreationButton(ButtonGroup aButtonGroup,
                                              ToggleExtrudeAction aExtrudeAction,
                                              final Map<ILspView, ILspLayer> aView2Layer,
                                              final LonLatCreateControllerModel.Type a2DType,
                                              final ILcdIcon aIcon,
                                              String aToolTipText) {

    final AbstractButton button = new JToggleButton() {
      @Override
      public JToolTip createToolTip() {
        JToolTip toolTip = super.createToolTip();
        toolTip.setOpaque(true);
        return toolTip;
      }

      @Override
      public Dimension getPreferredSize() {
        return fPreferredButtonSize;
      }
    };
    final ALcdAction createAction = new CreateAction(aView2Layer, aButtonGroup, aIcon, a2DType, aToolTipText, aExtrudeAction);
    TLcdSWAction swing_action = new TLcdSWAction(createAction);
    ToolTipManager.sharedInstance().registerComponent(button);
    ToolTipManager.sharedInstance().setEnabled(false);

    button.addActionListener(swing_action);
    button.setIcon(new TLcdSWIcon(aIcon));
    button.setDisabledIcon(new TLcdSWIcon(new TLcdGreyIcon(aIcon)));
    button.setToolTipText(aToolTipText);
    button.setEnabled(swing_action.isEnabled());
    button.addMouseListener(this);

    createAction.addPropertyChangeListener(new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getSource() == createAction) {
          button.setEnabled(createAction.isEnabled());
        }
      }
    });

    aButtonGroup.add(button);
    return button;
  }

  private static class StyleMediator {
    //Sliders and so on.
    private HueSlider fFillColorSlider;
    private HueSlider fLineColorSlider;
    private JSlider fLineWidthSlider;

    private List<StyleEditorModel> fStyleEditorModels;
    private final WeakReference<EditingPanelFactory> fEditingPanelFactoryWeakReference;
    private SimpleSelectionChangeListener fSelectionChangeListener = new SimpleSelectionChangeListener(this);
    private boolean fListenersInitialized = false;

    private StyleMediator(EditingPanelFactory aEditingPanelFactory, HueSlider aFillColorSlider, HueSlider aLineColorSlider, JSlider aLineWidthSlider) {
      fEditingPanelFactoryWeakReference = new WeakReference<EditingPanelFactory>(aEditingPanelFactory);
      fFillColorSlider = aFillColorSlider;
      fLineColorSlider = aLineColorSlider;
      fLineWidthSlider = aLineWidthSlider;
      fFillColorSlider.addPropertyChangeListener("value", new PropertyChangeListener() {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
          if (fStyleEditorModels != null) {
            for (StyleEditorModel styleEditorModel : fStyleEditorModels) {
              Color oldFillColor = styleEditorModel.getFillColor();
              if (oldFillColor != null) {
                Color newColor = applyHue(oldFillColor, fFillColorSlider.getValue());
                styleEditorModel.setFillColor(newColor);
                styleEditorModel.apply();
              }
            }
          }
        }
      });
      fLineColorSlider.addPropertyChangeListener("value", new PropertyChangeListener() {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
          if (fStyleEditorModels != null) {
            for (StyleEditorModel styleEditorModel : fStyleEditorModels) {
              Color oldFillColor = styleEditorModel.getLineColor();
              if (oldFillColor != null) {
                Color newColor = applyHue(oldFillColor, fLineColorSlider.getValue());
                styleEditorModel.setLineColor(newColor);
                styleEditorModel.apply();
              }
            }
          }
        }
      });
      fLineWidthSlider.addChangeListener(new ChangeListener() {
        @Override
        public void stateChanged(ChangeEvent e) {
          if (fStyleEditorModels != null) {
            for (StyleEditorModel styleEditorModel : fStyleEditorModels) {
              Double oldLineWidth = styleEditorModel.getLineWidth();
              if (oldLineWidth != null) {
                styleEditorModel.setLineWidth((double) fLineWidthSlider.getValue() / 100.0 + 1.0);
                styleEditorModel.apply();
              }
            }
          }
        }
      });

      refreshStyleEditorModels();
      lazyInitListeners();
      refreshContent();
    }

    private void lazyInitListeners() {
      if (!fListenersInitialized && fStyleEditorModels != null) {
        for (StyleEditorModel styleEditorModel : fStyleEditorModels) {
          Iterator<ILspLayer> layersIterator = styleEditorModel.getLayersIterator();
          while (layersIterator.hasNext()) {
            ILspLayer layer = layersIterator.next();
            layer.addSelectionListener(fSelectionChangeListener);
          }
        }
        fListenersInitialized = true;
      }
    }

    private void refreshStyleEditorModels() {
      EditingPanelFactory editingPanelFactory = fEditingPanelFactoryWeakReference.get();
      HashMap<ILspView, ILspLayer> viewLayerMap = editingPanelFactory.getViewLayerMap();
      List<StyleEditorModel> styleEditorModels = new ArrayList<StyleEditorModel>();
      if (viewLayerMap != null && !viewLayerMap.isEmpty()) {
        Set<ILspView> views = viewLayerMap.keySet();
        for (ILspView view : views) {
          ILspLayer layer = viewLayerMap.get(view);
          if (layer instanceof ILspInteractivePaintableLayer) {
            ILspStyler styler = ((ILspInteractivePaintableLayer) layer).getStyler(TLspPaintRepresentationState.REGULAR_BODY);
            if (styler instanceof TLspEditableStyler) {
              TLspEditableStyler editableStyler = (TLspEditableStyler) styler;
              StyleEditorModel styleEditorModel = new StyleEditorModel(view, editableStyler, Collections.singletonList(viewLayerMap.get(view)));
              styleEditorModels.add(styleEditorModel);
            }
          }
        }
        if (!styleEditorModels.isEmpty()) {
          fStyleEditorModels = styleEditorModels;
        } else {
          fStyleEditorModels = null;
        }
      } else {
        fStyleEditorModels = null;
      }
    }

    public void refreshContent() {
      refreshStyleEditorModels();
      if (fStyleEditorModels == null || fStyleEditorModels.isEmpty() || fStyleEditorModels.get(0).getFillColor() == null) {
        fFillColorSlider.setEnabled(false);
      } else {
        fFillColorSlider.setEnabled(true);
        Color fillColor = fStyleEditorModels.get(0).getFillColor();
        float[] hsb = Color.RGBtoHSB(fillColor.getRed(), fillColor.getGreen(), fillColor.getBlue(), null);
        fFillColorSlider.setValue(((int) (hsb[0] * 255f)));
      }

      if (fStyleEditorModels == null || fStyleEditorModels.isEmpty() || fStyleEditorModels.get(0).getLineColor() == null) {
        fLineColorSlider.setEnabled(false);
      } else {
        fLineColorSlider.setEnabled(true);
        Color lineColor = fStyleEditorModels.get(0).getLineColor();
        float[] hsb = Color.RGBtoHSB(lineColor.getRed(), lineColor.getGreen(), lineColor.getBlue(), null);
        fLineColorSlider.setValue(((int) (hsb[0] * 255f)));
      }

      if (fStyleEditorModels == null || fStyleEditorModels.isEmpty() || fStyleEditorModels.get(0).getLineWidth() == null) {
        fLineWidthSlider.setEnabled(false);
      } else {
        fLineWidthSlider.setEnabled(true);
        Double lineWidth = fStyleEditorModels.get(0).getLineWidth();
        fLineWidthSlider.setValue((lineWidth.intValue() - 1) * 300);
      }
    }

    private Color applyHue(Color aColor, int aHue) {
      float[] hsb = Color.RGBtoHSB(aColor.getRed(), aColor.getGreen(), aColor.getBlue(), null);
      hsb[0] = ((float) (aHue + 1)) / 256f;
      int newFillColor = Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]);
      Color convertedColor = new Color(newFillColor);
      return new Color(convertedColor.getRed(), convertedColor.getGreen(), convertedColor.getBlue(), aColor.getAlpha());
    }
  }

  private static class SimpleSelectionChangeListener implements ILcdSelectionListener {

    private StyleMediator fSelectionBasedPanel;

    public SimpleSelectionChangeListener(StyleMediator aSelectionBasedPanel) {
      fSelectionBasedPanel = aSelectionBasedPanel;
    }

    @Override
    public void selectionChanged(TLcdSelectionChangedEvent aSelectionEvent) {
      fSelectionBasedPanel.refreshContent();
    }
  }

}
