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
package samples.symbology.nvg.gui;

import java.awt.Component;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.EnumSet;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.luciad.format.nvg.model.TLcdNVGSymbol;
import com.luciad.format.nvg.nvg20.model.TLcdNVG20SymbolizedContent;
import com.luciad.format.nvg.nvg20.model.TLcdNVG20Text;
import samples.common.TextComponentHint;
import samples.common.TitledCollapsiblePane;
import samples.common.TitledSeparator;
import samples.common.TwoColumnPanel;
import samples.symbology.common.EMilitarySymbology;
import samples.symbology.common.gui.SymbolCustomizerFactory;
import samples.symbology.common.gui.customizer.AbstractSymbolCustomizer;
import samples.symbology.common.gui.customizer.SymbolCustomizer;
import samples.symbology.common.util.SymbologyFavorites;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelListener;
import com.luciad.model.TLcdModelChangedEvent;
import com.luciad.symbology.app6a.model.ILcdAPP6ACoded;
import com.luciad.symbology.milstd2525b.model.ILcdMS2525bCoded;
import com.luciad.util.ILcdFilter;
import com.luciad.util.ILcdStringTranslator;

import samples.symbology.nvg.gui.style.NVGStyleCustomizer;

/**
 * SymbolCustomizer factory for NVG Samples. This factory creates {@link AbstractSymbolCustomizer}
 * which lets user to modify symbology properties.
 */
public class NVGCustomizerFactory {

  /**
   * Creates a customizer consisting of the given parts.
   * @param aFireModelChangeEvent true if model changes should be fired. Typically this is only false if you're
   *                         customizing objects that are not (yet) contained in a model.
   * @param aFavorites       the instance responsible for managing the favorite symbols
   * @param aTitledSections  if true, a title will be displayed for in the regular symbol properties sub-panel.
   *                         Use false if the panel is already contained in, for example, a titled dialog or panel.
   * @param aHierarchyFilter an optional filter to reduce the amount of symbols when browsing through the symbol hierarchy.
   *                         See {@link samples.symbology.common.gui.SymbolSelectionBar}. Can be null.
   * @param aStringTranslator a string translator. Can be null.
   * @return a GUI panel consisting of the given parts
   */
  public static AbstractSymbolCustomizer createCustomizer(
      boolean aFireModelChangeEvent,
      SymbologyFavorites aFavorites,
      boolean aTitledSections,
      ILcdFilter<String> aHierarchyFilter,
      ILcdStringTranslator aStringTranslator) {
    return new NVGSymbolCustomizerPanel(aFireModelChangeEvent, aFavorites, aTitledSections, aHierarchyFilter, aStringTranslator);
  }

  private static class SymbolProxyListener implements ILcdModelListener {
    private TLcdNVG20SymbolizedContent fSymbolizedContent = null;
    private Object fMilitaryObject = null;

    @Override
    public void modelChanged(TLcdModelChangedEvent aEvent) {
      if (fMilitaryObject != null &&
          aEvent.containsElement(fMilitaryObject) &&
          fSymbolizedContent != null) {
        if (fMilitaryObject instanceof ILcdAPP6ACoded) {
          fSymbolizedContent.setSymbolFromAPP6((ILcdAPP6ACoded) fMilitaryObject);
        } else {
          fSymbolizedContent.setSymbolFromMS2525((ILcdMS2525bCoded) fMilitaryObject);
        }
        aEvent.getModel().elementChanged(fSymbolizedContent, ILcdModel.FIRE_NOW);
      }
    }
  }

  private static class NVGSymbolCustomizerPanel extends AbstractSymbolCustomizer {
    private final JPanel fComponent;
    private final AbstractSymbolCustomizer fSymbolCustomizer;
    private final AbstractSymbolCustomizer fTextCustomizer;
    private final AbstractSymbolCustomizer fStyleCustomizer;

    private final SymbolProxyListener fSymbolProxyListener = new SymbolProxyListener();
    private ILcdModel fModel;

    private NVGSymbolCustomizerPanel(boolean aFireModelChange, SymbologyFavorites aFavorites, boolean aDisplayPropertiesTitle, ILcdFilter<String> aHierarchyFilter, ILcdStringTranslator aStringTranslator) {
      super(aFireModelChange, aStringTranslator);
      fComponent = new JPanel();
      fComponent.setAlignmentX(Component.LEFT_ALIGNMENT);
      fComponent.setLayout(new BoxLayout(fComponent, BoxLayout.Y_AXIS));
      fSymbolCustomizer = new SymbolCustomizer(EnumSet.of(SymbolCustomizerFactory.Part.REGULAR, SymbolCustomizerFactory.Part.ADVANCED),
                                               aFireModelChange,
                                               aFavorites,
                                               aDisplayPropertiesTitle,
                                               aHierarchyFilter,
                                               aStringTranslator);
      fTextCustomizer = new TextCustomizer(true, aStringTranslator);
      fStyleCustomizer = new NVGStyleCustomizer(true, aStringTranslator);
      final TitledSeparator separator = new TitledSeparator(translate("Symbol"));
      separator.setAlignmentX(Component.LEFT_ALIGNMENT);
      fComponent.add(separator);
      fComponent.add(pad(fSymbolCustomizer.getComponent()));
      fComponent.add(pad(fTextCustomizer.getComponent()));
      fComponent.add(pad(fStyleCustomizer.getComponent()));
    }

    private static JComponent pad(JComponent aComponent) {
      aComponent.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
      return aComponent;
    }

