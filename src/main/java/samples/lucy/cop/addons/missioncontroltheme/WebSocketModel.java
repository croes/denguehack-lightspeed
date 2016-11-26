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
package samples.lucy.cop.addons.missioncontroltheme;

import static com.luciad.util.concurrent.TLcdLockUtil.Lock;
import static com.luciad.util.concurrent.TLcdLockUtil.writeLock;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import com.luciad.datamodel.ILcdDataObject;
import com.luciad.datamodel.TLcdDataModel;
import com.luciad.datamodel.TLcdDataProperty;
import com.luciad.datamodel.TLcdDataType;
import com.luciad.format.geojson.TLcdGeoJsonModelDecoder;
import com.luciad.gui.TLcdAWTUtil;
import com.luciad.io.ILcdInputStreamFactory;
import com.luciad.lucy.ILcyLucyEnv;
import com.luciad.lucy.util.properties.ALcyProperties;
import com.luciad.model.ALcdModel;
import com.luciad.model.ILcdModel;
import com.luciad.model.TLcdDataModelDescriptor;
import com.luciad.reference.format.TLcdEPSGReferenceParser;
import com.luciad.shape.ILcdPoint;
import com.luciad.shape.ILcdShape;
import com.luciad.shape.ILcdShapeList;
import com.luciad.shape.shape3D.ILcd3DEditablePoint;
import com.luciad.util.logging.ILcdLogger;
import com.luciad.util.logging.TLcdLoggerFactory;

import samples.lucy.cop.PathResolver;
import samples.lucy.cop.websocket.WebSocketConnection;
import samples.lucy.cop.websocket.WebSocketFactory;
import samples.lucy.cop.websocket.WebSocketListener;

/**
 * Model which gets all of its data from a WebSocket connection which
 * provides GeoJson data.
 */
public class WebSocketModel extends ALcdModel {
  private static final ILcdLogger LOGGER = TLcdLoggerFactory.getLogger(WebSocketModel.class);

  private static final String SOURCE_NAME = "source";

  private final Map<Long, ILcdDataObject> fIDsToObjects = new HashMap<Long, ILcdDataObject>();
  private final TLcdDataType fDataType;
  private final String fServerIDPropertyName;
  private WebSocketConnection fWebSocketConnection;
  private final String fSourceName;

  public WebSocketModel(String aPropertiesPrefix, ALcyProperties aProperties,
                        String aTypeName, String aDisplayName,
                        TLcdDataModel aDataModel, TLcdDataType aDataType,
                        String aServerIDPropertyName, ILcyLucyEnv aLucyEnv) {
    this(aLucyEnv.getService(PathResolver.class).convertPath(aProperties.getString(aPropertiesPrefix + SOURCE_NAME, null)),
         aTypeName, aDisplayName, aDataModel, aDataType, aServerIDPropertyName);
  }

  WebSocketModel(String aSourceName, String aTypeName, String aDisplayName,
                 TLcdDataModel aDataModel, TLcdDataType aDataType,
                 String aServerIDPropertyName) {
    fDataType = aDataType;
    fServerIDPropertyName = aServerIDPropertyName;
    try {
      setModelReference(new TLcdEPSGReferenceParser().parseModelReference("EPSG:4326"));
    } catch (ParseException e) {
      throw new RuntimeException(e);
    }
    fSourceName = aSourceName;
    if (fSourceName == null) {
      LOGGER.warn("The source name for the web socket model is not available. The web socket model will remain empty");
    }
    setModelDescriptor(new TLcdDataModelDescriptor(fSourceName, aTypeName, aDisplayName, aDataModel, Collections.singleton(aDataType), aDataModel.getTypes()));
  }

