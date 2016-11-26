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
package samples.gxy.editing;

import java.util.List;

import com.luciad.model.ILcdModelReference;
import com.luciad.shape.ILcdCompositeCurve;
import com.luciad.shape.ILcdCurve;
import com.luciad.shape.ILcdRing;
import com.luciad.shape.constraints.ILcdCurveConnector;
import com.luciad.shape.constraints.ILcdCurveConnectorProvider;
import com.luciad.shape.constraints.TLcdCurveConnectorUtil;
import com.luciad.shape.constraints.TLcdDefaultCurveConnectorProvider;

/**
 * Utility class for composite curves.
 */
public class CompositeCurveUtil {

  private ILcdCurveConnectorProvider fCurveConnectorProvider = new TLcdDefaultCurveConnectorProvider();

  public ILcdCurveConnectorProvider getCurveConnectorProvider() {
    return fCurveConnectorProvider;
  }

  public void connectSubCurveStartPoint(ILcdCompositeCurve aCompositeCurve, int aIndex, ILcdModelReference aModelReference) {
    if (isRing(aCompositeCurve) || aIndex > 0) {
      List<ILcdCurve> curves = aCompositeCurve.getCurves();
      ILcdCurve curve = curves.get(aIndex);
      ILcdCurve previous = curves.get((aIndex - 1 + curves.size()) % curves.size());
      ILcdCurveConnector curveConnector = getCurveConnectorProvider().getCurveConnector(previous, curve, null);
      curveConnector.connect(previous, curve, null, aModelReference);
    }
  }

  public void connectCompositeCurve(ILcdCompositeCurve aCompositeCurve, int aStartIndex, ILcdModelReference aModelReference) {
    if (isRing(aCompositeCurve)) {
      TLcdCurveConnectorUtil.connectCompositeRing(aCompositeCurve, aStartIndex, getCurveConnectorProvider(), aModelReference);
    } else {
      connectCompositeCurveAsCurve(aCompositeCurve, aStartIndex, aModelReference);
    }
  }

  public void connectCompositeCurveAsCurve(ILcdCompositeCurve aCompositeCurve, int aStartIndex, ILcdModelReference aModelReference) {
    TLcdCurveConnectorUtil.connectCompositeCurve(aCompositeCurve, aStartIndex, getCurveConnectorProvider(), aModelReference);
  }

  public boolean isRing(ILcdCompositeCurve aCompositeCurve) {
    return aCompositeCurve instanceof ILcdRing;
  }
}
