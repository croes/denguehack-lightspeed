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
package samples.lucy.printing;

import com.luciad.lucy.TLcyMain;

/**
 * This sample demonstrates how to customize the printing functionality in Lucy.
 * It has two custom print add-on extensions, one for ILcdGXYView based maps, and one
 * for Lightspeed maps.
 * <p/>
 * The add-ons modify the printed content as follows:
 * <ul>
 * <li>the page layout is rearranged using a custom printable component factory.
 *     The factory class is specified in the print configuration file (print_sample.cfg).
 *     which is specified in the addons_print_sample.xml file.</li>
 * <li>a title page and watermark are added in a custom print add-on class</li>
 * </ul>
 * The custom Lightspeed add-on also installs a print action with custom quality and page format settings.
 */
public class PrintSampleMain {
  public static void main(String[] aArgs) {
    TLcyMain.main(aArgs, "-addons", "samples/printing/addons_print_sample.xml");
  }
}
