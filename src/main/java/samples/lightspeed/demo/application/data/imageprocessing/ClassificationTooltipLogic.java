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

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import com.luciad.imaging.ALcdBasicImage;
import com.luciad.imaging.ALcdImage;
import com.luciad.imaging.ILcdImageModelDescriptor;
import com.luciad.model.ILcdModel;
import com.luciad.shape.ILcdPoint;
import com.luciad.shape.shape2D.TLcdXYPoint;
import com.luciad.shape.shape3D.ILcd3DEditablePoint;
import com.luciad.util.ILcdDisposable;
import com.luciad.util.TLcdOutOfBoundsException;
import com.luciad.view.ILcdLayer;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.TLspContext;
import com.luciad.view.lightspeed.controller.visualinspection.TLspFlickerController;
import com.luciad.view.lightspeed.controller.visualinspection.TLspSwipeController;
import com.luciad.view.lightspeed.layer.ILspLayer;

import samples.common.lightspeed.visualinspection.SwipeController;
import samples.lightspeed.demo.framework.gui.DemoUIColors;
import samples.lightspeed.demo.framework.gui.TooltipMouseListener;

/**
 * Class that allows to show a tooltip indicating the classification of the underlying material.
 */
class ClassificationTooltipLogic implements TooltipMouseListener.TooltipLogic, ILcdDisposable {

  private TLspFlickerController fFlickerController;
  private TLspSwipeController fSwipeController;
  private ALcdBasicImage fImage1, fImage2;
  private ALcdImage fLandSatImage;
  private ILspView fView;
  private ILspLayer fLayer1, fLayer2, fLandSatLayer;
  private SpectrumReader fSpectrumReader;
  private ExecutorService fExecutorService;
  private JLabel fLabel = new JLabel("Unknown") {

    @Override
    protected void paintComponent(Graphics g) {
      Graphics g2 = g.create();
      g2.setColor(DemoUIColors.PANEL_COLOR);
      g2.fillRect(0, 0, getSize().width, getSize().height);
      g2.setColor(DemoUIColors.PANEL_BORDER_COLOR);
      g2.drawRect(0, 0, getSize().width - 1, getSize().height - 1);
      g2.translate(4, 0);
      super.paintComponent(g2);
    }
  };

  public ClassificationTooltipLogic(ILspView aView,
                                    TLspSwipeController aSwipeController,
                                    TLspFlickerController aFlickerController,
                                    ILcdModel aModel1,
                                    ALcdBasicImage aImage1,
                                    ILcdModel aModel2,
                                    ALcdBasicImage aImage2,
                                    ILcdModel aLandSatModel,
                                    ALcdImage aLandSatImage) {
    fView = aView;
    fSwipeController = aSwipeController;
    fFlickerController = aFlickerController;
    fImage1 = aImage1;
    fImage2 = aImage2;
    fLayer1 = (ILspLayer) aView.layerOf(aModel1);
    fLayer2 = (ILspLayer) aView.layerOf(aModel2);
    if (aLandSatModel != null) {
      fLandSatLayer = (ILspLayer) aView.layerOf(aLandSatModel);
    }
    fLandSatImage = aLandSatImage;
    fSpectrumReader = new SpectrumReader();
    fExecutorService = Executors.newSingleThreadExecutor();
  }

  @Override
  public boolean canHandleLayer(ILcdLayer aLayer) {
    return aLayer.getModel().getModelDescriptor() instanceof ILcdImageModelDescriptor;
  }

  public void setSwipeController(SwipeController aSwipeController) {
    fSwipeController = aSwipeController;
  }

  public ILspView getView() {
    return fView;
  }

  @Override
  public boolean shouldConsiderLayer(ILcdLayer aLayer) {
    return canHandleLayer(aLayer) && aLayer.isVisible();
  }

  @Override
  public void willRecalculateTooltip() {
    // Do nothing
  }

  @Override
  public Component createLabelComponent(Object aModelElement) {
    return fLabel;
  }

