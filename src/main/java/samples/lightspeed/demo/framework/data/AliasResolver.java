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
package samples.lightspeed.demo.framework.data;

import java.util.HashMap;
import java.util.List;

import org.jdom.Element;
import org.jdom.Namespace;

import samples.lightspeed.demo.framework.exception.DataSetException;

/**
 * Helper class that resolves aliases used in the XML files.
 * <p>
 * Before the resolver does any work, it must be initialized by calling the {@link #load(Element, Namespace)}
 * method. When initialized, the resolver will be able to correctly resolve strings (i.e. replace known
 * alias references with the associated value) with the {@link #resolve(String)} method.  
 */
class AliasResolver {

  // Mapping of aliases to their values
  private static HashMap<String, String> fAliasMap = new HashMap<String, String>();

  private AliasResolver() {
    // Instances of this class are not allowed
  }

  /**
   * Loads any alias entries that are found under the given XML element.
   * <p>
   * Note that as a side-effect, all aliases that were previously loaded are removed.
   *
   * @param aRoot the root element that is to be searched for alias entries
   * @param aNamespace the namespace of the root element
   */
  static void load(Element aRoot, Namespace aNamespace) throws DataSetException {
    fAliasMap.clear();
    List<Element> aliasElements = aRoot.getChildren("Alias", aNamespace);
    for (Element element : aliasElements) {
      String name = resolve(element, "name");
      String value = resolve(element, "value");
      fAliasMap.put(name, value);
    }
  }

  /**
   * Resolves the given string, replacing the alias reference with its associated value.
   * <p>
   * In order for the given string to be correctly resolved, it must adhere to the following format:
   * <code>@alias:string</code>, where alias is the alias reference and string is the part to follow
   * the alias. For example, <code>@foo:bar</code> with foo associated to <code>banana</code> will
   * be resolved to : <code>bananabar</code>.
   * <p>
   * When the given string does not have the correct format, the method will just return the same string.
   * For example, <code>foobar</code> will be resolved to <code>foobar</code> and <code>@foobar</code>
   * will be resolved to <code>@foobar</code>. When the given string is <code>null</code>, the method
   * will return <code>null</code><br>
   * When the alias is not known, a {@link samples.lightspeed.demo.framework.exception.DataSetException}
   * will be thrown.
   *
   * @param aString the string to be resolved
   * @return the resolved string
   * @throws DataSetException when the alias referenced the given string is unknown 
   */
  static String resolve(String aString) throws DataSetException {
    if (aString == null) {
      return null;
    }
    if (!aString.startsWith("@")) {
      return aString; // Incorrect syntax
    }
    int pos = aString.indexOf(":");
    if (pos == -1) {
      return aString; // Incorrect syntax
    }

    // Syntax is correct, now extract alias
    String alias = aString.substring(1, pos);

    String aliasValue = fAliasMap.get(alias);
    if (aliasValue == null) {
      throw new DataSetException("Unable to resolve alias [" + alias + "] in string [" + aString + "]");
    }

    // Return resolved string
    return aString.replace("@" + alias + ":", aliasValue);
  }

  /**
   * Resolves the value of the element attribute with given name.
   *
   * @see #resolve(String)
   * @param aElement the xml element to which the attribute belongs
   * @param aAttrName the name of the attribute that is to be resolved
   * @return the resolved string
   * @throws DataSetException when the alias referenced in the given string is unknown
   */
  static String resolve(Element aElement, String aAttrName) throws DataSetException {
    return resolve(aElement.getAttributeValue(aAttrName));
  }
}
