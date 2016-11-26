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
package samples.gxy.contour.complexPolygon;

import java.awt.event.ActionEvent;

import javax.swing.JOptionPane;

import com.luciad.gui.ALcdAction;
import com.luciad.gui.TLcdImageIcon;
import com.luciad.model.ILcdModel;
import com.luciad.util.ILcdStatusListener;
import com.luciad.view.gxy.ILcdGXYEditableLabelsLayer;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.ILcdGXYView;
import com.luciad.view.gxy.asynchronous.TLcdGXYAsynchronousEditableLabelsLayerWrapper;
import com.luciad.view.gxy.asynchronous.TLcdGXYAsynchronousLayerWrapper;

import samples.gxy.common.layers.GXYLayerUtil;
import samples.gxy.contour.ContourLevels;

/**
 * Action to create complex polygon contours.
 */
public class CreateComplexPolygonContoursAction extends ALcdAction {

  private ILcdGXYView fGXYView;
  private ILcdGXYLayer fDMEDLayer;

  private ContourModelFactory fModelFactory;
  private ContourLayerFactory fLayerFactory;
  private ILcdStatusListener fStatusListener;

  /**
   * Construct a new CreateComplexPolygonContoursAction.
   * @param aContourLevels utility representing the contour levels of this sample
   * @param aDisjoint      Whether overlapping or disjoint complex polygons are desired. Disjoint
   *                       complex polygons are slower to draw because there are up to twice as much
   *                       segments, but can be drawn translucent because there are no parts of
   *                       other complex polygons behind them.
   */
  public CreateComplexPolygonContoursAction(ContourLevels aContourLevels, boolean aDisjoint, ILcdStatusListener aStatusListener) {
    fStatusListener = aStatusListener;
    setIcon(new TLcdImageIcon("images/gui/i16_los.gif"));
    setShortDescription(aDisjoint ? "Create Disjoint Complex Polygon Contours" : "Create Complex Polygon Contours");

    fModelFactory = new ContourModelFactory(aContourLevels, aDisjoint);
    fLayerFactory = new ContourLayerFactory(aContourLevels, aDisjoint);
  }

  @Override
  public void actionPerformed(ActionEvent e) {

    //Use a thread to display the progress bar updates
    Thread thread = new Thread(new Runnable() {
      @Override
      public void run() {
        //Add the layer to the view, using the AWT thread
        GXYLayerUtil.addGXYLayer(fGXYView, createContourLayer(fStatusListener));
      }
    });
    thread.setPriority(Thread.MIN_PRIORITY);
    thread.start();
  }

  public ILcdGXYLayer createContourLayer(ILcdStatusListener aStatusListener) {
    try {
      ILcdModel model = fModelFactory.createModel(fDMEDLayer, aStatusListener);
      ILcdGXYLayer layer = fLayerFactory.createGXYLayer(model);

      if (layer instanceof ILcdGXYEditableLabelsLayer) {
        layer = new TLcdGXYAsynchronousEditableLabelsLayerWrapper((ILcdGXYEditableLabelsLayer) layer);
      } else {
        layer = new TLcdGXYAsynchronousLayerWrapper(layer);
      }
      return layer;
    } catch (Exception ex) {
      ex.printStackTrace();
      JOptionPane.showMessageDialog(null, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
    return null;
  }

  /**
   * Set the DMED layer from which to create the contours.
   *
   * @param aDMEDLayer the DMED layer from which to create the contours.
   */
  public void setDMEDLayer(ILcdGXYLayer aDMEDLayer) {
    fDMEDLayer = aDMEDLayer;
  }

  /**
   * Set the view to which to add the contour layer.
   *
   * @param aGXYView the view to which to add the contour layer.
   */
  public void setGXYView(ILcdGXYView aGXYView) {
    fGXYView = aGXYView;
  }
}
