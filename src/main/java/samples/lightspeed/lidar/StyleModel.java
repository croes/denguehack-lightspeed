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
package samples.lightspeed.lidar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

import com.luciad.format.las.TLcdLASModelDescriptor;
import com.luciad.geodesy.TLcdGeodeticDatum;
import com.luciad.model.ILcdModel;
import com.luciad.reference.TLcdGeodeticReference;
import com.luciad.shape.ILcdBounded;
import com.luciad.shape.shape3D.ILcd3DEditableBounds;
import com.luciad.shape.shape3D.TLcdLonLatHeightBounds;
import com.luciad.transformation.ILcdModelModelTransformation;
import com.luciad.transformation.TLcdDefaultModelModelTransformation;
import com.luciad.util.ILcdChangeListener;
import com.luciad.util.ILcdChangeSource;
import com.luciad.util.ILcdInterval;
import com.luciad.util.TLcdChangeEvent;
import com.luciad.util.TLcdChangeSupport;
import com.luciad.util.TLcdInterval;
import com.luciad.util.TLcdNoBoundsException;
import com.luciad.view.ILcdLayer;
import com.luciad.view.ILcdLayeredListener;
import com.luciad.view.TLcdLayeredEvent;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.ILspStyledLayer;
import com.luciad.view.lightspeed.layer.TLspPaintRepresentationState;
import com.luciad.view.lightspeed.style.styler.ILspStyler;

/**
 * Offers a combined model on all the stylers of all LAS layers in the view.
 * <p>
 * This can be used to give all LAS layers the same style.
 * <p>
 * You can attach {@link #addChangeListener listeners} to this model, and set/get {@link #setStyleProperty styling}
 * for all layers in the view in one go.
 *
 * @since 2014.0
 */
public class StyleModel implements ILcdChangeSource {

  private final TLcdChangeSupport fChangeSupport = new TLcdChangeSupport();

  private String fCurrentStyleProperty = null;

  private List<ILspStyledLayer> fLASLayers;

  private ViewListener fViewListener;

  private List<ILspView> fViews;

  public StyleModel(ILspView... aViews) {
    fLASLayers = new LinkedList<ILspStyledLayer>();
    fViewListener = new ViewListener();
    fViews = Arrays.asList(aViews);

    for (ILspView view : fViews) {
      Enumeration layers = view.layers();
      while (layers.hasMoreElements()) {
        ILcdLayer layer = (ILcdLayer) layers.nextElement();
        if (layer.getModel().getModelDescriptor() instanceof TLcdLASModelDescriptor) {
          fLASLayers.add((ILspStyledLayer) layer);
        }
      }
      view.addLayeredListener(fViewListener);
    }

    if (fLASLayers.size() != 0 && getLASLayersThatCanUseStyleProperty(TLcdLASModelDescriptor.COLOR).isEmpty()) {
      setStyleProperty(TLcdLASModelDescriptor.HEIGHT);
    } else {
      setStyleProperty(TLcdLASModelDescriptor.COLOR);
    }
  }

  @Override
  public void addChangeListener(ILcdChangeListener aListener) {
    fChangeSupport.addChangeListener(aListener);
  }

  @Override
  public void removeChangeListener(ILcdChangeListener aListener) {
    fChangeSupport.removeChangeListener(aListener);
  }

  public void removeStyleModelFromViews() {
    for (ILspView view : fViews) {
      view.removeLayeredListener(fViewListener);
    }
    fViews = new LinkedList<ILspView>();
    fLASLayers.clear();
  }

  /**
   * @return The currently set property used for styling.
   */
  public String getStyleProperty() {
    return fCurrentStyleProperty;
  }

