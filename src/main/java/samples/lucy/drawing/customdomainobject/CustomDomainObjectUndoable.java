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
package samples.lucy.drawing.customdomainobject;

import com.luciad.gui.ALcdUndoable;
import com.luciad.gui.TLcdCannotUndoRedoException;
import com.luciad.shape.ILcdPoint;
import com.luciad.shape.shape2D.TLcdLonLatPoint;

/**
 * {@link ALcdUndoable} that is capable of undoing changes to 
 * {@link CustomDomainObject} instances.
 *
 * This undoable will be wrapped in other undoable that take care of locking
 * and model notification, so we don't need to be concerned with that here. 
 */
public class CustomDomainObjectUndoable extends ALcdUndoable {

  private ILcdPoint fOld;
  private ILcdPoint fNew;
  private CustomDomainObject fTarget;

  public CustomDomainObjectUndoable(CustomDomainObject aTarget, ILcdPoint aNewLocation) {
    super("change location");
    fOld = new TLcdLonLatPoint(aTarget.getLon(), aTarget.getLat());
    fNew = new TLcdLonLatPoint(aNewLocation.getX(), aNewLocation.getY());
    fTarget = aTarget;
  }

  @Override
  protected void redoImpl() throws TLcdCannotUndoRedoException {
    apply(fNew);
  }

  @Override
  protected void undoImpl() throws TLcdCannotUndoRedoException {
    apply(fOld);
  }

  protected void apply(ILcdPoint aPoint) {
    fTarget.move2D(aPoint);
  }

}
