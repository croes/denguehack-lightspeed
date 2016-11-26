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
package samples.lightspeed.demo.application.data.imageprocessing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseMotionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.luciad.imaging.ALcdBasicImage;
import com.luciad.imaging.ALcdImage;
import com.luciad.imaging.ALcdMultilevelImage;
import com.luciad.imaging.ALcdMultilevelImageMosaic;
import com.luciad.imaging.operator.ALcdImageOperatorChain;
import com.luciad.imaging.operator.util.TLcdLookupTable;
import com.luciad.model.ILcdModel;
import com.luciad.util.ILcdDisposable;
import com.luciad.util.TLcdColorMap;
import com.luciad.util.TLcdInterval;
import com.luciad.view.ILcdLayer;
import com.luciad.view.lightspeed.ILspAWTView;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.controller.ALspController;
import com.luciad.view.lightspeed.controller.ILspController;
import com.luciad.view.lightspeed.controller.visualinspection.TLspFlickerController;
import com.luciad.view.lightspeed.controller.visualinspection.TLspPortholeController;
import com.luciad.view.lightspeed.controller.visualinspection.TLspSwipeController;
import com.luciad.view.lightspeed.layer.ILspEditableStyledLayer;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.TLspPaintRepresentationState;
import com.luciad.view.lightspeed.services.asynchronous.ILspTaskExecutor;
import com.luciad.view.lightspeed.style.styler.ILspStyler;

import samples.common.NoopStringTranslator;
import samples.common.lightspeed.visualinspection.SwipeController;
import samples.lightspeed.demo.framework.application.Framework;
import samples.lightspeed.demo.framework.gui.TooltipMouseListener;
import samples.lightspeed.imaging.multispectral.MultispectralOperatorStyler;
import samples.lightspeed.imaging.multispectral.OperatorModel;
import samples.lightspeed.imaging.multispectral.curves.CurvesPanel;
import samples.lightspeed.imaging.multispectral.general.GeneralOperationPanel;

/**
 * Creates the menu panel for the imaging theme and adds the necessary listeners to the view.
 */
class ImageCustomizerPanel implements ILcdDisposable {

  private static final boolean TOUCH_ENABLED = Boolean.parseBoolean(Framework.getInstance()
                                                                             .getProperty("controllers.touch.enabled", "false"));

  private final JPanel fPanel;
  private ILcdModel fModel1;
  private ILcdModel fModel2;
  private ILcdModel fLandSat7Model;
  private OperatorModelExtended fOperatorModelExtended;
  private ExtendedOperator fImageOperatorChain;
  private OperatorModelExtended fLandSat7OperatorModelExtended;
  private ExtendedOperator fLandSat7ImageOperatorChain;
  private CompositeExtendedOperatorModel fCompositeExtendedOperatorModel;

  private List<ILspAWTView> fViews;
  private Map<ILspAWTView, ILspController> fView2Controller;
  private Map<ILspAWTView, TLspFlickerController> fView2FlickerController;
  private Map<ILspAWTView, TLspSwipeController> fView2SwipeController;
  private final TransparentTitledPanel fCurvesPanel;
  private final CurvesPanel fCurvesPanelHistograms;
  private List<ClassificationTooltipLogic> fClassificationTooltipLogic;
  private ESelectionState fSelectionState = ESelectionState.FLICKER;
  private final ALcdBasicImage fImage1, fImage2;
  private final ALcdImage fLandSat7Image;

  private final Map<ILspView, List<Collection<? extends ILspLayer>>> fLayerCombinations = new HashMap<>();
  private JToggleButton image2000Left;
  private JToggleButton image2012Left;
  private JToggleButton image2003Left;

  private JToggleButton image2000Right;
  private JToggleButton image2012Right;
  private JToggleButton image2003Right;

  private List<JToggleButton> fAllButtons = new ArrayList<>();
  private Map<JToggleButton, JToggleButton> fLeftToRightButtons = new HashMap<>();
  private Map<JToggleButton, JToggleButton> fRightToLeftButtons = new HashMap<>();

  private final Map<Object, Integer> fLayerIndexMap = new HashMap<>();

  private JRadioButton fSideBySideButton;
  private JRadioButton fFlickerButton;
  private JRadioButton fPortHoleButton;

  private static final boolean IS_MAC = System.getProperty("os.name").toLowerCase().startsWith("mac");

