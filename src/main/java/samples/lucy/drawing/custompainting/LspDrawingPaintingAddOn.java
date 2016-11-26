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
package samples.lucy.drawing.custompainting;

import com.luciad.lucy.addons.drawing.format.ALcyDomainObjectSupplier;
import com.luciad.lucy.addons.drawing.format.TLcyDrawingFormat;
import com.luciad.lucy.addons.drawing.lightspeed.ALcyLspDomainObjectSupplier;
import com.luciad.lucy.addons.drawing.lightspeed.TLcyLspDrawingAddOn;
import com.luciad.lucy.addons.drawing.lightspeed.TLcyLspDrawingFormat;
import com.luciad.lucy.util.properties.ALcyProperties;

/**
 * {@code TLcyLspDrawingAddOn} extension that wraps the circle supplier.
 */
public class LspDrawingPaintingAddOn extends TLcyLspDrawingAddOn {

  @Override
  protected TLcyLspDrawingFormat createBaseFormat() {
    return new CustomDomainObjectDrawingFormat(getDrawingAddOn().getDrawingFormat(), getPreferences(), getLongPrefix(), getShortPrefix(), this);
  }

  private static class CustomDomainObjectDrawingFormat extends TLcyLspDrawingFormat {
    private CustomDomainObjectDrawingFormat(TLcyDrawingFormat aDrawingFormat, ALcyProperties aProperties, String aLongPrefix, String aShortPrefix, TLcyLspDrawingAddOn aLspDrawingAddOn) {
      super(aDrawingFormat, aProperties, aLongPrefix, aShortPrefix, aLspDrawingAddOn);
    }

    @Override
    protected ALcyLspDomainObjectSupplier createLspDomainObjectSupplier(ALcyDomainObjectSupplier aDomainObjectSupplier) {
      ALcyLspDomainObjectSupplier supplier = super.createLspDomainObjectSupplier(aDomainObjectSupplier);
      if (TLcyDrawingFormat.CIRCLE_DOMAIN_OBJECT_ID.equals(aDomainObjectSupplier.getDomainObjectID())) {
        supplier = new LspCircleSupplierWrapper(getLucyEnv(), supplier);
      }
      return supplier;
    }

  }
}
