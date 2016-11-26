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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Enumeration;

import com.luciad.model.ILcdIntegerIndexedModel;
import com.luciad.model.ILcdModel;

/**
 * Various utility functions to work with a database.
 */
public class HanaDatabaseUtil {

  private static final String TABLE_EXISTS_QUERY = "select count(*) from TABLES where schema_name = ? and table_name = ?";
  private static final String SCHEMA_EXISTS_QUERY = "select count(*) from SCHEMAS where schema_name = ?";
  private static final String CREATE_SCHEMA_QUERY = "create schema ?";
  private static final String CREATE_TABLE_QUERY = "create column table ?";
  private static final String MERGE_TABLE_QUERY = "merge delta of ?";

  private static final int UPLOAD_OBJECT_BATCH_SIZE = 1000;

  private final HanaConnectionExecutorService fExecutorService;

  public HanaDatabaseUtil(HanaConnectionExecutorService aExecutorService) {
    fExecutorService = aExecutorService;
  }

  public boolean tableExists(String aSchemaName, String aTableName) {
    final boolean[] exists = {false};
    fExecutorService.submitQueryAndWait(TABLE_EXISTS_QUERY, new HanaConnectionExecutorService.ResultHandler() {
      @Override
      public void handleRow(ResultSet aResultSet) throws IOException, SQLException {
        exists[0] = aResultSet.getInt(1) > 0;
      }
    }, aSchemaName, aTableName);
    return exists[0];
  }

  public boolean createTable(String aSchemaName, String aTableName, String aColumnDefinition) {
    final boolean[] success = {false};
    String query = CREATE_TABLE_QUERY.replace("?", aSchemaName + "." + aTableName);
    query += " (" + aColumnDefinition + ")";
    fExecutorService.submitAndWait(query, new HanaConnectionExecutorService.ResultHandler() {
      @Override
      public void handleRow(ResultSet aResultSet) throws IOException, SQLException {
        // Do nothing
      }

      @Override
      public void handleEnd() throws IOException, SQLException {
        success[0] = true;
      }
    });
    return success[0];
  }

  public boolean mergeTable(String aSchemaName, String aTableName) {
    final boolean[] success = {false};
    String query = MERGE_TABLE_QUERY.replace("?", aSchemaName + "." + aTableName);
    fExecutorService.submitAndWait(query, new HanaConnectionExecutorService.ResultHandler() {
      @Override
      public void handleRow(ResultSet aResultSet) throws IOException, SQLException {
        // Do nothing
      }

      @Override
      public void handleEnd() throws IOException, SQLException {
        success[0] = true;
      }
    });
    return success[0];
  }

  public boolean schemaExists(String aSchemaName) {
    final boolean[] exists = {false};
    fExecutorService.submitQueryAndWait(SCHEMA_EXISTS_QUERY, new HanaConnectionExecutorService.ResultHandler() {
      @Override
      public void handleRow(ResultSet aResultSet) throws IOException, SQLException {
        exists[0] = aResultSet.getInt(1) > 0;
      }
    }, aSchemaName);
    return exists[0];
  }

  public boolean createSchema(String aSchemaName) {
    final boolean[] success = {false};
    String query = CREATE_SCHEMA_QUERY.replace("?", aSchemaName);
    fExecutorService.submitAndWait(query, new HanaConnectionExecutorService.ResultHandler() {
      @Override
      public void handleRow(ResultSet aResultSet) throws IOException, SQLException {
        // Do nothing
      }

      @Override
      public void handleEnd() throws IOException, SQLException {
        success[0] = true;
      }
    });
    return success[0];
  }

  public boolean tableIsEmpty(String aSchemaName, String aTableName) {
    String query = "select * from " + aSchemaName + "." + aTableName + " limit 1";
    final boolean[] empty = {true};
    fExecutorService.submitQueryAndWait(query, new HanaConnectionExecutorService.ResultHandler() {
      @Override
      public void handleRow(ResultSet aResultSet) throws IOException, SQLException {
        empty[0] = false;
      }
    });
    return empty[0];
  }

  public void uploadModelObjects(UploadObjectHandler aUploadObjectHandler, String aQuery, ILcdModel aModel) throws SQLException {
    Connection connection = fExecutorService.getConnection();
    boolean wasAutoCommit = connection.getAutoCommit();
    try {
      int size = aModel instanceof ILcdIntegerIndexedModel ? ((ILcdIntegerIndexedModel) aModel).size() : -1;
      connection.setAutoCommit(false);
      Enumeration elements = aModel.elements();
      int it = 0;
      PreparedStatement ps = connection.prepareStatement(aQuery);
      while (elements.hasMoreElements()) {
        Object obj = elements.nextElement();
        try {
          aUploadObjectHandler.prepareObject(obj, ps);
          ps.addBatch();
        } catch (SQLException sqlException) {
          throw new IllegalArgumentException(sqlException);
        } catch (IOException ioe) {
          throw new IllegalArgumentException(ioe);
        }
        it++;
        if (it % UPLOAD_OBJECT_BATCH_SIZE == 0) {
          ps.executeBatch();
          aUploadObjectHandler.progress(size == -1 ? 0.5 : (double) it / size);
        }
      }
      if (it % UPLOAD_OBJECT_BATCH_SIZE != 0) {
        ps.executeBatch();
        aUploadObjectHandler.progress(size == -1 ? 0.5 : (double) it / size);
      }
    } finally {
      connection.setAutoCommit(wasAutoCommit);
      fExecutorService.releaseConnection(connection);
    }
  }

  private final static byte[] sHEX_LOOKUP = new byte[]{
      '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
  };

  /**
   * Creates the hexadecimal byte array equivalent to the given byte array.
   *
   * @param aWKB the byte array value to encode.
   * @return the hexadecimal encoded byte array.
   * @throws IOException if the byte array contains invalid values.
   */
  public static byte[] toHex(byte[] aWKB) throws IOException {
    int size = aWKB.length * 2;
    byte[] hex = new byte[size];
    for (int i = 0, c = 0; c < size; i++, c += 2) {
      int h = ((aWKB[i] & 0xF0) >> 4);
      int l = (aWKB[i] & 0xF);
      hex[c] = (byte) toHex(h);
      hex[c + 1] = (byte) toHex(l);
    }

    return hex;
  }

  private static int toHex(int aValue) throws IOException {
    if (aValue >= 0 && aValue <= 15) {
      return sHEX_LOOKUP[aValue];
    } else {
      throw new IOException("Illegal hex value: " + aValue);
    }
  }

  public interface UploadObjectHandler {
    public void prepareObject(Object aObject, PreparedStatement aPreparedStatement) throws SQLException, IOException;

    public void progress(double aProgress);
  }
}
