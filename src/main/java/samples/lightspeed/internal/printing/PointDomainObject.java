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
package samples.lightspeed.internal.printing;

import java.awt.Color;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.luciad.gui.TLcdSymbol;
import com.luciad.shape.shape3D.TLcdLonLatHeightPoint;
import com.luciad.util.ILcdOriented;
import com.luciad.view.lightspeed.TLspContext;
import com.luciad.view.lightspeed.style.ALspStyle;
import com.luciad.view.lightspeed.style.ILspWorldElevationStyle;
import com.luciad.view.lightspeed.style.TLspIconStyle;
import com.luciad.view.lightspeed.style.TLspViewDisplacementStyle;
import com.luciad.view.lightspeed.style.styler.ALspStyleCollector;
import com.luciad.view.lightspeed.style.styler.ALspStyler;

/**
 */
public class PointDomainObject extends TLcdLonLatHeightPoint implements ILcdOriented {

  private final List<? extends ALspStyle> fStyles;
  private double fOrientation;

  public PointDomainObject(double aLon, double aLat, TLspIconStyle.ScalingMode aIconMode, ILspWorldElevationStyle.ElevationMode aElevationMode, boolean aUseDisplacement) {
    this(aLon, aLat, 0.0, 0, createIconStyles(aIconMode, aElevationMode, aUseDisplacement));
  }

  PointDomainObject(double aLon, double aLat, double aHeight, double aOrientation, List<? extends ALspStyle> aStyles) {
    super(aLon, aLat, aHeight);
    fOrientation = aOrientation;
    fStyles = aStyles;
  }

  private static List<ALspStyle> createIconStyles(TLspIconStyle.ScalingMode aIconMode, ILspWorldElevationStyle.ElevationMode aElevationMode, boolean aUseDisplacement) {
    ALspStyle[] styles;
    if (aUseDisplacement) {
      styles = new ALspStyle[2];
    } else {
      styles = new ALspStyle[1];
    }
    styles[0] = TLspIconStyle.newBuilder().
        icon(new TLcdSymbol(TLcdSymbol.FILLED_CIRCLE, 32, Color.black, Color.yellow)).
                                 elevationMode(aElevationMode).
                                 scalingMode(aIconMode).
                                 offset(0, (!aUseDisplacement) ? -16 : 0).
                                 build();
    if (aUseDisplacement) {
      styles[1] = TLspViewDisplacementStyle.newBuilder().viewDisplacement(16, 16).build();
    }
    return Arrays.asList(styles);
  }

  public List<? extends ALspStyle> getStyles() {
    return fStyles;
  }

  @Override
  public double getOrientation() {
    return fOrientation;
  }

  public static class PointDomainObjectStyler extends ALspStyler {
    @Override
    public void style(Collection<?> aObjects, ALspStyleCollector aStyleCollector, TLspContext aContext) {
      for (Object object : aObjects) {
        if (object instanceof PointDomainObject) {
          PointDomainObject pointDomainObject = (PointDomainObject) object;
          aStyleCollector.object(object).styles(pointDomainObject.getStyles()).submit();
        }
      }
    }
  }
}
