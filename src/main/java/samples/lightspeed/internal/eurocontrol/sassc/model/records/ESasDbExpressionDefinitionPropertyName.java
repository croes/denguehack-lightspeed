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

public enum ESasDbExpressionDefinitionPropertyName {
  SEPARATOR {
    @Override
    public String getPropertyName(int anId) {
      return null;
    }

    protected String getName() {
      return ".";
    }
  },
  VALUE_SEPARATOR {
    @Override
    public String getPropertyName(int anId) {
      return getName();
    }

    protected String getName() {
      return ",";
    }
  },
  PROPERTIES_PREFIX {
    @Override
    public String getPropertyName(int anId) {
      return getName();
    }

    protected String getName() {
      return "expression";
    }
  },
  DISPLAY_NAME_PROPERTY {
    @Override
    protected String getName() {
      return "displayName";
    }
  },
  NAME_PROPERTY {
    @Override
    protected String getName() {
      return "name";
    }
  },

  SQL_EXPRESSION_PROPERTY {
    @Override
    protected String getName() {
      return "expression";
    }
  },
  WHERE_CLAUSE_PROPERTY {
    @Override
    protected String getName() {
      return "where";
    }
  },
  GROUP_BY_CLAUSE_PROPERTY {
    @Override
    protected String getName() {
      return "groupBy";
    }
  },
  DEFAULT_NULL_VALUE {
    @Override
    protected String getName() {
      return "defaultValue";
    }
  },
  ALLOW_UNLOADING_PROPERTY {
    @Override
    protected String getName() {
      return "allowUnloading";
    }
  },
  IS_MULTIPLE_VALUE_PROPERTY {
    @Override
    protected String getName() {
      return "isMultiValue";
    }
  },
  IS_DISCRETE_PROPERTY {
    @Override
    protected String getName() {
      return "isDiscrete";
    }
  },
  IS_DELAYED_PROPERTY {
    @Override
    protected String getName() {
      return "isDelayed";
    }
  },
  TYPE_PROPERTY {
    @Override
    protected String getName() {
      return "type";
    }
  },
  FORMAT_PROPERTY {
    @Override
    protected String getName() {
      return "format";
    }
  },
  ALIAS_PROPERTY {
    @Override
    protected String getName() {
      return "alias";
    }
  },
  TABLE_NAME_PROPERTY {
    @Override
    protected String getName() {
      return "tableName";
    }
  };

  public String toString() {
    return getName();
  }

  public String getPropertyName(int anId) {
    return PROPERTIES_PREFIX.toString() + SEPARATOR + Integer.toString(anId)
           + SEPARATOR + getName();
  }

  protected abstract String getName();
}
