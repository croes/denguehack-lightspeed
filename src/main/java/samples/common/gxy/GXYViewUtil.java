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
package samples.common.gxy;

import com.luciad.reference.ILcdGridReference;
import com.luciad.util.ILcdStatusListener;
import com.luciad.view.ILcdPaintExceptionHandler;
import com.luciad.view.ILcdViewInvalidationListener;
import com.luciad.view.gxy.ILcdGXYView;
import com.luciad.view.gxy.ILcdGXYViewXYWorldTransformation;
import com.luciad.view.gxy.TLcdGXYViewBufferedImage;
import com.luciad.view.gxy.TLcdGXYViewCanvas;
import com.luciad.view.gxy.TLcdGXYViewJPanel;
import com.luciad.view.gxy.TLcdGXYViewJPanelLightWeight;
import com.luciad.view.gxy.TLcdGXYViewPanel;
import com.luciad.view.gxy.TLcdGXYViewXYWorldTransformation;

/**
 * View related utility methods.
 * Its primary purpose is to expose new view methods that are not in ILcdGXYView.
 */
public class GXYViewUtil {

  public static void addStatusListener(ILcdGXYView aView, ILcdStatusListener aStatusListener) {
    if (aView instanceof TLcdGXYViewBufferedImage) {
      ((TLcdGXYViewBufferedImage) aView).addStatusListener(aStatusListener);
    } else if (aView instanceof TLcdGXYViewJPanel) {
      ((TLcdGXYViewJPanel) aView).addStatusListener(aStatusListener);
    } else if (aView instanceof TLcdGXYViewPanel) {
      ((TLcdGXYViewPanel) aView).addStatusListener(aStatusListener);
    } else if (aView instanceof TLcdGXYViewCanvas) {
      ((TLcdGXYViewCanvas) aView).addStatusListener(aStatusListener);
    } else if (aView instanceof TLcdGXYViewJPanelLightWeight) {
      ((TLcdGXYViewJPanelLightWeight) aView).addStatusListener(aStatusListener);
    } else {
      throw new UnsupportedOperationException("Cannot register a status listener to " + aView.getClass());
    }
  }

  public static void addViewInvalidationListener(ILcdGXYView aView, ILcdViewInvalidationListener aViewInvalidationListener) {
    if (aView instanceof TLcdGXYViewBufferedImage) {
      ((TLcdGXYViewBufferedImage) aView).addViewInvalidationListener(aViewInvalidationListener);
    } else if (aView instanceof TLcdGXYViewJPanel) {
      ((TLcdGXYViewJPanel) aView).addViewInvalidationListener(aViewInvalidationListener);
    } else if (aView instanceof TLcdGXYViewPanel) {
      ((TLcdGXYViewPanel) aView).addViewInvalidationListener(aViewInvalidationListener);
    } else if (aView instanceof TLcdGXYViewCanvas) {
      ((TLcdGXYViewCanvas) aView).addViewInvalidationListener(aViewInvalidationListener);
    } else if (aView instanceof TLcdGXYViewJPanelLightWeight) {
      ((TLcdGXYViewJPanelLightWeight) aView).addViewInvalidationListener(aViewInvalidationListener);
    } else {
      throw new UnsupportedOperationException("Cannot register a view invalidation listener to " + aView.getClass());
    }
  }

  public static void removeViewInvalidationListener(ILcdGXYView aView, ILcdViewInvalidationListener aViewInvalidationListener) {
    if (aView instanceof TLcdGXYViewBufferedImage) {
      ((TLcdGXYViewBufferedImage) aView).removeViewInvalidationListener(aViewInvalidationListener);
    } else if (aView instanceof TLcdGXYViewJPanel) {
      ((TLcdGXYViewJPanel) aView).removeViewInvalidationListener(aViewInvalidationListener);
    } else if (aView instanceof TLcdGXYViewPanel) {
      ((TLcdGXYViewPanel) aView).removeViewInvalidationListener(aViewInvalidationListener);
    } else if (aView instanceof TLcdGXYViewCanvas) {
      ((TLcdGXYViewCanvas) aView).removeViewInvalidationListener(aViewInvalidationListener);
    } else if (aView instanceof TLcdGXYViewJPanelLightWeight) {
      ((TLcdGXYViewJPanelLightWeight) aView).removeViewInvalidationListener(aViewInvalidationListener);
    } else {
      throw new UnsupportedOperationException("Cannot remove a view invalidation listener from " + aView.getClass());
    }
  }

