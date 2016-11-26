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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.Customizer;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.ScrollPaneConstants;
import javax.swing.tree.DefaultMutableTreeNode;

import samples.common.gui.PopupLocation;
import samples.common.gui.PopupPanel;
import samples.common.gui.PopupPanelButton;
import samples.symbology.common.EMilitarySymbology;
import samples.symbology.common.util.MilitarySymbolFacade;
import com.luciad.symbology.app6a.view.swing.TLcdAPP6AObjectCustomizer;
import com.luciad.symbology.milstd2525b.model.ILcdMS2525bCoded;
import com.luciad.symbology.milstd2525b.view.swing.TLcdMS2525bObjectCustomizer;
import com.luciad.util.ILcdFilter;
import com.luciad.util.ILcdStringTranslator;

/**
 * Allows the user to select a military symbol by showing a pop-up with
 * a symbol hierarchy tree.
 * <p/>
 * Override {@link #symbolSelected} to determine what happens when the user selects a symbol.
 * For example, a new instance of the symbol could be created and placed on the map.
 */
public abstract class SymbologyPopupButton extends PopupPanelButton {

  private String fSelectButtonText = "Select";

  private ILcdFilter<String> fSIDCFilter;
  private final ILcdStringTranslator fStringTranslator;

  private EMilitarySymbology fSymbology;
  private String fDefaultSIDC;

  public SymbologyPopupButton(EMilitarySymbology aSymbology, ILcdFilter<String> aSIDCFilter, ILcdStringTranslator aStringTranslator) {
    super(new JPanel(new BorderLayout()), false, PopupLocation.RIGHT);
    fSIDCFilter = aSIDCFilter;
    fStringTranslator = aStringTranslator;
    fSymbology = aSymbology;
    setText(translate("Browse"));
    setToolTipText("Click to browse the symbol hierarchy");
    setEnabled(fSymbology != null);
  }

  public ILcdFilter<String> getSIDCFilter() {
    return fSIDCFilter;
  }

  public void setSIDCFilter(ILcdFilter<String> aFilter) {
    fSIDCFilter = aFilter;
  }

  private String translate(String aString) {
    return fStringTranslator == null ? aString : fStringTranslator.translate(aString);
  }

  public void setSelectButtonText(String aSelectButtonText) {
    fSelectButtonText = aSelectButtonText;
  }

  public void setSymbology(EMilitarySymbology aSymbology, String aDefaultSIDC) {
    fSymbology = aSymbology;
    fDefaultSIDC = aDefaultSIDC;
    setEnabled(fSymbology != null);
  }

  protected abstract void symbolSelected(String aSIDC);

  @Override
  protected void preparePopupContent(JComponent aContent) {
    final Object symbol = MilitarySymbolFacade.newElement(fSymbology, false);
    if (fDefaultSIDC != null) {
      MilitarySymbolFacade.setSIDC(symbol, fDefaultSIDC);
    }
    AbstractAction createAction = new AbstractAction() {
      @Override
      public void actionPerformed(ActionEvent e) {
        getPopup().setPopupVisible(false);
        fDefaultSIDC = MilitarySymbolFacade.getSIDC(symbol); // remember the selection
        symbolSelected(fDefaultSIDC);
      }
    };
    aContent.removeAll();
    Component hierarchyCustomizer = createHierarchyCustomizer(symbol, new TreeDoubleClickAndKeyListener(createAction, getPopup()));
    aContent.add(hierarchyCustomizer, BorderLayout.CENTER);

    JButton create = new JButton();
    create.setAction(createAction);
    create.setText(translate(fSelectButtonText));
    JPanel bottomPanel = new JPanel(new BorderLayout());
    bottomPanel.add(create, BorderLayout.EAST);
    aContent.add(bottomPanel, BorderLayout.SOUTH);
    aContent.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
  }

