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
package samples.lightspeed.internal.eurocontrol.sassc.model.records;

import static samples.lightspeed.internal.eurocontrol.sassc.model.records.ESasDbExpressionDefinitionPropertyName.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 *
 */
public class TSasDbExpressionDefinition implements Comparable<TSasDbExpressionDefinition> {

  /**
   * Format String for text.
   */
  public static final String FORMAT_TEXT = "TEXT";
  /**
   * Format String for decimal values.
   */
  public static final String FORMAT_DECIMAL = "DECIMAL";
  /**
   * Format String for octal values.
   */
  public static final String FORMAT_OCTAL = "OCTAL";
  /**
   * Format String for hexadecimal values.
   */
  public static final String FORMAT_HEXADECIMAL = "HEXADECIMAL";
  /**
   * Format String for short time.
   */
  public static final String FORMAT_SHORT_TIME = "SHORT_TIME";
  /**
   * Format String for long time.
   */
  public static final String FORMAT_LONG_TIME = "LONG_TIME";

  private static final String JOIN_CLAUSE_PROPERTY = " join";

  private String fName;
  private String fTableName;
  private String fSqlExpression;
  private String fWhereClause;
  private String fGroupBy;
  private String fDisplayName;
  private ESasJavaType fJavaType;
  private String fFormat;
  private String fDefaultNullValue;
  private boolean fAllowUnloading;
  private int fColumnIndex;
  private boolean fIsLoaded;
  private boolean fMultiValue;
  private String fAliasTableName;
  private String fAliasValuesColumnName;
  private String fAliasNamesColumnName;
  private boolean fDelayedExtraction;
  private boolean fExtract;
  private boolean fDiscrete;
  private Map<Object, Object> fAliases = new HashMap<Object, Object>();
  private Map<Object, Object> fInvertedAliases = new HashMap<Object, Object>();

  public TSasDbExpressionDefinition(String aName, String aTableName, String aSqlExpression,
                                    String aWhereClause, String aGroupBy, ESasJavaType aJavaType,
                                    String aFormat, String aDefaultNullValue, String aDisplayName,
                                    boolean aAllowUnloading, boolean aMultiValue, boolean aDelayedExtraction,
                                    boolean aDiscrete) {
    fName = aName;
    fTableName = aTableName;
    fSqlExpression = aSqlExpression;
    fWhereClause = aWhereClause;
    fGroupBy = aGroupBy;
    fJavaType = aJavaType;
    fFormat = aFormat;
    fDefaultNullValue = aDefaultNullValue;
    fDisplayName = aDisplayName;
    fAllowUnloading = aAllowUnloading;
    fMultiValue = aMultiValue;
    fDelayedExtraction = aDelayedExtraction;
    fExtract = false;
    fDiscrete = aDiscrete;
  }

  public static TSasDbExpressionDefinition load(Properties aProps, int aIndex) {
    String name = aProps.getProperty(NAME_PROPERTY.getPropertyName(aIndex)).trim();
    String tableName = aProps.getProperty(TABLE_NAME_PROPERTY.getPropertyName(aIndex)).trim();
    String expression = aProps.getProperty(SQL_EXPRESSION_PROPERTY.getPropertyName(aIndex)).trim();

    String whereClause = aProps.getProperty(WHERE_CLAUSE_PROPERTY.getPropertyName(aIndex));
    String groupBy = aProps.getProperty(GROUP_BY_CLAUSE_PROPERTY.getPropertyName(aIndex));
    String displayName = aProps.getProperty(DISPLAY_NAME_PROPERTY.getPropertyName(aIndex));
    String defaultNullValue = aProps.getProperty(DEFAULT_NULL_VALUE.getPropertyName(aIndex));

    final String allowUnloadingProperty = aProps.getProperty(ALLOW_UNLOADING_PROPERTY.getPropertyName(aIndex));
    final String multiValueProperty = aProps.getProperty(IS_MULTIPLE_VALUE_PROPERTY.getPropertyName(aIndex));
    final String delayedExtractionProperty = aProps.getProperty(IS_DELAYED_PROPERTY.getPropertyName(aIndex));
    final String discreteProperty = aProps.getProperty(IS_DISCRETE_PROPERTY.getPropertyName(aIndex));

    ESasJavaType
        javaType =
        ESasJavaType.valueOf(aProps.getProperty(TYPE_PROPERTY.getPropertyName(aIndex)).trim());
    String format = aProps.getProperty(FORMAT_PROPERTY.getPropertyName(aIndex));
    String alias = aProps.getProperty(ALIAS_PROPERTY.getPropertyName(aIndex));

    if (whereClause != null) {
      whereClause.trim();
    }
    if (groupBy != null) {
      groupBy.trim();
    }
    if (displayName != null) {
      displayName.trim();
    }
    if (defaultNullValue != null) {
      defaultNullValue.trim();
    }

    boolean allowUnloading = allowUnloadingProperty != null &&
                             Boolean.parseBoolean(allowUnloadingProperty.trim());
    boolean multiValue = multiValueProperty != null &&
                         Boolean.parseBoolean(multiValueProperty.trim());
    boolean delayedExtraction = delayedExtractionProperty != null &&
                                Boolean.parseBoolean(delayedExtractionProperty.trim());
    boolean discrete = discreteProperty != null &&
                       Boolean.parseBoolean(discreteProperty.trim());

    TSasDbExpressionDefinition result =
        new TSasDbExpressionDefinition(name, tableName, expression, whereClause, groupBy,
                                       javaType, format, defaultNullValue, displayName, allowUnloading,
                                       multiValue, delayedExtraction, discrete);

    if (alias != null) {
      alias = alias.trim();
      String[] aliasProperties = alias.split(VALUE_SEPARATOR.getPropertyName(0));
      if (aliasProperties.length >= 3) {
        if (javaType != ESasJavaType.INT) {
          //TODO: add some exception throwing here

        } else {
          result.setAlias(aliasProperties[0].trim(),
                          aliasProperties[1].trim(),
                          aliasProperties[2].trim());
        }
      } else {
        //TODO: add some exception throwing here
      }
    }
    return result;
  }

