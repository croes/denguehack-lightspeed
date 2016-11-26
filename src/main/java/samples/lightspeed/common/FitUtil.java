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
package samples.lightspeed.common;

import java.awt.Component;
import java.util.Arrays;
import java.util.Collection;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import com.luciad.gui.TLcdAWTUtil;
import com.luciad.model.ILcdModelReference;
import com.luciad.shape.ILcdBounds;
import com.luciad.util.TLcdNoBoundsException;
import com.luciad.util.TLcdOutOfBoundsException;
import com.luciad.view.lightspeed.ALspViewAdapter;
import com.luciad.view.lightspeed.ILspAWTView;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.util.TLspViewNavigationUtil;
import com.luciad.view.opengl.binding.ILcdGLDrawable;

/**
 * Thin convenience layer around TLspViewNavigationUtil.
 * Its primary purpose is to allow fitting layers on views that are not yet fully initialized.
 */
public class FitUtil {

  private FitUtil() {
  }

  public static void fitOnLayers(final LightspeedSample aSample,
                                 final Collection<ILspLayer> aLayers) {
    fitOnLayers(aSample, aSample.getView(), false, aLayers);
  }

  public static void fitOnLayers(final LightspeedSample aSample,
                                 final ILspLayer... aLayers) {
    fitOnLayers(aSample, aSample.getView(), false, aLayers);
  }

  public static void fitOnLayers(final Component aComponentForFailMessage,
                                 final ILspView aView,
                                 final boolean aAnimatedFit,
                                 final Collection<ILspLayer> aLayers) {
    if (aLayers != null) {
      ILspLayer[] layers = new ILspLayer[aLayers.size()];
      aLayers.toArray(layers);
      FitUtil.fitOnLayers(aComponentForFailMessage, aView, aAnimatedFit, layers);
    }
  }

  public static void fitOnLayers(final Component aComponentForFailMessage,
                                 final ILspView aView,
                                 final boolean aAnimatedFit,
                                 final ILspLayer... aLayers) {
    if (aLayers != null && aLayers.length > 0) {
      boolean fit = false;
      for (ILspLayer layer : aLayers) {
        if (layer.isVisible()) {
          fit = true;
        }
      }
      if (fit) {
        TLcdAWTUtil.invokeNowOrLater(
            new Runnable() {
              public void run() {
                //At startup the view's width and height might not
                //have been initialized. To make sure that these
                //have been set properly, fitting is performed
                //at the end of the view's repaint by using an
                //ILspViewListener.
                aView.addViewListener(new ALspViewAdapter() {
                  @Override
                  public void preRender(ILspView aView, ILcdGLDrawable aGLDrawable) {
                    aView.removeViewListener(this);
                    try {
                      if (aAnimatedFit) {
                        new TLspViewNavigationUtil(aView).animatedFit(Arrays.asList(aLayers));
                      } else {
                        new TLspViewNavigationUtil(aView).fit(aLayers);
                      }
                    } catch (TLcdOutOfBoundsException e) {
                      showMessageDialog(aView, aComponentForFailMessage, "Layer not visible in current projection.");
                    } catch (final TLcdNoBoundsException e) {
                      showMessageDialog(aView, aComponentForFailMessage, "Could not fit on the layer.\n" + e.getMessage());
                    }
                  }
                });
              }
            }
        );
      }
    }
  }

  public static void fitOnBounds(final LightspeedSample aSample,
                                 final ILcdBounds aBounds,
                                 final ILcdModelReference aModelReference) {

    fitOnBounds(aSample, aSample.getView(), aBounds, aModelReference);
  }

  public static void fitOnBounds(final Component aComponentForFailMessage,
                                 final ILspView aView,
                                 final ILcdBounds aBounds,
                                 final ILcdModelReference aModelReference) {
    aView.addViewListener(new ALspViewAdapter() {
      @Override
      public void preRender(ILspView aView, ILcdGLDrawable aGLDrawable) {
        aView.removeViewListener(this);
        try {
          new TLspViewNavigationUtil(aView).fitOnModelBounds(aBounds, aModelReference);
        } catch (TLcdOutOfBoundsException e) {
          showMessageDialog(aView, aComponentForFailMessage, "Could not fit on destination, destination is outside the valid bounds");
        }
      }
    });
  }

  private static void showMessageDialog(ILspView aView, final Component aComponent, final String aMessage) {
    if (aView instanceof ILspAWTView) {
      SwingUtilities.invokeLater(new Runnable() {
        @Override
        public void run() {
          JOptionPane.showMessageDialog(aComponent, aMessage);
        }
      });
    }
  }
}
