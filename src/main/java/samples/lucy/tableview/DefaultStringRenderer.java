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

import javax.swing.AbstractButton;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import org.jdesktop.swingx.renderer.DefaultTableRenderer;
import org.jdesktop.swingx.renderer.StringValue;

import com.luciad.lucy.ILcyLucyEnv;
import com.luciad.model.ILcdModel;

/**
 * <p>Extension of {@link DefaultTableRenderer} that is able to
 * correctly render almost any object passed to it. It also features special formatting for ILcdPoint,
 * java.util.Date and ILcdISO19103Measure.</p>
 *
 * <p>Whenever possible, the formats registered to ILcyLucyEnv are used.  When for example
 * a geodetic point needs to be formatted, the ILcyLucyEnv.getDefaultLonLatPointFormat() is used,
 * or when a distance needs to be formatted, the ILcyLucyEnv.getDefaultDistanceFormat() is used.</p>
 *
 * <p>The implementation uses instanceof to find out whether a feature value is an ILcdPoint, an
 * ILcdISO19103Measure, a Double etc... . We prefer instanceof over the more common practice of
 * registering a renderer for a specific class. Doing so however requires the column class of the
 * table model to use those exact same interfaces (say ILcdPoint). If the column class is for example
 * a sub interface (say ILcd2DEditablePoint) or a class (say TLcdXYPoint), JTable will fail to find
 * this renderer.</p>
 */
public class DefaultStringRenderer extends DefaultTableRenderer {

  boolean fListenerAdded = false;
  private ILcyLucyEnv fLucyEnv;

  public DefaultStringRenderer(ILcyLucyEnv aLucyEnv, ILcdModel aModel) {
    this(ValueUtil.createCompositeStringValue(aLucyEnv, aModel.getModelReference()), aLucyEnv);
  }

  private DefaultStringRenderer(StringValue aConverter, ILcyLucyEnv aLucyEnv) {
    super(aConverter);
    fLucyEnv = aLucyEnv;
  }

  @Override
  public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    //since this renderer uses the Lucy back-end formats to format the objects,
    //a listener must be added to the back-end which triggers a repaint of the table when
    //one of the formats change. The table is not available on construction of this renderer, so
    //the listener is added the first time the table is passed in this method
    //this assumes the renderer is not shared between multiple tables
    if (!(fListenerAdded)) {
      fLucyEnv.addPropertyChangeListener(new LucyFormatListener(table));
      fListenerAdded = true;
    }
    Component comp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
    handleAlignment(comp, table.getColumnClass(column));
    return comp;
  }

  protected void handleAlignment(Component aComponent, Class<?> aColumnClass) {
    if (NumberPropertyUtil.isNumberClass(aColumnClass)) {
      setAlignment(aComponent, SwingConstants.RIGHT);
    }
  }

  @SuppressWarnings("MagicConstant")
  protected void setAlignment(Component aComponent, int aAlignment) {
    if (aComponent instanceof JLabel) {
      ((JLabel) aComponent).setHorizontalAlignment(aAlignment);
    } else if (aComponent instanceof AbstractButton) {
      ((AbstractButton) aComponent).setHorizontalAlignment(aAlignment);
    } else if (aComponent instanceof JTextField) {
      ((JTextField) aComponent).setHorizontalAlignment(aAlignment);
    }
  }
}
