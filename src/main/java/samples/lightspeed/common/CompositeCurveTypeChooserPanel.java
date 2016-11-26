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
package samples.lightspeed.common;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

import com.luciad.gui.TLcdIconFactory;
import com.luciad.gui.swing.TLcdSWIcon;
import com.luciad.view.lightspeed.editor.TLspCreateCurveEditorModel;

/**
 * A panel containing some togglebuttons to set the next curve that should be used while creating composite curves.
 */
public class CompositeCurveTypeChooserPanel extends JPanel {

  private final TLspCreateCurveEditorModel fCurveCreateModel;

  private final Icon BULGE_ICON;
  private final Icon BY_3_POINTS_ICON;
  private final Icon POLYLINE_ICON;

  public CompositeCurveTypeChooserPanel(TLspCreateCurveEditorModel aCurveCreateModel) {
    fCurveCreateModel = aCurveCreateModel;

    BULGE_ICON = new TLcdSWIcon(TLcdIconFactory.create(TLcdIconFactory.DRAW_ARC_BY_BULGE_ICON));
    BY_3_POINTS_ICON = new TLcdSWIcon(TLcdIconFactory.create(TLcdIconFactory.DRAW_ARC_BY_3_POINTS_ICON));
    POLYLINE_ICON = new TLcdSWIcon(TLcdIconFactory.create(TLcdIconFactory.DRAW_POLYGON_ICON));

    initPanel();
  }

  private void initPanel() {
    setLayout(new GridLayout(1, 3));

    ButtonGroup buttonGroup = new ButtonGroup();

    final JToggleButton circularArcByBulgeButton = new JToggleButton(BULGE_ICON, fCurveCreateModel.getNextType() == TLspCreateCurveEditorModel.Type.CIRCULAR_ARC_BY_BULGE);
    circularArcByBulgeButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        if (circularArcByBulgeButton.isSelected()) {
          fCurveCreateModel.setNextType(TLspCreateCurveEditorModel.Type.CIRCULAR_ARC_BY_BULGE);
        }
      }
    });
    final JToggleButton circularArcBy3PointsButton = new JToggleButton(BY_3_POINTS_ICON, fCurveCreateModel.getNextType() == TLspCreateCurveEditorModel.Type.CIRCULAR_ARC_BY_3_POINTS);
    circularArcBy3PointsButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        if (circularArcBy3PointsButton.isSelected()) {
          fCurveCreateModel.setNextType(TLspCreateCurveEditorModel.Type.CIRCULAR_ARC_BY_3_POINTS);
        }
      }
    });
    final JToggleButton polylineButton = new JToggleButton(POLYLINE_ICON, fCurveCreateModel.getNextType() == TLspCreateCurveEditorModel.Type.POLYLINE);
    polylineButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        if (polylineButton.isSelected()) {
          fCurveCreateModel.setNextType(TLspCreateCurveEditorModel.Type.POLYLINE);
        }
      }
    });

    buttonGroup.add(circularArcByBulgeButton);
    buttonGroup.add(circularArcBy3PointsButton);
    buttonGroup.add(polylineButton);

    add(circularArcByBulgeButton);
    add(circularArcBy3PointsButton);
    add(polylineButton);

  }
}
