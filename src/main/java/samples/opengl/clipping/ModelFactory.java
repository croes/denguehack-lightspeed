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
package samples.opengl.clipping;

import com.luciad.geodesy.TLcdGeodeticDatum;
import com.luciad.model.*;
import com.luciad.reference.TLcdGeodeticReference;
import com.luciad.shape.shape2D.TLcdLonLatEllipse;
import com.luciad.util.ILcdFireEventMode;

/**
 * A utility class that can create some hard coded models. These models are used
 * as sample data in the sample application.
 */
class ModelFactory {

  public static final String ELLIPSE_MODEL_DISPLAY_NAME = "Ellipse";
  public static final String ELLIPSE_MODEL_TYPE_NAME = ELLIPSE_MODEL_DISPLAY_NAME;

  public static ILcdModel createEllipseModel() {
    TLcdModelDescriptor descriptor = new TLcdModelDescriptor(
            "",
            ELLIPSE_MODEL_DISPLAY_NAME,
            ELLIPSE_MODEL_TYPE_NAME
    );
    TLcdGeodeticDatum datum = new TLcdGeodeticDatum();
    TLcdGeodeticReference modelReference = new TLcdGeodeticReference( datum );
    TLcdVectorModel model = new TLcdVectorModel( modelReference, descriptor );

    TLcdLonLatEllipse ellipse1 =
            new TLcdLonLatEllipse( 0, 0, 1000000, 500000, 10, datum.getEllipsoid() );
    model.addElement( ellipse1, ILcdFireEventMode.NO_EVENT );

    return model;
  }
}
