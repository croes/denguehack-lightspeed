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
package samples.gxy.touch.editing;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;

import com.luciad.gui.TLcdHaloIcon;
import com.luciad.gui.TLcdImageIcon;
import com.luciad.gui.swing.TLcdOverlayLayout;
import com.luciad.gui.swing.TLcdSWIcon;
import com.luciad.view.gxy.ILcdGXYView;
import com.luciad.view.gxy.controller.TLcdGXYMultiPointEditControllerModel;
import com.luciad.view.gxy.controller.TLcdGXYSelectControllerModel2;
import com.luciad.view.gxy.controller.touch.TLcdGXYTouchSelectEditController;

import samples.gxy.common.toolbar.ToolBar;

/**
 * <p>This class inherits from TLcdGXYTouchSelectEditController, and uses some buttons added over the
 * map to modify behaviour. The methods {@link #selectHowMode(Rectangle, int, int)} and
 * {@link #editHowMode(List, List)} are overridden and return a value based on the
 * state of the buttons. The buttons allow you to modify as follows.</p>
 *
 * <ul>
 *
 * <li> The first button toggles between selecting by touching objects, and selecting by dragging
 * a rectangle around objects. </li>
 *
 * <li> The second button describes what will happen to objects that are interacted with, either
 * select them and nothing else, add them to the selection, or remove them from the selection.</li>
 *
 * <li> The third button toggles between showing a choose pop-up or not when selecting. When the pop-up
 * is shown, the user can choose which of the possible objects will be interacted with. </li>
 *
 * <li> The last button switches the edit behaviour between translating objects and reshaping objects.
 * The precise meaning of reshaping is defined by the editors for the edited object.</li>
 *
 * </ul>
 */

public class TouchSelectEditController extends TLcdGXYTouchSelectEditController {

  private Container fContainer;
  private MyButtonPanel fButtonPanel = new MyButtonPanel();
  private boolean fChoose = false;

  private int fSelectHow = TLcdGXYSelectControllerModel2.SELECT_HOW_FIRST_TOUCHED;
  private boolean fReshaping = false;

  /**
   * Creates a TouchSelectEditController.
   *
   * @param aContainer the container to which the panel with modifier buttons will be added.
   * @param aNbOfEditPoints the number of touches that will be taken into account for editing.
   * @param aEditingEnabled indicates whether or not this controller will be used for editing,
   * the reshape/translate button will only be added when it is.
   */
  public TouchSelectEditController(Container aContainer, int aNbOfEditPoints, boolean aEditingEnabled) {
    super(new ToolBar.SelectControllerModelWithDialog(), new TLcdGXYMultiPointEditControllerModel(), aNbOfEditPoints);
    fContainer = aContainer;
    fButtonPanel = new MyButtonPanel(aEditingEnabled);
    getSelectControllerModel().setSensitivity(15);
    getEditControllerModel().setSensitivity(15);
  }

  /**
   * Creates a TouchSelectEditController.
   *
   * @param aContainer the container to which the panel with modifier buttons will be added.
   * @param aNbOfEditPoints the number of touches that will be taken into account for editing.
   */
  public TouchSelectEditController(Container aContainer, int aNbOfEditPoints) {
    this(aContainer, aNbOfEditPoints, true);
  }

  /**
   * Creates a TouchSelectEditController.
   *
   * @param aContainer                 the container to which the panel with modifier buttons will be added.
   * @param aSelectControllerModel     the selection model. Must not be <code>null</code>.
   * @param aEditControllerModel       the edit model. May be <code>null</code>. When set to
   *                                   <code>null</code>, this controller will no longer try to edit
   *                                   and only perform selection.
   * @param aMaximalNumberOfEditPoints the number of touches that will be taken into account for editing.
   */
  public TouchSelectEditController(Container aContainer,
                                   TLcdGXYSelectControllerModel2 aSelectControllerModel,
                                   TLcdGXYMultiPointEditControllerModel aEditControllerModel,
                                   int aMaximalNumberOfEditPoints) {
    super(aSelectControllerModel, aEditControllerModel, aMaximalNumberOfEditPoints);
    fContainer = aContainer;
    fButtonPanel = new MyButtonPanel(true);
    getSelectControllerModel().setSensitivity(15);
    getEditControllerModel().setSensitivity(15);
  }

  @Override
  protected void startInteractionImpl(ILcdGXYView aGXYView) {
    super.startInteractionImpl(aGXYView);
    if (fContainer != null) {
      fContainer.add(fButtonPanel, TLcdOverlayLayout.Location.NORTH_WEST);
      revalidateContainer();
    }
  }

  @Override
  protected void terminateInteractionImpl(ILcdGXYView aGXYView) {
    super.terminateInteractionImpl(aGXYView);
    if (fContainer != null) {
      fContainer.remove(fButtonPanel);
      revalidateContainer();
    }
  }

  private void revalidateContainer() {
    if (fContainer instanceof JComponent) {
      ((JComponent) fContainer).revalidate();
    } else {
      fContainer.invalidate();
      fContainer.validate();
    }
    fContainer.repaint();
  }

