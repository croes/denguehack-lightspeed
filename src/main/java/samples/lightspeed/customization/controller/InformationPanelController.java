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
package samples.lightspeed.customization.controller;

import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.Timer;

import com.luciad.datamodel.ILcdDataObject;
import com.luciad.datamodel.TLcdDataProperty;
import com.luciad.gui.TLcdIconFactory;
import com.luciad.gui.swing.TLcdOverlayLayout;
import com.luciad.shape.ILcdPoint;
import com.luciad.shape.shape2D.TLcdXYPoint;
import com.luciad.view.lightspeed.ILspAWTView;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.TLspContext;
import com.luciad.view.lightspeed.controller.ALspController;
import com.luciad.view.lightspeed.layer.ALspTouchInfo;
import com.luciad.view.lightspeed.layer.ILspInteractivePaintableLayer;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.TLspDomainObjectContext;
import com.luciad.view.lightspeed.layer.TLspPaintRepresentation;
import com.luciad.view.lightspeed.layer.TLspPaintRepresentationState;
import com.luciad.view.lightspeed.layer.TLspPaintState;
import com.luciad.view.lightspeed.query.TLspPaintedObjectsTouchQuery;

/**
 * This controller adds an information panel for the first touched object under the mouse cursor.
 */
public class InformationPanelController extends ALspController {

  private static final int PANEL_WIDTH_OFFSET = 10;
  private static final int PANEL_HEIGHT_OFFSET = 6;
  private static final Color PANEL_FILL_COLOR = Color.WHITE;
  private static final Color PANEL_FRAME_COLOR = Color.BLACK;

  private static final int MOVEMENT_DELAY_IN_MILLISECONDS = 100;

  // The label.
  private JLabel fLabel;

  // Used for movePanel()
  private Timer fTimer = null;
  private int fX, fY;

  public InformationPanelController() {
    setName("Information Panels");
    setShortDescription("Show information panels for objects under the mouse...");
    setIcon(TLcdIconFactory.create(TLcdIconFactory.FIXED_IMAGE_LAYER_ICON));
    setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
  }

  /**
   * Adds the component to the specified view and repaints the view.
   *
   * @param aComponent the component.
   * @param aView      the view.
   */
  protected void addComponentToView(Component aComponent, ILspView aView) {
    if (aView instanceof ILspAWTView) {
      Container container = ((ILspAWTView) aView).getOverlayComponent();
      container.add(aComponent, TLcdOverlayLayout.Location.NO_LAYOUT);
      container.validate();
      container.repaint();
    } else if (aView instanceof Container) {
      Container container = (Container) aView;
      container.add(aComponent);
      container.validate();
      container.repaint();
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
      Container container = ((ILspAWTView) aView).getOverlayComponent();
      container.remove(aComponent);
      container.validate();
      container.repaint();
    } else  if (aView instanceof Container) {
      Container container = (Container) aView;
      container.remove(aComponent);
      container.validate();
      container.repaint();
    }
  }

  @Override
  protected void terminateInteractionImpl(ILspView aView) {
    super.terminateInteractionImpl(aView);
    // Remove the information panel when this controller is no longer active.
    removePanel();
  }

  @Override
  public AWTEvent handleAWTEventImpl(AWTEvent aAWTEvent) {
    // Handle mouse events only
    if (aAWTEvent instanceof MouseEvent) {
      MouseEvent event = (MouseEvent) aAWTEvent;

      if (event.getID() == MouseEvent.MOUSE_MOVED || event.getID() == MouseEvent.MOUSE_DRAGGED) {
        // move the information panel to the mouse cursor
        movePanel(event.getX(), event.getY());
      } else if (event.getID() == MouseEvent.MOUSE_EXITED) {
        // remove the information panel if the mouse is outside the view
        removePanel();
      }
    }

    // Do not consume events, in order to make it possible for other controllers to use them.
    return aAWTEvent;
  }

  /**
   * Schedules a movement of the information panel to the x and y coordinates specified. <p/>
   * Updates are scheduled and throttled to avoid retrieving the selection candidates too often. In
   * some cases retrieving the selection candidates can be costly.
   *
   * @param aX the new <i>x</i>-coordinate.
   * @param aY the new <i>y</i>-coordinate.
   */
  private synchronized void movePanel(int aX, int aY) {
    fX = aX;
    fY = aY;

    setPanelPosition(fX, fY);

    if (fTimer == null) {
      fTimer = new Timer(MOVEMENT_DELAY_IN_MILLISECONDS, new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
          fTimer.stop();
          fTimer = null;
          movePanelImpl();
        }
      });
      fTimer.setRepeats(false);
      fTimer.start();
    }
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
   */
  private synchronized void movePanelImpl() {
    TLspDomainObjectContext domainObjectContext = getTouchedDomainObject(fX, fY);

    if (domainObjectContext == null) {
      // Remove the label if it was added before.
      removePanel();
    } else {
      // Add the label if it does not exist yet.
      if (fLabel == null) {
        fLabel = new MyLabel();
        addComponentToView(fLabel, getView());
      }

      // Retrieve the information String.
      String information = fetchInformation(domainObjectContext);

      // Adjust the label.
      fLabel.setText(information);

      // Adjust the label position
      setPanelPosition(fX, fY);
    }
  }

  private TLspDomainObjectContext getTouchedDomainObject(int aX, int aY) {
    ILcdPoint point = new TLcdXYPoint(aX, aY);

    TLspPaintState[] paintStates = {TLspPaintState.EDITED, TLspPaintState.SELECTED, TLspPaintState.REGULAR};

    // Iterate over all layers
    Enumeration layers = getView().layersBackwards();
    while (layers.hasMoreElements()) {
      Object layerObject = layers.nextElement();
      if (layerObject instanceof ILspInteractivePaintableLayer) {
        ILspInteractivePaintableLayer layer = (ILspInteractivePaintableLayer) layerObject;
        TLspContext context = new TLspContext(layer, getView());

        // Iterate over its paint representations and paint states
        Collection<TLspPaintRepresentation> paintRepresentations = layer.getPaintRepresentations();
        for (TLspPaintRepresentation paintRepresentation : paintRepresentations) {
          for (TLspPaintState paintState : paintStates) {
            TLspPaintRepresentationState prs = TLspPaintRepresentationState.getInstance(paintRepresentation, paintState);

            // Query the touched objects
            Collection<ALspTouchInfo> result = layer.query(new TLspPaintedObjectsTouchQuery(prs, point, 1.0), context);
            if (!result.isEmpty()) {
              return new TLspDomainObjectContext(result.iterator().next().getDomainObject(), getView(), layer, prs);
            }
          }
        }
      }
    }

    return null;
  }

  /**
   * Adjusts the position of the panel
   * @param aX the x position
   * @param aY the y position
   */
  private void setPanelPosition(int aX, int aY) {
    if (fLabel != null) {
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
