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
package samples.lightspeed.demo.application.data.lighting;

import java.util.ArrayList;
import java.util.List;

import com.luciad.format.kml22.model.TLcdKML22ModelDescriptor;
import com.luciad.util.ILcdFilter;
import com.luciad.view.lightspeed.ILspView;
import com.luciad.view.lightspeed.layer.ILspLayer;

/**
 * Extension of the regular Lighting theme which adds a layer painting progress indicator on the view.
 */
public class LightingThemeInternal extends LightingTheme {

  private static final String PROGRESS_INDICATOR_TEXT = "Loading buildings...";

  private final ILcdFilter<ILspLayer> fKMLLayerFilter = new ILcdFilter<ILspLayer>() {
    @Override
    public boolean accept(ILspLayer aObject) {
      return aObject != null &&
             aObject.getModel() != null &&
             aObject.getModel().getModelDescriptor() instanceof TLcdKML22ModelDescriptor;
    }
  };

  private final List<LayerPaintingProgressIndicator> fLayerPaintingProgressIndicators = new ArrayList<>();

  @Override
  public void activate() {
    super.activate();
    List<ILspView> views = getViews();
    for (ILspView view : views) {
      fLayerPaintingProgressIndicators.add(new LayerPaintingProgressIndicator(fKMLLayerFilter, view, PROGRESS_INDICATOR_TEXT));
    }
  }

  @Override
  public void deactivate() {
    super.deactivate();
    for (LayerPaintingProgressIndicator progressIndicator : fLayerPaintingProgressIndicators) {
      progressIndicator.dispose();
    }
    fLayerPaintingProgressIndicators.clear();
  }
}
