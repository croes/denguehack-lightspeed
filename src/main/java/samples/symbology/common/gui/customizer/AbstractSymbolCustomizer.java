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
package samples.symbology.common.gui.customizer;

import static com.luciad.util.concurrent.TLcdLockUtil.Lock;
import static com.luciad.util.concurrent.TLcdLockUtil.writeLock;

import java.util.Objects;

import javax.swing.JComponent;

import com.luciad.gui.ALcdUndoable;
import com.luciad.gui.TLcdCannotUndoRedoException;
import com.luciad.gui.TLcdUndoManager;
import samples.symbology.common.EMilitarySymbology;
import samples.symbology.common.util.MilitarySymbolFacade;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelListener;
import com.luciad.model.TLcdModelChangedEvent;
import com.luciad.util.ILcdChangeListener;
import com.luciad.util.ILcdChangeSource;
import com.luciad.util.ILcdDisposable;
import com.luciad.util.ILcdStringTranslator;
import com.luciad.util.TLcdChangeEvent;
import com.luciad.util.TLcdChangeSupport;

/**
 * An abstract GUI component that modifies a military symbol.
 * The component is automatically updated when the model changes.
 * <p/>
 * When extending this class, implement the following methods:
 * <ul>
 * <li>{@link #getComponent} to return the actual GUI component representation (e.g. a checkbox, text field, ...).
 * </li>
 * <li>{@link #setSymbolImpl} to initialize your GUI component with the symbol's current value</li>
 * <li>{@link #setEnabled} to enable or disable the GUI component (e.g. if the GUI component is not applicable)</li>
 * </ul>
 * When your GUI component detects a change by the user, call
 * {@link #applyChange(Runnable, Runnable)} with the needed logic to apply and undo the symbol
 * change.
 * This class will then automatically lock the model and fire events and ILcdUndoable instances.
 */
public abstract class AbstractSymbolCustomizer implements ILcdChangeSource, ILcdDisposable {

  private final TLcdChangeSupport fChangeSupport = new TLcdChangeSupport();
  private final ILcdModelListener fModelListener = new ModelListener();

  private final boolean fFireModelChange;
  private final ILcdStringTranslator fStringTranslator;

  private TLcdUndoManager fUndoManager;

  private EMilitarySymbology fSymbology;
  private ILcdModel fModel;
  private Object fSymbol;
  private Object fID;

  private boolean fDisposed = false;

  /**
   * @param aFireModelChange true if model changes should be fired. Typically this is false if you're
   *                         customizing objects that are not (yet) contained in a model.
   * @param aStringTranslator translator to be used for tooltips, labels, etc
   */
  public AbstractSymbolCustomizer(boolean aFireModelChange, ILcdStringTranslator aStringTranslator) {
    fFireModelChange = aFireModelChange;
    fStringTranslator = aStringTranslator;
  }

  public void setUndoManager(TLcdUndoManager aUndoManager) {
    fUndoManager = aUndoManager;
  }

  public TLcdUndoManager getUndoManager() {
    return fUndoManager;
  }

  public ILcdStringTranslator getStringTranslator() {
    return fStringTranslator;
  }

  protected void fireChangeEvent(TLcdChangeEvent aEvent) {
    fChangeSupport.fireChangeEvent(aEvent);
  }

  @Override
  public void addChangeListener(ILcdChangeListener aListener) {
    fChangeSupport.addChangeListener(aListener);
  }

  @Override
  public void removeChangeListener(ILcdChangeListener aListener) {
    fChangeSupport.removeChangeListener(aListener);
  }

  /**
   * @param aMilitarySymbology the symbology the symbol belongs to
   * @param aModel             the model containing the symbol
   * @param aSymbol            the symbol to customize, or null to clear the panel and remove any
   *                           listeners
   */
  public void setSymbol(EMilitarySymbology aMilitarySymbology, ILcdModel aModel, Object aSymbol) {
    if (fDisposed) {
      return;
    }

    if (fModel != null) {
      fModel.removeModelListener(fModelListener);
    }
    setSymbolImpl(aMilitarySymbology, aModel, aSymbol);
    // Only change the state after calling setSymbolImpl.
    // This way, setSymbolImpl can still check the previous state.
    fSymbology = aMilitarySymbology;
    fModel = aModel;
    fSymbol = aSymbol;
    fID = getID(aSymbol);
    if (aModel != null) {
      aModel.addModelListener(fModelListener);
    }
  }

