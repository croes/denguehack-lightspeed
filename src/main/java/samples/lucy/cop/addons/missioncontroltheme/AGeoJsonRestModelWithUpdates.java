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

import static com.luciad.util.concurrent.TLcdLockUtil.*;

import static samples.lucy.cop.addons.missioncontroltheme.GeoJsonServerCommunicationUtil.copyAllShapes;
import static samples.lucy.cop.addons.missioncontroltheme.GeoJsonServerCommunicationUtil.copyDataObjectValues;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import com.luciad.datamodel.TLcdDataModel;
import com.luciad.datamodel.TLcdDataType;
import com.luciad.gui.TLcdAWTUtil;
import com.luciad.lucy.ILcyLucyEnv;
import com.luciad.lucy.util.properties.ALcyProperties;
import com.luciad.model.ALcdModel;
import com.luciad.model.ILcdModel;
import com.luciad.model.TLcdDataModelDescriptor;
import com.luciad.reference.format.TLcdEPSGReferenceParser;
import com.luciad.shape.ILcdShape;
import com.luciad.util.logging.ILcdLogger;
import com.luciad.util.logging.TLcdLoggerFactory;

import samples.lucy.cop.PathResolver;
import samples.lucy.cop.websocket.WebSocketConnection;
import samples.lucy.cop.websocket.WebSocketFactory;
import samples.lucy.cop.websocket.WebSocketListener;

/**
 * <p>{@code ILcdModel} which uses a REST service to obtain the model
 * elements, and a {@code WebSocketNotifier} to get updates from the server.</p>
 *
 * <p>During the initialization, the model will make a copy of all the data on
 * the server to have its own local copy. Adding/removing/updating an element will
 * push those changes to the server.</p>
 *
 * <p>When the server pushes changes to the client, the {@code WebSocketNotifier} will update
 * the local copy of the element.</p>
 *
 * <p>This class is only designed to communicate with a server which uses GeoJson to transfer
 * the data between the client and the server.</p>
 */
abstract class AGeoJsonRestModelWithUpdates extends ALcdModel {

  private static final ILcdLogger LOGGER = TLcdLoggerFactory.getLogger(AGeoJsonRestModelWithUpdates.class);

  private static final String SOURCE = "source";
  private static final String WEB_SOCKET_SOURCE = "webSocketSource";

  private final DefaultHttpClient fHttpClient = new DefaultHttpClient();
  private final String fRESTServiceURL;
  private final String fWebSocketSource;
  private final GeoJsonServerCommunicationUtil fGeoJsonUtil;
  private WebSocketConnection fWebSocketConnection;

  private final Map<Integer, GeoJsonRestModelElement> fIDToObjectMap = new ConcurrentHashMap<>();
  private final Set<GeoJsonRestModelElement> fAddedButNotCommittedElements = new HashSet<>();

  /**
   * Do all updates on a single thread.
   */
  private final ExecutorService fExecutor = createSingleThreadExecutorService();

  protected AGeoJsonRestModelWithUpdates(TLcdDataModel aDataModel,
                                         TLcdDataType aDomainObjectDataType,
                                         String aTypeName,
                                         String aDisplayName,
                                         String aPropertyPrefix,
                                         ALcyProperties aProperties, ILcyLucyEnv aLucyEnv) {
    this(aDataModel, aDomainObjectDataType, aTypeName, aDisplayName,
         aLucyEnv.getService(PathResolver.class).convertPath(aProperties.getString(aPropertyPrefix + SOURCE, null)),
         aLucyEnv.getService(PathResolver.class).convertPath(aProperties.getString(aPropertyPrefix + WEB_SOCKET_SOURCE, null)));
  }

  AGeoJsonRestModelWithUpdates(TLcdDataModel aDataModel,
                               TLcdDataType aDomainObjectDataType,
                               String aTypeName,
                               String aDisplayName,
                               String aRESTServiceURL,
                               String aWebSocketURL) {
    try {
      setModelReference(new TLcdEPSGReferenceParser().parseModelReference("EPSG:4326"));
    } catch (ParseException e) {
      throw new RuntimeException(e);
    }
    fRESTServiceURL = aRESTServiceURL;
    if (fRESTServiceURL == null) {
      throw new RuntimeException("The source of the REST service is not configured in the properties");
    }
    fWebSocketSource = aWebSocketURL;
    if (fWebSocketSource == null) {
      throw new RuntimeException("The source of the web socket service is not configured in the properties");
    }
    setModelDescriptor(
        new TLcdDataModelDescriptor(fRESTServiceURL,
                                    aTypeName,
                                    aDisplayName,
                                    aDataModel,
                                    Collections.singleton(aDomainObjectDataType),
                                    aDataModel.getTypes()));
    fGeoJsonUtil = new GeoJsonServerCommunicationUtil(this);

    addAllElementsFromServerToThisModel();
    fWebSocketConnection = createWebSocketConnection();
    if (fWebSocketConnection != null) {
      LOGGER.debug("The websocket connection will be opened and the REST model will be updated");
      fWebSocketConnection.open();
    } else {
      throw new RuntimeException("No websocket connection could be established.");
    }
  }