    @Override
    public void setSymbol(EMilitarySymbology aMilitarySymbology, ILcdModel aModel, Object aSymbol) {
      if (fModel != null) {
        fModel.removeModelListener(fSymbolProxyListener);
      }
      setSymbolImpl(aMilitarySymbology, aModel, aSymbol);
      fModel = aModel;
      if (fModel != null) {
        fModel.addModelListener(fSymbolProxyListener);
      }
    }

    @Override
    protected void setSymbolImpl(EMilitarySymbology aMilitarySymbology, ILcdModel aModel, Object aSymbol) {
      if (aSymbol instanceof TLcdNVG20Text) {
        fTextCustomizer.setSymbol(null, aModel, aSymbol);
      } else {
        fTextCustomizer.setEnabled(false);
      }
      if (NVGStyleCustomizer.isStyledSymbolizedContent(aSymbol)) {
        fStyleCustomizer.getComponent().setVisible(true);
        fStyleCustomizer.setSymbol(null, aModel, aSymbol);
      } else {
        fStyleCustomizer.setSymbol(null, null, null);
        fStyleCustomizer.getComponent().setVisible(false);
      }
      if (aSymbol instanceof TLcdNVG20SymbolizedContent) {
        fSymbolProxyListener.fSymbolizedContent = (TLcdNVG20SymbolizedContent) aSymbol;
        fSymbolProxyListener.fMilitaryObject = retrieveMilitarySymbol((TLcdNVG20SymbolizedContent) aSymbol);
        if (fSymbolProxyListener.fMilitaryObject == null) {
          fSymbolProxyListener.fSymbolizedContent = null;
        }
        fSymbolCustomizer.setSymbol(aMilitarySymbology, aModel, fSymbolProxyListener.fMilitaryObject);
      } else if (aSymbol instanceof ILcdAPP6ACoded || aSymbol instanceof ILcdMS2525bCoded) {
        assert false : "Handled by the proxy listener. This block should not be reached.";
      } else {
        fSymbolCustomizer.setSymbol(null, null, null);
        fSymbolProxyListener.fSymbolizedContent = null;
        fSymbolProxyListener.fMilitaryObject = null;
      }
    }

    @Override
    public JComponent getComponent() {
      return fComponent;
    }

    @Override
    public void setEnabled(boolean aEnabled) {
      fComponent.setEnabled(aEnabled);
    }
  }

  private static class TextCustomizer extends AbstractSymbolCustomizer {

    private final TitledCollapsiblePane fCollapsiblePane;
    private final JTextField fTextField;
    private TLcdNVG20Text fTextContent;
    private final TextComponentHint fSearchLabel;
    private String fPrevText;

    public TextCustomizer(boolean aFireModelChange, ILcdStringTranslator aStringTranslator) {
      super(aFireModelChange, aStringTranslator);
      fTextField = new JTextField();

      fTextField.addFocusListener(new FocusListener() {
        @Override
        public void focusGained(FocusEvent e) {
          fPrevText = fTextContent != null ? fTextContent.getTextString() : null;
        }

        @Override
        public void focusLost(FocusEvent e) {
          updateText();
        }
      });

      fTextField.addKeyListener(new KeyListener() {
        @Override
        public void keyTyped(KeyEvent e) {
        }

        @Override
        public void keyPressed(KeyEvent e) {
          if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            updateText();
          } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            cancelChange();
          }
        }

        @Override
        public void keyReleased(KeyEvent e) {

        }
      });

      fSearchLabel = new TextComponentHint("Enter text", fTextField);
      final TwoColumnPanel panel =
          new TwoColumnPanel()
              .contentBuilder()
              .row("Text", TextComponentHint.overlay(fTextField, fSearchLabel))
              .build();
      fCollapsiblePane = new TitledCollapsiblePane("Text properties", panel);
      fCollapsiblePane.setAlignmentX(Component.LEFT_ALIGNMENT);
    }

    private void cancelChange() {
      if (fTextContent != null && !fTextField.getText().equals(fPrevText)) {
        applyChange(new Runnable() {
          @Override
          public void run() {
            fTextContent.setTextString(fPrevText);
          }
        }, new Runnable() {
          @Override
          public void run() {
            //no undo support
          }
        });
      }
    }

    private void updateText() {
      if (fTextContent != null) {
        final String text = fTextField.getText().trim().length() > 0 ? fTextField.getText() : fPrevText;
        fPrevText = text;
        applyChange(new Runnable() {
          @Override
          public void run() {
            fTextContent.setTextString(text);
          }
        }, new Runnable() {
          @Override
          public void run() {
            //no undo support
          }
        });
      }
    }

    @Override
    protected Object getID(Object aSymbol) {
      // Text objects don't have an ID, use object itself
      return aSymbol;
    }

    @Override
    protected void setSymbolImpl(EMilitarySymbology aMilitarySymbology, ILcdModel aModel, Object aSymbol) {
      if (aSymbol != null) {
        setEnabled(true);
        fTextContent = (TLcdNVG20Text) aSymbol;
        fTextField.setText(fTextContent.getTextString());
      }
    }

    @Override
    public JComponent getComponent() {
      return fCollapsiblePane;
    }

    @Override
    public void setEnabled(boolean aEnabled) {
      fCollapsiblePane.setVisible(aEnabled);
      fCollapsiblePane.setCollapsed(!aEnabled);
    }
  }

  public static Object retrieveMilitarySymbol(TLcdNVG20SymbolizedContent aSymbolizedContent) {
    if (TLcdNVGSymbol.isAPP6ASymbol(aSymbolizedContent.getSymbol())) {
      return aSymbolizedContent.getAPP6CodedFromMapObject();
    } else if (TLcdNVGSymbol.isMS2525bSymbol(aSymbolizedContent.getSymbol())) {
      return aSymbolizedContent.getMS2525CodedFromMapObject();
    }
    return null;
  }

}
