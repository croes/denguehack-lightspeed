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
package samples.gxy.rectification;

import java.awt.Frame;
import java.awt.event.ActionEvent;

import com.luciad.gui.ALcdAction;
import com.luciad.gui.ILcdAction;
import com.luciad.gui.TLcdAWTUtil;
import com.luciad.gui.TLcdIconFactory;

import samples.gxy.rectification.util.OKCancelDialog;

/**
 * Displays a dialog box where the user can modify the type and parameters of the raster
 * referencer.
 */
public class EditRasterReferencerAction extends ALcdAction {

  private TiePointsRectifier fTiePointsContext;
  private RasterReferencerCustomizer fCustomizer = new RasterReferencerCustomizer();

  public EditRasterReferencerAction(TiePointsRectifier aTiePointsContext) {
    fTiePointsContext = aTiePointsContext;

    putValue(ILcdAction.SMALL_ICON, TLcdIconFactory.create(TLcdIconFactory.GRID_ICON));
    setShortDescription("Edit raster projection parameters.");
  }

  public void actionPerformed(ActionEvent aEvent) {
    if (fTiePointsContext.getTargetRasterModel() == null) {
      return;
    }

    fCustomizer.setRasterReferencer(fTiePointsContext.getRasterReferencer());

    Frame frame = TLcdAWTUtil.findParentFrame(aEvent);
    OKCancelDialog dialog = new OKCancelDialog(frame, fCustomizer);
    dialog.setVisible(true);

    if (!dialog.isCanceled()) {
      fTiePointsContext.setRasterReferencer(fCustomizer.getRasterReferencer());
      fTiePointsContext.updateRasterReference();
    }
    dialog.dispose();
  }
}
