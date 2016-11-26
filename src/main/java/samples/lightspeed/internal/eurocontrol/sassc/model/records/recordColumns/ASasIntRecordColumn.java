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

import samples.lightspeed.internal.eurocontrol.sassc.model.records.TSasDbExpressionDefinition;

/**
 * Abstract base class for single- and multi-valued int record columns.
 *
 * @author Bruno Christiaen
 */
public abstract class ASasIntRecordColumn extends ASasRecordColumn {

  /**
   * Integer value used to represent NULL in DB.
   */
  protected static final int NULL_VALUE = Integer.MIN_VALUE;
  protected static final int DEFAULT_VALUE = 0;

  /**
   * Create a new record column based on the given definition.
   * @param aDefinition The record column definition.
   */
  public ASasIntRecordColumn(TSasDbExpressionDefinition aDefinition) {
    super(aDefinition);
  }

  /**
   * {@inheritDoc}
   */
  public Object getNullValueAsObject() {
    return getNullValue();
  }

  /**
   * Integer value used to indicate a NULL value in the database.
   * @return The value.
   */
  public static int getNullValue() {
    return NULL_VALUE;
  }

  /**
   * Parse a value from a DB column and convert into an internal value.
   * @param aValue The value from the JDBC ResultSet.
   * @return The internal representation of the value.
   */
  protected static int parseDBValue(Object aValue) {
    int value;
    if (aValue == null) {
      value = NULL_VALUE;
    } else {
      value = ((Number) aValue).intValue();
    }
    return value;
  }
}
