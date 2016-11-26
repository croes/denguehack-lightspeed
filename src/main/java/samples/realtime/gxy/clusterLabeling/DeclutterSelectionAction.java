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
package samples.realtime.gxy.clusterLabeling;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import com.luciad.gui.ALcdAction;
import com.luciad.gui.TLcdImageIcon;
import com.luciad.util.ILcdFilter;
import com.luciad.view.gxy.ILcdGXYEditableLabelsLayer;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.ILcdGXYView;
import com.luciad.view.labeling.algorithm.TLcdCollectedLabeledObjectInfo;

/**
 * Action that smoothly declutters the labels of the selected objects, but keeps the other labels
 * fixed.
 */
class DeclutterSelectionAction extends ALcdAction {

  private static final String KEY_SELECTED = "selected";

  private final ILcdGXYView fGXYView;
  private final ILcdFilter fModelFilter;
  private final AnimatedDeclutterLabelingAlgorithmProvider fAlgorithmProvider;

  public DeclutterSelectionAction(ILcdFilter aModelFilter, ILcdGXYView aGXYView, AnimatedDeclutterLabelingAlgorithmProvider aAlgorithmProvider) {
    super("Declutter selection", new TLcdImageIcon("images/gui/i16_eyes.gif"));
    fGXYView = aGXYView;
    fModelFilter = aModelFilter;
    fAlgorithmProvider = aAlgorithmProvider;
  }

  public void actionPerformed(ActionEvent e) {
    Object selected = getValue(KEY_SELECTED);

    if (Boolean.TRUE.equals(selected)) {
      putValue(KEY_SELECTED, Boolean.FALSE);
      fAlgorithmProvider.stopDecluttering();
    } else {
      putValue(KEY_SELECTED, Boolean.TRUE);
      fAlgorithmProvider.declutterObjects(getSelectedObjects());
    }
  }

  private List<TLcdCollectedLabeledObjectInfo> getSelectedObjects() {
    List<TLcdCollectedLabeledObjectInfo> objectsToDeclutter = new ArrayList<TLcdCollectedLabeledObjectInfo>();
    for (Enumeration layers = fGXYView.layersBackwards(); layers.hasMoreElements(); ) {
      ILcdGXYLayer layer = (ILcdGXYLayer) layers.nextElement();

      if (!(layer instanceof ILcdGXYEditableLabelsLayer)) {
        continue;
      }

      if (!fModelFilter.accept(layer.getModel())) {
        continue;
      }

      for (Enumeration selection = layer.selectedObjects(); selection.hasMoreElements(); ) {
        Object selected_object = selection.nextElement();
        TLcdCollectedLabeledObjectInfo labeled_object = new TLcdCollectedLabeledObjectInfo(selected_object, layer);
        objectsToDeclutter.add(labeled_object);
      }
    }
    return objectsToDeclutter;
  }
}
