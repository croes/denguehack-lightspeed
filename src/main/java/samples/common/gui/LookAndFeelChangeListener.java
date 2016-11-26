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
package samples.common.gui;

import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.luciad.gui.TLcdAWTUtil;

/**
 * Updates the look and feel of the given component, should it change.
 *
 * It uses weak references to avoid introducing memory leaks, so you don't have to care
 * about removing this listener again.
 */
public class LookAndFeelChangeListener implements PropertyChangeListener {
  private final WeakReference<Component> fContent;

  public static void install(Component aComponent) {
    UIManager.addPropertyChangeListener(new LookAndFeelChangeListener(aComponent));
  }

  private LookAndFeelChangeListener(Component aContent) {
    fContent = new WeakReference<>(aContent);
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    final Component content = fContent.get();
    if (content != null) {
      if (evt.getPropertyName() == null || "lookAndFeel".equals(evt.getPropertyName())) {
        TLcdAWTUtil.invokeLater(new Runnable() {
          @Override
          public void run() {
            SwingUtilities.updateComponentTreeUI(content);
          }
        });
      }
    } else {
      UIManager.removePropertyChangeListener(this);
    }
  }
}
