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

import java.awt.Color;
import java.util.Properties;

import com.luciad.view.lightspeed.layer.ILspLayerFactory;

/**
 * Abstract {@code ILspLayerFactory} with an extra configuration step.
 */
public abstract class AbstractLayerFactory implements ILspLayerFactory {

  public void configure(Properties aProperties) {
    // Does nothing by default
  }

  /**
   * Parses a color in the format "AARRGGBB".
   */
  public static Color getColor(Properties aProperties, String aKey, String aDefault) {
    String color = aProperties.getProperty(aKey, aDefault);
    int rgb = Integer.parseInt(color.substring(2, 8), 16);
    int alpha = Integer.parseInt(color.substring(0, 2), 16);
    return new Color((alpha << 24) | rgb, true);
  }
}
