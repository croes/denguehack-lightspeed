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
package samples.lucy.cop.addons.cop;

import java.awt.Component;
import java.util.concurrent.ExecutionException;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import com.luciad.lucy.ILcyLucyEnv;
import com.luciad.lucy.ILcyLucyEnvListener;
import com.luciad.lucy.TLcyLucyEnvEvent;
import com.luciad.lucy.addons.ALcyPreferencesAddOn;
import com.luciad.lucy.gui.balloon.TLcyCompositeBalloonContentProvider;
import com.luciad.lucy.util.ALcyTool;
import com.luciad.lucy.util.TLcyVetoException;

import samples.lucy.cop.PathResolver;
import samples.lucy.theme.ThemeManager;

/**
 * <p>Main add-on of the COP sample:</p>
 *
 * <ul>
 *
 *   <li>It registers the {@link ThemeManager} on the Lucy back-end, which allows to switch between
 *   all the registered themes</li>
 *
 *   <li>It registers the {@link PathResolver} on the Lucy back-end, allowing to convert the relative
 *   paths to the server to absolute paths</li>
 *
 *   <li>It registers an {@link com.luciad.view.swing.ILcdBalloonContentProvider ILcdBalloonContentProvider}
 *   which shows the object properties in a balloon.</li>
 *
 * </ul>
 *
 */
public class CopAddOn extends ALcyPreferencesAddOn {

  private static final String THEMES_PREFIX = "themes.";
  private static final String WEB_SERVER_LOCATION_PROPERTY = "webServerLocation";
  private static final String WEB_SOCKET_SERVER_LOCATION_PROPERTY = "webSocketServerLocation";

  private ThemeManager fThemeManager;

  public CopAddOn() {
    super(ALcyTool.getLongPrefix(CopAddOn.class), ALcyTool.getShortPrefix(CopAddOn.class));
  }

  @Override
  public void plugInto(ILcyLucyEnv aLucyEnv) {
    super.plugInto(aLucyEnv);

    fThemeManager = new ThemeManager(aLucyEnv);
    aLucyEnv.addService(fThemeManager);

    PathResolver pathResolver = new PathResolver(getPreferences().getString(getShortPrefix() + WEB_SERVER_LOCATION_PROPERTY, "http://localhost:8072"),
                                                 getPreferences().getString(getShortPrefix() + WEB_SOCKET_SERVER_LOCATION_PROPERTY, "ws://localhost:8072"));
    aLucyEnv.addService(pathResolver);

    final ThemeSelectionApplicationPaneTool themeSelectionApplicationPaneTool = new ThemeSelectionApplicationPaneTool(getPreferences(), getLongPrefix() + THEMES_PREFIX, getShortPrefix() + THEMES_PREFIX);
    themeSelectionApplicationPaneTool.plugInto(getLucyEnv());
    aLucyEnv.addLucyEnvListener(new ILcyLucyEnvListener() {
      @Override
      public void lucyEnvStatusChanged(TLcyLucyEnvEvent aEvent) throws TLcyVetoException {
        if (aEvent.getID() == TLcyLucyEnvEvent.INITIALIZED) {
          aEvent.getLucyEnv().removeLucyEnvListener(this);
          themeSelectionApplicationPaneTool.setApplicationPaneActive(true);
        }
      }
    });

    new TLcyCompositeBalloonContentProvider(aLucyEnv).addBalloonContentProvider(new ObjectPropertiesBalloonContentProvider(aLucyEnv));
    informUserWhenServerIsNotRunning(aLucyEnv);
  }

  @Override
  public void unplugFrom(ILcyLucyEnv aLucyEnv) {
    super.unplugFrom(aLucyEnv);
    if (fThemeManager != null) {
      fThemeManager.setActiveTheme(null);
      aLucyEnv.removeService(fThemeManager);
    }
  }

  private void informUserWhenServerIsNotRunning(ILcyLucyEnv aLucyEnv) {
    aLucyEnv.addLucyEnvListener(new ILcyLucyEnvListener() {
      @Override
      public void lucyEnvStatusChanged(TLcyLucyEnvEvent aEvent) throws TLcyVetoException {
        if (aEvent.getID() == TLcyLucyEnvEvent.INITIALIZED) {
          new ServerConnectionCheck(getPreferences().getString(getShortPrefix() + WEB_SERVER_LOCATION_PROPERTY, "http://localhost:8072"),
                                    aEvent.getLucyEnv().getTopLevelComponent(0))
              .execute();
          aEvent.getLucyEnv().removeLucyEnvListener(this);
        }
      }
    });
  }

  /**
   * Use a SwingWorker extension to check whether the server is running. The attempt to connect
   * to the server should happen on a background thread, while showing the message to the user
   * when the connection is not available should happen on the EDT.
   */
  private static class ServerConnectionCheck extends SwingWorker<Boolean, Void> {
    private final String fServerAddress;
    private final Component fParentComponent;

    private ServerConnectionCheck(String aServerAddress, Component aParentComponent) {
      fServerAddress = aServerAddress;
      fParentComponent = aParentComponent;
    }

    @Override
    protected Boolean doInBackground() throws Exception {
      HttpGet get = new HttpGet(fServerAddress);
      try {
        HttpResponse response = new DefaultHttpClient().execute(get);
        StatusLine statusLine = response.getStatusLine();
        //verify whether status code indicates success (2xx) code
        return statusLine != null && statusLine.getStatusCode() >= 200 && statusLine.getStatusCode() < 300;
      } catch (Throwable e) {
        //ignore
      } finally {
        get.releaseConnection();
      }
      return Boolean.FALSE;
    }

    @Override
    protected void done() {
      try {
        Boolean connectionSuccessful = get();
        if (!connectionSuccessful) {
          JOptionPane.showMessageDialog(fParentComponent,
                                        "The sample server is not running.\nPlease start it by running the startCOPServer script in the distrib/copservices folder of your release "
                                        + "and restart the Lucy COP sample afterwards.",
                                        "Sample server is not running",
                                        JOptionPane.ERROR_MESSAGE);
        }
      } catch (InterruptedException e) {
        //ignore
      } catch (ExecutionException e) {
        //ignore
      }
    }
  }

}
