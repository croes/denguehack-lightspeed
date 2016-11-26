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
package samples.lightspeed.debug;

import java.util.Hashtable;
import java.util.Map;

import javax.swing.JTree;

import com.luciad.util.collections.ILcdMultiKeyMap;

/**
 * A utility class to construct a JTree based on cache.
 */
class DebugCacheJTreeFactory {

  public static JTree getAsJTree(ILcdMultiKeyMap<Object, Object> aCache) {
    return new JTree(getAsStringHashtable(aCache));
  }

  /**
   * Recursively converts all keys and values to strings.
   */
  private static Hashtable<?, ?> getAsStringHashtable(ILcdMultiKeyMap<Object, Object> aCache) {
    // First prune.
    Hashtable<StringWrapper, Hashtable<?, ?>> hashtable = new Hashtable<StringWrapper, Hashtable<?, ?>>();
    for (Map.Entry<Object, ? extends ILcdMultiKeyMap<Object, Object>> entry : aCache
        .branchEntrySet()) {
      hashtable.put(new StringWrapper(entry.getKey().toString() + " [" + (entry.getValue())
          .size() + "|" + (entry.getValue()).branchSize() + "]"), getAsStringHashtable((entry
          .getValue())));
    }
    for (Map.Entry<Object, Object> entry : aCache.entrySet()) {
      Hashtable<StringWrapper, String> table = new Hashtable<StringWrapper, String>();
      table.put(new StringWrapper(entry.getValue().toString()), "");
      hashtable.put(new StringWrapper(entry.getKey().toString()), table);
    }
    return hashtable;
  }

  /**
   * Wraps a String but ensures that two equal
   * Strings are not equal when wrapped.
   */
  private static class StringWrapper {
    private String fString;

    private StringWrapper(String aString) {
      fString = aString;
    }

    @Override
    public String toString() {
      return fString;
    }
  }
//
}
