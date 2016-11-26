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
package samples.lucy.drawing;

import com.luciad.lucy.addons.drawing.format.ALcyShapeSupplier;
import com.luciad.lucy.addons.drawing.util.context.TLcyShapeContext;
import com.luciad.lucy.gui.customizer.ILcyCustomizerPanelFactory;
import com.luciad.shape.ILcdShape;
import com.luciad.util.ILcdFilter;

/**
 * Small utility class to facilitate the creation of customizer panel factories for
 * <code>ALcyShapeSupplier</code> implementations.
 */
public abstract class ShapeCustomizerPanelFactory implements ILcyCustomizerPanelFactory {
  private final ILcdFilter fShapeContextFilter;

  public ShapeCustomizerPanelFactory(final ALcyShapeSupplier aShapeSupplier) {
    fShapeContextFilter = createShapeContextFilter(new ILcdFilter() {
      @Override
      public boolean accept(Object aShape) {
        return aShapeSupplier.canHandle((ILcdShape) aShape);
      }
    });
  }

  @Override
  public boolean canCreateCustomizerPanel(Object aObject) {
    return fShapeContextFilter.accept(aObject);
  }

  public ILcdFilter getShapeContextFilter() {
    return fShapeContextFilter;
  }

  private static ILcdFilter createShapeContextFilter(final ILcdFilter aShapeFilter) {
    return new ILcdFilter() {
      @Override
      public boolean accept(Object aObject) {
        if (aObject instanceof TLcyShapeContext) {
          TLcyShapeContext context = (TLcyShapeContext) aObject;
          return aShapeFilter.accept(context.getShape());
        }
        return false;
      }
    };
  }
}
