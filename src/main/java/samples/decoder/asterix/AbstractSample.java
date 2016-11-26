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
package samples.decoder.asterix;

import java.io.IOException;

import com.luciad.util.ILcdFilter;
import com.luciad.util.logging.ILcdLogger;
import com.luciad.util.logging.TLcdLoggerFactory;
import com.luciad.view.gxy.ILcdGXYEditableLabelsLayer;
import com.luciad.view.gxy.controller.TLcdGXYEditController2;

import samples.gxy.common.GXYSample;
import samples.realtime.gxy.tracksimulator.FirstTouchedLabelEditController;

/**
 * Abstract base class that provides the common functionality for the ASTERIX samples.
 */
public abstract class AbstractSample extends GXYSample {

  private static ILcdLogger sLogger = TLcdLoggerFactory.getLogger(AbstractSample.class.getName());

  private static final String CONFIG_NAME = "samples/decoder/asterix/locations.cfg";

  private TransformationProvider fTransformationProvider = null;

  @Override
  protected void createGUI() {
    try {
      fTransformationProvider = new TransformationProvider(CONFIG_NAME);
    } catch (IOException e) {
      sLogger.error(TransformationProvider.class.getName() + ".<init>",
                    "TransformationProvider: Failed to read/find config file[" + CONFIG_NAME +
                    "] relative to classpath", e);
    }
    initializeDecoders();

    super.createGUI();

    //add the selection listener that will pop up the frame with the data object information
    getToolBars()[0].setGXYControllerEdit(createEditController());
    getView().setGXYController(getToolBars()[0].getGXYCompositeEditController());
  }

  //Please refer to the realtime developer guide for more information about this.
  private TLcdGXYEditController2 createEditController() {
    FirstTouchedLabelEditController editController = new FirstTouchedLabelEditController();
    editController.setStickyLabelsLayerFilter(new MySelectableRealtimeLayersFilter());
    editController.setInstantEditing(true);
    editController.setEditFirstTouchedLabelOnly(true);
    return editController;
  }

  /**
   * Filter that only accepts layers if
   *  - they are ILcdGXYEditableLabelsLayer
   *  - they are selectable
   *  - they are realtime layers (defined by the number of cached background layers)
   */
  private class MySelectableRealtimeLayersFilter implements ILcdFilter {
    public boolean accept(Object aObject) {
      if (aObject instanceof ILcdGXYEditableLabelsLayer) {
        ILcdGXYEditableLabelsLayer layer = (ILcdGXYEditableLabelsLayer) aObject;
        return layer.isSelectableSupported() && layer.isSelectable() &&
               getView().indexOf(layer) >= getView().getNumberOfCachedBackgroundLayers();
      } else {
        return false;
      }
    }
  }

  protected void initializeDecoders() {
  }

  public TransformationProvider getTransformationProvider() {
    return fTransformationProvider;
  }
}
