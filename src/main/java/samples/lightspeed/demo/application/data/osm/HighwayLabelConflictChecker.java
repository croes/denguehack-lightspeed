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
package samples.lightspeed.demo.application.data.osm;

import static samples.lightspeed.demo.application.data.osm.RoadUtil.*;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.luciad.datamodel.ILcdDataObject;
import com.luciad.view.lightspeed.label.ILspLabelConflictChecker;
import com.luciad.view.lightspeed.label.TLspLabelConflictChecker;
import com.luciad.view.lightspeed.label.TLspLabelObstacle;
import com.luciad.view.lightspeed.label.TLspLabelPlacement;
import com.luciad.view.lightspeed.layer.ILspLayer;

/**
 * A conflict checker which makes sure each highway has only a limited number of icons, properly
 * spaced (instead of an icon on each separate road segment).
 */
class HighwayLabelConflictChecker implements ILspLabelConflictChecker {

  private TLspLabelConflictChecker fDefaultLabelConflictChecker = new TLspLabelConflictChecker();
  private Map<HighwayId, ILspLabelConflictChecker> fHighwayConflictCheckerMap = new HashMap<HighwayId, ILspLabelConflictChecker>();
  private List<ILspLayer> fRoadLayers;
  private Map<TLspLabelPlacement, TLspLabelPlacement> fPlacementMap = new HashMap<TLspLabelPlacement, TLspLabelPlacement>();
  private Rectangle fViewBounds;

  HighwayLabelConflictChecker(List<ILspLayer> aRoadLayers) {
    fRoadLayers = aRoadLayers;
  }

  // Implementations for ILspLabelConflictChecker

  @Override
  public void addObstacle(TLspLabelObstacle aObstacle) {
    fDefaultLabelConflictChecker.addObstacle(aObstacle);
  }

  @Override
  public void reset(Rectangle aViewBounds) {
    fDefaultLabelConflictChecker.reset(aViewBounds);
    for (ILspLabelConflictChecker checker : fHighwayConflictCheckerMap.values()) {
      checker.reset(aViewBounds);
    }
    fViewBounds = aViewBounds;
    fPlacementMap.clear();
  }

  @Override
  public void removeObstacle(TLspLabelObstacle aObstacle) {
    fDefaultLabelConflictChecker.removeObstacle(aObstacle);
  }

  @Override
  public List<TLspLabelObstacle> getObstacles() {
    return fDefaultLabelConflictChecker.getObstacles();
  }

  @Override
  public void addPlacement(TLspLabelPlacement aPlacement) {
    fDefaultLabelConflictChecker.addPlacement(aPlacement);
    ILspLabelConflictChecker roadChecker = getChecker(aPlacement);
    if (roadChecker != null) {
      TLspLabelPlacement roadPlacement = createRoadPlacement(aPlacement);
      roadChecker.addPlacement(roadPlacement);
      fPlacementMap.put(aPlacement, roadPlacement);
    }
  }

  @Override
  public void removePlacement(TLspLabelPlacement aPlacement) {
    fDefaultLabelConflictChecker.removePlacement(aPlacement);
    ILspLabelConflictChecker roadChecker = getChecker(aPlacement);
    if (roadChecker != null) {
      TLspLabelPlacement roadPlacement = fPlacementMap.get(aPlacement);
      if (roadPlacement != null) {
        roadChecker.removePlacement(roadPlacement);
      }
    }
  }

  @Override
  public List<TLspLabelPlacement> getPlacements() {
    return fDefaultLabelConflictChecker.getPlacements();
  }

  @Override
  public Conflict getConflict(TLspLabelPlacement aPlacement) {
    Conflict defaultConflict = fDefaultLabelConflictChecker.getConflict(aPlacement);
    ILspLabelConflictChecker roadChecker = getChecker(aPlacement);
    if (roadChecker != null) {
      TLspLabelPlacement roadPlacement = fPlacementMap.get(aPlacement);
      if (roadPlacement == null) {
        roadPlacement = createRoadPlacement(aPlacement);
      }
      Conflict roadConflict = roadChecker.getConflict(roadPlacement);
      return combineConflicts(defaultConflict, roadConflict);
    }
    return defaultConflict;
  }

  private ILspLabelConflictChecker getChecker(TLspLabelPlacement aPlacement) {
    if (fRoadLayers.contains(aPlacement.getLabelID().getLayer())) {
      ILcdDataObject road = (ILcdDataObject) aPlacement.getLabelID().getDomainObject();
      if (isHighway(road)) {
        HighwayId highwayId = getHighwayId(road);
        ILspLabelConflictChecker roadChecker = fHighwayConflictCheckerMap.get(highwayId);
        if (roadChecker == null) {
          roadChecker = new TLspLabelConflictChecker();
          if (fViewBounds != null) {
            roadChecker.reset(fViewBounds);
          }
          fHighwayConflictCheckerMap.put(highwayId, roadChecker);
        }
        return roadChecker;
      }
    }
    return null;
  }

  @Override
  public Object clone() {
    try {
      HighwayLabelConflictChecker clone = (HighwayLabelConflictChecker) super.clone();
      clone.fViewBounds = (Rectangle) this.fViewBounds.clone();
      clone.fDefaultLabelConflictChecker = this.fDefaultLabelConflictChecker.clone();
      clone.fHighwayConflictCheckerMap = new HashMap<HighwayId, ILspLabelConflictChecker>();
      for (Map.Entry<HighwayId, ILspLabelConflictChecker> entry : fHighwayConflictCheckerMap.entrySet()) {
        clone.fHighwayConflictCheckerMap.put(entry.getKey(), (ILspLabelConflictChecker) entry.getValue().clone());
      }
      clone.fPlacementMap = new HashMap<TLspLabelPlacement, TLspLabelPlacement>(this.fPlacementMap);
      clone.fRoadLayers = new ArrayList<ILspLayer>(fRoadLayers);
      return clone;
    } catch (CloneNotSupportedException e) {
      throw new RuntimeException(e);
    }
  }

  private static Conflict combineConflicts(Conflict aConflict1, Conflict aConflict2) {
    if (aConflict1 == null) {
      return aConflict2;
    } else if (aConflict2 == null) {
      return aConflict1;
    } else {
      return new Conflict(aConflict1.isOutsideView() || aConflict2.isOutsideView(),
                          aConflict1.isPartiallyOutsideView() || aConflict2.isPartiallyOutsideView(),
                          aConflict1.isOverlapWithPlacements() || aConflict2.isOverlapWithPlacements(),
                          aConflict1.isOverlapWithObstacles() || aConflict2.isOverlapWithObstacles());
    }
  }

  private static TLspLabelPlacement createRoadPlacement(TLspLabelPlacement aPlacement) {
    return new TLspLabelPlacement(aPlacement.getLabelID(),
                                  aPlacement.getLocation(),
                                  aPlacement.isVisible(),
                                  aPlacement.getX() - 100,
                                  aPlacement.getY() - 100,
                                  aPlacement.getWidth() + 200,
                                  aPlacement.getHeight() + 200,
                                  aPlacement.getRotation());
  }

}
