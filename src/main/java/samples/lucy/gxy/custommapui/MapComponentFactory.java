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
package samples.lucy.gxy.custommapui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;

import com.luciad.gui.ILcdAction;
import com.luciad.gui.ILcdIcon;
import com.luciad.gui.TLcdIconFactory;
import com.luciad.gui.swing.TLcdOverlayLayout;
import com.luciad.lucy.gui.ILcyToolBar;
import com.luciad.lucy.gui.TLcyCommonWidgetFactory;
import com.luciad.lucy.gui.TLcyGroupDescriptor;
import com.luciad.lucy.map.TLcyMapComponent;
import com.luciad.lucy.map.TLcyMapComponentFactory;
import com.luciad.lucy.util.language.TLcyLang;
import com.luciad.view.gxy.ILcdGXYController;

/**
 * This map component factory is an extension of
 * <code>TLcyMapComponentFactory</code> that slightly modifies the behaviour.
 *
 * Note that the File|Open action has a different icon, that
 * the ruler controller was removed and that the toolbar is
 * now below the map.  The scale combo box is no longer
 * inside the toolbar, but at the far right side.  An
 * (interactive) overlay panel is added to the top left.
 */
public class MapComponentFactory extends TLcyMapComponentFactory {

  private ILcyToolBar fRightToolBar;

  public MapComponentFactory() {
    //install a custom TLcyMapLayerControlFactory
    setMapLayerControlFactory(new MapLayerControlFactory());
  }

  @Override
  protected void finalizeCreation(TLcyMapComponent aMapComponent) {
    super.finalizeCreation(aMapComponent);

    //don't store a reference to this component to help garbage collection
    fRightToolBar = null;

  }

  @Override
  protected Container createContainer(int aID, TLcyMapComponent aMapComponent) {
    Container container = super.createContainer(aID, aMapComponent);
    if (aID == SOUTH_CONTAINER) {
      container.setLayout(new BorderLayout());
    }
    return container;
  }

  @Override
  protected ILcdAction createAction(int aID, TLcyMapComponent aMapComponent) {
    ILcdAction action = super.createAction(aID, aMapComponent);

    //Modify the icon of the open action
    if (aID == OPEN_FILE_ACTION && action != null) {
      ILcdIcon new_icon = TLcdIconFactory.create(TLcdIconFactory.GLOBE_ICON);
      action.putValue(ILcdAction.SMALL_ICON, new_icon);
    }

    return action;
  }

  @Override
  protected ILcdGXYController createGXYController(
      int aID,
      TLcyMapComponent tLcyMapComponent) {

    //Remove the ruler controller
    if (aID == RULER_CONTROLLER) {
      return null;
    }
    //leave all other controllers untouched
    else {
      return super.createGXYController(aID, tLcyMapComponent);
    }
  }

  @Override
  protected void insertComponent(int aID, Component aComponent, TLcyMapComponent aMapComponentSFCT) {
    if (aID == MAP_SCALE_LABEL_COMPONENT) {
      //insert the map scale label at a different place, using swing
      if (aComponent != null) {
        fRightToolBar.insertComponent(aComponent, new TLcyGroupDescriptor("ScaleGroup"));
      }
    } else {
      super.insertComponent(aID, aComponent, aMapComponentSFCT);
    }
  }

  @Override
  protected void setToolBar(ILcyToolBar aToolBar, TLcyMapComponent aMapComponentSFCT) {
    //tell the map component about the toolbar it should use
    aMapComponentSFCT.setToolBar(aToolBar);
    aMapComponentSFCT.setToolBarComponent(aToolBar.getComponent());

    //add the toolbar (using swing) in a different location
    Container south_content_pane = aMapComponentSFCT.getSouthPanel();
    if (south_content_pane != null) {
      south_content_pane.add(aToolBar.getComponent(), BorderLayout.WEST);
    }
  }

  @Override
  protected void setRightToolBar(ILcyToolBar aRightToolBar, TLcyMapComponent aMapComponentSFCT) {
    //add the toolbar (using swing) in a different location
    Container south_content_pane = aMapComponentSFCT.getSouthPanel();
    if (south_content_pane != null) {
      south_content_pane.add(aRightToolBar.getComponent(), BorderLayout.EAST);
    }
  }

  @Override
  protected ILcyToolBar createRightToolBar(TLcyMapComponent aMapComponent) {
    fRightToolBar = super.createRightToolBar(aMapComponent);
    return fRightToolBar;
  }

  @Override
  protected void insertContainer(int aID, Container aContainer, TLcyMapComponent aMapComponentSFCT) {
    super.insertContainer(aID, aContainer, aMapComponentSFCT);
    if (aID == CENTER_CONTAINER) {
      aMapComponentSFCT.getMapOverlayPanel().add(createOverlay(),
                                                 TLcdOverlayLayout.Location.NORTH_WEST);
    }
  }

  private Component createOverlay() {
    // The panel that will contain the actual content
    JPanel content = new JPanel();
    content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

    // Add a title around the content
    Component titled = TLcyCommonWidgetFactory.getSharedInstance().createTitledPanel(
        TLcyLang.getString("Overlay"), content, new Insets(5, 10, 10, 10));

    // Add another panel around it, that contains the border
    final JPanel bordered = new JPanel(new BorderLayout()) {
      @Override
      protected void paintComponent(Graphics g) {
        Color c = g.getColor();
        g.setColor(new Color(255, 255, 255, 180));

        g.fillRect(0, 0, getWidth(), getHeight());
        g.setColor(c);
        super.paintComponent(g);
      }
    };

    bordered.add(titled, BorderLayout.CENTER);
    bordered.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.BLACK));

    //Make the titled panel and its children transparent (optional step)
    setOpaque(false, titled);

    // Now add the opaque content (JTree doesn't very well support being transparent)
    JTree tree = new JTree();
    content.add(new JScrollPane(tree));
    content.setPreferredSize(new Dimension(150, 150)); //use fixed size

    // This panel stays opaque, but has a transparent background color.
    // All its children that aren't opaque (e.g. 'titled') will therefore have this
    // background color as well (optional step)
    bordered.setOpaque(false);
    bordered.setBackground(new Color(255, 255, 255, 180)); //transparent white

    bordered.setBounds(40, 70, 200, 200);
    return bordered;
  }

  private void setOpaque(boolean aOpaque, Component aComponent) {
    if (aComponent instanceof JComponent) {
      JComponent jComponent = (JComponent) aComponent;
      jComponent.setOpaque(aOpaque);

      for (Component child : jComponent.getComponents()) {
        setOpaque(aOpaque, child);
      }
    }
  }
}
