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
package samples.decoder.ecdis.common;

import java.awt.Rectangle;
import java.lang.ref.WeakReference;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.luciad.format.s57.ILcdS57Attribute;
import com.luciad.format.s57.ILcdS57Object;
import com.luciad.gui.TLcdAWTUtil;
import samples.common.gxy.GXYViewUtil;
import com.luciad.model.ILcdModel;
import com.luciad.shape.ILcdBounds;
import com.luciad.shape.shape2D.TLcdXYBounds;
import com.luciad.util.ILcdFunction;
import com.luciad.util.concurrent.TLcdLockUtil;
import com.luciad.util.logging.ILcdLogger;
import com.luciad.util.logging.TLcdLoggerFactory;
import com.luciad.view.ILcdLayer;
import com.luciad.view.ILcdLayered;
import com.luciad.view.ILcdLayeredListener;
import com.luciad.view.ILcdView;
import com.luciad.view.ILcdViewInvalidationListener;
import com.luciad.view.TLcdLayeredEvent;
import com.luciad.view.TLcdViewInvalidationEvent;
import com.luciad.view.gxy.ILcdGXYEditableLabelsLayer;
import com.luciad.view.gxy.ILcdGXYView;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.TLspContext;
import com.luciad.view.lightspeed.layer.ILspInteractivePaintableLayer;
import com.luciad.view.lightspeed.layer.TLspPaintRepresentationState;
import com.luciad.view.lightspeed.query.TLspPaintedObjectsQuery;

/**
 * Utility class for querying and extracting visible objects and date information from an S57 Model.
 * <p>
 *   Date information in S57 is defined with the codes 85 (START_DATE) and 86 (END_DATE).
 *   By invoking {@link S57DateFilterUtil#extractVisibleObjectsDates(ILcdView, ILcdModel, ILcdLayer) extractVisibleObjectsDates} method, you can get the all START_DATE and END_DATE values of the visible domain objects in the current view bounds.
 *   This enables you to create a context sensitive time filter since the possible filter values can be updated from the visible geographical region.
 * </p>
 */
public class S57DateFilterUtil {

  private static final ILcdLogger LOGGER = TLcdLoggerFactory.getLogger(S57DateFilterUtil.class);
  private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMdd");
  private static final int START_DATE_CODE = 85;
  private static final int END_DATE_CODE = 86;

  /**
   * Extracts the visible domain objects in the given model.
   * @param aView The view which the layer is displayed
   * @param aModel The model which has the domain objects
   * @param aLayer The layer of the model
   * @return List of the visible domain objects
   */
  static List<Object> extractVisibleObjects(ILcdView aView, ILcdModel aModel, ILcdLayer aLayer) {
    try {
      TLcdLockUtil.readLock(aModel);
      //all S57 layers in a ILcdGXYView must implement ILcdGXYEditableLabelsLayer
      if (aView instanceof ILcdGXYView && aLayer instanceof ILcdGXYEditableLabelsLayer) {
        ObjectCollectorFunction applyFunction = new ObjectCollectorFunction();
        int width = ((ILcdGXYView) aView).getWidth();
        int height = ((ILcdGXYView) aView).getHeight();
        ((ILcdGXYEditableLabelsLayer) aLayer).applyOnInteract(applyFunction, new Rectangle(0, 0, width, height), true, (ILcdGXYView) aView);
        return applyFunction.fObjects;
      }//all S57 layers in a ILspView must implement ILspInteractivePaintableLayer
      else if (aView instanceof ILspView && aLayer instanceof ILspInteractivePaintableLayer) {
        ILspView view = (ILspView) aView;
        ILcdBounds viewBounds = new TLcdXYBounds(0, 0, view.getWidth(), view.getHeight());
        ILspInteractivePaintableLayer layer = (ILspInteractivePaintableLayer) aLayer;
        return new ArrayList<>(layer.query(
            new TLspPaintedObjectsQuery(TLspPaintRepresentationState.REGULAR_BODY, viewBounds),
            new TLspContext(layer, (ILspView) aView)));
      } else {
        throw new RuntimeException("Unrecognized view and layer types: " + aView + " / " + aLayer);
      }
    } finally {
      TLcdLockUtil.readUnlock(aModel);
    }
  }

  /**
   * ILcdFunction implementation which will accumulate the visible s57 objects
   * in a GXY view
   */
  private static class ObjectCollectorFunction implements ILcdFunction {
    private List<Object> fObjects = new ArrayList<>();

    @Override
    public boolean applyOn(Object aObject) throws IllegalArgumentException {
      fObjects.add(aObject);
      return true;
    }
  }

  /**
   * Extracts the date information of the visible domain objects in the given model.
   * @param aView The view which the layer is displayed
   * @param aModel The model which has the domain objects
   * @param aLayer The layer of the model
   * @return List of available start and end dates of the visible domain objects
   */
  static List<Date> extractVisibleObjectsDates(ILcdView aView, ILcdModel aModel, ILcdLayer
      aLayer) {
    List<Object> objects = extractVisibleObjects(aView, aModel, aLayer);
    return extractDatesFromObjects(objects);
  }

