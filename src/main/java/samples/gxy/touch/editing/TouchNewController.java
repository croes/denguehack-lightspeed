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

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;

import com.luciad.gui.ILcdIcon;
import com.luciad.gui.TLcdImageIcon;
import com.luciad.gui.swing.TLcdGreyIcon;
import com.luciad.gui.swing.TLcdOverlayLayout;
import com.luciad.gui.swing.TLcdSWIcon;
import com.luciad.view.gxy.ILcdGXYView;
import com.luciad.view.gxy.controller.ALcdGXYNewControllerModel2;
import com.luciad.view.gxy.controller.touch.TLcdGXYTouchNewController;

/**
 * This TLcdGXYTouchNewController adds buttons over the view to enable the necessary actions: from
 * top to bottom: undo, redo, commit and cancel.
 */

public class TouchNewController extends TLcdGXYTouchNewController {

  private Container fContainer;
  private TLcdOverlayLayout.Location fLocation;
  private MyButtonPanel fEditPanel = new MyButtonPanel();

  public TouchNewController(ALcdGXYNewControllerModel2 aNewControllerModel, Container aContainer,
                            TLcdOverlayLayout.Location aLocation) {
    super(aNewControllerModel);
    fContainer = aContainer;
    fLocation = aLocation;
  }

  public TouchNewController(ALcdGXYNewControllerModel2 aNewControllerModel, Container aContainer) {
    this(aNewControllerModel, aContainer, TLcdOverlayLayout.Location.NORTH_WEST);
  }

  @Override
  protected void startInteractionImpl(ILcdGXYView aGXYView) {
    super.startInteractionImpl(aGXYView);
    if (fContainer != null) {
      fContainer.add(fEditPanel, fLocation);
      revalidateContainer();
    }
  }

  @Override
  protected void terminateInteractionImpl(ILcdGXYView aGXYView) {
    super.terminateInteractionImpl(aGXYView);
    if (fContainer != null) {
      fContainer.remove(fEditPanel);
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

  private void updatePanel() {
    fEditPanel.update(canCommit(), canCancel(), canUndo(), canRedo());
  }

  @Override
  public void handleAWTEvent(AWTEvent aEvent) {
    super.handleAWTEvent(aEvent);
    //Possibly the state of the object is changed
    updatePanel();
  }

  private class MyButtonPanel extends JPanel {

    private JButton fCommitButton;
    private JButton fCancelButton;
    private JButton fUndoButton;
    private JButton fRedoButton;
    private JPanel fContent;

    private MyButtonPanel() {
      init();
    }

    private void init() {
      setLayout(new BorderLayout());
      fCommitButton = new MyActionButton("images/gui/touchicons/commit_64.png");
      fCommitButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          doCommit();
          updatePanel();
        }
      });

      fCancelButton = new MyActionButton("images/gui/touchicons/cancel_64.png");
      fCancelButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          doCancel();
          updatePanel();
        }
      });

      fUndoButton = new MyActionButton("images/gui/touchicons/undo_64.png");
      fUndoButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          doUndo();
          updatePanel();
        }
      });

      fRedoButton = new MyActionButton("images/gui/touchicons/redo_64.png");
      fRedoButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          doRedo();
          updatePanel();
        }
      });

      fContent = new JPanel();
      fContent.setLayout(new BoxLayout(fContent, BoxLayout.Y_AXIS));

      setOpaque(false, fContent);
      add(fContent, BorderLayout.CENTER);

      fContent.add(fUndoButton);
      fContent.add(fRedoButton);
      fContent.add(fCommitButton);
      fContent.add(fCancelButton);
      update(canCommit(), canCancel(), canUndo(), canRedo());

      setOpaque(false);
      setVisible(true);
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

    public void update(boolean aCommit, boolean aCancel, boolean aUndo, boolean aRedo) {
      fCommitButton.setEnabled(aCommit);
      fCancelButton.setEnabled(aCancel);
      fUndoButton.setEnabled(aUndo);
      fRedoButton.setEnabled(aRedo);
    }
  }

  public static class MyActionButton extends JButton {

    private static Color SELECT_COLOR = new Color(255, 255, 255, 100);
    private Dimension fDimension;

    public MyActionButton(String aActivePath) {
      TLcdImageIcon icon = new TLcdImageIcon(aActivePath);
      Icon sw_icon = new TLcdSWIcon(icon);
      fDimension = new Dimension(sw_icon.getIconWidth(), sw_icon.getIconHeight());
      setIcon(sw_icon);
      ILcdIcon greyIcon = new TLcdGreyIcon(sw_icon);
      setDisabledIcon(new TLcdSWIcon(greyIcon));
      setRolloverEnabled(false);
      setFocusable(false);
      setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
      if (getModel().isPressed()) {
        g.setColor(SELECT_COLOR);
        g.fillRect(1, 1, this.getWidth() - 3, this.getHeight() - 3);
      }
      if (isEnabled()) {
        getIcon().paintIcon(this, g, 0, 0);
      } else {
        getDisabledIcon().paintIcon(this, g, 0, 0);
      }
    }

    @Override
    public Dimension getPreferredSize() {
      return fDimension;
    }
  }
}
