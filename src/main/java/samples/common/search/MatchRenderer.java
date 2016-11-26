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

import java.awt.Component;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import samples.common.gui.TextHighlighter;
import com.luciad.util.TLcdPair;

/**
 * Renderer for a list cell to show how the user search string has matched the row.
 * <p/>
 * Copyright (c) 2012, Lennart Schedin All rights reserved.
 * <p/>
 * Redistribution and use in source and binary forms, with or without modification, are permitted
 * provided that the following conditions are met:
 * <p/>
 * Redistributions of source code must retain the above copyright notice, this list of conditions
 * and the following disclaimer. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. Neither the name of the organization nor the names of
 * its contributors may be used to endorse or promote products derived from this software without
 * specific prior written permission.
 * <p/>
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY
 * WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
class MatchRenderer implements TableCellRenderer {

  private final DefaultTableCellRenderer fDefaultTableCellRenderer;

  private String fSearchString;
  private Matcher fMatcher;

  public MatchRenderer() {
    TableCellRenderer tableCellRenderer = new JTable().getDefaultRenderer(String.class);
    if (tableCellRenderer instanceof DefaultTableCellRenderer) {
      fDefaultTableCellRenderer = (DefaultTableCellRenderer) tableCellRenderer;
    } else {
      fDefaultTableCellRenderer = new DefaultTableCellRenderer();
    }
  }

  /**
   * Informs the renderer what the user has searched for and with which fMatcher
   */
  public void setSearchInformation(String searchString, Matcher aMatcher) {
    fSearchString = searchString;
    fMatcher = aMatcher;
  }

  @Override
  public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    Component component = fDefaultTableCellRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
    try {
      component = renderHook(value.toString(), component, isSelected);
    } catch (Exception e) {
      System.err.println("Search string: " + fSearchString);
      System.err.println(value.toString());
      e.printStackTrace();
    }
    if (table.getRowHeight(row) < component.getPreferredSize().height) {
      table.setRowHeight(row, component.getPreferredSize().height);
    }

    if (isSelected) {
      component.setBackground(table.getSelectionBackground());
    } else {
      component.setBackground(table.getBackground());
    }

    return component;
  }

  protected Component renderHook(String row, Component component, boolean isSelected) {

    if (fMatcher == null || fSearchString == null) {
      return component;
    }

    int[][] matches = fMatcher.matchArea(fSearchString, row);

    List<TLcdPair<Integer, Integer>> indices = new ArrayList<>();
    if (matches != null) {
      for (int[] match : matches) {
        indices.add(new TLcdPair<Integer, Integer>(match[0], match[1]));
      }
    }
    if (!isSelected) {
      fDefaultTableCellRenderer.setText(TextHighlighter.createHighlightedText(indices, row));
    } else {
      fDefaultTableCellRenderer.setText(TextHighlighter.createHighlightedSelectedText(indices, row));
    }
    return component;
  }

  public void setFont(Font aFont) {
    fDefaultTableCellRenderer.setFont(aFont);
  }
}
