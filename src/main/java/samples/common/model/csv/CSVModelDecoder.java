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
package samples.common.model.csv;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;

import com.luciad.datamodel.ILcdDataObject;
import com.luciad.datamodel.TLcdDataModel;
import com.luciad.datamodel.TLcdDataProperty;
import com.luciad.datamodel.TLcdDataType;
import com.luciad.io.ILcdInputStreamFactory;
import com.luciad.io.ILcdInputStreamFactoryCapable;
import com.luciad.io.TLcdInputStreamFactory;
import com.luciad.model.ILcd2DBoundsIndexedModel;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelDecoder;
import com.luciad.model.ILcdModelDescriptor;
import com.luciad.model.ILcdModelReference;
import com.luciad.model.TLcd2DBoundsIndexedModel;
import com.luciad.reference.TLcdGeodeticReference;
import com.luciad.shape.ILcdPoint;
import com.luciad.shape.shape2D.TLcdLonLatPoint;
import com.luciad.shape.shape3D.TLcdLonLatHeightPoint;
import com.luciad.util.ILcdCharsetSettable;
import com.luciad.util.TLcdStringUtil;
import com.luciad.util.logging.ILcdLogger;
import com.luciad.util.logging.TLcdLoggerFactory;
import com.luciad.util.service.LcdService;

/**
 * This model decoder decodes point-based data in character-separated files.
 *
 * <h3>Input files</h3>
 *
 * <table class="simple">
 * <tr>
 * <th>File</th>
 * <th>Required</th>
 * <th>Entry point</th>
 * <th>Description</th>
 * </tr>
 * <tr>
 * <td>*.csv, *.tsv</td>
 * <td align="center">x</td>
 * <td align="center">x</td>
 * <td>File containing the data as character-separated values</td>
 * </tr>
 * The {@link #setSeparator(String) separator} and {@link #setCoordinateIndexHolder(CoordinateIndexHolder) geometry-containing fields}
 * can be specified; by default they are automatically detected.
 *
 * <h3>Supported file transfer protocols</h3>
 * <ul>
 * <li>This model decoder supports all transfer protocols that are supported by
 * the <code>inputStreamFactory</code> of this decoder.
 * </ul>
 *
 * <h3>Model structure</h3>
 * <ul>
 * <li>This model decoder creates a model per file.</li>
 * <li>All models returned by this model decoder implement {@link ILcd2DBoundsIndexedModel}.</li>
 * <li>The model reference is by default WGS 84, but can be {@link #setDefaultModelReference(ILcdModelReference) overridden}.</li>
 * <li>All models returned by this model decoder have a {@link CSVModelDescriptor}.</li>
 * </ul>
 *
 * <h3>Model elements</h3>
 * The supported shapes implement both {@link ILcdPoint} and {@link ILcdDataObject}.
 * Height values will be exposed as well, if present.
 *
 * <h3>Thread safety</h3>
 * <ul>
 * <li>The decoding of models is not thread-safe.</li>
 * <li>The decoded models are thread-safe for read access.</li>
 * </ul>
 *
 */
@LcdService(service = ILcdModelDecoder.class, priority = LcdService.LOW_PRIORITY)
public class CSVModelDecoder implements ILcdModelDecoder, ILcdCharsetSettable, ILcdInputStreamFactoryCapable {

  private static final String EMPTY_VALUE = "";
  private static ILcdLogger sLogger = TLcdLoggerFactory.getLogger(CSVModelDecoder.class.getName());

  private static final String DEFAULT_DISPLAY_NAME = "CSV";
  private static final String DEFAULT_EMPTY_VALUE = "";

  private static final char DEFAULT_QUOTE = '"';

  private String[] fExtensions = new String[]{".csv", ".tsv"};
  private String fCharacterSet = "UTF-8";
  private String fSeparator = null;

  private ILcdInputStreamFactory fInputStreamFactory = new TLcdInputStreamFactory();

  private CoordinateIndexHolder fCoordinateIndexHolder = null;
  private List<Field> fFields = null;
  private ILcdModelReference fDefaultModelReference = new TLcdGeodeticReference();
  private boolean fFieldNamesAtFirstRow = true;
  private boolean fAddID = false;

  private int fPieceLimit = 25 * 1000 * 1000;

  @Override
  public String getDisplayName() {
    return DEFAULT_DISPLAY_NAME;
  }

