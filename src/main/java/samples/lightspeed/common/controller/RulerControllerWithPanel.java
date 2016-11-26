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
package samples.lightspeed.common.controller;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;
import javax.swing.JPanel;

import com.luciad.gui.swing.TLcdOverlayLayout;
import com.luciad.view.lightspeed.ILspAWTView;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.controller.ruler.TLspRulerController;

public class RulerControllerWithPanel extends TLspRulerController {

  RulerControllerPanel panel;

  public RulerControllerWithPanel() {
    super(true);
  }

  @Override
  public void startInteractionImpl(ILspView aView) {
    super.startInteractionImpl(aView);
    if (aView instanceof ILspAWTView) {
      panel = new RulerControllerPanel(this);
      Container overlayComponent = ((ILspAWTView) aView).getOverlayComponent();
      overlayComponent.add(panel, TLcdOverlayLayout.Location.NORTH);
      overlayComponent.validate();
      overlayComponent.repaint();
    }
  }

  @Override
  public void terminateInteraction(ILspView aView) {
    super.terminateInteraction(aView);
    if (aView instanceof ILspAWTView) {
      Container overlayComponent = ((ILspAWTView) aView).getOverlayComponent();
      overlayComponent.remove(panel);
      overlayComponent.validate();
      overlayComponent.repaint();
    }
    panel = null;
  }

  /**
   * This ruler panel allows to change a ruler's measurement mode.
   */
  private static class RulerControllerPanel extends JPanel {

    private static final String[] MODE_NAMES = {
        "Geodesic Distance",
        "Rhumbline Distance",
        "Cartesian Distance"
    };

    private static final MeasureMode[] MODES = {
        MeasureMode.MEASURE_GEODETIC,
        MeasureMode.MEASURE_RHUMB,
        MeasureMode.MEASURE_CARTESIAN
    };

    private JComboBox fComboBox;
    private TLspRulerController fRulerController;

    public RulerControllerPanel(TLspRulerController aRulerController) {
      fRulerController = aRulerController;
      initGUI();
    }

    /**
     * Returns the index in the combo box for the given measurement mode.
     *
     * @param aMeasureMode The measurement mode, defined by TLcdAdvancedMapRulerController
     * @return The index in the combo box.
     */
    private int findIndexForMeasureMode(MeasureMode aMeasureMode) {
      for (int index = 0; index < MODES.length; index++) {
        MeasureMode mode = MODES[index];
        if (mode == aMeasureMode) {
          return index;
        }
      }
      return -1;
    }

    /**
     * Initializes the user interface. It creates the combo box with the different
     * measurement modes and the labels containing the distances and azimuth.
     */
    private void initGUI() {
      //Create the combo box containing the different measurement modes
      fComboBox = new JComboBox(MODE_NAMES);
      fComboBox.setSelectedIndex(findIndexForMeasureMode(fRulerController.getMeasureMode()));
      //Add a listener to the combo box to set the new selected measurement mode to the controller
      fComboBox.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          fRulerController.setMeasureMode(MODES[fComboBox.getSelectedIndex()]);
        }
      });
      setLayout(new BorderLayout());
      add(BorderLayout.NORTH, fComboBox);
    }

  }
}
