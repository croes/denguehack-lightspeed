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
package samples.common;

import java.awt.Component;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;

import com.luciad.gui.ILcdAction;
import com.luciad.gui.TLcdIconImageUtil;
import com.luciad.gui.swing.TLcdGreyIcon;
import com.luciad.gui.swing.TLcdSWAction;
import com.luciad.gui.swing.TLcdSWIcon;

/**
 * Swing related utility class.
 */
public class SwingUtil {

  public static final List<Image> sLuciadFrameImage;

  private static final Insets BUTTON_MARGINS = new Insets(2, 2, 2, 2);

  protected SwingUtil() {
  }

   // find the Luciad icon.
  static {
    TLcdIconImageUtil util = new TLcdIconImageUtil();
    List<Image> images = new ArrayList<>();
    images.add(util.getImage("images/luciad_icon16.png"));
    images.add(util.getImage("images/luciad_icon32.png"));
    images.add(util.getImage("images/luciad_icon64.png"));
    images.add(util.getImage("images/luciad_icon128.png"));
    sLuciadFrameImage = images;
  }

  /**
   * Makes the given button have equal margins at all sides. That means that if the content is
   * square, the button will be square. A typical use case is a button with a square icon.
   *
   * This is not needed for buttons that are part of a JToolBar, where this is taken care of by
   * Swing.
   *
   * @param aButton The button.
   */
  public static void makeSquare(AbstractButton aButton) {
    makeSquare(aButton, null);
  }

  /**
   * Idem. The given margins are used though.
   *
   * @param aButton The button.
   */
  public static void makeSquare(AbstractButton aButton, Insets aOptionalInsets) {
    // Nimbus look and feel: overrule the content margins with those for tool bar buttons.
    // Other look & feels will simply ignore this.
    UIDefaults overrides = new UIDefaults();
    overrides.put("Button.contentMargins", aOptionalInsets != null ? aOptionalInsets :
                                           UIManager.get("ToolBar:Button.contentMargins"));
    overrides.put("ToggleButton.contentMargins", aOptionalInsets != null ? aOptionalInsets :
                                                 UIManager.get("ToolBar:ToggleButton.contentMargins"));
    aButton.putClientProperty("Nimbus.Overrides", overrides);

    // Other look and feels: set hard coded square margins
    aButton.setMargin(aOptionalInsets != null ? aOptionalInsets : BUTTON_MARGINS);
  }

  public static void makeTripleDotButton(AbstractButton aButton) {
    aButton.setText(" ... ");
    makeSquare(aButton);
  }

  /**
   * Makes a toolbar less conspicuous i.e.<!-- --> more like a regular JPanel, so that it integrates
   * more nicely with other components.
   * @param aToolBar the toolbar to change
   */
  public static void makeFlat(JToolBar aToolBar) {
    aToolBar.setFloatable(false);
    aToolBar.setOpaque(false);
    aToolBar.setBorderPainted(false);
  }

  /**
   * Creates a button linked to the given action.
   * @param aParent the parent of the button. Used to configure accelerator keys.
   * @param aAction the action that should be executed when pressing the button.
   * @param aToggle whether or not a toggle button should be created
   */
  public static AbstractButton createButtonForAction(JComponent aParent, ILcdAction aAction, boolean aToggle) {
    // Create the button
    final AbstractButton button = aToggle ? new JToggleButton() : new JButton();
    button.setMargin(new Insets(2, 2, 2, 2));

    // Create an adapter from ILcdAction to javax.swing.Action
    TLcdSWAction swing_action = new TLcdSWAction(aAction);

    // Set the action to the button. This also configures the icon, tool tip, etc.
    button.setAction(swing_action);
    // we don't want the text of the action to clutter the button by default
    if (Boolean.TRUE.equals(aAction.getValue(ILcdAction.SHOW_ACTION_NAME))) {
      String name = (String) aAction.getValue(Action.NAME);
      button.setText(name == null ? "" : name);
    } else {
      button.setText("");
    }

    //Set a disabled icon
    Icon icon = (Icon) swing_action.getValue(Action.SMALL_ICON);
    if (icon != null) {
      button.setDisabledIcon(new TLcdSWIcon(new TLcdGreyIcon(icon)));
    }
    //Add a keyboard shortcut if applicable.
    KeyStroke acceleratorKey = (KeyStroke) swing_action.getValue(Action.ACCELERATOR_KEY);
    if (acceleratorKey != null) {
      aParent.getActionMap().put(aAction, swing_action);
      aParent.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(acceleratorKey, aAction);
    }
    //Hide the button if needed
    Boolean visible = (Boolean) swing_action.getValue(ILcdAction.VISIBLE);
    if ( visible != null) {
      button.setVisible(visible);
    }
    swing_action.addPropertyChangeListener(new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        if (ILcdAction.VISIBLE.equals(evt.getPropertyName())) {
          button.setVisible((Boolean) evt.getNewValue());
        }
      }
    });
    return button;
  }

  /**
   * @param aComponent a component that is being displayed
   *
   * @return the graphics device for the screen displaying the given component
   */
  public static GraphicsDevice getScreenDevice(Component aComponent) {
    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
    GraphicsDevice[] gd = ge.getScreenDevices();
    for (GraphicsDevice device : gd) {
      GraphicsConfiguration gc = device.getDefaultConfiguration();
      Rectangle r = gc.getBounds();
      Rectangle frameBounds = SwingUtilities.getRoot(aComponent).getBounds();
      Point center = new Point(frameBounds.getLocation().x + frameBounds.getSize().width / 2,
                               frameBounds.getLocation().y + frameBounds.getSize().height / 2);
      if (r.contains(center)) {
        return device;
      }
    }
    return ge.getDefaultScreenDevice();
  }

}
