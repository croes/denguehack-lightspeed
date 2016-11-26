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

import com.luciad.gui.TLcdSymbol;
import com.luciad.lucy.gui.TLcyTwoColumnLayoutBuilder;
import com.luciad.lucy.gui.customizer.ALcyCustomizerPanel;
import com.luciad.lucy.map.style.lightspeed.TLcyLspCustomizableStyleContext;
import com.luciad.util.ILcdFilter;
import com.luciad.view.lightspeed.style.TLspIconStyle;
import com.luciad.view.lightspeed.style.styler.TLspCustomizableStyle;

import samples.common.gui.ColorChooser;

/**
 * <p>
 *   {@code ALcyCustomizerPanel} instance for the icon style of the cluster.
 *   We cannot use the standard customizer panel as that offers too many customization options.
 *   For example the size of the cluster icon is calculated based on the number of elements contained
 *   in the cluster.
 *   The user should not be able to alter that size.
 * </p>
 *
 * <p>
 *   Therefore, this panel only allows to adjust the fill and border color of the icon.
 * </p>
 *
 * <p>
 *   Consult the Lucy developer guide for more information about providing UI to alter the styling of a
 *   Lightspeed layer.
 *   See the "Create UI to modify the layer style" section in the Lightspeed chapter.
 * </p>
 */
final class ClusterIconStyleCustomizerPanel extends ALcyCustomizerPanel {

  static final ILcdFilter<Object> FILTER = new ILcdFilter<Object>() {
    @Override
    public boolean accept(Object aObject) {
      return !findAllClusterIconStyles(aObject).isEmpty();
    }
  };

  private ColorChooser fBorderColorChooser;
  private ColorChooser fFillColorChooser;

  private boolean fUpdatingUI = false;
  private final PropertyChangeListener fColorChooserListener = new PropertyChangeListener() {
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
      if (!fUpdatingUI) {
        setChangesPending(true);
      }
    }
  };

  ClusterIconStyleCustomizerPanel(ILcdFilter aObjectFilter) {
    super(aObjectFilter);
    initUI();
    fBorderColorChooser.addPropertyChangeListener(fColorChooserListener);
    fFillColorChooser.addPropertyChangeListener(fColorChooserListener);
  }

  private void initUI() {
    fBorderColorChooser = new ColorChooser(Color.WHITE, 25, 25);
    fFillColorChooser = new ColorChooser(Color.WHITE, 25, 25);
    TLcyTwoColumnLayoutBuilder.newBuilder().row()
                              .columnOne(new JLabel("Border"), fBorderColorChooser)
                              .columnTwo(new JLabel("Fill"), fFillColorChooser)
                              .build()
                              .populate(this);
  }

  @Override
  protected boolean applyChangesImpl() {
    Object object = getObject();
    List<TLcyLspCustomizableStyleContext> allClusterIconStyles = findAllClusterIconStyles(object);
    for (TLcyLspCustomizableStyleContext customizableStyleContext : allClusterIconStyles) {
      TLspCustomizableStyle customizableStyle = customizableStyleContext.getStyle();
      TLspIconStyle iconStyle = (TLspIconStyle) customizableStyle.getStyle();
      TLcdSymbol icon = (TLcdSymbol) iconStyle.getIcon();
      TLcdSymbol modifiedIcon = new TLcdSymbol(icon.getMemberIndex(), icon.getSize(), fBorderColorChooser.getColor(), fFillColorChooser.getColor());
      customizableStyle.setStyle(iconStyle.asBuilder().icon(modifiedIcon).build());
    }
    return true;
  }

  @Override
  protected void updateCustomizerPanelFromObject() {
    boolean old = fUpdatingUI;
    fUpdatingUI = false;
    try {
      Object object = getObject();
      List<TLcyLspCustomizableStyleContext> allClusterIconStyles = findAllClusterIconStyles(object);
      //use the first style to init the value of the color choosers
      if (!allClusterIconStyles.isEmpty()) {
        TLspCustomizableStyle customizableStyle = allClusterIconStyles.get(0).getStyle();
        TLspIconStyle iconStyle = (TLspIconStyle) customizableStyle.getStyle();
        TLcdSymbol icon = (TLcdSymbol) iconStyle.getIcon();
        fBorderColorChooser.setColor(icon.getBorderColor());
        fFillColorChooser.setColor(icon.getFillColor());
      }
    } finally {
      fUpdatingUI = old;
    }
  }

  private static List<TLcyLspCustomizableStyleContext> findAllClusterIconStyles(Object aObject) {
    List<TLcyLspCustomizableStyleContext> result = new ArrayList<>();
    if (aObject instanceof TLcyLspCustomizableStyleContext) {
      if (ClusterBodyStyler.CLUSTER_ICON_STYLE_IDENTIFIER.equals(((TLcyLspCustomizableStyleContext) aObject).getStyle().getIdentifier()) &&
          ((TLcyLspCustomizableStyleContext) aObject).getStyle().getStyle() instanceof TLspIconStyle) {
        result.add((TLcyLspCustomizableStyleContext) aObject);
      }
    } else if (aObject instanceof Object[]) {
      for (int i = 0; i < ((Object[]) aObject).length; i++) {
        Object o = ((Object[]) aObject)[i];
        result.addAll(findAllClusterIconStyles(o));
      }
    }
    return result;
  }
}
