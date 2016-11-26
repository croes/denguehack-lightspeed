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
package samples.lightspeed.demo.application.data.density;

import java.awt.Color;
import java.awt.image.IndexColorModel;
import java.util.Properties;
import java.util.Scanner;

import com.luciad.format.raster.TLcdJetIndexColorModelFactory;
import com.luciad.view.lightspeed.style.TLspIndexColorModelStyle;

/**
 * Util class for fetching an indexed color model from the provided properties.
 * This indexed color model is encoded by couples of indices (0-255) and colors,
 * denoted as an integer in a hexadecimal representation (Must be 8 characters).
 * Each of the 4 (RGBA) color components is stored with 8 bits (AARRGGBB).
 */
public class DensityIndexColorModelStyleUtil {

  /**
   * Creates a new <code>TLspIndexColorModelStyle</code> object based on the
   * provided properties, more specifically on the "density.index.color.model"
   * property. If this property is not available, the default color model style
   * is returned.
   * @param aProperties the provided properties
   * @return the new <code>TLspIndexColorModelStyle</code> object
   */
  public TLspIndexColorModelStyle retrieveIndexColorModelStyle(Properties aProperties) {
    return getIndexColorModelStyle(aProperties.getProperty(
        "density.index.color.model",
        "0 000000FF 1 FF0000FF 10 FF00FFFF 20 FFFFFF00 30 FFFFC800 255 FFFF0000"));
  }

  /**
   * Parses the provided String hexadecimal representation and creates a new
   * <code>java.awt.Color</code> object with the parsed values.
   * @param aInt the provided String hexadecimal representation
   * @return a new <code>java.awt.Color</code> object with the parsed values.
   */
  private Color getColor(String aInt) {
    if (aInt.length() != 8) {
      return new Color(0);
    }
    //parse rgb and alpha separately to avoid a bug in Integer.parseInt()
    int rgb = Integer.parseInt(aInt.substring(2, 8), 16);
    int alpha = Integer.parseInt(aInt.substring(0, 2), 16);
    Color c = new Color((alpha << 24) | rgb, true);
    return new Color((alpha << 24) | rgb, true);
  }

  /**
   * Returns a new <code>TLspIndexColorModelStyle</code> object based on
   * the specifications in the provided string.
   * @param aString the provided string
   * @return a new <code>TLspIndexColorModelStyle</code> object
   */
  private TLspIndexColorModelStyle getIndexColorModelStyle(String aString) {
    TLcdJetIndexColorModelFactory factory = new TLcdJetIndexColorModelFactory();
    factory.setBits(8);
    factory.setSize(256);
    Scanner scanner = new Scanner(aString);
    while (scanner.hasNext()) {
      int idx = scanner.nextInt();
      factory.setBasicColor(idx, getColor(scanner.next()));
    }
    IndexColorModel colorModel = (IndexColorModel) factory.createColorModel();
    return TLspIndexColorModelStyle.newBuilder()
                                   .indexColorModel(colorModel)
                                   .transparent(false)
                                   .build();
  }

}
