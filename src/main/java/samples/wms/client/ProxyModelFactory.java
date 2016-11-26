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

import java.awt.Component;
import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.AccessControlException;

import javax.swing.JDialog;
import javax.swing.JOptionPane;

import com.luciad.datamodel.TLcdDataModel;
import com.luciad.model.ILcdModel;
import com.luciad.util.ILcdFireEventMode;
import com.luciad.util.concurrent.TLcdLockUtil;
import com.luciad.wms.client.model.ALcdWMSNamedLayer;
import com.luciad.wms.client.model.ALcdWMSProxy;
import com.luciad.wms.client.model.TLcdOGCWMSProxyModelDecoder;
import com.luciad.wms.client.model.TLcdWMSProxyModelDescriptor;
import com.luciad.wms.client.model.TLcdWMSStyledNamedLayerWrapper;

import samples.gxy.common.ProgressUtil;

public class ProxyModelFactory {

  private static final int MAX_INITIAL_LAYER_COUNT = 8;

  private ProxyModelFactory() {
  }

  public static ILcdModel createWMSModel(String aURL, Component aGUI, boolean aSupportAutoUpdate) throws IOException {
    return createWMSModel(aURL, aGUI, null, aSupportAutoUpdate, true);
  }

  public static ILcdModel createWMSModel(String aURL, Component aGUI, TLcdDataModel[] aSLDDataModels,
                                         boolean aSupportAutoUpdate, boolean aHandleErrors) throws IOException {

    if (isCorrectServiceURL(aGUI, aURL, aHandleErrors)) {
      JDialog progress = ProgressUtil.createProgressDialog(aGUI, "Loading data from Web Map Service...");
      ProgressUtil.showDialog(progress);

      try {
        // Decode a proxy object (within a model).
        TLcdOGCWMSProxyModelDecoder wmsModelDecoder = new TLcdOGCWMSProxyModelDecoder(aSLDDataModels);
        ILcdModel wmsModel = wmsModelDecoder.decode(aURL);

        // Initialize proxy model.
        ProxyModelFactory.initializeWebMapServerProxy(wmsModel, aSLDDataModels, aSupportAutoUpdate);

        ProgressUtil.hideDialog(progress);
        return wmsModel;
      } catch (IOException ioex) {
        ProgressUtil.hideDialog(progress);
        String[] message = new String[]{
            "An exception occurred while connecting to the WMS:",
            ioex.getMessage(),
            "Please check if the WMS is up and running."
        };
        JOptionPane.showMessageDialog(aGUI, message);
      }
    }
    return null;
  }

  /**
   * Initializes the ALcdWMSProxy object in the given model.
   *
   * @param aWMSModel An ILcdModel containing a ALcdWMSProxy object.
   */
  public static void initializeWebMapServerProxy(ILcdModel aWMSModel, TLcdDataModel[] aSLDDataModels, boolean aSupportAutoUpdate) {
    if (aWMSModel.getModelDescriptor() instanceof TLcdWMSProxyModelDescriptor) {
      // A WMS proxy model contains 1 object that is an instance of ALcdWMSProxy.
      ALcdWMSProxy wmsProxy = (ALcdWMSProxy) aWMSModel.elements().nextElement();

      initializeWebMapServerProxy(wmsProxy);

      // We wrap the decoded proxy object inside a ProxyWrapper object,
      // to be able to monitor the WMS for any updates, if necessary.
      // We also supply the WMS model, so that ProxyWrapper can fire model changes
      // when updates are detected and applied.
      ProxyWrapper proxyWrapper = new ProxyWrapper(wmsProxy, aWMSModel, aSLDDataModels);
      proxyWrapper.setAutoUpdateEnabled(aSupportAutoUpdate);

      TLcdLockUtil.writeLock(aWMSModel);
      try {
        aWMSModel.removeElement(wmsProxy, ILcdFireEventMode.FIRE_LATER);
        aWMSModel.addElement(proxyWrapper, ILcdFireEventMode.FIRE_LATER);
        aWMSModel.fireCollectedModelChanges();
      } finally {
        TLcdLockUtil.writeUnlock(aWMSModel);
      }
    }
  }