  public ImageCustomizerPanel(List<ILspAWTView> aViews, ILcdModel aModel1, ILcdModel aModel2, ILcdModel aModel3) {
    fModel1 = aModel1;
    fModel2 = aModel2;
    fLandSat7Model = aModel3;
    fViews = aViews;

    fImage1 = (ALcdBasicImage) getHighResImage(fModel1);
    fImage2 = (ALcdBasicImage) getHighResImage(fModel2);
    fLandSat7Image = fLandSat7Model != null ? getHighResImage(fLandSat7Model) : null;

    ILspTaskExecutor taskExecutor = aViews.get(0).getServices().getTaskExecutor();
    fOperatorModelExtended = new OperatorModelExtended(fModel1, fImage1, taskExecutor);
    if (fLandSat7Model != null) {
      fLandSat7OperatorModelExtended = new OperatorModelExtended(fLandSat7Model, fLandSat7Image, taskExecutor) {
        @Override
        public void setSelectedBands(int[] aSelectedBands) {
          for (int index = 0; index < aSelectedBands.length; ++index) {
            if (aSelectedBands[index] == 6) {
              aSelectedBands[index] = 7;
            }
          }
          super.setSelectedBands(aSelectedBands);
        }

        @Override
        protected String[] createBandNames(int aNbBands) {
          String[] bandNames = new String[aNbBands + 1];
          for (int i = 0; i < bandNames.length; i++) {
            bandNames[i] = "Band " + Integer.toString(i + 1);
          }
          //landsat has 8 bands but we need only 7 bands
          //and we need the 8th band iso 7th so we swap
          //this hacks the naming for the demo
          bandNames[7] = "Band 7";

          return bandNames;
        }
      };
      fLandSat7ImageOperatorChain = new ExtendedOperator(fLandSat7OperatorModelExtended, createLandSat7ColorLookupTable());
      fCompositeExtendedOperatorModel = new CompositeExtendedOperatorModel(fImage1, taskExecutor, fOperatorModelExtended, fLandSat7OperatorModelExtended);
    } else {
      fCompositeExtendedOperatorModel = new CompositeExtendedOperatorModel(fImage1, taskExecutor, fOperatorModelExtended);
    }
    fImageOperatorChain = new ExtendedOperator(fOperatorModelExtended);
    fView2Controller = new HashMap<>();

    // Initialize swipe and flicker controllers
    fView2FlickerController = new HashMap<>();
    fView2SwipeController = new HashMap<>();
    fLayerCombinations.clear();
    for (ILspAWTView view : fViews) {
      List<Collection<? extends ILspLayer>> layers = new ArrayList<>();
      fLayerCombinations.put(view, layers);
      TLspFlickerController flickerController = new TLspFlickerController();
      ILspLayer layer1 = (ILspLayer) view.layerOf(fModel1);
      ILspLayer layer2 = (ILspLayer) view.layerOf(fModel2);
      layers.addAll(Arrays.asList(Collections.singleton(layer1), Collections.singleton(layer2), Arrays.asList(layer1, layer2), Arrays.asList(layer2, layer1)));
      //add landsat7 combinations if exists.
      if (fLandSat7Model != null) {
        ILspLayer landSatLayer = (ILspLayer) view.layerOf(fLandSat7Model);
        layers.addAll(Arrays.asList(Collections.singleton(landSatLayer), Arrays.asList(landSatLayer, layer1), Arrays.asList(landSatLayer, layer2), Arrays.asList(layer1, landSatLayer), Arrays.asList(layer2, landSatLayer)));
      }
      flickerController.setLayers(layers);
      flickerController.startInteraction(view);
      fView2FlickerController.put(view, flickerController);
    }

    fClassificationTooltipLogic = new ArrayList<>();

    // Create panel
    DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout("min(150dlu;pref)"));
    builder.border(BorderFactory.createEmptyBorder(10, 10, 0, 10));
    builder.opaque(false);

    //  Add panels
    GeneralOperationPanel generalOperationPanel = new GeneralOperationPanel(fCompositeExtendedOperatorModel);
    generalOperationPanel.setOpaque(false);
    BandSelectPanel bandSelectPanel = new BandSelectPanel(fCompositeExtendedOperatorModel);
    bandSelectPanel.setOpaque(false);
    fCurvesPanelHistograms = new CurvesPanel(fCompositeExtendedOperatorModel, 160);
    fCurvesPanelHistograms.setOpaque(false);
    JPanel imageComparisonPanel = createImageComparisonPanel();
    imageComparisonPanel.setOpaque(false);

