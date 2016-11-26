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
package samples.lucy.format.generated.lightspeed;

import com.luciad.lucy.ILcyLucyEnv;
import com.luciad.lucy.addons.lightspeed.ALcyLspFormatAddOn;
import com.luciad.lucy.format.lightspeed.ALcyLspFormat;
import com.luciad.lucy.format.lightspeed.TLcyLspSafeGuardFormatWrapper;
import com.luciad.lucy.util.ALcyTool;

/**
 * <p>Add-on that plugs in {@link GeneratedLspFormat}.</p>
 *
 * <p>If desired, it is possible to merge this add-on into {@link samples.lucy.format.generated.GeneratedFormatAddOn},
 * by replacing it with this in the {@link samples.lucy.format.generated.GeneratedFormatAddOn#plugInto(ILcyLucyEnv)} method:</p>
 * <pre>
 *   fLspFormatTool = new TLcyLspFormatTool(new TLcyLspSafeGuardFormatWrapper(new GeneratedLspFormat(...)));
 *   fLspFormatTool.plugInto(aLucyEnv);
 * </pre>
 *
 * <p>and similarly in the {@link samples.lucy.format.generated.GeneratedFormatAddOn#unplugFrom(ILcyLucyEnv)} method:</p>
 * <pre>
 *   fLspFormatTool.unplugFrom(aLucyEnv);
 * </pre>
 */
public class GeneratedLspFormatAddOn extends ALcyLspFormatAddOn {
  public GeneratedLspFormatAddOn() {
    super(ALcyTool.getLongPrefix(GeneratedLspFormatAddOn.class),
          ALcyTool.getShortPrefix(GeneratedLspFormatAddOn.class));
  }

  @Override
  protected ALcyLspFormat createBaseFormat() {
    return new GeneratedLspFormat(getLucyEnv(), getLongPrefix(), getShortPrefix(), getPreferences());
  }

  @Override
  protected ALcyLspFormat createFormatWrapper(ALcyLspFormat aBaseFormat) {
    return new TLcyLspSafeGuardFormatWrapper(aBaseFormat);
  }
}
