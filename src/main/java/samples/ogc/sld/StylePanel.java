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
package samples.ogc.sld;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JEditorPane;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.luciad.ogc.sld.model.TLcdSLDDescription;
import com.luciad.ogc.sld.model.TLcdSLDFeatureTypeStyle;
import com.luciad.ogc.sld.model.TLcdSLDParameterValue;
import com.luciad.util.collections.ILcdCollection;
import com.luciad.util.collections.ILcdCollectionListener;
import com.luciad.util.collections.TLcdCollectionEvent;
import com.luciad.view.ILcdLayer;
import com.luciad.view.ILcdLayered;
import com.luciad.view.ILcdLayeredListener;
import com.luciad.view.TLcdLayeredEvent;
import com.luciad.view.gxy.TLcdGXYLayer;

import samples.gxy.common.TitledPanel;

/**
 * A panel that enables choosing a style from a list and applying the style
 * to a layer in another list.
 */
public abstract class StylePanel extends JPanel {

  private Hashtable<ILcdLayer, Object> fOriginalStyles;
  private JList fStylesList;
  private JEditorPane fDescriptionTextArea;
  private ILcdCollection<ILcdLayer> fSelectedLayers;
  private DefaultListModel fListModel;
  private SLDFeatureTypeStyleStore fStyleStore;

  public StylePanel(ILcdLayered aView, ILcdCollection<ILcdLayer> aSelectedLayers, SLDFeatureTypeStyleStore aStyleStore) {
    fStyleStore = aStyleStore;

    fSelectedLayers = aSelectedLayers;
    fSelectedLayers.addCollectionListener(new ILcdCollectionListener<ILcdLayer>() {
      @Override
      public void collectionChanged(TLcdCollectionEvent<ILcdLayer> aCollectionEvent) {
        updateStylesList();
      }
    });

    // we assume a layer has only 1 painter provider.
    fOriginalStyles = new Hashtable<>();
    aView.addLayeredListener(new OriginalStyleUpdater());

    fListModel = new DefaultListModel();
    fStylesList = new JList(fListModel);
    fStylesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    fStylesList.setVisibleRowCount(10);

    this.updateStylesList();
    fStylesList.addListSelectionListener(new ListSelectionListener() {

      @Override
      public void valueChanged(ListSelectionEvent aEvent) {
        if (!aEvent.getValueIsAdjusting()) {
          showAbstract(fStylesList.getSelectedValue());
          applyStyleToSelectedLayer();
        }
      }
    });

    JScrollPane listScrollPane = new JScrollPane(fStylesList);

    fDescriptionTextArea = new JEditorPane();
    fDescriptionTextArea.setContentType("text/html");

    setLayout(new BorderLayout());
    add(TitledPanel.createTitledPanel("Styles", listScrollPane), BorderLayout.CENTER);

    final JScrollPane sldInfoScroll = new JScrollPane(fDescriptionTextArea);
    sldInfoScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    sldInfoScroll.setPreferredSize(new Dimension(250, 170));
    add(TitledPanel.createTitledPanel("Description", sldInfoScroll), BorderLayout.SOUTH);
  }

  /**
   * Returns the original styling information from the layer.
   * @return The currently set style of {@code aLayer}
   */
  protected Object getOriginalStyle(ILcdLayer aLayer) {
    Object originalStyle;
    if (aLayer instanceof TLcdGXYLayer) {
      TLcdGXYLayer gxy_layer = (TLcdGXYLayer) aLayer;
      originalStyle = gxy_layer.getGXYPainterProvider();
    } else {
      originalStyle = null;
    }
    return originalStyle;
  }

  /**
   * Restores the original style of the layer.
   *
   * @param aOriginalStyle Style returned by
   *          {@link #getOriginalStyle(ILcdLayer)}
   * @param aLayer The layer on which to set the styling.
   * @see #getOriginalStyle(ILcdLayer)
   */
  protected abstract void applyOriginalStyleToLayer(Object aOriginalStyle, ILcdLayer aLayer);

  /**
   * Applies the given style to the given layer
   */
  protected abstract void applyStyleToLayer(ILcdLayer aLayer, TLcdSLDFeatureTypeStyle aStyle);

