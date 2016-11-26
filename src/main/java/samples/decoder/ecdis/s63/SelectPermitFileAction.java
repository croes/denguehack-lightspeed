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
import java.io.File;
import java.util.Arrays;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import com.luciad.format.s63.TLcdS63UnifiedModelDecoder;
import com.luciad.gui.ALcdAction;
import com.luciad.gui.TLcdAWTUtil;
import com.luciad.gui.TLcdImageIcon;
import com.luciad.util.TLcdStringUtil;

/**
 * Select the basic permit file to use during decoding.
 */
public class SelectPermitFileAction extends ALcdAction {

  private JFileChooser fFileChooser = new JFileChooser();
  private final TLcdS63UnifiedModelDecoder fModelDecoder;

  public SelectPermitFileAction(TLcdS63UnifiedModelDecoder aModelDecoder) {
    fModelDecoder = aModelDecoder;

    String userDirectory = new File(new File(new File(System.getProperty("user.dir"), "Data"), "Ecdis"), "Encrypted").getAbsolutePath();
    fFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
    fFileChooser.setCurrentDirectory(new File(userDirectory));
    fFileChooser.setFileFilter(new FileFilter() {
      @Override
      public String getDescription() {
        return "S-63 Basic/Meta permit files";
      }

      @Override
      public boolean accept(File pathname) {
        return pathname.isDirectory() ||
               pathname.getPath().toLowerCase().endsWith("enc.pmt") ||
               pathname.getPath().toLowerCase().endsWith("permit.txt");
      }
    });

    setIcon(new TLcdImageIcon("samples/images/open_key.png"));
    setShortDescription("Select Basic or Meta Permit file");
  }

  @Override
  public void actionPerformed(ActionEvent event) {
    if (fFileChooser.showOpenDialog(TLcdAWTUtil.findParentFrame(event)) == JFileChooser.APPROVE_OPTION) {
      String file = fFileChooser.getSelectedFile().getPath();
      if (TLcdStringUtil.endsWithIgnoreCase(file, "enc.pmt")) {
        fModelDecoder.setBasicPermitSources(Arrays.asList(file));
      } else if (TLcdStringUtil.endsWithIgnoreCase(file, "permit.txt")) {
        fModelDecoder.setMetaPermitSources(Arrays.asList(file));
      }
    }
  }
}
