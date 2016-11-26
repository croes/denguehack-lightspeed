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
package samples.gxy.common;

import java.awt.Point;
import java.awt.event.MouseEvent;

import com.luciad.view.gxy.ILcdGXYLayerSubsetList;
import com.luciad.view.gxy.ILcdGXYView;
import com.luciad.view.gxy.controller.TLcdGXYEditController2;
import com.luciad.view.gxy.controller.TLcdGXYEditControllerModel2;

/**
 * Extension of TLcdGXYEditController2 that overrides its editWhatMode() method to make sure
 * only one label is edited at a time .
 */
public class FirstTouchedLabelEditController extends TLcdGXYEditController2 {

  private boolean fEditFirstTouchedLabelOnly = true;

  public boolean isEditFirstTouchedLabelOnly() {
    return fEditFirstTouchedLabelOnly;
  }

  public void setEditFirstTouchedLabelOnly(boolean aEditFirstTouchedLabelOnly) {
    fEditFirstTouchedLabelOnly = aEditFirstTouchedLabelOnly;
  }

  /**
   * This method is overridden because we only want to edit one label at a time.
   */
  protected int editWhatMode(ILcdGXYView aGXYView, ILcdGXYLayerSubsetList aSnappables,
                             Point aFrom, Point aTo,
                             MouseEvent aMouseEvent, int aEditHow) {
    int mode = super.editWhatMode(aGXYView, aSnappables, aFrom, aTo, aMouseEvent, aEditHow);
    if (fEditFirstTouchedLabelOnly && mode == TLcdGXYEditControllerModel2.EDIT_WHAT_LABELS) {
      mode = TLcdGXYEditControllerModel2.EDIT_WHAT_FIRST_TOUCHED_LABEL;
    }
    return mode;
  }

}
