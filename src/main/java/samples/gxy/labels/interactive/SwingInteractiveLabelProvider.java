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
package samples.gxy.labels.interactive;

import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import com.luciad.view.gxy.ALcdGXYInteractiveLabelProvider;
import com.luciad.view.gxy.ILcdGXYContext;

/**
 * Implementation of ALcdGXYInteractiveLabelProvider that can provide an interactive label :
 * InteractiveSwingLabelComponent. This label consists of a title bar and a text field. When
 * the text field has focus, this provider will indicate that the label interaction should not
 * be stopped. This allows the user to move his mouse away from the text field and possibly
 * on the view, without the interaction being interrupted. This ALcdGXYInteractiveLabelProvider
 * also commits all changes from the text field when interaction is stopped.
 */
class SwingInteractiveLabelProvider extends ALcdGXYInteractiveLabelProvider {

  private final InteractiveSwingLabelComponent fInteractiveLabelComponent;
  private boolean fCanStop = true;

  public SwingInteractiveLabelProvider(InteractiveSwingLabelComponent aInteractiveLabelComponent) {
    fInteractiveLabelComponent = aInteractiveLabelComponent;

    // When the user presses 'Enter' on the text field, the values should be committed
    // and the interactive label disappear.
    fInteractiveLabelComponent.addPressedEnterActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        fCanStop = true;
        stopInteraction();
      }
    });

    // When the user has clicked in the text field, he needs to be able to type in the
    // text field even though he moves the mouse away.
    fInteractiveLabelComponent.addCommentsFieldFocusListener(new FocusAdapter() {
      public void focusGained(FocusEvent e) {
        fCanStop = false;
      }

      public void focusLost(FocusEvent e) {
        fCanStop = true;
      }
    });

    // When the user has clicked on the cross icon, the interaction with the label should be cancelled.
    fInteractiveLabelComponent.addCrossIconMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        EventQueue.invokeLater(new Runnable() {
          public void run() {
            cancelInteraction();
          }
        });
      }
    });

    // When the user has clicked on the cross icon, the interaction with the label should be stopped.
    fInteractiveLabelComponent.addTickIconMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        EventQueue.invokeLater(new Runnable() {
          public void run() {
            stopInteraction();
          }
        });
      }
    });
  }

  public boolean canProvideInteractiveLabel(Object aObject, int aLabelIndex, int aSubLabelIndex, ILcdGXYContext aGXYContext) {
    return true;
  }

  public Component provideInteractiveLabel(Object aObject, int aLabelIndex, int aSubLabelIndex, ILcdGXYContext aGXYContext) {
    fInteractiveLabelComponent.setObject(aObject, aGXYContext.getGXYLayer().getModel());
    fCanStop = true;
    fireInteractionStartedEvent(aObject, aLabelIndex, aSubLabelIndex, aGXYContext, fInteractiveLabelComponent);
    return fInteractiveLabelComponent;
  }

  public boolean canStopInteraction() {
    return fCanStop;
  }

  public boolean stopInteraction() {
    if (!canStopInteraction()) {
      Toolkit.getDefaultToolkit().beep();
      return false;
    }
    fInteractiveLabelComponent.commitCommentChanges();
    fireInteractionStoppedEvent();
    return true;
  }

  public void cancelInteraction() {
    fireInteractionCancelledEvent();
  }

  public void dispatchMouseEvent(MouseEvent aMouseEvent, Component aInteractiveLabel, Component aContainer) {
    if (!fInteractiveLabelComponent.dispatchMouseEvent(aMouseEvent)) {
      aContainer.dispatchEvent(convertAWTEvent(aMouseEvent, aContainer));
    }
  }
}
