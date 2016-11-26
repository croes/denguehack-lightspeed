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
package samples.lightspeed.internal.uniticon;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.luciad.gui.ILcdIcon;
import com.luciad.symbology.milstd2525b.model.ELcdMS2525Standard;
import com.luciad.symbology.milstd2525b.model.ILcdMS2525bCoded;
import com.luciad.symbology.milstd2525b.view.TLcdMS2525bObjectIconProvider;
import com.luciad.symbology.milstd2525b.view.gxy.ILcdMS2525bStyle;

/**
 * @author tomn
 * @since 2012.0
 */
public class UnitIcon {

  public static void main(String[] args) {
    if (args.length < 1) {
      System.out.println("USAGE: UnitIcon <MS2525b code> [size]");
      System.out.println("This tool produces a PNG file with the corresponding MS2525b icon at");
      System.out.println("the specified resolution (default is 32x32).");
      System.out.println("EXAMPLE: UnitIcon SUGPUCRH------- 128");
      System.out.println("This command produces a 128x128 pixel pony.");
      return;
    }

    final String code = args[0];
    System.out.println("code = " + code);

    int size = 32;
    if (args.length > 1) {
      size = Integer.parseInt(args[1]);
    }
    System.out.println("size = " + size);

    TLcdMS2525bObjectIconProvider provider = new TLcdMS2525bObjectIconProvider();
    final ILcdMS2525bStyle style = provider.getDefaultStyle();
    style.setSizeSymbol(size);
    provider.setDefaultStyle(style);

    ILcdMS2525bCoded object = new ILcdMS2525bCoded() {
      @Override
      public String getMS2525Code() {
        return code;
      }

      @Override
      public ELcdMS2525Standard getMS2525Standard() {
        return ELcdMS2525Standard.MIL_STD_2525b;
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
    };

    ILcdIcon icon = provider.getIcon(object);

    BufferedImage image = new BufferedImage(
        icon.getIconWidth(),
        icon.getIconHeight(),
        BufferedImage.TYPE_4BYTE_ABGR
    );
    Graphics2D g = image.createGraphics();
    icon.paintIcon(null, g, 0, 0);
    g.dispose();

    try {
      String out = code.replaceAll("\\*", "") + ".png";
      System.out.println("out = " + out);
      ImageIO.write(image, "PNG", new File(out));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
