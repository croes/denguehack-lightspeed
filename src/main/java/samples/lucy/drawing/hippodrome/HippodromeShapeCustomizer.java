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
package samples.lucy.drawing.hippodrome;

import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JLabel;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.Sizes;
import samples.lucy.text.PositiveNumberFormat;
import samples.lucy.util.ValidatingTextField;
import com.luciad.lucy.ILcyLucyEnv;
import com.luciad.lucy.addons.drawing.gui.customizer.ALcyShapeCustomizerPanel;
import com.luciad.lucy.addons.drawing.util.context.TLcyShapeContext;
import com.luciad.lucy.util.language.TLcyLang;
import com.luciad.shape.ILcdPoint;
import com.luciad.util.ILcdFilter;

import samples.gxy.hippodromePainter.IHippodrome;

/**
 * This Customizer can edit the properties of a hippodrome. It shows 
 * textfields for start and end point and for width.
 */
public class HippodromeShapeCustomizer extends ALcyShapeCustomizerPanel {

  private static final String[] NAMES = new String[]{
      TLcyLang.getString("Start point"),
      TLcyLang.getString("End point"),
      TLcyLang.getString("Width")
  };

  ValidatingTextField fWidthField;
  ValidatingTextField fStartPointField;
  ValidatingTextField fEndPointField;
  private ILcyLucyEnv fLucyEnv;

  private PropertyChangeListener fChangeListener = new PropertyChangeListener() {
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
      setChangesPending(true);
    }
  };

  public HippodromeShapeCustomizer(ILcdFilter aShapeContextFilter, ILcyLucyEnv aLucyEnv) {
    super(aShapeContextFilter, TLcyLang.getString("Hippodrome"));
    fLucyEnv = aLucyEnv;
    this.setLayout(new FormLayout("right:max(50dlu;p):none, " + //first column: alignment = right, width is maximum of 100dlu and preferred size, no growing
                                  "10dlu:none, " + //second column: gap between label and component, fixed size of 10dlu
                                  "fill:default:grow",              //third column: fill all the available horizontal space and can be resized until its minimal size
                                  ""));

    fWidthField = new ValidatingTextField(new PositiveNumberFormat(aLucyEnv.getDefaultDistanceFormat()), fLucyEnv);
    fStartPointField = new ValidatingTextField(aLucyEnv.getDefaultLonLatPointFormat(), fLucyEnv);
    fEndPointField = new ValidatingTextField(aLucyEnv.getDefaultLonLatPointFormat(), fLucyEnv);
    fWidthField.addPropertyChangeListener("value", fChangeListener);
    fStartPointField.addPropertyChangeListener("value", fChangeListener);
    fEndPointField.addPropertyChangeListener("value", fChangeListener);

    addRow(NAMES[0], fStartPointField);
    addRow(NAMES[1], fEndPointField);
    addRow(NAMES[2], fWidthField);
  }

  @Override
  protected boolean applyChangesImpl() {
    if (!isPanelEditable()) {
      return true;
    }

    //no need to fire undoables, the TLcySLDDomainObjectSupplier class provides this for you.
    //it also is not necessary to take any write locks on the model, again the
    //TLcySLDDomainObjectSupplier class takes care of this.
    IHippodrome hippodrome = getHippodrome();
    hippodrome.setWidth(((Number) fWidthField.getValue()).doubleValue());
    ILcdPoint point = (ILcdPoint) fStartPointField.getValue();
    hippodrome.moveReferencePoint(point, IHippodrome.START_POINT);
    point = (ILcdPoint) fEndPointField.getValue();
    hippodrome.moveReferencePoint(point, IHippodrome.END_POINT);
    return true;
  }

  @Override
  protected void updateCustomizerPanelFromObject(boolean aPanelEditable) {
    //no need to acquire a read lock on the model, as the TLcySLDDomainObjectSupplier class
    //makes sure the proper lock is acquired before this method is called.
    IHippodrome hippodrome = getHippodrome();
    if (hippodrome != null) {
      fWidthField.removePropertyChangeListener("value", fChangeListener);
      fStartPointField.removePropertyChangeListener("value", fChangeListener);
      fEndPointField.removePropertyChangeListener("value", fChangeListener);

      fWidthField.setFormat(new PositiveNumberFormat(fLucyEnv.getDefaultDistanceFormat()), hippodrome.getWidth());
      fStartPointField.setFormat(fLucyEnv.getDefaultLonLatPointFormat(), hippodrome.getStartPoint());
      fEndPointField.setFormat(fLucyEnv.getDefaultLonLatPointFormat(), hippodrome.getEndPoint());

      fWidthField.setEditable(isPanelEditable());
      fStartPointField.setEditable(isPanelEditable());
      fEndPointField.setEditable(isPanelEditable());

      fWidthField.addPropertyChangeListener("value", fChangeListener);
      fStartPointField.addPropertyChangeListener("value", fChangeListener);
      fEndPointField.addPropertyChangeListener("value", fChangeListener);
    }
  }

  protected IHippodrome getHippodrome() {
    TLcyShapeContext context = (TLcyShapeContext) getObject();
    if (context != null) {
      return (IHippodrome) context.getShape();
    }
    return null;
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
