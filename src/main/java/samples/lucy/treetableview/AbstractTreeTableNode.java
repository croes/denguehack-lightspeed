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
package samples.lucy.treetableview;

import org.jdesktop.swingx.treetable.AbstractMutableTreeTableNode;

import com.luciad.util.iso19103.ILcdISO19103Measure;
import com.luciad.util.iso19103.TLcdISO19103Measure;

/**
 * A common abstract class for tree table nodes of <code>ILcdFeatured</code>
 * and <code>ILcdDataObject</code> type tree tables. This class features all mutual
 * methods. 
 */
public abstract class AbstractTreeTableNode extends AbstractMutableTreeTableNode {
  public AbstractTreeTableNode(Object aUserObject) {
    super(aUserObject);
  }

  /**
   * Returns the class type of the node to be rendered.
   * @return The class of the node to be rendered.
   */
  public abstract Class<?> getObjectClass();

  /**
   * <p>Convert the user object as retrieved from the data model to an object suited for the {@code TreeTableModel}.</p>
   *
   * <p>It is not possible to set a renderer for a certain interface, only for a certain class. In order
   * to format {@code ILcdISO19103Measure} instances correctly, we convert them to {@code TLcdISO19103Measure} instances.</p>
   *
   * <p>The implementation of this method must be kept in sync with the {@link #convertUserObjectClass(Object, Class)} method.</p>
   * @param aUserObject The user object
   * @return The converted user object
   */
  static Object convertUserObject(Object aUserObject) {
    if (aUserObject instanceof ILcdISO19103Measure && !(aUserObject instanceof TLcdISO19103Measure)) {
      return new TLcdISO19103Measure(((ILcdISO19103Measure) aUserObject).getValue(), ((ILcdISO19103Measure) aUserObject).getUnitOfMeasure());
    }
    return aUserObject;
  }

  /**
   * <p>As the user object is converted in {@link #convertUserObject(Object)}, we need to adjust the
   * class as well..</p>
   *
   * <p>It is not possible to set a renderer for a certain interface, only for a certain class. In
   * order to format {@code ILcdISO19103Measure} instances correctly, we convert them to {@code
   * TLcdISO19103Measure} instances.</p>
   *
   * <p>The implementation of this method must be kept in sync with the {@link
   * #convertUserObject(Object)} method.</p>
   *
   * @param aUserObject The user object
   * @param aClass      The original class
   *
   * @return The converted user object class
   */
  static Class<?> convertUserObjectClass(Object aUserObject, Class<?> aClass) {
    if (aUserObject instanceof ILcdISO19103Measure && !(aUserObject instanceof TLcdISO19103Measure)) {
      return TLcdISO19103Measure.class;
    }
    return aClass;
  }
}
