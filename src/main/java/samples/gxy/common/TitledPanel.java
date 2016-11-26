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
package samples.gxy.common;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JSeparator;

import samples.common.TitledSeparator;

/**
 * Panel that adds a custom titled border above the given content. It
 * will also add separators to the west, east or south of the content
 * if specified.
 *
 * Keep in mind that when the NORTH flag is not specified, the title
 * will not be present.
 */
public class TitledPanel extends JPanel {

  public static final int NORTH = 1;
  public static final int SOUTH = 2;
  public static final int EAST = 4;
  public static final int WEST = 8;

  private TitledPanel(String aTitle, Component aContent, int aBorderFlag, Insets aInsets) {
    super(new BorderLayout());

    JPanel north_content_south_panel = new JPanel(new BorderLayout(0, 3));
    north_content_south_panel.add(aContent, BorderLayout.CENTER);
    if ((aBorderFlag & NORTH) != 0) {
      north_content_south_panel.add(new TitledSeparator(aTitle), BorderLayout.NORTH);
    }
    if ((aBorderFlag & SOUTH) != 0) {
      north_content_south_panel.add(new JSeparator(JSeparator.HORIZONTAL), BorderLayout.SOUTH);
    }
    if (aInsets != null) {
      north_content_south_panel.setBorder(BorderFactory.createEmptyBorder(
          0, aInsets.left, 0, aInsets.right
                                                                         ));
    }

    add(north_content_south_panel, BorderLayout.CENTER);
    if ((aBorderFlag & WEST) != 0) {
      add(new JSeparator(JSeparator.VERTICAL), BorderLayout.WEST);
    }
    if ((aBorderFlag & EAST) != 0) {
      add(new JSeparator(JSeparator.VERTICAL), BorderLayout.EAST);
    }
    if (aInsets != null) {
      setBorder(BorderFactory.createEmptyBorder(
          aInsets.top, 0, aInsets.bottom, 0
                                               ));
    }
  }

  public static TitledPanel createTitledPanel(String aTitle, Component aComponent) {
    return createTitledPanel(aTitle, aComponent, NORTH);
  }

  public static TitledPanel createTitledPanel(String aTitle, Component aComponent, int aBorderFlag) {
    return new TitledPanel(aTitle, aComponent, aBorderFlag, new Insets(2, 3, 2, 4));
  }

}
