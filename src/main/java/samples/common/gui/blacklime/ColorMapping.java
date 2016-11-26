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
package samples.common.gui.blacklime;

import java.util.HashMap;
import java.util.Map;

/**
 * Changes the mapping of certain derived colors, that cannot be configured in the normal way, using properties.
 */
class ColorMapping {
  static final Map<Descriptor, Descriptor> MAPPING = new HashMap<>();

  static {
    Descriptor text = new Descriptor("text", 0, 0, 0, 0);
    Descriptor disabledText = new Descriptor("nimbusDisabledText", 0, 0, 0, 0);

    // Hard coded values as used in CheckBoxMenuItemPainter and RadioButtonMenuItemPainter
    MAPPING.put(new Descriptor("nimbusBlueGrey", 0.0f, -0.08983666f, -0.17647058f, 0), disabledText);
    MAPPING.put(new Descriptor("nimbusBlueGrey", 0.055555582f, -0.096827686f, -0.45882353f, 0), text);
    MAPPING.put(new Descriptor("nimbusBlueGrey", 0.0f, -0.110526316f, 0.25490195f, 0), text);

    // Hard coded values as used in MenuPainter
    MAPPING.put(new Descriptor("nimbusBlueGrey", 0.0f, -0.08983666f, -0.17647058f, 0), disabledText);
    MAPPING.put(new Descriptor("nimbusBlueGrey", 0.055555582f, -0.09663743f, -0.4627451f, 0), text);
  }

  // Simple container object with generated hashCode/equals for the map to work
  static class Descriptor {
    String fParentColorName;
    float fHOffset;
    float fSOffset;
    float fBOffset;
    int fOffset;

    public Descriptor(String aParentColorName, float aHOffset, float aSOffset, float aBOffset, int aOffset) {
      fParentColorName = aParentColorName;
      fHOffset = aHOffset;
      fSOffset = aSOffset;
      fBOffset = aBOffset;
      fOffset = aOffset;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof Descriptor)) {
        return false;
      }

      Descriptor that = (Descriptor) o;

      if (Float.compare(that.fBOffset, fBOffset) != 0) {
        return false;
      }
      if (Float.compare(that.fHOffset, fHOffset) != 0) {
        return false;
      }
      if (fOffset != that.fOffset) {
        return false;
      }
      if (Float.compare(that.fSOffset, fSOffset) != 0) {
        return false;
      }
      if (!fParentColorName.equals(that.fParentColorName)) {
        return false;
      }

      return true;
    }

    @Override
    public int hashCode() {
      int result = fParentColorName.hashCode();
      result = 31 * result + (fHOffset != +0.0f ? Float.floatToIntBits(fHOffset) : 0);
      result = 31 * result + (fSOffset != +0.0f ? Float.floatToIntBits(fSOffset) : 0);
      result = 31 * result + (fBOffset != +0.0f ? Float.floatToIntBits(fBOffset) : 0);
      result = 31 * result + fOffset;
      return result;
    }
  }
}
