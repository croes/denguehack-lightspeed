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
package samples.lucy.tableview;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.Locale;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.OverlayLayout;
import javax.swing.SwingConstants;

import org.jdesktop.swingx.JXFindBar;
import org.jdesktop.swingx.JXFindPanel;
import org.jdesktop.swingx.search.SearchFactory;
import org.jdesktop.swingx.search.Searchable;

import com.luciad.gui.ALcdAction;
import com.luciad.gui.ILcdAction;
import com.luciad.gui.TLcdIconFactory;
import com.luciad.gui.swing.TLcdSWIcon;
import com.luciad.lucy.gui.ILcyActionBar;
import com.luciad.lucy.gui.ILcyCustomizableRepresentationAction;
import com.luciad.lucy.gui.ILcyToolBar;
import com.luciad.lucy.gui.TLcyAlwaysFitJToolBar;
import com.luciad.lucy.gui.TLcyGroupDescriptor;
import com.luciad.lucy.util.language.TLcyLang;

/**
 * <p>Action to find an entry in a table.</p>
 *
 * <p>The action implements {@link ILcyCustomizableRepresentationAction} as well. When inserted in a
 * toolbar, it will show an inline version of the find panel. Otherwise, the action will show a
 * dialog containing the search component.</p>
 */
class FindInTableAction extends ALcdAction implements ILcyCustomizableRepresentationAction {

  private CustomFindPanel fFindPanel;
  private CustomFindBar fFindBar;
  private JTable fTable;
  private Searchable fSearchable;

  public FindInTableAction(JTable aTable, Searchable aSearchable) {
    super(TLcyLang.getString("Find in table"), TLcdIconFactory.create(TLcdIconFactory.SEARCH_ICON));
    fTable = aTable;
    fSearchable = aSearchable;
    setShortDescription(getName());
    fFindPanel = new CustomFindPanel(aSearchable);
    fFindBar = new CustomFindBar(aSearchable);
  }

  @Override
  public Component customizeRepresentation(Component aDefaultComponent,
                                           ILcdAction aWrapperAction,
                                           ILcyActionBar aActionBar) {
    //custom representation for tool bars
    if (aActionBar instanceof ILcyToolBar) {
      // Add spacer, so that the find bar is at the far right.
      // It would make more sense to return a component that automatically scales between pref size
      // and max size, eating up the available width. But Nimbus Look&Feel doesn't handle that well.
      ((ILcyToolBar) aActionBar).insertComponent(Box.createHorizontalGlue(), new TLcyGroupDescriptor("FindGroup"));
      return TLcyAlwaysFitJToolBar.createToolBarPanel(fFindBar);
    }
    //use the default representation for all other action bars
    return aDefaultComponent;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    if (!(fFindBar.isVisible())) {
      //retrieve the parent frame to show a pop-up menu
      SearchFactory searchFactory = new SearchFactory() {
        @Override
        public JXFindPanel getSharedFindPanel() {
          return fFindPanel;
        }
      };
      searchFactory.showFindInput(fTable, fSearchable);
    } else {
      //the find bar is visible, just request focus in the text field and select all text
      fFindBar.getTextField().requestFocus();
      fFindBar.getTextField().select(0, Integer.MAX_VALUE);
    }
  }

  /**
   * An extension of JXFindPanel which uses TLcyLang.getString for the translations.
   */
  private static class CustomFindPanel extends JXFindPanel {

    private CustomFindPanel(Searchable aSearchable) {
      super(aSearchable);
    }

    @Override
    protected String getUIString(String key) {
      return TLcyLang.getString(super.getUIString(key));
    }

    @Override
    protected String getUIString(String key, Locale locale) {
      return TLcyLang.getString(super.getUIString(key, locale));
    }
  }

