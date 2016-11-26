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
package samples.ogc.wcs.server;

import com.luciad.ogc.wcs.ILcdWCSServiceMetadata;

/**
 * An example implementation of <code>ILcdWCSServiceMetadata</code>.
 */
class WCSServiceMetadata implements ILcdWCSServiceMetadata {
  public String getDescription() {
    return "LuciadLightspeed Web Coverage Service";
  }

  public String getName() {
    return "WCS";
  }

  public String getLabel() {
    return "OGC compliant Web Coverage Service implementation powered by Luciad";
  }

  public String getMetaDataLink() {
    return null;
  }

  public String getFees() {
    return "none";
  }

  public int getKeywordCount() {
    return 0;
  }

  public String getKeyword(int aIndex) throws IndexOutOfBoundsException {
    throw new IndexOutOfBoundsException("No keywords specified");
  }

  public int getAccessConstraintCount() {
    return 0;
  }

  public String getAccessConstraint(int aIndex) throws IndexOutOfBoundsException {
    throw new IndexOutOfBoundsException("No access constraints defined");
  }
}
