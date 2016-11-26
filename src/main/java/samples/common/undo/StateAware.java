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
package samples.common.undo;

import java.util.Map;

import com.luciad.model.ILcdModel;

/**
 * Interface describing objects that can store their state. Do note that when implementing
 * restoreState, the Map is not necessarily the same as when the state was stored, as for instance
 * someone could store the state right before and right after making a change, and could remove that
 * part of the state that remained the same throughout this change.
 *
 * @see "Memento" design pattern in "Design Patterns - Elements of Reusable Object-Oriented
 *      Software" by Gamma et al.
 */
public interface StateAware {

  /**
   * Stores the state of this object in aMap.
   *
   * @param aMap The Map in which the object can store its state.
   * @param aSourceModel
   */
  void storeState(Map aMap, ILcdModel aSourceModel) throws StateException;

  /**
   * Restores the state of this object.
   *
   * @param aMap The Map containing the state. Note that this map doesn't necessarily contain all
   *             information stored in storeState, because the object managing the states (in this sample the
   *             ModelElementEditedUndoable) may remove duplicate state. As such, implementations of this method
   *             should be able to handle null values, which indicate that the state has not changed.
   * @param aTargetModel
   */
  void restoreState(Map aMap, ILcdModel aTargetModel) throws StateException;
}
