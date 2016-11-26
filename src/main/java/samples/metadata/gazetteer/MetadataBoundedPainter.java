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
package samples.metadata.gazetteer;

import com.luciad.shape.ILcdBounded;
import com.luciad.view.gxy.TLcdG2DLineStyle;
import com.luciad.view.gxy.painter.TLcdGXYBoundsPainter;

import java.awt.*;

/**
 * A painter for bounded objects.
 * This is an extension of the bounds painter where the bounds are extracted from the
 * object passed.
 */
class MetadataBoundedPainter extends TLcdGXYBoundsPainter {

  public MetadataBoundedPainter() {
    TLcdG2DLineStyle line_style = new TLcdG2DLineStyle();
    line_style.setAntiAliasing( true );
    line_style.setColor( Color.blue );
    line_style.setSelectionColor( Color.red );
    line_style.setLineWidth( 2 );
    line_style.setSelectionLineWidth( 3 );
    setLineStyle( line_style );
  }

  public void setObject( Object aObject ) {
    if ( aObject instanceof ILcdBounded ) {
      ILcdBounded bounded = (ILcdBounded) aObject;
      super.setObject( bounded.getBounds() );
    } else {
      super.setObject( null );
    }
  }

}
