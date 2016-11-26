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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JLabel;
import javax.swing.JTextField;

import com.luciad.datamodel.ILcdDataObject;
import com.luciad.lucy.ILcyLucyEnv;
import com.luciad.lucy.gui.TLcyTwoColumnLayoutBuilder;
import com.luciad.lucy.gui.customizer.ALcyCustomizerPanelFactory;
import com.luciad.lucy.gui.customizer.ALcyDomainObjectCustomizerPanel;
import com.luciad.lucy.gui.customizer.ILcyCustomizerPanel;
import com.luciad.lucy.util.context.TLcyDomainObjectContext;
import com.luciad.model.ILcdModel;
import com.luciad.util.ILcdFilter;
import com.luciad.util.concurrent.TLcdLockUtil;

/**
 * {@code ILcyCustomizerPanelFactory} implementation for the annotation domain objects
 */
final class AnnotationsCustomizerPanelFactory extends ALcyCustomizerPanelFactory {

  private static final ILcdFilter<Object> ANNOTATIONS_DOMAIN_OBJECT_FILTER = new ILcdFilter<Object>() {
    @Override
    public boolean accept(Object aObject) {
      if (aObject instanceof TLcyDomainObjectContext) {
        TLcyDomainObjectContext domainObjectContext = ((TLcyDomainObjectContext) aObject);
        Object domainObject = domainObjectContext.getDomainObject();
        return domainObjectContext.getModel() instanceof AnnotationModel &&
               domainObject instanceof GeoJsonRestModelElement;
      }
      return false;
    }
  };

  private final ILcyLucyEnv fLucyEnv;

  AnnotationsCustomizerPanelFactory(ILcyLucyEnv aLucyEnv) {
    super(ANNOTATIONS_DOMAIN_OBJECT_FILTER);
    fLucyEnv = aLucyEnv;
  }

  @Override
  protected ILcyCustomizerPanel createCustomizerPanelImpl(Object aObject) {
    return new AnnotationsCustomizerPanel(fLucyEnv);
  }

  private static class AnnotationsCustomizerPanel extends ALcyDomainObjectCustomizerPanel {

    private final ILcyLucyEnv fLucyEnv;
    private JTextField fTextField;

    private boolean fManuallyUpdatingUI = false;

    private AnnotationsCustomizerPanel(ILcyLucyEnv aLucyEnv) {
      super(ANNOTATIONS_DOMAIN_OBJECT_FILTER, "Annotations");
      fLucyEnv = aLucyEnv;
      initUI();
    }

    private void initUI() {
      fTextField = new JTextField(15);
      fTextField.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
          if (!fManuallyUpdatingUI) {
            setChangesPending(true);
          }
        }
      });

      TLcyTwoColumnLayoutBuilder builder = TLcyTwoColumnLayoutBuilder.newBuilder();

      builder.row().columnOne(new JLabel("Description"), fTextField).build();

      builder.populate(this);
    }

    @Override
    protected boolean applyChangesImpl() {
      ILcdDataObject domainObject = (ILcdDataObject) getDomainObject();
      if (domainObject != null) {
        ILcdModel model = getModel();
        try (TLcdLockUtil.Lock autoUnlock = TLcdLockUtil.writeLock(model)) {
          Object currentValue = domainObject.getValue(AnnotationModel.TEXT_PROPERTY);
          if ("".equals(fTextField.getText()) && currentValue == null) {
            return true;
          }
          boolean adjusted = !(fTextField.getText().equals(currentValue));
          domainObject.setValue(AnnotationModel.TEXT_PROPERTY, fTextField.getText());
          if (adjusted) {
            model.elementChanged(domainObject, ILcdModel.FIRE_LATER);
          }
        } finally {
          model.fireCollectedModelChanges();
        }
        return true;
      }
      return false;
    }

    @Override
    protected void updateCustomizerPanelFromObject(boolean aPanelEditable) {
      fTextField.setEditable(aPanelEditable);
      ILcdDataObject domainObject = (ILcdDataObject) getDomainObject();
      if (domainObject != null) {
        try (TLcdLockUtil.Lock autoUnlock = TLcdLockUtil.readLock(getModel())) {
          boolean oldValue = fManuallyUpdatingUI;
          fManuallyUpdatingUI = true;
          try {
            String value = (String) domainObject.getValue(AnnotationModel.TEXT_PROPERTY);
            fTextField.setText(value != null ? value : "");//avoid that the customizer panel shows null
          } finally {
            fManuallyUpdatingUI = oldValue;
          }
        }
      }
    }
  }
}
