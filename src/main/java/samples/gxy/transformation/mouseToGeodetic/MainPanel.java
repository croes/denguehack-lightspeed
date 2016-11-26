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
package samples.gxy.transformation.mouseToGeodetic;

import java.awt.GridLayout;

import javax.swing.JPanel;
import javax.swing.JTextArea;

import samples.gxy.common.GXYSample;
import samples.gxy.common.TitledPanel;

/**
 * This sample contains a custom implementation of ILcdGXYController, which
 * transforms a rectangle dragged by the user (in view coordinates) into
 * model coordinates. The mouse location is also
 * converted into an arbitrary model reference (a grid reference with a
 * polar projection for the north pole).
 */
public class MainPanel extends GXYSample {

  private final JTextArea fPointArea = new JTextArea(8, 25);
  private final JTextArea fBoundsArea = new JTextArea(8, 25);

  @Override
  protected JPanel createBottomPanel() {
    // Create the output areas
    fPointArea.setLineWrap(true);
    fPointArea.setWrapStyleWord(true);
    fBoundsArea.setLineWrap(true);
    fBoundsArea.setWrapStyleWord(true);
    // Create a titled panel around the output area
    JPanel outputHolder = new JPanel(new GridLayout(1, 2, 5, 5));
    outputHolder.add(fPointArea);
    outputHolder.add(fBoundsArea);
    return TitledPanel.createTitledPanel("Output Area", outputHolder);
  }

  @Override
  protected void createGUI() {
    super.createGUI();
    // Create the transforming controller and add mouse wheel zoom support
    TransformCoordinatesController transformController = new TransformCoordinatesController(fPointArea, fBoundsArea);
    getToolBars()[0].addGXYController(transformController);
    getView().setGXYController(getToolBars()[0].getGXYController(transformController));
  }

  // Main method
  public static void main(final String[] aArgs) {
    startSample(MainPanel.class, "Model-world and world-view transformations");
  }
}