  public static boolean containsDefinition(Properties aProps, int aIndex) {
    String name = aProps.getProperty(NAME_PROPERTY.getPropertyName(aIndex));
    return name != null;
  }

  public String getName() {
    return fName;
  }

  public void setName(String aName) {
    fName = aName;
  }

  public String getTableName() {
    return fTableName;
  }

  public void setTableName(String aTableName) {
    fTableName = aTableName;
  }

  public String getSqlExpression() {
    return fSqlExpression;
  }

  public void setSqlExpression(String aSqlExpression) {
    fSqlExpression = aSqlExpression;
  }

  public String getWhereClause() {
    return fWhereClause;
  }

  public void setWhereClause(String aWhereClause) {
    fWhereClause = aWhereClause;
  }

  public String getGroupBy() {
    return fGroupBy;
  }

  public void setGroupBy(String aGroupBy) {
    fGroupBy = aGroupBy;
  }

  public String getDisplayName() {
    return fDisplayName;
  }

  public void setDisplayName(String aDisplayName) {
    fDisplayName = aDisplayName;
  }

  public ESasJavaType getJavaType() {
    return fJavaType;
  }

  public void setJavaType(ESasJavaType aJavaType) {
    fJavaType = aJavaType;
  }

  public String getFormat() {
    return fFormat;
  }

  public void setFormat(String aFormat) {
    fFormat = aFormat;
  }

  public String getDefaultNullValue() {
    return fDefaultNullValue;
  }

  public boolean hasDefaultValue() {
    return getDefaultNullValue() != null && !getDefaultNullValue().trim().isEmpty();
  }

  public void setDefaultNullValue(String aDefaultNullValue) {
    fDefaultNullValue = aDefaultNullValue;
  }

  public boolean isAllowUnloading() {
    return fAllowUnloading;
  }

  public void setAllowUnloading(boolean aAllowUnloading) {
    fAllowUnloading = aAllowUnloading;
  }

  public int getColumnIndex() {
    return fColumnIndex;
  }

  public void setColumnIndex(int aColumnIndex) {
    fColumnIndex = aColumnIndex;
  }

  public boolean isIsLoaded() {
    return fIsLoaded;
  }

  public void setIsLoaded(boolean aIsLoaded) {
    fIsLoaded = aIsLoaded;
  }

  public boolean isMultiValue() {
    return fMultiValue;
  }

  public void setMultiValue(boolean aMultiValue) {
    fMultiValue = aMultiValue;
  }

  public boolean isDelayedExtraction() {
    return fDelayedExtraction;
  }

  public void setDelayedExtraction(boolean aDelayedExtraction) {
    fDelayedExtraction = aDelayedExtraction;
  }

  public boolean isDiscrete() {
    return fDiscrete;
  }

  public void setDiscrete(boolean aDiscrete) {
    fDiscrete = aDiscrete;
  }

  public boolean isExtract() {
    return fExtract;
  }

  public void setExtract(boolean aExtract) {
    fExtract = aExtract;
  }

  public String getAliasTableName() {
    return fAliasTableName;
  }

  public void setAliasTableName(String aAliasTableName) {
    fAliasTableName = aAliasTableName;
  }

  public String getAliasValuesColumnName() {
    return fAliasValuesColumnName;
  }

