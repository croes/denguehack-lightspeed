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

/**
 *
 */
public enum ESasJavaType {

  INT {
    @Override
    public Comparable getValueFromString(String aString) {
      if (aString.compareTo(NULL_STRING) == 0) {
        return null;
      } else {
        return Integer.valueOf(aString);
      }
    }

    @Override
    public Class<?> getClazz() {
      return Integer.class;
    }

    @Override
    public int getRequiredNbBytesInArray() {
      return 4;
    }
  },

  LONG {
    @Override
    public Comparable getValueFromString(String aString) {
      if (aString.compareTo(NULL_STRING) == 0) {
        return null;
      } else {
        return Long.valueOf(aString);
      }
    }

    @Override
    public Class<?> getClazz() {
      return Long.class;
    }

    @Override
    public int getRequiredNbBytesInArray() {
      return 8;
    }

  },

  FLOAT {
    @Override
    public Comparable getValueFromString(String aString) {
      if (aString.compareTo(NULL_STRING) == 0) {
        return null;
      } else {
        return Float.valueOf(aString);
      }
    }

    @Override
    public Class<?> getClazz() {
      return Float.class;
    }

    @Override
    public int getRequiredNbBytesInArray() {
      return 4;
    }

  },

  STRING {
    @Override
    public Comparable getValueFromString(String aString) {
      if (aString.compareTo(NULL_STRING) == 0) {
        return null;
      } else {
        return aString;
      }
    }

    @Override
    public Class<?> getClazz() {
      return String.class;
    }

    @Override
    public int getRequiredNbBytesInArray() {
      return 24;
    }

  },

  BOOLEAN {
    @Override
    public Comparable getValueFromString(String aString) {
      if (aString.compareTo(NULL_STRING) == 0) {
        return null;
      } else {
        boolean bool_value = Boolean.parseBoolean(aString);
        return bool_value ? Byte.valueOf((byte) 1) : Byte.valueOf((byte) 0);
      }
    }

    @Override
    public Class<?> getClazz() {
      return Byte.class;
    }

    @Override
    public int getRequiredNbBytesInArray() {
      return 1;
    }

  };
  private static final String NULL_STRING = "NULL";

  /**
   * Transform the given string into an instance of this.
   *
   * @param aString
   * @return
   */
  public abstract Comparable getValueFromString(String aString);

  public abstract Class<?> getClazz();

  public abstract int getRequiredNbBytesInArray();

}
