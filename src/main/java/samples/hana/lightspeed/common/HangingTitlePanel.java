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
package samples.hana.lightspeed.common;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.Box;
import javax.swing.JPanel;

import samples.common.HaloLabel;

/**
 * Wraps a panel to add haloed label above it.
 */
public class HangingTitlePanel {
  private HangingTitlePanel() {
  }

  public static JPanel create(String aTitle, Component aContent) {
    return create(aTitle, aContent, 16);
  }

  public static JPanel create(String aTitle, Component aContent, int aIndent) {
    JPanel panel = new JPanel(new BorderLayout(0, 5));
    panel.setOpaque(false);
    HaloLabel haloLabel = FontStyle.createHaloLabel(aTitle, FontStyle.H2);
    panel.add(haloLabel, BorderLayout.NORTH);
    panel.add(aContent, BorderLayout.CENTER);
    panel.add(Box.createHorizontalStrut(aIndent), BorderLayout.WEST);
    return panel;
  }
}
