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
package samples.lucy.gxy.waypoints;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.luciad.lucy.util.properties.ALcyProperties;
import com.luciad.lucy.util.properties.TLcyStringProperties;
import com.luciad.lucy.util.properties.codec.TLcyStringPropertiesCodec;
import com.luciad.lucy.workspace.ALcyWorkspaceCodec;
import com.luciad.lucy.workspace.ALcyWorkspaceObjectCodec;
import com.luciad.lucy.workspace.TLcyWorkspaceAbortedException;
import com.luciad.model.ILcdModel;
import com.luciad.view.gxy.ILcdGXYLayerFactory;
import com.luciad.view.gxy.TLcdGXYLayer;

/**
 * <p>
 *   {@code ALcyWorkspaceObjectCodec} implementation for the way point layers.
 * </p>
 *
 * <p>
 *  It stores a reference to the model to be able to re-create the layer when needed,
 *  and stores all style settings which can be changed by the user as well.
 * </p>
 *
 * <p>
 *   It delegates the actual layer creation to the layer factory.
 *   This is the same layer factory instance as created in the format.
 * </p>
 */
final class GXYWayPointsLayerWorkspaceCodec extends ALcyWorkspaceObjectCodec {
  private static final String MODEL_REFERENCE_KEY = "model";

  private static final String VISIBLE_KEY = "layer.visible";
  private static final String SELECTABLE_KEY = "layer.selectable";
  private static final String LABELED_KEY = "layer.labeled";
  private static final String SELECTION_LABELED_KEY = "layer.selectionLabeled";
  private static final String EDITABLE_KEY = "layer.editable";
  private static final String LABEL_KEY = "layer.label";

  private final String fUID;
  private final String fShortPrefix;
  private final ILcdGXYLayerFactory fLayerFactory;

  GXYWayPointsLayerWorkspaceCodec(String aLongPrefix, String aShortPrefix, ILcdGXYLayerFactory aLayerFactory) {
    fLayerFactory = aLayerFactory;
    fShortPrefix = aShortPrefix;
    fUID = aLongPrefix + "layerCodec";
  }

  @Override
  public String getUID() {
    return fUID;
  }

  @Override
  public boolean canEncodeObject(ALcyWorkspaceCodec aWSCodec, Object aObject, Object aParent) {
    //All checks are done by the safeguard format wrapper
    return true;
  }

  @Override
  public void encodeObject(ALcyWorkspaceCodec aWSCodec, Object aObject, Object aParent, OutputStream aOut) throws IOException, TLcyWorkspaceAbortedException {
    TLcyStringProperties properties = new TLcyStringProperties();

    TLcdGXYLayer layer = (TLcdGXYLayer) aObject;
    ILcdModel model = layer.getModel();

    String referenceToModel = aWSCodec.encodeReference(model);
    properties.putString(fShortPrefix + MODEL_REFERENCE_KEY, referenceToModel);

    //Store the style related settings
    //We only store the settings which can be altered by the user in the UI
    properties.putBoolean(fShortPrefix + VISIBLE_KEY, layer.isVisible());
    properties.putBoolean(fShortPrefix + SELECTABLE_KEY, layer.isSelectable());
    properties.putBoolean(fShortPrefix + LABELED_KEY, layer.isLabeled());
    properties.putBoolean(fShortPrefix + SELECTION_LABELED_KEY, layer.isSelectionLabeled());
    properties.putBoolean(fShortPrefix + EDITABLE_KEY, layer.isEditable());
    properties.putString(fShortPrefix + LABEL_KEY, layer.getLabel());

    new TLcyStringPropertiesCodec().encode(properties, aOut);
  }

  @Override
  public Object createObject(ALcyWorkspaceCodec aWSCodec, Object aParent, InputStream aIn) throws IOException, TLcyWorkspaceAbortedException {
    ALcyProperties props = new TLcyStringPropertiesCodec().decode(aIn);

    String modelReference = props.getString(fShortPrefix + MODEL_REFERENCE_KEY, null);
    if (modelReference != null) {
      ILcdModel model = (ILcdModel) aWSCodec.decodeReference(modelReference);
      if (model != null) {
        TLcdGXYLayer layer = (TLcdGXYLayer) fLayerFactory.createGXYLayer(model);

        //restore the style related settings
        layer.setVisible(props.getBoolean(fShortPrefix + VISIBLE_KEY, layer.isVisible()));
        layer.setSelectable(props.getBoolean(fShortPrefix + SELECTABLE_KEY, layer.isSelectable()));
        layer.setLabeled(props.getBoolean(fShortPrefix + LABELED_KEY, layer.isLabeled()));
        layer.setSelectionLabeled(props.getBoolean(fShortPrefix + SELECTION_LABELED_KEY, layer.isSelectionLabeled()));
        layer.setEditable(props.getBoolean(fShortPrefix + EDITABLE_KEY, layer.isEditable()));
        layer.setLabel(props.getString(fShortPrefix + LABEL_KEY, layer.getLabel()));

        return layer;
      }
    }
    return null;
  }

  @Override
  public void decodeObject(ALcyWorkspaceCodec aWSCodec, Object aObject, Object aParent, InputStream aIn) throws IOException, TLcyWorkspaceAbortedException {
    //all the work is done in the createObject method
    //nothing must be done here
  }
}
