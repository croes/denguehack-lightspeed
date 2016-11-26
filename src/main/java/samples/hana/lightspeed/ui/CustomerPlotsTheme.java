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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.LinearGradientPaint;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.luciad.view.lightspeed.ILspView;

import samples.common.OptionsPanelBuilder;
import samples.hana.lightspeed.MainPanel;
import samples.hana.lightspeed.common.HangingTitlePanel;
import samples.hana.lightspeed.common.ThemeComponent;
import samples.hana.lightspeed.common.UIUtil;
import samples.hana.lightspeed.domain.CustomerCategory;
import samples.hana.lightspeed.domain.InsuranceCompany;
import samples.hana.lightspeed.model.HanaConnectionExecutorService;
import samples.hana.lightspeed.styling.CustomerPlotStyler;

public class CustomerPlotsTheme extends ThemeComponent.Theme {

  public static final NumberFormat sDollarFormat = new DecimalFormat("###,###,###");

  private static final ThemeComponent.POI REHOBOTH_BEACH = new ThemeComponent.POI(
      -077.09298462, 38.86886370, 0.11547169, 0.12069968, "Go to North-East of Washington DC");

  private final MainPanel fMainPanel;
  private final int fMaxPolicyValue;

  public CustomerPlotsTheme(MainPanel aMainPanel, int aMaxPolicyValue, HanaConnectionExecutorService aExecutorService) {
    super("Policy holder filtering", "samples/hana/lightspeed/insurance/customerplots.html", REHOBOTH_BEACH);
    fMainPanel = aMainPanel;
    fMaxPolicyValue = aMaxPolicyValue;
    if (aExecutorService != null) {
      aExecutorService.addBusyListener(getListener());
    }
  }

  @Override
  public ILspView getView() {
    return fMainPanel.getView();
  }

  @Override
  public Component getGui() {
    final JSlider policyValueSlider = new JSlider() {
      @Override
      public Dimension getPreferredSize() {
        Dimension pref = super.getPreferredSize();
        pref.width = 220; //more less as wide as the other components
        return pref;
      }

      @Override
      public void paint(Graphics g) {
        ((Graphics2D) g).setPaint(new LinearGradientPaint(0, 0, getWidth(), getHeight(), new float[]{0, .5f, 1}, new Color[]{Color.green, Color.orange, Color.red}));
        g.fillRect((int) (getWidth() * 0.1), 0,
                   (int) (getWidth() * 0.8), 5);
        super.paint(g);
      }
    };
    int maxPV = fMaxPolicyValue;
    policyValueSlider.setPaintLabels(true);
    Hashtable<Integer, JLabel> standardLabels = new Hashtable<Integer, JLabel>();
    standardLabels.put(0, new JLabel("$0"));
    standardLabels.put(maxPV, new JLabel("$" + sDollarFormat.format(maxPV)));
    policyValueSlider.setLabelTable(standardLabels);
    policyValueSlider.setModel(new DefaultBoundedRangeModel(0, 1, 0, maxPV));
    policyValueSlider.addChangeListener(new ChangeListener() {
      @Override
      public void stateChanged(ChangeEvent e) {
        fMainPanel.getThemeManager().setMinPolicyValue(policyValueSlider.getValue());
      }
    });

    List<Action> categoryActions = new ArrayList<Action>();
    for (CustomerCategory c : CustomerCategory.values()) {
      categoryActions.add(new CategoryAction(c));
    }
    JPanel categories = createTypeFilterPanel(categoryActions, new AbstractAction("Show all") {
      @Override
      public void actionPerformed(ActionEvent e) {
        fMainPanel.getThemeManager().setCustomerStyling(null);
      }
    }, 1, 6);

    List<Action> companies = new ArrayList<Action>();
    for (InsuranceCompany c : InsuranceCompany.values()) {
      companies.add(new CompanyAction(c));
    }
    JPanel companiesContent = createTypeFilterPanel(companies, new AbstractAction("Show all") {
      @Override
      public void actionPerformed(ActionEvent e) {
        fMainPanel.getThemeManager().setInsuranceFiltering(null);
      }
    }, 2, 3);

    JPanel content = new JPanel();
    content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
    content.setOpaque(false);
    content.add(HangingTitlePanel.create("Minimum policy value", UIUtil.wrapWithSpaceEater(policyValueSlider)));
    content.add(HangingTitlePanel.create("Category", categories));
    content.add(HangingTitlePanel.create("Insurance company", companiesContent));
    return content;
  }

  private class CategoryAction extends AbstractAction {
    private CustomerCategory fCategory;

    public CategoryAction(CustomerCategory aCategory) {
      super(aCategory.toString(), CustomerPlotStyler.getSwingIcon(aCategory));
      fCategory = aCategory;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      fMainPanel.getThemeManager().setCustomerStyling(fCategory);
    }
  }

  private class CompanyAction extends AbstractAction {
    private InsuranceCompany fCompany;

    public CompanyAction(InsuranceCompany aCompany) {
      super(aCompany.toString(), CustomerPlotStyler.getSwingIcon(aCompany));
      fCompany = aCompany;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      fMainPanel.getThemeManager().setInsuranceFiltering(fCompany);
    }
  }

  private JPanel createTypeFilterPanel(List<Action> aActions, final Action aReset, int aRows, int aCols) {
    JPanel buttonPanel = new JPanel(new GridLayout(aRows, aCols, 5, 5));
    buttonPanel.setOpaque(false);

    ButtonGroup g = new ButtonGroup();
    for (final Action action : aActions) {
      AbstractButton b = createButton(action);
      b.setAction(action);
      g.add(b);
      buttonPanel.add(b);
    }

    final AbstractButton reset = OptionsPanelBuilder.createUnderlinedButton((String) aReset.getValue(Action.NAME));
    reset.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        ((AbstractButton) e.getSource()).setSelected(true);
        aReset.actionPerformed(e);
      }
    });

    g.add(reset);
    reset.setSelected(true);

    JPanel content = new JPanel();
    content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
    content.setOpaque(false);
    content.add(UIUtil.wrapWithSpaceEater(buttonPanel));
    content.add(UIUtil.wrapWithSpaceEater(reset));
    return content;
  }

  private AbstractButton createButton(Action aAction) {
    AbstractButton b = UIUtil.createUnderlinedButton(aAction);
    b.setToolTipText(b.getText());
    b.setText("");
    return b;
  }

  @Override
  public void activate() {
    fMainPanel.getThemeManager().activatePlotsTheme();
    UIUtil.fit(getView(), REHOBOTH_BEACH);
  }
}
