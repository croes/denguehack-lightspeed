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
package samples.tea.gxy.los;

import com.luciad.shape.*;
import com.luciad.tea.ILcdPoint2PointIntervisibility;

/**
 * This class combines two intervisibility computations into one object that can be
 * painted with the painter <code>P2PIntervisibilityPainter</code>.
 */
class P2PIntervisibility implements ILcdPointList {

  private ILcdPoint2PointIntervisibility fP2PIntervisibilityRadar;
  private ILcdPoint2PointIntervisibility fP2PIntervisibilitySkyBackground;

  public P2PIntervisibility( ILcdPoint2PointIntervisibility aP2PIntervisibilityRadar,
                             ILcdPoint2PointIntervisibility aP2PIntervisibilitySkyBackground ) {
    fP2PIntervisibilityRadar         = aP2PIntervisibilityRadar;
    fP2PIntervisibilitySkyBackground = aP2PIntervisibilitySkyBackground;
  }

  public ILcdPoint2PointIntervisibility getP2PIntervisibilityRadar() {
    return fP2PIntervisibilityRadar;
  }

  public ILcdPoint2PointIntervisibility getP2PIntervisibilitySkyBackground() {
    return fP2PIntervisibilitySkyBackground;
  }

  public ILcdPoint getPoint( int aIndex ) throws IndexOutOfBoundsException {
    return ( (ILcdPointList) fP2PIntervisibilityRadar ).getPoint( aIndex );
  }

  public int getPointCount() {
    return ( (ILcdPointList) fP2PIntervisibilityRadar ).getPointCount();
  }
}
