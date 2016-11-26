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
package samples.realtime.common;

import static com.luciad.util.concurrent.TLcdLockUtil.Lock;
import static com.luciad.util.concurrent.TLcdLockUtil.readLock;

import java.awt.Graphics;
import java.awt.Rectangle;

import com.luciad.shape.ILcdBounds;
import com.luciad.util.ILcdFunction;
import com.luciad.util.TLcdNoBoundsException;
import com.luciad.view.gxy.ILcdGXYView;
import com.luciad.view.gxy.TLcdGXYLayer;

/**
 * This class extends <code>TLcdGXYLayer</code> to guard the painting of a model with a read lock.
 * <p/> This class is only needed when performing model changes outside of the Event Dispatch Thread
 * (EDT). If possible, always perform model changes in the EDT.
 */
public class LockedGXYLayer extends TLcdGXYLayer {

  public void paint(Graphics aGraphics, int aIndex, ILcdGXYView aILcdGXYView) {
    try (Lock autoUnlock = readLock(getModel())) {
      super.paint(aGraphics, aIndex, aILcdGXYView);
    }
  }

  public int applyOnInteract(ILcdFunction aFunction, Rectangle aBounds, boolean aStrictInteract, ILcdGXYView aGXYView) {
    try (Lock autoUnlock = readLock(getModel())) {
      return super.applyOnInteract(aFunction, aBounds, aStrictInteract, aGXYView);
    }
  }

  public int applyOnInteract(ILcdFunction aFunction, Graphics aGraphics, int aPaintMode, ILcdGXYView aGXYView) {
    try (Lock autoUnlock = readLock(getModel())) {
      return super.applyOnInteract(aFunction, aGraphics, aPaintMode, aGXYView);
    }
  }

  public int applyOnInteractLabels(ILcdFunction aLabelFunction, Graphics aGraphics, int aPaintMode, ILcdGXYView aGXYView) {
    try (Lock autoUnlock = readLock(getModel())) {
      return super.applyOnInteractLabels(aLabelFunction, aGraphics, aPaintMode, aGXYView);
    }
  }

  public ILcdBounds getBounds(int aMode, ILcdGXYView aGXYView) throws TLcdNoBoundsException {
    try (Lock autoUnlock = readLock(getModel())) {
      return super.getBounds(aMode, aGXYView);
    }
  }
}