  private void handleWebSocketInput(String aIncomingWebSocketMessage) {
    if (isAddOrUpdateWebSocketMessage(aIncomingWebSocketMessage)) {
      ILcdModel model = fGeoJsonUtil.convertServerResponseToModel(aIncomingWebSocketMessage);
      if (model != null) {
        fExecutor.submit(new WebSocketAddOrUpdateRunnable(model));
      }
    } else {
      fExecutor.submit(new WebSocketDeleteRunnable(fGeoJsonUtil.convertToIDsList(aIncomingWebSocketMessage)));
    }
  }

  private boolean isAddOrUpdateWebSocketMessage(String aIncomingWebSocketMessage) {
    return aIncomingWebSocketMessage.contains("Feature");
  }

  @Override
  public void dispose() {
    super.dispose();
    if (fWebSocketConnection != null) {
      LOGGER.debug("The websocket connection will be closed and the rest model will no longer be updated");
      fWebSocketConnection.close();
    }
    fWebSocketConnection = null;
  }

  @Override
  public Enumeration elements() {
    try (Lock autoUnlock = readLock(this)) {
      //make a copy of the elements
      Set<GeoJsonRestModelElement> resultingList = new HashSet<>();
      resultingList.addAll(fIDToObjectMap.values());
      resultingList.addAll(fAddedButNotCommittedElements);
      return Collections.enumeration(resultingList);
    }
  }

  @Override
  public boolean canRemoveElement(Object aElement) {
    return aElement instanceof GeoJsonRestModelElement &&
           fIDToObjectMap.values().contains(aElement);
  }

  @Override
  public void removeElement(Object aElement, int aEventMode) {
    if (fAddedButNotCommittedElements.contains(aElement)) {
      fAddedButNotCommittedElements.remove(aElement);
      fModelEventSupport.elementRemoved(aElement, aEventMode);
      return;
    }
    if (canRemoveElement(aElement)) {
      fIDToObjectMap.remove(((GeoJsonRestModelElement) aElement).getID());
      fModelEventSupport.elementRemoved(aElement, aEventMode);
      fExecutor.submit(new CommitLocalRemoveRunnable((GeoJsonRestModelElement) aElement));
    } else {
      throw new UnsupportedOperationException("Cannot remove element [" + aElement + "]. Call canRemoveElement first");
    }
  }

  @Override
  public boolean canAddElement(Object aElement) {
    return aElement instanceof GeoJsonRestModelElement;
  }

  /**
   * {@inheritDoc}
   *
   * <p>Adding an element will not add the element to the server. You need to manually call the
   * {@link #creationFinished(GeoJsonRestModelElement)} method once the creation is done.</p>
   *
   * @param aElement   {@inheritDoc}
   * @param aEventMode {@inheritDoc}
   *
   * @see #creationFinished(GeoJsonRestModelElement)
   */
  @Override
  public void addElement(Object aElement, int aEventMode) {
    GeoJsonRestModelElement domainObject = (GeoJsonRestModelElement) aElement;
    fAddedButNotCommittedElements.add(domainObject);
    fModelEventSupport.elementAdded(aElement, aEventMode);
  }

  /**
   * Method which needs to be called when the creation of an object is finished. This will commit
   * the changes to the server.
   *
   * @param aAddedElement The element of which the creation is finished
   *
   * @see #addElement(Object, int)
   */
  public void creationFinished(GeoJsonRestModelElement aAddedElement) {
    boolean wasPreviouslyAdded = false;
    try (Lock autoUnlock = writeLock(this)) {
      //remove the element again. The element will be re-added when the server response is received,
      //and the element has an id
      //this avoids that the element can be edited when it not yet has an ID
      wasPreviouslyAdded = fAddedButNotCommittedElements.remove(aAddedElement);
    }
    if (wasPreviouslyAdded) {
      fModelEventSupport.elementRemoved(aAddedElement, FIRE_NOW);
    }
    fExecutor.submit(new CommitLocalAddRunnable(aAddedElement));
  }

