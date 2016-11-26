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

import com.luciad.view.gxy.ILcdGXYContext;
import com.luciad.view.gxy.ILcdGXYEditor;
import com.luciad.view.gxy.ILcdGXYEditorProvider;

/**
 * Editor wrapper that adds rotation.
 * Object rotation is defined and retrieved using the RotatingPainterSupport class.
 * The model objects must implement ILcdShape.
 */
public class RotatingEditorWrapper implements ILcdGXYEditor, ILcdGXYEditorProvider {

  private ILcdGXYEditor fEditor;
  private ILcdGXYEditorProvider fEditorProvider;
  private Object fObject;

  private RotationSupport fObjectRotationSupport;

  public RotatingEditorWrapper(ILcdGXYEditor aEditor, RotationSupport aObjectRotationSupport) {
    fEditor = aEditor;
    fObjectRotationSupport = aObjectRotationSupport;
  }

  public RotatingEditorWrapper(ILcdGXYEditorProvider aEditorProvider, RotationSupport aObjectRotationSupport) {
    fEditorProvider = aEditorProvider;
    fObjectRotationSupport = aObjectRotationSupport;
  }

  public boolean edit(Graphics aGraphics, int aMode, ILcdGXYContext aGXYContext) {
    boolean result = privateEdit(getObject(), aMode, aGXYContext);
    return result || fEditor.edit(aGraphics, aMode, aGXYContext);
  }

  private boolean privateEdit(Object aObject, int aMode, ILcdGXYContext aGXYContext) {
    return fObjectRotationSupport.rotateObject(aObject, aGXYContext);
  }

  public int getCreationClickCount() {
    return fEditor.getCreationClickCount();
  }

  public boolean acceptSnapTarget(Graphics aGraphics, ILcdGXYContext aGXYContext) {
    return fEditor.acceptSnapTarget(aGraphics, aGXYContext);
  }

  public void setObject(Object aObject) {
    fObject = aObject;
    fEditor.setObject(aObject);
  }

  public Object getObject() {
    return fObject;
  }

  public String getDisplayName() {
    return fEditor.getDisplayName();
  }

  public void addPropertyChangeListener(PropertyChangeListener aPropertyChangeListener) {
    fEditor.addPropertyChangeListener(aPropertyChangeListener);
  }

  public void removePropertyChangeListener(PropertyChangeListener aPropertyChangeListener) {
    fEditor.removePropertyChangeListener(aPropertyChangeListener);
  }

  public ILcdGXYEditor getGXYEditor(Object aObject) {
    if (fEditorProvider != null) {
      fEditor = fEditorProvider.getGXYEditor(aObject);
    }
    if (aObject != getObject()) {
      setObject(aObject);
    }
    return this;
  }

  public Object clone() {
    try {
      RotatingEditorWrapper clone = (RotatingEditorWrapper) super.clone();
      // deeply cloned state
      clone.fEditor = (ILcdGXYEditor) fEditor.clone();
      clone.fEditorProvider = (ILcdGXYEditorProvider) fEditorProvider.clone();
      // transient state
      clone.fObjectRotationSupport = (RotationSupport) fObjectRotationSupport.clone();
      return clone;
    } catch (CloneNotSupportedException e) {
      throw new RuntimeException("super.clone should be supported but it isn't.", e);
    }
  }

}
