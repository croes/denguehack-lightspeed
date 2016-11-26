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

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import com.luciad.datamodel.ILcdDataObject;
import com.luciad.format.geojson.TLcdGeoJsonModelDecoder;
import com.luciad.gui.TLcdAWTUtil;
import com.luciad.io.ILcdInputStreamFactory;
import com.luciad.model.ILcdDataModelDescriptor;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelDecoder;
import com.luciad.model.TLcd2DBoundsIndexedModel;
import com.luciad.model.TLcdDataModelDescriptor;
import com.luciad.model.TLcdModelDescriptor;
import com.luciad.reference.TLcdGeodeticReference;
import com.luciad.util.ILcdStatusListener;
import com.luciad.util.ILcdStatusSource;
import com.luciad.util.TLcdStatusEvent;
import com.luciad.util.TLcdStatusEventSupport;
import com.luciad.util.concurrent.TLcdLockUtil;
import com.luciad.util.logging.ILcdLogger;
import com.luciad.util.logging.TLcdLoggerFactory;

/**
 * {@code ILcdModel} implementation for the gazetteer layer. You can call the
 * {@link #showElementsOfType(Type) showElementsOfType}
 * method to indicate which elements should be shown.
 *
 */
final class GazetteerModel extends TLcd2DBoundsIndexedModel implements ILcdStatusSource {
  private static final ILcdLogger LOGGER = TLcdLoggerFactory.getLogger(GazetteerModel.class);
  private static final String TYPE_NAME = "GazetteerType";
  private static final String DISPLAY_NAME = "Gazetteer model descriptor";

  /**
   * Enum of the supported types
   */
  public static enum Type {
    AIRPORT("Airport", "Airport"),
    SCHOOL("School", "School"),
    HOSPITAL("Hospital", "Hospital");

    /**
     * The type name which should be used in the query
     */
    private final String fTypeNameForQuery;
    private final String fDisplayName;

    private Type(String aQueryTypeName, String aDisplayName) {
      fTypeNameForQuery = aQueryTypeName;
      fDisplayName = aDisplayName;
    }

    String getTypeNameForQuery() {
      return fTypeNameForQuery;
    }

    public String getDisplayName() {
      return fDisplayName;
    }
  }

  private final String fServerAddress;
  private final ExecutorService fExecutor = createExecutorService();
  private final TLcdStatusEventSupport fStatusEventSupport = new TLcdStatusEventSupport();

  public GazetteerModel(String aServerAddress) {
    fServerAddress = aServerAddress;
    setModelReference(new TLcdGeodeticReference());
    setModelDescriptor(new TLcdModelDescriptor(null, TYPE_NAME, DISPLAY_NAME));
  }

  public void showElementsOfType(Type aType) {
    fExecutor.submit(new ModelUpdaterRunnable(aType, this, fStatusEventSupport));
  }

  @Override
  public void addStatusListener(ILcdStatusListener aListener) {
    fStatusEventSupport.addStatusListener(aListener);
  }

  @Override
  public void removeStatusListener(ILcdStatusListener aListener) {
    fStatusEventSupport.removeStatusListener(aListener);
  }

  /**
   * Returns the {@code Type} represented by the model object {@code aModelObject}
   * @param aModelObject The model object
   * @return The type of the model object
   */
  public static Type retrieveType(Object aModelObject) {
    final String featureClass = "featureClass";
    if (aModelObject instanceof ILcdDataObject && ((ILcdDataObject) aModelObject).hasValue(featureClass)) {
      String queryFeatureClass = (String) ((ILcdDataObject) aModelObject).getValue(featureClass);
      for (Type type : Type.values()) {
        if (queryFeatureClass.equals(type.getTypeNameForQuery())) {
          return type;
        }
      }
    }
    throw new IllegalArgumentException("Object [" + aModelObject + "] is not an element of the gazetteer model");
  }

  /**
   * Runnable which allows to update the gazetteer model on a background thread
   */
  private static class ModelUpdaterRunnable implements Runnable {
    private final Type fType;
    private final GazetteerModel fGazetteerModel;
    private final TLcdStatusEventSupport fStatusEventSupport;

