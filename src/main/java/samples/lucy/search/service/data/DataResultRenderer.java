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
package samples.lucy.search.service.data;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import org.jdesktop.swingx.icon.EmptyIcon;
import org.jdesktop.swingx.renderer.StringValue;

import com.luciad.gui.ILcdIcon;
import com.luciad.gui.TLcdResizeableIcon;
import com.luciad.gui.swing.TLcdSWIcon;
import samples.common.gui.TextHighlighter;
import samples.lucy.tableview.ValueUtil;
import com.luciad.lucy.ILcyLucyEnv;
import com.luciad.model.ILcdModelReference;

/**
 * Renderer for a {@link DataSearchResult}. It shows the layer icon the search result belongs too, along with the
 * name of the layer and the matching property/value of the object.
 */
final class DataResultRenderer implements TableCellRenderer {
  private static final int ICON_SIZE = 32;
  private static final EmptyIcon EMPTY_ICON = new EmptyIcon(ICON_SIZE, ICON_SIZE);
  private final ILcyLucyEnv fLucyEnv;

  private final JPanel fCompositePanel;
  private final JLabel fLayerLabel = new JLabel();
  private final JLabel fPropertyLabel = new JLabel();
  private final JLabel fIconLabel = new JLabel();

  public DataResultRenderer(ILcyLucyEnv aLucyEnv) {
    fLucyEnv = aLucyEnv;

    JPanel labelPanel = new JPanel();
    labelPanel.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 0));
    labelPanel.setOpaque(false);
    labelPanel.setLayout(new BoxLayout(labelPanel, BoxLayout.PAGE_AXIS));

    labelPanel.add(fLayerLabel);
    labelPanel.add(fPropertyLabel);

    fCompositePanel = new JPanel(new BorderLayout());
    fCompositePanel.add(fIconLabel, BorderLayout.WEST);
    fCompositePanel.add(labelPanel, BorderLayout.CENTER);
    fCompositePanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
  }

  @Override
  public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    DataSearchResult searchResult = (DataSearchResult) value;

    Pattern searchPattern = searchResult.getSearchPattern();
    fLayerLabel.setText(!isSelected ?
                        TextHighlighter.createHighlightedText(searchPattern, searchResult.getLayer().getLabel()) :
                        TextHighlighter.createHighlightedSelectedText(searchPattern, searchResult.getLayer().getLabel())
    );

    Object propertyValue = searchResult.getPropertyValue();
    StringValue compositeStringValue = ValueUtil.createCompositeStringValue(fLucyEnv, (ILcdModelReference) searchResult.getReference());

    String text = "";
    String[] propertyPath = searchResult.getPropertyPath();
    for (int i = 0; i < propertyPath.length; i++) {
      text += propertyPath[i];
      if (i < propertyPath.length - 1) {
        for (int j = i; j > 0; j--) {
          text += "&nbsp;";
        }
        text += "&rarr;&nbsp;";
      }
    }
    text += ": " + (propertyValue == null ? "" : compositeStringValue.getString(propertyValue));
    fPropertyLabel.setText(!isSelected ?
                           TextHighlighter.createHighlightedText(searchPattern, text) :
                           TextHighlighter.createHighlightedSelectedText(searchPattern, text)
    );

    ILcdIcon icon = searchResult.getLayer().getIcon();
    fIconLabel.setIcon(icon != null ?
                       new TLcdSWIcon(new TLcdResizeableIcon(icon, ICON_SIZE, ICON_SIZE)) :
                       EMPTY_ICON);

    return fCompositePanel;
  }

}
