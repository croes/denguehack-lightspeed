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

import com.luciad.lucy.ILcyLucyEnv;
import com.luciad.lucy.gui.customizer.ALcyDomainObjectCustomizerPanel;
import com.luciad.util.ILcdFilter;

/**
 * <p>This customizer panel displays the features of one domain object. It has two columns:
 * one column with the property names, and one with the property values.  If a property itself
 * is has more properties, the child-elements of this property are shown as well.</p>
 *
 * <p>This is a read-only customizer panel: the feature values cannot be changed using this
 * customizer panel.</p>
 *
 * <p>The <code>TLcyDomainObjectContext</code> passed to <code>setObject</code>
 * must match with these requirements:
 * <ul>
 * <li>The domain object of the context must have attributes, therefore it
 * must implement <code>ILcdFeatured</code>.
 * <li>The <code>ILcdModelDescriptor</code> of the <code>ILcdModel</code> of the context should
 * describe the attributes, so it must implement the <code>ILcdFeaturedDescriptor</code> or
 * <code>ILcdFeaturedProvider</code> interface.
 * </ul></p>
 *
 * <p>
 *   If you want to modify the contents of the customizer panel, you need to
 *   extend the {@link TreeTableGUIFactory}.
 * </p>
 */
public class TreeTableViewCustomizerPanel extends ALcyDomainObjectCustomizerPanel {

  private boolean fHideNullOrEmptyNodes = true;
  private boolean fObjectSet = false;
  private ILcyLucyEnv fLucyEnv;

  /**
   * Creates a new <code>TreeTableViewCustomizerPanel</code>.
   * @param aLucyEnv The Lucy environment.
   * @param aObjectContextFilter The filter that verifies if the given object is acceptable. Note that
   * it at least needs to verify the requirements listed in the class comment.
   *  can be used to achieve that.
   */
  public TreeTableViewCustomizerPanel(ILcyLucyEnv aLucyEnv, ILcdFilter aObjectContextFilter) {
    super(aObjectContextFilter, null);
    fLucyEnv = aLucyEnv;
  }

  public boolean isHideNullOrEmptyNodes() {
    return fHideNullOrEmptyNodes;
  }

  public void setHideNullOrEmptyNodes(boolean aHideNullOrEmptyNodes) {
    if (aHideNullOrEmptyNodes != fHideNullOrEmptyNodes) {
      fHideNullOrEmptyNodes = aHideNullOrEmptyNodes;
      firePropertyChange("hideNullOrEmptyNodes", !aHideNullOrEmptyNodes, aHideNullOrEmptyNodes);
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>We only allow to set the object once. Once a non-<code>null</code> object has been set, we
   * only allow to set <code>null</code>, on which the customizer panel will be cleaned up.
   * Afterwards, it is impossible to set another object on the panel. This allows our panels to be
   * cached, and stores the state. E.g. when switching layer selection back and forth, the state of
   * the created table views will remain the same.</p>
   */
  @Override
  public boolean canSetObject(Object aObject) {
    //once an object has been set, only accept null objects or the current object
    if (fObjectSet) {
      return aObject == null || aObject.equals(getObject());
    }
    return super.canSetObject(aObject);
  }

  @Override
  public void setObject(Object aObject) {
    //make sure we remove this as context from the actionbar manager the first time a null object is set
    if (fObjectSet && aObject == null && getObject() != null) {
      fLucyEnv.getUserInterfaceManager().getActionBarManager().disposeContext(this);
    }
    super.setObject(aObject);
    //change the boolean flag as soon as a non-null object is set
    if (aObject != null) {
      fObjectSet = true;
    }
  }

  @Override
  protected void updateCustomizerPanelFromObject(boolean aPanelEditable) {
    //the customizer panel is created for a certain object, and we do not allow to change the object
    //nothing should happen here
  }

  @Override
  protected boolean applyChangesImpl() {
    //the table model keeps itself in sync with the ILcdModel. No need to implement this method, just
    //return true ("no changes")
    return true;
  }
}
