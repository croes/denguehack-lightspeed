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
package samples.lucy.search.service.location.coordinate;

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
import com.luciad.lucy.util.language.TLcyLang;

/**
 * Renderer for {@link CoordinateSearchResult} instances
 */
final class CoordinateSearchResultRenderer implements TableCellRenderer {

  private static final ILcdIcon ICON = TLcdIconFactory.create(TLcdIconFactory.LOCATION_ICON,
                                                              TLcdIconFactory.getDefaultTheme(),
                                                              TLcdIconFactory.Size.SIZE_32);
  private static final String SHOW_LOCATION_ON_MAP = TLcyLang.getString("Show location on map:");

  private final CoordinateSearchService.PointFormatProvider fPointFormatProvider;

  private final JPanel fComponent;
  private final JLabel fLocationLabel = new JLabel();

  public CoordinateSearchResultRenderer(CoordinateSearchService.PointFormatProvider aPointFormatProvider) {
    fPointFormatProvider = aPointFormatProvider;

    JPanel panel = new JPanel();
    panel.setOpaque(false);
    panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));

    JLabel showLocationOnMapLabel = new JLabel(SHOW_LOCATION_ON_MAP);
    panel.add(showLocationOnMapLabel);
    panel.add(fLocationLabel);
    panel.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 0));

    JLabel iconLabel = new JLabel();
    iconLabel.setIcon(new TLcdSWIcon(ICON));

    fComponent = new JPanel(new BorderLayout());
    fComponent.add(iconLabel, BorderLayout.WEST);
    fComponent.add(panel, BorderLayout.CENTER);
    fComponent.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
  }

  @Override
  public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    CoordinateSearchResult searchResult = (CoordinateSearchResult) value;
    Pattern searchPattern = searchResult.getSearchPattern();
    fLocationLabel.setText(TextHighlighter.createHighlightedText(searchPattern, fPointFormatProvider.retrieveFormat().format(searchResult.getResult())));
    return fComponent;
  }
}
