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
package samples.lucy.clustering;

import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;

import com.luciad.lucy.gui.TLcyTwoColumnLayoutBuilder;
import com.luciad.lucy.gui.customizer.ALcyCustomizerPanel;
import com.luciad.lucy.map.style.lightspeed.TLcyLspCustomizableStyleContext;
import com.luciad.util.ILcdFilter;
import com.luciad.view.lightspeed.style.TLspTextStyle;
import com.luciad.view.lightspeed.style.styler.TLspCustomizableStyle;

import samples.common.gui.ColorChooser;

/**
 * <p>
 *   {@code ALcyCustomizerPanel} instance for the text style of the cluster.
 *   We cannot use the standard customizer panel as that offers too many customization options.
 *   It should only allow to change the text color.
 * </p>
 *
 * <p>
 *   Consult the Lucy developer guide for more information about providing UI to alter the styling of a
 *   Lightspeed layer.
 *   See the "Create UI to modify the layer style" section in the Lightspeed chapter.
 * </p>
 */
final class ClusterTextStyleCustomizerPanel extends ALcyCustomizerPanel {
  static final ILcdFilter<Object> FILTER = new ILcdFilter<Object>() {
    @Override
    public boolean accept(Object aObject) {
      return !findAllClusterTextStyles(aObject).isEmpty();
    }
  };

  private ColorChooser fTextColorChooser;

  private boolean fUpdatingUI = false;
  private final PropertyChangeListener fColorChooserListener = new PropertyChangeListener() {
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
      if (!fUpdatingUI) {
        setChangesPending(true);
      }
    }
  };

  ClusterTextStyleCustomizerPanel(ILcdFilter aObjectFilter) {
    super(aObjectFilter);
    initUI();
    fTextColorChooser.addPropertyChangeListener(fColorChooserListener);
  }

  private void initUI() {
    fTextColorChooser = new ColorChooser(Color.WHITE, 25, 25);
    TLcyTwoColumnLayoutBuilder.newBuilder().row()
                              .columnOne(new JLabel("Text"), fTextColorChooser)
                              .build()
                              .populate(this);
  }

  @Override
  protected boolean applyChangesImpl() {
    Object object = getObject();
    List<TLcyLspCustomizableStyleContext> allClusterTextStyles = findAllClusterTextStyles(object);
    for (TLcyLspCustomizableStyleContext customizableStyleContext : allClusterTextStyles) {
      TLspCustomizableStyle customizableStyle = customizableStyleContext.getStyle();
      TLspTextStyle textStyle = (TLspTextStyle) customizableStyle.getStyle();
      customizableStyle.setStyle(textStyle.asBuilder().textColor(fTextColorChooser.getColor()).build());
    }
    return true;
  }

  @Override
  protected void updateCustomizerPanelFromObject() {
    boolean old = fUpdatingUI;
    fUpdatingUI = false;
    try {
      Object object = getObject();
      List<TLcyLspCustomizableStyleContext> allClusterTextStyles = findAllClusterTextStyles(object);
      //use the first style to init the value of the color choosers
      if (!allClusterTextStyles.isEmpty()) {
        TLspCustomizableStyle customizableStyle = allClusterTextStyles.get(0).getStyle();
        TLspTextStyle textStyle = (TLspTextStyle) customizableStyle.getStyle();
        fTextColorChooser.setColor(textStyle.getTextColor());
      }
    } finally {
      fUpdatingUI = old;
    }
  }

  private static List<TLcyLspCustomizableStyleContext> findAllClusterTextStyles(Object aObject) {
    List<TLcyLspCustomizableStyleContext> result = new ArrayList<>();
    if (aObject instanceof TLcyLspCustomizableStyleContext) {
      if (ClusterBodyStyler.CLUSTER_TEXT_STYLE_IDENTIFIER.equals(((TLcyLspCustomizableStyleContext) aObject).getStyle().getIdentifier()) &&
          ((TLcyLspCustomizableStyleContext) aObject).getStyle().getStyle() instanceof TLspTextStyle) {
        result.add((TLcyLspCustomizableStyleContext) aObject);
      }
    } else if (aObject instanceof Object[]) {
      for (int i = 0; i < ((Object[]) aObject).length; i++) {
        Object o = ((Object[]) aObject)[i];
        result.addAll(findAllClusterTextStyles(o));
      }
    }
    return result;
  }
}
