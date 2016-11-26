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
package samples.wms.server;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.concurrent.atomic.AtomicReference;

import javax.servlet.ServletException;

import com.luciad.datamodel.ILcdDataObject;
import com.luciad.format.geojson.TLcdGeoJsonModelEncoder;
import com.luciad.io.ILcdOutputStreamFactory;
import com.luciad.io.ILcdOutputStreamFactoryCapable;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelEncoder;
import com.luciad.model.TLcdVectorModel;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.ILcdGXYLayerSubsetList;
import com.luciad.view.gxy.ILcdGXYView;
import com.luciad.wms.server.ILcdWMSGetFeatureInfoRequestEncoder;
import com.luciad.wms.server.TLcdWMSRequestContext;

/**
 * Feature info encoder that encodes the entire feature as geojson.
 */
public class WMSJsonFeatureInfoEncoder implements ILcdWMSGetFeatureInfoRequestEncoder {

  @Override
  public String getContentType() {
    return "application/json";
  }

  @Override
  public void encode(ILcdGXYLayerSubsetList aSelectionSublist, OutputStream aOutputStream, ILcdGXYView aGXYView, TLcdWMSRequestContext aWMSRequestContext) throws ServletException {
    encode(aSelectionSublist, new TLcdGeoJsonModelEncoder(), aOutputStream);
  }

  private static void encode(ILcdGXYLayerSubsetList aSelectionSublist, ILcdModelEncoder aModelEncoder, OutputStream aOutputStream) throws ServletException {
    try {
      ILcdGXYLayer layer = findLayerOfFirstSupportedFeature(aSelectionSublist);
      if (layer != null) {
        encodeAllElementsInLayer(aSelectionSublist, layer, aModelEncoder, aOutputStream);
      }
    } catch (IOException e) {
      throw new ServletException("Error while encoding features", e);
    }
  }

  private static ILcdGXYLayer findLayerOfFirstSupportedFeature(ILcdGXYLayerSubsetList aSelectionSublist) {
    for (Enumeration elements = aSelectionSublist.elements(); elements.hasMoreElements(); ) {
      Object element = elements.nextElement();
      if (element instanceof ILcdDataObject) {
        return aSelectionSublist.retrieveGXYLayer(element);
      }
    }
    return null;
  }

  private static void encodeAllElementsInLayer(ILcdGXYLayerSubsetList aSelectionSublist, ILcdGXYLayer aLayer, ILcdModelEncoder aModelEncoder, OutputStream aOutputStream) throws IOException {
    ILcdModel modelToEncode = convertAllFeaturesInLayerToModel(aSelectionSublist, aLayer);
    ByteArrayOutputStreamFactory byteArrayOutputStreamFactory = new ByteArrayOutputStreamFactory("out");
    if (!(aModelEncoder instanceof ILcdOutputStreamFactoryCapable)) {
      return;
    } else {
      ILcdOutputStreamFactoryCapable outputStreamFactoryCapable = (ILcdOutputStreamFactoryCapable) aModelEncoder;
      outputStreamFactoryCapable.setOutputStreamFactory(byteArrayOutputStreamFactory);
    }
    aModelEncoder.export(modelToEncode, "out");
    byte[] latestData = byteArrayOutputStreamFactory.getByteArrayOutputStream().toByteArray();
    aOutputStream.write(latestData);
    aOutputStream.flush();
  }

  private static ILcdModel convertAllFeaturesInLayerToModel(ILcdGXYLayerSubsetList aSelectionSublist, ILcdGXYLayer aLayer) {
    TLcdVectorModel model = new TLcdVectorModel(aLayer.getModel().getModelReference());
    model.setModelDescriptor(aLayer.getModel().getModelDescriptor());
    for (Enumeration elements = aSelectionSublist.elements(); elements.hasMoreElements(); ) {
      Object element = elements.nextElement();
      ILcdGXYLayer layer = aSelectionSublist.retrieveGXYLayer(element);
      if (element instanceof ILcdDataObject && layer == aLayer) {
        model.addElement(element, ILcdModel.NO_EVENT);
      }
    }
    return model;
  }

  private static class ByteArrayOutputStreamFactory implements ILcdOutputStreamFactory {
    private final AtomicReference<ByteArrayOutputStream> fByteArrayOutputStream = new AtomicReference<ByteArrayOutputStream>();
    private final String fExpectedSource;

    ByteArrayOutputStreamFactory(String aExpectedSource) {
      fExpectedSource = aExpectedSource;
    }

    @Override
    public OutputStream createOutputStream(String s) throws IOException {
      ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
      if (fExpectedSource.equals(s)) {
        fByteArrayOutputStream.set(byteArrayOutputStream);
      }
      return byteArrayOutputStream;
    }

    ByteArrayOutputStream getByteArrayOutputStream() {
      return fByteArrayOutputStream.get();
    }
  }
}
