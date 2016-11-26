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
package samples.wms.client;

import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.NoSuchElementException;

import com.luciad.datamodel.TLcdDataModel;
import com.luciad.model.ILcdModel;
import com.luciad.shape.ILcdBounds;
import com.luciad.util.ILcdExceptionHandler;
import com.luciad.util.logging.ILcdLogger;
import com.luciad.util.logging.TLcdLoggerFactory;
import com.luciad.view.ILcdXYWorldReference;
import com.luciad.wms.client.model.*;
import com.luciad.wms.sld.model.TLcdSLDStyledLayerDescriptor;

/**
 * Extension of <code>ALcdWMSProxy</code> that delegates to an ALcdWMSProxy
 * that represents the active WMS configuration.
 * <p/>
 * This extension offers functionality to monitor the WMS server for any updates.
 * If {@link #isAutoUpdateEnabled} returns true, the WMS server is contacted upon each invocation of
 * {@link #createImageInputStream(TLcdWMSGetMapContext)},
 * {@link #createImage(int, int, ILcdXYWorldReference, ILcdBounds, Color)} or
 * {@link ALcdWMSProxy#createFeatureInfoInputStream(TLcdWMSGetFeatureInfoContext, TLcdWMSGetFeatureInfoParameters)},
 * to check whether new capabilities have been installed. If there are new capabilities,
 * the most recent layer configuration is retrieved and configured on this proxy object.
 * Otherwise, the WMS server is not monitored and the initial registered layer configuration
 * on the proxy object remains unchanged.
 */
@SuppressWarnings("deprecation")
public class ProxyWrapper extends ALcdWMSProxy {

  private static ILcdLogger sLogger = TLcdLoggerFactory.getLogger(ProxyWrapper.class.getName());

  private ALcdWMSProxy fDelegateProxy;
  private ILcdModel fProxyModel;
  private TLcdOGCWMSProxyModelDecoder fProxyModelDecoder;
  private boolean fAutoUpdateEnabled = false;

  /**
   * Constructs a new <code>ProxyWrapper</code> with the given <code>ALcdWMSProxy</code> object.
   *
   * @param aDelegateProxy the <code>ALcdWMSProxy</code> to which must be delegated.
   * @param aProxyModel the model that contains this proxy.
   * @param aSLDDataModels any SLD data model extensions that should be taken into account by this WMS proxy.
   */
  public ProxyWrapper(ALcdWMSProxy aDelegateProxy, ILcdModel aProxyModel, TLcdDataModel[] aSLDDataModels) {
    fDelegateProxy = aDelegateProxy;
    fProxyModel = aProxyModel;
    fProxyModelDecoder = new TLcdOGCWMSProxyModelDecoder(aSLDDataModels);
    fProxyModelDecoder.setValidating(false);
  }

  @SuppressWarnings("deprecation")
  @Override
  public InputStream createImageInputStream(int aWidth, int aHeight, ILcdXYWorldReference aXYWorldReference,
                                            ILcdBounds aBounds, Color aBackgroundColor) throws IOException {
    TLcdWMSGetMapContext getMapContext = TLcdWMSGetMapContext.newBuilder()
                                                             .imageSize(aWidth, aHeight)
                                                             .mapReference(aXYWorldReference)
                                                             .mapBounds(aBounds)
                                                             .backgroundColor(aBackgroundColor)
                                                             .build();
    return createImageInputStream(getMapContext);
  }

  @Override
  public InputStream createImageInputStream(TLcdWMSGetMapContext aGetMapContext) throws IOException {
    if (fAutoUpdateEnabled) {
      update();
    }
    try {
      return fDelegateProxy.createImageInputStream(aGetMapContext);
    } catch (TLcdOGCWMSServiceException e) {
      // Perform an update to check for changes in the WMS capabilities.
      update();
      return fDelegateProxy.createImageInputStream(aGetMapContext);
    }
  }

  @SuppressWarnings("deprecation")
  @Override
  public InputStream createFeatureInfoInputStream(int aWidth, int aHeight, ILcdXYWorldReference aXYWorldReference,
                                                  ILcdBounds aBounds, Color aBackgroundColor, int aX, int aY, int aFeatureCount) throws IOException {
    TLcdWMSGetMapContext getMapContext = TLcdWMSGetMapContext.newBuilder()
                                                             .imageSize(aWidth, aHeight)
                                                             .mapReference(aXYWorldReference)
                                                             .mapBounds(aBounds)
                                                             .backgroundColor(aBackgroundColor)
                                                             .build();
    TLcdWMSGetFeatureInfoContext getFeatureInfoContext = TLcdWMSGetFeatureInfoContext.newBuilder()
                                                                                     .queryCoordinate(aX, aY)
                                                                                     .getMapContext(getMapContext)
                                                                                     .build();
    TLcdWMSGetFeatureInfoParameters parameters = TLcdWMSGetFeatureInfoParameters.newBuilder().maxFeatureCount(aFeatureCount).build();
    return createFeatureInfoInputStream(getFeatureInfoContext, parameters);
  }

