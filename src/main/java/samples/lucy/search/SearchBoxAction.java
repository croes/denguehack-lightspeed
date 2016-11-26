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
package samples.lucy.search;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.lang.ref.WeakReference;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;

import com.luciad.gui.ALcdAction;
import com.luciad.gui.ILcdAction;
import com.luciad.gui.TLcdAWTUtil;
import samples.lucy.search.ui.SearchFieldFactory;
import com.luciad.lucy.gui.ILcyActionBar;
import com.luciad.lucy.gui.ILcyCustomizableRepresentationAction;
import com.luciad.lucy.gui.ILcyToolBar;
import com.luciad.lucy.gui.TLcyAlwaysFitJToolBar;
import com.luciad.lucy.util.language.TLcyLang;

/**
 * <p>
 *   The search action.
 *   This action implements {@link ILcyCustomizableRepresentationAction} to show a search input field when the
 *   action is inserted in an {@code ILcyToolBar}.
 * </p>
 *
 * @since 2016.0
 */
final class SearchBoxAction extends ALcdAction implements ILcyCustomizableRepresentationAction {
  private final SearchManager fSearchManager;
  private WeakReference<JComponent> fSearchFieldOfCustomRepresentation = new WeakReference<>(null);

  SearchBoxAction(SearchManager aSearchManager) {
    fSearchManager = aSearchManager;
  }

  @Override
  public Component customizeRepresentation(Component aDefaultComponent, ILcdAction aWrapperAction, ILcyActionBar aActionBar) {
    if (aActionBar instanceof ILcyToolBar) {
      JComponent searchField = createSearchField();
      fSearchFieldOfCustomRepresentation = new WeakReference<>(searchField);
      return TLcyAlwaysFitJToolBar.createToolBarPanel(searchField);
    }
    return aDefaultComponent;
  }

  private JComponent createSearchField() {
    return SearchFieldFactory.createSearchField(fSearchManager, "");
  }

  @Override
  public void actionPerformed(ActionEvent aEvent) {
    JComponent searchField = fSearchFieldOfCustomRepresentation.get();
    if (searchField != null) {
      // If the search box is created via customizeRepresentation, and thus added directly to the UI, simply request
      // focus to the search box. This for example happens when the acceleratorKey is pressed (e.g. ctrl F).
      searchField.requestFocusInWindow();
    } else {
      Frame parent = TLcdAWTUtil.findParentFrame(aEvent);
      JDialog dialog = new JDialog(parent, TLcyLang.getString("Search..."), true);

      JPanel panel = new JPanel(new BorderLayout());
      panel.add(createSearchField(), BorderLayout.CENTER);

      dialog.setContentPane(panel);
      dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

      dialog.pack();
      dialog.setLocationRelativeTo(parent);
      dialog.setVisible(true);
    }
  }
}
