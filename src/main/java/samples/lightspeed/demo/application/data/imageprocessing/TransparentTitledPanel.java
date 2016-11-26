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

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.JPanel;

import samples.common.TitledSeparator;

/**
 * JPanel with a title and a transparent background. Used for
 * the side panel in the Image Processing theme.
 */
class TransparentTitledPanel extends JPanel {

  private TransparentTitledPanel(String aTitle, Component aContent) {
    super(new BorderLayout());

    JPanel north_content_south_panel = new JPanel(new BorderLayout(0, 3));
    north_content_south_panel.setOpaque(false);
    north_content_south_panel.add(aContent, BorderLayout.CENTER);

    TitledSeparator titledSeparator = new TitledSeparator(aTitle);
    titledSeparator.setOpaque(false);
    north_content_south_panel.add(titledSeparator, BorderLayout.NORTH);

    add(north_content_south_panel, BorderLayout.CENTER);
    setOpaque(false);
  }

  public static TransparentTitledPanel createTitledPanel(String aTitle, Component aComponent) {
    return new TransparentTitledPanel(aTitle, aComponent);
  }
}
