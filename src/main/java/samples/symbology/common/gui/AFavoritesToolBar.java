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

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;

import com.luciad.gui.ILcdIcon;
import com.luciad.gui.ILcdObjectIconProvider;
import com.luciad.gui.ILcdUndoableListener;
import com.luciad.gui.TLcdResizeableIcon;
import com.luciad.gui.swing.TLcdSWIcon;
import samples.common.SwingUtil;
import samples.symbology.common.EMilitarySymbology;
import samples.symbology.common.util.MilitarySymbolFacade;
import samples.symbology.common.util.SymbologyFavorites;
import com.luciad.util.TLcdPair;
import com.luciad.util.TLcdSystemPropertiesUtil;
import com.luciad.util.collections.ILcdCollection;
import com.luciad.util.collections.ILcdCollectionListener;
import com.luciad.util.collections.TLcdCollectionEvent;

/**
 * A toolbar for selecting often-used military symbols.
 * It shows buttons for every symbol in the {@link #getFavorites} collection.
 * <p/>
 * Implement {@link #symbolSelected} to determine what happens when a favorite symbol is selected.
 */
public abstract class AFavoritesToolBar {
  private static final String SIDC = "SIDC";
  private static final String SYMBOLOGY = "SYMBOLOGY";

  private int fSymbolSize = 32;
  private final int fButtonHeight;
  private final JPopupMenu fPopupMenu;
  private final ILcdCollectionListener fCollectionListener;
  private final SymbologyFavorites fFavorites;
  private ILcdObjectIconProvider fMS2525IconProvider;
  private ILcdObjectIconProvider fAPP6IconProvider;
  private final ILcdUndoableListener fUndoManager;
  private final JToolBar fToolBar;

