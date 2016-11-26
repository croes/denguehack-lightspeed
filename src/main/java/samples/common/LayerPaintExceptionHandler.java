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
package samples.common;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import com.luciad.util.ILcdChangeListener;
import com.luciad.util.ILcdChangeSource;
import com.luciad.util.TLcdChangeEvent;
import com.luciad.util.TLcdChangeSupport;
import com.luciad.view.ILcdLayer;
import com.luciad.view.ILcdPaintExceptionHandler;
import com.luciad.view.TLcdLoggingPaintExceptionHandler;

/**
 * Stores layer paint exceptions so that they can be {@link #getExceptions queried} later.
 */
public class LayerPaintExceptionHandler implements ILcdPaintExceptionHandler, ILcdChangeSource {

  private final Map<ILcdLayer, LogRecord> fExceptions = Collections.synchronizedMap(new WeakHashMap<ILcdLayer, LogRecord>());
  private final ILcdPaintExceptionHandler fDelegate;
  private final TLcdChangeSupport fChangeSupport = new TLcdChangeSupport();

  public LayerPaintExceptionHandler() {
    this(new TLcdLoggingPaintExceptionHandler());
  }

  public LayerPaintExceptionHandler(ILcdPaintExceptionHandler aDelegate) {
    fDelegate = aDelegate;
  }

  @Override
  public boolean handlePaintException(Exception aException, ILcdLayer aLayer) {
    boolean result = fDelegate.handlePaintException(aException, aLayer);

    if (aLayer != null) {
      LogRecord value = new LogRecord(Level.SEVERE, aException.getLocalizedMessage());
      value.setThrown(aException);
      fExceptions.put(aLayer, value);
      fChangeSupport.fireChangeEvent(new TLcdChangeEvent(this));
    }

    return result;
  }

  public Map<ILcdLayer, LogRecord> getExceptions() {
    return fExceptions;
  }

  @Override
  public void addChangeListener(ILcdChangeListener aListener) {
    fChangeSupport.addChangeListener(aListener);
  }

  @Override
  public void removeChangeListener(ILcdChangeListener aListener) {
    fChangeSupport.removeChangeListener(aListener);
  }

  public void fireChangeEvent() {
    fChangeSupport.fireChangeEvent(new TLcdChangeEvent(this));
  }
}
