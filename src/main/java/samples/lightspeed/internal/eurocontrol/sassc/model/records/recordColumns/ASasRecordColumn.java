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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import samples.lightspeed.internal.eurocontrol.sassc.model.records.TSasDbExpressionDefinition;

/**
 * A record column is a table of values of a specific type that represent a characteristic of all the records. For
 * instance, the latitude of all records will be stored in a record column.
 *
 * @author patrickd
 */
public abstract class ASasRecordColumn {
  private TSasDbExpressionDefinition fDefinition;

  /**
   * Create a new record column based on the given definition.
   *
   * @param aDefinition The record column definition.
   */
  protected ASasRecordColumn(TSasDbExpressionDefinition aDefinition) {
    fDefinition = aDefinition;
  }

  /**
   * Return the definition of this record column.
   *
   * @return the definition of this record column.
   */
  public TSasDbExpressionDefinition getDefinition() {
    return fDefinition;
  }

  protected void setDefinition(TSasDbExpressionDefinition aDefinition) {
    this.fDefinition = aDefinition;
  }

  /**
   * Set a value at the given index into the record column.
   *
   * @param aIndex       The index in the record column.
   * @param aColumnValue The value to set as it is returned by the JDBC ResultSet.
   */
  public abstract void setValue(int aIndex, Object aColumnValue);

  /**
   * Return the value at the given index as an object.
   *
   * @param aIndex The index in the column.
   * @return The value.
   */
  public abstract Object getObject(int aIndex);

  /**
   * Get the value.
   *
   * @param aIndex the value's index
   * @return If it's null then return the code value for null else, return the value
   */
  public abstract Object getObjectOrNullCode(int aIndex);

  /**
   * Return whether the value at given index is null.
   *
   * @param aIndex The index in the column.
   * @return True if the value is null.
   */
  public abstract boolean isNull(int aIndex);

  /**
   * Value used in the record column to indicate a NULL value in the database, but as an Object.
   *
   * @return The value.
   */
  public abstract Object getNullValueAsObject();

  /**
   * Store this into a binary file. The name of the file is based on the definition of the record column.
   *
   * @param aDirectory Directory in which to save the column.
   */
  public void store(String aDirectory) {
    try {
      File output_file = new File(createRecordColumnFilePath(aDirectory, getDefinition()));
      output_file.setWritable(true, false);
      ObjectOutputStream output_stream = null;
      try {
        output_stream = new ObjectOutputStream(
            new BufferedOutputStream(new FileOutputStream(
                output_file))
        );
        writeHeader(output_stream, getDefinition());
        storeValues(output_stream);
      } finally {
        if (output_stream != null) {
          output_stream.flush();
          output_stream.close();
        }
      }
    } catch (IOException aException) {
      //TODO: add proper exception throwing throughout this class
    }
  }

  /**
   * Create the standard filename and path for a column based on a given directory and an expression definition.
   *
   * @param aDirectory  The directory where the column is stored.
   * @param aDefinition The column definition.
   * @return The full path to the column.
   */
  public static String createRecordColumnFilePath(String aDirectory, TSasDbExpressionDefinition aDefinition) {
    return aDirectory + File.separator + aDefinition.getName() + ".rc";
  }

  /**
   * Write the column header to an output stream. This is the part that comes before what's written by {@link
   * #storeValues(ObjectOutputStream)}.
   *
   * @param aOutputStream The stream to write to.
   * @param aDefinition   The column definition.
   * @throws IOException If there is a problem writing the column header.
   */
  protected static void writeHeader(ObjectOutputStream aOutputStream, TSasDbExpressionDefinition aDefinition)
      throws IOException {
    aOutputStream.writeObject(aDefinition.getName());
    aOutputStream.writeInt(aDefinition.getAliases().size());
    for (Object key : aDefinition.getAliases().keySet()) {
      Object value = aDefinition.getAliases().get(key);
      aOutputStream.writeObject(key);
      aOutputStream.writeObject(value);
    }
  }

  abstract protected void storeValues(ObjectOutputStream aOutputStream) throws IOException;

  /**
   * Load column data from a binary file and store it into the current column. The definition of this column needs to
   * correspond to the definition of the column that is stored in the file (name is checked). Loading data into the
   * column will replace any data that was already stored in it. The size of the column is adapted to the size of the
   * stored column.
   *
   * @param aFileName The file to load.
   */
  public void load(String aFileName) {
    ObjectInputStream input_stream = null;
    try {
      input_stream = new ObjectInputStream(new BufferedInputStream(new FileInputStream(aFileName)));
      String definitionName = (String) input_stream.readObject();
      if (!definitionName.equals(fDefinition.getName())) {
        //TODO
      }
      int nrAliases = input_stream.readInt();
      HashMap<Object, Object> aliases = new HashMap<Object, Object>();
      for (int i = 0; i < nrAliases; i++) {
        Object value = input_stream.readObject();
        Object alias = input_stream.readObject();
        aliases.put(value, alias);
      }
      fDefinition.setAliases(aliases);
      loadValues(input_stream);
    } catch (IOException e) {
      //TODO
    } catch (ClassNotFoundException e) {
      //TODO
    } finally {
      try {
        if (input_stream != null) {
          input_stream.close();
        }
      } catch (IOException e) {
        // nothing can be done
      }
    }
  }

  /**
   * Load column values from the given input stream. This should read what is written by {@link
   * #storeValues(ObjectOutputStream)}. Loading values shall adapt the size of the column to the number of
   * values stored in the file. Any data present in the column is overwritten.
   *
   * @param aInputStream Stream to read from.
   * @throws IOException            If there is a problem reading the file.
   * @throws ClassNotFoundException If a class that was written to the file is not found.
   */
  abstract protected void loadValues(ObjectInputStream aInputStream) throws IOException,
                                                                            ClassNotFoundException;

  @Override
  public String toString() {
    return super.toString() + "(" + fDefinition.getName() + ")";
  }

  /**
   * Check whether the column is empty.
   *
   * @return True if the column is empty.
   */
  abstract public boolean isEmpty();

  /**
   * Get the number of values in this record column.
   *
   * @return The number of values.
   */
  abstract public int getSize();

  /**
   * Collect all distinct values in this record column.
   *
   * @return a List of distinct values
   */
  public Collection<Object> collectDistinctValues() {
    Set<Object> values = new HashSet<Object>();
    for (int index = 0; index < getSize(); index++) {
      Object value = getObjectOrNullCode(index);
      if (!values.contains(value)) {
        values.add(value);
      }
    }
    return values;
  }

}
