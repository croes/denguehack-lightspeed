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
package samples.lucy.drawing.hippodrome;

import com.luciad.lucy.addons.drawing.lightspeed.ALcyLspShapeSupplier;
import com.luciad.view.lightspeed.editor.ILspEditor;
import com.luciad.view.lightspeed.geometry.discretization.ILspShapeDiscretizer;
import com.luciad.view.lightspeed.layer.TLspPaintRepresentation;

import samples.lightspeed.customization.hippodrome.HippodromeEditor;
import samples.lightspeed.customization.hippodrome.HippodromeShapeDiscretizer;

/**
 * <p>{@link ALcyLspShapeSupplier ALcyLspShapeSupplier} for hippodrome shapes.</p>
 *
 * <p>It reuses the functionality from the Lightspeed samples, where an editor and shape discretizer were
 * made for hippodrome shapes.</p>
 *
 * @since 2012.1
 */
public class LspHippodromeShapeSupplier extends ALcyLspShapeSupplier {
  @Override
  public ILspShapeDiscretizer createShapeDiscretizer(TLspPaintRepresentation aPaintRepresentation) {
    return aPaintRepresentation == TLspPaintRepresentation.BODY ? new HippodromeShapeDiscretizer() : null;
  }

  @Override
  public ILspEditor createShapeEditor(TLspPaintRepresentation aPaintRepresentation) {
    return aPaintRepresentation == TLspPaintRepresentation.BODY ? new HippodromeEditor() : null;
  }
}
