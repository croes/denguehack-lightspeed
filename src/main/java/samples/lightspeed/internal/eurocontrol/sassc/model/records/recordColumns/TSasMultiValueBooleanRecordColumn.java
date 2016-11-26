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
import samples.lightspeed.internal.eurocontrol.sassc.model.util.ByteArrayList;

// todo It would be better if the fact that booleans are stored as bytes could be fully encapsulated in this class.

/**
 * Record column for wich all elements are lists of booleans.
 *
 * @author Bruno Christiaen
 */
public class TSasMultiValueBooleanRecordColumn extends ASasBooleanRecordColumn {

  private ByteArrayList[] fValues;

  /**
   * Create an empty record column. It can be filled later by loading data into it using {@link #load(String)}.
   *
   * @param aDefinition The definition of this column.
   */
  public TSasMultiValueBooleanRecordColumn(TSasDbExpressionDefinition aDefinition) {
    this(aDefinition, 0);
  }

  /**
   * Create a record column with the given size.
   *
   * @param aDefinition The definition of this column.
   * @param aSize       The number of values that can be stored in this column.
   */
  public TSasMultiValueBooleanRecordColumn(TSasDbExpressionDefinition aDefinition, int aSize) {
    super(aDefinition);
    fValues = new ByteArrayList[aSize];
  }

  /**
   * Returns an Iterator of Booleans containing all the values for the record at the given index.
   *
   * @param aIndex The record index.
   * @return All the values for the given record, or an empty iterator if null.
   */
  public Iterator<Byte> getValue(int aIndex) {
    assert aIndex < fValues.length;
    if (fValues[aIndex] == null) {
      final List<Byte> emptyList = Collections.emptyList();
      return emptyList.iterator();
    }
    return new ByteArrayListIterator(fValues[aIndex]);
  }

  /**
   * Returns an Iterator of Booleans containing all the values for the record at the given index.
   *
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
      return getDefinition().hasDefaultValue() ? getDefinition().getDefaultNullValue() : getNullValue();
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
    final byte parsedValue = parseDBValue(aValue);
    if (fValues[aIndex] == null) {
      fValues[aIndex] = new ByteArrayList(2);
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
    aOutputStream.writeInt(fValues.length);
    for (int i = 0; i < fValues.length; i++) {
      if (fValues[i] == null) {
        aOutputStream.writeInt(0);
      } else {
        aOutputStream.writeInt(fValues[i].size());
        for (int j = 0; j < fValues[i].size(); j++) {
          aOutputStream.writeByte(fValues[i].get(j));
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
    int nrRecords = aInputStream.readInt();
    fValues = new ByteArrayList[nrRecords];
    for (int i = 0; i < nrRecords; i++) {
      int nrValues = aInputStream.readInt();
      if (nrValues > 0) {
        fValues[i] = new ByteArrayList(nrValues);
        for (int j = 0; j < nrValues; j++) {
          fValues[i].add(aInputStream.readByte());
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

  private class ByteArrayListIterator implements Iterator<Byte> {
    private ByteArrayList fByteArrayList;
    private int fCurrent;

    public ByteArrayListIterator(ByteArrayList aByteArrayList) {
      fByteArrayList = aByteArrayList;
    }

    public boolean hasNext() {
      return fCurrent < fByteArrayList.size();
    }

    public Byte next() {
      if (hasNext()) {
        return fByteArrayList.get(fCurrent++);
      } else {
        throw new NoSuchElementException(fCurrent + " >= " + fByteArrayList.size());
      }
    }

    public void remove() {
      throw new UnsupportedOperationException("Remove elements not supported.");
    }
  }
}
