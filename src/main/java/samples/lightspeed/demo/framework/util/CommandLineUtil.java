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
package samples.lightspeed.demo.framework.util;

import com.luciad.util.logging.ILcdLogger;
import com.luciad.util.logging.TLcdLoggerFactory;

/**
 * Utility class for interpreting command line arguments.
 */
public class CommandLineUtil {

  private static ILcdLogger sLogger = TLcdLoggerFactory.getLogger(CommandLineUtil.class);

  private CommandLineUtil() {
    super();
  }

  public static void parseCommandLineParametersSFCT(String[] args, CaseInsensitiveProperties aCommandLineProperties) {
    for (int index = 0; index < args.length; index++) {
      String option = args[index].trim();

      if (option.length() > 1 && option.charAt(0) == '-') {
        //we found a key
        String option_key = option.substring(1);
        if (index + 1 < args.length) {
          String option_value = args[index + 1].trim();
          if (option_value.length() > 1 && option_value.charAt(0) == '-') {
            //this is not a value, it's another key: store the option_key with value "true"
            aCommandLineProperties.setProperty(option_key, "true");
          } else {
            aCommandLineProperties.setProperty(option_key, option_value);
            index++;
          }
        } else {
          //no arguments left, so no value present: store the option_key with value "true"
          aCommandLineProperties.setProperty(option_key, "true");
        }
      } else {
        sLogger.warn("Ignoring badly formatted command line option[" + option + "]");
      }
    }
  }
}
