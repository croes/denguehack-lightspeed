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
package samples.lucy.treetableview;

import javax.swing.SwingConstants;

import com.luciad.datamodel.TLcdDataProperty;
import samples.lucy.tableview.TableEditorCustomizerPanelAdapter;
import com.luciad.lucy.gui.customizer.ILcyCustomizerPanel;
import com.luciad.lucy.gui.customizer.dataproperty.ALcyDataPropertyCustomizerPanel;
import com.luciad.view.ILcdLayer;

/**
 * Extension of the TableEditorCustomizerPanelAdapter. Enforces a left alignment for all editors.
 */
class TreeTableEditorCustomizerPanelAdapter extends TableEditorCustomizerPanelAdapter {

  public TreeTableEditorCustomizerPanelAdapter(ILcyCustomizerPanel aCustomizerPanel,
                                               ILcdLayer aLayer,
                                               TLcdDataProperty aDataProperty) {
    super(aCustomizerPanel, aLayer, aDataProperty);
  }

  @Override
  protected void handleAlignment(ALcyDataPropertyCustomizerPanel aCustomizerPanel, TLcdDataProperty aDataProperty) {
    //Everything left!
    aCustomizerPanel.putValue(ILcyCustomizerPanel.HORIZONTAL_ALIGNMENT_HINT, SwingConstants.LEFT);
  }
}