  @Override
  public InputStream createFeatureInfoInputStream(TLcdWMSGetFeatureInfoContext aGetFeatureInfoContext, TLcdWMSGetFeatureInfoParameters aGetFeatureInfoParameters) throws IOException {
    if (fAutoUpdateEnabled) {
      update();
    }
    try {
      return fDelegateProxy.createFeatureInfoInputStream(aGetFeatureInfoContext, aGetFeatureInfoParameters);
    } catch (IOException e) {
      // Perform an update to check for changes in the WMS capabilities.
      update();
      return fDelegateProxy.createFeatureInfoInputStream(aGetFeatureInfoContext, aGetFeatureInfoParameters);
    }
  }

  /**
   * This method checks whether the capabilities at the server have been updated since the most
   * recently downloaded capabilities. This check is carried out by using the Update Sequence
   * parameter in the capabilities of a LuciadLightspeed WMS Server. If the capabilities have been updated,
   * the changes are applied to the proxy object.
   */
  private synchronized void update() {
    // Check whether the capabilities have been updated.
    ILcdModel proxyModel;
    try {
      proxyModel = fProxyModelDecoder.decode(getWMSCapabilities().getServerURL().toString());
    } catch (IOException e) {
      sLogger.error("Could not update proxy object - server connection failure: " + e.getMessage());
      return;
    }

    ALcdWMSProxy proxy = (ALcdWMSProxy) proxyModel.elements().nextElement();
    ALcdOGCWMSCapabilities capabilities = proxy.getWMSCapabilities();

    // Use the update sequence feature to check whether the capabilities have been updated at the server.
    if (!capabilities.getUpdateSequence().equals(getWMSCapabilities().getUpdateSequence())) {
      ProxyModelFactory.initializeWebMapServerProxy(proxy);

      ALcdWMSProxy oldDelegateProxy = fDelegateProxy;
      fDelegateProxy = proxy;
      fProxyModel.elementChanged(oldDelegateProxy, ILcdModel.FIRE_NOW);
    }
  }

  /**
   * Sets whether this ProxyWrapper should monitor the WMS server for updates.
   *
   * @param aAutoUpdateEnabled whether this ProxyWrapper should monitor the WMS server for updates.
   *
   * @see #isAutoUpdateEnabled
   */
  public void setAutoUpdateEnabled(boolean aAutoUpdateEnabled) {
    this.fAutoUpdateEnabled = aAutoUpdateEnabled;
  }

  /**
   * Returns whether this ProxyWrapper is monitoring the WMS server for updates.
   *
   * @return true if this ProxyWrapper is monitoring the WMS server for updates.
   *
   * @see #setAutoUpdateEnabled
   */
  public boolean isAutoUpdateEnabled() {
    return fAutoUpdateEnabled;
  }

  @Override
  public String getMapFormat() {
    return fDelegateProxy.getMapFormat();
  }

  @Override
  public void setMapFormat(String aMapFormat) {
    fDelegateProxy.setMapFormat(aMapFormat);
  }

  @Override
  public String getFeatureInfoFormat() {
    return fDelegateProxy.getFeatureInfoFormat();
  }

  @Override
  public void setFeatureInfoFormat(String aFeatureInfoFormat) {
    fDelegateProxy.setFeatureInfoFormat(aFeatureInfoFormat);
  }

  @Override
  public String getExceptionFormat() {
    return fDelegateProxy.getExceptionFormat();
  }

  @Override
  public void setExceptionFormat(String aExceptionFormat) {
    fDelegateProxy.setExceptionFormat(aExceptionFormat);
  }

  @Override
  public int getSupportedMapFormatCount() {
    return fDelegateProxy.getSupportedMapFormatCount();
  }

  @Override
  public String getSupportedMapFormat(int aIndex) throws IndexOutOfBoundsException {
    return fDelegateProxy.getSupportedMapFormat(aIndex);
  }

  @Override
  public int getWMSRootNamedLayerCount() {
    return fDelegateProxy.getWMSRootNamedLayerCount();
  }

  @Override
  public ALcdWMSNamedLayer getWMSRootNamedLayer(int aIndex) throws IndexOutOfBoundsException {
    return fDelegateProxy.getWMSRootNamedLayer(aIndex);
  }

  @Override
  public void addStyledNamedLayer(TLcdWMSStyledNamedLayerWrapper aStyledNamedLayer) {
    fDelegateProxy.addStyledNamedLayer(aStyledNamedLayer);
  }

  @Override
  public int getStyledNamedLayerCount() {
    return fDelegateProxy.getStyledNamedLayerCount();
  }

  @Override
  public TLcdWMSStyledNamedLayerWrapper getStyledNamedLayer(int aIndex) throws IndexOutOfBoundsException {
    return fDelegateProxy.getStyledNamedLayer(aIndex);
  }

  @Override
  public void moveStyledNamedLayer(TLcdWMSStyledNamedLayerWrapper aStyledNamedLayer, int aToIndex) throws IndexOutOfBoundsException, NoSuchElementException {
    fDelegateProxy.moveStyledNamedLayer(aStyledNamedLayer, aToIndex);
  }

