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
package samples.gxy.projections;

import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JComboBox;

import com.luciad.gui.ILcdAction;
import com.luciad.projection.*;
import com.luciad.reference.ILcdGridReference;
import com.luciad.util.TLcdInterval;
import com.luciad.view.gxy.ILcdGXYView;

/**
 * This is a Swing JComboBox allowing to select an ILcdProjection and set it to
 * a given ILcdGXYView. This is done by means of SetProjectionAction.
 */
public class ProjectionComboBox extends JComboBox {

  private static final int INITIAL_PROJECTION_INDEX = -1;

  MyItemListener fMyItemListener = new MyItemListener();
  SetProjectionAction[] fActions;

  /**
   * Creates a new combobox for the given view.
   */
  public ProjectionComboBox(ILcdGXYView aGXYView) {
    this(aGXYView, INITIAL_PROJECTION_INDEX);
  }

  /**
   * Creates a new combobox for the given view, selecting the given projection.
   * @param aDefaultIndex the index of the desired projection, or -1
   *                      to take the currently configured projection
   */
  public ProjectionComboBox(ILcdGXYView aGXYView, int aDefaultIndex) {
    fActions = new SetProjectionAction[]{
        // Note: SetProjectionAction is part of this sample package
        new SetProjectionAction(new TLcdEquidistantCylindrical(),
                                "Equidistant cylindrical",
                                aGXYView),
        new SetProjectionAction(new TLcdOrthographic(),
                                "Orthographic",
                                aGXYView),
        new SetProjectionAction(new TLcdGnomonic(),
                                "Gnomonic",
                                aGXYView),
        new SetProjectionAction(new TLcdPolarStereographic(TLcdPolarStereographic.NORTH_POLE),
                                "Polar Stereographic (North)",
                                aGXYView),
        new SetProjectionAction(new TLcdPolarStereographic(TLcdPolarStereographic.SOUTH_POLE),
                                "Polar Stereographic (South)",
                                aGXYView),
        new SetProjectionAction(new TLcdStereographic(),
                                "Stereographic",
                                aGXYView),
        new SetProjectionAction(createMercator(),
                                "Mercator",
                                aGXYView),
        new SetProjectionAction(createPseudoMercator(),
                                "Pseudo Mercator",
                                aGXYView),
        new SetProjectionAction(new TLcdObliqueMercator(),
                                "Oblique Mercator",
                                aGXYView),
        new SetProjectionAction(new TLcdTransverseMercator(),
                                "Transverse Mercator",
                                aGXYView),
        new SetProjectionAction(new TLcdLambertConformal(),
                                "Lambert Conformal",
                                aGXYView),
        new SetProjectionAction(new TLcdCassini(),
                                "Cassini",
                                aGXYView),
        new SetProjectionAction(new TLcdVerticalPerspective(),
                                "Vertical Perspective",
                                aGXYView),
    };
    for (SetProjectionAction fAction : fActions) {
      addItem(fAction);
    }
    // add a ItemListener to trigger the SetProjectionAction when another
    // item is selected within the JComboBox
    this.addItemListener(fMyItemListener);
    // set the desired projection
    if (aDefaultIndex > 0) {
      this.setSelectedIndex(aDefaultIndex);
    } else {
      ILcdGridReference worldReference = (ILcdGridReference) aGXYView.getXYWorldReference();
      Class<? extends ILcdProjection> projectionClass = worldReference.getProjection().getClass();
      boolean found = false;
      // prefer exact class matches
      for (int index = 0; !found && index < fActions.length; index++) {
        if (projectionClass == fActions[index].getProjection().getClass()) {
          this.setSelectedIndex(index);
          found = true;
        }
      }
      // fall back to inheritance
      for (int index = 0; !found && index < fActions.length; index++) {
        if (projectionClass.isAssignableFrom(fActions[index].getProjection().getClass())) {
          this.setSelectedIndex(index);
          found = true;
        }
      }
    }
    
    setMaximumRowCount(25); // avoid scroll bar in combo box
    setMaximumSize(getPreferredSize());
  }

  private static ILcdProjection createMercator() {
    TLcdMercator mercator = new TLcdMercator();
    mercator.setLatitudeLimits(new TLcdInterval(-85.0, 85.0));
    return mercator;
  }

  private static ILcdProjection createPseudoMercator() {
    TLcdPseudoMercator pseudoMercator = new TLcdPseudoMercator();
    pseudoMercator.setLatitudeLimits(new TLcdInterval(-85.0, 85.0));
    return pseudoMercator;
  }

  private static class MyItemListener implements ItemListener {
    // This method defines the action to be performed when an item in the
    // JComboBox has been selected.
    public void itemStateChanged(ItemEvent e) {
      SetProjectionAction action = (SetProjectionAction) ((JComboBox) e.getSource()).getSelectedItem();
      ActionEvent action_event = new ActionEvent(this,
                                                 ActionEvent.ACTION_PERFORMED,
                                                 (String) action.getValue(ILcdAction.NAME));
      action.actionPerformed(action_event);
    }
  }
}
