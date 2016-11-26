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
package samples.decoder.aixmcommon;

import com.luciad.view.ILcdView;

import samples.common.action.ShowPropertiesAction;
import samples.common.dataObjectDisplayTree.DataObjectTreeCellRenderer;
import samples.common.dataObjectDisplayTree.ISOMeasureTreeCellRenderer;
import samples.decoder.aixm5.AIXM5TreeCellRenderer;
import samples.decoder.aixm51.AIXM51TreeCellRenderer;

import javax.swing.tree.TreeCellRenderer;
import java.awt.Component;

/**
 * Simple show properties action that adds formatting for AIXM 5.x.
 * 
 */
public class ShowAIXMPropertiesAction extends ShowPropertiesAction {

  /**
   * Overridden constructor that adds specific formatting.
   */
  public ShowAIXMPropertiesAction( ILcdView aView, Component aParentComponent ) {
    super( aView, aParentComponent, createCellRenderer(), null );
  }

  /**
   * 
   * @return A custom AIXM 5.x tree cell renderer.
   */
  private static TreeCellRenderer createCellRenderer() {
    DataObjectTreeCellRenderer cellRenderer = new DataObjectTreeCellRenderer();
    cellRenderer.addCellRenderer( new AIXM51TreeCellRenderer() );
    cellRenderer.addCellRenderer( new AIXM5TreeCellRenderer() );
    cellRenderer.addCellRenderer( new ISOMeasureTreeCellRenderer() );
    return cellRenderer;
  }

}
