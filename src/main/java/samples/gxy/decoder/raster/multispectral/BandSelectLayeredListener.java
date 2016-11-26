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
package samples.gxy.decoder.raster.multispectral;

import static samples.gxy.decoder.raster.multispectral.ImageUtil.isMultispectral;

import java.awt.Component;
import java.util.Map;
import java.util.WeakHashMap;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import com.luciad.imaging.ALcdImage;
import com.luciad.imaging.operator.ALcdImageOperatorChain;
import com.luciad.util.collections.ILcdCollection;
import com.luciad.view.ILcdLayer;
import com.luciad.view.ILcdLayered;

import samples.common.action.LayerCustomizerSupport;

/**
 * Selects bands for a multi-band image.
 */
public abstract class BandSelectLayeredListener<S extends ILcdLayered, T extends ILcdLayer> extends
                                                                                            LayerCustomizerSupport<S, T> {
  private final Map<ALcdImage, int[]> fSelectedBands = new WeakHashMap<>();
  private final Component fOwner;

  protected BandSelectLayeredListener(Component aOwner, S aLayered, ILcdCollection<ILcdLayer> aSelectedLayers) {
    super(aLayered, aSelectedLayers);
    fOwner = aOwner;
  }

  @Override
  protected void layerAdded(S aView, final T aLayer) {
    // If a layer was added, check if band selection is required
    final ALcdImage image = ImageUtil.getImage(aLayer);
    if (image != null && fSelectedBands.get(image) == null) {
      if (isMultispectral(image)) {
        int[] selectedBands = new int[]{0, 1, 2};
        fSelectedBands.put(image, selectedBands);
        setBandSelectFilter(aLayer, image, ImageUtil.createBandSelectOperatorChain(selectedBands));
        SwingUtilities.invokeLater(new Runnable() {
          @Override
          public void run() {
            JOptionPane.showMessageDialog(fOwner,
                                          "Layer " + aLayer.getLabel() + "\n" +
                                          "contains an image with more than 3 bands.\n" +
                                          "To visualize this, a band select operator was applied\n"         +
                                          "that selects the first 3 bands. More information on multiband\n" +
                                          "images and image operators can be found in the\n"                +
                                          "'Multispectral image visualization' sample.",
                                          "Multiband image",
                                          JOptionPane.INFORMATION_MESSAGE);
          }
        });
      }
    }
  }

  @Override
  protected void layerRemoved(S aView, T aLayer) {
  }

  @Override
  protected void layerSelected(S aView, T aLayer) {
  }

  protected abstract void setBandSelectFilter(T aLayer, ALcdImage aImage, ALcdImageOperatorChain aBandSelect);
}
