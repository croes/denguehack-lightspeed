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
package samples.lucy.frontend.mapcentric.previewer;

import static samples.lucy.frontend.mapcentric.previewer.MouseRollOverTracker.HideableChildrenPanel;
import static samples.lucy.frontend.mapcentric.previewer.MouseRollOverTracker.install;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.beans.Customizer;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;

import com.luciad.gui.ALcdAction;
import com.luciad.gui.ILcdAction;
import com.luciad.gui.swing.TLcdOverlayLayout;
import samples.common.UIColors;
import samples.common.gui.Hyperlink;
import samples.common.gui.LookAndFeelChangeListener;
import samples.lucy.frontend.mapcentric.MapCentricFrontendMain;
import samples.lucy.frontend.mapcentric.gui.RelativePreferredSizePanel;
import com.luciad.lucy.ILcyLucyEnv;
import com.luciad.lucy.addons.previewer.TLcyPreviewAddOn;
import com.luciad.lucy.addons.previewer.view.TLcyPreviewAddOnCustomizerFactory;
import com.luciad.lucy.map.ILcyGenericMapComponent;
import com.luciad.lucy.util.language.TLcyLang;
import com.luciad.lucy.util.properties.ALcyProperties;
import com.luciad.lucy.util.properties.codec.TLcyStringPropertiesCodec;

/**
 * Customization of the regular GUI factory that:
 * - Hides some of the more advanced widgets such as the time range slider.
 * - Inserts the widgets in the map overlay panel, instead of in a dedicated pane.
 * - The preview controls are only available when relevant, so when time related data is loaded.
 *
 * This class is specified to be used in the previewer configuration file.
 */
public class PreviewerOnMapCustomizerFactory extends TLcyPreviewAddOnCustomizerFactory {
  private JPanel fOnMapControls;

  private JPanel fTimeLabelsPanel; // Contains labels with current time, and time range.

  private JPanel fPlaybackPanel; // Contains buttons for play and stop.
  private JPanel fTimeSeekPanel; // Contains buttons to seek forward/backward in time.
  private JPanel fTimeRangePanel; // Contains button to pick time range
  private HideableChildrenPanel fButtonBar; // Combination of the three above panels.

  @Override
  public Customizer createPreviewAddOnCustomizer() {
    initPanels();

    JPanel controls = fOnMapControls; // below call to super calls finalizeCreation which nulls fOnMapControls
    Customizer delegate = super.createPreviewAddOnCustomizer();

    return new CustomizerWrapper(delegate, controls, getPreviewAddOn(), getLucyEnv());
  }

  private void initPanels() {
    fPlaybackPanel = new JPanel();
    fPlaybackPanel.setLayout(new BoxLayout(fPlaybackPanel, BoxLayout.X_AXIS));

    fTimeSeekPanel = new JPanel();
    fTimeSeekPanel.setLayout(new BoxLayout(fTimeSeekPanel, BoxLayout.X_AXIS));

    fTimeRangePanel = new JPanel();
    fTimeRangePanel.setLayout(new BoxLayout(fTimeRangePanel, BoxLayout.X_AXIS));

    fOnMapControls = new RelativePreferredSizePanel(MapCentricFrontendMain.TIME_CONTROLS_WIDTH, 1);

    final JComponent finalOnMap = fOnMapControls;
    fButtonBar = new HideableChildrenPanel() {
      @Override
      public void setPaintChildren(boolean aPaintChildren) {
        super.setPaintChildren(aPaintChildren);
        // Show or hide the partially transparent background of the fOnMapControls
        // Copy fOnMapControls in a variable as it is set to null later on.
        finalOnMap.setOpaque(aPaintChildren);
        finalOnMap.repaint();
      }
    };
    fButtonBar.setLayout(new BoxLayout(fButtonBar, BoxLayout.X_AXIS));
    fButtonBar.add(fPlaybackPanel);
    fButtonBar.add(Box.createHorizontalStrut(20));
    fButtonBar.add(fTimeSeekPanel);
    fButtonBar.add(Box.createHorizontalStrut(20));
    fButtonBar.add(fTimeRangePanel);

    fTimeLabelsPanel = new JPanel();
    fTimeLabelsPanel.setLayout(new BoxLayout(fTimeLabelsPanel, BoxLayout.X_AXIS));

    fOnMapControls.setLayout(new BoxLayout(fOnMapControls, BoxLayout.Y_AXIS));
    fOnMapControls.add(fTimeLabelsPanel);
    fOnMapControls.add(Box.createVerticalStrut(5));
    fOnMapControls.add(fButtonBar);
    fOnMapControls.setBackground(UIColors.alpha(UIColors.bg(), 80));
    fOnMapControls.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
  }