  @Override
  public void removeStyledNamedLayer(int aIndex) throws IndexOutOfBoundsException {
    fDelegateProxy.removeStyledNamedLayer(aIndex);
  }

  @Override
  public void clearStyledNamedLayers() {
    fDelegateProxy.clearStyledNamedLayers();
  }

  @Override
  public void addDimension(TLcdOGCWMSDimensionWrapper aDimension) {
    fDelegateProxy.addDimension(aDimension);
  }

  @Override
  public int getDimensionCount() {
    return fDelegateProxy.getDimensionCount();
  }

  @Override
  public TLcdOGCWMSDimensionWrapper getDimension(int aIndex) throws IndexOutOfBoundsException {
    return fDelegateProxy.getDimension(aIndex);
  }

  @Override
  public void removeDimension(int aIndex) {
    fDelegateProxy.removeDimension(aIndex);
  }

  @Override
  public void clearDimensions() {
    fDelegateProxy.clearDimensions();
  }

  @Override
  public void setBackgroundImageTransparent(boolean aTransparent) {
    fDelegateProxy.setBackgroundImageTransparent(aTransparent);
  }

  @Override
  public boolean isBackgroundImageTransparent() {
    return fDelegateProxy.isBackgroundImageTransparent();
  }

  @Override
  public void setPixelSize(double aPixelSize) {
    fDelegateProxy.setPixelSize(aPixelSize);
  }

  @Override
  public double getPixelSize() {
    return fDelegateProxy.getPixelSize();
  }

  @Override
  public void putAdditionalParameter(String aParameterName, String aParameterValue) throws IllegalArgumentException {
    fDelegateProxy.putAdditionalParameter(aParameterName, aParameterValue);
  }

  @Override
  public void removeAdditionalParameter(String aParameterName) {
    fDelegateProxy.removeAdditionalParameter(aParameterName);
  }

  @Override
  public boolean isSupportedXYworldReference(ILcdXYWorldReference aXYWorldReference, ALcdWMSNamedLayer aWMSNamedLayer) {
    return fDelegateProxy.isSupportedXYworldReference(aXYWorldReference, aWMSNamedLayer);
  }

  @Override
  public ALcdOGCWMSCapabilities getWMSCapabilities() {
    return fDelegateProxy.getWMSCapabilities();
  }

  @Override
  public ILcdBounds getBounds() {
    return fDelegateProxy.getBounds();
  }

  @Override
  public boolean isCacheImage() {
    return fDelegateProxy.isCacheImage();
  }

  @Override
  public void setCacheImage(boolean aCacheImage) {
    fDelegateProxy.setCacheImage(aCacheImage);
  }

  @Override
  public TLcdSLDStyledLayerDescriptor getStyledLayerDescriptor() {
    return fDelegateProxy.getStyledLayerDescriptor();
  }

  @Override
  public URL getStyledLayerDescriptorURL() {
    return fDelegateProxy.getStyledLayerDescriptorURL();
  }

  @Override
  public void setStyledLayerDescriptor(TLcdSLDStyledLayerDescriptor aSLD) {
    fDelegateProxy.setStyledLayerDescriptor(aSLD);
  }

  @Override
  public void setStyledLayerDescriptorURL(URL aURL) {
    fDelegateProxy.setStyledLayerDescriptorURL(aURL);
  }

  @Override
  public void insertIntoCache(Object aKey, Object aObject) {
    fDelegateProxy.insertIntoCache(aKey, aObject);
  }

  @Override
  public Object getCachedObject(Object aKey) {
    return fDelegateProxy.getCachedObject(aKey);
  }

  @Override
  public Object removeCachedObject(Object aKey) {
    return fDelegateProxy.removeCachedObject(aKey);
  }

  @Override
  public void clearCache() {
    fDelegateProxy.clearCache();
  }

  /**
   * Creates and returns a clone this object.
   * The delegate ALcdWMSProxy object is also cloned.
   */
  @Override
  public Object clone() {
    ProxyWrapper clone = (ProxyWrapper) super.clone();
    clone.fDelegateProxy = (ALcdWMSProxy) this.fDelegateProxy.clone();
    return clone;
  }

  @Override
  public ILcdExceptionHandler getExceptionHandler() {
    return fDelegateProxy.getExceptionHandler();
  }

  @Override
  public void setExceptionHandler(ILcdExceptionHandler aExceptionHandler) {
    fDelegateProxy.setExceptionHandler(aExceptionHandler);
  }

  @Override
  public boolean isInvertXYForEPSG4326() {
    return fDelegateProxy.isInvertXYForEPSG4326();
  }

  @Override
  public void setInvertXYForEPSG4326(boolean aInvertXYForEPSG4326) {
    fDelegateProxy.setInvertXYForEPSG4326(aInvertXYForEPSG4326);
  }

  @Override
  public String getDescription() {
    return fDelegateProxy.getDescription();
  }
}