  @Override
  public void dispose() {
    if (fModel != null) {
      fModel.removeModelListener(fModelListener);
    }
    fDisposed = true;
  }

  protected Object getID(Object aSymbol) {
    return aSymbol == null ? null : MilitarySymbolFacade.getSIDC(aSymbol);
  }

  /**
   * Check if the last call to {@linkplain #setSymbol(EMilitarySymbology, ILcdModel, Object)}
   * was performed with a fundamentally different symbol. This method will return true iff
   * the SIDC of the symbol was changed or a completely different object was set.
   */
  protected final boolean isNewSymbol(Object aSymbol) {
    return aSymbol != fSymbol ||
           aSymbol == null ||
           !Objects.equals(getID(aSymbol), fID);
  }

  /**
   * Sets up the customizer for the given symbol.
   * If this method is called, getSymbol() will still return the old symbol.
   */
  protected abstract void setSymbolImpl(EMilitarySymbology aMilitarySymbology, ILcdModel aModel, Object aSymbol);

  protected void applyChange(Runnable aChangeRunnable, Runnable aUndoRunnable) {
    UndoableSymbolCustomization undoableSymbolCustomization = new UndoableSymbolCustomization(aChangeRunnable, aUndoRunnable, getModel(), getSymbol());
    if (fUndoManager != null && aUndoRunnable != null) {
      fUndoManager.addUndoable(undoableSymbolCustomization);
    }
  }

  public abstract JComponent getComponent();

  public abstract void setEnabled(boolean aEnabled);

  public EMilitarySymbology getSymbology() {
    return fSymbology;
  }

  public ILcdModel getModel() {
    return fModel;
  }

  public Object getSymbol() {
    return fSymbol;
  }

  private class ModelListener implements ILcdModelListener {
    @Override
    public void modelChanged(TLcdModelChangedEvent aEvent) {
      if (aEvent.containsElement(getSymbol())) {
        setSymbol(getSymbology(), getModel(), getSymbol());
      }
    }
  }

  protected String translate(String aName) {
    return fStringTranslator == null ?
           aName :
           fStringTranslator.translate(aName);
  }

  private class UndoableSymbolCustomization extends ALcdUndoable {

    private final Runnable fChangeRunnable;
    private final Runnable fUndoRunnable;

    private final ILcdModel fModel;
    private final Object fSymbol;

    public UndoableSymbolCustomization(Runnable aChangeRunnable, Runnable aUndoRunnable, ILcdModel aModel, Object aSymbol) {
      super("symbol customization");
      fChangeRunnable = aChangeRunnable;
      fUndoRunnable = aUndoRunnable;
      fModel = aModel;
      fSymbol = aSymbol;
      apply(fChangeRunnable);
    }

    @Override
    protected boolean canUndoImpl() {
      return fUndoRunnable != null;
    }

    @Override
    protected void undoImpl() throws TLcdCannotUndoRedoException {
      try {
        apply(fUndoRunnable);
      } catch (Exception e) {
        throw new TLcdCannotUndoRedoException(e.getMessage());
      }
    }

    @Override
    protected void redoImpl() throws TLcdCannotUndoRedoException {
      try {
        apply(fChangeRunnable);
      } catch (Exception e) {
        throw new TLcdCannotUndoRedoException(e.getMessage());
      }
    }

    private void apply(Runnable aRunnable) throws RuntimeException {
      try (Lock autoUnlock = writeLock(fModel)) {
        aRunnable.run();
        if (fFireModelChange) {
          fModel.elementChanged(fSymbol, ILcdModel.FIRE_NOW);
        }
        fireChangeEvent(new TLcdChangeEvent(this));
      }
    }
  }
}
