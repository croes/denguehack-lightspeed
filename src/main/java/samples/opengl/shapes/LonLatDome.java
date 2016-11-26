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

package samples.opengl.shapes;

import com.luciad.geodesy.ILcdEllipsoid;
import com.luciad.shape.ILcdPoint;
import com.luciad.shape.shape2D.TLcdLonLatCircle;

/**
 * An extension of TLcdLonLatCircle. This simply allows us to distinguish between
 * objects that should be drawn as a circle and objects that should be drawn as a
 * dome.
 */
class LonLatDome extends TLcdLonLatCircle {

  public LonLatDome() {
  }

  public LonLatDome( TLcdLonLatCircle aCircle ) {
    super( aCircle );
  }

  public LonLatDome( ILcdPoint aPoint, double v, ILcdEllipsoid aEllipsoid ) {
    super( aPoint, v, aEllipsoid );
  }

  public LonLatDome( double v, double v1, double v2, ILcdEllipsoid aEllipsoid ) {
    super( v, v1, v2, aEllipsoid );
  }
}
