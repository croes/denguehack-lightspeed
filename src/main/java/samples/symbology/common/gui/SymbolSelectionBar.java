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
package samples.symbology.common.gui;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.EnumSet;

import javax.swing.JTextField;
import javax.swing.JToolBar;

import samples.common.SwingUtil;
import samples.symbology.common.EMilitarySymbology;
import com.luciad.symbology.app6a.view.swing.TLcdAPP6AObjectCustomizer;
import com.luciad.util.ILcdFilter;
import com.luciad.util.ILcdStringTranslator;

/**
 * Lets the user select a military symbol by typing in a search field or by clicking
 * on a button that opens a symbol hierarchy tree.
 * <p/>
 * Implement {@link #symbolSelected} to determine what happens when a symbol is selected.
 */
public abstract class SymbolSelectionBar extends JToolBar {

  private final SymbologySearchField fSearchField;
  private final SymbologyPopupButton fPopupButton;

  public SymbolSelectionBar(EMilitarySymbology aSymbology, ILcdStringTranslator aStringTranslator) {
    this(EnumSet.allOf(BarComponent.class), aSymbology, null, aStringTranslator);
  }

  /**
   * Creates a toolbar with the given selection components.
   * @param aComponents the components to include in the toolbar
   * @param aSymbology  the symbology to browse
   * @param aSIDCFilter a filter passed to the symbol hierarchy tree. See
   * {@link TLcdAPP6AObjectCustomizer#getSIDCFilter()} for more information.
   */
  public SymbolSelectionBar(EnumSet<BarComponent> aComponents, EMilitarySymbology aSymbology, ILcdFilter<String> aSIDCFilter, ILcdStringTranslator aStringTranslator) {
    FlowLayout layout = new FlowLayout(FlowLayout.LEFT);
    layout.setVgap(0);
    setLayout(layout);
    // A search field that creates new symbols
    fPopupButton = new SymbologyPopupButton(aSymbology, aSIDCFilter, aStringTranslator) {
      @Override
      protected void symbolSelected(String aSIDC) {
        SymbolSelectionBar.this.symbolSelected(aSIDC);
      }

      @Override
      public void setText(String text) {
        //keep the displayed name at most 28 chars to limit
        //the width of the button
        if (text != null && text.length() > 28) {
          text = text.substring(0, 25) + "...";
        }
        super.setText(text);
      }
    };

    fSearchField = new SymbologySearchField(aSymbology) {

      @Override
      protected void symbolSelected(String aSIDC) {
        SymbolSelectionBar.this.symbolSelected(aSIDC);
      }
    };

    if (aComponents.contains(BarComponent.SEARCH)) {
      add(fSearchField);
    }
    if (aComponents.size() > 1) {
      addSeparator();
    }
    if (aComponents.contains(BarComponent.BROWSE)) {
      add(fPopupButton);
    }
    SwingUtil.makeFlat(this);
  }

  public SymbologyPopupButton getBrowseButton() {
    return fPopupButton;
  }

  protected abstract void symbolSelected(String aSIDC);

  public void setSymbology(EMilitarySymbology aSymbology, String aDefaultSIDC) {
    fSearchField.setSymbology(aSymbology);
    fPopupButton.setSymbology(aSymbology, aDefaultSIDC);
  }

  public ILcdFilter<String> getSIDCFilter() {
    return fPopupButton.getSIDCFilter();
  }

  public void setSIDCFilter(ILcdFilter<String> aFilter) {
    fPopupButton.setSIDCFilter(aFilter);
    fSearchField.setSIDCFilter(aFilter);
  }

  public EMilitarySymbology getSymbology() {
    return fSearchField.getSymbology();
  }

  @Override
  public void setEnabled(boolean enabled) {
    super.setEnabled(enabled);
    fSearchField.getTextField().setEnabled(enabled);
    fPopupButton.setEnabled(enabled);
  }

  @Override
  public Dimension getMinimumSize() {
    return super.getPreferredSize();
  }

  @Override
  public Dimension getMaximumSize() {
    return super.getPreferredSize();
  }

  public void setSearchHint(String aSearchHint) {
    fSearchField.setSearchHint(aSearchHint);
  }

  public void setSelectButtonText(String aSelectButtonText) {
    fPopupButton.setSelectButtonText(aSelectButtonText);
  }

  public JTextField getTextField() {
    return fSearchField.getTextField();
  }

  public enum BarComponent {
    SEARCH,
    BROWSE
  }
}