  public AFavoritesToolBar(SymbologyFavorites aFavorites, ILcdUndoableListener aUndoManager) {
    fFavorites = aFavorites;
    fUndoManager = aUndoManager;
    fButtonHeight = getButtonHeight();
    updateIconProviders();
    fPopupMenu = createPopupMenu();
    fCollectionListener = new CollectionListener(this);
    fFavorites.get().addCollectionListener(fCollectionListener);

    fToolBar = createToolBar();
    SwingUtil.makeFlat(fToolBar); // Cosmetic enhancement
    FlowLayout layout = new FlowLayout(FlowLayout.LEFT);
    layout.setVgap(0);
    fToolBar.setLayout(layout);

    fToolBar.addPropertyChangeListener(new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        //do nothing, just here so that there is a reference from the JToolBar to this
        //AFavoritesToolBar. This way the life time of the two is equal to each other. Useful
        //when this action bar is simply used as a facade for the JToolBar and no (strong)
        //reference is kept to it otherwise.
      }
    });

    setSymbolSize(fSymbolSize);
    updateToolbar();
  }

  protected JToolBar createToolBar() {
    return new JToolBar();
  }

  public JToolBar getToolBar() {
    return fToolBar;
  }

  public void setOpaque(boolean aOpaque) {
    fToolBar.setOpaque(aOpaque);
  }

  private void updateIconProviders() {
    fMS2525IconProvider = MilitarySymbolFacade.createMS2525IconProvider(fSymbolSize);
    fAPP6IconProvider = MilitarySymbolFacade.createAPP6IconProvider(fSymbolSize);
  }

  private int getButtonHeight() {
    JButton button = new JButton(new TLcdSWIcon(new TLcdResizeableIcon(null, fSymbolSize, fSymbolSize)));
    return button.getPreferredSize().height;
  }

  public void setSymbolSize(int aSymbolSize) {
    fSymbolSize = aSymbolSize;
    updateIconProviders();
  }

  public ILcdCollectionListener getCollectionListener() {
    return fCollectionListener;
  }

  public ILcdUndoableListener getUndoManager() {
    return fUndoManager;
  }

  private JPopupMenu createPopupMenu() {
    final JPopupMenu popup = new JPopupMenu();
    popup.add(new JMenuItem(new AbstractAction("Remove") {
      public void actionPerformed(ActionEvent e) {
        String sidc = (String) ((JComponent) popup.getInvoker()).getClientProperty(SIDC);
        EMilitarySymbology symbology = (EMilitarySymbology) ((JComponent) popup.getInvoker()).getClientProperty(SYMBOLOGY);
        fFavorites.get(symbology).remove(sidc);
      }
    }));
    return popup;
  }

  public SymbologyFavorites getFavorites() {
    return fFavorites;
  }

  protected abstract void symbolSelected(TLcdPair<EMilitarySymbology, String> aSymbol);

  private ILcdIcon getSymbolIcon(Object aSymbol) {
    ILcdIcon icon = null;
    if (fMS2525IconProvider.canGetIcon(aSymbol)) {
      icon = fMS2525IconProvider.getIcon(aSymbol);
    } else if (fAPP6IconProvider.canGetIcon(aSymbol)) {
      icon = fAPP6IconProvider.getIcon(aSymbol);
    }
    return icon;
  }

  protected void updateToolbar() {
    fToolBar.removeAll();

    // Avoid variable size of the favorites bar, depending on whether it contains actions or not (see populateActions).
    fToolBar.add(Box.createRigidArea(new Dimension(1, fButtonHeight)));

    populateActions();
    fToolBar.revalidate();
    fToolBar.repaint();
  }

  protected void populateActions() {
    ILcdCollection<TLcdPair<EMilitarySymbology, String>> favorites = getFavorites().get();
    favorites.removeCollectionListener(fCollectionListener);
    for (final TLcdPair<EMilitarySymbology, String> pair : favorites) {
      final JButton btn = getFavoriteButton(pair.getKey(), pair.getValue());
      fToolBar.add(btn);
    }
    favorites.addCollectionListener(fCollectionListener);
  }

  protected JButton getFavoriteButton(EMilitarySymbology aSymbology, String aSIDC) {
    final JButton btn = new JButton();
    btn.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        symbolSelected(new TLcdPair<>((EMilitarySymbology) btn.getClientProperty(SYMBOLOGY)
            , (String) btn.getClientProperty(SIDC)));
      }
    });
    btn.addMouseListener(new FavoriteContextMenuListener());
    btn.putClientProperty(SIDC, aSIDC);
    btn.putClientProperty(SYMBOLOGY, aSymbology);
    setButtonStyle(aSymbology, aSIDC, btn);
    return btn;
  }

  private void setButtonStyle(EMilitarySymbology aSymbology, String aSIDC, JButton aBtn) {
    Object symbol = MilitarySymbolFacade.newElement(aSymbology, false);
    MilitarySymbolFacade.setSIDC(symbol, aSIDC);
    ILcdIcon symbolIcon;
    String symbolName = getSymbolName(symbol);
    if (isLine(symbol)) {
      symbolIcon = new LabeledAreaIcon(fSymbolSize, fSymbolSize,
                                       getAffiliationColor(symbol),
                                       getAcronym(symbolName));
    } else {
      symbolIcon = getSymbolIcon(symbol);
    }
    if (symbolIcon != null) {
      aBtn.setIcon(new TLcdSWIcon(symbolIcon));
      SwingUtil.makeSquare(aBtn);
    } else {
      aBtn.setText(aSIDC);
    }
    aBtn.setToolTipText(symbolName);
    ;

  }

  private String getSymbolName(Object aSymbol) {
    return MilitarySymbolFacade.getDisplayName(aSymbol);
  }

  private String getAcronym(String aString) {
    Pattern pattern = Pattern.compile("([A-Za-z]?)[^\\s]*");
    Matcher matcher = pattern.matcher(aString);
    StringBuilder acronymBuilder = new StringBuilder();
    while (matcher.find()) {
      acronymBuilder.append(matcher.group(1));
    }
    return acronymBuilder.toString();
  }

  private boolean isLine(Object aSymbol) {
    return MilitarySymbolFacade.isLine(aSymbol);
  }

  private Color getAffiliationColor(Object aSymbol) {
    return MilitarySymbolFacade.getAffiliationColor(aSymbol, MilitarySymbolFacade.getAffiliationValue(aSymbol));
  }

  private class FavoriteContextMenuListener extends MouseAdapter {
    @Override
    public void mousePressed(MouseEvent e) {
      if (SwingUtilities.isRightMouseButton(e)) {
        fPopupMenu.show(e.getComponent(), e.getX(), e.getY());
      }
    }
  }

  private static class CollectionListener implements ILcdCollectionListener {

    private final WeakReference<AFavoritesToolBar> fToolbar;

    private CollectionListener(AFavoritesToolBar aToolbar) {
      // the favorites collection is a shared object that should not prevent garbage collection of this widget
      fToolbar = new WeakReference<AFavoritesToolBar>(aToolbar);
    }

    @Override
    public void collectionChanged(TLcdCollectionEvent aCollectionEvent) {
      AFavoritesToolBar favoritesToolbar = fToolbar.get();
      if (favoritesToolbar == null) {
        aCollectionEvent.getSource().removeCollectionListener(this);
      } else {
        switch (aCollectionEvent.getType()) {
        case ELEMENT_ADDED:
          favoritesToolbar.updateToolbar();
          break;
        case ELEMENT_REMOVED:
          favoritesToolbar.updateToolbar();
          break;
        }
      }
    }
  }

  private static class LabeledAreaIcon implements ILcdIcon {

    private static final Font FONT = new Font("dialog", Font.PLAIN, 10);
    private static final int STROKE_WIDTH = 1;
    private static final int CHARS_PER_LINE = 3;
    private static final int MAX_LINES = 2;

    private final int fWidth;
    private final int fHeight;
    private final Color fAreaColor;
    private final String fLabel;

    private LabeledAreaIcon(int aWidth, int aHeight, Color aAreaColor, String aLabel) {
      fWidth = aWidth;
      fHeight = aHeight;
      fAreaColor = aAreaColor;
      fLabel = aLabel;
    }

    public void paintIcon(Component aComponent, Graphics aGraphics, int aX, int aY) {
      Graphics2D g = (Graphics2D) aGraphics;
      Object previous = g.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
      g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

      g.setColor(fAreaColor);
      g.fillRect(aX, aY, fWidth, fHeight);
      g.setColor(Color.BLACK);
      Stroke oldStroke = g.getStroke();
      g.setStroke(new BasicStroke(STROKE_WIDTH));
      g.drawRect(aX, aY, fWidth, fHeight);
      g.setStroke(oldStroke);

      g.setFont(FONT);
      FontMetrics fm = aGraphics.getFontMetrics();

      String[] lines = split(fLabel, CHARS_PER_LINE);
      int lineCount = Math.min(MAX_LINES, lines.length);
      int width = 0;
      for (int i = 0; i < lineCount; ++i) {
        Rectangle2D bounds = fm.getStringBounds(lines[i], aGraphics);
        width = Math.max(width, (int) bounds.getWidth());
      }
      int x = (fWidth - width) / 2;
      int y = (int) (fHeight + fm.getAscent() / 1.5f) / 2 - (lineCount - 1) * fm.getHeight() / 2;
      for (int i = 0; i < lineCount; ++i, y += fm.getHeight()) {
        aGraphics.drawString(lines[i], aX + x, aY + y);
      }

      g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, previous);
    }

    private String[] split(String aString, int aPartLength) {
      int index = 0;
      List<String> parts = new ArrayList<String>();
      while (index < aString.length()) {
        parts.add(aString.substring(index, Math.min(aString.length(), index += aPartLength)));
      }
      return parts.toArray(new String[parts.size()]);
    }

    public int getIconWidth() {
      return fWidth;
    }

    public int getIconHeight() {
      return fHeight;
    }

    public Object clone() {
      try {
        return super.clone();
      } catch (CloneNotSupportedException e) {
        throw new UnsupportedOperationException(e);
      }
    }
  }

}