    private ModelUpdaterRunnable(Type aType, GazetteerModel aGazetteerModel, TLcdStatusEventSupport aStatusEventSupport) {
      fType = aType;
      fGazetteerModel = aGazetteerModel;
      fStatusEventSupport = aStatusEventSupport;
    }

    @Override
    public void run() {
      String serverAddress = fGazetteerModel.fServerAddress;
      String query = serverAddress + "gazetteer/search?class=" + fType.getTypeNameForQuery() + "&minX=-80.19&minY=33.76&maxX=-66.33&maxY=46.57&limit=10000";

      try (TLcdStatusEvent.Progress ignored = TLcdStatusEvent.startIndeterminateProgress(fStatusEventSupport.asListener(), this, "Start updating gazetteer model")) {
        DefaultHttpClient httpClient = new DefaultHttpClient();
        HttpGet request = new HttpGet(query);
        try {
          HttpResponse response = httpClient.execute(request);
          int statusCode = response.getStatusLine().getStatusCode();
          if (statusCode == 200) {
            HttpEntity entity = response.getEntity();
            final String dummySource = "gazetteer_dummy_source.json";
            ILcdModelDecoder modelDecoder = createModelDecoderForServerResponse(entity.getContent(), dummySource);
            ILcdModel decodedModelFromServerResponse = modelDecoder.decode(dummySource);
            updateGazetteerModel(decodedModelFromServerResponse);
          } else {
            LOGGER.error("Cannot update gazetteer model. Server responded with code " + statusCode + " for request [" + query + "]");
          }
        } finally {
          request.releaseConnection();
        }
      } catch (IOException e) {
        LOGGER.error("Exception while updating the gazetteer model", e);
      }
    }

    private void updateGazetteerModel(final ILcdModel aDecodedModelFromServerResponse) {
      TLcdAWTUtil.invokeAndWait(new Runnable() {
        @Override
        public void run() {
          try (TLcdLockUtil.Lock autoUnlock = TLcdLockUtil.writeLock(fGazetteerModel)) {
            fGazetteerModel.removeAllElements(FIRE_LATER);
            Enumeration elements = aDecodedModelFromServerResponse.elements();
            while (elements.hasMoreElements()) {
              Object element = elements.nextElement();
              fGazetteerModel.addElement(element, FIRE_LATER);
            }
            fGazetteerModel.setModelReference(aDecodedModelFromServerResponse.getModelReference());
            ILcdDataModelDescriptor dataModelDescriptor = (ILcdDataModelDescriptor) aDecodedModelFromServerResponse.getModelDescriptor();
            fGazetteerModel.setModelDescriptor(
                new TLcdDataModelDescriptor(null,
                                            TYPE_NAME,
                                            DISPLAY_NAME,
                                            dataModelDescriptor.getDataModel(),
                                            dataModelDescriptor.getModelElementTypes(),
                                            dataModelDescriptor.getModelTypes()));
          }
          fGazetteerModel.fireCollectedModelChanges();
        }
      });
    }

    private ILcdModelDecoder createModelDecoderForServerResponse(InputStream aInputStream, String aDummySourceName) {
      TLcdGeoJsonModelDecoder modelDecoder = new TLcdGeoJsonModelDecoder();
      modelDecoder.setInputStreamFactory(new DummyInputStreamFactory(aDummySourceName, aInputStream));
      return modelDecoder;
    }
  }

  private static class DummyInputStreamFactory implements ILcdInputStreamFactory {
    private final String fDummySourceName;
    private final InputStream fInputStream;

    private DummyInputStreamFactory(String aDummySourceName, InputStream aInputStream) {
      fDummySourceName = aDummySourceName;
      fInputStream = aInputStream;
    }

    @Override
    public InputStream createInputStream(String aSource) throws IOException {
      if (fDummySourceName.equals(aSource)) {
        return fInputStream;
      }
      throw new IOException("Cannot create input stream for [" + aSource + "]");
    }
  }

  private static ExecutorService createExecutorService() {
    ThreadFactory threadFactory = new ThreadFactory() {
      @SuppressWarnings("NullableProblems")
      @Override
      public Thread newThread(Runnable r) {
        Thread thread = new Thread(r, "Gazetteer model updater");
        thread.setDaemon(true);
        return thread;
      }
    };
    return Executors.newSingleThreadExecutor(threadFactory);
  }
}
