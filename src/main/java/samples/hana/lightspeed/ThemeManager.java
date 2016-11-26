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

import java.awt.Container;
import java.awt.EventQueue;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

import com.luciad.datamodel.ILcdDataObject;
import com.luciad.format.hana.TLcdHanaModelDescriptor;
import com.luciad.gui.swing.TLcdOverlayLayout;
import com.luciad.model.ILcdModel;
import com.luciad.util.TLcdPair;
import com.luciad.util.logging.ILcdLogger;
import com.luciad.util.logging.TLcdLoggerFactory;
import com.luciad.view.lightspeed.ILspAWTView;

import samples.hana.lightspeed.common.Configuration;
import samples.hana.lightspeed.common.HanaConnectionParameters;
import samples.hana.lightspeed.common.ThemeComponent;
import samples.hana.lightspeed.domain.CustomerCategory;
import samples.hana.lightspeed.domain.InsuranceCompany;
import samples.hana.lightspeed.model.BoundsIndexedHanaModel;
import samples.hana.lightspeed.model.HanaConnectionExecutorService;
import samples.hana.lightspeed.model.StormsFilter;
import samples.hana.lightspeed.model.StormsModel;
import samples.hana.lightspeed.statistics.StatisticsProvider;
import samples.hana.lightspeed.styling.CustomerPlotStyler;
import samples.hana.lightspeed.ui.CustomerDensityTheme;
import samples.hana.lightspeed.ui.CustomerPlotsTheme;
import samples.hana.lightspeed.ui.InsuranceCompanyTheme;
import samples.hana.lightspeed.ui.StormTimeComponent;

public class ThemeManager {

  private static final ILcdLogger sLogger = TLcdLoggerFactory.getLogger(ThemeManager.class);

  private final MainPanel fMainPanel;
  private final StormsFilter fStormsFilter;

  private CustomerPlotStyler fCustomerStyler;
  private StatisticsProvider fStatisticsProvider;

  public ThemeManager(MainPanel aMainPanel, StormsFilter aStormsFilter) {
    fMainPanel = aMainPanel;
    fStormsFilter = aStormsFilter;
  }

  public void initialize(BoundsIndexedHanaModel aCustomersModel, final StormsModel aStormsModel) {
    ILspAWTView view = fMainPanel.getView();
    Container overlay = view.getOverlayComponent();

    if (aStormsModel != null) {
      // Create the storms component and add it to the view
      final StormTimeComponent stormTimeComponent = new StormTimeComponent();
      stormTimeComponent.addPropertyChangeListener("time", new PropertyChangeListener() {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
          setTime(aStormsModel, (Long) evt.getNewValue());
        }
      });
      overlay.add(stormTimeComponent, TLcdOverlayLayout.Location.SOUTH);
      ((TLcdOverlayLayout) overlay.getLayout()).putConstraint(stormTimeComponent, TLcdOverlayLayout.Location.SOUTH, TLcdOverlayLayout.ResolveClash.VERTICAL);

      // Make sure the time range and active storms are updated
      EventQueue.invokeLater(new Runnable() {
        @Override
        public void run() {
          updateTimeRange(aStormsModel, stormTimeComponent);
        }
      });

      fMainPanel.getLayersManager().addStormsLayers(aStormsModel);
      fMainPanel.getLayersManager().setVisible("storms");
    }

