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
package samples.lightspeed.demo.application.gui.menu;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.ScrollPaneConstants;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.FormLayout;
import com.luciad.gui.ILcdUndoableListener;
import com.luciad.gui.TLcdUndoableEvent;
import com.luciad.view.lightspeed.ILspView;

import samples.common.HaloLabel;
import samples.common.OptionsPanelScrollPane;
import samples.lightspeed.demo.application.data.milsym.MilSymFilter;
import samples.lightspeed.demo.framework.application.Framework;
import samples.lightspeed.demo.framework.data.themes.AbstractTheme;
import samples.lightspeed.demo.framework.gui.DemoUIColors;
import samples.symbology.common.BattleDimension;
import samples.symbology.common.util.SymbologyFavorites;
import samples.symbology.lightspeed.LspFavoritesToolbar;
import samples.symbology.lightspeed.LspSymbologyCreationBar;

public class MilSymPanelFactory implements IThemePanelFactory {

  private final Map<ILspView, MilSymFilter> fMilSymFilters;
  private final boolean fIsTouchEnabled;
  private final SymbologyFavorites fFavorites = new SymbologyFavorites();
  private LspFavoritesToolbar fFavoritesToolbar;

  public MilSymPanelFactory(Map<ILspView, MilSymFilter> aMilSymFilter) {
    fMilSymFilters = aMilSymFilter;
    fIsTouchEnabled = Boolean.parseBoolean(Framework.getInstance().getProperty("controllers.touch.enabled", "false"));
    fFavorites.load();
  }

  public void destroy() {
    fFavorites.save();
  }

  public SymbologyFavorites getFavorites() {
    return fFavorites;
  }

  @Override
  public List<JPanel> createThemePanels(AbstractTheme aTheme) {
    List<ILspView> views = Framework.getInstance().getFrameworkContext().getViews();
    fFavoritesToolbar = new LspFavoritesToolbar(views, new UndoableListener(), fFavorites);
    ArrayList<JPanel> panels = new ArrayList<JPanel>();
    panels.add(createVisibilityPanel());
    return panels;
  }

  public JPanel createSymbolCreationPanel() {
    DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout("p, 5dlu, p, 5dlu, p"));
    builder.border(Borders.DIALOG);

    builder.append(new HaloLabel("Symbol", 15, true), 3);
    builder.nextLine();

    builder.append(new HaloLabel("Search", 12, true), 1);
    List<ILspView> views = Framework.getInstance().getFrameworkContext().getViews();
    LspSymbologyCreationBar bar = new LspSymbologyCreationBar(views, new UndoableListener());
    builder.append(bar, 1);
    builder.nextLine();

    builder.append(new HaloLabel("Favorites", 12, true), 1);
    JScrollPane scrollPane = new OptionsPanelScrollPane(fFavoritesToolbar.getToolBar());
    scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
    scrollPane.setBorder(null);
    scrollPane.setOpaque(false);
    scrollPane.getViewport().setOpaque(false);
    scrollPane.setViewportBorder(BorderFactory.createEmptyBorder());
    builder.append(scrollPane, 3);
    builder.nextLine();

    JPanel contentPanel = builder.getPanel();
    contentPanel.setSize(contentPanel.getLayout().preferredLayoutSize(contentPanel));
    contentPanel.setCursor(Cursor.getDefaultCursor());
    contentPanel.setOpaque(false);
    return contentPanel;
  }

  public JPanel createTitledPanel(AbstractButton aButton, String title) {
    DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout("p"));
    builder.border(Borders.DIALOG);

    builder.append(new HaloLabel(title, 15, true));
    builder.nextLine();

    JToolBar toolBar = new JToolBar();
    toolBar.add(aButton);

    toolBar.setFloatable(false);
    toolBar.setBorderPainted(false);
    toolBar.setOpaque(false);

    builder.append(toolBar);

    JPanel contentPanel = builder.getPanel();
    contentPanel.setSize(contentPanel.getLayout().preferredLayoutSize(contentPanel));
    contentPanel.setCursor(Cursor.getDefaultCursor());
    contentPanel.setOpaque(false);
    return contentPanel;
  }

  private JPanel createVisibilityPanel() {
    DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout("p, 5dlu, p, 5dlu, p"));
    builder.border(Borders.DIALOG);

    builder.append(new HaloLabel("Battle Dimensions", 15, true), 3);
    builder.nextLine();

    boolean nextLine = false;
    for (BattleDimension bd : BattleDimension.values()) {
      JToggleButton checkBox;
      if (fIsTouchEnabled) {
        checkBox = new JToggleButton();
      } else {
        checkBox = new JCheckBox();
      }
      checkBox.setSelected(true);
      for (MilSymFilter filter : fMilSymFilters.values()) {
        checkBox.addItemListener(filter);
      }
      checkBox.putClientProperty(BattleDimension.class, bd);
      if (fIsTouchEnabled) {
        checkBox.setText(bd.toString());
        builder.append(checkBox);
      } else {
        combineAndAdd(builder, checkBox, new HaloLabel(bd.toString()));
      }
      if (nextLine) {
        builder.nextLine();
      }
      nextLine = !nextLine;
    }

    JPanel contentPanel = builder.getPanel();
    contentPanel.setSize(contentPanel.getLayout().preferredLayoutSize(contentPanel));
    contentPanel.setCursor(Cursor.getDefaultCursor());
    return contentPanel;
  }

  private static void combineAndAdd(DefaultFormBuilder aBuilder, Component aComponent1, Component aComponent2) {
    JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
    panel.setBackground(DemoUIColors.TRANSPARENT);
    panel.add(aComponent1);
    panel.add(aComponent2);
    aBuilder.append(panel);
  }

  private static class UndoableListener implements ILcdUndoableListener {
    @Override
    public void undoableHappened(TLcdUndoableEvent aEvent) {
    }
  }
}
