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
package samples.lucy.density;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

import com.luciad.lucy.map.ILcySnappable;
import com.luciad.lucy.util.properties.ALcyProperties;
import com.luciad.lucy.util.properties.TLcyStringProperties;
import com.luciad.lucy.util.properties.codec.TLcyStringPropertiesCodec;
import com.luciad.lucy.workspace.ALcyWorkspaceCodec;
import com.luciad.lucy.workspace.ALcyWorkspaceObjectCodec;
import com.luciad.model.ILcdModel;
import com.luciad.util.ILcdFilter;
import com.luciad.util.ILcdFireEventMode;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.ILcdGXYLayerFactory;
import com.luciad.view.gxy.TLcdGXYLayer;

/**
 * Extension of <code>ALcyWorkspaceObjectCodec</code> that can encode/decode a
 * <code>ILcdGXYLayer</code>.  An <code>ILcdFilter</code> must be provided to make sure this codec
 * does not try to encode layers that it should not encode.  A <code>ILcdGXYLayerFactory</code> must
 * be provided to create the actual <code>ILcdGXYLayer</code> instances.
 *
 */
final class WorkspaceGXYLayerCodec extends ALcyWorkspaceObjectCodec {

  private static final String VISIBLE = "visible";
  private static final String SELECTABLE = "selectable";
  private static final String EDITABLE = "editable";
  private static final String LABELED = "labeled";
  private static final String SELECTIONLABELED = "selectionLabeled";
  private static final String SNAPPINGON = "snappingOn";
  private static final String LABEL = "label";
  private static final String MODEL = "model";
  private static final String SELECTION = "selection";

  private String fUID;
  private final String fShortPrefix;
  private ILcdFilter<ILcdModel> fModelFilter;
  private ILcdGXYLayerFactory fGXYLayerFactory;

  /**
   * Creates a new <code>WorkspaceGXYLayerCodec</code>.
   *
   * @param aUID The UID for this layer codec.  Every instance must have a different UID.
   *@param aShortPrefix
   * @param aModelFilter The filter that must only return true in its accept method for models
   * of layers that this codec knows how to encode.
   *@param aGXYLayerFactory The <code>ILcdGXYLayerFactory</code> used to create the actual layer
   */
  public WorkspaceGXYLayerCodec(String aUID,
                                String aShortPrefix,
                                ILcdFilter<ILcdModel> aModelFilter,
                                ILcdGXYLayerFactory aGXYLayerFactory) {
    fUID = aUID;
    fShortPrefix = aShortPrefix;
    fModelFilter = aModelFilter;
    fGXYLayerFactory = aGXYLayerFactory;
  }

  @Override
  public String getUID() {
    return fUID;
  }

  public String getShortPrefix() {
    return fShortPrefix;
  }

  @Override
  public boolean canEncodeObject(ALcyWorkspaceCodec aWSCodec, Object aObject, Object aParent) {
    return aObject instanceof ILcdGXYLayer &&
           fModelFilter.accept(((ILcdGXYLayer) aObject).getModel()) &&

           //Because the layer has no meaning if its model can't be restored, we put this test in here.
           aWSCodec.canEncodeReference(((ILcdGXYLayer) aObject).getModel());
  }

  @Override
  public void encodeObject(ALcyWorkspaceCodec aWSCodec,
                           Object aObject,
                           Object aParent,
                           OutputStream aOut) throws IOException {
    if (!canEncodeObject(aWSCodec, aObject, aParent)) {
      throw new IOException("Cannot encode object[" + aObject + "]");
    }

    TLcyStringProperties props = new TLcyStringProperties();
    encodeObject(aWSCodec, aObject, props);

    //Store the actual data to the output stream
    new TLcyStringPropertiesCodec().encode(props, aOut);
  }

  public void encodeObject(ALcyWorkspaceCodec aWSCodec,
                           Object aObject,
                           TLcyStringProperties aProperties) throws IOException {
    //Write some of the layer properties.  More properties will be needed
    //to fully restore the layer state, such as color settings.
    ILcdGXYLayer layer = (ILcdGXYLayer) aObject;
    writeGXYLayerProperties(layer, aProperties, fShortPrefix);

    String model_ref = aWSCodec.encodeReference(layer.getModel());
    if (model_ref != null) {
      aProperties.putString(fShortPrefix + MODEL, model_ref);
    }

    encodeSelection(aWSCodec, layer, aProperties);
  }

  @Override
  public Object createObject(ALcyWorkspaceCodec aWSCodec, Object aParent, InputStream aIn) throws IOException {
    // Load properties from input stream.
    ALcyProperties props = new TLcyStringPropertiesCodec().decode(aIn);

    // Create/decode layer.
    return createObject(aWSCodec, props);
  }

  public Object createObject(ALcyWorkspaceCodec aWSCodec, ALcyProperties aProperties) throws IOException {
    String model_ref = aProperties.getString(fShortPrefix + MODEL, null);
    if (model_ref != null) {
      //In the javadoc of 'createObject', there is a note concerning the method call below.
      //Because we know the model will not keep a reference to the layer we are decoding here,
      //there is no risk for circular references, and it is safe to call this method anyway.
      ILcdModel model = (ILcdModel) aWSCodec.decodeReference(model_ref);
      if (model == null) {
        //At this point, we know that either:
        //1) The model could not be decoded for some reason: the model codec will write a message
        //2) Or that there is no suitable codec for the model, the framework will write a message
        //   about that.

        // => return null: this layer has no meaning without its model anyway.
        return null;
      } else {
        ILcdGXYLayer layer = fGXYLayerFactory.createGXYLayer(model);
        if (layer != null) {
          readGXYLayerProperties(layer, aProperties, fShortPrefix);
          decodeSelection(aWSCodec, layer, aProperties);
          return layer;
        } else {
          aWSCodec.getLogListener().warn(this, "decodeLayer: Can't create layer for model of '" + model_ref + "'");
        }
      }
    } else {
      aWSCodec.getLogListener().warn(this, "decodeLayer: model of layer is null");
    }
    return null;
  }

