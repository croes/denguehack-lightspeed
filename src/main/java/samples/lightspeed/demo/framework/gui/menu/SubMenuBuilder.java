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
package samples.lightspeed.demo.framework.gui.menu;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import com.jgoodies.binding.adapter.Bindings;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.ConstantSize;
import com.jgoodies.forms.layout.FormLayout;
import com.luciad.gui.TLcdAWTUtil;
import com.luciad.util.collections.TLcdIdentityHashSet;
import com.luciad.view.ILcdLayered;
import com.luciad.view.ILcdXYZWorldReference;
import com.luciad.view.lightspeed.ILspAWTView;
import com.luciad.view.lightspeed.camera.TLspViewXYZWorldTransformation3D;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.util.TLspViewNavigationUtil;

import samples.common.HaloLabel;
import samples.lightspeed.common.ProjectionSupport;
import samples.lightspeed.demo.framework.application.Framework;
import samples.lightspeed.demo.framework.data.themes.AbstractTheme;
import samples.lightspeed.demo.framework.gui.DemoUIColors;

/**
 * This class represents a factory for building submenus. When using this class, the user should
 * first
 * configure it by setting a main menu, a view, an application and an action listener. These
 * attributes
 * will then be used (or altered as a side effect) when creating the submenus through the build
 * methods.
 */
public class SubMenuBuilder {

  private SlideMenu fParentMenu;
  private ILspAWTView fAWTView;

  public SubMenuBuilder(SlideMenu aParentMenu, ILspAWTView aAWTView) {
    setParentMenu(aParentMenu);
    setAWTView(aAWTView);
  }

  public void setParentMenu(SlideMenu aParentMenu) {
    if (aParentMenu != null) {
      fParentMenu = aParentMenu;
    } else {
      throw new IllegalArgumentException("Could not configure submenu builder, reason: parent menu can not be null!");
    }
  }

