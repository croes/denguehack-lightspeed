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
package samples.decoder.kml22.common.modelcontenttree;

import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelContainer;
import com.luciad.model.TLcdModelContainerEvent;

/**
 * Extends the basic functionality of the <code>TLcdModelContainerEvent</code>
 * with a <code>TreeModelObject</code> of which this event originates from.
 */
public class ModelNodeContainerEvent extends TLcdModelContainerEvent{
  private TreeModelObject fModelObject;

  /**
   * An event that indicates that aModelContainer has changed in aID manner, which involves aModel.
   *
   * @param aModelObject    a <code>TreeModelObject</code> from which this event originates.
   * @param aModelContainer the model container that has changed.
   * @param aID             description of the change to the model container. Compare to
   *                        CONTENT_CHANGED, MODEL_ADDED, MODEL_REMOVED.
   * @param aModel          the model which caused the change to the model container.
   */
  public ModelNodeContainerEvent( TreeModelObject aModelObject, ILcdModelContainer aModelContainer, int aID, ILcdModel aModel ) {
    super( aModelContainer, aID, aModel );
    fModelObject = aModelObject;
  }

  /**
   * Gets the <code>TreeModelObject</code> from which this event originates.
   * @return the <code>TreeModelObject</code> from which this event originates.
   */
  public TreeModelObject getModelObject() {
    return fModelObject;
  }
}