  private void applyStyleToSelectedLayer() {
    Iterator<ILcdLayer> selectedObjects = fSelectedLayers.iterator();
    if (selectedObjects.hasNext()) {
      ILcdLayer selected_layer = selectedObjects.next();
      Object selected_style = fStylesList.getSelectedValue();
      if (selected_style instanceof TLcdSLDFeatureTypeStyle) {
        try {
          applyStyleToLayer(selected_layer, (TLcdSLDFeatureTypeStyle) selected_style);
        } catch (IllegalArgumentException e) {
          showIncompatibleLayerMessage(selected_layer, e);
        }
      } else {
        Object style = fOriginalStyles.get(selected_layer);
        applyOriginalStyleToLayer(style, selected_layer);
      }
      selected_layer.setVisible(true);
    } else {
      JOptionPane.showMessageDialog(null, new String[]{"Before applying a style select a layer, by clicking on", "its name in the layer control."});
    }

  }

  /**
   * Adds a new style to the list.
   */
  public void addStyle(TLcdSLDFeatureTypeStyle aStyle) {
    fListModel.addElement(aStyle);
  }

  private void updateStylesList() {
    fListModel.removeAllElements();
    Iterator<ILcdLayer> selectedObjects = fSelectedLayers.iterator();
    if (selectedObjects.hasNext()) {
      ILcdLayer layer = selectedObjects.next();
      List<TLcdSLDFeatureTypeStyle> styles = fStyleStore.getStylesForModel(layer.getModel());
      fListModel.addElement("Default");
      if (styles != null) {
        for (TLcdSLDFeatureTypeStyle style : styles) {
          fListModel.addElement(style);
        }
      }
    }
  }

  private void showIncompatibleLayerMessage(ILcdLayer aLayer, IllegalArgumentException aException) {
    JOptionPane.showMessageDialog(fStylesList,
                                  new String[]{"Could not apply the chosen style to " + aLayer.getLabel(),
                                               aException != null ? aException.getMessage() : ""});
  }

  private void showAbstract(Object aObject) {
    // the style is either the default or it is a style read from files
    String html = " <html>" + "<body>";
    if (aObject instanceof TLcdSLDFeatureTypeStyle) {
      TLcdSLDFeatureTypeStyle featureTypeStyle = (TLcdSLDFeatureTypeStyle) aObject;

      TLcdSLDDescription description = featureTypeStyle.getDescription();
      if (description != null) {
        String style_abstract = description.getAbstract();
        String style_title = description.getTitle();
        if (style_abstract != null) {
          html += style_abstract;
        } else if (style_title != null) {
          html += style_title;
        }
      }
    } else {
      html += "Default style of the layer.";
      // this was the default style for a layer.
    }
    html += "</body></html>";
    fDescriptionTextArea.setText(html);
  }

  protected String printColor(final TLcdSLDParameterValue aColorParameter, String aName, String aHtmlSFCT) {
    if (aColorParameter != null) {
      if (aColorParameter.getContentCount() == 1 && aColorParameter.isContentText(0)) {
        aHtmlSFCT += aName + ":<B style=\"width:15px;height:15px;float:left;color:" + aColorParameter.getText(0) + "\" >";
        aHtmlSFCT += aColorParameter.getText(0) + "</B><br/>";
      }
    }
    return aHtmlSFCT;
  }

  private class OriginalStyleUpdater implements ILcdLayeredListener {

    public void layeredStateChanged(TLcdLayeredEvent aLayeredEvent) {
      if (aLayeredEvent.getID() == TLcdLayeredEvent.LAYER_ADDED) {
        ILcdLayer layer = aLayeredEvent.getLayer();
        Object originalStyle;
        originalStyle = getOriginalStyle(layer);
        if (originalStyle != null) {
          fOriginalStyles.put(layer, originalStyle);
        }
      } else if (aLayeredEvent.getID() == TLcdLayeredEvent.LAYER_REMOVED) {
        ILcdLayer layer = aLayeredEvent.getLayer();
        fOriginalStyles.remove(layer);
      }
    }

  }
}
