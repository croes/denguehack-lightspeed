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
package samples.common.dataObjectDisplayTree;

/**
 * An interface for customized cell renders in a <code>DataObjectTreeCellRenderer</code>
 * @see samples.common.dataObjectDisplayTree.DataObjectTreeCellRenderer
 */
public interface DataTypeCellRenderer {
  /**
   * Determines whether this <code>DataTypeCellRenderer</code> can render
   * a given <code>DataObjectTreeNode</code> or not.
   * @param aDataObjectTreeNode An <code>DataObjectTreeNode</code> to check
   * @return true if this <code>DataTypeCellRenderer</code> can render
   * the given <code>DataObjectTreeNode</code> false otherwise.
   */
  boolean canRender(DataObjectTreeNode aDataObjectTreeNode);

  /**
   * Creates a string value for a given <code>DataObjectTreeNode</code>
   * @param aDataObjectTreeNode an <code>DataObjectTreeNode</code>
   * @return a string value for a given <code>DataObjectTreeNode</code>
   */
  String render(DataObjectTreeNode aDataObjectTreeNode);
}
