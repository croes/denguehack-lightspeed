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
package samples.lucy.cop.gazetteer;

import java.awt.Component;

import com.luciad.lucy.gui.ALcyApplicationPaneTool;
import com.luciad.lucy.util.properties.ALcyProperties;
import com.luciad.view.lightspeed.layer.ILspLayer;

/**
 * {@code ALcyApplicationPaneTool} for the gazetteer UI
 */
final class GazetteerApplicationPaneTool extends ALcyApplicationPaneTool {
  private final GazetteerModel fGazetteerModel;
  private final ILspLayer fGazetteerLayer;

  GazetteerApplicationPaneTool(ALcyProperties aProperties, String aLongPrefix, String aShortPrefix,
                               GazetteerModel aGazetteerModel, ILspLayer aGazetteerLayer) {
    super(aProperties, aLongPrefix, aShortPrefix);
    fGazetteerModel = aGazetteerModel;
    fGazetteerLayer = aGazetteerLayer;
  }

  @Override
  protected Component createContent() {
    return new GazetteerUI(fGazetteerModel, fGazetteerLayer);
  }
}
