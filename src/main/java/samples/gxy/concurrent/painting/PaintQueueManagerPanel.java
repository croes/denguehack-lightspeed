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
package samples.gxy.concurrent.painting;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import com.luciad.view.gxy.ILcdGXYView;
import com.luciad.view.gxy.asynchronous.manager.ALcdGXYAsynchronousPaintQueueManager;
import com.luciad.view.gxy.asynchronous.manager.TLcdGXYAsynchronousPaintQueueManager;

/**
 * Panel allowing to switch between several paint queue managers
 */
public class PaintQueueManagerPanel extends JPanel {
  private ILcdGXYView fView;

  private ALcdGXYAsynchronousPaintQueueManager fCurrentManager = null;

  private PaintTimeBasedPaintHintProvider fProvider;

  /**
   * Create a new panel and set the default paint queue manager on the view
   * @param aView The view
   */
  public PaintQueueManagerPanel(ILcdGXYView aView) {
    fView = aView;

    //initialize the paint hint provider
    fProvider = new PaintTimeBasedPaintHintProvider(fView);

    //create the GUI
    initGUI();
  }

  private void initGUI() {
    //create the buttons
    JRadioButton defaultManagerButton = new JRadioButton(
        new ChangeManagerAction("Default", new TLcdGXYAsynchronousPaintQueueManager()));
    JRadioButton paintTimeManagerButton = new JRadioButton(
        new ChangeManagerAction("Paint time", new PaintTimeBasedAsynchronousPaintQueueManager(fProvider)));
    JRadioButton fixedCountManagerButton = new JRadioButton(
        new ChangeManagerAction("Fixed count", new FixedCountPaintQueueManager()));

    //group the buttons
    ButtonGroup group = new ButtonGroup();
    group.add(defaultManagerButton);
    group.add(paintTimeManagerButton);
    group.add(fixedCountManagerButton);
    defaultManagerButton.doClick();

    setLayout(new GridLayout(0, 1));
    add(defaultManagerButton);
    add(paintTimeManagerButton);
    add(fixedCountManagerButton);
  }

  private class ChangeManagerAction extends AbstractAction {
    private ALcdGXYAsynchronousPaintQueueManager fPaintQueueManager;

    public ChangeManagerAction(String name, ALcdGXYAsynchronousPaintQueueManager aPaintQueueManager) {
      super(name);
      fPaintQueueManager = aPaintQueueManager;
    }

    public void actionPerformed(ActionEvent aActionEvent) {
      //first dispose of the current manager
      if (fCurrentManager != null) {
        fCurrentManager.setGXYView(null);
      }
      //activate the new manager
      fCurrentManager = fPaintQueueManager;
      fCurrentManager.setGXYView(fView);
    }
  }
}
