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
package samples.lucy.lightspeed.map.guifactory;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;

import com.luciad.geodesy.TLcdGeodeticDatum;
import com.luciad.gui.swing.TLcdOverlayLayout;
import com.luciad.lucy.ILcyLucyEnv;
import com.luciad.lucy.gui.ILcyActiveSettable;
import com.luciad.lucy.gui.TLcyActionBarUtil;
import com.luciad.lucy.gui.TLcyCommonWidgetFactory;
import com.luciad.lucy.map.action.lightspeed.TLcyLspProjectionActiveSettable;
import com.luciad.lucy.map.lightspeed.ILcyLspMapComponent;
import com.luciad.lucy.map.lightspeed.TLcyLspMapComponentFactory;
import com.luciad.lucy.util.language.TLcyLang;
import com.luciad.lucy.util.properties.ALcyProperties;
import com.luciad.projection.TLcdGnomonic;
import com.luciad.reference.TLcdGridReference;
import com.luciad.view.lightspeed.controller.ILspController;

/**
 * This map component factory is an extension of
 * <code>TLcyLspMapComponentFactory</code> that slightly modifies the behaviour.
 *
 * Note that the ruler controller has been removed, the toolbar is put below the
 * map, an (interactive) overlay panel is added to the top left, an extra layer
 * is added to the map and a custom projection is added.
 */
public class LspMapComponentFactory extends TLcyLspMapComponentFactory {
  // Some arbitrary value above 10000
  private static final int GNOMONIC_PROJECTION_ACTIVE_SETTABLE_ID = 10999;

  public LspMapComponentFactory(ILcyLucyEnv aLucyEnv) {
    super(aLucyEnv);

    //Install a custom TLcyMapLayerControlFactory.
    setMapLayerControlFactory(new LspMapLayerControlFactory(aLucyEnv));

    // Add an ID for the Gnomonic active settable, as such the createActiveSettable method
    // will get called with that ID.
    getActiveSettableIDs().add(GNOMONIC_PROJECTION_ACTIVE_SETTABLE_ID);
  }

  @Override
  protected ILcyActiveSettable createActiveSettable(int aActiveSettableID, ALcyProperties aProperties) {
    if (aActiveSettableID == GNOMONIC_PROJECTION_ACTIVE_SETTABLE_ID) {

      // Create an active settable that enables the Gnomonic projection
      TLcdGridReference gnomonicReference = new TLcdGridReference(new TLcdGeodeticDatum(), new TLcdGnomonic());
      TLcyLspProjectionActiveSettable as = new TLcyLspProjectionActiveSettable(getView(), gnomonicReference, false);

      // Tell the action its ID, used to parse the settings from the config file
      as.putValue(TLcyActionBarUtil.ID_KEY, "TLcyLspMapAddOn.gnomonicProjectionActiveSettable");

      return as;
    } else {
      return super.createActiveSettable(aActiveSettableID, aProperties);
    }
  }

  @Override
  protected boolean isActiveSettableDeactivatePossible(int aActiveSettableID, ALcyProperties aProperties) {
    if (aActiveSettableID == GNOMONIC_PROJECTION_ACTIVE_SETTABLE_ID) {
      return false; // Projections can only be deactivated by activating another one
    } else {
      return super.isActiveSettableDeactivatePossible(aActiveSettableID, aProperties);
    }
  }

  @Override
  protected ILspController createController(int aID, ALcyProperties aProperties) {
    //Remove the ruler controller.
    if (aID == RULER_CONTROLLER) {
      return null;
    }
    //Leave all other controllers untouched.
    else {
      return super.createController(aID, aProperties);
    }
  }

  @Override
  protected ILcyLspMapComponent createGUIContent(ALcyProperties aProperties) {
    ILcyLspMapComponent mapComponent = super.createGUIContent(aProperties);
    //Add an extra overlay panel to the map component.
    //This code can also be moved outside the factory by using a listener which adds
    //the overlay to the map component overlay panel for each created map component
    if (mapComponent != null &&
        mapComponent.getMapOverlayPanel() != null) {
      Container overlayPanel = mapComponent.getMapOverlayPanel();
      overlayPanel.add(createOverlay(), TLcdOverlayLayout.Location.NORTH_WEST);
    }
    return mapComponent;
  }

  @Override
  protected Component createPanel(int aPanelID, ALcyProperties aProperties) {
    if (aPanelID == NORTH_PANEL) {
      //Don't add the toolbar at the top.
      return null;
    } else if (aPanelID == SOUTH_PANEL) {
      return super.createPanel(NORTH_PANEL, aProperties);
    } else {
      return super.createPanel(aPanelID, aProperties);
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

      //Anti aliasing enabled because otherwise the letters of the title show artifacts.
      @Override
      public void paint(Graphics g) {
        Graphics2D graphics2D = (Graphics2D) g;
        Object aa = graphics2D.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        super.paint(g);
        if (aa == null) {
          graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_DEFAULT);
        }
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, aa);
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

    bordered.setPreferredSize(new Dimension(200, 200));
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
