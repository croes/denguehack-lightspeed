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
/*
 *
 * Copyright (c) 1999-2015 Luciad All Rights Reserved.
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
package samples.lightspeed.imaging.multispectral;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.luciad.datamodel.ILcdDataObject;
import com.luciad.gui.TLcdAWTUtil;
import com.luciad.gui.TLcdIconFactory;
import com.luciad.gui.TLcdUserDialog;
import com.luciad.gui.awt.dialogs.TLcdDialogManager;
import com.luciad.imaging.operator.ALcdImageOperatorChain;
import com.luciad.imaging.operator.TLcdPixelRescaleOp;
import com.luciad.util.ILcdFilter;
import com.luciad.util.collections.ILcdCollection;
import com.luciad.util.logging.ILcdLogger;
import com.luciad.util.logging.TLcdLoggerFactory;
import com.luciad.view.ILcdLayer;
import com.luciad.view.ILcdTreeLayered;
import com.luciad.view.ILcdView;
import com.luciad.view.lightspeed.layer.TLspPaintRepresentationState;
import com.luciad.view.lightspeed.layer.raster.TLspRasterLayer;
import com.luciad.view.lightspeed.style.styler.ILspStyler;

import samples.common.layerControls.actions.AbstractLayerTreeAction;

/**
 * Actions that can save images in a multispectral TLspRasterLayer, retaining MultispectralOperatorStyler settings.
 *
 * @since 2015.1
 */
public class SaveImageAction extends AbstractLayerTreeAction {

  private final ILcdView fView;
  private Frame fParentFrame;
  private static final ILcdLogger LOGGER = TLcdLoggerFactory.getLogger(SaveImageAction.class);
  private String fSavedPath = System.getProperty("user.dir") + File.separator + "resources" + File.separator + "Data" + File.separator;

  /**
   *
   * Action that will save the image of the currently selected layer in the layer tree on disk.
   * The Layer has to be a <code>TLspRasterLayer</code> that uses <code>ImageOperatorStyler</code>.
   *
   *
   * @param aView           The view to work on. Must implement {@code ILcdTreeLayered} as well.
   * @param aSelectedLayers The layers to take into account.
   */
  public SaveImageAction(ILcdView aView, ILcdCollection<ILcdLayer> aSelectedLayers) {
    this(aView, aSelectedLayers, null, 1, 1);
    setIcon(TLcdIconFactory.create(TLcdIconFactory.SAVE_ICON));
    setShortDescription("Save Image");
  }

  /**
   * Constructs a new {@code SaveImageAction}. It takes a filter to decide which
   * objects should be taken into account. The action is enabled if the number of objects that are
   * accepted by the filter is in the given min/max range, disabled otherwise.
   *
   * @param aView           The view to work on. Must implement {@code ILcdTreeLayered} as well.
   * @param aSelectedLayers The selected layers to take into account.
   * @param aLayerFilter    Sets the object filter. The filter is used to detect if the objects of
   *                        the tree selection are recognized (accepted) by this action. If all
   *                        objects of the tree selection are recognized, the action is enabled. The
   *                        given object can be {@code null} in that case a default RasterLayerFilter is created.
   * @param aMinObjectCount Defines the minimum number of objects that should pass the filter for
   *                        the action to be enabled.
   * @param aMaxObjectCount Defines the maximum number of objects that should pass the filter for
   *                        the action to be enabled. You can use {@code -1} as a short cut for
   *                        {@code Integer.MAX_VALUE}, to leave the maximum unbounded.
   */
  public SaveImageAction(ILcdView aView,
                         ILcdCollection<ILcdLayer> aSelectedLayers,
                         ILcdFilter<ILcdLayer> aLayerFilter,
                         int aMinObjectCount,
                         int aMaxObjectCount) {
    super((ILcdTreeLayered) aView, true, aMinObjectCount, aMaxObjectCount);
    fView = aView;
    addLayerFilter(aLayerFilter == null ? new RasterLayerFilter() : aLayerFilter);
    installSelectionListener(aSelectedLayers);
  }

  /**
   * Sets the parent Frame to be used by Dialogs (optional).
   */
  public void setParentFrame(Frame aParentFrame) {
    fParentFrame = aParentFrame;
  }

  public Frame getParentFrame() {
    return fParentFrame;
  }

  /**
   * Returns the view as it was provided to the constructor.
   *
   * @return the view as it was provided to the constructor.
   */
  public ILcdView getView() {
    return fView;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    if (isEnabled()) {
      saveImage(e);
    }
  }

