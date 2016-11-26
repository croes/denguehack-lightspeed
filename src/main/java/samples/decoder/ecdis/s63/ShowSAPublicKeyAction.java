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
import java.io.InputStream;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;

import com.luciad.format.s63.TLcdS63UnifiedModelDecoder;
import com.luciad.gui.ALcdAction;
import com.luciad.gui.TLcdAWTUtil;
import com.luciad.gui.TLcdImageIcon;

/**
 * Show the SA's public key in a message dialog.
 */
public class ShowSAPublicKeyAction extends ALcdAction {

  private TLcdS63UnifiedModelDecoder fS63Decoder;

  public ShowSAPublicKeyAction(TLcdS63UnifiedModelDecoder aS63Decoder) {
    fS63Decoder = aS63Decoder;
    setIcon(new TLcdImageIcon("images/gui/i16_edit.gif"));
    setShortDescription("Show SA Public Key");
  }

  @Override
  public void actionPerformed(ActionEvent event) {
    JDialog dialog = new JDialog(TLcdAWTUtil.findParentFrame(event), "SA Public Key Info");
    try {
      JTabbedPane tabbedPane = new JTabbedPane();
      CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
      for (int i = 0; i < fS63Decoder.getSACertificateSources().size(); i++) {
        InputStream input = fS63Decoder.getInputStreamFactory().createInputStream(fS63Decoder.getSACertificateSources().get(i));
        Certificate certificate = certFactory.generateCertificate(input);
        JTextArea textArea = new JTextArea(certificate.getPublicKey().toString());
        textArea.setEditable(false);
        tabbedPane.addTab("Key " + (i + 1), textArea);
        input.close();
      }
      dialog.add(tabbedPane);
    } catch (Exception e) {
      e.printStackTrace();
      JLabel errorLabel = new JLabel("An error occured while displaying the SA Public Key Info.");
      errorLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
      dialog.add(errorLabel);
    }
    dialog.setVisible(true);
    dialog.pack();
  }
}