  /**
   * Method which needs to be called when the editing of an object is finished. This will
   * commit the changes to the server.
   * @param aEditedElement The element of which the editing has finished
   */
  public void editingFinished(GeoJsonRestModelElement aEditedElement) {
    if (fIDToObjectMap.values().contains(aEditedElement)) {
      fExecutor.submit(new CommitLocalChangeRunnable(aEditedElement));
    }
  }

  @Override
  public void elementsChanged(Vector aElements, int aFireEventMode) {
    throw new UnsupportedOperationException("It is not possible to change multiple elements at once. Use elementChanged for each element individually");
  }

  @Override
  public void allElementsChanged(int aFireEventMode) {
    throw new UnsupportedOperationException("It is not possible to change all elements at once. Use elementChanged for each element individually");
  }

  /**
   * {@inheritDoc}
   * <p>Editing an object will not update the object on the server. You need to call {@link #editingFinished(GeoJsonRestModelElement)}
   * to commit the changes to the server.</p>
   * @param aElement {@inheritDoc}
   * @param aFireEventMode {@inheritDoc}
   */
  @Override
  public void elementChanged(Object aElement, int aFireEventMode) {
    super.elementChanged(aElement, aFireEventMode);
  }

  private static ExecutorService createSingleThreadExecutorService() {
    ThreadFactory threadFactory = new ThreadFactory() {
      @SuppressWarnings("NullableProblems")
      @Override
      public Thread newThread(Runnable r) {
        Thread thread = new Thread(r, "REST store model updater");
        thread.setDaemon(true);
        return thread;
      }
    };
    return Executors.newSingleThreadExecutor(threadFactory);
  }

  private void addAllElementsFromServerToThisModel() {
    ILcdModel model = getAllElementsFromServer();
    if (model != null) {
      @SuppressWarnings("unchecked") Enumeration<GeoJsonRestModelElement> elements = model.elements();
      while (elements.hasMoreElements()) {
        GeoJsonRestModelElement nextElement = elements.nextElement();
        fIDToObjectMap.put(nextElement.getID(), nextElement);
      }
    }
  }

  private ILcdModel getAllElementsFromServer() {
    HttpGet httpGet = new HttpGet(fRESTServiceURL);
    try {
      HttpResponse response = fHttpClient.execute(httpGet);
      try {
        StatusLine statusLine = response.getStatusLine();
        if (statusLine.getStatusCode() == 200) {
          HttpEntity entity = response.getEntity();
          return fGeoJsonUtil.convertServerResponseToModel(entity.getContent());
        }
      } finally {
        httpGet.releaseConnection();
      }
    } catch (IOException e) {
      LOGGER.error("Error while requesting [" + fRESTServiceURL + "]", e);
    }
    return null;
  }

