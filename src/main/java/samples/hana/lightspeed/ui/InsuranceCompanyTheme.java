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
package samples.hana.lightspeed.ui;

import static samples.hana.lightspeed.domain.AdministrativeLevel.getAdministrativeLevel;
import static samples.hana.lightspeed.domain.AdministrativeLevel.getAdministrativeLevelKey;
import static samples.hana.lightspeed.domain.WindSpeed.*;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import com.luciad.datamodel.ILcdDataObject;
import com.luciad.gui.ILcdIcon;
import com.luciad.gui.TLcdAWTUtil;
import com.luciad.gui.TLcdIconFactory;
import com.luciad.gui.swing.TLcdSWIcon;
import com.luciad.util.ILcdChangeListener;
import com.luciad.util.ILcdSelectionListener;
import com.luciad.util.TLcdChangeEvent;
import com.luciad.util.TLcdSelectionChangedEvent;
import com.luciad.view.lightspeed.ILspView;

import samples.common.HaloLabel;
import samples.hana.lightspeed.MainPanel;
import samples.hana.lightspeed.common.ColorPalette;
import samples.hana.lightspeed.common.FontStyle;
import samples.hana.lightspeed.common.HangingTitlePanel;
import samples.hana.lightspeed.common.ThemeComponent;
import samples.hana.lightspeed.domain.AdministrativeLevel;
import samples.hana.lightspeed.domain.InsuranceCompany;
import samples.hana.lightspeed.statistics.Statistics;
import samples.hana.lightspeed.statistics.StatisticsProvider;
import samples.hana.lightspeed.statistics.StormStatistics;

