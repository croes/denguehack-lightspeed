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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import samples.lightspeed.internal.eurocontrol.sassc.model.records.TSasDbExpressionDefinition;

/**
 * Record column for wich all elements are lists of strings.
 *
 * @author Bruno Christiaen
 */
public class TSasMultiValueStringRecordColumn extends ASasStringRecordColumn {

  private ArrayList<List<String>> fValues;

  /**
   * Create an empty record column. It can be filled later by loading data into it using {@link #load(String)}.
   *
   * @param aDefinition The definition of this column.
   */
  public TSasMultiValueStringRecordColumn(TSasDbExpressionDefinition aDefinition) {
    this(aDefinition, 0);
  }

  /**
   * Create a record column with the given size.
   *
   * @param aDefinition The definition of this column.
   * @param aSize       The number of values that can be stored in this column.
   */
  public TSasMultiValueStringRecordColumn(TSasDbExpressionDefinition aDefinition, int aSize) {
    super(aDefinition);
    fValues = createList(aSize);
  }

  /**
   * Returns an Iterator of Strings containing all the values for the record at the given index.
   *
   * @param aIndex The record index.
   * @return All the values for the given record, or an empty iterator if null.
   */
  public Iterator<String> getValue(int aIndex) {
    assert aIndex < fValues.size();
    final List<String> strings = fValues.get(aIndex);
    if (strings == null) {
      final List<String> emptyList = Collections.emptyList();
      return emptyList.iterator();
    }
    return strings.iterator();
  }

  /**
   * Returns an Iterator of Strings containing all the values for the record at the given index.
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
      return getNullValueAsObject();
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
    List<String> stringList = fValues.get(aIndex);
    if (stringList == null) {
      stringList = new ArrayList<String>(2);
      fValues.set(aIndex, stringList);
    }
    stringList.add((String) aValue);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isNull(int aIndex) {
    return (fValues.get(aIndex) == null);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void storeValues(ObjectOutputStream aOutputStream) throws IOException {
    aOutputStream.writeInt(fValues.size());
    for (int i = 0; i < fValues.size(); i++) {
      final List<String> stringList = fValues.get(i);
      if (stringList == null) {
        aOutputStream.writeInt(0);
      } else {
        aOutputStream.writeInt(stringList.size());
        for (int j = 0; j < stringList.size(); j++) {
          aOutputStream.writeObject(stringList.get(j));
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
    fValues = createList(nrRecords);
    for (int i = 0; i < nrRecords; i++) {
      int nrValues = aInputStream.readInt();
      if (nrValues > 0) {
        final ArrayList<String> stringList = new ArrayList<String>(nrValues);
        fValues.set(i, stringList);
        for (int j = 0; j < nrValues; j++) {
          stringList.add((String) aInputStream.readObject());
        }
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isEmpty() {
    return fValues.size() == 0;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int getSize() {
    return fValues.size();
  }

  private static ArrayList<List<String>> createList(int aSize) {
    ArrayList<List<String>> values = new ArrayList<List<String>>(aSize);
    final List<List<String>> list = Collections.nCopies(aSize, null);
    values.addAll(list);
    return values;
  }

}