  @Override
  public void decodeObject(ALcyWorkspaceCodec aWSCodec, Object aObject,
                           Object aParent, InputStream aIn) throws IOException {
    //nothing else to do here, everything is initialised in createObject
  }

  public static void writeGXYLayerProperties(ILcdGXYLayer aGXYLayer, ALcyProperties aProperties, String aShortPrefix) {
    aProperties.putString(aShortPrefix + LABEL, aGXYLayer.getLabel());
    aProperties.putBoolean(aShortPrefix + VISIBLE, aGXYLayer.isVisible());
    aProperties.putBoolean(aShortPrefix + SELECTABLE, aGXYLayer.isSelectable());
    aProperties.putBoolean(aShortPrefix + EDITABLE, aGXYLayer.isEditable());
    aProperties.putBoolean(aShortPrefix + LABELED, aGXYLayer.isLabeled());
    if (aGXYLayer instanceof TLcdGXYLayer) {
      aProperties.putBoolean(aShortPrefix + SELECTIONLABELED, ((TLcdGXYLayer) aGXYLayer).isSelectionLabeled());
    }
    if (aGXYLayer instanceof ILcySnappable) {
      aProperties.putBoolean(aShortPrefix + SNAPPINGON, ((ILcySnappable) aGXYLayer).isSnappingOn());
    }
  }

  public static void readGXYLayerProperties(ILcdGXYLayer aGXYLayer, ALcyProperties aProperties, String aShortPrefix) {
    //Restore some of the basic layer properties.
    aGXYLayer.setLabel(aProperties.getString(aShortPrefix + LABEL, aGXYLayer.getLabel()));
    aGXYLayer.setVisible(aProperties.getBoolean(aShortPrefix + VISIBLE, aGXYLayer.isVisible()));
    aGXYLayer.setSelectable(aProperties.getBoolean(aShortPrefix + SELECTABLE, aGXYLayer.isSelectable()));
    aGXYLayer.setEditable(aProperties.getBoolean(aShortPrefix + EDITABLE, aGXYLayer.isEditable()));
    aGXYLayer.setLabeled(aProperties.getBoolean(aShortPrefix + LABELED, aGXYLayer.isLabeled()));
    if (aGXYLayer instanceof TLcdGXYLayer) {
      ((TLcdGXYLayer) aGXYLayer).setSelectionLabeled(aProperties.getBoolean(aShortPrefix + SELECTIONLABELED, ((TLcdGXYLayer) aGXYLayer).isSelectionLabeled()));
    }
    if (aGXYLayer instanceof ILcySnappable) {
      ((ILcySnappable) aGXYLayer).setSnappingOn(aProperties.getBoolean(aShortPrefix + SNAPPINGON, ((ILcySnappable) aGXYLayer).isSnappingOn()));
    }
  }

  /**
   * Encodes the selection of the given aGXYLayer.
   * @param aWSCodec The Workspace codec.
   * @param aGXYLayer The layer to encode the selection of.
   * @param aPropsSFCT The properties object to store the selection in.
   *
   * @throws IOException In case of IO failure.
   */
  private void encodeSelection(ALcyWorkspaceCodec aWSCodec,
                               ILcdGXYLayer aGXYLayer,
                               TLcyStringProperties aPropsSFCT)
      throws IOException {

    ILcdModel model = aGXYLayer.getModel();
    Enumeration selected_objects = aGXYLayer.selectedObjects();
    ArrayList selection_refs = new ArrayList(aGXYLayer.getSelectionCount());
    while (selected_objects.hasMoreElements()) {
      Object selected_object = selected_objects.nextElement();
      if (aWSCodec.canEncodeReference(selected_object, aGXYLayer.getModel())) {
        selection_refs.add(aWSCodec.encodeReference(selected_object, model));
      }
    }
    aPropsSFCT.putStringArray(fShortPrefix + SELECTION, (String[]) selection_refs.toArray(new String[selection_refs.size()]));
  }

  /**
   * Restores the selection of the given aGXYLayer.
   *
   * @param aWSCodec The workspace codec.
   * @param aGXYLayer The layer to restore the selection of.
   * @param aProps The properties object containing the selection to restore.
   * @throws IOException In case of IO failure.
   */
  private void decodeSelection(ALcyWorkspaceCodec aWSCodec,
                               final ILcdGXYLayer aGXYLayer,
                               ALcyProperties aProps)
      throws IOException {

    List selection_refs = Arrays.asList(aProps.getStringArray(fShortPrefix + SELECTION, new String[0]));
    if (selection_refs != null) {
      for (int i = 0; i < selection_refs.size(); i++) {
        Object object = aWSCodec.decodeReference((String) selection_refs.get(i));
        if (object != null) {
          aGXYLayer.selectObject(object, true, ILcdFireEventMode.FIRE_LATER);
        }
      }

      if (!selection_refs.isEmpty()) {
        aGXYLayer.fireCollectedSelectionChanges();
      }
    }
  }
}
