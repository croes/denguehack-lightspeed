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
package samples.tea;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import samples.tea.AbstractVisibilityAction.Dialog;

/**
 * Panel with drop-downs to specify the visibility computation.
 */
public class IntervisibilityPanel extends JPanel {

  private final AbstractVisibilityAction fAction;
  private final JComboBox<AbstractVisibilityAction.Shape> fFromBox;
  private final JComboBox<AbstractVisibilityAction.Shape> fToBox;
  private Thread fThread;

  public IntervisibilityPanel(final AbstractVisibilityAction aAction) {
    super(new GridLayout(0, 1, 5, 0));
    fAction = aAction;
    JPanel panel = new JPanel();
    add(panel);
    panel.add(new JLabel("From", JLabel.RIGHT));
    fFromBox = new JComboBox<>(AbstractVisibilityAction.Shape.values());
    fToBox = new JComboBox<>(AbstractVisibilityAction.Shape.values());
    fToBox.removeItem(AbstractVisibilityAction.Shape.POINT);
    ActionListener actionListener = new ActionListener() {

      private boolean fBusy = false;

      @Override
      public void actionPerformed(ActionEvent e) {
        if (fBusy) {
          return;
        }
        fBusy = true;
        final AbstractVisibilityAction.Shape from = (AbstractVisibilityAction.Shape) fFromBox.getSelectedItem();
        final AbstractVisibilityAction.Shape to = (AbstractVisibilityAction.Shape) fToBox.getSelectedItem();
        fToBox.removeAllItems();
        fFromBox.removeAllItems();
        for (AbstractVisibilityAction.Shape shape : AbstractVisibilityAction.Shape.values()) {
          if (shape != to) {
            fFromBox.addItem(shape);
          }
          if (shape != from && shape != AbstractVisibilityAction.Shape.POINT) {
            fToBox.addItem(shape);
          }
        }
        fFromBox.setSelectedItem(from);
        fToBox.setSelectedItem(to);
        Thread thread = new Thread(new Runnable() {
          @Override
          public void run() {
            from.to(to, fAction, Dialog.ENABLED);
          }
        });
        thread.setPriority(Thread.MIN_PRIORITY);
        thread.start();
        fBusy = false;
      }
    };
    actionListener.actionPerformed(null);
    fFromBox.addActionListener(actionListener);
    fToBox.addActionListener(actionListener);
    panel.add(fFromBox);
    panel.add(new JLabel("to", JLabel.RIGHT));
    panel.add(fToBox);
  }

  public void performAction(final Object aObject) {
    final AbstractVisibilityAction.Shape from = (AbstractVisibilityAction.Shape) fFromBox.getSelectedItem();
    final AbstractVisibilityAction.Shape to = (AbstractVisibilityAction.Shape) fToBox.getSelectedItem();

    if (aObject != null && !aObject.equals(from.getObject(fAction)) && !aObject.equals(to.getObject(fAction))) {
      return;
    }

    if (fThread != null && fThread.isAlive()) {
      fThread.interrupt();
    }
    fThread = new Thread(new Runnable() {
      @Override
      public void run() {
        from.to(to, fAction, Dialog.DISABLED);
      }
    });
    fThread.setPriority(Thread.MIN_PRIORITY);
    fThread.start();
  }

}