  @Override
  public void updateForFoundModelElement(Object aModelElement, ILcdLayer aLayer, MouseEvent aMouseEvent, TooltipMouseListener aTooltipMouseListener) {
    final ALcdImage activeImage = getActiveImage(aMouseEvent);
    final ILspLayer activeLayer = activeImage == fImage1 ? fLayer1 : (activeImage == fImage2 ? fLayer2 : null);

    if (activeImage != null && activeLayer != null && activeLayer.isVisible()) {
      if (activeImage instanceof ALcdBasicImage) {
        fSpectrumReader.setImage((ALcdBasicImage) activeImage);
      }
      final TLspContext context = new TLspContext(activeLayer, fView);
      ILcdPoint worldPoint = fView.getServices().getTerrainSupport().getPointOnTerrain(new TLcdXYPoint(aMouseEvent.getX(), aMouseEvent.getY()), context);
      if (worldPoint != null) {
        final ILcd3DEditablePoint modelPoint = context.getModelReference().makeModelPoint().cloneAs3DEditablePoint();
        try {
          context.getModelXYZWorldTransformation().worldPoint2modelSFCT(worldPoint, modelPoint);
          if (activeImage.getBounds().contains2D(modelPoint)) {
            final TooltipMouseListener listener = aTooltipMouseListener;
            final MouseEvent event = aMouseEvent;
            Runnable r = new Runnable() {
              @Override
              public void run() {
                List<float[]> spectrumValues = fSpectrumReader.retrieveSpectrumFromArea(modelPoint);
                SpectrumClassifier classifier = new SpectrumClassifier();
                final SpectrumClassifier.Classification classification = classifier.getClassification(spectrumValues);
                SwingUtilities.invokeLater(new Runnable() {
                  @Override
                  public void run() {
                    fLabel.setText(classification.getDisplayName());
                    listener.showTooltip(modelPoint, activeLayer, event.getX(), event.getY());
                  }
                });
              }
            };
            fExecutorService.submit(r);
            return;
          }
        } catch (TLcdOutOfBoundsException ignored) {

        }
      }

    }

    aTooltipMouseListener.showTooltip(null, null, aMouseEvent.getX(), aMouseEvent.getY());
  }

  private ALcdImage getActiveImage(MouseEvent aMouseEvent) {
    ALcdImage activeImage = null;
    if (fFlickerController.getView() != null && fFlickerController.getVisibleIndex() == 0) {
      activeImage = fImage1;
    } else if (fFlickerController.getView() != null && fFlickerController.getVisibleIndex() == 1) {
      activeImage = fImage2;
    } else if (fFlickerController.getView() != null && fFlickerController.getVisibleIndex() == 3) {
      //dont make classification for landsat image for now
      activeImage = null;
    } else if (fSwipeController != null && fSwipeController.getView() != null) {
      double x = aMouseEvent.getX();
      double y = aMouseEvent.getY();
      TLspSwipeController.SwipeLineOrientation orientation = fSwipeController.getSwipeLineOrientation();
      List<Collection<ILspLayer>> layers = fSwipeController.getLayers();
      //we always expect only two layers in the swipe controler
      ILspLayer layer1 = layers.get(0).iterator().next();
      ILspLayer layer2 = layers.get(1).iterator().next();
      ALcdImage leftImage = layer1 == fLayer1 ? fImage1 : layer1 == fLayer2 ? fImage2 : null;
      ALcdImage rightImage = layer2 == fLayer2 ? fImage2 : layer2 == fLayer1 ? fImage1 : null;
      Point location = fSwipeController.getSwipeLineLocation();
      if (orientation == TLspSwipeController.SwipeLineOrientation.HORIZONTAL) {
        if (y <= location.getY()) {
          activeImage = leftImage;
        } else {
          activeImage = rightImage;
        }
      } else {
        if (x < location.getX()) {
          activeImage = leftImage;
        } else {
          activeImage = rightImage;
        }
      }
    }

    return activeImage;
  }

  @Override
  public int getQuerySensitivity() {
    return 0;
  }

  @Override
  public void dispose() {
    fSpectrumReader.dispose();
  }
}
