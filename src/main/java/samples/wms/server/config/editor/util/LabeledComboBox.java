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
package samples.wms.server.config.editor.util;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Convenience class to display a labeled combo box.
 */
public class LabeledComboBox extends LabeledComponent {

  private List fChangeListeners = new ArrayList();

  private JComboBox getList() {
    return (JComboBox) getComponent();
  }

  public LabeledComboBox(String aLabel, Object[] aItems, Object aDefault) {
    super(aLabel, new JComboBox(aItems));

    getList().setSelectedItem(aDefault);
    getList().addActionListener(new ChangeSelectionListener());
  }

  public Object getSelectedItem() {
    return getList().getSelectedItem();
  }

  public void addChangeListener(ChangeListener aListener) {
    fChangeListeners.add(aListener);
  }

  private class ChangeSelectionListener implements ActionListener {

    public void actionPerformed(ActionEvent e) {

      ChangeEvent ce = new ChangeEvent(getList());
      for (int i = 0; i < fChangeListeners.size(); i++) {
        ChangeListener l = (ChangeListener) fChangeListeners.get(i);
        l.stateChanged(ce);
      }
    }
  }
}
