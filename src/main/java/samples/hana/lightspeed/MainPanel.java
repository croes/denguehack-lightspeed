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
package samples.hana.lightspeed;

import java.awt.Component;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.io.IOException;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import com.luciad.gui.swing.TLcdOverlayLayout;
import com.luciad.reference.TLcdGeodeticReference;
import com.luciad.shape.ILcdBounds;
import com.luciad.shape.shape2D.TLcdLonLatBounds;
import com.luciad.util.TLcdSystemPropertiesUtil;
import com.luciad.view.lightspeed.ILspAWTView;
import com.luciad.view.lightspeed.swing.navigationcontrols.TLspNavigationControlsBuilder;

import samples.common.DefaultExceptionHandler;
import samples.common.MacUtil;
import samples.hana.lightspeed.common.HanaConnectionParameters;
import samples.hana.lightspeed.model.BoundsIndexedHanaModel;
import samples.hana.lightspeed.model.CustomersModelFactory;
import samples.hana.lightspeed.model.StormsFilter;
import samples.hana.lightspeed.model.StormsModel;
import samples.hana.lightspeed.model.StormsModelFactory;
import samples.lightspeed.common.FitUtil;
import samples.lightspeed.common.FullScreenAction;
import samples.lightspeed.common.LightspeedSample;

/**
 * This sample is an extensive demonstration of how to visualize data in an SAP HANA database.
 * <p/>
 * It uses two datasets in the database, uploading them if necessary:
 * <ul>
 *    <li>STORMS: shapes representing windspeeds at a certain time - based on the Sandy hurricane</li>
 *    <li>CUSTOMERS: points representing insurance company policy holders - semi-random</li>
 * </ul>
 * <p/>
 * The user can use the time slider to see the extent and impact of the storm at a certain time.
 * <ul>
 *   <li>See how many policy holders are affected, per US state and county</li>
 *   <li>See a heat map of the customers</li>
 *   <li>See individual customers as icons, and filter on them</li>
 * </ul>
 * <p/>
 * Code pointers:
 * <ul>
 *   <li>Package {@link samples.hana.lightspeed.domain} contains some classes for domain object modeling.</li>
 *   <li>Package {@link samples.hana.lightspeed.model} contains various model implementations and utilities.</li>
 *   <li>Package {@link samples.hana.lightspeed.statistics} contains functionality to query affected policy holders from the database.</li>
 *   <li>Package {@link samples.hana.lightspeed.styling} contains layer stylers to visualize domain objects</li>
 *   <li>Package {@link samples.hana.lightspeed.ui} contains UI elements for user interaction.</li>
 * </ul>
 *
 * @since 2014.0
 */
public class MainPanel extends LightspeedSample {

  private final String[] fArgs;

  private LayersManager fLayersManager;
  private ThemeManager fThemeManager;

  public MainPanel(String[] aArgs) {
    super();
    fArgs = aArgs;
  }

  @Override
  protected void createGUI() {
    // Trigger the connection dialog if needed
    HanaConnectionParameters.getInstance();
    super.createGUI();
    StormsFilter stormsFilter = new StormsFilter();
    fThemeManager = new ThemeManager(this, stormsFilter);
    fLayersManager = new LayersManager(getView(), stormsFilter);
    if (!isWindowed(fArgs)) {
      if (!TLcdSystemPropertiesUtil.isMacOS()) {
        SwingUtilities.invokeLater(new Runnable() {
          @Override
          public void run() {
            new FullScreenAction(getView()).actionPerformed(new ActionEvent(this, 0, ""));
          }
        });
      }
    }
  }

  public LayersManager getLayersManager() {
    return fLayersManager;
  }

  public ThemeManager getThemeManager() {
    return fThemeManager;
  }

  @Override
  protected void addNavigationControls(JComponent aOverlayPanel) {
    TLcdOverlayLayout layout = (TLcdOverlayLayout) aOverlayPanel.getLayout();
    Component navigationControls = TLspNavigationControlsBuilder.newBuilder(getView()).build();
    aOverlayPanel.add(navigationControls);
    layout.putConstraint(navigationControls, TLcdOverlayLayout.Location.NORTH_WEST, TLcdOverlayLayout.ResolveClash.VERTICAL);
  }

  @Override
  protected void addData() throws IOException {
    fLayersManager.addBackgroundLayers();

    final ILcdBounds bounds = new TLcdLonLatBounds(-130, 10, 70, 40);
    FitUtil.fitOnBounds(this, bounds, new TLcdGeodeticReference());

    Thread thread = new Thread(new Runnable() {
      @Override
      public void run() {
        // It is possible that data needs to be uploaded when creating hana models. This can take some time, so we
        // load these models on a background thread and initialize the themes afterwards.
        final BoundsIndexedHanaModel customerModel = new CustomersModelFactory(MainPanel.this).createModel();
        final StormsModel stormsModel = new StormsModelFactory(MainPanel.this).createModel();

        EventQueue.invokeLater(new Runnable() {
          @Override
          public void run() {
            fThemeManager.initialize(customerModel, stormsModel);
          }
        });
      }
    }, "Load-hana-data-thread");
    thread.setUncaughtExceptionHandler(new DefaultExceptionHandler());
    thread.start();
  }

  @Override
  protected Component[] createToolBars(ILspAWTView aView) {
    return null;
  }

  public static void main(final String[] aArgs) {
    useBlackLime();
    startSample(MainPanel.class, aArgs, "Hana Sample");
  }

  private static boolean isWindowed(String[] aArgs) {
    boolean windowed = false;
    for (String arg : aArgs) {
      if ("-windowed".equalsIgnoreCase(arg)) {
        windowed = true;
      }
    }
    return windowed;
  }
}
