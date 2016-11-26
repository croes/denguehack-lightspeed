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
package samples.decoder.ecdis.gxy;

import com.luciad.format.s52.gxy.TLcdS52GXYLayerFactory;
import com.luciad.util.service.LcdService;
import com.luciad.view.gxy.ILcdGXYLayerFactory;

import samples.decoder.ecdis.common.S52DisplaySettingsSingleton;

/**
 * <p>An {@code ILcdGXYLayerFactory} implementation which is capable of creating gxy layers for S-57
 * models. The layer factory has a default constructor which allows to register it in the service
 * mechanism.</p>
 */
@LcdService(service = ILcdGXYLayerFactory.class, priority = LcdService.LOW_PRIORITY)
public class S52GXYLayerFactory extends TLcdS52GXYLayerFactory {
  public S52GXYLayerFactory() {
    super(S52DisplaySettingsSingleton.getSettings());
  }
}
