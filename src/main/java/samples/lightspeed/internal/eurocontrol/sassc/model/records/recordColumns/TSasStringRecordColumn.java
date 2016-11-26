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

import samples.lightspeed.internal.eurocontrol.sassc.model.records.TSasDbExpressionDefinition;

/**
 * Record columns for wich all elements are Strings.
 *
 * @author patrickd
 */
public class TSasStringRecordColumn extends ASasStringRecordColumn {

  private String[] fValues;

  public TSasStringRecordColumn(TSasDbExpressionDefinition aDefinition, int aSize) {
    super(aDefinition);
    fValues = new String[aSize];
  }

  String getValue(int aIndex) {
    assert aIndex < fValues.length;
    return fValues[aIndex];
  }

  @Override
  public Object getObject(int aIndex) {
    return getValue(aIndex);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Object getObjectOrNullCode(int aIndex) {
    if (isNull(aIndex)) {
      return getDefinition().hasDefaultValue() ? getDefinition().getDefaultNullValue() : null;
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
    return (fValues[aIndex] == null);
  }

  @Override
  protected void storeValues(ObjectOutputStream aOutputStream) throws IOException {
    aOutputStream.writeObject(fValues.length);
    for (int i = 0; i < fValues.length; i++) {
      aOutputStream.writeObject(fValues[i]);
    }
  }

  @Override
  protected void loadValues(ObjectInputStream aInputStream) throws IOException,
                                                                   ClassNotFoundException {
    int nrValues = (Integer) aInputStream.readObject();
    fValues = new String[nrValues];
    for (int i = 0; i < nrValues; i++) {
      String value = (String) aInputStream.readObject();
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
