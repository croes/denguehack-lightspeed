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
package samples.lucy.cop.addons.missioncontroltheme;

import java.text.NumberFormat;

import javax.swing.JLabel;
import javax.swing.JTextField;

import com.luciad.datamodel.ILcdDataObject;
import com.luciad.lucy.ILcyLucyEnv;
import com.luciad.lucy.gui.TLcyTwoColumnLayoutBuilder;
import com.luciad.lucy.gui.customizer.ALcyDomainObjectCustomizerPanel;
import com.luciad.lucy.gui.customizer.ILcyCustomizerPanel;
import com.luciad.lucy.gui.customizer.ILcyCustomizerPanelFactory;
import com.luciad.lucy.util.context.TLcyDomainObjectContext;
import com.luciad.model.ILcdModel;
import com.luciad.shape.ILcdPoint;
import com.luciad.shape.ILcdShapeList;
import com.luciad.text.TLcdLonLatPointFormat;
import com.luciad.util.ILcdFilter;
import com.luciad.util.concurrent.TLcdLockUtil;

import samples.lucy.util.ValidatingTextField;

/**
 * {@code ILcyCustomizerPanelFactory} implementation for the blue forces domain objects
 */
final class BlueForcesCustomizerPanelFactory implements ILcyCustomizerPanelFactory {

  private static final ILcdFilter<Object> BLUE_FORCES_DOMAIN_OBJECT_FILTER = new ILcdFilter<Object>() {
    @Override
    public boolean accept(Object aObject) {
      if (aObject instanceof TLcyDomainObjectContext) {
        TLcyDomainObjectContext domainObjectContext = ((TLcyDomainObjectContext) aObject);
        Object domainObject = domainObjectContext.getDomainObject();
        return domainObjectContext.getModel() instanceof BlueForcesModel &&
               domainObject instanceof ILcdDataObject &&
               domainObject instanceof ILcdShapeList &&
               ((ILcdShapeList) domainObject).getShapeCount() == 1 &&
               ((ILcdShapeList) domainObject).getShape(0) instanceof ILcdPoint;
      }
      return false;
    }
  };

  private final ILcyLucyEnv fLucyEnv;

  BlueForcesCustomizerPanelFactory(ILcyLucyEnv aLucyEnv) {
    fLucyEnv = aLucyEnv;
  }

  @Override
  public boolean canCreateCustomizerPanel(Object aObject) {
    return BLUE_FORCES_DOMAIN_OBJECT_FILTER.accept(aObject);
  }

  @Override
  public ILcyCustomizerPanel createCustomizerPanel(Object aObject) {
    return new BlueForcesCustomizerPanel(fLucyEnv);
  }

  private static class BlueForcesCustomizerPanel extends ALcyDomainObjectCustomizerPanel {

    private final ILcyLucyEnv fLucyEnv;
    private JTextField fNameField;
    private ValidatingTextField fEffectivenessField;

    private ValidatingTextField fLocationField;

    private BlueForcesCustomizerPanel(ILcyLucyEnv aLucyEnv) {
      super(BLUE_FORCES_DOMAIN_OBJECT_FILTER, "Blue forces");
      fLucyEnv = aLucyEnv;
      initUI();
    }

    private void initUI() {
      fNameField = new JTextField(15);
      fEffectivenessField = new ValidatingTextField(NumberFormat.getPercentInstance(), fLucyEnv);
      TLcdLonLatPointFormat pointFormat = new TLcdLonLatPointFormat("lat(+DMS2),lon(+DMS2)");
      fLocationField = new ValidatingTextField(pointFormat, fLucyEnv);

      fNameField.setEditable(false);
      fEffectivenessField.setEditable(false);
      fLocationField.setEditable(false);

      TLcyTwoColumnLayoutBuilder builder = TLcyTwoColumnLayoutBuilder.newBuilder();

      builder.addTitledSeparator("Unit info");
      builder.row().columnOne(new JLabel("Name"), fNameField).build();
      builder.row().columnOne(new JLabel("Effectiveness"), fEffectivenessField).build();

      builder.addTitledSeparator("Position");
      builder.row().spanBothColumns(fLocationField).build();

      builder.populate(this);
    }

    @Override
    protected boolean applyChangesImpl() {
      //UI is not editable, so this should not be called
      return false;
    }

    @Override
    protected void updateCustomizerPanelFromObject(boolean aPanelEditable) {
      ILcdDataObject domainObject = (ILcdDataObject) getDomainObject();
      if (domainObject != null) {
        ILcdModel model = getModel();
        try (TLcdLockUtil.Lock autoUnlock = TLcdLockUtil.readLock(model)) {
          fNameField.setText((String) domainObject.getValue(BlueForcesModel.NAME_PROPERTY));
          fEffectivenessField.setValue(domainObject.getValue(BlueForcesModel.EFFECTIVENESS_PROPERTY));
          fLocationField.setValue(((ILcdShapeList) domainObject).getShape(0));
        }
      }
    }
  }
}
