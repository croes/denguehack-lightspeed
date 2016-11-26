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
package samples.lucy.fundamentals.waypoints.model;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.Format;

import javax.swing.JLabel;

import com.luciad.datamodel.ILcdDataObject;
import com.luciad.lucy.ILcyLucyEnv;
import com.luciad.lucy.gui.TLcyTwoColumnLayoutBuilder;
import com.luciad.lucy.gui.customizer.ALcyDomainObjectCustomizerPanel;
import com.luciad.lucy.util.context.TLcyDomainObjectContext;
import com.luciad.model.ILcdModel;
import com.luciad.shape.ALcdShape;
import com.luciad.shape.ILcdPoint;
import com.luciad.shape.shape3D.TLcdLonLatHeightPoint;
import com.luciad.util.ALcdWeakPropertyChangeListener;
import com.luciad.util.ILcdFilter;
import com.luciad.util.concurrent.TLcdLockUtil;

import samples.gxy.fundamentals.step3.WayPointDataTypes;
import samples.lucy.fundamentals.waypoints.WayPointsModelFilter;
import samples.lucy.text.StringFormat;
import samples.lucy.util.ValidatingTextField;

/**
 * <p>
 *   {@code ILcyCustomizerPanel} implementation for the way point domain objects.
 * </p>
 */
final class WayPointCustomizerPanel extends ALcyDomainObjectCustomizerPanel {

  private final ILcyLucyEnv fLucyEnv;

  private ValidatingTextField fNameField;
  private ValidatingTextField fLocationField;
  private ValidatingTextField fHeightField;

