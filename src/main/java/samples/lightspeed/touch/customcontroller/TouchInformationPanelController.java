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
package samples.lightspeed.touch.customcontroller;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.swing.JLabel;

import com.luciad.datamodel.ILcdDataObject;
import com.luciad.datamodel.TLcdDataProperty;
import com.luciad.gui.TLcdIconFactory;
import com.luciad.gui.swing.TLcdOverlayLayout;
import com.luciad.input.touch.TLcdTouchPoint;
import com.luciad.view.lightspeed.ILspAWTView;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.controller.selection.TLspSelectControllerModel;
import com.luciad.view.lightspeed.controller.selection.TLspSelectPointInput;
import com.luciad.view.lightspeed.controller.touch.ALspConfinedTouchController;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.TLspDomainObjectContext;
import com.luciad.view.lightspeed.layer.TLspPaintRepresentation;

/**
 * This controller adds an information panel for the first touched object under the mouse cursor.
 */
public class TouchInformationPanelController extends ALspConfinedTouchController {

  private static final int PANEL_WIDTH_OFFSET = 10;
  private static final int PANEL_HEIGHT_OFFSET = 6;
  private static final Color PANEL_FILL_COLOR = Color.WHITE;
  private static final Color PANEL_FRAME_COLOR = Color.BLACK;

  // The label.
  private JLabel fLabel;

  // Used to retrieve the touched objects.
  private TLspSelectControllerModel fSelectControllerModel = new TLspSelectControllerModel();

  private Set<TLspPaintRepresentation> fWhat = Collections.singleton(TLspPaintRepresentation.BODY);

  public TouchInformationPanelController() {
    super(1);
    setName("Information Panels");
    setShortDescription("Touch and hold an object to reveal an info panel");
    setIcon(TLcdIconFactory.create(TLcdIconFactory.FIXED_IMAGE_LAYER_ICON));
  }

  /**
   * Adds the component to the specified view and repaints the view.
   *
   * @param aComponent the component.
   * @param aView      the view.
   */
  protected void addComponentToView(Component aComponent, ILspView aView) {
    if (aView instanceof ILspAWTView) {
      Container overlayComponent = ((ILspAWTView) aView).getOverlayComponent();
      overlayComponent.add(aComponent, TLcdOverlayLayout.Location.NO_LAYOUT);
      overlayComponent.revalidate();
      overlayComponent.repaint();
    }
  }

  /**
   * Removes the component from the specified view and repaints the view.
   *
   * @param aComponent the component.
   * @param aView      the view.
   */
  protected void removeComponentFromView(Component aComponent, ILspView aView) {
    if (aView instanceof ILspAWTView) {
      Container overlayComponent = ((ILspAWTView) aView).getOverlayComponent();
      overlayComponent.remove(aComponent);
      overlayComponent.revalidate();
      overlayComponent.repaint();
    }
  }

  @Override
  protected void terminateInteractionImpl(ILspView aView) {
    super.terminateInteractionImpl(aView);
    // Remove the information panel when this controller is no longer active.
    removePanel();
  }

  @Override
  protected List<TLcdTouchPoint> touchPointAvailable(List<TLcdTouchPoint> aTouchPoints, TLcdTouchPoint aTouchDown) {
    movePanel(aTouchDown.getLocation().x, aTouchDown.getLocation().y);
    return aTouchPoints;
  }

  @Override
  protected List<TLcdTouchPoint> touchPointMoved(List<TLcdTouchPoint> aTrackedTouchPoints, TLcdTouchPoint aTouchMoved) {
    removePanel();
    aTrackedTouchPoints.clear();
    return aTrackedTouchPoints;
  }

  @Override
  protected List<TLcdTouchPoint> touchPointWithdrawn(List<TLcdTouchPoint> aTouchPoints, TLcdTouchPoint aTouchUp) {
    removePanel();
    return aTouchPoints;
  }

