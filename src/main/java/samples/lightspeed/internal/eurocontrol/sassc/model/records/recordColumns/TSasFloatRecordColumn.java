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

/**
 * Record columns for wich all elements are float.
 *
 * @author patrickd
 */
public class TSasFloatRecordColumn extends ASasFloatRecordColumn {
  private float[] fValues;

  /**
   * Create an empty record column. It can be filled later by loading data into it using {@link #load(String)}.
   *
   * @param aDefinition The definition of this column.
   */
  public TSasFloatRecordColumn(TSasDbExpressionDefinition aDefinition) {
    this(aDefinition, 0);
  }

  /**
   * Create a record column with the given size.
   *
   * @param aDefinition The definition of this column.
   * @param aSize       The number of values that can be stored in this column.
   */
  public TSasFloatRecordColumn(TSasDbExpressionDefinition aDefinition, int aSize) {
    super(aDefinition);
    fValues = new float[aSize];
    Arrays.fill(fValues, NULL_VALUE);
  }

  public float getValue(int aIndex) {
    assert aIndex < fValues.length;
    if (isNull(aIndex)) {
      return getDefinition().hasDefaultValue() ? Float.parseFloat(getDefinition().getDefaultNullValue()) : DEFAULT_VALUE;
    }
    return fValues[aIndex];
  }

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

  @Override
  public void setValue(int aIndex, Object aValue) {
    fValues[aIndex] = parseDBValue(aValue);
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
      aOutputStream.writeFloat(fValues[i]);
    }
  }

  @Override
  protected void loadValues(ObjectInputStream aInputStream) throws IOException,
                                                                   ClassNotFoundException {
    int nrValues = (Integer) aInputStream.readObject();
    fValues = new float[nrValues];
    for (int i = 0; i < nrValues; i++) {
      float value = aInputStream.readFloat();
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
