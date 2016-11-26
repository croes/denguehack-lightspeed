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
package samples.lucy.cop.addons.cop;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JComponent;

import com.luciad.lucy.ILcyLucyEnv;
import com.luciad.lucy.gui.customizer.ILcyCustomizerPanel;
import com.luciad.lucy.gui.customizer.TLcyCompositeCustomizerPanelFactory;
import com.luciad.lucy.util.context.TLcyDomainObjectContext;
import com.luciad.view.swing.ALcdBalloonDescriptor;
import com.luciad.view.swing.ILcdBalloonContentProvider;
import com.luciad.view.swing.TLcdModelElementBalloonDescriptor;

/**
 * <p>An {@code ILcdBalloonContentProvider} which uses the available {@code ILcyCustomizerPanelFactory}
 * instances for {@link TLcyDomainObjectContext}s to create the content for the balloon.</p>
 */
final class ObjectPropertiesBalloonContentProvider implements ILcdBalloonContentProvider {

  private final ILcyLucyEnv fLucyEnv;
  private ILcyCustomizerPanel fLastCreatedCustomizerPanel;

  ObjectPropertiesBalloonContentProvider(ILcyLucyEnv aLucyEnv) {
    fLucyEnv = aLucyEnv;
  }

  @Override
  public boolean canGetContent(ALcdBalloonDescriptor aBalloonDescriptor) {
    TLcyDomainObjectContext domainObjectContext = createDomainObjectContext(aBalloonDescriptor);
    return domainObjectContext != null &&
           new TLcyCompositeCustomizerPanelFactory(fLucyEnv).canCreateCustomizerPanel(domainObjectContext);
  }

  @Override
  public JComponent getContent(ALcdBalloonDescriptor aBalloonDescriptor) {
    if (fLastCreatedCustomizerPanel != null) {
      if (fLastCreatedCustomizerPanel.canSetObject(null)) {
        fLastCreatedCustomizerPanel.setObject(null);
      }
      fLastCreatedCustomizerPanel = null;
    }
    TLcyDomainObjectContext domainObjectContext = createDomainObjectContext(aBalloonDescriptor);
    if (domainObjectContext == null) {
      return null;
    }
    TLcyCompositeCustomizerPanelFactory factory = new TLcyCompositeCustomizerPanelFactory(fLucyEnv);
    if (factory.canCreateCustomizerPanel(domainObjectContext)) {
      ILcyCustomizerPanel customizerPanel = factory.createCustomizerPanel(domainObjectContext);
      if (customizerPanel != null) {
        fLastCreatedCustomizerPanel = customizerPanel;
        customizerPanel.setObject(domainObjectContext);
        //the balloon has no OK button to confirm the changes. Make sure the changes are immediately
        //applied when the customizer panel indicates they are available
        customizerPanel.addPropertyChangeListener(new ImmediateApplyListener());
        return ((JComponent) customizerPanel);
      }
    }
    return null;
  }

  private TLcyDomainObjectContext createDomainObjectContext(ALcdBalloonDescriptor aBalloonDescriptor) {
    if (aBalloonDescriptor instanceof TLcdModelElementBalloonDescriptor) {
      TLcdModelElementBalloonDescriptor balloonDescriptor = (TLcdModelElementBalloonDescriptor) aBalloonDescriptor;
      return new TLcyDomainObjectContext(aBalloonDescriptor.getObject(),
                                         balloonDescriptor.getModel(),
                                         balloonDescriptor.getLayer(),
                                         null);
    }
    return null;
  }

  private static final class ImmediateApplyListener implements PropertyChangeListener {
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
      if ("changesPending".equals(evt.getPropertyName()) ||
          "changesValid".equals(evt.getPropertyName())) {
        ILcyCustomizerPanel customizerPanel = (ILcyCustomizerPanel) evt.getSource();
        if (customizerPanel.isChangesPending() && customizerPanel.isChangesValid()) {
          customizerPanel.applyChanges();
        }
      }
    }
  }
}
