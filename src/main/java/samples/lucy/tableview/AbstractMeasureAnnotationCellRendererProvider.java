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

import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import org.jdesktop.swingx.renderer.DefaultTableRenderer;
import org.jdesktop.swingx.renderer.StringValue;

import com.luciad.datamodel.TLcdDataProperty;
import com.luciad.lucy.ILcyLucyEnv;
import com.luciad.util.iso19103.ILcdISO19103Measure;
import com.luciad.util.iso19103.ILcdISO19103UnitOfMeasure;
import com.luciad.util.iso19103.TLcdISO19103Measure;
import com.luciad.util.iso19103.TLcdISO19103MeasureAnnotation;

/**
 * Implementations of this class can provide renderers for values of data properties that have
 * been annotated with a <code>TLcdISO19103MeasureAnnotation</code>. It makes sure that the values
 * are formatted consistent with their unit of measure.
 */
public abstract class AbstractMeasureAnnotationCellRendererProvider implements ITableCellRendererProvider {

  private final MeasureRenderer fRenderer;

  public AbstractMeasureAnnotationCellRendererProvider(StringValue aMeasureStringValue, int aAlignment) {
    if (aMeasureStringValue == null) {
      throw new IllegalArgumentException("The passed measure string value can't be null!");
    }
    fRenderer = new MeasureRenderer(aMeasureStringValue, aAlignment);
  }

  public AbstractMeasureAnnotationCellRendererProvider(ILcyLucyEnv aLucyEnv, int aAlignment) {
    this(createCompositeStringValue(aLucyEnv), aAlignment);
  }

  private static StringValue createCompositeStringValue(ILcyLucyEnv aLucyEnv) {
    CompositeStringValue composite = new CompositeStringValue();
    //make sure null values are handled consistently
    composite.addStringValue(ValueUtil.createNullStringValue());
    //make sure the renderer is capable of formatting ISO1903Measures, ...
    composite.addStringValue(ValueUtil.createISO19103MeasureStringValue(aLucyEnv));
    return composite;
  }

  @Override
  public boolean canProvideRenderer(JTable aTable, int aRow, int aColumn) {
    ILcdISO19103UnitOfMeasure uom = findUnitOfMeasure(aTable, aRow, aColumn);
    return uom != null && isValidValue(aTable.getValueAt(aRow, aColumn), uom);
  }

  @Override
  public TableCellRenderer provideRenderer(JTable aTable, int aRow, int aColumn) {
    ILcdISO19103UnitOfMeasure uom = findUnitOfMeasure(aTable, aRow, aColumn);
    if (uom != null && isValidValue(aTable.getValueAt(aRow, aColumn), uom)) {
      fRenderer.setUnitOfMeasure(uom);
      return fRenderer;
    } else {
      throw new IllegalArgumentException("Can't provide a renderer, call canProvideRenderer first!");
    }
  }

  /**
   * The specifics of how to get a data property to check for annotations is handles by the subclasses.
   */
  protected abstract TLcdDataProperty getDataProperty(JTable aTable, int aRow, int aColumn);

  /**
   * Retrieve the unit of measure from a TLcdISO19103MeasureAnnotation.
   */
  private ILcdISO19103UnitOfMeasure findUnitOfMeasure(JTable aTable, int aRow, int aColumn) {
    TLcdDataProperty property = getDataProperty(aTable, aRow, aColumn);
    if (property != null) {
      TLcdISO19103MeasureAnnotation annotation = property.getAnnotation(TLcdISO19103MeasureAnnotation.class);
      if (annotation != null) {
        return annotation.getUnitOfMeasure();
      }
    }
    return null;
  }

  /**
   * Checks whether or not the current value can be used to generate a measure. Value null is handled
   * separately.
   */
  private static boolean isValidValue(Object aValue, ILcdISO19103UnitOfMeasure aUOM) {
    return aValue == null || convertValueToMeasure(aValue, aUOM) != null;
  }

  private static ILcdISO19103Measure convertValueToMeasure(Object aValue, ILcdISO19103UnitOfMeasure aUOM) {
    if (aValue instanceof ILcdISO19103Measure) {
      if (((ILcdISO19103Measure) aValue).getUnitOfMeasure().getMeasureType() ==
          aUOM.getMeasureType()) {
        return (ILcdISO19103Measure) aValue;
      } else {
        return null;
      }
    } else if (aValue instanceof Number) {
      return new TLcdISO19103Measure(((Number) aValue).doubleValue(), aUOM);
    } else if (aValue instanceof String) {
      try {
        double value = Double.parseDouble((String) aValue);
        return new TLcdISO19103Measure(value, aUOM);
      } catch (NumberFormatException e) {
        return null;
      }
    }
    return null;
  }

  /**
   * Measure renderer that converts values to measures.
   */
  static class MeasureRenderer extends DefaultTableRenderer {

    private ILcdISO19103UnitOfMeasure fUnitOfMeasure;

    private MeasureRenderer(StringValue aMeasureStringValue, int aAlignment) {
      super(aMeasureStringValue, aAlignment);
    }

    public void setUnitOfMeasure(ILcdISO19103UnitOfMeasure aUnitOfMeasure) {
      fUnitOfMeasure = aUnitOfMeasure;
    }

    @Override
    public String getString(Object value) {
      ILcdISO19103Measure convertedValue = convertValueToMeasure(value, fUnitOfMeasure);
      return super.getString(convertedValue);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
      ILcdISO19103Measure convertedValue = convertValueToMeasure(value, fUnitOfMeasure);
      return super.getTableCellRendererComponent(table, convertedValue, isSelected, hasFocus, row, column);
    }
  }
}
