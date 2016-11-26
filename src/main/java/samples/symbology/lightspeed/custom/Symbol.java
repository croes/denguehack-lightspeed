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
package samples.symbology.lightspeed.custom;

import java.util.HashMap;
import java.util.Map;

import com.luciad.shape.ILcdBounded;
import com.luciad.shape.ILcdBounds;
import com.luciad.shape.ILcdShape;

/**
 * A custom military symbol domain object.
 */
class Symbol implements ILcdBounded {

  private final String fSymbolCode;
  private final ILcdShape fGeometry;
  private final Map<String, String> fModifiers = new HashMap<String, String>();

  /**
   * Creates a new symbol.
   *
   * @param aSymbolCode the ms2525 code
   * @param aGeometry   the geometry
   */
  public Symbol(String aSymbolCode, ILcdShape aGeometry) {
    fSymbolCode = aSymbolCode;
    fGeometry = aGeometry;
  }

  /**
   * @return the geometry of the symbol
   */
  public ILcdShape getGeometry() {
    return fGeometry;
  }

  /**
   * @return the MS2525 code of the symbol
   */
  public String getSymbolCode() {
    return fSymbolCode;
  }

  @Override
  public ILcdBounds getBounds() {
    return fGeometry.getBounds();
  }

  /**
   * Returns the modifiers.
   *
   * @return the modifiers
   *
   * @see com.luciad.symbology.milstd2525b.model.ILcdMS2525bCoded#getTextModifierKey
   * @see com.luciad.symbology.milstd2525b.model.ILcdMS2525bCoded#getTextModifierValue
   */
  public Map<String, String> getModifiers() {
    return fModifiers;
  }
}