  /**
   * An extension of JXFindBar which uses TLcyLang.getString for the translations and which replaces
   * the Next and Previous buttons by icons, and the Find Label by an icon.
   */
  private static class CustomFindBar extends JXFindBar {
    private CustomFindBar(Searchable aSearchable) {
      super(aSearchable);
      setOpaque(false);
    }

    @Override
    public Dimension getMinimumSize() {
      //A tool bar with Nimbus L&F messes up a combination of glue's and components having different
      //min/pref sizes, make min size identical to pref size for that reason.
      return getPreferredSize();
    }

    @Override
    protected String getUIString(String key) {
      return TLcyLang.getString(super.getUIString(key));
    }

    @Override
    protected String getUIString(String key, Locale locale) {
      return TLcyLang.getString(super.getUIString(key, locale));
    }

    @Override
    protected void build() {
      setLayout(new FlowLayout(SwingConstants.LEADING));

      // Overlay the search label on the search text field
      JLayeredPane lp = new JLayeredPane();
      lp.setLayout(new OverlayLayout(lp));
      lp.add(searchField, new Integer(0));
      lp.add(searchLabel, new Integer(1));
      add(lp);

      // Add the previous and next buttons to their own mini tool bar
      final JToolBar tb = new JToolBar();
      tb.setOpaque(false);
      tb.setFloatable(false);
      tb.setBorderPainted(false);
      tb.add(findPrevious);
      tb.add(findNext);
      add(tb);
    }

    @Override
    protected void bind() {
      super.bind();
      //actions will be set on those buttons. However, we like a custom representation so adjust
      //the icon and text
      findNext.setIcon(new TLcdSWIcon(TLcdIconFactory.create(TLcdIconFactory.MOVE_DOWN_ICON)));
      findNext.setToolTipText(TLcyLang.getString("Find next"));
      findNext.setText(null);
      findPrevious.setIcon(new TLcdSWIcon(TLcdIconFactory.create(TLcdIconFactory.MOVE_UP_ICON)));
      findPrevious.setText(null);
      findPrevious.setIconTextGap(0);
      findPrevious.setToolTipText(TLcyLang.getString("Find previous"));

      // Assign the typical short cut keys to the search field
      final String previous = "previous";
      final String next = "next";
      searchField.getInputMap().put(KeyStroke.getKeyStroke("shift ENTER"), previous);
      searchField.getInputMap().put(KeyStroke.getKeyStroke("shift F3"), previous);
      searchField.getInputMap().put(KeyStroke.getKeyStroke("ENTER"), next);
      searchField.getInputMap().put(KeyStroke.getKeyStroke("F3"), next);
      searchField.getActionMap().put(previous, findPrevious.getAction());
      searchField.getActionMap().put(next, findNext.getAction());
    }

    @Override
    protected void initComponents() {
      super.initComponents();

      searchLabel = new JLabel("  " + TLcyLang.getString("Find"));
      searchLabel.setForeground(Color.GRAY);
      searchLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

      // Only show the find label when the search field is empty and not focused
      searchField.addFocusListener(new FocusListener() {
        @Override
        public void focusGained(FocusEvent e) {
          searchLabel.setVisible(false);
        }

        @Override
        public void focusLost(FocusEvent e) {
          String text = searchField.getText();
          searchLabel.setVisible(text == null || text.isEmpty());
        }
      });
      searchLabel.setAlignmentX(searchField.getAlignmentX()); //use identical alignment because they are overlayed

      //make sure the buttons look like square buttons. The default margins are larger left/right then
      //at the top/bottom
      findPrevious.setMargin(new Insets(2, 2, 2, 2));
      findNext.setMargin(new Insets(2, 2, 2, 2));

      //make sure wrap search is on, as well as case-insensitive search
      wrapCheck.setSelected(true);
      matchCheck.setSelected(false);
    }

    @Override
    protected void bindSearchLabel(Locale locale) {
      //do nothing with the search label, we already customized it representation
    }

    public JTextField getTextField() {
      //expose the text field
      return searchField;
    }
  }
}
