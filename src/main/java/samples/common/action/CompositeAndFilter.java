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
package samples.common.action;

import java.util.Vector;

import com.luciad.util.ILcdChangeListener;
import com.luciad.util.ILcdChangeSource;
import com.luciad.util.ILcdFilter;
import com.luciad.util.TLcdChangeEvent;
import com.luciad.util.TLcdChangeSupport;

/**
 * <p>Implementation of <code>ILcdFilter</code> that logically ands the
 * associated filters together. This means that <code>accept</code> will only
 * return true if the given object is accepted by all associated filters.</p>
 *
 */
public class CompositeAndFilter<T> implements ILcdFilter<T>, ILcdChangeSource {

  private Vector<ILcdFilter<T>> fFilters = new Vector<ILcdFilter<T>>();
  private TLcdChangeSupport fChangeSupport = new TLcdChangeSupport();
  private MyChangeListener fMyChangeListener = new MyChangeListener();

  /**
   * <p>Constructs a new <code>TLcyCompositeAndFilter</code>, having no associated
   * <code>ILcdFilter</code>s. It will therefore accept every object.</p>
   */
  public CompositeAndFilter() {
  }

  /**
   * <p>Returns <code>true</code> if all associated <code>ILcdFilter</code>s accept the given <code>Object</code> or if no
   * <code>ILcdFilter</code>s are associated.  Returns <code>false</code> otherwise.</p>
   *
   * @param aObject The object to evaluate.
   * @return <code>true</code> if all filters accept the object.
   *
   * @see #addFilter(ILcdFilter)
   */
  @Override
  public boolean accept(T aObject) {
    for (int i = 0, c = getFilterCount(); i < c; i++) {
      if (!getFilter(i).accept(aObject)) {
        return false;
      }
    }
    return true;
  }

  /**
   * <p>Adds the given filter to this composite filter.</p>
   *
   * @param aFilter The filter to add.  Must be different from <code>null</code>.
   *
   * @see #removeFilter(ILcdFilter)
   */
  public void addFilter(ILcdFilter<T> aFilter) {
    if (aFilter == null) {
      throw new NullPointerException("Cannot add null as a filter");
    }
    fFilters.add(aFilter);
    if (aFilter instanceof ILcdChangeSource) {
      ((ILcdChangeSource) aFilter).removeChangeListener(fMyChangeListener);
      ((ILcdChangeSource) aFilter).addChangeListener(fMyChangeListener);
    }
    fChangeSupport.fireChangeEvent(new TLcdChangeEvent(this));
  }

  /**
   * <p>Removes the given filter from this composite filter. Does nothing if the
   * given <code>ILcdFilter</code> is not associated to this composite filter.</p>
   *
   * @param aFilter The filter to remove.
   *
   * @see #addFilter(ILcdFilter)
   */
  public void removeFilter(ILcdFilter<T> aFilter) {
    if (aFilter != null) {
      if (fFilters.remove(aFilter)) {
        fChangeSupport.fireChangeEvent(new TLcdChangeEvent(this));
      }
      if (aFilter instanceof ILcdChangeSource) {
        ((ILcdChangeSource) aFilter).removeChangeListener(fMyChangeListener);
      }
    }
  }

  /**
   * <p>Returns the amount if <code>ILcdFilter</code>s currently associated.</p>
   *
   * @return the amount if <code>ILcdFilter</code>s currently associated.
   *
   * @see #getFilter(int)
   */
  public int getFilterCount() {
    return fFilters.size();
  }

  /**
   * <p>Returns the filter at the given index.</p>
   * @param aIndex The index to retrieve the <code>ILcdFilter</code> at.  0 <= aIndex <= getFilterCount()
   * @return the filter at the given index.
   *
   * @see #getFilterCount()
   */
  public ILcdFilter<T> getFilter(int aIndex) {
    return fFilters.get(aIndex);
  }

  @Override
  public void addChangeListener(ILcdChangeListener aListener) {
    fChangeSupport.addChangeListener(aListener);
  }

  @Override
  public void removeChangeListener(ILcdChangeListener aListener) {
    fChangeSupport.removeChangeListener(aListener);
  }

  private class MyChangeListener implements ILcdChangeListener {
    @Override
    public void stateChanged(TLcdChangeEvent aChangeEvent) {
      fChangeSupport.fireChangeEvent(new TLcdChangeEvent(CompositeAndFilter.this));
    }
  }
}
