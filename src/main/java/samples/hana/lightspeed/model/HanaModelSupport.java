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
package samples.hana.lightspeed.model;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;

import com.luciad.datamodel.ILcdDataObject;
import com.luciad.datamodel.TLcdDataType;
import com.luciad.format.hana.TLcdHanaGeometrySupport;
import com.luciad.format.hana.TLcdHanaModelDescriptor;
import com.luciad.shape.ILcdShape;
import com.luciad.shape.TLcdDataObjectShapeList;

public final class HanaModelSupport {

  private final TLcdHanaGeometrySupport fGeometryReader;

  public HanaModelSupport() {
    fGeometryReader = new TLcdHanaGeometrySupport();
  }

  /**
   * Creates a model descriptor for a single table based on an (empty) query result.
   */
  public TLcdHanaModelDescriptor createHanaModelDescriptor(ResultSetMetaData aMetaData, String aGeometryColumnName, String aUrl, String aTableName) {
    try {
      int columnCount = aMetaData.getColumnCount();
      String[] columnNames = new String[columnCount];
      String[] columnClassNames = new String[columnCount];
      for (int col = 0; col < columnCount; col++) {
        columnNames[col] = aMetaData.getColumnName(col + 1);
        columnClassNames[col] = aMetaData.getColumnClassName(col + 1);
      }

      return TLcdHanaModelDescriptor.createModelDescriptor(
          aUrl,
          aTableName,
          columnNames,
          columnClassNames,
          aGeometryColumnName
      );
    } catch (SQLException e) {
      throw new IllegalStateException(e);
    }
  }

  /**
   * Creates a data object with geometry for a single record.
   */
  public ILcdDataObject createObject(ResultSet aResultSet, TLcdHanaModelDescriptor aModelDescriptor) {
    try {
      String geometryColumn = aModelDescriptor.getGeometryColumn();
      ILcdShape shape = fGeometryReader.readGeometry(aResultSet.getBytes(geometryColumn));

      TLcdDataType modelElementType = aModelDescriptor.getModelElementType();
      TLcdDataObjectShapeList dataObject = new TLcdDataObjectShapeList(modelElementType, shape) {
        @Override
        public boolean equals(Object that) {
          return (this == that);
        }
      };

      ResultSetMetaData metaData = aResultSet.getMetaData();
      int columns = metaData.getColumnCount();
      for (int col = 0; col < columns; col++) {
        String name = metaData.getColumnName(col + 1);
        Object value = aResultSet.getObject(col + 1);
        if (value instanceof Timestamp) {
          value = ((Timestamp) value).getTime();
        }
        dataObject.setValue(name, value);
      }

      return dataObject;
    } catch (SQLException e) {
      throw new IllegalStateException(e);
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }
}
