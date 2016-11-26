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
package samples.lightspeed.internal.eurocontrol.sassc.model.records.recordColumns;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import samples.lightspeed.internal.eurocontrol.sassc.model.records.TSasDbExpressionDefinition;
import samples.lightspeed.internal.eurocontrol.sassc.model.util.IntArrayList;

/**
 * Record column for wich all elements are lists of integers.
 *
 * @author Bruno Christiaen
 */
public class TSasMultiValueIntRecordColumn extends ASasIntRecordColumn {

  private IntArrayList[] fValues;

  /**
   * Create an empty record column. It can be filled later by loading data into it using {@link #load(String)}.
   *
   * @param aDefinition The definition of this column.
   */
  public TSasMultiValueIntRecordColumn(TSasDbExpressionDefinition aDefinition) {
    this(aDefinition, 0);
  }

  /**
   * Create a record column with the given size.
   *
   * @param aDefinition The definition of this column.
   * @param aSize       The number of values that can be stored in this column.
   */
  public TSasMultiValueIntRecordColumn(TSasDbExpressionDefinition aDefinition, int aSize) {
    super(aDefinition);
    fValues = new IntArrayList[aSize];
  }

  /**
   * Returns an Iterator of Integers containing all the values for the record at the given index.
   * @param aIndex The record index.
   * @return All the values for the given record, or an empty iterator if null.
   */
  public Iterator<Integer> getValue(int aIndex) {
    assert aIndex < fValues.length;
    if (fValues[aIndex] == null) {
      final List<Integer> emptyList = Collections.emptyList();
      return emptyList.iterator();
    }
    return new IntArrayListIterator(fValues[aIndex]);
  }

  /**
   * Returns an Iterator of Integers containing all the values for the record at the given index.
   * @param aIndex The record index.
   * @return All the values for the given record, or null if there are none.
   */
  @Override
  public Object getObject(int aIndex) {
    return isNull(aIndex) ? null : getValue(aIndex);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Object getObjectOrNullCode(int aIndex) {
    if (isNull(aIndex)) {
      return getDefinition().hasDefaultValue() ? getDefinition().getDefaultNullValue() : DEFAULT_VALUE;
    } else {
      return getValue(aIndex);
    }
  }

  /**
   * Add the value to the list of values for the record at the given index.
   *
   * @param aIndex Record index.
   * @param aValue Value to add.
   */
  @Override
  public void setValue(int aIndex, Object aValue) {
    final int parsedValue = parseDBValue(aValue);
    if (fValues[aIndex] == null) {
      fValues[aIndex] = new IntArrayList(2);
    }
    fValues[aIndex].add(parsedValue);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isNull(int aIndex) {
    return (fValues[aIndex] == null);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void storeValues(ObjectOutputStream aOutputStream) throws IOException {
    aOutputStream.writeObject(fValues.length);
    for (int i = 0; i < fValues.length; i++) {
      if (fValues[i] == null) {
        aOutputStream.writeInt(0);
      } else {
        aOutputStream.writeInt(fValues[i].size());
        for (int j = 0; j < fValues[i].size(); j++) {
          aOutputStream.writeInt(fValues[i].get(j));
        }
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void loadValues(ObjectInputStream aInputStream)
      throws IOException, ClassNotFoundException {
    int nrRecords = (Integer) aInputStream.readObject();
    fValues = new IntArrayList[nrRecords];
    for (int i = 0; i < nrRecords; i++) {
      int nrValues = aInputStream.readInt();
      if (nrValues > 0) {
        fValues[i] = new IntArrayList(nrValues);
        for (int j = 0; j < nrValues; j++) {
          fValues[i].add(aInputStream.readInt());
        }
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isEmpty() {
    return fValues.length == 0;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int getSize() {
    return fValues.length;
  }

  private class IntArrayListIterator implements Iterator<Integer> {
    private IntArrayList fIntArrayList;
    private int fCurrent;

    public IntArrayListIterator(IntArrayList aIntArrayList) {
      fIntArrayList = aIntArrayList;
    }

    public boolean hasNext() {
      return fCurrent < fIntArrayList.size();
    }

    public Integer next() {
      if (hasNext()) {
        return fIntArrayList.get(fCurrent++);
      } else {
        throw new NoSuchElementException(fCurrent + " >= " + fIntArrayList.size());
      }
    }

    public void remove() {
      throw new UnsupportedOperationException("Remove elements not supported.");
    }
  }
}