  private boolean fUpdatingUI = false;
  private final PropertyChangeListener fTextFieldListener = new PropertyChangeListener() {
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
      if (!fUpdatingUI) {
        setChangesPending(true);
      }
    }
  };

  WayPointCustomizerPanel(ILcyLucyEnv aLucyEnv) {
    // Note that we still have to pass a filter to the super constructor, even when using the TLcyLspSafeGuardFormatWrapper
    // The wrapper only protects the ILcyCustomizerPanelFactory, not the ILcyCustomizerPanel instances itself
    // Therefore we need a filter to ensure we only accept our domain object contexts
    super(new ILcdFilter() {
      private final WayPointsModelFilter fModelFilter = new WayPointsModelFilter();

      @Override
      public boolean accept(Object aObject) {
        return aObject instanceof TLcyDomainObjectContext &&
               fModelFilter.accept(((TLcyDomainObjectContext) aObject).getModel());
      }
    }, "Way points");
    fLucyEnv = aLucyEnv;
    initUI();
    fLucyEnv.addPropertyChangeListener(new PointFormatListener(this));
    fLucyEnv.addPropertyChangeListener(new AltitudeFormatListener(this));
  }

  private void initUI() {
    fNameField = new ValidatingTextField(new StringFormat(), fLucyEnv);
    fLocationField = new ValidatingTextField(fLucyEnv.getDefaultLonLatPointFormat(), fLucyEnv);
    fHeightField = new ValidatingTextField(fLucyEnv.getDefaultAltitudeFormat(), fLucyEnv);

    TLcyTwoColumnLayoutBuilder.newBuilder()
                              .addTitledSeparator("Way point")
                              .row()
                              .columnOne(new JLabel("Name"), fNameField)
                              .build()
                              .row()
                              .columnOne(new JLabel("Location"), fLocationField)
                              .build()
                              .row()
                              .columnOne(new JLabel("Height"), fHeightField)
                              .build()
                              .populate(this);

    fNameField.addPropertyChangeListener("value", fTextFieldListener);
    fLocationField.addPropertyChangeListener("value", fTextFieldListener);
    fHeightField.addPropertyChangeListener("value", fTextFieldListener);

  }

  @Override
  protected boolean applyChangesImpl() {
    ILcdDataObject wayPoint = (ILcdDataObject) getDomainObject();
    if (wayPoint != null) {
      ILcdModel model = getModel();
      try (TLcdLockUtil.Lock autoUnlock = TLcdLockUtil.writeLock(model)) {
        wayPoint.setValue(WayPointDataTypes.NAME, fNameField.getValue());
        TLcdLonLatHeightPoint location = (TLcdLonLatHeightPoint) wayPoint.getValue(WayPointDataTypes.POINT);
        ILcdPoint updatedLocation = (ILcdPoint) fLocationField.getValue();
        double height = (double) fHeightField.getValue();
        location.move3D(updatedLocation.getX(), updatedLocation.getY(), height);
        model.elementChanged(wayPoint, ILcdModel.FIRE_LATER);
      } finally {
        model.fireCollectedModelChanges();
      }
    }
    return true;
  }

  @Override
  protected void updateCustomizerPanelFromObject(boolean aPanelEditable) {
    fNameField.setEditable(aPanelEditable);
    fLocationField.setEditable(aPanelEditable);
    fHeightField.setEditable(aPanelEditable);

    boolean old = fUpdatingUI;
    try {
      fUpdatingUI = true;
      ILcdDataObject domainObject = (ILcdDataObject) getDomainObject();
      if (domainObject != null) {
        try (TLcdLockUtil.Lock autoUnlock = TLcdLockUtil.readLock(getModel())) {
          fNameField.setValue((String) domainObject.getValue(WayPointDataTypes.NAME));
          TLcdLonLatHeightPoint location = (TLcdLonLatHeightPoint) ALcdShape.fromDomainObject(domainObject);
          fLocationField.setValue(location);
          fHeightField.setValue(location.getZ());
        }
      } else {
        fNameField.setValue("");
        fLocationField.setValue(null);
        fHeightField.setValue(0);
      }
    } finally {
      fUpdatingUI = old;
    }
  }

  private void updatePointFormat(Format aPointFormat) {
    boolean old = fUpdatingUI;
    try {
      fUpdatingUI = true;
      fLocationField.setFormat(aPointFormat, fLocationField.getValue());
    } finally {
      fUpdatingUI = old;
    }
  }

  private void updateAltitudeFormat(Format aAltitudeFormat) {
    boolean old = fUpdatingUI;
    try {
      fUpdatingUI = true;
      fHeightField.setFormat(aAltitudeFormat, fHeightField.getValue());
    } finally {
      fUpdatingUI = old;
    }
  }

  /**
   * The location field should be formatted using the point format exposed on the Lucy back-end.
   * When this format changes, the UI must be updated.
   */
  private static class PointFormatListener extends ALcdWeakPropertyChangeListener<WayPointCustomizerPanel> {

    private PointFormatListener(WayPointCustomizerPanel aObjectToModify) {
      super(aObjectToModify);
    }

    @Override
    protected void propertyChangeImpl(WayPointCustomizerPanel aWayPointCustomizerPanel, PropertyChangeEvent aPropertyChangeEvent) {
      String propertyName = aPropertyChangeEvent.getPropertyName();
      if ("defaultLonLatPointFormat".equals(propertyName)) {
        aWayPointCustomizerPanel.updatePointFormat((Format) aPropertyChangeEvent.getNewValue());
      }
    }
  }

  /**
   * The altitude field should be formatted using the altitude format exposed on the Lucy back-end.
   * When this format changes, the UI must be updated
   */
  private static class AltitudeFormatListener extends ALcdWeakPropertyChangeListener<WayPointCustomizerPanel> {
    private AltitudeFormatListener(WayPointCustomizerPanel aObjectToModify) {
      super(aObjectToModify);
    }

    @Override
    protected void propertyChangeImpl(WayPointCustomizerPanel aWayPointCustomizerPanel, PropertyChangeEvent aPropertyChangeEvent) {
      String propertyName = aPropertyChangeEvent.getPropertyName();
      if ("defaultAltitudeFormat".equals(propertyName) || "defaultUserAltitudeUnit".equals(propertyName)) {
        aWayPointCustomizerPanel.updateAltitudeFormat(((ILcyLucyEnv) aPropertyChangeEvent.getSource()).getDefaultAltitudeFormat());
      }
    }
  }
}
