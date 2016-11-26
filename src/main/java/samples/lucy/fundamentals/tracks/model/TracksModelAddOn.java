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
package samples.lucy.fundamentals.tracks.model;

import com.luciad.lucy.addons.ALcyFormatAddOn;
import com.luciad.lucy.format.ALcyFormat;
import com.luciad.lucy.format.TLcySafeGuardFormatWrapper;
import com.luciad.lucy.util.ALcyTool;

/**
 * <p>
 *   Add-on which provides the functionality to decode the track models.
 * </p>
 *
 * <p>
 *   The {@link samples.lucy.fundamentals.tracks.TracksAddOn} provides visualization
 *   support for the track models.
 * </p>
 */
public class TracksModelAddOn extends ALcyFormatAddOn {
  public TracksModelAddOn() {
    super(ALcyTool.getLongPrefix(TracksModelAddOn.class),
          ALcyTool.getShortPrefix(TracksModelAddOn.class));
  }

  @Override
  protected ALcyFormat createBaseFormat() {
    return new TracksModelFormat(getLucyEnv(), getLongPrefix(), getShortPrefix(), getPreferences());
  }

  @Override
  protected ALcyFormat createFormatWrapper(ALcyFormat aBaseFormat) {
    return new TLcySafeGuardFormatWrapper(aBaseFormat);
  }
}
