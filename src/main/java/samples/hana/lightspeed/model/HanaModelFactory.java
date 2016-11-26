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
import java.sql.SQLException;

import javax.swing.JOptionPane;

import com.luciad.model.ILcdModel;
import com.luciad.util.TLcdStatusEvent;
import com.luciad.util.logging.ILcdLogger;
import com.luciad.util.logging.TLcdLoggerFactory;

import samples.gxy.common.ProgressUtil;
import samples.hana.lightspeed.common.Configuration;

/**
 * Abstract class that can be used to create models from a Hana database. If no data is found, this class adds
 * support to upload sample data to the database.
 */
public abstract class HanaModelFactory<T extends ILcdModel> {

  private static final ILcdLogger sLogger = TLcdLoggerFactory.getLogger(HanaModelFactory.class);

  private static final String SCHEMA_NAME = Configuration.get("database.schema");

  private final Component fParentComponent;
  private final HanaConnectionExecutorService fExecutorService;
  private final String fDataDisplayName;
  private final String fTableName;
  private final String fColumnDefinition;
  private final String fInsertObjectQuery;

  protected HanaModelFactory(Component aParentComponent,
                             HanaConnectionExecutorService aExecutorService,
                             String aDataDisplayName,
                             String aTableName, String aColumnDefinition, String aInsertObjectQuery) {
    fParentComponent = aParentComponent;
    fExecutorService = aExecutorService;
    fDataDisplayName = aDataDisplayName;
    fTableName = aTableName;
    fColumnDefinition = aColumnDefinition;
    fInsertObjectQuery = aInsertObjectQuery;
  }

  public T createModel() {
    HanaDatabaseUtil databaseUtil = new HanaDatabaseUtil(fExecutorService);

    boolean tableHasData = databaseUtil.tableExists(SCHEMA_NAME, fTableName) &&
                           !databaseUtil.tableIsEmpty(SCHEMA_NAME, fTableName);
    if (!tableHasData) {
      tableHasData = uploadData(databaseUtil) &&
                     databaseUtil.tableExists(SCHEMA_NAME, fTableName) &&
                     !databaseUtil.tableIsEmpty(SCHEMA_NAME, fTableName);
      if (!tableHasData) {
        return null;
      }
    }
    return createModel(fExecutorService);
  }

  protected abstract T createModel(HanaConnectionExecutorService aExecutorService);

  protected abstract ILcdModel createSampleDataModel() throws IOException;

  protected abstract HanaDatabaseUtil.UploadObjectHandler createUploadObjectHandler();

  private boolean uploadData(HanaDatabaseUtil aDatabaseUtil) {
    String message;
    boolean schemaExists = aDatabaseUtil.schemaExists(SCHEMA_NAME);
    boolean tableExists = schemaExists;
    if (!schemaExists) {
      message = "Schema '" + SCHEMA_NAME + "' does not exist.\nPress Yes to create schema '" + SCHEMA_NAME + "', table '" + fTableName + "' and upload sample " + fDataDisplayName + " data.";
    } else {
      tableExists = aDatabaseUtil.tableExists(SCHEMA_NAME, fTableName);
      if (!tableExists) {
        message = "Table '" + fTableName + "' does not exist.\nPress Yes to create table '" + fTableName + "' and upload sample " + fDataDisplayName + " data.";
      } else {
        message = "No " + fDataDisplayName + " data found.\nPress Yes to upload sample " + fDataDisplayName + " data.";
      }
    }
    message += "\n\nNote that the schema and table name can be configured in the config.properties file.";
    int result = JOptionPane.showConfirmDialog(fParentComponent, message, "Upload " + fDataDisplayName + " data ?", JOptionPane.YES_NO_OPTION);
    if (result == JOptionPane.NO_OPTION) {
      return false;
    }

    final ProgressUtil.ProgressDialog progressDialog = ProgressUtil.createProgressDialog(fParentComponent, "Uploading " + fDataDisplayName + " data...", false);
    progressDialog.setFocusableWindowState(false);
    ProgressUtil.showDialog(progressDialog);

    try (TLcdStatusEvent.Progress progress = TLcdStatusEvent.startProgress(progressDialog, this, "Uploading data")) {
      if (!schemaExists) {
        if (!aDatabaseUtil.createSchema(SCHEMA_NAME)) {
          throw new IllegalArgumentException("Creating schema failed: " + SCHEMA_NAME);
        }
      }
      if (!tableExists) {
        if (!aDatabaseUtil.createTable(SCHEMA_NAME, fTableName, fColumnDefinition)) {
          throw new IllegalArgumentException("Creating table failed: " + SCHEMA_NAME + "." + fTableName);
        }
      }

      ILcdModel sampleDataModel = createSampleDataModel();
      try {
        aDatabaseUtil.uploadModelObjects(createUploadObjectHandler(progress), fInsertObjectQuery, sampleDataModel);
      } catch (SQLException e) {
        sLogger.error(e.getMessage(), e);
        return false;
      }

      progress.progress(1.0, "Merging delta");
      if (!aDatabaseUtil.mergeTable(SCHEMA_NAME, fTableName)) {
        throw new IllegalArgumentException("Merging delta of table failed: " + SCHEMA_NAME + "." + fTableName);
      }
      return true;
    } catch (IOException e) {
      sLogger.error(e.getMessage(), e);
      return false;
    } finally {
      ProgressUtil.hideDialog(progressDialog, true);
    }
  }

  private HanaDatabaseUtil.UploadObjectHandler createUploadObjectHandler(final TLcdStatusEvent.Progress aStatusProgress) {
    final HanaDatabaseUtil.UploadObjectHandler handler = createUploadObjectHandler();
    return new HanaDatabaseUtil.UploadObjectHandler() {
      @Override
      public void prepareObject(Object aObject, PreparedStatement aPreparedStatement) throws SQLException, IOException {
        handler.prepareObject(aObject, aPreparedStatement);
      }

      @Override
      public void progress(double aProgress) {
        handler.progress(aProgress);
        String message = "Uploading " + fDataDisplayName + " data... " + (int) (aProgress * 100.0) + "%";
        aStatusProgress.progress(aProgress, message);
      }
    };
  }
}
