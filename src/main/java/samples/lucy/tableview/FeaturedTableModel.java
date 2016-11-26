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
package samples.lucy.tableview;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import com.luciad.model.ILcdModel;
import com.luciad.util.ILcdFeatured;
import com.luciad.util.ILcdFeaturedDescriptor;
import com.luciad.util.iso19103.ILcdISO19103Measure;
import com.luciad.util.iso19103.TLcdISO19103Measure;
import com.luciad.util.logging.ILcdLogger;
import com.luciad.util.logging.TLcdLoggerFactory;

/**
 * <p>This is an implementation of a swing TableModel for displaying, in a JTable, Objects contained in
 * an ILcdIntegerIndexedModel that implement ILcdFeatured, i.e. Objects with a set of attributes
 * associated to them.</p>
 *
 * <p>This table model will sync itself with the {@code ILcdModel} for which it was constructed. When
 * the {@code ILcdModel} is adjusted, the table model will automatically be adjusted and fire the
 * necessary events to warn the {@code JTable} it has been updated.</p>
 */
class FeaturedTableModel extends LuciadModelWrapper {

  /**
   * Verify whether a {@code FeaturedTableModel} can be created for {@code aModel}
   *
   * @param aModel The model
   *
   * @return {@code true} when a {@code FeaturedTableModel} can be created for
   *         {@code aModel}, {@code false} otherwise.
   */
  public static boolean acceptsFeaturedModel(ILcdModel aModel) {
    if (aModel != null) {
      aModel = retrieveModelForTableView(aModel);
    }
    return aModel != null &&
           //model descriptor is explicitly not a data model descriptor
           !(DataObjectTableModel.acceptsDataObjectModel(aModel)) &&
           //instead it is a featured descriptor
           (aModel.getModelDescriptor() instanceof ILcdFeaturedDescriptor || aModel instanceof ILcdFeaturedDescriptor) &&
           //model is of type ILcdIntegerIndexedModel
           LuciadModelWrapper.acceptsModel(aModel);
  }

  private static final ILcdLogger sLogger = TLcdLoggerFactory.getLogger(FeaturedTableModel.class.getName());
  private final ILcdFeaturedDescriptor fFeaturedDescriptor;
  private final List<Integer> fWarnedList = new ArrayList<Integer>();

  /**
   * <p>Creates a new {@code TLcyFeaturedTableModel} for the given {@code ILcdModel}.</p>
   *
   * @param aModel The model to create the table model for. It should pass the {@link
   *               #acceptsFeaturedModel(ILcdModel)} check
   */
  public FeaturedTableModel(ILcdModel aModel) {
    super(aModel);
    if (!acceptsFeaturedModel(aModel)) {
      throw new IllegalArgumentException("No FeaturedTableModel can be constructed for [" + aModel + "].");
    }
    if (getOriginalModel() instanceof ILcdFeaturedDescriptor) {
      fFeaturedDescriptor = (ILcdFeaturedDescriptor) getOriginalModel();
    } else {
      fFeaturedDescriptor = (ILcdFeaturedDescriptor) getOriginalModel().getModelDescriptor();
    }
  }

  @Override
  public Object getValueAt(int aRowIndex, int aColumnIndex) {
    Object object = getObjectAtRow(aRowIndex);

    Object value = null;
    if (object instanceof ILcdFeatured) {
      ILcdFeatured featured = (ILcdFeatured) object;
      try {
        value = featured.getFeature(aColumnIndex);

        if (value instanceof ILcdISO19103Measure) {
          return new TLcdISO19103Measure(((ILcdISO19103Measure) value).getValue(), ((ILcdISO19103Measure) value).getUnitOfMeasure());
        }

      } catch (IndexOutOfBoundsException e) {
        return null;
      }
    }
    return value;
  }

  @Override
  public String getColumnName(int aColumn) {
    return fFeaturedDescriptor.getFeatureName(aColumn);
  }

  @Override
  public Class getColumnClass(int aColumnIndex) {
    Class feature_class = fFeaturedDescriptor.getFeatureClass(aColumnIndex);
    if (feature_class == null) {
      feature_class = Object.class;
      warnForNullClass(aColumnIndex);
    } else if (ILcdISO19103Measure.class.isAssignableFrom(feature_class)) {
      return TLcdISO19103Measure.class;
    }
    return feature_class;
  }

  private void warnForNullClass(int aFeatureIndex) {
    if (fWarnedList.contains(new Integer(aFeatureIndex))) {
      return;
    }
    sLogger.warn(MessageFormat.format("Feature class for feature {0} is null!",
                                      fFeaturedDescriptor.getFeatureName(aFeatureIndex)), this);
    fWarnedList.add(aFeatureIndex);
  }

  @Override
  public int getColumnCount() {
    return fFeaturedDescriptor.getFeatureCount();
  }

  @Override
  public Object getColumnDescriptor(int aColumnIndex) {
    return null;
  }

  @Override
  public String getColumnTooltipText(int aColumnIndex) {
    return getColumnName(aColumnIndex);
  }
}