public class InsuranceCompanyTheme extends ThemeComponent.Theme implements ILcdChangeListener, ILcdSelectionListener,
                                                                           Runnable {

  private static final ThemeComponent.POI SUSSEX = new ThemeComponent.POI(
      -076.36423475, 38.03982542, 002.19154837, 001.14366958, "Go to Sussex, Delaware");

  private final MainPanel fMainPanel;
  private final StatisticsProvider fStatisticsProvider;
  private JPanel fPanel = new JPanel(new BorderLayout(0, 15));
  private Map<InsuranceCompany, List<Double>> fDataset = null;
  private long fTotal;
  private long fAffected;
  private long fTotalPolicyValue;
  private ILcdDataObject fSelectedObject;

  public InsuranceCompanyTheme(MainPanel aMainPanel, StatisticsProvider aStatisticsProvider) {
    super("Affected by storm", "samples/hana/lightspeed/insurance/insurancecompany.html", SUSSEX);
    fMainPanel = aMainPanel;
    fMainPanel.getView().addLayerSelectionListener(this);
    fStatisticsProvider = aStatisticsProvider;
    fStatisticsProvider.addChangeListener(this);
    fPanel.setOpaque(false);
    resetDataSet();
  }

  @Override
  public ILspView getView() {
    return fMainPanel.getView();
  }

  @Override
  public Component getGui() {
    return fPanel;
  }

  private void resetDataSet() {
    fPanel.removeAll();

    JComponent chartContent;
    if (fDataset != null) {
      JComponent chartPanel = StackedBarChartComponent.createChart(fDataset);
      chartPanel.setOpaque(false);
      chartPanel.setBackground(ColorPalette.transparent);

      chartContent = new JPanel(new BorderLayout());
      chartContent.setOpaque(false);
      chartContent.add(FontStyle.createHaloLabel("Policy holders per insurance company:", FontStyle.NORMAL), BorderLayout.NORTH);
      chartContent.add(chartPanel, BorderLayout.CENTER);
      HaloLabel chartCountLabel = FontStyle.createHaloLabel(getChartCountSummary(), FontStyle.NORMAL);
      HaloLabel chartValueLabel = FontStyle.createHaloLabel(getChartValueSummary(), FontStyle.NORMAL);
      JPanel chartSummary = new JPanel(new BorderLayout());
      chartSummary.add(chartCountLabel, BorderLayout.NORTH);
      chartSummary.add(chartValueLabel, BorderLayout.SOUTH);
      chartSummary.setOpaque(false);
      chartContent.add(chartSummary, BorderLayout.SOUTH);
    } else {
      ILcdIcon busy = TLcdIconFactory.create(TLcdIconFactory.BUSY_ANIMATED_ICON);
      JLabel loading = new JLabel("Loading", new TLcdSWIcon(busy), SwingConstants.CENTER);
      loading.setVerticalAlignment(SwingConstants.CENTER);
      loading.setHorizontalTextPosition(SwingConstants.CENTER);
      loading.setVerticalTextPosition(SwingConstants.BOTTOM);
      chartContent = loading;
    }
    chartContent.setPreferredSize(new Dimension(280, 300));

    fPanel.add(WindSpeedLegendComponent.create(), BorderLayout.NORTH);
    fPanel.add(HangingTitlePanel.create(getChartTitle(), chartContent), BorderLayout.CENTER);
    fPanel.revalidate();
  }

  protected String getChartTitle() {
    String name = "United States";
    if (fSelectedObject != null) {
      AdministrativeLevel level = AdministrativeLevel.getAdministrativeLevel(fSelectedObject);
      if (level != AdministrativeLevel.country) {
        name = AdministrativeLevel.getName(fSelectedObject);
      }
    }
    return name;
  }

  protected String getChartCountSummary() {
    return String.format(Locale.ENGLISH, "Total affected holders: %,d (%.0f%%)",
                         fAffected, (float) fAffected / fTotal * 100);
  }

  protected String getChartValueSummary() {
    return String.format(Locale.ENGLISH, "Total affected value: $%,d", fTotalPolicyValue);
  }

  @Override
  public void activate() {
    fMainPanel.getThemeManager().activateInsuranceTheme();
  }

  @Override
  public void selectionChanged(TLcdSelectionChangedEvent aTLcdSelectionChangedEvent) {
    fSelectedObject = null;

    if (aTLcdSelectionChangedEvent.getSelection().getSelectionCount() > 0) {
      Enumeration selectedObjects = aTLcdSelectionChangedEvent.getSelection().selectedObjects();
      while (selectedObjects.hasMoreElements()) {
        Object selected = selectedObjects.nextElement();
        if (selected instanceof ILcdDataObject) {
          try {
            getAdministrativeLevel((ILcdDataObject) selected);
            getAdministrativeLevelKey((ILcdDataObject) selected);
            fSelectedObject = (ILcdDataObject) selected;
          } catch (Exception e) {
            // selected object not supported for charts
          }
        }
      }
    }

    stateChanged(null);
  }

  @Override
  public void stateChanged(TLcdChangeEvent aTLcdChangeEvent) {
    TLcdAWTUtil.invokeNowOrLater(this);
  }

  @Override
  public void run() {
    fDataset = createMap();
    resetDataSet();
  }

  protected Map<InsuranceCompany, List<Double>> createMap() {
    AdministrativeLevel level = AdministrativeLevel.country;
    String key = "US";

    if (fSelectedObject != null) {
      level = getAdministrativeLevel(fSelectedObject);
      key = getAdministrativeLevelKey(fSelectedObject);
    }

    Statistics total = fStatisticsProvider.getTotal(level, key);
    StormStatistics affected = fStatisticsProvider.getAffected(level, key);

    fTotal = (total == null) ? -1 : total.getTotalCount();
    fAffected = (affected == null) ? -1 : affected.get(S34).getTotalCount();
    fTotalPolicyValue = (affected == null) ? -1 : affected.get(S34).getTotalValue();

    if (total == null || affected == null) {
      return null;
    } else {
      InsuranceCompany categories[] = total.getCategories();
      Map<InsuranceCompany, List<Double>> map = new LinkedHashMap<InsuranceCompany, List<Double>>();

      for (InsuranceCompany cat : categories) {
        List<Double> mapForCategory = new ArrayList<Double>();
        map.put(cat, mapForCategory);
        double tot = total.getCount(cat);
        double s34 = affected.get(S34).getCount(cat);
        double s50 = affected.get(S50).getCount(cat);
        double s64 = affected.get(S64).getCount(cat);
        double s0 = tot - s34;
        s34 -= s50;
        s50 -= s64;
        mapForCategory.add(s64);
        mapForCategory.add(s50);
        mapForCategory.add(s34);
        mapForCategory.add(s0);
      }
      return map;
    }
  }
}