  /**
   * Registers <code>aFunction</code> which is going to run for each visible ILcdS57Object which has a START_DATE and/or an END_DATE value when aLayered is invalidated.
   * This method is used to extract all date information from the visible part of a s57 layer.
   * @param aLayered The ILcdLayered which has aLayer.
   * @param aLayer The layer which has the s57 model.
   * @param aFunction The function which is going to run after aLayered is invalidated.
   */
  public static void registerApplyOnCurrentObjects(final ILcdLayered aLayered, final ILcdLayer aLayer, final ILcdFunction aFunction) {
    final S57ViewInvalidationListener aInvalidationListener = new S57ViewInvalidationListener(aLayer, aFunction);
    aLayered.addLayeredListener(new ILcdLayeredListener() {
      @Override
      public void layeredStateChanged(TLcdLayeredEvent e) {
        if (e.getLayer() == aLayer && (e.getID() == TLcdLayeredEvent.LAYER_REMOVED)) {
          aLayered.removeLayeredListener(this);
          if (aLayered instanceof ILspView) {
            ((ILspView) aLayered).removeViewInvalidationListener(aInvalidationListener);
          } else if (aLayered instanceof ILcdGXYView) {
            GXYViewUtil.removeViewInvalidationListener((ILcdGXYView) aLayered, aInvalidationListener);
          }
        }
      }
    });
    if (aLayered instanceof ILspView) {
      ((ILspView) aLayered).addViewInvalidationListener(aInvalidationListener);
    } else if (aLayered instanceof ILcdGXYView) {
      GXYViewUtil.addViewInvalidationListener((ILcdGXYView) aLayered, aInvalidationListener);
    }
  }

  /**
   * Extracts a date list from the given objects
   * @param aObjects A list of s57 objects
   * @return List of start and end dates of the objects
   */
  private static List<Date> extractDatesFromObjects(List<Object> aObjects) {
    List<Date> dateList = new ArrayList<>();
    if (aObjects != null) {
      for (Object aObject : aObjects) {
        if (aObject instanceof ILcdS57Object) {
          ILcdS57Object s57Object = (ILcdS57Object) aObject;
          ILcdS57Attribute startDateAtt = s57Object.getIdentifier().getS57AttributeForCode(START_DATE_CODE);
          ILcdS57Attribute endDateAtt = s57Object.getIdentifier().getS57AttributeForCode(END_DATE_CODE);
          Date startDate = null;
          Date endDate = null;
          if (startDateAtt != null) {
            try {
              startDate = DATE_FORMAT.parse(startDateAtt.getStringValue());
            } catch (ParseException e) {
              LOGGER.warn("Could not parse date value " + startDateAtt.getStringValue());
            }
          }
          if (endDateAtt != null) {
            try {
              endDate = DATE_FORMAT.parse(endDateAtt.getStringValue());
            } catch (ParseException e) {
              LOGGER.warn("Could not parse date value " + endDateAtt.getStringValue());
            }
          }
          if (startDate != null || endDate != null) {
            startDate = startDate == null ? endDate : startDate;
            endDate = endDate == null ? startDate : endDate;
            if (startDate.getTime() > endDate.getTime()) {
              Date temp = startDate;
              startDate = endDate;
              endDate = temp;
            }
            dateList.add(startDate);
            dateList.add(endDate);
          }
        }
      }
    }
    return dateList;
  }

  /**
   * This invalidation listener will query the layer for visible S57 objects each time the view is panned/zoomed etc.
   */
  private static class S57ViewInvalidationListener implements ILcdViewInvalidationListener {

    private static final long MIN_TIME = 500L;
    private final WeakReference<ILcdLayer> fLayer;
    private final ILcdFunction fFunction;
    private long fLastTime = 0;

    public S57ViewInvalidationListener(ILcdLayer aLayer, ILcdFunction aFunction) {
      fLayer = new WeakReference<>(aLayer);
      fFunction = aFunction;
    }

    @Override
    public void viewInvalidated(final TLcdViewInvalidationEvent aEvent) {
      final ILcdLayer layer = fLayer.get();
      if (layer != null && layer.isVisible() &&
          ("autoUpdate".equalsIgnoreCase(aEvent.getMessage())
           || aEvent.getReason() == TLcdViewInvalidationEvent.PAN
           || aEvent.getReason() == TLcdViewInvalidationEvent.SCALE)) {

        //prevent excessive queries
        long now = System.currentTimeMillis();
        if (now - fLastTime <= MIN_TIME) {
          return;
        }
        fLastTime = now;

        TLcdAWTUtil.invokeAndWait(new Runnable() {
          @Override
          public void run() {
            fFunction.applyOn(S57DateFilterUtil.extractVisibleObjectsDates((ILcdView) aEvent.getSource(), layer.getModel(), layer));
          }
        });
      }
    }
  }

}