    builder.append(TransparentTitledPanel.createTitledPanel("General", generalOperationPanel));
    builder.nextLine();

    builder.append(TransparentTitledPanel.createTitledPanel("Band selection", bandSelectPanel));
    builder.nextLine();

    fCurvesPanel = TransparentTitledPanel.createTitledPanel("Curves", fCurvesPanelHistograms);
    builder.append(fCurvesPanel);
    builder.nextLine();

    builder.append(TransparentTitledPanel.createTitledPanel("Image comparison", imageComparisonPanel));

    fPanel = builder.getPanel();

    // Setup layout
    fPanel.setSize(fPanel.getLayout().preferredLayoutSize(fPanel));
    fOperatorModelExtended.addChangeListener(new OperatorModelChangeListener(fViews, fModel1, fImageOperatorChain));
    fOperatorModelExtended.addChangeListener(new OperatorModelChangeListener(fViews, fModel2, fImageOperatorChain));
    if (fLandSat7Model != null) {
      fLandSat7OperatorModelExtended.addChangeListener(new OperatorModelChangeListener(fViews, fLandSat7Model, fLandSat7ImageOperatorChain));
    }
  }

   @Override
  public void dispose() {
    fCurvesPanelHistograms.dispose();
    cleanClassificationTooltipLogic();
  }

  //setup for when imaging theme is activated
  public void activate() {
    addClassificationToolTips(fViews, fClassificationTooltipLogic);

    for (ILspAWTView view : fViews) {
      ILcdLayer layer = view.layerOf(fModel1);
      ILspStyler styler = ((ILspEditableStyledLayer) layer).getStyler(TLspPaintRepresentationState.REGULAR_BODY);
      if (styler instanceof MultispectralOperatorStyler) {
        ((MultispectralOperatorStyler) styler).setOperatorModel(fOperatorModelExtended);
      }
      layer = view.layerOf(fModel2);
      styler = ((ILspEditableStyledLayer) layer).getStyler(TLspPaintRepresentationState.REGULAR_BODY);
      if (styler instanceof MultispectralOperatorStyler) {
        ((MultispectralOperatorStyler) styler).setOperatorModel(fOperatorModelExtended);
      }

      if (fLandSat7Model != null) {
        layer = view.layerOf(fLandSat7Model);
        styler = ((ILspEditableStyledLayer) layer).getStyler(TLspPaintRepresentationState.REGULAR_BODY);
        if (styler instanceof MultispectralOperatorStyler) {
          ((MultispectralOperatorStyler) styler).setOperatorModel(fLandSat7OperatorModelExtended);
        }
      }
    }

    for (ILspView view : fViews) {
      ILcdLayer layer = view.layerOf(fModel1);
      setImageOperators(layer, fImageOperatorChain);

      layer = view.layerOf(fModel2);
      setImageOperators(layer, fImageOperatorChain);

      if (fLandSat7Model != null) {
        layer = view.layerOf(fLandSat7Model);
        setImageOperators(layer, fLandSat7ImageOperatorChain);
      }
    }

    //restore the state
    if (fSelectionState == ESelectionState.SWIPE || fSelectionState == ESelectionState.PORTHOLE) {
      setRightButtonsEnabled(true);
      activateMultiSelectionController(fSelectionState);
    } else {
      setRightButtonsEnabled(false);
      activateFlicker();
    }
  }

  /**
   * Remove mouse listener when deactivated
   */
  public void deactivate() {
    for (ILspAWTView view : fViews) {
      MouseMotionListener[] mouseMotionListeners = view.getHostComponent().getMouseMotionListeners();
      for (MouseMotionListener mouseMotionListener : mouseMotionListeners) {
        if (mouseMotionListener instanceof TooltipMouseListener) {
          TooltipMouseListener listener = (TooltipMouseListener) mouseMotionListener;
          listener.showTooltip(null, null, 0, 0);
          view.getHostComponent().removeMouseMotionListener(mouseMotionListener);
        }
      }
    }

    cleanClassificationTooltipLogic();
    removeControllers();
  }

  private void cleanClassificationTooltipLogic() {
    for (ClassificationTooltipLogic aFClassificationTooltipLogic : fClassificationTooltipLogic) {
      aFClassificationTooltipLogic.dispose();
    }
    fClassificationTooltipLogic.clear();
  }

  private void addClassificationToolTips(List<ILspAWTView> aViews, List<ClassificationTooltipLogic> aToolTips) {
    for (ILspAWTView view : aViews) {
      TLspSwipeController swipeController = fView2SwipeController.get(view);
      TLspFlickerController flickerController = fView2FlickerController.get(view);
      ClassificationTooltipLogic tooltip = new ClassificationTooltipLogic(view,
                                                                          swipeController,
                                                                          flickerController,
                                                                          fModel1, fImage1, fModel2, fImage2, fLandSat7Model, fLandSat7Image);

      aToolTips.add(tooltip);

      view.getHostComponent().addMouseMotionListener(
          new TooltipMouseListener(view,
                                   Arrays.asList((ILspLayer) view.layerOf(fModel1), (ILspLayer) view.layerOf(fModel2)),
                                   Collections.<TooltipMouseListener.TooltipLogic>singletonList(tooltip))
      );
    }
  }

  private static ALcdImage getHighResImage(ILcdModel aModel) {
    Enumeration elements = aModel.elements();
    Object o = elements.nextElement();
    if (o instanceof ALcdMultilevelImage) {
      ALcdMultilevelImage multilevelImage = (ALcdMultilevelImage) o;
      return multilevelImage.getLevel(multilevelImage.getConfiguration().getNumberOfLevels() - 1);
    } else {
      ALcdMultilevelImageMosaic multilevelImage = (ALcdMultilevelImageMosaic) ALcdImage.fromDomainObject(o);
      return multilevelImage.getLevel(multilevelImage.getConfiguration().getNumberOfLevels() - 1);
    }
  }

  //creates the panel that allows for image selection and activating the swipe operator
  private JPanel createImageComparisonPanel() {
    JPanel panel = new JPanel(new BorderLayout(10, 0));

    fSideBySideButton = new JRadioButton("Swipe");
    fSideBySideButton.setToolTipText("<html>Compare both images at the same time by moving the mouse cursor over the image.<br>" +
                                     "The swipe line will follow your mouse cursor.<html>");

    fFlickerButton = new JRadioButton("Off");
    fFlickerButton.setToolTipText("<html>Change the displayed image quickly by selecting the related image on the left.</html>");
    fFlickerButton.setSelected(true);

    fPortHoleButton = new JRadioButton("Porthole");
    fPortHoleButton.setToolTipText("<html>Compare two images by exposing one image through a porthole in the other set.</html>");

    fSideBySideButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        if (fSideBySideButton.isSelected()) {
          fSelectionState = ESelectionState.SWIPE;
          setRightButtonsEnabled(true);
          prepareMultiImageSelection();
          activateMultiSelectionController(fSelectionState);
        }
      }
    });

    fPortHoleButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        if (fPortHoleButton.isSelected()) {
          fSelectionState = ESelectionState.PORTHOLE;
          setRightButtonsEnabled(true);
          prepareMultiImageSelection();
          activateMultiSelectionController(fSelectionState);
        }
      }
    });

    fFlickerButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        if (fFlickerButton.isSelected()) {
          fSelectionState = ESelectionState.FLICKER;
          setRightButtonsEnabled(false);
          activateFlicker();
        }
      }
    });

    JPanel operatorsPanel = new JPanel();
    operatorsPanel.setOpaque(false);
    operatorsPanel.setLayout(new BoxLayout(operatorsPanel, BoxLayout.Y_AXIS));
    operatorsPanel.add(fFlickerButton);
    operatorsPanel.add(fSideBySideButton);
    operatorsPanel.add(fPortHoleButton);

    ButtonGroup opSelectionGroup = new ButtonGroup();
    opSelectionGroup.add(fSideBySideButton);
    opSelectionGroup.add(fFlickerButton);
    opSelectionGroup.add(fPortHoleButton);

    JToolBar imagesLeftToolbar = createImagesLeftToolbar();
    JToolBar imagesRightToolbar = createImagesRightToolBar();

    JToolBar mainToolBar = new JToolBar();
    mainToolBar.setFloatable(false);
    mainToolBar.setBorderPainted(false);

    mainToolBar.add(imagesLeftToolbar);
    mainToolBar.add(Box.createHorizontalGlue());
    mainToolBar.add(operatorsPanel);
    mainToolBar.add(Box.createHorizontalGlue());
    mainToolBar.add(imagesRightToolbar);

    panel.add(mainToolBar, BorderLayout.CENTER);

    registerLayerIndexes();

    return panel;
  }

  private void registerLayerIndexes() {
    fAllButtons.add(image2000Right);
    fAllButtons.add(image2003Right);
    fAllButtons.add(image2000Left);
    fAllButtons.add(image2003Left);

    fLayerIndexMap.put(image2003Left, 0);
    fLayerIndexMap.put(image2000Left, 1);
    fLayerIndexMap.put(Arrays.asList(image2003Left, image2000Right), 2);
    fLayerIndexMap.put(Arrays.asList(image2000Left, image2003Right), 3);

    if (fLandSat7Image != null) {
      fAllButtons.add(image2012Right);
      fAllButtons.add(image2012Left);

      fLayerIndexMap.put(image2012Left, 4);
      fLayerIndexMap.put(Arrays.asList(image2012Left, image2003Right), 5);
      fLayerIndexMap.put(Arrays.asList(image2012Left, image2000Right), 6);
      fLayerIndexMap.put(Arrays.asList(image2003Left, image2012Right), 7);
      fLayerIndexMap.put(Arrays.asList(image2000Left, image2012Right), 8);

      fLeftToRightButtons.put(image2012Left, image2012Right);
      fRightToLeftButtons.put(image2012Right, image2012Left);
    }

    fLeftToRightButtons.put(image2000Left, image2000Right);
    fLeftToRightButtons.put(image2003Left, image2003Right);
    fRightToLeftButtons.put(image2000Right, image2000Left);
    fRightToLeftButtons.put(image2003Right, image2003Left);

  }

  private JToolBar createImagesRightToolBar() {
    JToolBar imagesToolBar = new JToolBar();
    imagesToolBar.setFloatable(false);
    imagesToolBar.setBorderPainted(false);
    ButtonGroup imageSelectionGroupRight = new ButtonGroup();

    image2000Right = new ImageSelectionButton("2000", fOperatorModelExtended);
    image2000Right.setToolTipText("Show imagery from Landsat7 archive captured at the beginning of June in 2000.");
    image2000Right.setSelected(false);

    image2003Right = new ImageSelectionButton("2003", fOperatorModelExtended);
    image2003Right.setToolTipText("Show imagery from Landsat7 archive captured at the end of May in 2003.");
    image2003Right.setSelected(false);

    if (fLandSat7Image != null) {
      image2012Right = new ImageSelectionButton("2012", fOperatorModelExtended);
      image2012Right.setToolTipText("Show imagery from Landsat7 archive captured at 2012.");
      image2012Right.setSelected(false);
      imageSelectionGroupRight.add(image2012Right);
    }

    imageSelectionGroupRight.add(image2000Right);
    imageSelectionGroupRight.add(image2003Right);

    imagesToolBar.add(image2000Right);
    imagesToolBar.add(image2003Right);
    if (fLandSat7Model != null) {
      imagesToolBar.add(image2012Right);
    }

    return imagesToolBar;
  }

  private JToolBar createImagesLeftToolbar() {
    ButtonGroup imageSelectionGroupLeft = new ButtonGroup();

    JToolBar imagesToolBar = new JToolBar();
    imagesToolBar.setFloatable(false);
    imagesToolBar.setBorderPainted(false);

    image2000Left = new ImageSelectionButton("2000", fOperatorModelExtended);
    image2000Left.setToolTipText("Show imagery from Landsat7 archive captured at the beginning of June in 2000.");
    image2000Left.setSelected(false);

    image2003Left = new ImageSelectionButton("2003", fOperatorModelExtended);
    image2003Left.setToolTipText("Show imagery from Landsat7 archive captured at the end of May in 2003.");
    image2003Left.setSelected(true);

    if (fLandSat7Image != null) {
      image2012Left = new ImageSelectionButton("2012", fLandSat7OperatorModelExtended);
      image2012Left.setToolTipText("Show imagery from Landsat7 archive captured at 2012.");
      image2012Left.setSelected(false);
      imageSelectionGroupLeft.add(image2012Left);
    }

    //add buttons to the selection groups
    imageSelectionGroupLeft.add(image2000Left);
    imageSelectionGroupLeft.add(image2003Left);

    //add buttons to the toolbar
    imagesToolBar.add(image2000Left);
    imagesToolBar.add(image2003Left);
    if (fLandSat7Model != null) {
      imagesToolBar.add(image2012Left);
    }
    return imagesToolBar;
  }

  private void setRightButtonsEnabled(boolean aEnabled) {
    //enable all buttons first
    for (JToggleButton button : fAllButtons) {
      button.setEnabled(true);
    }
    //disable buttons and remove from the group to unselect them all
    if (!aEnabled) {
      image2000Right.getModel().setGroup(null);
      image2003Right.getModel().setGroup(null);
      if (fLandSat7Model != null) {
        image2012Right.getModel().setGroup(null);
      }
      image2000Right.setSelected(false);
      image2003Right.setSelected(false);
      if (fLandSat7Model != null) {
        image2012Right.setSelected(false);
      }
      //since they are all unselected, add into a new group
      ButtonGroup selectionGroup = new ButtonGroup();
      selectionGroup.add(image2000Right);
      selectionGroup.add(image2003Right);
      if (fLandSat7Model != null) {
        selectionGroup.add(image2012Right);
      }
    }

    image2000Right.setEnabled(aEnabled);
    image2003Right.setEnabled(aEnabled);

    if (fLandSat7Model != null) {
      image2012Right.setEnabled(aEnabled);
    }
  }

  private void imageSelectionChanged(OperatorModelExtended aOperatorModelExtended) {
    //update curves panel when image selection changed
    fCompositeExtendedOperatorModel.setActiveOperatorModel(aOperatorModelExtended);
    fCurvesPanelHistograms.updateEditingViews();

    if (fSelectionState == ESelectionState.FLICKER) {
      activateFlicker();
    } else {
      activateMultiSelectionController(fSelectionState);
    }
  }

  /**
   *
   * @return the index of the selected layer pair in flicker controller
   */
  private int getSelectedLayerIndex() {
    JToggleButton leftButton = getLeftSelectedButton();
    JToggleButton rightButton = getRightSelectedButton();
    if (rightButton != null) {
      return fLayerIndexMap.get(Arrays.asList(leftButton, rightButton));
    } else {
      return fLayerIndexMap.get(leftButton);
    }
  }

  private JToggleButton getLeftSelectedButton() {
    return image2000Left.isSelected() ? image2000Left : image2003Left.isSelected() ? image2003Left : image2012Left != null && image2012Left.isSelected() ? image2012Left : null;
  }

  private JToggleButton getRightSelectedButton() {
    return image2000Right.isSelected() ? image2000Right : image2003Right.isSelected() ? image2003Right : image2012Right != null && image2012Right.isSelected() ? image2012Right : null;
  }

  private void selectNextButton(JToggleButton aButton) {
    //button is on left toolbar
    if (aButton == getLeftSelectedButton()) {
      if (aButton == image2000Left) {
        image2003Left.setSelected(true);
      } else {
        image2000Left.setSelected(true);
      }
    } else if (aButton == getRightSelectedButton()) {
      if (aButton == image2000Right) {
        image2003Right.setSelected(true);
      } else {
        image2000Right.setSelected(true);
      }
    }
  }

  private void activateFlicker() {
    removeControllers();
    int layerIndex = getSelectedLayerIndex();
    for (ILspAWTView view : fViews) {
      fView2FlickerController.get(view).setVisibleIndex(layerIndex);
      repaintView(view);
    }
  }

  //activates either swipe or porthole controller
  private void activateMultiSelectionController(ESelectionState aSelectionState) {
    boolean isSwipeController = aSelectionState == ESelectionState.SWIPE;
    boolean isPortholeController = aSelectionState == ESelectionState.PORTHOLE;
    Point lastSwipeLocation = removeControllers();
    int index = getSelectedLayerIndex();
    for (ILspAWTView view : fViews) {
      Collection<? extends ILspLayer> layersToSwipe = fLayerCombinations.get(view).get(index);
      //activate selected layers
      fView2FlickerController.get(view).setVisibleIndex(index);
      ILspController currentController = view.getController();

      view.setController(null);
      Iterator<? extends ILspLayer> iterator = layersToSwipe.iterator();
      ALspController controller;
      if (isSwipeController) {
        SwipeController swipeController = new SwipeController(view, view.getController(), new NoopStringTranslator());
        swipeController.setLayers(Collections.singleton(iterator.next()), Collections.singleton(iterator.next()));
        swipeController.setShowLayerSelectionDialog(false);
        fView2SwipeController.put(view, swipeController);
        controller = swipeController;
        for (ClassificationTooltipLogic aToolTipLogic : fClassificationTooltipLogic) {
          if (aToolTipLogic.getView() == view) {
            aToolTipLogic.setSwipeController(swipeController);
          }
        }
      } else {
        TLspPortholeController portholeController = new TLspPortholeController();
        portholeController.setLayers(Collections.singleton(iterator.next()), Collections.singleton(iterator.next()));
        controller = portholeController;
      }
      if ((isSwipeController && !(currentController instanceof TLspSwipeController))
          || (isPortholeController && !(currentController instanceof TLspPortholeController))) {
        //store the old controller
        fView2Controller.put(view, currentController);
        //don't use the pan controller if porthole is active on touch screen
        if (!isPortholeController || !TOUCH_ENABLED) {
          controller.appendController(currentController);
        }
      }
      view.setController(controller);
      if (null != lastSwipeLocation && controller instanceof TLspSwipeController) {
        ((TLspSwipeController) controller).setSwipeLineLocation(lastSwipeLocation.getX(), lastSwipeLocation.getY());
      }
      repaintView(view);
    }
  }

  /**
   * Prepares the image buttons for multiselection
   * selects a default image on the right side if there is not a selection already
   */
  private void prepareMultiImageSelection() {
    JToggleButton rightButton = fLeftToRightButtons.get(getLeftSelectedButton());
    rightButton.setSelected(false);
    if (getRightSelectedButton() == null) {
      //make a default selection on the right side
      if (rightButton != image2000Right) {
        image2000Right.setSelected(true);
      } else {
        image2003Right.setSelected(true);
      }
    } else {
      JToggleButton leftButton = fRightToLeftButtons.get(getRightSelectedButton());
      leftButton.setSelected(false);
    }
  }

  private Point removeControllers() {
    for (ILspAWTView view : fViews) {
      ILspController controller = fView2Controller.remove(view);
      if (null != controller) {
        view.setController(controller);
        TLspSwipeController swipeController = fView2SwipeController.remove(view);
        if (swipeController != null) {
          return swipeController.getSwipeLineLocation();
        }
      }
    }
    return null;
  }

  private static void repaintView(ILspAWTView aView) {
    if (IS_MAC) {
      aView.getHostComponent().repaint();
    }
  }

  public JPanel getPanel() {
    return fPanel;
  }

  /**
   * Listener for the operator model, if values change this listener will update the styler to reflect the changes.
   */
  private class OperatorModelChangeListener implements PropertyChangeListener {

    private final List<ILspAWTView> fViews;
    private final ILcdModel fModel;
    private final ExtendedOperator fExtendedOperator;

    public OperatorModelChangeListener(List<ILspAWTView> aViews, ILcdModel aModel, ExtendedOperator aExtendedOperator) {
      fViews = aViews;
      fModel = aModel;
      fExtendedOperator = aExtendedOperator;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
      if (evt.getSource() instanceof OperatorModel) {
        if (evt.getPropertyName().equals(OperatorModelExtended.NORMALIZEDVI_CHANGE_EVENT)) {
          boolean b = !(Boolean) evt.getNewValue();
          enableComponents(fCurvesPanel, b);
        }

        if (!evt.getPropertyName().equals(OperatorModel.STYLER_PROPERTY_CHANGE_EVENT)) {
          OperatorModel operatorModel = (OperatorModel) evt.getSource();
          List<ALcdImageOperatorChain> imageOperatorChains = operatorModel.getImageOperators();
          ALcdImageOperatorChain chain = imageOperatorChains.remove(0);
          imageOperatorChains.add(0, fExtendedOperator);
          //if removed operator is a bandselect operator, ensure that it will be used by the extended operator
          if (chain instanceof OperatorModel.BandSelectOperatorChain) {
            fExtendedOperator.setBandSelectOperatorChain((OperatorModel.BandSelectOperatorChain) chain);
          }

          for (ILspView view : fViews) {
            ILcdLayer layer = view.layerOf(fModel);
            setImageOperators(layer, imageOperatorChains.toArray(new ALcdImageOperatorChain[imageOperatorChains.size()]));
          }
        } else {
          for (ILspView view : fViews) {
            ILcdLayer layer = view.layerOf(fModel);
            invalidateStyle(layer);
          }
        }
      }
    }
  }

  private static void enableComponents(Container container, boolean enable) {
    Component[] components = container.getComponents();
    for (Component component : components) {
      component.setEnabled(enable);
      if (component instanceof Container) {
        enableComponents((Container) component, enable);
      }
    }
  }

  //set the imageOperatorChains on the styler
  private static void setImageOperators(ILcdLayer aLayer, ALcdImageOperatorChain... aImageOperatorChains) {
    if (aLayer instanceof ILspEditableStyledLayer) {
      ILspEditableStyledLayer layer = (ILspEditableStyledLayer) aLayer;
      ILspStyler styler = layer.getStyler(TLspPaintRepresentationState.REGULAR_BODY);
      // If we can find an ImageOperatorStyler, replace the operator
      if (styler instanceof MultispectralOperatorStyler) {
        ((MultispectralOperatorStyler) styler).setImageOperatorChain(chain(aImageOperatorChains));
        layer.invalidate();
      }
    }
  }

  private void invalidateStyle(ILcdLayer aLayer) {
    if (aLayer instanceof ILspEditableStyledLayer) {
      ILspEditableStyledLayer layer = (ILspEditableStyledLayer) aLayer;
      ILspStyler styler = layer.getStyler(TLspPaintRepresentationState.REGULAR_BODY);
      // If we can find an ImageOperatorStyler, replace the operator
      if (styler instanceof MultispectralOperatorStyler) {
        ((MultispectralOperatorStyler) styler).fireStyleChangeEvent();
        layer.invalidate();
      }
    }
  }

  /**
   * Creates an operator chain that appends a number of chains, i.e., applies the operators from first to last.
   *
   * @param aImageOperatorChains the chains to be appended
   *
   * @return a chain which combines the given chains
   */
  private static ALcdImageOperatorChain chain(ALcdImageOperatorChain... aImageOperatorChains) {
    final ALcdImageOperatorChain[] imageOperatorChains = aImageOperatorChains;
    return new ALcdImageOperatorChain() {
      @Override
      public ALcdImage apply(ALcdImage aInput) {
        ALcdBasicImage image = (ALcdBasicImage) aInput;
        for (ALcdImageOperatorChain operatorChain : imageOperatorChains) {
          image = (ALcdBasicImage) operatorChain.apply(image);
        }
        return image;

      }
    };
  }

  private enum ESelectionState {
    FLICKER, SWIPE, PORTHOLE
  }

  private class ImageSelectionButton extends JToggleButton {
    public ImageSelectionButton(String text, final OperatorModelExtended aOperatorModelExtended) {
      super(text);
      addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
          if (fSelectionState != ESelectionState.FLICKER) {
            //prevent selecting the same image on both sides
            if (getLeftSelectedButton() == ImageSelectionButton.this && getRightSelectedButton() == fLeftToRightButtons.get(ImageSelectionButton.this)) {
              selectNextButton(getRightSelectedButton());
            } else if (getRightSelectedButton() == ImageSelectionButton.this && getLeftSelectedButton() == fRightToLeftButtons.get(ImageSelectionButton.this)) {
              selectNextButton(getLeftSelectedButton());
            }
          }
          imageSelectionChanged(aOperatorModelExtended);
        }
      });
    }
  }

  /**
   * Creates a different color lookup table for landsat7 image
   */
  private static TLcdLookupTable createLandSat7ColorLookupTable() {
    Color color0 = new Color(31, 18, 26);
    Color color1 = new Color(148, 47, 37);
    Color color2 = new Color(195, 68, 2);
    Color color3 = new Color(229, 109, 10);
    Color color4 = new Color(243, 185, 70);
    Color color5 = new Color(254, 231, 106);
    Color color6 = new Color(218, 236, 104);
    Color color7 = new Color(160, 209, 77);
    Color color8 = new Color(67, 159, 34);
    Color color9 = new Color(16, 104, 23);
    Color color10 = new Color(22, 87, 44);

    Color[] colors = new Color[]{color0, color1, color2, color3, color4, color5, color6, color7, color8, color9, color10};
    double[] levels = new double[]{-1, -0.15, -0.1, 0.0, 0.1, 0.15, 0.2, 0.3, 0.4, 0.7, 1};

    TLcdColorMap colorMap = new TLcdColorMap(new TLcdInterval(-1, 1), levels, colors);
    return TLcdLookupTable.newBuilder().fromColorMap(colorMap).build();

  }

}
