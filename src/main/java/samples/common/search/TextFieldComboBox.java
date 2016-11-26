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

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

import javax.swing.JTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

import com.luciad.gui.ILcdObjectIconProvider;
import samples.common.PopupTriangleIcon;
import samples.common.UIColors;

/**
 * Text field that functions as a combo box, using a FixedListBackend to select and auto-complete
 * the possible values.
 */
public abstract class TextFieldComboBox extends JTextField {

  private final FixedListBackend fFixedListSearchService;
  private boolean fBackgroundLocked = false;
  private final SearchResultPopup fSearchResultPopup;

  protected TextFieldComboBox(int columns) {
    this(columns, null);
  }

  protected TextFieldComboBox(int columns, ILcdObjectIconProvider aObjectIconProvider) {
    super(columns);

    setDocument(new PlainDocument() {
      @Override
      public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
        // only allow the operation if the result matches the list of possible values
        String newValue = getText(0, offs) + str + getText(offs, getLength() - offs);
        if (newValue.isEmpty() || fFixedListSearchService.matches(newValue)) {
          super.insertString(offs, str, a);
        }
      }

      @Override
      public void remove(int offs, int len) throws BadLocationException {
        // only allow the operation if the result matches the list of possible values
        String newValue = getText(0, offs) + getText(offs + len, getLength() - offs - len);
        if (newValue.isEmpty() || fFixedListSearchService.matches(newValue)) {
          super.remove(offs, len);
        }
      }
    });

    fFixedListSearchService = new FixedListBackend(aObjectIconProvider) {
      @Override
      protected void valueSelected(String aRow) {
        TextFieldComboBox.this.valueSelected(getText(), aRow);
      }
    };
    fSearchResultPopup = SearchResultPopup.install(this, fFixedListSearchService);
    addMouseMotionListener(new CursorListener());

    addFocusListener(new FocusAdapter() {
      @Override
      public void focusGained(FocusEvent e) {
        //put all the values in the pop-up menu again
        fFixedListSearchService.setSearchableContent(fFixedListSearchService.getSearchContent());
      }

    });

    addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        fSearchResultPopup.toggle();
      }
    });
  }

  final boolean isPopupShowing() {
    return fSearchResultPopup.isPopupShowing();
  }

  @Override
  public void setText(String t) {
    setEnablePopup(false);
    super.setText(t);
    setCaretPosition(0);
    setToolTipText(t);
    setEnablePopup(true);
  }

  @Override
  protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    if (isEnabled()) {
      PopupTriangleIcon icon = new PopupTriangleIcon(16);
      icon.setColor(Color.GRAY);
      Insets i = getInsets();
      if (fBackgroundLocked) {
        g.setColor(getBackground());
      } else {
        g.setColor(UIColors.bg());
      }
      g.fillRect(getWidth() - 20, i.top, 20 - i.right, getHeight() - i.top - i.bottom);
      icon.paintIcon(this,
                     g,
                     getWidth() - 16,
                     i.top + (getHeight() - i.top - i.bottom - icon.getIconHeight()) / 2);
    }
  }

  @Override
  public void setBackground(Color bg) {
    if (!fBackgroundLocked) {
      super.setBackground(bg);
    }
  }

  protected abstract void valueSelected(String aOldValue, String aValue);

  public void setSearchContent(String[] rows) {
    fFixedListSearchService.setSearchableContent(rows);
  }

  public String[] getSearchContent() {
    return fFixedListSearchService.getSearchContent();
  }

  public void setEnablePopup(boolean aEnabled) {
    fFixedListSearchService.setEnabled(aEnabled);
  }

  public FixedListBackend getFixedListSearchService() {
    return fFixedListSearchService;
  }

  public final void setBackgroundLocked(boolean aLocked) {
    fBackgroundLocked = aLocked;
  }

  public void setWidthFactor(double aWidthFactor) {
    fSearchResultPopup.setWidthFactor(aWidthFactor);
  }

  private class CursorListener implements MouseMotionListener {

    private final Cursor fDefaultCursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
    private final Cursor fHandCursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);

    @Override
    public void mouseDragged(MouseEvent e) {
    }

    @Override
    public void mouseMoved(MouseEvent e) {
      if (isEnabled() && getWidth() - e.getX() < 20) {
        setCursor(fHandCursor);
      } else {
        setCursor(fDefaultCursor);
      }
    }
  }
}