  public static void setPaintExceptionHandler(ILcdGXYView aView, ILcdPaintExceptionHandler aHandler) {
    if (aView instanceof TLcdGXYViewBufferedImage) {
      ((TLcdGXYViewBufferedImage) aView).setPaintExceptionHandler(aHandler);
    } else if (aView instanceof TLcdGXYViewJPanel) {
      ((TLcdGXYViewJPanel) aView).setPaintExceptionHandler(aHandler);
    } else if (aView instanceof TLcdGXYViewPanel) {
      ((TLcdGXYViewPanel) aView).setPaintExceptionHandler(aHandler);
    } else if (aView instanceof TLcdGXYViewCanvas) {
      ((TLcdGXYViewCanvas) aView).setPaintExceptionHandler(aHandler);
    } else if (aView instanceof TLcdGXYViewJPanelLightWeight) {
      ((TLcdGXYViewJPanelLightWeight) aView).setPaintExceptionHandler(aHandler);
    } else {
      throw new UnsupportedOperationException("Cannot set a paint exception handler for " + aView.getClass());
    }
  }

  public static double getScaleX(ILcdGXYView aView) {
    ILcdGXYViewXYWorldTransformation transformation = aView.getGXYViewXYWorldTransformation();
    if (transformation instanceof TLcdGXYViewXYWorldTransformation) {
      return ((TLcdGXYViewXYWorldTransformation) transformation).getScaleX();
    }
    return aView.getScale();
  }

  public static double getScaleY(ILcdGXYView aView) {
    ILcdGXYViewXYWorldTransformation transformation = aView.getGXYViewXYWorldTransformation();
    if (transformation instanceof TLcdGXYViewXYWorldTransformation) {
      return ((TLcdGXYViewXYWorldTransformation) transformation).getScaleY();
    }
    return aView.getScale();
  }

  public static void setScale(ILcdGXYView aView, double aScaleX, double aScaleY, boolean aRepaint, boolean aAdjusting) {
    if (aView instanceof TLcdGXYViewBufferedImage) {
      ((TLcdGXYViewBufferedImage) aView).setScale(aScaleX, aScaleY, aRepaint, aAdjusting);
    } else if (aView instanceof TLcdGXYViewJPanel) {
      ((TLcdGXYViewJPanel) aView).setScale(aScaleX, aScaleY, aRepaint, aAdjusting);
    } else if (aView instanceof TLcdGXYViewPanel) {
      ((TLcdGXYViewPanel) aView).setScale(aScaleX, aScaleY, aRepaint, aAdjusting);
    } else if (aView instanceof TLcdGXYViewCanvas) {
      ((TLcdGXYViewCanvas) aView).setScale(aScaleX, aScaleY, aRepaint, aAdjusting);
    } else if (aView instanceof TLcdGXYViewJPanelLightWeight) {
      ((TLcdGXYViewJPanelLightWeight) aView).setScale(aScaleX, aScaleY, aRepaint, aAdjusting);
    } else {
      double meanScale = Math.sqrt(aScaleX * aScaleY);
      aView.setScale(meanScale, aRepaint, aAdjusting);
    }
  }

  // conservative that retains the aspect ratio
  public static void setScale(ILcdGXYView aGXYViewSFCT, double aScale) {
    double currentAspectRatio = getScaleX(aGXYViewSFCT) / getScaleY(aGXYViewSFCT);
    double newScaleX = getScaleX(aGXYViewSFCT) > getScaleY(aGXYViewSFCT) ? aScale : aScale * currentAspectRatio;
    double newScaleY = getScaleY(aGXYViewSFCT) > getScaleX(aGXYViewSFCT) ? aScale : aScale / currentAspectRatio;
    setScale(aGXYViewSFCT, newScaleX, newScaleY, aGXYViewSFCT.isAutoUpdate(), false);
  }

}