  /**
   * Set the property that should be used for styling, if applicable.
   * <p>
   * See also {@link LASStyler#setStyleProperty}.
   *
   * @param aProperty A property name, see {@link TLcdLASModelDescriptor} and {@link LASStyler#SUPPORTED_PROPERTIES}.
   */
  public void setStyleProperty(String aProperty) {
    fCurrentStyleProperty = aProperty;
    ILcdInterval heightInterval = calculateHeightInterval();
    for (ILspStyledLayer layer : fLASLayers) {
      ILspStyler styler = layer.getStyler(TLspPaintRepresentationState.REGULAR_BODY);
      if (styler instanceof LASStyler) {
        LASStyler lasStyler = (LASStyler) layer.getStyler(TLspPaintRepresentationState.REGULAR_BODY);
        lasStyler.setStyleProperty(fCurrentStyleProperty, heightInterval);
      }
    }
    fChangeSupport.fireChangeEvent(new TLcdChangeEvent(this));
  }

  /**
   * @return All LAS layers in the views.
   */
  public Collection<ILspLayer> getLASLayers() {
    return new ArrayList<ILspLayer>(fLASLayers);
  }

  /**
   * @return List of LAS Layers that can use the property for styling
   */
  public Collection<ILspLayer> getLASLayersThatCanUseStyleProperty(String aProperty) {
    Collection<ILspLayer> result = new LinkedList<ILspLayer>();
    for (ILspStyledLayer layer : fLASLayers) {
      ILspStyler styler = layer.getStyler(TLspPaintRepresentationState.REGULAR_BODY);
      if (styler instanceof LASStyler) {
        LASStyler lasStyler = (LASStyler) styler;
        if (lasStyler.canUseStyleProperty(aProperty)) {
          result.add(layer);
        }
      }
    }
    return result;
  }

  /**
   * Calculates the union of the height ranges of all LAS layers in the views (in meters above ellipsoid).
   */
  private ILcdInterval calculateHeightInterval() {
    ILcdModelModelTransformation transformation
        = new TLcdDefaultModelModelTransformation
        (new TLcdGeodeticReference(new TLcdGeodeticDatum()), new TLcdGeodeticReference(new TLcdGeodeticDatum()));

    TLcdInterval interval = null;
    for (ILspStyledLayer layer : fLASLayers) {
      try {
        ILcdModel model = layer.getModel();
        if (model.getModelDescriptor() instanceof TLcdLASModelDescriptor && model instanceof ILcdBounded) {
          ILcdBounded bounded = (ILcdBounded) model;

          //transform the different bounds in the same reference
          ILcd3DEditableBounds dstBounds = new TLcdLonLatHeightBounds();
          transformation.setSourceReference(model.getModelReference());
          transformation.sourceBounds2destinationSFCT(bounded.getBounds(), dstBounds);

          double zMin = dstBounds.getLocation().getZ();
          double zMax = zMin + dstBounds.getDepth();
          if (interval == null) {
            interval = new TLcdInterval(zMin, zMax);
          } else {
            interval.setMin(Math.min(interval.getMin(), zMin));
            interval.setMax(Math.max(interval.getMax(), zMax));
          }
        }
      } catch (TLcdNoBoundsException e) {
        // ignore, not visible
      }
    }

    if (interval == null) {
      interval = new TLcdInterval(0, 2000);
    }

    return interval;
  }

  private class ViewListener implements ILcdLayeredListener {
    @Override
    public void layeredStateChanged(TLcdLayeredEvent e) {
      if (!(e.getLayer().getModel().getModelDescriptor() instanceof TLcdLASModelDescriptor)) {
        return;
      }
      if (e.getID() == TLcdLayeredEvent.LAYER_ADDED) {
        fLASLayers.add((ILspStyledLayer) e.getLayer());
      } else if (e.getID() == TLcdLayeredEvent.LAYER_REMOVED) {
        fLASLayers.remove(e.getLayer());
      }
      // There are no models that can support this property, switch to height
      if (fLASLayers.size() != 0 && 0 == getLASLayersThatCanUseStyleProperty(fCurrentStyleProperty).size()) {
        setStyleProperty(TLcdLASModelDescriptor.HEIGHT);
      } else {
        setStyleProperty(fCurrentStyleProperty);
      }
      fChangeSupport.fireChangeEvent(new TLcdChangeEvent(this));
    }
  }
}
