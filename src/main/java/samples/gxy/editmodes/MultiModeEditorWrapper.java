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
package samples.gxy.editmodes;

import java.awt.Graphics;
import java.beans.PropertyChangeListener;
import java.util.EnumMap;

import com.luciad.view.gxy.ILcdGXYContext;
import com.luciad.view.gxy.ILcdGXYEditor;
import com.luciad.view.gxy.ILcdGXYEditorProvider;

/**
 * Wraps and switches between {@link ILcdGXYEditor ILcdGXYEditor}s according to
 * the {@link MultiModeController.Mode Mode} of a {@link MultiModeController}.
 */
public class MultiModeEditorWrapper implements ILcdGXYEditor, ILcdGXYEditorProvider {

  private MultiModeController fController;
  private Object fObject;
  private EnumMap<MultiModeController.Mode, ILcdGXYEditor> fEditors;

  /**
   * Creates a new multi-mode editor wrapper.
   *
   * @param aController the multi-mode controller whose mode this wrapper needs to select the editor
   * @param aEditors the editors to be wrapped and the mode they need to be used in
   */
  public MultiModeEditorWrapper(MultiModeController aController, EnumMap<MultiModeController.Mode, ILcdGXYEditor> aEditors) {
    fController = aController;
    fEditors = aEditors;
  }

  @Override
  public ILcdGXYEditor getGXYEditor(Object aObject) {
    setObject(aObject);
    return this;
  }

  @Override
  public boolean acceptSnapTarget(Graphics aGraphics, ILcdGXYContext aGXYContext) {
    return retrieveDelegateEditor().acceptSnapTarget(aGraphics, aGXYContext);
  }

  @Override
  public void addPropertyChangeListener(PropertyChangeListener aPropertyChangeListener) {
    retrieveDelegateEditor().addPropertyChangeListener(aPropertyChangeListener);
  }

  @Override
  public boolean edit(Graphics aGraphics, int aMode, ILcdGXYContext aGXYContext) {
    return retrieveDelegateEditor().edit(aGraphics, aMode, aGXYContext);
  }

  @Override
  public int getCreationClickCount() {
    return retrieveDelegateEditor().getCreationClickCount();
  }

  @Override
  public String getDisplayName() {
    return retrieveDelegateEditor().getDisplayName();
  }

  @Override
  public Object getObject() {
    return fObject;
  }

  @Override
  public void removePropertyChangeListener(PropertyChangeListener aPropertyChangeListener) {
    retrieveDelegateEditor().removePropertyChangeListener(aPropertyChangeListener);
  }

  @Override
  public void setObject(Object aObject) {
    fObject = aObject;

    for (ILcdGXYEditor editor : fEditors.values()) {
      editor.setObject(aObject);
    }
  }

  @Override
  public Object clone() {
    try {
      MultiModeEditorWrapper clone = (MultiModeEditorWrapper) super.clone();
      clone.fEditors = fEditors.clone();
      return clone;
    } catch (CloneNotSupportedException e) {
      throw new RuntimeException("super.clone should be supported but it isn't.", e);
    }
  }

  // selects the editor according to the mode of the multi-mode edit controller.
  private ILcdGXYEditor retrieveDelegateEditor() {
    ILcdGXYEditor editor = fEditors.get(fController.getMode());

    if (editor == null && fController.getMode() != MultiModeController.Mode.DEFAULT) {
      return fEditors.get(MultiModeController.Mode.DEFAULT);
    }

    return editor;
  }
}
