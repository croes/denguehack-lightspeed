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
package samples.decoder.ecdis.common;

import java.util.Date;

import com.luciad.format.s52.TLcdS52DisplaySettings;

/**
 * Keeps a static final instance of  {@link TLcdS52DisplaySettings} and returns it when requested.
 * The static display settings instance can be shared with an application and {@link S57DimensionalFilterProvider} to filter time based S57 data.
 * By invoking {@link TLcdS52DisplaySettings#setDateFilterValue(Date)}, you can filter the S57 data in the bounded S52 layers to this display settings instance.
 */
public class S52DisplaySettingsSingleton {
  private final static TLcdS52DisplaySettings fSettings = new TLcdS52DisplaySettings();

  private S52DisplaySettingsSingleton() {
    throw new AssertionError("No instances allowed");
  }

  public static TLcdS52DisplaySettings getSettings() {
    return fSettings;
  }
}
