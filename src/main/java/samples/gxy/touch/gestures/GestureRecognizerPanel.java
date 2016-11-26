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
package samples.gxy.touch.gestures;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import samples.gxy.common.TitledPanel;

/**
 * Displays checkboxes for enabling or disabling the available gesture.
 */
public class GestureRecognizerPanel extends JPanel {
  public GestureRecognizerPanel(final GestureRecognizerController aController) {
    final JCheckBox right_flick = new JCheckBox("Two-finger right flick", true);
    final JCheckBox left_flick = new JCheckBox("Two-finger left flick", true);
    final JCheckBox ellipse = new JCheckBox("Ellipse gesture", true);
    final JCheckBox z_gesture = new JCheckBox("Z gesture", true);

    right_flick.setAlignmentX(Component.LEFT_ALIGNMENT);
    left_flick.setAlignmentX(Component.LEFT_ALIGNMENT);
    ellipse.setAlignmentX(Component.LEFT_ALIGNMENT);
    z_gesture.setAlignmentX(Component.LEFT_ALIGNMENT);

    setLayout(new BorderLayout(0, 5));
    setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));

    JPanel content = new JPanel();
    BoxLayout layout = new BoxLayout(content, BoxLayout.Y_AXIS);
    content.setLayout(layout);
    content.add(right_flick);
    content.add(left_flick);
    content.add(ellipse);
    content.add(z_gesture);
    add(content, BorderLayout.CENTER);

    setLayout(new BorderLayout());
    add(BorderLayout.NORTH, TitledPanel.createTitledPanel("Enabled gestures", content));

    right_flick.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        aController.enableRecognizer(aController.getRightFlickRecognizer(),
                                     right_flick.isSelected());
      }
    });

    left_flick.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        aController.enableRecognizer(aController.getLeftFlickRecognizer(),
                                     left_flick.isSelected());
      }
    });

    ellipse.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        aController.enableRecognizer(aController.getEllipseGestureRecognizer(),
                                     ellipse.isSelected());
      }
    });

    z_gesture.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        aController.enableRecognizer(aController.getZGestureRecognizer(),
                                     z_gesture.isSelected());
      }
    });
  }
}
