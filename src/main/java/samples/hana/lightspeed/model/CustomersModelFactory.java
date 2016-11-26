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
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.luciad.datamodel.ILcdDataObject;
import com.luciad.datamodel.TLcdDataType;
import com.luciad.format.hana.TLcdHanaModelDescriptor;
import com.luciad.format.shp.TLcdSHPModelDecoder;
import com.luciad.model.ILcdModel;
import com.luciad.shape.shape2D.TLcd2DEditableFeaturedPoint;

import samples.hana.lightspeed.common.Configuration;
import samples.hana.lightspeed.common.HanaConnectionParameters;
import samples.hana.lightspeed.domain.CustomerCategory;
import samples.hana.lightspeed.domain.CustomerPoint;
import samples.hana.lightspeed.domain.InsuranceCompany;

/**
 * Creates a customers model. If no table containing customers data exists, a new table using sample
 * customers data is created.
 */
public class CustomersModelFactory extends HanaModelFactory<BoundsIndexedHanaModel> {

  private static final String SCHEMA_NAME = Configuration.get("database.schema");
  private static final String CUSTOMERS_TABLE_NAME = Configuration.get("database.customers.tableName");
  private static final String CUSTOMERS_COLUMN_DEFINITION = "INSURANCE_ID INTEGER, CATEGORY VARCHAR(20), STATE_ID INTEGER, COUNTY_ID INTEGER, POLICY_VALUE bigint, LOCATION ST_POINT";
  private static final String INSERT_CUSTOMERS_QUERY = "insert into " + SCHEMA_NAME + "." + CUSTOMERS_TABLE_NAME + " values(?,?,?,?,?,new ST_POINT(?))";

  public CustomersModelFactory(Component aParentComponent) {
    super(aParentComponent, createExecutorService(), "customers", CUSTOMERS_TABLE_NAME, CUSTOMERS_COLUMN_DEFINITION, INSERT_CUSTOMERS_QUERY);
  }

  private static HanaConnectionExecutorService createExecutorService() {
    return new HanaConnectionExecutorService("Points",
                                             HanaConnectionParameters.getInstance(),
                                             Configuration.getInt("database.connections.points"));
  }

  @Override
  protected BoundsIndexedHanaModel createModel(HanaConnectionExecutorService aExecutorService) {
    final String tableName = SCHEMA_NAME + "." + CUSTOMERS_TABLE_NAME;
    return new BoundsIndexedHanaModel(tableName, "LOCATION", aExecutorService) {
      @Override
      protected String getTileQuery(String aSpatialQuery) {
        return "select LOCATION.ST_X(), LOCATION.ST_Y(), CATEGORY, INSURANCE_ID, POLICY_VALUE from " + tableName + " where " + aSpatialQuery;
      }

      @Override
      protected ILcdDataObject createObject(ResultSet aResultSet) {
        try {
          double x = aResultSet.getDouble(1);
          double y = aResultSet.getDouble(2);
          CustomerCategory category = CustomerCategory.valueOfDb(aResultSet.getString(3));
          InsuranceCompany insurance = InsuranceCompany.valueOfDb(aResultSet.getInt(4));
          int policyValue = aResultSet.getInt(5);
          TLcdHanaModelDescriptor modelDescriptor = (TLcdHanaModelDescriptor) getModelDescriptor();
          TLcdDataType modelElementType = modelDescriptor.getModelElementType();
          return new CustomerPoint(modelElementType, x, y, category, policyValue, insurance);
        } catch (SQLException e) {
          throw new IllegalStateException(e);
        }
      }
    };
  }

  @Override
  protected ILcdModel createSampleDataModel() throws IOException {
    TLcdSHPModelDecoder shpDecoder = new TLcdSHPModelDecoder();
    return shpDecoder.decode("Data/generated_customers.SHP");
  }

  @Override
  protected HanaDatabaseUtil.UploadObjectHandler createUploadObjectHandler() {
    return new HanaDatabaseUtil.UploadObjectHandler() {
      @Override
      public void prepareObject(Object aObject, PreparedStatement aPreparedStatement) throws SQLException, IOException {
        ILcdDataObject dataObject = (ILcdDataObject) aObject;
        TLcd2DEditableFeaturedPoint point = (TLcd2DEditableFeaturedPoint) aObject;
        aPreparedStatement.setInt(1, (Integer) dataObject.getValue("INSURANCE"));
        aPreparedStatement.setString(2, (String) dataObject.getValue("CATEGORY"));
        aPreparedStatement.setInt(3, (Integer) dataObject.getValue("STATE_ID"));
        aPreparedStatement.setInt(4, (Integer) dataObject.getValue("COUNTY_ID"));
        aPreparedStatement.setInt(5, (Integer) dataObject.getValue("POLICY_VAL"));
        aPreparedStatement.setString(6, "Point (" + point.getX() + " " + point.getY() + ")");
      }

      @Override
      public void progress(double aProgress) {
      }
    };
  }
}
