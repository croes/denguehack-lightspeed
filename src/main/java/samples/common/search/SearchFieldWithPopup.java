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
package samples.common.search;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JTextField;

import samples.common.TextComponentHint;

/**
 * Search field with a pop-up listing the matching values.
 * Implement {@link #valueSelected} to determine what happens when selecting a value.
 * Call {@link #setSearchableContent(String[])} to initialize the content that can be searched.
 */
public abstract class SearchFieldWithPopup extends JPanel {

  private final FixedListBackend fSearchService;
  private final JTextField fTextField;
  private final TextComponentHint fSearchLabel;
  private final SearchResultPopup fSearchResultPopup;

  protected SearchFieldWithPopup() {
    fTextField = new JTextField("", 20); //20 columns per default
    fSearchService = new FixedListBackend() {
      @Override
      protected void valueSelected(String aValue) {
        // no point in searching for a null value
        if (aValue != null) {
          SearchFieldWithPopup.this.valueSelected(aValue);
        }
        getTextField().setText(null);
      }

    };
    fSearchService.setClearText(true);
    fSearchResultPopup = SearchResultPopup.install(fTextField, fSearchService);
    setLayout(new BorderLayout());
    fSearchLabel = new TextComponentHint("Search", fTextField);
    add(TextComponentHint.overlay(fTextField, fSearchLabel), BorderLayout.CENTER);
  }

  public final JTextField getTextField() {
    return fTextField;
  }

  protected abstract void valueSelected(String aValue);

  public final void setSearchHint(String aSearchHint) {
    fSearchLabel.setText(aSearchHint);
  }

  public final void setSearchableContent(String[] aStrings) {
    fSearchService.setSearchableContent(aStrings);
  }

  public final FixedListBackend getSearchService() {
    return fSearchService;
  }

  public void setWidthFactor(double aWidthFactor) {
    fSearchResultPopup.setWidthFactor(aWidthFactor);
  }

  final boolean isPopupShowing() {
    return fSearchResultPopup.isPopupShowing();
  }
}
