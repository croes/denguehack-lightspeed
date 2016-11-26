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
package samples.symbology.nvg.common;

import java.awt.Color;
import java.awt.Point;
import java.util.HashMap;
import java.util.Map;

import com.luciad.format.nvg.nvg20.model.TLcdNVG20SymbolizedContent;
import com.luciad.gui.ILcdIcon;
import com.luciad.gui.ILcdObjectIconProvider;
import com.luciad.gui.TLcdAnchoredIcon;
import com.luciad.gui.TLcdSVGIcon;
import com.luciad.gui.TLcdSymbol;

/**
 * ILcdObjectIconProvider implementation to be used for domain specific symbols.
 */
public class CivilianIconProvider implements ILcdObjectIconProvider {

  // name of the domain specific symbol set.
  public static final String CUSTOM_CIVILIAN_DOMAIN_NAME = "civilian";

  private Map<String, ILcdIcon> fIconMap = new HashMap<>();

  @Override
  public ILcdIcon getIcon(Object aObject) throws IllegalArgumentException {
    String code = ((TLcdNVG20SymbolizedContent) aObject).getSymbol().getTextRepresentation();
    ILcdIcon icon;
    if (fIconMap.containsKey(code)) {
      icon = fIconMap.get(code);
    } else {
      switch (code) {
      case "church":
        icon = new TLcdSVGIcon("Data/NVG/NVG20/church.svg", 32, 32);
        break;
      case "hospital":
        icon = new TLcdSVGIcon("Data/NVG/NVG20/hospital.svg", 32, 32);
        break;
      default:
        icon = new TLcdSymbol(TLcdSymbol.FILLED_CIRCLE, 32, Color.black, Color.CYAN);
        break;
      }
      fIconMap.put(code, icon);
    }
    return new TLcdAnchoredIcon(icon, new Point(16, 36));
  }

  @Override
  public boolean canGetIcon(Object aObject) {
    return true;
  }
}
