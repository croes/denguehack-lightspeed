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

import java.lang.ref.SoftReference;
import java.text.MessageFormat;

import com.luciad.gui.ALcdUndoable;
import com.luciad.gui.TLcdCannotUndoRedoException;
import samples.lucy.undo.ModelElementChangedUndoable;
import com.luciad.lucy.addons.drawing.format.TLcySLDDomainObject;
import com.luciad.lucy.util.language.TLcyLang;
import com.luciad.ogc.sld.model.TLcdSLDFeatureTypeStyle;

/**
 * {@link ALcdUndoable} for applying styles. This undoable keeps track of the 
 * old and new style and the object to which the style should be applied.
 *
 * Note that the undoable doesn't need to concern itself with model locking
 * or model change notification. This is done by appropriate wrappers. See
 * for this the {@link ApplyStyleAction}.
 */
public class ApplyStyleUndoable extends ALcdUndoable {

  public static final MessageFormat UNDO_MESSAGE_FORMAT = new MessageFormat(TLcyLang.getString("Apply style {0}"));

  private SoftReference<TLcySLDDomainObject> fEditedObject;
  private TLcdSLDFeatureTypeStyle fOldStyle;
  private TLcdSLDFeatureTypeStyle fNewStyle;

  public ApplyStyleUndoable(TLcySLDDomainObject aEditedObject, TLcdSLDFeatureTypeStyle aNewStyle) {
    super(UNDO_MESSAGE_FORMAT.format(new Object[]{aNewStyle.getDescription().getTitle()}));
    fEditedObject = new SoftReference<TLcySLDDomainObject>(aEditedObject);
    fOldStyle = aEditedObject.getStyle();
    fNewStyle = aNewStyle;
  }

  @Override
  protected void undoImpl() throws TLcdCannotUndoRedoException {
    applyState(fOldStyle);
  }

  @Override
  protected void redoImpl() throws TLcdCannotUndoRedoException {
    applyState(fNewStyle);
  }

  private void applyState(TLcdSLDFeatureTypeStyle aStyle) {
    getEditedObject().setStyle(aStyle);
  }

  private TLcySLDDomainObject getEditedObject() {
    TLcySLDDomainObject domainObject = fEditedObject.get();
    if (domainObject == null) {
      throw new TLcdCannotUndoRedoException(ModelElementChangedUndoable.DOMAIN_OBJECT_UNREFERENCED_MESSAGE);
    }
    return domainObject;
  }

}
