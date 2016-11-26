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
package samples.lucy.drawing.customdomainobject;

import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JLabel;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.Sizes;
import com.luciad.gui.ALcdUndoable;
import com.luciad.lucy.ILcyLucyEnv;
import com.luciad.lucy.gui.customizer.ALcyDomainObjectCustomizerPanel;
import com.luciad.lucy.util.language.TLcyLang;
import com.luciad.shape.ILcdPoint;
import com.luciad.util.ILcdFilter;

import samples.lucy.util.ValidatingTextField;

/**
 * This Customizer can edit the properties of a CustomDomainObject. It shows
 * a single field in which a location can be entered.
 */
public class CustomDomainObjectCustomizerPanel extends ALcyDomainObjectCustomizerPanel {
  private static final String LOCATION = TLcyLang.getString("Location");

  private final ValidatingTextField fPointField;
  private final ILcyLucyEnv fLucyEnv;

  private final PropertyChangeListener fChangeListener = new PropertyChangeListener() {
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
      setChangesPending(true);
    }
  };

  public CustomDomainObjectCustomizerPanel(ILcdFilter aDomainObjectContextFilter, ILcyLucyEnv aLucyEnv) {
    super(aDomainObjectContextFilter, TLcyLang.getString("Custom point"));
    fLucyEnv = aLucyEnv;
    this.setLayout(new FormLayout("right:max(50dlu;p):none, " + //first column: alignment = right, width is maximum of 50dlu and preferred size, no growing
                                  "5dlu:none, " + //second column: gap between label and component, fixed size of 5dlu
                                  "fill:default:grow",              //third column: fill all the available horizontal space and can be resized until its minimal size
                                  ""));

    fPointField = new ValidatingTextField(aLucyEnv.getDefaultDistanceFormat(), fLucyEnv);
    fPointField.addPropertyChangeListener("value", fChangeListener);

    addRow(LOCATION, fPointField);
  }

  @Override
  protected boolean applyChangesImpl() {
    //it is not necessary to take any write locks on the model, again the
    //CustomDomainObjectSupplier class takes care of this.
    CustomDomainObject target = getCustomDomainObject();
    ILcdPoint newLocation = (ILcdPoint) fPointField.getValue();
    ALcdUndoable undoable = new CustomDomainObjectUndoable(target, newLocation);
    target.move2D(newLocation);
    fireUndoableHappened(undoable);
    return true;
  }

  @Override
  protected void updateCustomizerPanelFromObject(boolean aPanelEditable) {
    fPointField.setEditable(aPanelEditable);

    //no need to acquire a read lock on the model, as the CustomDomainObjectSupplier class
    //makes sure the proper lock is acquired before this method is called.
    CustomDomainObject target = getCustomDomainObject();
    if (target != null) {
      fPointField.removePropertyChangeListener("value", fChangeListener);
      fPointField.setFormat(fLucyEnv.getDefaultLonLatPointFormat(), target);
      fPointField.addPropertyChangeListener("value", fChangeListener);
    }
  }

  protected CustomDomainObject getCustomDomainObject() {
    return (CustomDomainObject) getDomainObject();
  }

  private void addRow(String aLabel, Component aEditor) {
    Component labelComponent = new JLabel(aLabel);
    FormLayout layout = (FormLayout) getLayout();
    DefaultFormBuilder builder = new DefaultFormBuilder(layout, this);
    builder.opaque(true);
    builder.lineGapSize(Sizes.dluX(1)); //size between the rows in dlu
    int rownumber = layout.getRowCount();
    if (rownumber > 0) {
      builder.setRow(rownumber + 1);
    }
    builder.append(labelComponent, aEditor);
  }
}