  void handleWebSocketMessage(String aWebSocketMessage) {
    final InputStream is = new ByteArrayInputStream(aWebSocketMessage.getBytes());
    try {
      TLcdGeoJsonModelDecoder md = new TLcdGeoJsonModelDecoder();
      md.setModelElementType(fDataType);
      md.setInputStreamFactory(new ILcdInputStreamFactory() {
        @Override
        public InputStream createInputStream(String aSource) throws IOException {
          return is;
        }
      });
      ILcdModel model = md.decode("webSocketFeatures.json");
      Enumeration en = model.elements();
      while (en.hasMoreElements()) {
        ILcdDataObject dataObject = (ILcdDataObject) en.nextElement();
        Object id = dataObject.getValue(fServerIDPropertyName);
        Number idAsNumber = id instanceof String ? Long.valueOf(((String) id)) : (Number) id;
        handleIncomingChange(dataObject, idAsNumber.longValue());
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private WebSocketConnection createWebSocketConnection(String aSourceName) {
    if (aSourceName == null) {
      return null;
    }
    try {
      URI uri = new URI(aSourceName);

      return new WebSocketFactory().connect(uri, new WebSocketListener() {
        @Override
        public void onText(WebSocketConnection aConnection, String aString) {
          handleWebSocketMessage(aString);
        }

        @Override
        public void onData(WebSocketConnection aConnection, byte[] aPayload) {
        }

        @Override
        public void onClosed(WebSocketConnection aConnection, int aCode, String aReason) {
        }
      });
    } catch (URISyntaxException e) {
      LOGGER.error("The source for the websocket connection is incorrect [" + aSourceName + "]", e);
    } catch (IOException e) {
      LOGGER.error("The connection could not be established", e);
    }
    return null;
  }

  @Override
  public void dispose() {
    super.dispose();
    deactivate();
  }

  public void activate() {
    fWebSocketConnection = createWebSocketConnection(fSourceName);
    if (fWebSocketConnection != null) {
      LOGGER.debug("The websocket connection will be opened and the web socket model will be updated");
      fWebSocketConnection.open();
    }
  }

  public void deactivate() {
    if (fWebSocketConnection != null) {
      LOGGER.debug("The websocket connection will be closed and the web socket model will no longer be updated");
      fWebSocketConnection.close();
    }
    fWebSocketConnection = null;
  }

  @Override
  public Enumeration elements() {
    return Collections.enumeration(fIDsToObjects.values());
  }

  private void handleIncomingChange(ILcdDataObject aElement, long aElementID) {
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("Incoming change for element with id [" + aElementID + "]");
    }
    Object currentElement = fIDsToObjects.get(aElementID);
    try (Lock autoUnlock = writeLock(this)) {
    if (currentElement != null) {
      updateElement(currentElement, aElement);
    } else {
      addElementToModel(aElement, aElementID);
    }
    }
  }

  private void addElementToModel(final ILcdDataObject aElement, long aElementID) {
    fIDsToObjects.put(aElementID, aElement);
    TLcdAWTUtil.invokeNowOrLater(new Runnable() {
      @Override
      public void run() {
        fModelEventSupport.elementAdded(aElement, FIRE_NOW);
      }
    });
  }

  private void updateElement(final Object aCurrentElement, Object aUpdatedElement) {
    if (aCurrentElement instanceof ILcdDataObject && aUpdatedElement instanceof ILcdDataObject) {
      for (TLcdDataProperty property : ((ILcdDataObject) aCurrentElement).getDataType().getProperties()) {
        ((ILcdDataObject) aCurrentElement).setValue(property.getName(), ((ILcdDataObject) aUpdatedElement).getValue(property.getName()));
      }
    }
    ILcdShape currentShape = (ILcdShape) aCurrentElement;
    if (aCurrentElement instanceof ILcdShapeList) {
      currentShape = ((ILcdShapeList) aCurrentElement).getShape(0);
    }
    ILcdShape updatedShape = (ILcdShape) aUpdatedElement;
    if (aUpdatedElement instanceof ILcdShapeList) {
      updatedShape = ((ILcdShapeList) aUpdatedElement).getShape(0);
    }
    if (currentShape instanceof ILcd3DEditablePoint && updatedShape instanceof ILcdPoint) {
      ((ILcd3DEditablePoint) currentShape).move3D(((ILcdPoint) updatedShape));
    }
    TLcdAWTUtil.invokeNowOrLater(new Runnable() {
      @Override
      public void run() {
        fModelEventSupport.elementChanged(aCurrentElement, FIRE_NOW);
      }
    });
  }
}
