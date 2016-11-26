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

import com.luciad.gui.ILcdIcon;
import com.luciad.gui.TLcdIconFactory;
import com.luciad.lucy.addons.drawing.format.ALcyDomainObjectSupplier;
import com.luciad.lucy.addons.drawing.lightspeed.ALcyLspDomainObjectSupplier;
import com.luciad.view.lightspeed.editor.ILspEditor;
import com.luciad.view.lightspeed.editor.TLspShapeEditor;
import com.luciad.view.lightspeed.geometry.discretization.ILspShapeDiscretizer;
import com.luciad.view.lightspeed.layer.TLspPaintRepresentation;
import com.luciad.view.lightspeed.layer.TLspPaintRepresentationState;
import com.luciad.view.lightspeed.style.TLspIconStyle;
import com.luciad.view.lightspeed.style.styler.ILspStyler;

/**
 * Lightspeed equivalent of the {@link CustomDomainObjectSupplier}
 */
public class LspCustomDomainObjectSupplier extends ALcyLspDomainObjectSupplier {

  public LspCustomDomainObjectSupplier(ALcyDomainObjectSupplier aDomainObjectSupplier) {
    super(aDomainObjectSupplier);
  }

  @Override
  public ILspShapeDiscretizer createShapeDiscretizer(TLspPaintRepresentation aPaintRepresentation) {
    //default discretizer can handle our point
    return null;
  }

  @Override
  public ILspEditor createShapeEditor(TLspPaintRepresentation aPaintRepresentation) {
    //the standard TLspShapeEditor can handle our point
    return aPaintRepresentation == TLspPaintRepresentation.BODY ? new TLspShapeEditor() : null;
  }

  @Override
  public ILspStyler createShapeStyler(TLspPaintRepresentationState aPaintRepresentationState) {
    if (aPaintRepresentationState == TLspPaintRepresentationState.REGULAR_BODY) {
      ILcdIcon icon = TLcdIconFactory.create(TLcdIconFactory.DRAW_SPHERE_ICON);
      return TLspIconStyle.newBuilder().icon(icon).build();
    }
    return null;
  }
}
