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
package samples.lightspeed.balloon;

import static com.luciad.util.concurrent.TLcdLockUtil.Lock;
import static com.luciad.util.concurrent.TLcdLockUtil.writeLock;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import com.luciad.gui.TLcdIconFactory;
import com.luciad.gui.swing.TLcdSWIcon;
import com.luciad.model.ILcdModel;
import com.luciad.shape.ILcdPoint;
import com.luciad.shape.shape2D.ILcd2DEditablePoint;
import com.luciad.text.TLcdLonLatPointFormat;
import com.luciad.view.swing.ALcdBalloonDescriptor;
import com.luciad.view.swing.ILcdBalloonContentProvider;
import com.luciad.view.swing.TLcdModelElementBalloonDescriptor;

/**
 * Basic balloon content provider for points.
 */
class BalloonContentProvider implements ILcdBalloonContentProvider {

  @Override
  public boolean canGetContent(ALcdBalloonDescriptor aBalloonDescriptor) {
    return aBalloonDescriptor.getObject() instanceof ILcdPoint;
  }

  @Override
  public JComponent getContent(ALcdBalloonDescriptor aBalloonDescriptor) {
    // Prepare a Swing panel to show the balloon in.
    final TLcdModelElementBalloonDescriptor balloonDescriptor = (TLcdModelElementBalloonDescriptor) aBalloonDescriptor;

    JLabel label = new JLabel("POINT",
                              new TLcdSWIcon(TLcdIconFactory.create(TLcdIconFactory.GLOBE_ICON, TLcdIconFactory.getDefaultTheme(), TLcdIconFactory.Size.SIZE_32)),
                              SwingConstants.LEFT);
    final JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    panel.add(label);

    final ILcdPoint point = (ILcdPoint) aBalloonDescriptor.getObject();
    final TLcdLonLatPointFormat format = new TLcdLonLatPointFormat(TLcdLonLatPointFormat.DEC_DEG_3);
    final JFormattedTextField textField = new JFormattedTextField(format);
    final String[] previousValue = {format.format(point.getX(), point.getY())};
    textField.setText(previousValue[0]);
    textField.setFocusLostBehavior(JFormattedTextField.COMMIT_OR_REVERT);

    // Used to move the point when editing the text field.
    textField.addPropertyChangeListener(new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        if ("value".equals(evt.getPropertyName())) {
          Object value = textField.getValue();

          if (textField.isEditValid() && point instanceof ILcd2DEditablePoint && value instanceof ILcdPoint) {
            ILcdModel model = balloonDescriptor.getModel();
            try (Lock autoUnlock = writeLock(model)) {
              ILcd2DEditablePoint editablePoint = (ILcd2DEditablePoint) point;
              editablePoint.move2D((ILcdPoint) textField.getValue());
              model.elementChanged(editablePoint, ILcdModel.FIRE_NOW);
            }

            previousValue[0] = format.format(value);
          } else {
            textField.setText(previousValue[0]);
          }

          panel.requestFocusInWindow();
        }
      }
    });

    panel.add(textField);

    return panel;
  }
}