  /**
   * Removes the panel from the view.
   */
  private synchronized void removePanel() {
    if (fLabel != null) {
      removeComponentFromView(fLabel, getView());
      fLabel = null;
    }
  }

  /**
   * Moves the information panel to the x and y coordinates specified.
   *
   * @param aX the new <i>x</i>-coordinate.
   * @param aY the new <i>y</i>-coordinate.
   */
  private synchronized void movePanel(int aX, int aY) {
    // Retrieve the object under the cursor.
    List<TLspDomainObjectContext> candidates = fSelectControllerModel.selectionCandidates(
        new TLspSelectPointInput(new Point(aX, aY)),
        fWhat,
        false,
        getView()
    );

    if (candidates.isEmpty()) {
      // Remove the label if it was added before.
      removePanel();
    } else {
      // Add the label if it does not exist yet.
      if (fLabel == null) {
        fLabel = new MyLabel();
        addComponentToView(fLabel, getView());
      }

      // Retrieve the information String.
      String information = fetchInformation(candidates.get(0));

      // Adjust the label.
      fLabel.setText(information);
      Dimension preferredSize = fLabel.getPreferredSize();
      int w = preferredSize.width + PANEL_WIDTH_OFFSET;
      int h = preferredSize.height + PANEL_HEIGHT_OFFSET;
      fLabel.setBounds(aX - w, aY - h, w, h);
    }
  }

  /**
   * Fetches information that is relevant to show to the user for the specified selection
   * candidate.
   *
   * @param aCandidate the selection candidate.
   *
   * @return the information about the selection candidate as a string.
   */
  private String fetchInformation(TLspDomainObjectContext aCandidate) {
    if (aCandidate != null) {
      // If the candidate has a data object, get some information in html.
      if (aCandidate.getObject() instanceof ILcdDataObject) {
        ILcdDataObject dataObject = (ILcdDataObject) aCandidate.getObject();
        String[] propertyNames = getPropertyNames(aCandidate.getLayer(), dataObject);
        return printInHtmlBlock(dataObject, propertyNames);
      } else {
        return aCandidate.getObject().toString();
      }
    }

    return "";
  }

  /**
   * Converts the properties of the given data objects to html text.
   *
   * @param aDataObject    the data objects.
   * @param aPropertyNames the properties to add.
   *
   * @return a html String representation of the given data object.
   */
  private String printInHtmlBlock(ILcdDataObject aDataObject, String[] aPropertyNames) {
    StringBuilder textBuilder = new StringBuilder();

    textBuilder.append("<html>");

    for (String propertyName : aPropertyNames) {
      TLcdDataProperty property = aDataObject.getDataType().getProperty(propertyName);
      Object value = aDataObject.getValue(property);
      textBuilder.append("<p>")
                 .append(property.getDisplayName())
                 .append(": <b>")
                 .append(value)
                 .append("</b>")
                 .append("</p>");
    }

    textBuilder.append("</html>");

    return textBuilder.toString();
  }

  /**
   * Return the property names to be used in the information panel.
   *
   * By default this method returns the first possible property.
   *
   * @param aLayer  the layer.
   * @param aObject the object.
   *
   * @return an array of property names.
   */
  protected String[] getPropertyNames(ILspLayer aLayer, ILcdDataObject aObject) {
    List<TLcdDataProperty> properties = aObject.getDataType().getProperties();
    return properties.isEmpty() ? new String[0] : new String[]{properties.get(0).getName()};
  }

  private static class MyLabel extends JLabel {

    public MyLabel() {
      setHorizontalAlignment(JLabel.CENTER);
    }

    @Override
    protected void paintComponent(Graphics g) {
      // Add a fill and frame color
      g.setColor(PANEL_FILL_COLOR);
      g.fillRect(0, 0, getSize().width, getSize().height);
      g.setColor(PANEL_FRAME_COLOR);
      g.drawRect(0, 0, getSize().width - 1, getSize().height - 1);
      super.paintComponent(g);
    }
  }

}

