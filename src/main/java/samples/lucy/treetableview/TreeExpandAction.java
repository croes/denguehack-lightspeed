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

import java.awt.event.ActionEvent;

import org.jdesktop.swingx.JXTreeTable;

import com.luciad.gui.ALcdAction;
import com.luciad.gui.TLcdIconFactory;
import com.luciad.lucy.util.language.TLcyLang;

/**
 * An action listener that is capable of expanding or collapsing an entire tree
 */
class TreeExpandAction extends ALcdAction {
  private boolean fExpand;
  private JXTreeTable fTree;

  TreeExpandAction(boolean aExpand, JXTreeTable aTree) {
    if (aExpand) {
      setIcon(TLcdIconFactory.create(TLcdIconFactory.EXPAND_NODE_ICON));
      setShortDescription(TLcyLang.getString("Expand all nodes"));
    } else {
      setIcon(TLcdIconFactory.create(TLcdIconFactory.COLLAPSE_NODE_ICON));
      setShortDescription(TLcyLang.getString("Collapse all nodes"));
    }
    fExpand = aExpand;
    fTree = aTree;
  }

  public void expandAll(JXTreeTable aTree, boolean aExpand) {
    if (aExpand) {
      for (int i = 0; i < aTree.getRowCount(); i++) {
        aTree.expandRow(i);
      }
    } else {
      for (int i = aTree.getRowCount() - 1; i >= 0; i--) {
        aTree.collapseRow(i);
      }
    }
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    expandAll(fTree, fExpand);
  }
}