  @Override
  protected void finalizeCreation(final Container aPreviewContainer) {
    super.finalizeCreation(aPreviewContainer);

    makeTransparent(fOnMapControls);
    install(fOnMapControls, fButtonBar);

    fOnMapControls = null;
    fTimeLabelsPanel = null;
    fPlaybackPanel = null;
    fTimeSeekPanel = null;
    fTimeRangePanel = null;
    fButtonBar = null;
  }

  private static void makeTransparent(Component c) {
    // Traverse the component tree and make all components transparent
    if (c instanceof JComponent) {
      ((JComponent) c).setOpaque(false);

      // Avoid combo boxes that are both transparent and editable, as they don't look nice
      if (c instanceof JComboBox) {
        ((JComboBox) c).setEditable(false);
      }
    }

    if (c instanceof Container) {
      for (Component child : ((Container) c).getComponents()) {
        makeTransparent(child);
      }
    }
  }

  @Override
  protected ILcdAction createAction(int aID, Container aPreviewPanel) {
    if (aID == SET_CURRENT_TIME_ACTION) {
      return null; // Use SET_CURRENT_TIME_NAMED_ACTION instead of this one. See insertAction method.
    } else {
      return super.createAction(aID, aPreviewPanel);
    }
  }

  @Override
  protected void insertAction(int aID, ILcdAction aAction, Container aPreviewPanelSFCT) {
    if (aAction == null) {
      return;
    }

    if (aID == SET_RANGE_BEGIN_TIME_ACTION ||
        aID == SET_RANGE_END_TIME_ACTION ||
        aID == SET_CURRENT_TIME_NAMED_ACTION) {
      // Using hyperlinks for the actions instead of the buttons provided by the super class.
      JComponent link = new Hyperlink(aAction);
      fTimeLabelsPanel.add(link);

      // Let current time appear more prominent.
      if (aID == SET_CURRENT_TIME_NAMED_ACTION) {
        link.setFont(link.getFont().deriveFont((float) link.getFont().getSize() + 3));
      }
      // Add spacers except after the last action.
      if (aID != SET_RANGE_END_TIME_ACTION) {
        fTimeLabelsPanel.add(Box.createHorizontalGlue());
      }
    } else {
      super.insertAction(aID, aAction, aPreviewPanelSFCT);
    }
  }

  @Override
  protected Component createComponent(int aID, Container aPreviewPanel) {
    // We don't need all the available components
    if (aID == STATUS_LABEL_COMPONENT ||
        aID == CPU_USAGE_COMPONENT ||
        aID == CURRENT_TIME_LABEL_COMPONENT ||
        aID == TIME_RANGE_SLIDER_COMPONENT ||
        aID == BEGIN_TIME_LABEL_COMPONENT ||
        aID == END_TIME_LABEL_COMPONENT) {
      return null;
    } else {
      return super.createComponent(aID, aPreviewPanel);
    }
  }

  @Override
  protected void insertComponent(int aID, Component aComponent, Container aPreviewPanelSFCT) {
    if (aComponent != null) {
      JComponent target;
      if (aID == PLAYBACK_COMPONENT || aID == TIME_FACTOR_COMPONENT) {
        target = fPlaybackPanel;
      } else if (aID == TIME_SEEK_COMPONENT) {
        target = fTimeSeekPanel;
      } else if (aID == TIME_RANGE_PICKER_COMPONENT) {
        target = fTimeRangePanel;
      } else {
        target = fOnMapControls;
      }
      if (aID == TIME_SLIDER_COMPONENT) {
        target.add(aComponent, 0); // Put the slider above the button bar
      } else {
        target.add(aComponent);
      }
    }
  }