  /**
   * @return <code>true</code> if the file extension is &lt;defaultExtension&gt; or &lt;defaultExtension&gt;.gz,
   * <code>false</code> otherwise.
   */
  @Override
  public boolean canDecodeSource(String aSource) {
    for (String extension : fExtensions) {
      if (TLcdStringUtil.endsWithIgnoreCase(aSource, extension)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public ILcdModel decode(String aSource) throws IOException {
    if (!canDecodeSource(aSource)) {
      return null;
    }
    CoordinateIndexHolder coordinates = fCoordinateIndexHolder;
    List<Field> fields = fFields;
    String separator = fSeparator;
    TLcdDataType type;
    ILcdModel model;

    // detect
    Scanner scanner = getScanner(aSource);
    try {
      if (scanner.hasNext()) {
        String line = stripChar(scanner.nextLine(), DEFAULT_QUOTE);
        separator = separator == null ? detectSeparator(line, aSource) : separator;
        List<String> values = split(line, separator);
        coordinates = coordinates == null ? detectCoordinatesIndexes(values) : coordinates;
        fields = fields == null ? detectFields(values) : fields;
        if (coordinates == null || coordinates.getHorizontalAxisIndex() == -1 || coordinates.getVerticalAxisIndex() == -1) {
          throw new IOException("Could not detect coordinates.");
        }

        TLcdDataModel dataModel = CSVDataTypes.createDataModel(fields, fAddID);
        type = dataModel.getDeclaredType(CSVDataTypes.TYPE);

        ILcdModelDescriptor modelDescriptor = new CSVModelDescriptor(aSource,
                                                                     dataModel,
                                                                     Collections.singleton(type),
                                                                     Collections.singleton(type));
        model = new TLcd2DBoundsIndexedModel(fDefaultModelReference, modelDescriptor);
      } else {
        throw new IOException("Empty file");
      }

    } finally {
      scanner.close();
    }

    // cache properties for speed
    Map<String, TLcdDataProperty> properties = new HashMap<>();
    for (Field field : fields) {
      String property = field.getName();
      properties.put(property, type.getDeclaredProperty(property));
    }
    if (fAddID) {
      properties.put("ID", type.getDeclaredProperty("ID"));
    }

    // limit the size based on the number of decoded properties
    int maxLine = fPieceLimit / properties.size();

    // restart and parse
    scanner = getScanner(aSource);
    try {
      if (fFieldNamesAtFirstRow) {
        scanner.nextLine();
      }
      int lineIndex = 0;
      while (scanner.hasNext() && lineIndex < maxLine) {
        List<String> values = split(stripChar(scanner.nextLine(), DEFAULT_QUOTE), separator);
        try {

          ILcdPoint point = parsePoint(coordinates, values);
          if (point != null) {
            CSVRecord record = new CSVRecord(point, type);
            if (fAddID) {
              record.setValue("ID", lineIndex);
            }

            for (int i = 0; i < fields.size(); i++) {
              String property = fields.get(i).getName();
              String value = i >= values.size() ? DEFAULT_EMPTY_VALUE : values.get(i);
              record.setValue(properties.get(property), value);
            }

            model.addElement(record, ILcdModel.NO_EVENT);
          } else {
            sLogger.debug("No spatial values in line %s. Line skipped.", lineIndex);
          }
          lineIndex++;
        } catch (Exception e) {
          sLogger.warn(String.format("Could not parse line %s. Line skipped.", lineIndex));
          sLogger.trace(String.format("Could not parse line %s. Line skipped.", lineIndex), e);
        }
      }
      if (lineIndex >= maxLine) {
        sLogger.warn("Stopped decoding: data limit reached after reading " + lineIndex + " lines (" + fPieceLimit + " pieces of information).");
      }
    } finally {
      scanner.close();
    }

    return model;
  }

  /**
   * Sets the maximum amount of field values to parse.
   * This is a failsafe when parsing very large files to avoid out-of-memory exceptions.
   * By default the value is 25 million.
   * If your domain object has 5 fields, this would allow you to parse 5 million domain objects.
   *
   * @param aPieceLimit the maximum amount of field values to parse
   */
  public void setPieceLimit(int aPieceLimit) {
    fPieceLimit = aPieceLimit;
  }

  public int getPieceLimit() {
    return fPieceLimit;
  }

  private ILcdPoint parsePoint(CoordinateIndexHolder aCoordinates, List<String> aValues) {
    if (aCoordinates.getHorizontalAxisIndex() < aValues.size() &&
        aCoordinates.getVerticalAxisIndex() < aValues.size()) {
      double longitude = parseToDouble(aValues.get(aCoordinates.getHorizontalAxisIndex()));
      double latitude = parseToDouble(aValues.get(aCoordinates.getVerticalAxisIndex()));
      if (Double.isNaN(longitude) || Double.isNaN(latitude)) {
        return null;
      }

      if (aCoordinates.getHeightAxisIndex() != -1) {
        double altitude = parseToDouble(aValues.get(aCoordinates.getHeightAxisIndex()));
        return Double.isNaN(altitude) ? new TLcdLonLatPoint(longitude, latitude) : new TLcdLonLatHeightPoint(longitude, latitude, altitude);
      } else {
        return new TLcdLonLatPoint(longitude, latitude);
      }
    }
    return null;
  }

  private List<String> split(String aUnquotedLine, String aSeparator) {
    String[] splitResult = aUnquotedLine.split(aSeparator);
    List<String> values = new ArrayList<>(splitResult.length + (fAddID ? 1 : 0));
    for (String split : splitResult) {
      values.add(split.trim());
    }
    return values;
  }

  // this works better on header lines
  private String detectSeparator(String aLine, String aSource) {
    // tabs are fairly unambiguous
    if (aSource.toLowerCase().endsWith(".tsv") || aLine.split("\t").length > 1) {
      return "\t";
    }
    // so are semicolons
    if (aLine.split(";").length > 1) {
      return ";";
    }
    // and pipes
    if (aLine.split("\\|").length > 1) {
      return "\\|";
    }
    // fall back to the extension
    return ",";
  }

  private List<Field> detectFields(List<String> aValues) {
    return retrieveFields(fFieldNamesAtFirstRow ? aValues : createDummyFieldNames(aValues.size()));
  }

  public String getSeparator() {
    return fSeparator;
  }

  public void setSeparator(String aSeparator) {
    fSeparator = aSeparator;
  }

  public boolean isFieldNamesAtFirstRow() {
    return fFieldNamesAtFirstRow;
  }

  public void setFieldNamesAtFirstRow(boolean aFieldNamesAtFirstRow) {
    fFieldNamesAtFirstRow = aFieldNamesAtFirstRow;
  }

  public boolean isAddID() {
    return fAddID;
  }

  public void setAddID(boolean aAddID) {
    fAddID = aAddID;
  }

  /**
   * Sets the character set to use to decode CSV files.
   *
   * @param aCharacterSet the character set to use to decode CSV files.
   *                      The supported character sets depend on the JVM implementation
   *                      and can be found at {@link String#String(byte[], String)}.
   */
  @Override
  public void setCharacterSet(String aCharacterSet) {
    try {
      // small test to see if the character set is supported.
      byte[] testBytes = new byte[]{22, 125, -127};
      new String(testBytes, aCharacterSet);
      fCharacterSet = aCharacterSet;
    } catch (UnsupportedEncodingException e) {
      throw new IllegalArgumentException("Character set " + aCharacterSet + " not supported.", e);
    }
  }

  @Override
  public String getCharacterSet() {
    return fCharacterSet;
  }

  @Override
  public void setInputStreamFactory(ILcdInputStreamFactory aInputStreamFactory) {
    fInputStreamFactory = aInputStreamFactory;
  }

  public String[] getExtensions() {
    return fExtensions;
  }

  public void setExtensions(String[] aExtensions) {
    fExtensions = aExtensions;
  }

  @Override
  public ILcdInputStreamFactory getInputStreamFactory() {
    return fInputStreamFactory;
  }

  public CoordinateIndexHolder getCoordinateIndexHolder() {
    return fCoordinateIndexHolder;
  }

  public void setCoordinateIndexHolder(CoordinateIndexHolder aCoordinateIndexHolder) {
    fCoordinateIndexHolder = aCoordinateIndexHolder;
  }

  public List<Field> getFieldNames() {
    return fFields;
  }

  /**
   * Sets the fields to parse out of a CSV row.
   * This is useful if the file itself does not contain a header row, or if you want to override that information.
   * By default the value is null, in which case the decoded files are expected to have a header row.
   *
   * @param aFields the fields to parse
   * @see #setFieldNamesAtFirstRow(boolean)
   */
  public void setFieldNames(List<Field> aFields) {
    fFields = aFields;
  }

  public ILcdModelReference getDefaultModelReference() {
    return fDefaultModelReference;
  }

  public void setDefaultModelReference(ILcdModelReference aDefaultModelReference) {
    fDefaultModelReference = aDefaultModelReference;
  }

  private Scanner getScanner(String aSource) throws IOException {
    InputStream inputStream = getInputStreamFactory().createInputStream(aSource);
    if (inputStream == null) {
      throw new IOException("Could not create an input stream for " + aSource);
    }
    return new Scanner(inputStream, fCharacterSet);
  }

  private List<Field> retrieveFields(List<String> aStrings) {
    List<Field> result = new ArrayList<>(aStrings.size());
    for (String value : aStrings) {
      String key = escapeNonLetterOrDigitChars(value);
      result.add(new Field(key, value));
    }
    return result;
  }

  private static String escapeNonLetterOrDigitChars(String aName) {
    StringBuilder builder = new StringBuilder(aName.length());
    for (int i = 0; i < aName.length(); i++) {
      char c = aName.charAt(i);
      if (!Character.isLetterOrDigit(c)) {
        c = '_';
      }
      builder.append(c);
    }
    return builder.toString();
  }

  private double parseToDouble(String aValue) {
    String value = stripChar(aValue, ' ');

    if (!aValue.isEmpty()) { // optimization to avoid heaps of exceptions
      try {
        return Double.parseDouble(value.replace(',', '.'));
      } catch (Exception e) {
        // not interested in this
      }
    }

    return Double.NaN;
  }

  private CoordinateIndexHolder detectCoordinatesIndexes(List<String> aStrings) {
    CoordinateIndexHolder result = new CoordinateIndexHolder();
    for (int i = 0; i < aStrings.size(); i++) {
      String value = aStrings.get(i);
      if (value == null || value.equals(DEFAULT_EMPTY_VALUE)) {
        continue;
      }

      if (result.getVerticalAxisIndex() == -1 &&
          (value.toLowerCase(Locale.ENGLISH).contains("latitude") ||
           value.toLowerCase(Locale.ENGLISH).contains("lat") ||
           value.toLowerCase(Locale.ENGLISH).contains("northing") ||
           value.toLowerCase(Locale.ENGLISH).contains("north") ||
           value.toLowerCase(Locale.ENGLISH).equals("y"))) {
        result.setVerticalAxisIndex(i);
      }

      if (result.getHorizontalAxisIndex() == -1 &&
          (value.toLowerCase(Locale.ENGLISH).contains("longitude") ||
          value.toLowerCase(Locale.ENGLISH).contains("long") ||
          value.toLowerCase(Locale.ENGLISH).contains("lon") ||
          value.toLowerCase(Locale.ENGLISH).contains("easting") ||
          value.toLowerCase(Locale.ENGLISH).contains("east") ||
          value.toLowerCase(Locale.ENGLISH).equals("x"))) {
        result.setHorizontalAxisIndex(i);
      }

      if (result.getHeightAxisIndex() == -1 &&
          (value.toLowerCase(Locale.ENGLISH).contains("altitude") ||
          value.toLowerCase(Locale.ENGLISH).contains("height") ||
          value.toLowerCase(Locale.ENGLISH).contains("elevation") ||
          value.toLowerCase(Locale.ENGLISH).equals("z"))) {
        result.setHeightAxisIndex(i);
      }
    }
    return result;
  }

  private List<String> createDummyFieldNames(int aNumberOfFieldNames) {
    List<String> fieldNames = new ArrayList<>(aNumberOfFieldNames);
    for (int i = 1; i <= aNumberOfFieldNames; i++) {
      fieldNames.add("Field_" + i);
    }
    return fieldNames;
  }

  /**
   * Removes all occurrences of the given character in the {@code String provided}.
   */
  private static String stripChar(String aString, char aChar) {
    return aString.replaceAll(String.valueOf(aChar), EMPTY_VALUE);
  }

  /**
   * CSV field description.
   */
  public static class Field {
    private final String fName;
    private final String fDisplayName;

    public Field(String aKey, String aDisplayName) {
      fName = aKey;
      fDisplayName = aDisplayName;
    }

    public String getName() {
      return fName;
    }

    public String getDisplayName() {
      return fDisplayName;
    }

    @Override
    public String toString() {
      return getDisplayName();
    }
  }

  /**
   * Specifies the coordinate column indices in a CSV file.
   */
  public static class CoordinateIndexHolder {

    private int fHorizontalAxisIndex = -1;
    private int fVerticalAxisIndex = -1;
    private int fHeightAxisIndex = -1;

    public void setHorizontalAxisIndex(int aIndex) {
      fHorizontalAxisIndex = aIndex;
    }

    public void setVerticalAxisIndex(int aIndex) {
      fVerticalAxisIndex = aIndex;
    }

    public void setHeightAxisIndex(int aIndex) {
      fHeightAxisIndex = aIndex;
    }

    public int getHorizontalAxisIndex() {
      return fHorizontalAxisIndex;
    }

    public int getVerticalAxisIndex() {
      return fVerticalAxisIndex;
    }

    public int getHeightAxisIndex() {
      return fHeightAxisIndex;
    }
  }
}
