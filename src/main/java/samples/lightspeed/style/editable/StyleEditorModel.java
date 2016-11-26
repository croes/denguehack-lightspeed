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
package samples.lightspeed.style.editable;

import java.awt.Color;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.luciad.util.logging.ILcdLogger;
import com.luciad.util.logging.TLcdLoggerFactory;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.TLspContext;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.style.ALspStyle;
import com.luciad.view.lightspeed.style.TLspFillStyle;
import com.luciad.view.lightspeed.style.TLspLineStyle;
import com.luciad.view.lightspeed.style.styler.ALspStyleCollector;
import com.luciad.view.lightspeed.style.styler.TLspEditableStyler;

/**
 * This class separates the logic from the gui code for the style editor.
 */
public class StyleEditorModel {

  private static ILcdLogger sLogger = TLcdLoggerFactory.getLogger(StyleEditorModel.class);

  private ILspView fView;
  private TLspEditableStyler fStyler;
  //A list of layers. If null, will iterate over all layers in given view.
  private List<ILspLayer> fLayers;
  private TLspFillStyle.Builder<?> fFillBuilder;
  private TLspLineStyle.Builder<?> fLineBuilder;
  private PropertyChangeSupport fPropertyChangeSupport;

  // Attributes
  private Double fLineWidth;
  private Color fLineColor;
  private TLspLineStyle.DashPattern fLinePattern;
  private Color fFillColor;
  private TLspFillStyle.StipplePattern fFillPattern;

  // Flags
  private boolean fSameLineWidth;
  private boolean fSameLineColor;
  private boolean fSameLinePattern;
  private boolean fSameFillColor;
  private boolean fSameFillPattern;
  private boolean fDirty = false;

  public StyleEditorModel(ILspView aView, TLspEditableStyler aStyler) {
    this(aView, aStyler, null);
  }

  public StyleEditorModel(ILspView aView, TLspEditableStyler aStyler, List<ILspLayer> aLayers) {
    fView = aView;
    fStyler = aStyler;
    fLayers = aLayers;
    fPropertyChangeSupport = new PropertyChangeSupport(this);
    initAreaBuilder();
  }

  public void addPropertyChangeListener(PropertyChangeListener listener) {
    fPropertyChangeSupport.addPropertyChangeListener(listener);
  }

  public void removePropertyChangeListener(PropertyChangeListener listener) {
    fPropertyChangeSupport.removePropertyChangeListener(listener);
  }

  public boolean isDirty() {
    return fDirty;
  }

  private void setDirty(boolean aDirty) {
    boolean oldValue = fDirty;
    fDirty = aDirty;
    fPropertyChangeSupport.firePropertyChange("dirty", oldValue, fDirty);
  }

  public Double getLineWidth() {
    return fLineWidth;
  }

  public void setLineWidth(double aLineWidth) {
    fLineWidth = aLineWidth;
    fLineBuilder.width(aLineWidth);
    fSameLineWidth = true;
    setDirty(true);
  }

  public Color getLineColor() {
    return fLineColor;
  }

  public void setLineColor(Color aLineColor) {
    fLineColor = aLineColor;
    fLineBuilder.color(aLineColor);
    fSameLineColor = true;
    setDirty(true);
  }

  public TLspLineStyle.DashPattern getLinePattern() {
    return fLinePattern;
  }

  public void setLinePattern(TLspLineStyle.DashPattern aLinePattern) {
    fLinePattern = aLinePattern;
    fLineBuilder.dashPattern(aLinePattern);
    fSameLinePattern = true;
    setDirty(true);
  }

  public Color getFillColor() {
    return fFillColor;
  }

  public void setFillColor(Color aFillColor) {
    fFillColor = aFillColor;
    fFillBuilder.color(aFillColor);
    fSameFillColor = true;
    setDirty(true);
  }

  public TLspFillStyle.StipplePattern getFillPattern() {
    return fFillPattern;
  }

  public void setFillPattern(TLspFillStyle.StipplePattern aFillPattern) {
    fFillPattern = aFillPattern;
    fFillBuilder.stipplePattern(aFillPattern);
    fSameFillPattern = true;
    setDirty(true);
  }

  public void apply() {
    Iterator<ILspLayer> layersIterator = getLayersIterator();
    while (layersIterator.hasNext()) {
      ILspLayer layer = layersIterator.next();
      TLspContext context = new TLspContext(layer, fView);

      Enumeration<?> e = layer.selectedObjects();
      while (e.hasMoreElements()) {
        setStyle(e.nextElement(), context);
      }
    }
    setDirty(false);
  }

  /**
   * This method initializes the area builder.
   */
  private void initAreaBuilder() {
    fFillBuilder = TLspFillStyle.newBuilder();
    fLineBuilder = TLspLineStyle.newBuilder();
    fLineWidth = null;
    fLineColor = null;
    fLinePattern = null;
    fFillColor = null;
    fFillPattern = null;

    fSameLineWidth = true;
    fSameLineColor = true;
    fSameLinePattern = true;
    fSameFillColor = true;
    fSameFillPattern = true;

    boolean first = true;
    Iterator<ILspLayer> layersIterator = getLayersIterator();
    while (layersIterator.hasNext()) {
      ILspLayer layer = layersIterator.next();
      List<?> selectedObjects = Collections.list(layer.selectedObjects());
      TLspContext context = new TLspContext(layer, fView);
      for (Object o : selectedObjects) {
        evaluateObject(o, context, first);
        first = false;
      }
    }
  }

