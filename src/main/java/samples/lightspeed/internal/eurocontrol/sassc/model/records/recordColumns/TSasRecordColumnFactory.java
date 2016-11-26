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

import java.io.File;

import samples.lightspeed.internal.eurocontrol.sassc.TSasEnvironmentFactory;
import samples.lightspeed.internal.eurocontrol.sassc.TSasParameters;
import samples.lightspeed.internal.eurocontrol.sassc.model.records.ESasJavaType;
import samples.lightspeed.internal.eurocontrol.sassc.model.records.TSasDbExpressionDefinition;

/**
 *
 */
public class TSasRecordColumnFactory {

  public ASasRecordColumn createAndLoadRecordColumn(final TSasDbExpressionDefinition aDefinition) {
    String fileName = getParameters().getCacheDirectory() + File.separator + aDefinition.getName() + ".rc";
    File columnFile = new File(fileName);
    if (!columnFile.exists()) {
      //TODO: add some logic to create a cached file by fetching the column from the database
    } else {
      aDefinition.setExtract(true);
    }
    ASasRecordColumn recordColumn = createRecordColumn(aDefinition, 0);
    recordColumn.load(fileName);
    return recordColumn;
  }

  public ASasRecordColumn createRecordColumn(TSasDbExpressionDefinition aDefinition, final int aSize) {
    final ESasJavaType type = aDefinition.getJavaType();

    if (aDefinition.isMultiValue()) {
      switch (type) {
      case BOOLEAN:
        return new TSasMultiValueBooleanRecordColumn(aDefinition, aSize);
      case FLOAT:
        return new TSasMultiValueFloatRecordColumn(aDefinition, aSize);
      case INT:
        return new TSasMultiValueIntRecordColumn(aDefinition, aSize);
      case LONG:
        return new TSasMultiValueLongRecordColumn(aDefinition, aSize);
      case STRING:
        return new TSasMultiValueStringRecordColumn(aDefinition, aSize);
      }
    } else {
      switch (type) {
      case BOOLEAN:
        return new TSasBooleanRecordColumn(aDefinition, aSize);
      case FLOAT:
        return new TSasFloatRecordColumn(aDefinition, aSize);
      case INT:
        return new TSasIntRecordColumn(aDefinition, aSize);
      case LONG:
        return new TSasLongRecordColumn(aDefinition, aSize);
      case STRING:
        return new TSasStringRecordColumn(aDefinition, aSize);
      }
    }
    //TODO: add some exception throwing here
    return null;
  }

  private TSasParameters getParameters() {
    return TSasEnvironmentFactory.getInstance().getEnvironment().getParameters();
  }
}
