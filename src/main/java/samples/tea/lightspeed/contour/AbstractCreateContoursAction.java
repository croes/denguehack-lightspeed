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
package samples.tea.lightspeed.contour;

import java.awt.event.ActionEvent;

import javax.swing.JDialog;

import com.luciad.gui.ALcdAction;
import com.luciad.gui.TLcdImageIcon;
import com.luciad.model.ILcdModel;
import com.luciad.shape.ILcdBounds;
import com.luciad.shape.shape2D.ILcd2DEditableBounds;
import com.luciad.util.ILcdFireEventMode;
import com.luciad.util.ILcdFunction;
import com.luciad.util.ILcdStatusListener;
import com.luciad.util.ILcdStatusSource;
import com.luciad.view.lightspeed.layer.ILspLayer;

import samples.gxy.common.ProgressUtil;
import samples.lightspeed.common.LightspeedSample;

/**
 * This is an extension of <code>ALcdAction</code> that allows the user to
 * create the contour lines of a DTED.
 *
 * @see <code>ALcdAction</code>
 * @see <code>ILcdLineOfSightCoverage</code>
 */
abstract class AbstractCreateContoursAction extends ALcdAction implements Runnable {

  private LightspeedSample fView;
  protected ILcdStatusSource fStatusSource;
  protected ILcdBounds fBounds;
  protected ILcd2DEditableBounds f2DEditableBounds;
  protected ImageHeightMatrixView fImageMatrixView;

  public AbstractCreateContoursAction() {
    setIcon(new TLcdImageIcon("images/gui/i16_eyes.gif"));
    setShortDescription("Create contours over the view area");
  }

  public void setStatusSource(ILcdStatusSource aStatusSource) {
    fStatusSource = aStatusSource;
  }

  public void setView(LightspeedSample aView) {
    fView = aView;
  }

  /**
   * Sets the ImageMatrixView from which altitude information will be retrieved.
   *
   * @param aImageModel the model from which to retrieve altitude information.
   */
  public void setImageModel(ILcdModel aImageModel) {
    fImageMatrixView = new ImageHeightMatrixView(aImageModel);
  }

  /**
   * Sets the bounds to compute the contours in.
   *
   * @param aBounds The bounds to compute the contours in.
   */
  public void setBounds(ILcdBounds aBounds) {
    fBounds = aBounds;

    f2DEditableBounds = fBounds.cloneAs2DEditableBounds();
    double factor = 0.2;
    f2DEditableBounds.move2D( fBounds.getLocation().getX() - ( factor * fBounds.getWidth () ),
                              fBounds.getLocation().getY() - ( factor * fBounds.getHeight() ) );
    f2DEditableBounds.setWidth( ( 1 + ( 2 * factor ) ) * fBounds.getWidth() );
    f2DEditableBounds.setHeight( ( 1 + ( 2 * factor ) ) * fBounds.getHeight() );
  }

  protected boolean isInitialized() {
    return fStatusSource != null && fImageMatrixView != null && fBounds != null;
  }

  public void actionPerformed(ActionEvent aActionEvent) {
    Thread thread = new Thread(this);
    thread.setPriority(Thread.MIN_PRIORITY);
    thread.start();
  }

  public void run() {
    JDialog dialog = ProgressUtil.createProgressDialog(fView, "Creating contour levels.");
    try {
      ProgressUtil.showDialog(dialog);
      fStatusSource.addStatusListener((ILcdStatusListener) dialog);

      fView.getView().addLayer(createContourLayer());
    }
    finally {
      fStatusSource.removeStatusListener((ILcdStatusListener) dialog);
      ProgressUtil.hideDialog(dialog);
    }
  }

  abstract ILspLayer createContourLayer();

  protected static class BuilderFunction implements ILcdFunction {

    private ILcdModel fModel;

    public BuilderFunction(ILcdModel aModel) {
      fModel = aModel;
    }

    public boolean applyOn(Object aObject) throws IllegalArgumentException {
      fModel.addElement(aObject, ILcdFireEventMode.NO_EVENT);
      return true;
    }

  }

}
