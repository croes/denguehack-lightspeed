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
package samples.gxy.balloon;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextPane;

import com.luciad.geodesy.TLcdGeodeticDatum;
import com.luciad.gui.TLcdImageIcon;
import com.luciad.gui.TLcdSymbol;
import com.luciad.gui.swing.TLcdOverlayLayout;
import com.luciad.gui.swing.TLcdSWIcon;
import samples.common.BalloonViewSelectionListener;
import com.luciad.model.ILcdModel;
import com.luciad.model.TLcdModelDescriptor;
import com.luciad.model.TLcdVectorModel;
import com.luciad.reference.TLcdGeodeticReference;
import com.luciad.shape.ILcdBounds;
import com.luciad.shape.shape2D.TLcdLonLatBounds;
import com.luciad.shape.shape2D.TLcdLonLatPoint;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.TLcdGXYLayer;
import com.luciad.view.gxy.TLcdGXYShapePainter;
import com.luciad.view.gxy.swing.TLcdGXYBalloonManager;
import com.luciad.view.map.TLcdGeodeticPen;

import samples.gxy.common.GXYSample;

/**
 * This sample demonstrates how to create your own balloons. The sample creates a hard-coded layer
 * for demonstration purposes. The layer contains 4 <code>BalloonDomainObject</code> elements, of
 * which 3 have a balloon attached to them.
 */
public class MainPanel extends GXYSample {

  private TLcdGXYBalloonManager fBalloonManager;

  protected void createGUI() {
    super.createGUI();
    //Create the balloon layer and add it to the view
    ILcdGXYLayer balloonLayer = createBalloonLayer();
    getView().addGXYLayer(balloonLayer);

    //Create a balloon manager with matching balloon content provider
    BalloonContentProvider balloonContentProvider = new BalloonContentProvider();
    fBalloonManager = new TLcdGXYBalloonManager(getView(), getOverlayPanel(), TLcdOverlayLayout.Location.NO_LAYOUT, balloonContentProvider);

    //Add a selection listener to the view that will create a balloon when a selection occurs.
    BalloonViewSelectionListener listener = new BalloonViewSelectionListener(getView(), fBalloonManager);
    getView().addLayerSelectionListener(listener);
    //Make balloons disappear if the layer becomes invisible or if the layer is removed.
    getView().getRootNode().addHierarchyLayeredListener(listener);
    getView().getRootNode().addHierarchyPropertyChangeListener(listener);
  }

  @Override
  protected ILcdBounds getInitialBounds() {
    return new TLcdLonLatBounds(-10.00, 34.00, 30.00, 30.00);
  }

  private ILcdGXYLayer createBalloonLayer() {
    TLcdGXYLayer layer = new TLcdGXYLayer();

    // We create our custom ILcdModel and set it to our ILcdGXYLayer.
    layer.setModel(createBalloonModel());

    // We set the other aspects of our ILcdGXYLayer
    layer.setGXYPen(new TLcdGeodeticPen());
    layer.setSelectable(true);
    layer.setLabel("Balloons");

    // We create an ILcdGXYPainter (com.luciad.view.gxy.ILcdGXYPainter)
    // that is able to paint a point as an airplane.
    TLcdGXYShapePainter painter = new TLcdGXYShapePainter();
    painter.setIcon(new TLcdImageIcon("images/mif/mif32_firetruck.gif"));
    painter.setSelectedIcon(new TLcdSymbol(TLcdSymbol.CIRCLE, 22, Color.red));
    layer.setGXYPainterProvider(painter);
    layer.setGXYEditorProvider(painter);
    return layer;
  }

  /**
   * Creates an ILcdModel with <code>BalloonDomainObject</code> instances as domain objects.
   * @return An ILcdModel with 4 <code>BalloonDomainObject</code> instances.
   */
  private ILcdModel createBalloonModel() {
    TLcdVectorModel model = new TLcdVectorModel();
    TLcdGeodeticDatum datum = new TLcdGeodeticDatum();
    model.setModelReference(new TLcdGeodeticReference(datum));
    model.setModelDescriptor(new TLcdModelDescriptor("Sample code", "BalloonType", "BalloonModel"));
    model.addElement(new BalloonDomainObject(new TLcdLonLatPoint(0, 42), createButtonBalloonComponent()), ILcdModel.FIRE_LATER);
    model.addElement(new BalloonDomainObject(new TLcdLonLatPoint(3, 41), createImageBalloonComponent()), ILcdModel.FIRE_LATER);
    model.addElement(new BalloonDomainObject(new TLcdLonLatPoint(5, 47), createLabelBalloonComponent()), ILcdModel.FIRE_LATER);
    model.addElement(new BalloonDomainObject(new TLcdLonLatPoint(-1, 39), null), ILcdModel.FIRE_LATER);
    model.fireCollectedModelChanges();
    return model;
  }

  /**
   * Creates a <code>JComponent</code> with some HTML text.
   * @return a <code>JComponent</code>
   */
  private JComponent createLabelBalloonComponent() {
    JTextPane textLabel = new JTextPane() {
      @Override
      public Dimension getPreferredSize() {
        Dimension preferredSize = super.getPreferredSize();
        // Make sure the initial balloon size is not too wide.
        // The balloon manager respects the preferred size of balloon components.
        return new Dimension(Math.min(250, preferredSize.width), preferredSize.height);
      }
    };
    textLabel.setContentType("text/html");
    textLabel.setText("<html><body>" +
                      "<p><b>This example</b> demonstrates the use " +
                      " of balloons. Of the four domain objects " +
                      " only three have a balloon attached with " +
                      " various <i>JComponents</i>.</p>" +
                      " <p><b>Balloons</b> are only displayed when " +
                      " a single object is selected.</p>" +
                      "</body></html>");
    textLabel.setBackground(new Color(190, 96, 224));
    return textLabel;
  }

  /**
   * Creates a <code>JComponent</code> with an image.
   * @return a <code>JComponent</code>.
   */
  private JComponent createImageBalloonComponent() {
    return new JLabel(new TLcdSWIcon(new TLcdImageIcon("images/mif/mif20_airplane.gif")));
  }

  /**
   * Creates a <code>JComponent</code> with a close button.
   * @return a <code>JComponent</code>
   */
  private JComponent createButtonBalloonComponent() {
    JButton button = new JButton("Close this balloon", new TLcdSWIcon(new TLcdImageIcon("images/mif/mif32_inte.gif")));
    button.addActionListener(new MyBalloonCloseListener());
    return button;
  }

  // Main method
  public static void main(final String[] aArgs) {
    startSample(MainPanel.class, "Balloon");
  }

  /**
   * An inner class that can close the active balloon.
   */
  private class MyBalloonCloseListener implements ActionListener {

    public void actionPerformed(ActionEvent e) {
      fBalloonManager.setBalloonDescriptor(null);
    }
  }
}