  public void setAWTView(ILspAWTView aAWTView) {
    if (aAWTView != null) {
      fAWTView = aAWTView;
    } else {
      throw new IllegalArgumentException("Could not configure submenu builder, reason: view can not be null!");
    }
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////

  public SlideMenu buildProjectionSubMenu(boolean aTouchUI) {

    // Create content of submenu
    final ProjectionSupport support = new ProjectionSupport(fAWTView);
    final double centerLon = Double.parseDouble(Framework.getInstance().getProperty("projection.center.lon", "0"));
    final double centerLat = Double.parseDouble(Framework.getInstance().getProperty("projection.center.lat", "0"));

    DefaultFormBuilder builder;
    if (aTouchUI) {
      builder = new DefaultFormBuilder(new FormLayout("p"));
      builder.lineGapSize(new ConstantSize(5, ConstantSize.DLUY));
    } else {
      builder = new DefaultFormBuilder(new FormLayout("p,5dlu,p"));
    }
    builder.border(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    ILcdXYZWorldReference worldReference = fAWTView.getXYZWorldReference();
    boolean is3d = fAWTView.getViewXYZWorldTransformation() instanceof TLspViewXYZWorldTransformation3D;
    final String activeName = support.toString(worldReference, is3d);

    Map<String, String> names = new LinkedHashMap<String, String>();
    names.put("Geocentric", "3D - Geocentric");
    names.put("Equidistant Cylindrical", "2D - Equidistant Cylindrical");
    names.put("Miller Cylindrical", "2D - Miller Cylindrical");
    names.put("Mercator", "2D - Mercator");
    names.put("Mollweide", "2D - Mollweide");
    names.put("Eckert IV", "2D - Eckert IV");
    names.put("Stereographic", "2D - Stereographic");
    names.put("Polar Stereographic (North)", "2D - Polar Stereographic");

    final String main2DProjection = "Equidistant Cylindrical";

    ButtonGroup group = new ButtonGroup();

    if (!aTouchUI) {
      for (final String name : names.keySet()) {
        JRadioButton radioButton = new JRadioButton();
        group.add(radioButton);
        radioButton.addActionListener(new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent e) {
            if (name.equals(main2DProjection)) {
              support.setProjection(name);
            } else {
              support.setProjection(name, centerLon, centerLat);
            }
          }
        });
        radioButton.setOpaque(false);
        if (name.equals(activeName)) {
          radioButton.setSelected(true);
        }
        builder.append(radioButton);
        builder.append(new HaloLabel(names.get(name)));

        // Add separator to indicate that First two projections are the main ones
        if (name.equals(main2DProjection)) {
          builder.appendSeparator();
        }
        builder.nextLine();

      }
    } else {

      for (final String name : names.keySet()) {
        JButton button = new JButton(names.get(name));
        button.addActionListener(new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent e) {
            if (name.equals(main2DProjection)) {
              support.setProjection(name);
            } else {
              support.setProjection(name, centerLon, centerLat);
            }
          }
        });

        builder.append(button);
        builder.nextLine();
      }
    }

    JPanel contentPanel = builder.getPanel();
    contentPanel.setSize(contentPanel.getLayout().preferredLayoutSize(contentPanel));

    // Add submenu to main menu
    return new SlideMenu(fParentMenu, contentPanel);
  }

  public SlideMenu buildLayerSubMenu(ILcdLayered aLayered, boolean aTouchUI) {

    /**
     * NOTE: this method only creates a checkbox for each layer that is already in the view. That
     *       is, if any layers were to be added after this method was called, these layers would
     *       not show up in the submenu. This can be remedied by integrating a layered listener into
     *       the GUI solution below.
     *       At the moment though, the demo application does not require this behaviour, since no
     *       options are provided to add/remove layers at runtime.
     */

    return new LayerSlideMenu(fParentMenu, fAWTView, aLayered, aTouchUI);
  }

  private static class LayerSlideMenu extends SlideMenu {
    private ILspAWTView fView;
    private ILcdLayered fLayered;
    private boolean fTouchUI;

    private LayerSlideMenu(SlideMenu aParentMenu, ILspAWTView aView, ILcdLayered aLayered, boolean aTouchUI) {
      super(aParentMenu);

      this.fView = aView;
      this.fLayered = aLayered;
      this.fTouchUI = aTouchUI;

      JPanel panel = updateContentPanel();
      setContentPanel(panel);

      Framework.getInstance().addPropertyChangeListener(new PropertyChangeListener() {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
          if (evt.getPropertyName() != null && evt.getPropertyName().equals("activeTheme")) {
            update();
          }
        }
      });
    }

    public void update() {
      setContentPanel(updateContentPanel());
      invalidate();
      repaint();
    }

    private JPanel updateContentPanel() {
      JPanel contentPanel;

      Framework framework = Framework.getInstance();
      AbstractTheme activeTheme = framework.getActiveTheme();
      TLcdIdentityHashSet<ILspLayer> themeLayers = activeTheme == null ? null : new TLcdIdentityHashSet<ILspLayer>(activeTheme.getLayers());

      if (fLayered != null) {
        DefaultFormBuilder builder = createLayerListBuilder(fTouchUI);
        // Sort layers by label, alphabetically
        List<ILspLayer> layers = new ArrayList<ILspLayer>(fLayered.layerCount());
        for (int i = 0; i < fLayered.layerCount(); i++) {
          layers.add((ILspLayer) fLayered.getLayer(i));
        }
        Collections.sort(layers, new Comparator<ILspLayer>() {
          @Override
          public int compare(ILspLayer o1, ILspLayer o2) {
            String label1 = o1.getLabel();
            String label2 = o2.getLabel();
            return label1.compareTo(label2);
          }
        });

        for (int i = 0; i < layers.size(); i++) {
          ILspLayer layer = layers.get(i);
          boolean active = (themeLayers != null && (themeLayers.contains(layer))) || framework.isBaseLayer(layer);
          final List<ILspLayer> layerList = Collections.<ILspLayer>singletonList(layer);
          if (fTouchUI) {
            addLayerToMenuForTouchUISFCT(layerList, active, builder);
          } else {
            addLayerToMenuForMouseUISFCT(layerList, fView, active, builder);
          }
        }
        contentPanel = builder.getPanel();
        contentPanel.setSize(contentPanel.getLayout().minimumLayoutSize(contentPanel));
      } else {
        Framework.getInstance().addLayerListener(new ApplicationLayersListener(fView, this, fTouchUI));
        contentPanel = createApplicationLayersPanel(fView, themeLayers, fTouchUI);
      }
      return contentPanel;
    }
  }

  private static JPanel createApplicationLayersPanel(ILspAWTView aView, Set<ILspLayer> aThemeLayers, boolean aUseTouchUI) {
    DefaultFormBuilder builder = createLayerListBuilder(aUseTouchUI);
    addApplicationLayersToPanel(aView, builder, aThemeLayers, aUseTouchUI);
    JPanel contentPanel = builder.getPanel();
    contentPanel.setSize(contentPanel.getLayout().minimumLayoutSize(contentPanel));
    return contentPanel;
  }

  private static DefaultFormBuilder createLayerListBuilder(boolean aUseTouchUI) {
    DefaultFormBuilder builder;
    if (aUseTouchUI) {
      builder = new DefaultFormBuilder(new FormLayout("p, 10dlu, p, 10dlu, p"));
      builder.lineGapSize(new ConstantSize(5, ConstantSize.DLUY));
    } else {
      builder = new DefaultFormBuilder(new FormLayout("p,5dlu,p, 10dlu, p,5dlu,p"));
    }
    builder.border(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    return builder;
  }

  private static Framework addApplicationLayersToPanel(ILspAWTView aView, DefaultFormBuilder aBuilder, Set<ILspLayer> aThemeLayers, boolean aUseTouchUI) {
    Framework app = Framework.getInstance();
    //Keep a list of all layer ids that aren't assigned to a specific theme
    Collection<String> layerIDs = app.getLayerIDs();
    List<List<ILspLayer>> layerLists = new ArrayList<List<ILspLayer>>();
    //Add all layers to the menu that don't have a specific theme
    for (String id : layerIDs) {
      try {
        List<ILspLayer> layers = app.getLayersWithID(id);
        layerLists.add(layers);
      } catch (IllegalArgumentException ignored) {
      }
    }

    Collections.sort(layerLists, new Comparator<List<ILspLayer>>() {
      @Override
      public int compare(List<ILspLayer> o1, List<ILspLayer> o2) {
        String label1 = o1.get(0).getLabel();
        String label2 = o2.get(0).getLabel();
        return label1.compareToIgnoreCase(label2);
      }
    });


    List<List<ILspLayer>> sortedList = new ArrayList<>();

    double nrOfColumns = aUseTouchUI?3.0:2.0;
    int layerCountFirstColumn = (int) Math.ceil(layerLists.size() / nrOfColumns);

    for (int i = 0; i < layerCountFirstColumn; i++) {
      for (int j = 0; j < nrOfColumns; j++) {
        if(j*layerCountFirstColumn + i <layerLists.size()){
          int index = i + (j * layerCountFirstColumn);
          sortedList.add(layerLists.get(index));
        }
      }
    }

    for (List<ILspLayer> layers : sortedList) {
      boolean active = (aThemeLayers != null && aThemeLayers.contains(layers.get(0))) || app.isBaseLayer(layers.get(0));
      if (aUseTouchUI) {
        addLayerToMenuForTouchUISFCT(layers, active, aBuilder);
      } else {
        addLayerToMenuForMouseUISFCT(layers, aView, active, aBuilder);
      }
    }
    return app;
  }

  private static void addLayerToMenuForMouseUISFCT(final List<ILspLayer> aLayers, final ILspAWTView aView, boolean aActive, DefaultFormBuilder aBuilderSFCT) {
    if (aLayers == null || aLayers.isEmpty()) {
      return;
    }
    JCheckBox box = new JCheckBox();

    Bindings.bind(box, new CheckBoxLayerController(aLayers));

    box.setOpaque(false);
    aBuilderSFCT.append(box);
    HaloLabel label = new HaloLabel(aLayers.get(0).getLabel());
    if (aActive) {
      label.setTextColor(DemoUIColors.HIGHLIGHTED_TEXT_COLOR);
    }

    label.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2) {
          try {
            new TLspViewNavigationUtil(aView).animatedFit(aLayers);
          } catch (Exception exception) {
          }
        }
      }
    });
    aBuilderSFCT.append(label);
  }

  private static void addLayerToMenuForTouchUISFCT(final List<ILspLayer> aLayers, boolean aActive, DefaultFormBuilder aBuilderSFCT) {
    if (aLayers == null || aLayers.isEmpty()) {
      return;
    }

    JButton button = new JButton(aLayers.get(0).getLabel());
    if (aActive) {
      button.setForeground(DemoUIColors.HIGHLIGHTED_TEXT_COLOR);
    }

    button.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        boolean visible = aLayers.get(0).isVisible();
        for (ILspLayer layer : aLayers) {
          layer.setVisible(!visible);
        }
      }
    });
    aBuilderSFCT.append(button);
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////

  private static final class ApplicationLayersListener implements PropertyChangeListener {
    private final SlideMenu fSlideMenu;
    private final boolean fUseTouchUI;
    private final ILspAWTView fView;

    public ApplicationLayersListener(ILspAWTView aView, SlideMenu aSlideMenu, boolean aUseTouchUI) {
      fSlideMenu = aSlideMenu;
      fUseTouchUI = aUseTouchUI;
      fView = aView;
    }

    @Override
    public void propertyChange(PropertyChangeEvent aEvt) {
      TLcdAWTUtil.invokeLater(new Runnable() {
        @Override
        public void run() {
          AbstractTheme theme = Framework.getInstance().getActiveTheme();
          TLcdIdentityHashSet<ILspLayer> themeLayers = theme == null ? null : new TLcdIdentityHashSet<ILspLayer>(theme.getLayers());
          fSlideMenu.setContentPanel(createApplicationLayersPanel(fView, themeLayers, fUseTouchUI));
          int curY = -fSlideMenu.getHeight();
          fSlideMenu.setLocation(fSlideMenu.getX(), curY);
        }
      });
    }
  }

}
