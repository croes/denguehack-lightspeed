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
package samples.lightspeed.customization.hippodrome;

import com.luciad.shape.shape3D.ILcdExtrudedShape;
import com.luciad.shape.shape3D.TLcdExtrudedShape;
import com.luciad.view.lightspeed.TLspContext;
import com.luciad.view.lightspeed.geometry.discretization.ALspEditable3DMesh;
import com.luciad.view.lightspeed.geometry.discretization.TLspDiscretizationException;
import com.luciad.view.lightspeed.geometry.discretization.TLspShapeDiscretizationParameters;
import com.luciad.view.lightspeed.geometry.discretization.TLspShapeDiscretizer;

import samples.gxy.hippodromePainter.IHippodrome;

/**
 * Extends {@code TLspShapeDiscretizer} to also handle {@code IHippodrome} objects.
 */
public class HippodromeShapeDiscretizer extends TLspShapeDiscretizer {

  @Override
  public void discretizeSFCT(
      Object aDomainObject,
      TLspShapeDiscretizationParameters aParameters,
      TLspContext aContext,
      ALspEditable3DMesh a3DMeshSFCT
  ) throws TLspDiscretizationException {

    // We discretize by converting the hippodrome in a shape that can be discretized
    // by the TLspShapeDiscretizer class.
    if (aDomainObject instanceof IHippodrome) {
      aDomainObject = ((IHippodrome) aDomainObject).getOutline();
    } else if ((aDomainObject instanceof ILcdExtrudedShape)
               && (((ILcdExtrudedShape) aDomainObject)
        .getBaseShape() instanceof IHippodrome)) {
      ILcdExtrudedShape extrudedShape = (ILcdExtrudedShape) aDomainObject;
      aDomainObject =
          new TLcdExtrudedShape(((IHippodrome) (extrudedShape).getBaseShape()).getOutline(),
                                extrudedShape.getMinimumZ(),
                                extrudedShape.getMaximumZ());
    }
    // Let TLspShapeDiscretizer do the work on the well-known shape.
    super.discretizeSFCT(aDomainObject, aParameters, aContext, a3DMeshSFCT);
  }
}
