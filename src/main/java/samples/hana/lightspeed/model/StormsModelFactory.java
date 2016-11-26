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

import java.awt.Component;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

import com.luciad.datamodel.ILcdDataObject;
import com.luciad.format.hana.TLcdHanaGeometrySupport;
import com.luciad.format.shp.TLcdSHPModelDecoder;
import com.luciad.model.ILcdModel;
import com.luciad.shape.ILcdShapeList;

import samples.hana.lightspeed.common.Configuration;
import samples.hana.lightspeed.common.HanaConnectionParameters;

/**
 * Creates a storms model. If no table containing storms data exists, a new table using sample
 * storms data is created.
 */
public final class StormsModelFactory extends HanaModelFactory<StormsModel> {

  private static final String SCHEMA_NAME = Configuration.get("database.schema");
  private static final String STORMS_TABLE_NAME = Configuration.get("database.storms.tableName");
  private static final String STORMS_COLUMN_DEFINITION = "SPEED INTEGER, STORMTIME TIMESTAMP, SHAPE ST_GEOMETRY";
  private static final String INSERT_STORM_QUERY = "insert into " + SCHEMA_NAME + "." + STORMS_TABLE_NAME + " values(?,?,?)";

  public StormsModelFactory(Component aParentComponent) {
    super(aParentComponent, createExecutorService(), "storms", STORMS_TABLE_NAME, STORMS_COLUMN_DEFINITION, INSERT_STORM_QUERY);
  }

  private static HanaConnectionExecutorService createExecutorService() {
    return new HanaConnectionExecutorService("Storms",
                                             HanaConnectionParameters.getInstance(),
                                             Configuration.getInt("database.connections.storms"));
  }

  @Override
  protected StormsModel createModel(HanaConnectionExecutorService aExecutorService) {
    return new StormsModel(aExecutorService, SCHEMA_NAME + "." + STORMS_TABLE_NAME);
  }

  @Override
  protected ILcdModel createSampleDataModel() throws IOException {
    TLcdSHPModelDecoder shpDecoder = new TLcdSHPModelDecoder();
    return shpDecoder.decode("Data/Sandy_storm.shp");
  }

  @Override
  protected HanaDatabaseUtil.UploadObjectHandler createUploadObjectHandler() {
    final TLcdHanaGeometrySupport geometryWriter = new TLcdHanaGeometrySupport();
    return new HanaDatabaseUtil.UploadObjectHandler() {
      @Override
      public void prepareObject(Object aObject, PreparedStatement aPreparedStatement) throws SQLException, IOException {
        ILcdShapeList shapeList = (ILcdShapeList) aObject;
        ByteArrayOutputStream geometry = new ByteArrayOutputStream();
        geometryWriter.writeGeometry(shapeList.getShape(0), 0, geometry);
        byte[] geometryByteArray = geometry.toByteArray();

        ILcdDataObject dataObject = (ILcdDataObject) aObject;

        aPreparedStatement.setInt(1, (Integer) dataObject.getValue("Speed"));
        aPreparedStatement.setTimestamp(2, new Timestamp(Long.parseLong((String) dataObject.getValue("Time"))));
        aPreparedStatement.setBytes(3, geometryByteArray);
      }

      @Override
      public void progress(double aProgress) {
      }
    };
  }
}
