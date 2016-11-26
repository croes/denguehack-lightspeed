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
package samples.lucy.drawing.styles;

import static com.luciad.util.concurrent.TLcdLockUtil.Lock;
import static com.luciad.util.concurrent.TLcdLockUtil.writeLock;

import java.awt.event.ActionEvent;
import java.util.Enumeration;

import com.luciad.gui.TLcdCompositeUndoable;
import samples.lucy.undo.ModelElementChangedUndoable;
import samples.lucy.undo.UndoableAction;
import com.luciad.lucy.addons.drawing.format.ALcyDomainObjectSupplier;
import com.luciad.lucy.addons.drawing.format.TLcyDrawingFormat;
import com.luciad.lucy.addons.drawing.format.TLcyDrawingSymbolizerType;
import com.luciad.lucy.addons.drawing.format.TLcySLDDomainObject;
import com.luciad.lucy.addons.drawing.format.TLcySLDDomainObjectSupplier;
import com.luciad.lucy.util.language.TLcyLang;
import com.luciad.model.ILcdModel;
import com.luciad.ogc.sld.model.TLcdSLDFeatureTypeStyle;
import com.luciad.shape.ILcdShapeList;
import com.luciad.util.ILcdFireEventMode;
import com.luciad.view.ILcdLayer;

/**
 * {@link UndoableAction} that applies a SLD style to all selected
 * objects.
 */
public class ApplyStyleAction extends UndoableAction {

  private TLcdSLDFeatureTypeStyle fStyle;
  private ILcdLayer fLayer;
  private TLcyDrawingFormat fDrawingFormat;

  public ApplyStyleAction(ILcdLayer aLayer, TLcdSLDFeatureTypeStyle aStyle, TLcyDrawingFormat aDrawingFormat) {
    fStyle = aStyle;
    fLayer = aLayer;
    fDrawingFormat = aDrawingFormat;
  }

  protected void applyStyle() {
    ILcdModel model = fLayer.getModel();
    TLcdCompositeUndoable compositeUndoable = new TLcdCompositeUndoable(TLcyLang.getString("apply style"));
    try (Lock autoUnlock = writeLock(model)) {
      Enumeration<?> selection = fLayer.selectedObjects();
      while (selection.hasMoreElements()) {
        Object obj = selection.nextElement();
        if (obj instanceof TLcySLDDomainObject) {
          TLcySLDDomainObject dShape = (TLcySLDDomainObject) obj;
          TLcdCompositeUndoable shapeUndoable = new TLcdCompositeUndoable(TLcyLang.getString("apply style"));
          applyStyle(dShape, shapeUndoable);
          fLayer.getModel().elementChanged(dShape, ILcdFireEventMode.FIRE_LATER);
          shapeUndoable.finish();
          if (shapeUndoable.canUndo()) {
            // wrapping the undoable with a ModelElementChangedUndoable makes
            // sure that the model is appropriately locked and that model change
            // events are triggered when needed
            compositeUndoable.addUndoable(new ModelElementChangedUndoable(shapeUndoable, dShape, fLayer));
          }
        }
      }
    }
    compositeUndoable.finish();
    if (compositeUndoable.canUndo()) {
      fireUndoableHappened(compositeUndoable);
    } else {
      compositeUndoable.die();
    }
    model.fireCollectedModelChanges();
  }

  // recurses inside ILcdShapeLists
  protected void applyStyle(TLcySLDDomainObject aShape, TLcdCompositeUndoable aCompositeUndoable) {
    if (aShape.getDelegateShape() instanceof ILcdShapeList) {
      ILcdShapeList shapeList = (ILcdShapeList) aShape.getDelegateShape();
      for (int i = 0; i < shapeList.getShapeCount(); i++) {
        applyStyle((TLcySLDDomainObject) shapeList.getShape(i), aCompositeUndoable);
      }
    } else {
      TLcySLDDomainObjectSupplier supplier = findDomainObjectSupplier(aShape);

      if (supplier != null && supplier.getDrawingSymbolizerType() == TLcyDrawingSymbolizerType.POLYGON) {
        aCompositeUndoable.addUndoable(new ApplyStyleUndoable(aShape, fStyle));
        aShape.setStyle(fStyle);
      }
    }
  }

  @Override
  public void actionPerformed(ActionEvent aActionEvent) {
    applyStyle();
  }

  private TLcySLDDomainObjectSupplier findDomainObjectSupplier(TLcySLDDomainObject aDomainObject) {
    for (ALcyDomainObjectSupplier supplier : fDrawingFormat.getDomainObjectSuppliers()) {
      if (supplier.canHandle(aDomainObject) && supplier instanceof TLcySLDDomainObjectSupplier) {
        return (TLcySLDDomainObjectSupplier) supplier;
      }
    }
    return null;
  }
}
