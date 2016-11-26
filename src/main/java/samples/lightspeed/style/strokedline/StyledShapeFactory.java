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
package samples.lightspeed.style.strokedline;

import java.util.List;

import com.luciad.geodesy.ILcdEllipsoid;
import com.luciad.shape.ILcdPoint;
import com.luciad.shape.ILcdShape;
import com.luciad.shape.shape2D.TLcdLonLatCircle;
import com.luciad.shape.shape2D.TLcdLonLatPoint;
import com.luciad.shape.shape2D.TLcdLonLatPolygon;
import com.luciad.shape.shape2D.TLcdLonLatPolyline;
import com.luciad.view.lightspeed.style.ALspStyle;

public class StyledShapeFactory {

  public static TLcdLonLatPoint createPoint(double aLon, double aLat, List<ALspStyle> aStyle) {
    return new StyledPoint(aLon, aLat, aStyle);
  }

  public static TLcdLonLatPolyline createPolyline(List<ALspStyle> aStyle) {
    return new StyledPolyline(aStyle);
  }

  public static TLcdLonLatPolygon createPolygon(List<ALspStyle> aStyle) {
    return new StyledPolygon(aStyle);
  }

  public static TLcdLonLatCircle createCircle(ILcdPoint aCenter, double aRadius, ILcdEllipsoid aEllipsoid, List<ALspStyle> aStyle) {
    return new StyledCircle(aCenter, aRadius, aEllipsoid, aStyle);
  }

  /**
   * Interface that augments an ILcdShape with a style. This is
   * used by the sample to assign different styles to individual objects.
   */
  public interface StyledShape extends ILcdShape {

    /**
     * Returns the style with which this shape should be painted.
     * @return a style
     */
    List<ALspStyle> getStyles();
  }

  /**
   * A point that implements IStyledShape.
   */
  private static class StyledPoint extends TLcdLonLatPoint implements StyledShape {

    private List<ALspStyle> fStyles;

    public StyledPoint(double aLon, double aLat, List<ALspStyle> aStyles) {
      super(aLon, aLat);
      fStyles = aStyles;
    }

    @Override
    public List<ALspStyle> getStyles() {
      return fStyles;
    }
  }

  /**
   * A polyline that implements IStyledShape.
   */
  private static class StyledPolyline extends TLcdLonLatPolyline implements StyledShape {

    private List<ALspStyle> fStyles;

    public StyledPolyline(List<ALspStyle> aStyles) {
      fStyles = aStyles;
    }

    @Override
    public List<ALspStyle> getStyles() {
      return fStyles;
    }
  }

  /**
   * A polyline that implements IStyledShape.
   */
  private static class StyledPolygon extends TLcdLonLatPolygon implements StyledShape {

    private List<ALspStyle> fStyles;

    public StyledPolygon(List<ALspStyle> aStyles) {
      fStyles = aStyles;
    }

    @Override
    public List<ALspStyle> getStyles() {
      return fStyles;
    }
  }

  private static class StyledCircle extends TLcdLonLatCircle implements StyledShape {

    private List<ALspStyle> fStyles;

    public StyledCircle(ILcdPoint aCenter, double aRadius, ILcdEllipsoid aEllipsoid, List<ALspStyle> aStyles) {
      super(aCenter, aRadius, aEllipsoid);
      fStyles = aStyles;
    }

    @Override
    public List<ALspStyle> getStyles() {
      return fStyles;
    }
  }

}
