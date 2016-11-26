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
package samples.gxy.tooltip;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.Enumeration;

import javax.swing.JComponent;

import com.luciad.datamodel.ILcdDataObject;
import com.luciad.datamodel.TLcdDataProperty;
import com.luciad.gui.TLcdIconFactory;
import com.luciad.model.ILcdDataModelDescriptor;
import com.luciad.util.ILcdFunction;
import com.luciad.view.TLcdDomainObjectContext;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.ILcdGXYPainter;
import com.luciad.view.gxy.ILcdGXYView;
import com.luciad.view.gxy.TLcdGXYContext;
import com.luciad.view.gxy.controller.ALcdGXYController;

/**
 * An ILcdGXYController that displays tooltips for the object under the mouse
 * cursor. The object is obtained using the applyOnInteract2DBounds() method of
 * ILcd2DBoundsIndexedModel. The ILcdDataObject interface is used to obtain the
 * tooltip text.
 */
public class ToolTipController
    extends ALcdGXYController
    implements MouseMotionListener {

  public ToolTipController() {
    super();
    setName("Tool Tips");
    setShortDescription("Show tool tips for objects under the mouse");
    setIcon(TLcdIconFactory.create(TLcdIconFactory.FIXED_IMAGE_LAYER_ICON));
  }

  @Override
  public void mouseMoved(MouseEvent aEvent) {
    ILcdGXYView view = getGXYView();
    String toolTip = getTooltipUnderPoint(aEvent.getPoint());
    ((JComponent) view).setToolTipText(toolTip);
  }

  @Override
  public void mouseDragged(MouseEvent e) {
  }

  @Override
  public void terminateInteraction(ILcdGXYView aGXYView) {
    super.terminateInteraction(aGXYView);
    ((JComponent) aGXYView).setToolTipText(null);
  }

  /**
   * Returns a tooltip for the object under the given point using a simple isTouched check.
   * See {@link samples.gxy.highlight.HighlightController HighlightController} for a function
   * that also takes into account touched labels.
   *
   * @param aPoint a point in AWT coordinates
   * @return a tooltip, or null if no object or tooltip could be found
   */
  private String getTooltipUnderPoint(Point aPoint) {
    ILcdGXYView view = getGXYView();
    // To obtain the topmost object under the cursor, we walk through the
    // layers in the view in reverse order.
    Enumeration layers = view.layersBackwards();
    String toolTip = null;
    while (toolTip == null && layers.hasMoreElements()) {
      ILcdGXYLayer layer = (ILcdGXYLayer) layers.nextElement();

      // Skip invisible layers.
      if (!layer.isVisible()) {
        continue;
      }

      MyTouchedObjectFunction function = new MyTouchedObjectFunction(view, layer, view.getGraphics(), aPoint);
      Rectangle rectangle = new Rectangle(aPoint);
      rectangle.grow(20, 20); //accommodate when representation larger than object (e.g. icons)
      layer.applyOnInteract(function, rectangle, false, view);
      TLcdDomainObjectContext touchedObject = function.getTouchedObject();
      toolTip = touchedObject == null ? null : fetchToolTip(touchedObject);
    }
    return toolTip;
  }

  private String fetchToolTip(TLcdDomainObjectContext aContextObject) {
    String text;
    Object domainObject = aContextObject.getDomainObject();
    if (domainObject instanceof ILcdDataObject) {
      text = "<html>";
      ILcdDataObject dataObject = (ILcdDataObject) domainObject;
      ILcdDataModelDescriptor descriptor = (ILcdDataModelDescriptor) aContextObject.getModel().getModelDescriptor();
      int numProperties = 2; //display first two properties in tool tip
      java.util.List<TLcdDataProperty> dataProperties = descriptor.getModelElementTypes().iterator().next().getProperties();
      for (int i = 0; i < numProperties && i < dataProperties.size(); i++) {
        text = text + "<p>" + dataProperties.get(i).getDisplayName() + ": <b>" + dataObject.getValue(dataProperties.get(i)) + "</b></p>";
      }
      text = text + "</html>";
    } else {
      text = domainObject.toString();
    }
    return text;
  }

  /**
   * ILcdFunction for use with ILcdGXYLayer.applyOnInteract.  For the objects identified by this call,
   * it verifies if they are really touched, as defined by the ILcdGXYPainter.
   */
  private class MyTouchedObjectFunction implements ILcdFunction {
    private TLcdGXYContext fContext;
    private Graphics fGraphics;
    private ILcdGXYLayer fLayer;
    private TLcdDomainObjectContext fTouchedObject = null;

    public MyTouchedObjectFunction(ILcdGXYView aGXYView, ILcdGXYLayer aLayer, Graphics aGraphics, Point aPoint) {
      fLayer = aLayer;
      fGraphics = aGraphics;
      fContext = new TLcdGXYContext(aGXYView, aLayer);
      fContext.setX(aPoint.x);
      fContext.setY(aPoint.y);
    }

    public TLcdDomainObjectContext getTouchedObject() {
      return fTouchedObject;
    }

    @Override
    public boolean applyOn(Object aObject) throws IllegalArgumentException {
      // If aObject is touched (as defined by the painter), we set the
      // view's tool tip and abort the applyOnInteract procedure.
      if (fLayer.getGXYPainter(aObject).isTouched(
          fGraphics, ILcdGXYPainter.DEFAULT | ILcdGXYPainter.BODY, fContext)) {
        fTouchedObject = new TLcdDomainObjectContext(aObject, fLayer.getModel(), fLayer, fContext.getGXYView());
        return false;
      }
      return true;
    }
  }
}
