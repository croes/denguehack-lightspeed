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
package samples.lightspeed.demo.application.gui.menu;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import com.jgoodies.binding.adapter.Bindings;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.luciad.view.lightspeed.layer.ILspLayer;

import samples.common.HaloLabel;
import samples.lightspeed.demo.framework.application.Framework;
import samples.lightspeed.demo.framework.data.themes.AbstractTheme;
import samples.lightspeed.demo.framework.gui.menu.CheckBoxLayerController;

/**
 */
public class DefaultPanelFactory implements IThemePanelFactory {

  public DefaultPanelFactory() {
    // Default constructor
  }

  @Override
  public List<JPanel> createThemePanels(AbstractTheme aTheme) {
    return Collections.emptyList();
//    return Collections.singletonList( createLayerVisibilityPanel( aTheme ) );
  }

//  public JPanel createLayerVisibilityPanel( AbstractTheme aTheme ) {
//    if ( aTheme.getLayers().isEmpty() ) {
//      return null;
//    }
//
//    boolean isTouchUI = isTouchUI();
//
//    DefaultFormBuilder builder;
//
//    if (isTouchUI) {
//      builder = new DefaultFormBuilder( new FormLayout( "p" ) );
//      builder.setLineGapSize(new ConstantSize(5, ConstantSize.DLUY));
//    }
//    else {
//      builder = new DefaultFormBuilder( new FormLayout( "p,5dlu,p" ) );
//    }
//    builder.setBorder( BorderFactory.createEmptyBorder( 10, 10, 10, 10 ) );
//
//    // Add title to content panel
//    HaloLabel titleLabel = new HaloLabel( "Theme Layers", 15, true ) {
//      @Override
//      public Dimension getPreferredSize() {
//        return new Dimension( 200, 25 );
//      }
//    };
//    builder.append( titleLabel, isTouchUI?1:3 );
//    builder.nextLine();
//
//    if (isTouchUI) {
//      addButtons(aTheme, builder);
//    }
//    else {
//      addCheckBoxes( aTheme, builder );
//    }
//
//    // Create content panel and set its size (not doing the latter will cause it to be invisible)
//    JPanel contentPanel = builder.getPanel();
//    contentPanel.setSize( contentPanel.getLayout().preferredLayoutSize( contentPanel ) );
//    contentPanel.setCursor( Cursor.getDefaultCursor() );
//    return contentPanel;
//  }

  private void addCheckBoxes(AbstractTheme aTheme, DefaultFormBuilder aBuilder) {
    // First we filter the theme's layers into a list of layer sets of layers
    // that have the same layers. This is necessary, because the application
    // might feature multiple views, in which case multiple layer instances
    // will be created for what is actually the same layer (hence the check
    // for layers with identical labels)

    Map<String, List<ILspLayer>> layerLabelMap = new HashMap<String, List<ILspLayer>>();
    for (ILspLayer layer : aTheme.getLayers()) {
      List<ILspLayer> layers = layerLabelMap.get(layer.getLabel());
      if (layers == null) {
        layers = new ArrayList<ILspLayer>();
        layerLabelMap.put(layer.getLabel(), layers);
      }
      layers.add(layer);
    }

    for (List<ILspLayer> layerSet : layerLabelMap.values()) {
      JCheckBox cb = new JCheckBox();
      Bindings.bind(cb, new CheckBoxLayerController(layerSet));

      cb.setOpaque(false);

      aBuilder.append(cb);
      aBuilder.append(new HaloLabel(layerSet.get(0).getLabel()));
      aBuilder.nextLine();
    }
  }

  private void addButtons(AbstractTheme aTheme, DefaultFormBuilder aBuilder) {
    // First we filter the theme's layers into a list of layer sets of layers
    // that have the same layers. This is necessary, because the application
    // might feature multiple views, in which case multiple layer instances
    // will be created for what is actually the same layer (hence the check
    // for layers with identical labels)

    Map<String, List<ILspLayer>> layerLabelMap = new HashMap<String, List<ILspLayer>>();
    for (ILspLayer layer : aTheme.getLayers()) {
      List<ILspLayer> layers = layerLabelMap.get(layer.getLabel());
      if (layers == null) {
        layers = new ArrayList<ILspLayer>();
        layerLabelMap.put(layer.getLabel(), layers);
      }
      layers.add(layer);
    }

    for (final List<ILspLayer> layerSet : layerLabelMap.values()) {
      JButton button = new JButton();
      button.setText(layerSet.get(0).getLabel());
      button.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
          boolean visible = layerSet.get(0).isVisible();
          for (ILspLayer layer : layerSet) {
            layer.setVisible(!visible);
          }

        }
      });

      aBuilder.append(button);
      aBuilder.nextLine();
    }
  }

  public static boolean isTouchUI() {
    return Boolean.parseBoolean(Framework.getInstance().getProperty("ui.touch", "false"));
  }
}
