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
package samples.lightspeed.demo.application.data.imageprocessing;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.luciad.format.gml32.model.TLcdGML32AbstractFeature;
import com.luciad.view.swing.ALcdBalloonDescriptor;
import com.luciad.view.swing.ILcdBalloonContentProvider;
import com.luciad.view.swing.TLcdModelElementBalloonDescriptor;

/**
 * Provides the content for the points of interest balloons.
 */
class InstructionBalloonContentProvider implements ILcdBalloonContentProvider {

  @Override
  public boolean canGetContent(ALcdBalloonDescriptor aALcdBalloonDescriptor) {
    if (aALcdBalloonDescriptor instanceof TLcdModelElementBalloonDescriptor) {
      TLcdModelElementBalloonDescriptor descriptor = (TLcdModelElementBalloonDescriptor) aALcdBalloonDescriptor;
      if (descriptor.getObject() instanceof TLcdGML32AbstractFeature) {
        return true;
      }
    }
    return false;
  }

  @Override
  public JComponent getContent(ALcdBalloonDescriptor aALcdBalloonDescriptor) {
    if (aALcdBalloonDescriptor instanceof TLcdModelElementBalloonDescriptor) {
      TLcdModelElementBalloonDescriptor descriptor =
          (TLcdModelElementBalloonDescriptor) aALcdBalloonDescriptor;
      Object modelObject = descriptor.getObject();
      if (modelObject instanceof TLcdGML32AbstractFeature) {
        TLcdGML32AbstractFeature feature = (TLcdGML32AbstractFeature) modelObject;
        String instructionTitle = (String) feature.getValue("Name");
        String instructionText = (String) feature.getValue("Instructions");

        JPanel panel = new JPanel();
        int balloonTextWidth = 250;
        String htmlText = String.format(
            "<html><body style='width:%spx;'><p><b>%s</b><br><br></p><p>%s</p></body></html>",
            balloonTextWidth,
            instructionTitle,
            instructionText);
        panel.add(new JLabel(htmlText));
        return panel;
      }
    }
    return null;
  }
}
