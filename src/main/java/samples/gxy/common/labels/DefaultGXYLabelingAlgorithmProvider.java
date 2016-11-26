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
package samples.gxy.common.labels;

import java.util.Map;
import java.util.WeakHashMap;

import samples.common.shape.ShapeUtil;
import com.luciad.util.service.LcdService;
import com.luciad.view.ILcdLayer;
import com.luciad.view.TLcdLabelIdentifier;
import com.luciad.view.gxy.ILcdGXYLabelPainter;
import com.luciad.view.gxy.ILcdGXYLabelPainter2;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.labeling.algorithm.ILcdGXYLabelLabelingAlgorithmProvider;
import com.luciad.view.gxy.labeling.algorithm.ILcdGXYLabelingAlgorithm;
import com.luciad.view.gxy.labeling.algorithm.discrete.TLcdGXYInPathLabelingAlgorithm;
import com.luciad.view.gxy.labeling.algorithm.discrete.TLcdGXYLabelPainterLocationLabelingAlgorithm;
import com.luciad.view.gxy.labeling.algorithm.discrete.TLcdGXYOnPathLabelingAlgorithm;

/**
 * This labeling algorithm provider checks if a shape is a line or a surface-like shape, and returns
 * an appropriate labeling algorithm.
 */
@LcdService(service = ILcdGXYLabelLabelingAlgorithmProvider.class, priority = LcdService.FALLBACK_PRIORITY)
public class DefaultGXYLabelingAlgorithmProvider implements ILcdGXYLabelLabelingAlgorithmProvider<ILcdGXYLabelingAlgorithm> {

  private ILcdGXYLabelingAlgorithm fDefaultAlgorithm;
  private Map<ILcdLayer, ILcdGXYLabelingAlgorithm> fAlgorithmMap = new WeakHashMap<>();

  private ILcdGXYLabelingAlgorithm fOnPathAlgorithm;
  private ILcdGXYLabelingAlgorithm fInPathAlgorithm;

  public DefaultGXYLabelingAlgorithmProvider() {
    this(new TLcdGXYLabelPainterLocationLabelingAlgorithm());
  }

  public DefaultGXYLabelingAlgorithmProvider(ILcdGXYLabelingAlgorithm aDefaultLabelingAlgorithm) {
    fDefaultAlgorithm = aDefaultLabelingAlgorithm;
    TLcdGXYOnPathLabelingAlgorithm onPathAlgorithm = new TLcdGXYOnPathLabelingAlgorithm();
    onPathAlgorithm.setAlignmentMode(TLcdGXYOnPathLabelingAlgorithm.AlignmentMode.ABOVE);
    fOnPathAlgorithm = onPathAlgorithm;
    fInPathAlgorithm = new TLcdGXYInPathLabelingAlgorithm();
  }

  public void setOnPathAlgorithm(ILcdGXYLabelingAlgorithm aOnPathAlgorithm) {
    fOnPathAlgorithm = aOnPathAlgorithm;
  }

  public void setInPathAlgorithm(ILcdGXYLabelingAlgorithm aInPathAlgorithm) {
    fInPathAlgorithm = aInPathAlgorithm;
  }

  @Override
  public ILcdGXYLabelingAlgorithm getLabelingAlgorithm(TLcdLabelIdentifier aLabel) {
    ILcdLayer layer = aLabel.getLayer();
    ILcdGXYLabelingAlgorithm algorithm = fAlgorithmMap.get(layer);
    if (algorithm != null) {
      return algorithm;
    }

    algorithm = getLabelingAlgorithm(aLabel.getDomainObject(), (ILcdGXYLayer) aLabel.getLayer());
    if (algorithm != null) {
      return algorithm;
    }

    fAlgorithmMap.put(layer, fDefaultAlgorithm);
    return fDefaultAlgorithm;
  }

  private ILcdGXYLabelingAlgorithm getLabelingAlgorithm(Object aAnchor, ILcdGXYLayer aLayer) {
    if (supportsPathLabelingAlgorithms(aAnchor, aLayer) && !ShapeUtil.isPointShape(aAnchor)) {
      if (ShapeUtil.isPolygonShape(aAnchor)) {
        return fInPathAlgorithm;
      } else if (ShapeUtil.isPolylineShape(aAnchor)) {
        return fOnPathAlgorithm;
      }
    }
    return fDefaultAlgorithm;
  }

  private boolean supportsPathLabelingAlgorithms(Object aDomainObject, ILcdGXYLayer aLayer) {
    // TLcdGXYOnPathLabelingAlgorithm and TLcdGXYInPathLabelingAlgorithm are only supported for
    // labels using an ILcdGXYLabelPainter2.
    ILcdGXYLabelPainter labelPainter = aLayer.getGXYLabelPainter(aDomainObject);
    return labelPainter instanceof ILcdGXYLabelPainter2;
  }
}