  private static JComponent createNotApplicableMessage(TLcyPreviewAddOn aAddOn) {
    try {
      ALcyProperties props = new TLcyStringPropertiesCodec().decode(aAddOn.getConfigSourceName());
      String untranslatedMessage = props.getString("TLcyPreviewAddOn.previewerNotApplicableMessage", "");
      String messageText = TLcyLang.getString(untranslatedMessage);

      // Wrapping the message in a (disabled) hyperlink here so that it looks similar to
      // the other UI elements.
      ALcdAction messageAction = new ALcdAction(messageText) {
        @Override
        public void actionPerformed(ActionEvent e) {
        }
      };
      messageAction.setEnabled(false);
      return new Hyperlink(messageAction);

    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Responsible for adding and removing the preview controls to and from the map.
   * When the time controls are not visible because there is no time related data, a message is
   * displayed instead. That message is empty by default, but it can be configured.
   *
   * Whenever the active map component changes, or when the simulator models change (= time related data),
   * it re-evaluates if the preview controls should be available or not.
   */
  private static class ControlsAdder {
    private final Component fControls;
    private final JComponent fNotApplicableMessage;
    private final ILcyLucyEnv fLucyEnv;
    private final TLcyPreviewAddOn fAddOn;
    private final PropertyChangeListener fListener;
    private final PropertyChangeListener fActiveMapListener;

    public ControlsAdder(Component aControls, TLcyPreviewAddOn aAddOn, ILcyLucyEnv aLucyEnv) {
      fAddOn = aAddOn;
      fControls = aControls;
      fLucyEnv = aLucyEnv;
      fNotApplicableMessage = createNotApplicableMessage(aAddOn);

      fListener = new PropertyChangeListener() {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
          if ("beginDate".equals(evt.getPropertyName()) ||
              "endDate".equals(evt.getPropertyName())) {
            updatePanelAvailability();
          }
        }
      };
      fAddOn.addPropertyChangeListener(fListener);

      fActiveMapListener = new PropertyChangeListener() {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
          if ("activeMapComponent".equals(evt.getPropertyName())) {
            updatePanelAvailability();
          }
        }
      };
      fLucyEnv.getCombinedMapManager().addPropertyChangeListener(fActiveMapListener);

      updatePanelAvailability();
    }

    public void cleanup() {
      fAddOn.removePropertyChangeListener(fListener);
      fLucyEnv.getCombinedMapManager().removePropertyChangeListener(fActiveMapListener);
      moveToParent(fControls, null);
      moveToParent(fNotApplicableMessage, null);
    }

    private void updatePanelAvailability() {
      Container parentForControls = null;
      Container parentForMessage = null;

      // Add to new location if applicable
      ILcyGenericMapComponent activeMap = fLucyEnv.getCombinedMapManager().getActiveMapComponent();
      if (activeMap != null) {
        // Only show the controls if there is a non-empty time range
        if (fAddOn.getEndDate() != null && fAddOn.getEndDate().after(fAddOn.getBeginDate())) {
          parentForControls = activeMap.getMapOverlayPanel();
        }
        // Show a message otherwise
        else {
          parentForMessage = activeMap.getMapOverlayPanel();
        }
      }

      moveToParent(fControls, parentForControls);
      moveToParent(fNotApplicableMessage, parentForMessage);
    }

    private void moveToParent(Component aChild, Container aNewParent) {
      Container currentParent = aChild.getParent();
      if (currentParent != aNewParent) {
        if (currentParent != null) {
          currentParent.remove(aChild);
          revalidateAndRepaint(currentParent);
        }
        if (aNewParent != null) {
          aNewParent.add(aChild, TLcdOverlayLayout.Location.NORTH);
          revalidateAndRepaint(aNewParent);
        }
      }
    }
  }

  private static void revalidateAndRepaint(Container aParent) {
    // Whenever a component is added to or removed from an already visible parent, Swing requires to call these methods.
    aParent.revalidate();
    aParent.repaint();
  }

  /**
   * Wraps the customizer that is created by the super-class. The bean that it customizes
   * is of type TLcyPreviewAddOn. When it is set to null, clean-up code can be performed.
   */
  private static class CustomizerWrapper extends JPanel implements Customizer {
    private final Customizer fDelegate;
    private final Component fControls;
    private final ILcyLucyEnv fLucyEnv;
    private ControlsAdder fControlsAdder;

    public CustomizerWrapper(Customizer aDelegate, Component aControls, TLcyPreviewAddOn aAddOn, ILcyLucyEnv aLucyEnv) {
      fDelegate = aDelegate;
      fControls = aControls;
      fLucyEnv = aLucyEnv;
      add((Component) aDelegate); // Cast is documented in Customizer interface

      // The controls are added and removed, so make sure the L&F is kept up to date
      LookAndFeelChangeListener.install(aControls);

      fControlsAdder = new ControlsAdder(aControls, aAddOn, aLucyEnv);
    }

    @Override
    public void setObject(Object bean) {
      if (fControlsAdder != null) {
        fControlsAdder.cleanup();
        fControlsAdder = null;
      }

      fDelegate.setObject(bean);

      if (bean != null) {
        fControlsAdder = new ControlsAdder(fControls, (TLcyPreviewAddOn) bean, fLucyEnv);
      }
    }
  }
}
