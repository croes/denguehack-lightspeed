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
package samples.lucy.search.service.location.geonames;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import com.luciad.gui.ILcdIcon;
import com.luciad.gui.TLcdIconFactory;
import com.luciad.gui.swing.TLcdSWIcon;
import samples.common.gui.TextHighlighter;

/**
 * Renderer for {@link GeoNamesSearchResult}s coming from the GeoNamesSearchTask.
 */
final class GeoNamesRenderer implements TableCellRenderer {

  private static final ILcdIcon ICON = TLcdIconFactory.create(TLcdIconFactory.LOCATION_ICON,
                                                              TLcdIconFactory.getDefaultTheme(),
                                                              TLcdIconFactory.Size.SIZE_32);

  private final JLabel fNameLabel = new JLabel();
  private final JLabel fCountryNameLabel = new JLabel();
  private final JPanel fComponent;

  public GeoNamesRenderer() {
    JPanel panel = new JPanel();
    panel.setOpaque(false);
    panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));

    panel.add(fNameLabel);
    panel.add(fCountryNameLabel);
    panel.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 0));

    fComponent = new JPanel();
    fComponent.setLayout(new BorderLayout());

    fComponent.add(new JLabel(new TLcdSWIcon(ICON)), BorderLayout.WEST);
    fComponent.add(panel, BorderLayout.CENTER);
    fComponent.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
  }

  @Override
  public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    GeoNamesSearchResult searchResult = (GeoNamesSearchResult) value;
    Pattern searchPattern = searchResult.getSearchPattern();
    fNameLabel.setText(TextHighlighter.createHighlightedText(searchPattern, searchResult.getName()));
    fCountryNameLabel.setText(TextHighlighter.createHighlightedText(searchPattern, searchResult.getCountryName()));
    return fComponent;
  }
}
