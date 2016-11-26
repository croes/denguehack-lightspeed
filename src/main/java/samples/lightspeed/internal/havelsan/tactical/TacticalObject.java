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
package samples.lightspeed.internal.havelsan.tactical;

import com.luciad.shape.shape2D.TLcdLonLatPoint;
import com.luciad.symbology.milstd2525b.model.ELcdMS2525Standard;
import com.luciad.symbology.milstd2525b.model.ILcdMS2525bCoded;
import com.luciad.util.ILcdOriented;

/**
 * @author tomn
 * @since 2012.0
 */
public class TacticalObject extends TLcdLonLatPoint implements ILcdMS2525bCoded, ILcdOriented {

  private String fCode;
  private double fCX;
  private double fCY;
  private double fRadius;
  private double fTime;
  private double fOrientation;

  public TacticalObject(String aCode, double aCX, double aCY, double aRadius) {
    fCode = aCode;
    fCX = aCX;
    fCY = aCY;
    fRadius = aRadius;
  }

  @Override
  public ELcdMS2525Standard getMS2525Standard() {
    return ELcdMS2525Standard.MIL_STD_2525b;
  }

  public void setTime(double aTime) {
    fTime = aTime;
    while (fTime > 1.0) {
      fTime -= 1.0;
    }
    double angle = 2.0 * aTime * Math.PI;
    move2D(
        fCX + Math.cos(angle) * fRadius,
        fCY + Math.sin(angle) * fRadius
    );
    fOrientation = 360.0 - aTime * 360.0;
  }

  public double getTime() {
    return fTime;
  }

  public double getOrientation() {
    return fOrientation;
  }

  @Override
  public String getMS2525Code() {
    return fCode;
  }

  @Override
  public int getTextModifierCount() {
    return 0;
  }

  @Override
  public String getTextModifierKey(int aIndex) {
    return null;
  }

  @Override
  public String getTextModifierKeyDisplayName(String aTextModifierKey) {
    return null;
  }

  @Override
  public String getTextModifierValue(String aTextModifierKey) {
    return null;
  }

  @Override
  public String getTextModifierValue(int aIndex) {
    return null;
  }
}
