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
package samples.lightspeed.demo.application.data.milsym;

import static com.luciad.util.concurrent.TLcdLockUtil.Lock;
import static com.luciad.util.concurrent.TLcdLockUtil.readLock;

import java.awt.Color;
import java.util.Collection;
import java.util.Enumeration;

import com.luciad.model.ILcdModel;
import com.luciad.reference.ILcdGeoReference;
import com.luciad.shape.ILcdPoint;
import com.luciad.shape.shape2D.ALcd2DEditablePolypoint;
import com.luciad.symbology.app6a.model.ILcdAPP6ACoded;
import com.luciad.symbology.app6a.view.gxy.ILcdAPP6AStyle;
import com.luciad.symbology.app6a.view.gxy.ILcdAPP6AStyled;
import com.luciad.symbology.app6a.view.lightspeed.TLspAPP6ASymbolStyle;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.TLspContext;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.style.ALspStyle;
import com.luciad.view.lightspeed.style.styler.ALspStyleCollector;
import com.luciad.view.lightspeed.style.styler.ALspStyler;
import com.luciad.view.lightspeed.style.styler.ILspStyleChangeListener;
import com.luciad.view.lightspeed.style.styler.ILspStyler;

import samples.lightspeed.demo.application.data.support.los.LOSCoverage;
import samples.lightspeed.demo.framework.application.Framework;

/**
 * Styler for military symbols that styles the symbols based on their visibility in the
 * line-of-sight coverage.
 */
public abstract class MilSymLOSStyler implements ILspStyler {

  private final static Color VISIBLE = Color.WHITE;
  private final ALspStyler fDelegate = new InternalStyler();

  protected abstract ILcdAPP6AStyle createDefaultStyle();

  @Override
  public void style(Collection<?> aObjects, ALspStyleCollector aStyleCollector, TLspContext aContext) {
    fDelegate.style(aObjects, aStyleCollector, aContext);
  }

  @Override
  public void addStyleChangeListener(ILspStyleChangeListener aListener) {
    fDelegate.addStyleChangeListener(aListener);
  }

  @Override
  public void removeStyleChangeListener(ILspStyleChangeListener aListener) {
    fDelegate.removeStyleChangeListener(aListener);
  }

  public void fireStyleChangeEvent() {
    fDelegate.fireStyleChangeEvent();
  }

  private class InternalStyler extends ALspStyler {
    @Override
    public void style(Collection<?> aObjects, ALspStyleCollector aStyleCollector, TLspContext aContext) {
      ILspView view = aContext.getView();
      Framework framework = Framework.getInstance();
      Collection<ILspLayer> losLayers = framework.getLayersWithID("layer.id.milsym.los", view);

      for (Object o : aObjects) {
        ALspStyle style = getStyle(o, (ILcdGeoReference) aContext.getModelReference(), losLayers);
        if (style != null) {
          aStyleCollector.object(o).style(style).submit();
        }
      }
    }

    private ALspStyle getStyle(Object aObject, ILcdGeoReference aGeoReference, Collection<ILspLayer> aLOSLayers) {
      if (aObject instanceof ILcdAPP6ACoded) {
        TLspAPP6ASymbolStyle.Builder<?> builder = TLspAPP6ASymbolStyle.newBuilder();
        ILcdAPP6AStyle style = createDefaultStyle();
        if (aObject instanceof ILcdAPP6AStyled) {
          style = ((ILcdAPP6AStyled) aObject).getAPP6AStyle();
        }
        if (style != null) {
          if (isVisible(aObject, aGeoReference, aLOSLayers)) {
            style.setHaloColor(VISIBLE);
            style.setHaloEnabled(true);
            style.setHaloThickness(1);
          } else {
            style.setHaloEnabled(false);
          }
          builder.app6aStyle(style);
        }
        return builder
            .app6aCoded((ILcdAPP6ACoded) aObject)
            .build();
      }
      return null;
    }

    public boolean isVisible(Object aObject, ILcdGeoReference aGeoReference, Collection<ILspLayer> aLOSLayers) {
      if (aLOSLayers == null || aLOSLayers.isEmpty()) {
        return false;
      }
      if (aObject instanceof ALcd2DEditablePolypoint) {
        ALcd2DEditablePolypoint p = (ALcd2DEditablePolypoint) aObject;
        // Only determine visibility for single point symbols.
        if (p.getPointCount() != 1) {
          return false;
        }
        for (int i = 0; i < p.getPointCount(); ++i) {
          for (ILspLayer layer : aLOSLayers) {
            ILcdModel model = layer.getModel();
            try (Lock autoUnlock = readLock(model)) {
              Enumeration e = model.elements();
              while (e.hasMoreElements()) {
                Object o = e.nextElement();
                if (o instanceof LOSCoverage) {
                  LOSCoverage coverage = (LOSCoverage) o;
                  if (isPointVisible(p.getPoint(i), aGeoReference, coverage)) {
                    return true;
                  }
                }
              }
            }
          }
        }
      }
      return false;
    }

    public boolean isPointVisible(ILcdPoint aPoint, ILcdGeoReference aGeoReference, LOSCoverage aCoverage) {
      return aCoverage != null && aCoverage.isPointVisible(aPoint, aGeoReference);
    }
  }
}
