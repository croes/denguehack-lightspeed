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
package samples.wms.server.config.editor;

import java.net.URL;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import com.luciad.wms.server.model.ALcdWMSCapabilities;
import com.luciad.wms.server.model.ALcdWMSLayer;

/**
 * This class is used to validate the WMS configuration prior to saving.
 * Descriptive warning/error messages can be retrieved and displayed so that
 * the user can correct his/her settings before saving the XML file.
 */
class WMSCapabilitiesValidator {

  private List fMessages = new ArrayList();

  /**
   * Check for duplicate layer names by putting the names in a hash table and
   * checking for collisions.
   */
  private void checkLayerNamesSFCT(ALcdWMSLayer aLayer, Map aNames) {

    String name = aLayer.getName();
    if (name != null) {
      if (aNames.get(name) != null) {
        fMessages.add(new ValidationMessage("Layer name conflict: '" + name + "' occurs multiple times."));
      }
      aNames.put(name, aLayer);
    }

    for (int i = 0; i < aLayer.getChildWMSLayerCount(); i++) {
      checkLayerNamesSFCT(aLayer.getChildWMSLayer(i), aNames);
    }
  }

  /**
   * Check the layer tree for duplicate names.
   */
  private void checkLayerNames(ALcdWMSCapabilities aCaps) {
    Map names = new Hashtable();
    checkLayerNamesSFCT(aCaps.getRootWMSLayer(0), names);
  }

  /**
   * Make sure all layers have their title set.
   */
  private void checkLayerTitles(ALcdWMSLayer aLayer) {

    String title = aLayer.getTitle();
    if ((title == null) || (title.trim().length() == 0)) {
      fMessages.add(new ValidationMessage("Layer '" + aLayer.getName() + "' does not have a title."));
    }

    for (int i = 0; i < aLayer.getChildWMSLayerCount(); i++) {
      checkLayerTitles(aLayer.getChildWMSLayer(i));
    }
  }

  /**
   * Check the layer tree for duplicate names.
   */
  private void checkLayerTitles(ALcdWMSCapabilities aCaps) {
    checkLayerTitles(aCaps.getRootWMSLayer(0));
  }

  /**
   * If the given layer is a leaf, check that it contains data.
   */
  private void checkLeavesForData(ALcdWMSLayer aLayer) {

    if (aLayer.getChildWMSLayerCount() == 0) {
      if ((aLayer.getSourceName() == null) || (aLayer.getSourceName().trim().length() == 0)) {
        fMessages.add(new ValidationMessage("Layer '" + aLayer.getTitle() + "' is a leaf in the layer tree but does not contain any data."));
      }
    } else {
      for (int i = 0; i < aLayer.getChildWMSLayerCount(); i++) {
        checkLeavesForData(aLayer.getChildWMSLayer(i));
      }
    }
  }

  /**
   * Make sure all leaf layers in the tree contain data.
   */
  private void checkLeavesForData(ALcdWMSCapabilities aCaps) {
    checkLeavesForData(aCaps.getRootWMSLayer(0));
  }

  /**
   * Check if at least one output format is published.
   */
  private void checkOutputFormats(ALcdWMSCapabilities aCaps) {
    if (aCaps.getMapFormatCount() == 0) {
      fMessages.add(new ValidationMessage("No output formats are published, so the server will not be able to serve any maps."));
    }
  }

  /** Validate the WMS service URL. If the user typed an invalid URL, an
   exception will have already been caught earlier on, and the URL object
   stored in the configuration will be null. */
  private void checkServiceURL(URL aURL) {
    if (aURL == null) {
      fMessages.add(new ValidationMessage("The service resource URL is not valid."));
    }
  }

  /**
   * Validate the current WMS configuration.
   */
  public void validate(ALcdWMSCapabilities aCaps) {

    fMessages.clear();

    checkOutputFormats(aCaps);
    checkLayerNames(aCaps);
    checkLayerTitles(aCaps);
    checkLeavesForData(aCaps);
    checkServiceURL(aCaps.getWMSServiceMetaData().getOnlineResource().getOnlineResource().getLinkage());
  }

  /**
   * Return a concatenation of all warning messages for display in the GUI.
   */
  public String getMessages() {
    String str = "";
    for (int i = 0; i < fMessages.size(); i++) {
      String msg = fMessages.get(i).toString();
      str = str + msg;
      if (i < fMessages.size() - 1) {
        str = str.concat("\n");
      }
    }
    return str;
  }

  private static class ValidationMessage {
    private String fMessage;

    public ValidationMessage(String aMessage) {
      fMessage = aMessage;
    }

    public String toString() {
      return "Warning : " + fMessage;
    }
  }
}
