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
import java.util.Arrays;

import samples.lightspeed.internal.eurocontrol.sassc.model.records.TSasDbExpressionDefinition;

// todo It would be better if the fact that booleans are stored as bytes could be fully encapsulated in this class.

/**
 * Record columns for wich all elements are boolean.
 * Note that bytes are used to store boolean values to be able to represent null values.
 * @author patrickd
 *
 */
public class TSasBooleanRecordColumn extends ASasBooleanRecordColumn {
  private byte[] fValues;

  /**
   * Create an empty record column. It can be filled later by loading data into it using {@link #load(String)}.
   *
   * @param aDefinition The definition of this column.
   */
  public TSasBooleanRecordColumn(TSasDbExpressionDefinition aDefinition) {
    this(aDefinition, 0);
  }

  public TSasBooleanRecordColumn(TSasDbExpressionDefinition aDefinition, int aSize) {
    super(aDefinition);
    fValues = new byte[aSize];
    Arrays.fill(fValues, NULL_VALUE);
  }

  public byte getValue(int aIndex) {
    assert aIndex < fValues.length;
    return fValues[aIndex];
  }

  @Override
  public Object getObject(int aIndex) {
    return isNull(aIndex) ? null : getValue(aIndex);
  }

  @Override
  public void setValue(int aIndex, Object aValue) {
    fValues[aIndex] = parseDBValue(aValue);
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

  @Override
  public boolean isNull(int aIndex) {
    return (fValues[aIndex] == getNullValue());
  }

  /**
   * {@inheritDoc}
   */
  public Object getNullValueAsObject() {
    return getNullValue();
  }

  @Override
  protected void storeValues(ObjectOutputStream aOutputStream) throws IOException {
    aOutputStream.writeObject(fValues.length);
    for (int i = 0; i < fValues.length; i++) {
      aOutputStream.writeByte(fValues[i]);
    }
  }

  @Override
  protected void loadValues(ObjectInputStream aInputStream)
      throws IOException, ClassNotFoundException {
    int nrValues = (Integer) aInputStream.readObject();
    fValues = new byte[nrValues];
    for (int i = 0; i < nrValues; i++) {
      byte value = aInputStream.readByte();
      fValues[i] = value;
    }
  }

  @Override
  public boolean isEmpty() {
    return fValues.length == 0;
  }

  @Override
  public int getSize() {
    return fValues.length;
  }
}
