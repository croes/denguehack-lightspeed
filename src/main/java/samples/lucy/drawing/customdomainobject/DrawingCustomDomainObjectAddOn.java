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
package samples.lucy.drawing.customdomainobject;

import com.luciad.lucy.addons.drawing.TLcyDrawingAddOn;
import com.luciad.lucy.addons.drawing.format.ALcyDomainObjectSupplier;
import com.luciad.lucy.addons.drawing.format.TLcyDrawingFormat;
import com.luciad.lucy.util.TLcyProperties;

/**
 * This is an extension of {@link TLcyDrawingAddOn}. It adds an additional
 * {@link ALcyDomainObjectSupplier} that can handle {@link CustomDomainObject}
 * instances.
 *
 * The addons_drawing_customdomainobject_sample.xml file ensures that
 * it is automatically loaded into lucy.
 *
 */
public class DrawingCustomDomainObjectAddOn extends TLcyDrawingAddOn {

  static final String CUSTOM_DOMAIN_OBJECT_ID = "customDomainObject";

  @Override
  protected TLcyDrawingFormat createBaseFormat() {
    TLcyDrawingFormat drawingFormat = super.createBaseFormat();
    ALcyDomainObjectSupplier supplier = new CustomDomainObjectSupplier(CUSTOM_DOMAIN_OBJECT_ID, new TLcyProperties(), getLucyEnv());
    drawingFormat.addDomainObjectSupplier(supplier);
    return drawingFormat;
  }
}