    if (aCustomersModel != null) {
      final HanaConnectionExecutorService statisticsExecutorService = new HanaConnectionExecutorService("Statistics",
                                                                                                        HanaConnectionParameters.getInstance(),
                                                                                                        Configuration.getInt("database.connections.statistics"));
      int maxPolicyValue = retrieveMaxPolicyValue(aCustomersModel, statisticsExecutorService);
      fCustomerStyler = new CustomerPlotStyler(maxPolicyValue);
      fStatisticsProvider = new StatisticsProvider(statisticsExecutorService);

      fMainPanel.getLayersManager().addCustomersLayers(aCustomersModel, fCustomerStyler, fStatisticsProvider);

      ArrayList<ThemeComponent.Theme> themes = new ArrayList<ThemeComponent.Theme>();
      if (Configuration.is("layer.states.visible") || Configuration.is("layer.counties.visible")) {
        themes.add(new InsuranceCompanyTheme(fMainPanel, fStatisticsProvider));
      }
      if (Configuration.is("layer.customer.visible")) {
        themes.add(new CustomerDensityTheme(fMainPanel, aCustomersModel.getHanaConnectionExecutorService()));
        themes.add(new CustomerPlotsTheme(fMainPanel, maxPolicyValue, aCustomersModel.getHanaConnectionExecutorService()));
      }
      ThemeComponent.Theme[] themesArray = themes.toArray(new ThemeComponent.Theme[themes.size()]);

      ThemeComponent themeComponent = ThemeComponent.create(themesArray);

      overlay.add(themeComponent, TLcdOverlayLayout.Location.NORTH_EAST);
      ((TLcdOverlayLayout) overlay.getLayout()).putConstraint(themeComponent, TLcdOverlayLayout.Location.NORTH_EAST, TLcdOverlayLayout.ResolveClash.VERTICAL);

      themeComponent.moveTheme(0);
    }
  }

  private void updateTimeRange(StormsModel aStormsModel, StormTimeComponent aStormTimeComponent) {
    final TLcdPair<Long, Long> timeRange = aStormsModel.getActiveTimeRange();
    long range = timeRange.getValue() - timeRange.getKey();
    aStormTimeComponent.setTimeRange(timeRange.getKey() - (range / 10), timeRange.getValue() + (range / 10));
    setTime(aStormsModel, timeRange.getKey() + (range * 4) / 5);
  }

  public void setMinPolicyValue(int aMinPolicyValue) {
    if (isCustomersDataAvailable()) {
      fCustomerStyler.setMinPolicyValue(aMinPolicyValue);
    }
  }

  public void activateInsuranceTheme() {
    fMainPanel.getLayersManager().setVisible("states", "counties");
  }

  public void activateDensityTheme() {
    fMainPanel.getLayersManager().setVisible("customer", "cities");
    if (isCustomersDataAvailable()) {
      fCustomerStyler.setDensity(true);
    }
  }

  public void activatePlotsTheme() {
    fMainPanel.getLayersManager().setVisible("customer", "cities");
    if (isCustomersDataAvailable()) {
      fCustomerStyler.setDensity(false);
    }
  }

  public void setCustomerStyling(CustomerCategory aCustomerCategory) {
    if (isCustomersDataAvailable()) {
      fCustomerStyler.setCustomerCategory(aCustomerCategory);
    }
  }

  public void setInsuranceFiltering(InsuranceCompany aInsuranceCompany) {
    if (isCustomersDataAvailable()) {
      fCustomerStyler.setInsuranceCompany(aInsuranceCompany);
    }
  }

  private boolean isCustomersDataAvailable() {
    return fCustomerStyler != null;
  }

  private void setTime(StormsModel aStormsModel, long aTime) {
    Collection<ILcdDataObject> activeStorms = aStormsModel.getActiveStorms(aTime);
    fStormsFilter.setActiveStorms(activeStorms);
    if (isCustomersDataAvailable()) {
      fStatisticsProvider.setActiveStorms(activeStorms);
    }
  }

  private static int retrieveMaxPolicyValue(ILcdModel aCustomersModel, HanaConnectionExecutorService aExecutorService) {
    TLcdHanaModelDescriptor modelDescriptor = (TLcdHanaModelDescriptor) aCustomersModel.getModelDescriptor();
    final int[] maxPolicyValue = {1};
    aExecutorService.submitQueryAndWait("select max(POLICY_VALUE) from " + modelDescriptor.getTableName(), new HanaConnectionExecutorService.ResultHandler() {
      @Override
      public void handleRow(ResultSet aResultSet) throws IOException, SQLException {
        maxPolicyValue[0] = aResultSet.getInt(1);
        sLogger.info("Max policy value is " + maxPolicyValue[0]);
      }
    });
    return maxPolicyValue[0];
  }
}