  private void saveImage(ActionEvent aActionEvent) {
    String outputFileName;
    Frame parentFrame = fParentFrame != null ? fParentFrame : TLcdAWTUtil.findParentFrame(aActionEvent);

    ArrayList<ILcdLayer> list = getFilteredLayers();

    for (ILcdLayer layer : list) {
      if (layer instanceof TLspRasterLayer) {

        JFileChooser chooser = new JFileChooser(fSavedPath);
        chooser.setFileFilter(new FileNameExtensionFilter("GeoTIFF", ".tiff"));
        if (chooser.showSaveDialog(parentFrame) == JFileChooser.APPROVE_OPTION) {
          outputFileName = chooser.getSelectedFile().getPath();
          fSavedPath = chooser.getSelectedFile().getParent();
          float brightness = 1;
          float contrast = 1;
          List<ALcdImageOperatorChain> chains = null;
          ILspStyler styler = ((TLspRasterLayer) layer).getStyler(TLspPaintRepresentationState.REGULAR_BODY);
          if (styler instanceof MultispectralOperatorStyler) {
            OperatorModel operatormodel = ((MultispectralOperatorStyler) styler).getOperatorModel();
            chains = operatormodel.getImageOperators();
            brightness = operatormodel.getBrightness();
            contrast = operatormodel.getContrast();
          }

          if (layer.getModel().elements().hasMoreElements()) {
            try {
              addBrightnessContrastOp(brightness, contrast, chains);
              SaveGeoTiff.save(chains, layer.getModel(), outputFileName);
            } catch (Exception e) {
              TLcdUserDialog.message("Image could not be saved :" + e.getMessage(), TLcdDialogManager.ERROR_MESSAGE, this, parentFrame);
            }
          } else {
            TLcdUserDialog.message("No image available in the model", TLcdDialogManager.ERROR_MESSAGE, this, parentFrame);
          }
        }
      }
    }
  }

  /**
   * add brightness and contrast as Image operator to the chains
   */
  private void addBrightnessContrastOp(float aBrightness, float aContrast, List<ALcdImageOperatorChain> aChains) {

    LOGGER.debug("brightness " + aBrightness);
    LOGGER.debug("contrast " + aContrast);

    //        result.rgb = ((result.rgb - 0.5) * contrast) + 0.5;
    //        result.rgb += (brightness - 1.0) * (contrast * 0.5 + 0.5);
    double ii = (aBrightness - 1) * (aContrast * 0.5 + 0.5);

    TLcdPixelRescaleOp pixelRescale = new TLcdPixelRescaleOp();
    ILcdDataObject params = pixelRescale.getParameterDataType().newInstance();
    params.setValue(TLcdPixelRescaleOp.SCALES, new double[]{1, 1, 1});
    params.setValue(TLcdPixelRescaleOp.OFFSETS, new double[]{-0.5, -0.5, -0.5});
    aChains.add(aChains.size(), ALcdImageOperatorChain.newBuilder().operator(pixelRescale, params).build());

    TLcdPixelRescaleOp pixelRescale2 = new TLcdPixelRescaleOp();
    params = pixelRescale2.getParameterDataType().newInstance();
    params.setValue(TLcdPixelRescaleOp.SCALES, new double[]{aContrast, aContrast, aContrast});
    params.setValue(TLcdPixelRescaleOp.OFFSETS, new double[]{0, 0, 0});
    aChains.add(aChains.size(), ALcdImageOperatorChain.newBuilder().operator(pixelRescale2, params).build());

    TLcdPixelRescaleOp pixelRescale3 = new TLcdPixelRescaleOp();
    params = pixelRescale3.getParameterDataType().newInstance();
    params.setValue(TLcdPixelRescaleOp.SCALES, new double[]{1, 1, 1});
    params.setValue(TLcdPixelRescaleOp.OFFSETS, new double[]{0.5, 0.5, 0.5});
    aChains.add(aChains.size(), ALcdImageOperatorChain.newBuilder().operator(pixelRescale3, params).build());

    TLcdPixelRescaleOp pixelRescale4 = new TLcdPixelRescaleOp();
    params = pixelRescale4.getParameterDataType().newInstance();
    params.setValue(TLcdPixelRescaleOp.SCALES, new double[]{1, 1, 1});
    params.setValue(TLcdPixelRescaleOp.OFFSETS, new double[]{ii, ii, ii});
    aChains.add(aChains.size(), ALcdImageOperatorChain.newBuilder().operator(pixelRescale4, params).build());
  }

}






