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
package samples.lightspeed.demo.application.data.los;

import static com.luciad.util.concurrent.TLcdLockUtil.Lock;
import static com.luciad.util.concurrent.TLcdLockUtil.readLock;

import java.util.Enumeration;
import java.util.List;

import com.luciad.model.ILcdModel;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.controller.manipulation.TLspEditController;
import com.luciad.view.lightspeed.controller.manipulation.TLspEditHandleStyler;
import com.luciad.view.lightspeed.editor.TLspHandleGeometryType;
import com.luciad.view.lightspeed.layer.ILspInteractivePaintableLayer;
import com.luciad.view.lightspeed.layer.TLspDomainObjectContext;
import com.luciad.view.lightspeed.layer.TLspPaintRepresentationState;
import com.luciad.view.lightspeed.style.ALspStyle;

/**
 * A special edit controller that includes layer, of which the object should all be editing candidates.
 * This class is used by LOS to prevent having to click on the LOS input shape before moving it around.
 */
public class LOSEditController extends TLspEditController {

  private ILspInteractivePaintableLayer fLayer;

  /**
   * Constructs a <code>LOSEditController</code> that always returns the objects in the given layer.
   * @param aLayer the layer in which the objects are present, that should always be editing candidates
   *
   */
  public LOSEditController(ILspInteractivePaintableLayer aLayer) {
    fLayer = aLayer;
    ((TLspEditHandleStyler) getHandleStyler()).setStyles(TLspHandleGeometryType.OUTLINE, (List<ALspStyle>) null);
    ((TLspEditHandleStyler) getFocusHandleStyler()).setStyles(TLspHandleGeometryType.OUTLINE, (List<ALspStyle>) null);
  }

  /**
   * Adds the set list of objects to the editing candidates list if their parent layer is visible.
   * @param aView the view in which to look for editing candidates
   * @return a list of editing candidates that includes all objects from the given layer (if it was visible.)
   */
  protected List<TLspDomainObjectContext> getEditingCandidates(ILspView aView) {
    List<TLspDomainObjectContext> editingCandidates = super.getEditingCandidates(aView);
    if (fLayer != null && fLayer.isVisible()) {
      final ILcdModel model = fLayer.getModel();
      try (Lock autoUnlock = readLock(model)) {
        Enumeration elements = model.elements();
        while (elements.hasMoreElements()) {
          Object object = elements.nextElement();
          editingCandidates.add(new TLspDomainObjectContext(object, aView, fLayer, TLspPaintRepresentationState.REGULAR_BODY));
        }
      }
    }
    return editingCandidates;
  }
}