  public void setAliasValuesColumnName(String aAliasValuesColumnName) {
    fAliasValuesColumnName = aAliasValuesColumnName;
  }

  public String getAliasNamesColumnName() {
    return fAliasNamesColumnName;
  }

  public void setAliasNamesColumnName(String aAliasNamesColumnName) {
    fAliasNamesColumnName = aAliasNamesColumnName;
  }

  public boolean isSimple() {
    return !hasJoin() &&
           !hasWhereClause() &&
           !hasGroupBy();
  }

  public boolean isNumeric(boolean acceptOnlyBase10) {
    boolean isNumeric = getJavaType() == ESasJavaType.FLOAT ||
                        getJavaType() == ESasJavaType.INT ||
                        getJavaType() == ESasJavaType.LONG;
    boolean acceptFormat = (!acceptOnlyBase10) ||
                           (acceptOnlyBase10 && getFormat() == null) ||
                           (acceptOnlyBase10 &&
                            !getFormat().equals(FORMAT_HEXADECIMAL) &&
                            !getFormat().equals(FORMAT_OCTAL));
    return isNumeric && acceptFormat;
  }

  public boolean hasJoin() {
    return fTableName.contains(VALUE_SEPARATOR.getPropertyName(0)) ||
           fTableName.contains(JOIN_CLAUSE_PROPERTY);
  }

  public boolean hasWhereClause() {
    return ((fWhereClause != null) && (!fWhereClause.isEmpty()));
  }

  public boolean hasGroupBy() {
    return ((fGroupBy != null) && (!fGroupBy.isEmpty()));
  }

  public TableName getRecordsTableName() {
    return TableName.getTableName(getName().substring(0, getName().indexOf('.')));
  }

  public Object getAliasFor(Object aValue) {
    return fAliases.get(aValue.toString());
  }

  public Object getInvertedAliasfor(Object aAlias) {
    return fInvertedAliases.get(aAlias);
  }

  public void setAlias(String aTableName, String aValuesColumn, String aNamesColumn) {
    fAliasTableName = aTableName;
    fAliasValuesColumnName = aValuesColumn;
    fAliasNamesColumnName = aNamesColumn;
  }

  public void setAliases(HashMap<Object, Object> aAliasesMap) {
    fAliases = aAliasesMap;
    fInvertedAliases = new HashMap<Object, Object>();
    for (Object key : fAliases.keySet()) {
      fInvertedAliases.put(fAliases.get(key), key);
    }
  }

  public Map<Object, Object> getAliases() {
    return fAliases;
  }

  @Override
  public String toString() {
    return getDisplayName();
  }

  @Override
  public boolean equals(Object aObject) {
    if (this == aObject) {
      return true;
    }

    if (!(aObject instanceof TSasDbExpressionDefinition)) {
      return false;
    }

    TSasDbExpressionDefinition that = (TSasDbExpressionDefinition) aObject;

    return (fName == null ? that.fName == null : fName.equals(that.fName));
  }

  @Override
  public int hashCode() {
    return 527 + fName.hashCode();
  }

  @Override
  public int compareTo(TSasDbExpressionDefinition aDefinition) {
    if (aDefinition == null || aDefinition.toString() == null) {
      return 1;
    }
    if (toString() == null) {
      return -1;
    }
    return toString().compareToIgnoreCase(aDefinition.toString());
  }

  /**
   * Database tablenames.
   */
  // This is needed because some tasks depends on the name of the tables of the database (!?)
  public static enum TableName {
    ADS_REPORT("SD_ADS"),
    MLAT_REPORT("SD_MLAT"),   // table containing multilateration data
    ADS_DATASOURCE("DS_ADS"),  // the ADS sensor table name
    DATA_SOURCES("LE_DS"), // table defining the data sources and their parameters
    TARGET_REPORT("SD_RADAR"), //table containing radar plots (Target Report)
    TRACK_UPDATE("SD_TRACK"), // table containing tracks (Track Update)
    MLAT_DATASOURCE("DS_MLAT"), // name of the MLAT sensor table
    TRACKER_DATASOURCE("DS_TRACKER"), // table containing tracker parameters
    SERVICE_MESSAGE("SD_SERVICE_MSGS") // table containing service messages
    ;

    private TableName(String tableName) {
      fTableName = tableName;
    }

    private final String fTableName;

    /**
     * Returns the real name of the table in the database.
     **/
    @Override
    public String toString() {
      return fTableName;
    }

    public static TableName getTableName(String aTableName) {
      for (TableName tableName : TableName.values()) {
        if (tableName.toString().equals(aTableName)) {
          return tableName;
        }
      }
      return null;
    }
  }
}