  public static void initializeWebMapServerProxy(ALcdWMSProxy aWMSProxy) {
    // Set a GetFeatureInfo format.
    // Note that this is not explicitly needed for the GetMap format,
    // because image/jpeg is already set by default.
    aWMSProxy.setFeatureInfoFormat("text/plain");

    aWMSProxy.setBackgroundImageTransparent(true);
    aWMSProxy.setMapFormat("image/png");

    // OGC WMS always has a single root layer.
    ALcdWMSNamedLayer rootLayer = aWMSProxy.getWMSRootNamedLayer(0);

    // We add a number of layers by default. If no limit is defined by the capabilities or if the limit
    // is larger than MAX_INITIAL_LAYER_COUNT, we use MAX_INITIAL_LAYER_COUNT as default.
    int advertisedMaxLayerCount = aWMSProxy.getWMSCapabilities().getWMSService().getLayerLimit();
    int maxLayerCount;
    if (advertisedMaxLayerCount != -1 && advertisedMaxLayerCount < MAX_INITIAL_LAYER_COUNT) {
      maxLayerCount = advertisedMaxLayerCount;
    } else {
      maxLayerCount = MAX_INITIAL_LAYER_COUNT;
    }

    addLayers(aWMSProxy, rootLayer, maxLayerCount);
  }

  public static void addLayers(ALcdWMSProxy aWMSProxy,
                               ALcdWMSNamedLayer aNamedLayer) {
    addLayers(aWMSProxy, aNamedLayer, Integer.MAX_VALUE);
  }

  public static void addLayers(ALcdWMSProxy aWMSProxy,
                               ALcdWMSNamedLayer aNamedLayer,
                               int aMaxLayerCount) {
    if (aNamedLayer.getNamedLayerName() != null) {
      aWMSProxy.addStyledNamedLayer(new TLcdWMSStyledNamedLayerWrapper(aNamedLayer));
    } else {
      for (int i = 0; i < aNamedLayer.getChildWMSNamedLayerCount() && aWMSProxy.getStyledNamedLayerCount() < aMaxLayerCount; i++) {
        ALcdWMSNamedLayer childLayer = aNamedLayer.getChildWMSNamedLayer(i);
        addLayers(aWMSProxy, childLayer, aMaxLayerCount);
      }
    }
  }

  private static boolean isCorrectServiceURL(Component aGUI, String aServiceURL, boolean aHandleErrors) throws IOException {
    URL testUrl = null;

    String message = null;
    Exception exception = null;

    if (aServiceURL == null) {
      message = "No URL filled in.";
    } else {
      try {
        new URL(aServiceURL);
        if (aServiceURL.endsWith("?")) {
          testUrl = new URL(aServiceURL + "REQUEST=GetCapabilities&SERVICE=WMS");
        } else {
          testUrl = new URL(aServiceURL + "?REQUEST=GetCapabilities&SERVICE=WMS");
        }
      } catch (MalformedURLException e) {
        message = "The specified URL is malformed.";
        message += "\n  " + aServiceURL;
        exception = e;
      }
      try {
        if (testUrl != null) {
          InputStream testStream = testUrl.openStream();
          testStream.close();
        }
      } catch (IOException ioe) {
        exception = ioe;
        if (ioe instanceof ConnectException) {
          message = "A problem occurred while connecting to the server. \nCheck whether the server is up and running. \nIf positive, the problem may be due to an incorrect URL.";
        } else if (ioe instanceof UnknownHostException) {
          message = "The WMS URL is incorrect, the host is unknown.";
          message += "\n" + aServiceURL;
        }
      } catch (AccessControlException ace) {
        exception = ace;
        message = "A security exception occurred while connecting to the server. \n" +
                  "Adapt your Java security settings to allow the connection \n" +
                  "Typically, this requires to add the line \n" +
                  "permission java.net.SocketPermission \"localhost:8080\", \"connect,resolve\"; \n" +
                  "to the permissions listed in the file lib/security/java.policy of your JRE.";
      } catch (SecurityException se) {
        exception = se;
        message = "A security exception occurred while connecting to the server.\n Adapt your security settings to allow the connection.";
      }
    }
    if (message == null) {
      return true;
    } else {
      if (aHandleErrors) {
        message = message + "\nNothing will be displayed on the map.";
        if (exception != null) {
          message = message + "\nCheck the console for more details.";
          System.out.println("An exception occurred while connecting with the specified WMS URL. Stacktrace:");
          exception.printStackTrace(System.out);
        }
        JOptionPane.showMessageDialog(aGUI, message, "Error connecting to the WMS", JOptionPane.ERROR_MESSAGE);
        return false;
      } else {
        throw new IOException(exception);
      }
    }
  }
}