  private WebSocketConnection createWebSocketConnection() {
    try {
      URI uri = new URI(fWebSocketSource);
      return new WebSocketFactory().connect(uri, new WebSocketListener() {
        @Override
        public void onText(WebSocketConnection aConnection, String aString) {
          if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("[" + System.identityHashCode(AGeoJsonRestModelWithUpdates.this) + "]" + "WebSocket listener received following message: " + aString);
          }
          handleWebSocketInput(aString);
        }

        @Override
        public void onData(WebSocketConnection aConnection, byte[] aPayload) {
          //do nothing
        }

        @Override
        public void onClosed(WebSocketConnection aConnection, int aCode, String aReason) {
          //do nothing
        }
      });
    } catch (URISyntaxException e) {
      LOGGER.error("The source for the WebSocket connection is incorrect [" + fWebSocketSource + "]", e);
    } catch (IOException e) {
      LOGGER.error("The connection could not be established", e);
    }
    return null;
  }

  /**
   * Returns the {@link com.luciad.datamodel.TLcdDataProperty#getName() name} of the server ID property.
   * @return the {@link com.luciad.datamodel.TLcdDataProperty#getName() name} of the server ID property.
   */
  abstract String getServerIDPropertyName();

  /**
   * Returns the {@link com.luciad.datamodel.TLcdDataProperty#getName() name} of the LuciadMobile unique ID property.
   * @return the {@link com.luciad.datamodel.TLcdDataProperty#getName() name} of the LuciadMobile unique ID property.
   */
  abstract String getMobileUniqueIDPropertyName();

  /**
   * Returns the data type of the model elements
   * @return the data type of the model elements
   */
  abstract TLcdDataType getDataType();

  /**
   * Creates a domain object for the shape {@code aShape}
   * @param aShape The shape to create the element for
   * @return A new domain object
   */
  GeoJsonRestModelElement createDomainObjectForShape(ILcdShape aShape) {
    GeoJsonRestModelElement geoJsonRestModelElement = new GeoJsonRestModelElement(getDataType(), getServerIDPropertyName(), getMobileUniqueIDPropertyName());
    geoJsonRestModelElement.setShapes(new ILcdShape[]{aShape});
    return geoJsonRestModelElement;
  }

  private class WebSocketAddOrUpdateRunnable extends EDTRunnable {
    private final ILcdModel fWebSocketMessageModel;

    private WebSocketAddOrUpdateRunnable(ILcdModel aWebSocketMessageModel) {
      fWebSocketMessageModel = aWebSocketMessageModel;
    }

    @Override
    protected void runImpl() {
      try (Lock autoUnlock = writeLock(AGeoJsonRestModelWithUpdates.this)) {
        @SuppressWarnings("unchecked") Enumeration<GeoJsonRestModelElement> elements = fWebSocketMessageModel.elements();
        while (elements.hasMoreElements()) {
          GeoJsonRestModelElement elementToAddOrUpdate = elements.nextElement();
          int id = elementToAddOrUpdate.getID();
          GeoJsonRestModelElement currentElement = fIDToObjectMap.get(id);
          if (currentElement == null) {
            fIDToObjectMap.put(id, elementToAddOrUpdate);
            fModelEventSupport.elementAdded(elementToAddOrUpdate, FIRE_LATER);
          } else {
            updateExistingObject(currentElement, elementToAddOrUpdate);
            fModelEventSupport.elementChanged(currentElement, FIRE_LATER);
          }
        }
      }
      fModelEventSupport.fireCollectedModelChanges();
    }

    private void updateExistingObject(GeoJsonRestModelElement aExistingObject, GeoJsonRestModelElement aNewObject) {
      copyDataObjectValues(aExistingObject, aNewObject);
      aExistingObject.clearShapes();
      copyAllShapes(aExistingObject, aNewObject);
    }

  }

  /**
   * Runnable which will remove elements from the local list of elements, if they
   * are present. The necessary model changes will be fired
   */
  private class WebSocketDeleteRunnable extends EDTRunnable {
    private final List<Integer> fIDsToDelete;

    private WebSocketDeleteRunnable(List<Integer> aIDsToDelete) {
      fIDsToDelete = aIDsToDelete;
    }

    @Override
    protected void runImpl() {
      try (Lock autoUnlock = writeLock(AGeoJsonRestModelWithUpdates.this)) {
        for (Integer id : fIDsToDelete) {
          GeoJsonRestModelElement removedElement = fIDToObjectMap.remove(id);
          if (removedElement != null) {
            fModelEventSupport.elementRemoved(removedElement, FIRE_LATER);
          }
        }
      }
      fModelEventSupport.fireCollectedModelChanges();
    }
  }

  /**
   * Runnable which can indicate to the server an element has been changed. The runnable assumes
   * the element is part of the local list of elements. When the element is different from the one
   * contained in the local list, the runnable assumes the element is already updated and the local
   * updates are thrown away
   */
  private class CommitLocalChangeRunnable implements Runnable {
    private final GeoJsonRestModelElement fElementToUpdate;

    private CommitLocalChangeRunnable(GeoJsonRestModelElement aElementToUpdate) {
      fElementToUpdate = aElementToUpdate;
    }

    @Override
    public void run() {
      try (Lock autoUnlock = readLock(AGeoJsonRestModelWithUpdates.this)) {
        int id = fElementToUpdate.getID();
        if (fElementToUpdate.equals(fIDToObjectMap.get(id))) {
          try {
            HttpPut put = new HttpPut(fRESTServiceURL + fElementToUpdate.getID());
            put.addHeader("Accept", "application/json");
            put.addHeader("Content-Type", "application/json");
            String encoded = fGeoJsonUtil.encodeSingleDomainObject(fElementToUpdate);

            StringEntity entity = new StringEntity(encoded);
            entity.setContentType("application/json");
            put.setEntity(entity);

            if (LOGGER.isTraceEnabled()) {
              LOGGER.trace("[" + System.identityHashCode(AGeoJsonRestModelWithUpdates.this) + "]" + "Sending to server using PUT (change): " + encoded);
            }

            HttpResponse response = fHttpClient.execute(put);
            try {
              int statusCode = response.getStatusLine().getStatusCode();
              if (statusCode != 200) {
                LOGGER.error("Object " + fElementToUpdate + " was incorrectly updated. Server returned status [" + statusCode + "]");
              }
            } finally {
              put.releaseConnection();
            }
          } catch (IOException e) {
            LOGGER.error("Error while updating element [" + fElementToUpdate + "]", e);
          }
        }
      }
    }
  }

  /**
   * <p>Runnable which can remove an element from the server.</p>
   *
   * <p>If the element is still contained
   * in the local list of elements, it will be removed from the local list as well. This scenario is possible
   * when selecting an object on the map and deleting it. Deleting will first deselect the object, which
   * triggers the {@link #editingFinished(GeoJsonRestModelElement)} method. This will send an update
   * event to the server, which we later receive through the WebSocket (which results in re-adding the
   * element).</p>
   */
  private class CommitLocalRemoveRunnable implements Runnable {
    private final GeoJsonRestModelElement fElement;

    private CommitLocalRemoveRunnable(GeoJsonRestModelElement aElement) {
      fElement = aElement;
    }

    @Override
    public void run() {
      TLcdAWTUtil.invokeLater(new Runnable() {
        @Override
        public void run() {
          GeoJsonRestModelElement currentElementWithSameID = null;
          try (Lock autoUnlock = writeLock(AGeoJsonRestModelWithUpdates.this)) {
            currentElementWithSameID = fIDToObjectMap.get(fElement.getID());
            if (currentElementWithSameID != null) {
              //the element has been re-added through the websocket, get rid of it
              fIDToObjectMap.remove(fElement.getID());
            }
          }
          if (currentElementWithSameID != null) {
            fModelEventSupport.elementRemoved(currentElementWithSameID, FIRE_NOW);
          }
        }
      });
      try (Lock autoUnlock = readLock(AGeoJsonRestModelWithUpdates.this)) {
        String deleteQuery = fRESTServiceURL + fElement.getID();
        HttpDelete delete = new HttpDelete(deleteQuery);
        if (LOGGER.isTraceEnabled()) {
          LOGGER.trace("[" + System.identityHashCode(AGeoJsonRestModelWithUpdates.this) + "]" + "Sending to server using DELETE (remove): " + deleteQuery);
        }
        try {
          fHttpClient.execute(delete);
        } catch (IOException e) {
          LOGGER.error("Exception while removing an element from the REST model", e);
        } finally {
          delete.releaseConnection();
        }
      }
    }
  }

  /**
   * Runnable which can add an element to the server. Once the server has assigned an id to the
   * element, it will be added to the local list of elements and a model change event will be send.
   */
  private class CommitLocalAddRunnable implements Runnable {
    private final GeoJsonRestModelElement fElement;

    private CommitLocalAddRunnable(GeoJsonRestModelElement aElement) {
      fElement = aElement;
    }

    @Override
    public void run() {
      try {
        HttpPost post = new HttpPost(fRESTServiceURL);
        post.addHeader("Accept", "application/json");
        post.addHeader("Content-Type", "application/json");
        String encoded = fGeoJsonUtil.encodeSingleDomainObject(fElement);
        StringEntity entity = new StringEntity(encoded);
        entity.setContentType("application/json");
        post.setEntity(entity);
        if (LOGGER.isTraceEnabled()) {
          LOGGER.trace("[" + System.identityHashCode(AGeoJsonRestModelWithUpdates.this) + "]" + "Sending to server using POST (add): " + encoded);
        }
        HttpResponse response = fHttpClient.execute(post);

        try {
          if (response.getStatusLine().getStatusCode() == 200) {
            ILcdModel model = fGeoJsonUtil.convertServerResponseToModel(response.getEntity().getContent());
            final int id = ((GeoJsonRestModelElement) model.elements().nextElement()).getID();
            TLcdAWTUtil.invokeLater(new Runnable() {
              @Override
              public void run() {
                try (Lock autoUnlock = writeLock(AGeoJsonRestModelWithUpdates.this)) {
                  fElement.setValue(getServerIDPropertyName(), id);
                  fIDToObjectMap.put(id, fElement);
                }
                fModelEventSupport.elementAdded(fElement, FIRE_NOW);
              }
            });
          }
        } finally {
          post.releaseConnection();
        }
      } catch (IOException e) {
        LOGGER.error("Error while adding element [" + fElement + "]", e);
      }
    }
  }

  private abstract static class EDTRunnable implements Runnable {
    @Override
    public void run() {
      TLcdAWTUtil.invokeLater(new Runnable() {
        @Override
        public void run() {
          runImpl();
        }
      });
    }

    protected abstract void runImpl();
  }
}