  private void setSelectHowImpl(int aMode) {
    switch (aMode) {
    case 0:
      fSelectHow = TLcdGXYSelectControllerModel2.SELECT_HOW_FIRST_TOUCHED;
      break;
    case 1:
      fSelectHow = TLcdGXYSelectControllerModel2.SELECT_HOW_ADD;
      break;
    case 2:
      fSelectHow = TLcdGXYSelectControllerModel2.SELECT_HOW_REMOVE;
      break;
    default:
      fSelectHow = TLcdGXYSelectControllerModel2.SELECT_HOW_FIRST_TOUCHED;
      break;
    }
    if (fChoose) {
      fSelectHow = fSelectHow | TLcdGXYSelectControllerModel2.SELECT_HOW_CHOOSE;
    }
  }

  @Override
  protected int selectHowMode(Rectangle aSelectionBounds, int aInputMode, int aSelectByWhatMode) {
    return fSelectHow;
  }

  @Override
  protected int editHowMode(List<Point> aFrom, List<Point> aTo) {
    return fReshaping ? TLcdGXYMultiPointEditControllerModel.EDIT_HOW_RESHAPING : TLcdGXYMultiPointEditControllerModel.EDIT_HOW_TRANSLATING;
  }

  private class MyButtonPanel extends JPanel {

    private MyButtonPanel() {
      this(true);
    }

    public MyButtonPanel(boolean aEditingEnabled) {
      init(aEditingEnabled);
    }

    private void init(boolean aEditingEnabled) {
      setLayout(new BorderLayout());

      final MyModeButton dragButton = new MyModeButton(new String[]{"images/gui/touchicons/tap_64.png",
                                                                    "images/gui/touchicons/drag_64.png"});
      dragButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          dragButton.incrementMode();
          setDragRectangle(dragButton.getMode() == 1);
        }
      });

      final MyModeButton reshapeButton = new MyModeButton(new String[]{"images/gui/touchicons/translate_64.png",
                                                                       "images/gui/touchicons/reshape_64.png"});
      if (aEditingEnabled) {
        reshapeButton.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            reshapeButton.incrementMode();
            fReshaping = reshapeButton.getMode() == 1;
          }
        });
      }

      final MyModeButton selectHowButton = new MyModeButton(new String[]{"images/gui/touchicons/selecttouch_64.png",
                                                                         "images/gui/touchicons/selectplus_64.png",
                                                                         "images/gui/touchicons/selectmin_64.png"});
      selectHowButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          selectHowButton.incrementMode();
          setSelectHowImpl(selectHowButton.fMode);
        }
      });

      final MyModeButton chooseButton = new MyModeButton(new String[]{"images/gui/touchicons/nochoose_64.png",
                                                                      "images/gui/touchicons/choose_64.png"});
      chooseButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          chooseButton.incrementMode();
          fChoose = chooseButton.getMode() == 1;
          setSelectHowImpl(selectHowButton.fMode);
        }
      });

      JPanel content = new JPanel();
      content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
      content.add(dragButton);
      content.add(selectHowButton);
      content.add(chooseButton);
      if (aEditingEnabled) {
        content.add(reshapeButton);
      }

      setOpaque(false, content);
      add(content, BorderLayout.CENTER);

      setOpaque(false);
    }

    private void setOpaque(boolean aOpaque, Component aComponent) {
      if (aComponent instanceof JComponent) {
        JComponent jComponent = (JComponent) aComponent;
        jComponent.setOpaque(aOpaque);

        for (Component child : jComponent.getComponents()) {
          setOpaque(aOpaque, child);
        }
      }
    }
  }

  // Transparent button that cycles between modes.
  public static class MyModeButton extends JButton {

    private int fMode = 0;
    private Icon[] fIconArray;

    public MyModeButton(String[] aPathArray) {
      try {
        fIconArray = createIconArray(aPathArray);
        Dimension size = new Dimension(fIconArray[0].getIconWidth(), fIconArray[1].getIconHeight());
        setPreferredSize(size);
        setMaximumSize(size);
        setMinimumSize(size);
      } catch (IOException e) {
        throw new IllegalArgumentException("One or more of the images can't be found!");
      }
    }

    private Icon[] createIconArray(String[] aPathArray) throws IOException {
      Icon[] icons = new Icon[aPathArray.length];
      for (int i = 0; i < aPathArray.length; i++) {
        TLcdImageIcon icon = new TLcdImageIcon(aPathArray[i]);
        TLcdHaloIcon haloIcon = new TLcdHaloIcon(icon);
        icons[i] = new TLcdSWIcon(haloIcon);
      }
      return icons;
    }

    public int getMode() {
      return fMode;
    }

    public void incrementMode() {
      if (fMode == fIconArray.length - 1) {
        fMode = 0;
      } else {
        fMode += 1;
      }
    }

    @Override
    protected void paintComponent(Graphics g) {
      fIconArray[fMode].paintIcon(this, g, 0, 0);
    }
  }
}
