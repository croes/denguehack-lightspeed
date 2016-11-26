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
package samples.gxy.decoder;

import com.luciad.view.ILcdInitialLayerIndexProvider;
import com.luciad.view.ILcdLayer;
import com.luciad.view.ILcdLayerTreeNode;

/**
 * Contrary to the default, this layer index provider adds new children to the bottom rather than to the top of the
 * layer stack. This ensures that layers are listed in order of appearance: the first layer is on top, while the last
 * layer is at the bottom.
 * <p/>
 * The implementation is a stateless singleton.
 *
 * @since 2013.1
 */
public class BottomInitialLayerIndexProvider implements ILcdInitialLayerIndexProvider {

  private static BottomInitialLayerIndexProvider INSTANCE = new BottomInitialLayerIndexProvider();

  private BottomInitialLayerIndexProvider() {
  }

  /**
   * Always returns 0, which means that new layers are added at the bottom.
   *
   * @param aNewChild the new child to be added
   * @param aParent the parent to add the new child to
   * @return always 0
   */
  @Override
  public int getInitialLayerIndex(ILcdLayer aNewChild, ILcdLayerTreeNode aParent) {
    // Always add new layers to the bottom, so that the first layer appears on top.
    return 0;
  }

  /**
   * Gets the singleton instance of this class.
   *
   * @return the singleton instance of this class
   */
  public static BottomInitialLayerIndexProvider getInstance() {
    return INSTANCE;
  }
}
