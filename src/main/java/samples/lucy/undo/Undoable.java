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
package samples.lucy.undo;

import java.text.MessageFormat;

import com.luciad.gui.ALcdUndoable;
import com.luciad.lucy.util.language.TLcyLang;

/**
 * Extension of ALcdUndoable that uses the I18N features of Lucy to create the undo and
 * redo display names.
 */
public abstract class Undoable extends ALcdUndoable {
  public static final String LAYER_UNREFERENCED_MESSAGE = TLcyLang.getString("Layer was unreferenced due to memory constraints.");
  public static final String VIEW_UNREFERENCED_MESSAGE = TLcyLang.getString("View was unreferenced due to memory constraints.");
  public static final String MODEL_UNREFERENCED_MESSAGE = TLcyLang.getString("Model was unreferenced due to memory constraints.");
  public static final String DOMAIN_OBJECT_UNREFERENCED_MESSAGE = TLcyLang.getString("Domain object was unreferenced due to memory constraints.");

  protected Undoable(String aDisplayName) {
    super(aDisplayName,
          new MessageFormat(TLcyLang.getString("Undo {0}")),
          new MessageFormat(TLcyLang.getString("Redo {0}"))
         );
  }
}
