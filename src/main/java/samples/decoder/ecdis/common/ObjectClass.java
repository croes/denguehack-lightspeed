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
package samples.decoder.ecdis.common;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.luciad.format.s57.ELcdS57ProductType;

public final class ObjectClass implements Comparable<ObjectClass> {

  private final Set<ELcdS57ProductType> fProductType = new HashSet<>();
  private final int fCode;
  private final String fString;

  ObjectClass(ELcdS57ProductType aProductType, int aCode, String aString) {
    fProductType.add(aProductType);
    fCode = aCode;
    fString = aString;
  }

  public Collection<ELcdS57ProductType> getProductTypes() {
    return fProductType;
  }

  protected void addProductTypes(Collection<ELcdS57ProductType> aProductType) {
    fProductType.addAll(aProductType);
  }

  public int getCode() {
    return fCode;
  }

  public String getString() {
    return fString;
  }

  @Override
  public String toString() {
    return fString;
  }

  @Override
  public int compareTo(ObjectClass aTo) {
    if (aTo == null) {
      return -1;
    }

    String source = fString == null ? "" : fString;
    String target = aTo.fString == null ? "" : aTo.fString;

    return source.compareTo(target);
  }
}
