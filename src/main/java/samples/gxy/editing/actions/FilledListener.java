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
package samples.gxy.editing.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;

import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.ILcdGXYView;
import com.luciad.view.gxy.TLcdGXYLayer;
import com.luciad.view.gxy.TLcdGXYShapePainter;

import samples.gxy.editing.ShapeGXYLayerFactory;

/**
 * Loops over all the view's layers and sets the fill mode if it finds a
 * supported painter.
 */
public class FilledListener implements ActionListener {
  private ILcdGXYView fGXYView;
  private int[] fTypes;

  public FilledListener(ILcdGXYView aGXYView, int[] aTypes) {
    fGXYView = aGXYView;
    fTypes = aTypes;
  }

  public void actionPerformed(ActionEvent e) {
    JComboBox cb = (JComboBox) e.getSource();
    for (int i = 0; i < fGXYView.layerCount(); i++) {
      ILcdGXYLayer layer = (ILcdGXYLayer) fGXYView.getLayer(i);
      TLcdGXYShapePainter painterEditor = ShapeGXYLayerFactory.retrieveGXYPainterEditor(layer);
      if (painterEditor != null) {
        painterEditor.setMode(fTypes[cb.getSelectedIndex()]);
        ((TLcdGXYLayer) layer).invalidate();
      }
    }
  }
}
