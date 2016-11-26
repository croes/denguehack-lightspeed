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
package samples.decoder.ecdis.s63;

import java.awt.event.ActionEvent;

import javax.swing.JOptionPane;

import com.luciad.format.s63.TLcdS63UnifiedModelDecoder;
import com.luciad.gui.ALcdAction;
import com.luciad.gui.TLcdAWTUtil;
import com.luciad.gui.TLcdImageIcon;

/**
 * Show the User Permit in a message dialog.
 */
public class ShowUserPermitAction extends ALcdAction {

  private TLcdS63UnifiedModelDecoder fS63Decoder;

  public ShowUserPermitAction(TLcdS63UnifiedModelDecoder aS63Decoder) {
    fS63Decoder = aS63Decoder;
    setIcon(new TLcdImageIcon("images/gui/i16_edit.gif"));
    setShortDescription("Show User Permit");
  }

  @Override
  public void actionPerformed(ActionEvent event) {
    String userPermit = fS63Decoder.getUserPermit();
    if (userPermit == null) {
      userPermit = "No User Permit attached to license (demo user permit will be used).";
    }
    JOptionPane.showMessageDialog(
        TLcdAWTUtil.findParentFrame(event),
        userPermit,
        "User Permit",
        JOptionPane.INFORMATION_MESSAGE
                                 );
  }
}
