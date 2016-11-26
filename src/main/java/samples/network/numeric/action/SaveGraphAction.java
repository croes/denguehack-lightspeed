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
package samples.network.numeric.action;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.IOException;

import javax.swing.JOptionPane;

import com.luciad.gui.ALcdAction;
import com.luciad.gui.TLcdIconFactory;
import com.luciad.network.graph.numeric.TLcdNumericGraphEncoder;

import samples.network.numeric.graph.NumericGraphManager;

public class SaveGraphAction extends ALcdAction {

  private Component fParentFrame;

  private NumericGraphManager fGraphManager;

  public SaveGraphAction(NumericGraphManager aNumericGraphManager,
                         Component aParentFrame) {
    super("Save Graph", TLcdIconFactory.create(TLcdIconFactory.SAVE_ICON));
    fGraphManager = aNumericGraphManager;
    fParentFrame = aParentFrame;
  }

  public void actionPerformed(ActionEvent e) {
    TLcdNumericGraphEncoder encoder = new TLcdNumericGraphEncoder();
    try {
      encoder.saveGraph(fGraphManager.getGraph());
    } catch (IOException e1) {
      JOptionPane.showMessageDialog(fParentFrame, "There was a problem during saving: " + e1.getMessage());
      e1.printStackTrace();
    }
  }
}
