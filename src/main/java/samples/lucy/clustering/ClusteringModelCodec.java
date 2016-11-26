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
package samples.lucy.clustering;

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
import com.luciad.model.transformation.ALcdTransformingModel;

/**
 * <p>
 *   {@code ALcyWorkspaceObjectCodec} for the {@link ALcdTransformingModel}
 *   instances created by the layer factory in {@link ClusteringFormat}
 * </p>
 *
 * <p>
 *    The layer factory used in the format decorates the model with a transforming model,
 *    and uses the transforming model as model for the layer.
 *    This class is the workspace codec for the transforming model.
 * </p>
 */
final class ClusteringModelCodec extends ALcyWorkspaceObjectCodec {

  private final String fUID;
  private final String fShortPrefix;

  public ClusteringModelCodec(String aLongPrefix, String aShortPrefix) {
    fShortPrefix = aShortPrefix;
    fUID = aLongPrefix + "clusteringModelCodec";
  }

  @Override
  public String getUID() {
    return fUID;
  }

  @Override
  public boolean canEncodeObject(ALcyWorkspaceCodec aWSCodec, Object aObject, Object aParent) {
    return aObject instanceof ALcdTransformingModel &&
           aWSCodec.canEncodeReference(((ALcdTransformingModel) aObject).getOriginalModel()) &&
           ClusteringFormat.SOURCE_NAME_FILTER.accept(((ILcdModel) aObject).getModelDescriptor().getSourceName());
  }

  @Override
  public void encodeObject(ALcyWorkspaceCodec aWSCodec, Object aObject, Object aParent, OutputStream aOut) throws IOException, TLcyWorkspaceAbortedException {
    TLcyStringProperties props = new TLcyStringProperties();
    props.putString(fShortPrefix + "originalModel", aWSCodec.encodeReference(((ALcdTransformingModel) aObject).getOriginalModel()));
    new TLcyStringPropertiesCodec().encode(props, aOut);
  }

  @Override
  public Object createObject(ALcyWorkspaceCodec aWSCodec, Object aParent, InputStream aIn) throws IOException, TLcyWorkspaceAbortedException {
    ALcyProperties properties = new TLcyStringPropertiesCodec().decode(aIn);
    String originalModelReference = properties.getString(fShortPrefix + "originalModel", null);
    if (originalModelReference != null) {
      ILcdModel originalModel = (ILcdModel) aWSCodec.decodeReference(originalModelReference);
      if (originalModel != null) {
        return ClusteringFormat.transformModel(originalModel);
      }
    }
    return null;
  }

  @Override
  public void decodeObject(ALcyWorkspaceCodec aWSCodec, Object aObject, Object aParent, InputStream aIn) throws IOException, TLcyWorkspaceAbortedException {
    //nothing to do here
  }
}
