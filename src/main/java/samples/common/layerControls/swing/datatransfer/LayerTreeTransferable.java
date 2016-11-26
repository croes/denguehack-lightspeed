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
package samples.common.layerControls.swing.datatransfer;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.tree.TreeModel;

import com.luciad.view.ILcdLayer;

/**
 * <p>Transferable for the <code>TLcdLayerTree</code>.</p>
 */
public class LayerTreeTransferable implements Transferable {
  /**
   * Array containing the supported data flavors
   */
  public final static DataFlavor[] SUPPORTED_FLAVORS = new DataFlavor[]{LayerTreeTransferHandler.LAYER_TREE_FLAVOR};

  private TransferableData fData;

  /**
   * <p>Create a new transferable for the layers contained in <code>aDraggedTreeNodes</code>.</p>
   *
   * @param aDraggedTreeNodes list containing all the layers to create this transferable for
   */
  public LayerTreeTransferable(ArrayList<ILcdLayer> aDraggedTreeNodes) {
    fData = new TransferableData(aDraggedTreeNodes);
  }

  /**
   * <p>Create a new transferable for the layers contained in <code>aDraggedTreeNodes</code> from
   * the source model <code>aSourceTreeModel</code>.</p>
   *
   * @param aDraggedTreeNodes list containing all the layers to create this transferable for
   * @param aSourceTreeModel  the source model of the layers
   */
  public LayerTreeTransferable(ArrayList<ILcdLayer> aDraggedTreeNodes, TreeModel aSourceTreeModel) {
    fData = new TransferableData(aDraggedTreeNodes, aSourceTreeModel);
  }

  public DataFlavor[] getTransferDataFlavors() {
    return SUPPORTED_FLAVORS;
  }

  public boolean isDataFlavorSupported(DataFlavor flavor) {
    return flavor.equals(LayerTreeTransferHandler.LAYER_TREE_FLAVOR);
  }

  public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
    if (flavor.equals(LayerTreeTransferHandler.LAYER_TREE_FLAVOR)) {
      return fData;
    } else {
      throw new UnsupportedFlavorException(flavor);
    }
  }

}
