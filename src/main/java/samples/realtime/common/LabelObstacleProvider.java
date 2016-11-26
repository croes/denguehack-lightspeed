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
package samples.realtime.common;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Enumeration;

import com.luciad.shape.shape2D.ILcd2DEditableBounds;
import com.luciad.shape.shape2D.TLcdXYBounds;
import com.luciad.util.ILcdFilter;
import com.luciad.util.ILcdFunction;
import com.luciad.util.ILcdInterval;
import com.luciad.util.TLcdNoBoundsException;
import com.luciad.view.gxy.ILcdGXYLabelObstacleProvider;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.ILcdGXYPainter;
import com.luciad.view.gxy.ILcdGXYView;
import com.luciad.view.gxy.TLcdGXYContext;
import com.luciad.view.gxy.TLcdGXYLabelObstacle;

/**
 * Returns all TimeStampedTrack's in the ILcdGXYView as obstacles for the label
 * placement: this means no labels are placed on top of the tracks.
 */
public class LabelObstacleProvider implements ILcdGXYLabelObstacleProvider {
  private ILcdFilter fModelFilter;


  /**
   * Constructs a new LabelObstacleProvider that retrieves its obstacles from the
   * objects of the models that pass the given filter.
   *
   * @param aModelFilter The filter that selects which models should be
   * considered for retrieving the obstacles from.
   */
  public LabelObstacleProvider(ILcdFilter aModelFilter) {
    fModelFilter = aModelFilter;
  }

  public java.util.List getLabelObstacles(Graphics aGraphics, ILcdGXYView aGXYView) {
    //The function whose applyOn method will be invoked for every track
    ObstacleProviderFunction function = new ObstacleProviderFunction();

    //The list of obstacles
    ArrayList obstacles = new ArrayList();

    //Loop over all layers of the given view
    Enumeration layers = aGXYView.layers();
    while (layers.hasMoreElements()) {
      ILcdGXYLayer layer = (ILcdGXYLayer) layers.nextElement();

      //Add the visible tracks (contained in track models) as obstacles for the labels
      if (layerVisible(layer, aGXYView) &&
          fModelFilter.accept(layer.getModel())) {

        function.addObstacles(aGraphics, aGXYView, layer, obstacles);
      }
    }
    return obstacles;
  }

  private boolean layerVisible(ILcdGXYLayer aGXYLayer, ILcdGXYView aGXYView) {
    double scale = aGXYView.getScale();
    ILcdInterval range = aGXYLayer.getScaleRange();

    return aGXYLayer.isVisible() &&
           (range == null || (range.getMin() <= scale && scale < range.getMax()));
  }

  /**
   * Retrieves, for a given layer and view, the obstacles as the bounds of all
   * objects that are painted on that layer. The bounds (area occupied by the
   * object) are retrieved from the painter.
   */
  private static class ObstacleProviderFunction implements ILcdFunction {
    private TLcdGXYContext fGXYContext = new TLcdGXYContext();
    private ILcd2DEditableBounds fBounds = new TLcdXYBounds();
    private java.util.List fObstacles;
    private Graphics fGraphics;

    public void addObstacles(Graphics aGraphics,
                             ILcdGXYView aGXYView,
                             ILcdGXYLayer aGXYLayer,
                             java.util.List aObstaclesSFCT) {

      fGraphics = aGraphics;
      fObstacles = aObstaclesSFCT;
      fGXYContext.resetFor(aGXYLayer, aGXYView); //init context object

      Rectangle view_bounds = new Rectangle(0, 0, aGXYView.getWidth(), aGXYView.getHeight());
      aGXYLayer.applyOnInteract(this, view_bounds, true, aGXYView);
    }

    public boolean applyOn(Object aObject) throws IllegalArgumentException {
      try {
        // Retrieve the painter for aObject
        ILcdGXYPainter painter = fGXYContext.getGXYLayer().getGXYPainter(aObject);

        // fBounds is adapted to contain the area occupied by the given aObject.
        painter.boundsSFCT(fGraphics, ILcdGXYPainter.BODY, fGXYContext, fBounds);

        //Convert fBounds to a TLcdObstacle and add it to the list
        fObstacles.add(new TLcdGXYLabelObstacle(
            (int) fBounds.getLocation().getX(),
            (int) fBounds.getLocation().getY(),
            (int) fBounds.getWidth(),
            (int) fBounds.getHeight(),
            0));
      } catch (TLcdNoBoundsException ignore) {
        //no harm, this object is simply not visible in the current projection
      }

      //Continue retrieving objects...
      return true;
    }
  }
}