  private Component createHierarchyCustomizer(Object aSymbol, final TreeDoubleClickAndKeyListener aDoubleClickActionListener) {
    if (aSymbol instanceof ILcdMS2525bCoded) {
      TLcdMS2525bObjectCustomizer customizer = new TLcdMS2525bObjectCustomizer(fSIDCFilter, TLcdMS2525bObjectCustomizer.FilterType.SIDC, fStringTranslator) {
        @Override
        protected void insertCustomizers(int[] aID, Customizer[] aCustomizer) {
          setLayout(new BorderLayout());
          add((JComponent) aCustomizer[0], BorderLayout.CENTER);
        }

        @Override
        protected Customizer createCustomizer(int aID) {
          if (aID == TLcdMS2525bObjectCustomizer.HIERARCHY_CUSTOMIZER) {
            Customizer hierarchyCustomizer = super.createCustomizer(aID);
            ((JComponent) hierarchyCustomizer).setBorder(null);
            ((JScrollPane) hierarchyCustomizer).setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
            ((JScrollPane) hierarchyCustomizer).getViewport().getView().addMouseListener(aDoubleClickActionListener);
            ((JScrollPane) hierarchyCustomizer).getViewport().getView().addKeyListener(aDoubleClickActionListener);
            return hierarchyCustomizer;
          }
          return null;
        }
      };
      customizer.setObject(aSymbol);
      return customizer;
    } else {
      TLcdAPP6AObjectCustomizer customizer = new TLcdAPP6AObjectCustomizer(fSIDCFilter, TLcdAPP6AObjectCustomizer.FilterType.SIDC, fStringTranslator) {
        @Override
        protected void insertCustomizers(int[] aID, Customizer[] aCustomizer) {
          setLayout(new BorderLayout());
          add((JComponent) aCustomizer[0], BorderLayout.CENTER);
        }

        @Override
        protected Customizer createCustomizer(int aID) {
          if (aID == TLcdAPP6AObjectCustomizer.HIERARCHY_CUSTOMIZER) {
            Customizer hierarchyCustomizer = super.createCustomizer(aID);
            ((JComponent) hierarchyCustomizer).setBorder(null);
            ((JScrollPane) hierarchyCustomizer).setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
            ((JScrollPane) hierarchyCustomizer).getViewport().getView().addMouseListener(aDoubleClickActionListener);
            ((JScrollPane) hierarchyCustomizer).getViewport().getView().addKeyListener(aDoubleClickActionListener);
            return hierarchyCustomizer;
          }
          return null;
        }

        @Override
        public Dimension getPreferredSize() {
          Dimension preferredSize = super.getPreferredSize();
          return new Dimension(Math.max(preferredSize.width, 300), preferredSize.height);
        }
      };
      customizer.setObject(aSymbol);
      return customizer;
    }
  }

  private static class TreeDoubleClickAndKeyListener extends MouseAdapter implements KeyListener {
    private final Action fAction;
    private final PopupPanel fPopup;

    private TreeDoubleClickAndKeyListener(Action aAction, PopupPanel aPopup) {
      fAction = aAction;
      fPopup = aPopup;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
      if (e.getClickCount() == 2 && e.getSource() instanceof JTree) {
        triggerAction(e);
      }
    }

    @Override
    public void keyTyped(KeyEvent e) {
      if (e.getKeyChar() == KeyEvent.VK_ENTER && e.getSource() instanceof JTree) {
        triggerAction(e);
      } else if (e.getKeyChar() == KeyEvent.VK_ESCAPE) {
        fPopup.setPopupVisible(false);
      }
    }

    private void triggerAction(InputEvent e) {
      JTree tree = (JTree) e.getSource();
      DefaultMutableTreeNode node = (DefaultMutableTreeNode)
          tree.getLastSelectedPathComponent();
      if (node != null && node.getChildCount() == 0) {
        fAction.actionPerformed(new ActionEvent(e.getSource(), e.getID(), "", e.getModifiers()));
      }
    }

    @Override
    public void keyPressed(KeyEvent e) {
      //do nothing
    }

    @Override
    public void keyReleased(KeyEvent e) {
      //do nothing
    }
  }
}
