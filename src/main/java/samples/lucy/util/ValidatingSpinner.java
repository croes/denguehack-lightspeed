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
package samples.lucy.util;

import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.Format;

import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.luciad.lucy.ILcyLucyEnv;

/**
 * A version of the JSpinner, which uses a validating text field as editor iso the regular
 * JFormattedTextField
 */
public class ValidatingSpinner extends JSpinner {
  /**
   * Create a new JSpinner with model <code>aSpinnerModel</code>. The format is used
   * for the validating text field (the editor field) and must only accept values which
   * are also accepted by the spinner model
   * @param aSpinnerModel the spinner model
   * @param aFormat the format
   */
  public ValidatingSpinner(SpinnerModel aSpinnerModel, Format aFormat) {
    this(aSpinnerModel, aFormat, null);
  }

  /**
   * Create a new JSpinner with model <code>aSpinnerModel</code>. The format is used
   * for the validating text field (the editor field) and must only accept values which
   * are also accepted by the spinner model
   * @param aSpinnerModel the spinner model
   * @param aFormat the format
   * @param aLucyEnv the Lucy backend.
   */
  public ValidatingSpinner(SpinnerModel aSpinnerModel, Format aFormat, ILcyLucyEnv aLucyEnv) {
    super(aSpinnerModel);
    setEditor(new MyEditor(this, aFormat, aLucyEnv));
  }

  /**
   * An editor for the JSpinner, which uses a formatted text field. It keeps the value of the editor
   * and the value of the spinner model in sync
   */
  protected static class MyEditor extends JPanel implements ChangeListener, PropertyChangeListener {
    private ValidatingTextField fTextField;
    private JSpinner fSpinner;
    private boolean fChangeListenerActive = true;
    private boolean fPropertyChangeListenerActive = true;

    private MyEditor(JSpinner aSpinner, Format aFormat, ILcyLucyEnv aLucyEnv) {
      setLayout(new BorderLayout());
      fTextField = new ValidatingTextField(aFormat, aLucyEnv);
      fTextField.setValue(aSpinner.getModel().getValue());
      fSpinner = aSpinner;
      add(fTextField, BorderLayout.CENTER);
      //add the necessary listeners to keep the editor in sync with the model
      fSpinner.addChangeListener(this);
      fTextField.addPropertyChangeListener("value", this);
      setToolTipText(fSpinner.getToolTipText());
    }

    @Override
    public void setToolTipText(String text) {
      super.setToolTipText(text);
      fTextField.setToolTipText(text);
    }

    @Override
    public void stateChanged(ChangeEvent e) {
      if (fChangeListenerActive) {
        //the value of the model has been changed
        Object newValue = fSpinner.getModel().getValue();
        //pass the value to the editor
        fPropertyChangeListenerActive = false;
        fTextField.setValue(newValue);
        fPropertyChangeListenerActive = true;
      }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
      if (fPropertyChangeListenerActive) {
        //the value in the textfield has been changed
        Object newValue = fTextField.getValue();
        //pass the value to the model
        fChangeListenerActive = false;
        fSpinner.getModel().setValue(newValue);
        fChangeListenerActive = true;
      }
    }

    /**
     * Returns the text field used by this editor
     * @return the text field used by this editor
     */
    public ValidatingTextField getTextField() {
      return fTextField;
    }
  }
}
