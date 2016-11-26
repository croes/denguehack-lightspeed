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
package samples.lightspeed.demo.application.data.maritime.countrycodeutil;

import java.awt.Color;
import java.awt.Point;
import java.util.Properties;

import com.luciad.gui.ILcdIcon;
import com.luciad.gui.TLcdAnchoredIcon;
import com.luciad.gui.TLcdImageIcon;
import com.luciad.gui.TLcdSymbol;

public class CountryCodeUtil {

  private static final String sPrefix = CountryCodeUtil.class.getPackage().getName().replace(".", "/");

  private static final Properties sIso2Name = new Properties();
  private static final Properties sMid2Iso = new Properties();

  static {
    try {
      sIso2Name.load(CountryCodeUtil.class.getResourceAsStream("/" + sPrefix + "/iso2-to-countryname.properties"));
      sMid2Iso.load(CountryCodeUtil.class.getResourceAsStream("/" + sPrefix + "/mid-to-iso2.properties"));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static String getCountryName(String aIso2Alpha) {
    return sIso2Name.getProperty(aIso2Alpha.toUpperCase().trim());
  }

  public static String getCountryFlagIconLocation(String aIso2Alpha) {
    return sPrefix + "/icons/" + aIso2Alpha.trim().toLowerCase() + ".png";
  }

  public static ILcdIcon getCountryFlagIcon(String aIso2Alpha) {
    if (aIso2Alpha == null) {
      return new TLcdImageIcon();
    }
    return new TLcdImageIcon(getCountryFlagIconLocation(aIso2Alpha));
  }

  public static ILcdIcon[] getCountryFlagIconsByMid(int aOffsetX, int aOffsetY) {
    ILcdIcon[] flagIcons = new ILcdIcon[999];
    for (int i = 0; i < flagIcons.length; i++) {
      String iso2 = getIso2AlphaCode("" + i);
      flagIcons[i] = iso2 != null ? getCountryFlagIcon(iso2) : new TLcdSymbol(TLcdSymbol.CIRCLE, 5, Color.red);
      flagIcons[i] = new TLcdAnchoredIcon(flagIcons[i], new Point(flagIcons[i].getIconWidth() - aOffsetX, flagIcons[i].getIconHeight() - aOffsetY));
    }
    return flagIcons;
  }

  public static String getIso2AlphaCode(String aMidCode) {
    String code = sMid2Iso.getProperty(aMidCode.trim());
    if (code == null) {
      code = "unknown";
    }
    return code;
  }
}