  public Iterator<ILspLayer> getLayersIterator() {
    if (fLayers != null) {
      return fLayers.iterator();
    } else {
      return new Iterator<ILspLayer>() {
        int fIndex = 0;

        @Override
        public boolean hasNext() {
          return fIndex != fView.layerCount();
        }

        @Override
        public ILspLayer next() {
          ILspLayer layer = fView.getLayer(fIndex);
          fIndex++;
          return layer;
        }

        @Override
        public void remove() {
          throw new UnsupportedOperationException("Remove not supported");
        }
      };
    }
  }

  private TLspFillStyle getFirstFillStyle(List<ALspStyle> aStyle) {
    for (ALspStyle style : aStyle) {
      if (style instanceof TLspFillStyle) {
        return (TLspFillStyle) style;
      }
    }
    return null;
  }

  private TLspLineStyle getFirstLineStyle(List<ALspStyle> aStyle) {
    for (ALspStyle style : aStyle) {
      if (style instanceof TLspLineStyle) {
        return (TLspLineStyle) style;
      }
    }
    return null;
  }

  /**
   * Helper method for initializing the area builder. The idea behind this method is to initialize
   * the area builder with the style values that are the same for all selected objects. For style
   * values that are not the same, a default value is set.
   */
  private void evaluateObject(Object aObject, TLspContext aContext, final boolean aInit) {
    Set<Object> objects = Collections.singleton(aObject);
    ALspStyleCollector collector = new ALspStyleCollector(objects) {
      @Override
      protected void submitImpl() {
        TLspFillStyle fillStyle = getFirstFillStyle(getStyles());
        TLspLineStyle lineStyle = getFirstLineStyle(getStyles());

        if (fillStyle != null && lineStyle != null) {
          if (aInit) {
            fLineWidth = lineStyle.getWidth();
            fLineColor = lineStyle.getColor();
            fLinePattern = lineStyle.getDashPattern();

            fFillColor = fillStyle.getColor();
            fFillPattern = fillStyle.getStipplePattern();
          } else {
            if (fSameLineWidth && lineStyle.getWidth() != fLineWidth) {
              fLineWidth = null;
              fSameLineWidth = false;
            }
            if (fSameLineColor && !lineStyle.getColor().equals(fLineColor)) {
              fLineColor = null;
              fSameLineColor = false;
            }

            TLspLineStyle.DashPattern lp = lineStyle.getDashPattern();
            if (fSameLinePattern && ((lp != null && !lp
                .equals(fLinePattern)) || (fLinePattern != null && !fLinePattern
                .equals(lp)))) {
              fLinePattern = null;
              fSameLinePattern = false;
            }
            if (fSameFillColor && !fillStyle.getColor().equals(fFillColor)) {
              fFillColor = null;
              fSameFillColor = false;
            }
            TLspFillStyle.StipplePattern fp = fillStyle.getStipplePattern();
            if (fSameFillPattern && ((fp != null && !fp
                .equals(fFillPattern)) || (fFillPattern != null && !fFillPattern
                .equals(fp)))) {
              fFillPattern = null;
              fSameFillPattern = false;
            }
          }

          // Customize area builder with common properties
          if (fLineWidth != null) {
            fLineBuilder.width(fLineWidth);
          }
          if (fLineColor != null) {
            fLineBuilder.color(fLineColor);
          }
          fLineBuilder.dashPattern(fLinePattern);
          if (fFillColor != null) {
            fFillBuilder.color(fFillColor);
          }
          fFillBuilder.stipplePattern(fFillPattern);
        }
      }
    };
    fStyler.style(objects, collector, aContext);
  }

  private void setStyle(final Object aObject, final TLspContext aContext) {
    Set<Object> objects = Collections.singleton(aObject);
    ALspStyleCollector collector = new ALspStyleCollector(objects) {
      @Override
      protected void submitImpl() {
        TLspFillStyle fs = getFirstFillStyle(getStyles());
        TLspLineStyle ls = getFirstLineStyle(getStyles());

        if (fs != null && ls != null) {

          if (!fSameLineWidth) {
            fLineBuilder.width(ls.getWidth());
          }
          if (!fSameLineColor) {
            fLineBuilder.color(ls.getColor());
          }
          if (!fSameLinePattern) {
            fLineBuilder.dashPattern(ls.getDashPattern());
          }
          if (!fSameFillColor) {
            fFillBuilder.color(fs.getColor());
          }
          if (!fSameFillPattern) {
            fFillBuilder.stipplePattern(fs.getStipplePattern());
          }

          ArrayList<ALspStyle> result = new ArrayList<ALspStyle>(2);
          result.add(fFillBuilder.build());
          result.add(fLineBuilder.build());
          fStyler.setStyle(aContext.getLayer().getModel(), aObject, result);
        } else {
          sLogger.warn("Not setting style, style is not an area style!");
        }
      }
    };
    fStyler.style(objects, collector, aContext);
  }

}
