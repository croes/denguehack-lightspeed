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
package samples.decoder.kml22.common.modelcontenttree;

import javax.swing.JTree;
import javax.swing.tree.TreePath;
import java.util.Enumeration;

/**
 * Utility class for a <code>JTree</code>.
 */
public class TreeUtil {

  /**
   * Save the expansion state of a tree.
   *
   * @param aTree The tree to save.
   * @return expanded tree path as Enumeration
   */

  public static Enumeration saveExpansionState( JTree aTree ) {
    return aTree.getExpandedDescendants( new TreePath( aTree.getModel().getRoot() ) );
  }


  /**
   * Restore the expansion state of a JTree.
   *
   * @param aTree        The tree to restore.
   * @param aEnumeration an Enumeration of expansion state. You can get it using {@link #saveExpansionState(JTree)}.
   */
  public static void loadExpansionState( JTree aTree, Enumeration aEnumeration ) {
    if ( aEnumeration != null ) {
      while ( aEnumeration.hasMoreElements() ) {
        TreePath treePath = ( TreePath ) aEnumeration.nextElement();
        try {
          aTree.expandPath( treePath );
        }
        catch ( ArrayIndexOutOfBoundsException e ) {
          //don't expand if it doesn't exist anymore
        }
      }
    }
  }
}

