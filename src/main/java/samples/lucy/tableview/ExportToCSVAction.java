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
package samples.lucy.tableview;

import java.awt.event.ActionEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JTable;
import javax.swing.filechooser.FileFilter;

import org.jdesktop.swingx.JXTable;

import com.luciad.gui.ALcdAction;
import com.luciad.gui.TLcdAWTUtil;
import com.luciad.gui.TLcdCompositeIcon;
import com.luciad.gui.TLcdIconFactory;
import com.luciad.gui.TLcdResizeableIcon;
import com.luciad.lucy.util.language.TLcyLang;
import com.luciad.util.logging.ILcdLogger;
import com.luciad.util.logging.TLcdLoggerFactory;

/**
 * <p>Action to export a {@link JTable} to a CSV file. The table must be set before this
 * action will be enabled.</p>
 *
 * <p>It will show a file chooser, allowing to set the destination csv file. The file chooser also
 * allows to modify the separator.</p>
 */
class ExportToCSVAction extends ALcdAction {
  private static final ILcdLogger LOGGER = TLcdLoggerFactory.getLogger(ExportToCSVAction.class.getName());
  private static final String EXTENSION = ".csv";
  private JXTable fTable;

  /**
   * Create a new action
   */
  public ExportToCSVAction() {
    this(null);
  }

  public ExportToCSVAction(JXTable aTable) {
    super("ExportToCSVAction");
    TLcdCompositeIcon composite = new TLcdCompositeIcon();
    composite.addIcon(new TLcdResizeableIcon(TLcdIconFactory.create(TLcdIconFactory.TABLE_ICON)));
    composite.addIcon(new TLcdResizeableIcon(TLcdIconFactory.create(TLcdIconFactory.CONVERT_DECO_ICON)));
    setIcon(composite);
    fTable = aTable;
  }

  @Override
  public boolean isEnabled() {
    return super.isEnabled() && fTable != null;
  }

  /**
   * Returns the table which will be exported to CSV
   *
   * @return the table which will be exported to CSV. Can be <code>null</code>.
   */
  public JTable getTable() {
    return fTable;
  }

  /**
   * Sets the table which will be exported to CSV. A non-null table must be set before this action
   * will be enabled.
   *
   * @param aTable the table which will be exported to CSV. May be <code>null</code>.
   */
  public void setTable(JXTable aTable) {
    fTable = aTable;
  }

  @Override
  public void actionPerformed(ActionEvent aEvent) {
    File file = selectFile(aEvent);

    if (file != null) {
      saveToCSV(file);
    }
  }

  File selectFile(ActionEvent aEvent) {
    //show JFileChooser
    JFileChooser fileChooser = new JFileChooser();
    fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
    fileChooser.setFileFilter(new FileFilter() {
      @Override
      public boolean accept(File f) {
        //allow directories or files with the correct extension
        return f.isDirectory() || EXTENSION.equals(getExtension(f));
      }

      @Override
      public String getDescription() {
        return TLcyLang.getString("CSV files");
      }
    });
    int result = fileChooser.showSaveDialog(TLcdAWTUtil.findParentFrame(aEvent.getSource()));

    File file = null;

    if (result == JFileChooser.APPROVE_OPTION) {
      file = fileChooser.getSelectedFile();
      file = checkExtension(file);
    }
    return file;
  }

  /**
   * Verify whether the file <code>aFile</code> has the correct extension, and change the extension
   * if necessary
   * @param aFile the file to check
   * @return a file with the correct extension
   */
  private File checkExtension(File aFile) {
    if (EXTENSION.equals(getExtension(aFile))) {
      return aFile;
    }
    return new File(aFile.getAbsolutePath() + EXTENSION);
  }

  /**
   * Returns the extension of the file <code>aFile</code>
   * @param aFile the file
   * @return the extension of the file in lowercase, including the '.'
   */
  private String getExtension(File aFile) {
    String name = aFile.getName();
    int index = name.lastIndexOf('.');
    if (index > 0 && index < name.length() - 1) {
      return name.substring(index).toLowerCase();
    } else {
      return "";
    }
  }

  /**
   * Perform the actual export of the table model to a CSV file
   *
   * @param aDestinationFile the CSV file
   */
  private void saveToCSV(File aDestinationFile) {
    try {
      BufferedWriter filewriter = new BufferedWriter(new FileWriter(aDestinationFile));

      String separator = ";";

      //first store the column headers
      int columnCount = fTable.getColumnCount();
      String headers = "";
      for (int i = 0; i < columnCount; i++) {
        headers = headers + escapeString(fTable.getModel().getColumnName(fTable.convertColumnIndexToModel(i)));
        if (i != columnCount - 1) {
          headers = headers + separator;
        }
      }
      filewriter.write(headers);
      filewriter.newLine();

      //loop over all rows
      int rowCount = fTable.getRowCount();
      for (int rowCounter = 0; rowCounter < rowCount; rowCounter++) {
        String row = "";
        for (int columnCounter = 0; columnCounter < columnCount; columnCounter++) {
          String objectToSave = fTable.getStringAt(rowCounter, columnCounter);
          row = row + escapeString(objectToSave);
          if (columnCounter != columnCount - 1) {
            row = row + separator;
          }
        }
        filewriter.write(row);
        filewriter.newLine();
      }

      //close writer to unlock and flush to disk
      filewriter.close();
    } catch (IOException aError) {
      LOGGER.error("Could not create output stream to file", aError);
      throw new RuntimeException(aError);
    }
  }

  /**
   * Escape all necessary characters in the String so it can be savely exported to a CSV file
   *
   * @param aString the String in which all special characters must be escaped
   *
   * @return a valid String representation of <code>aString</code>
   */
  private String escapeString(String aString) {
    if (aString == null) {
      aString = "";
    }
    //escape all "
    aString = aString.replaceAll("\"", "\"\"");
    return "\"" + aString + "\"";
  }
}
