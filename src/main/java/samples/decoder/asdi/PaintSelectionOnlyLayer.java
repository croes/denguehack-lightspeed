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
package samples.decoder.asdi;

import java.awt.Graphics;
import java.awt.Rectangle;

import com.luciad.model.ILcdModel;
import com.luciad.util.ILcdFunction;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.ILcdGXYView;

import samples.realtime.common.LockedGXYLayer;

/**
 * Extension of <code>LockedGXYLayer</code> that only paints the selected objects.
 */
public class PaintSelectionOnlyLayer extends LockedGXYLayer {

  public PaintSelectionOnlyLayer(ILcdModel aModel) {
    setModel(aModel);
    setLabel(aModel.getModelDescriptor().getDisplayName() + " (only selection visible)");
  }

  public int applyOnInteract(ILcdFunction aFunction, Rectangle aBounds, boolean aStrictInteract, ILcdGXYView aGXYView) {
    //No objects are visible in the given bounds, as this layer does not paint any objects
    return 0;
  }

  public void paint(Graphics aGraphics, int aMode, ILcdGXYView aGXYView) {
    //Only paint the selection
    if ((aMode & ILcdGXYLayer.SELECTION) != 0) {
      super.paint(aGraphics, aMode, aGXYView);
    }
  }
}
