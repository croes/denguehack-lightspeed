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
package samples.lightspeed.common.layercontrols;

import com.luciad.gui.ILcdIcon;
import com.luciad.view.ILcdLayer;
import com.luciad.view.ILcdTreeLayered;

import samples.common.layerControls.actions.AbstractPropertyBasedLayerTreeToggleAction;


/**
 * Layer tree toggle action that allows toggling (adding/removing) a style filter.
 */
public class StyleFilterLayerAction extends AbstractPropertyBasedLayerTreeToggleAction {
  /**
   * <p>Create a new toggle action for the property with name <code>aLayerPropertyName</code>.</p>
   *
   * @param aLayered           the <code>ILcdTreeLayered</code> instance to create the action for
   * @param aLayerPropertyName the name of the property to create a toggle action for
   * @param aIcon              the icon for the action
   */
  public StyleFilterLayerAction(ILcdTreeLayered aLayered, String aLayerPropertyName, ILcdIcon aIcon) {
    super(aLayered, aLayerPropertyName, aIcon);
  }

  /*// The style filter that can be added or removed using this action
  private final ILspStyleFilter fStyleFilter;

  *//**
   * Create an action to toggle a certain style of a layer.
   *
   * @param aLayered          the {@code ILcdTreeLayered} to create the action for
   * @param aPropertyName     the property name
   * @param aShortDescription a short textual description
   * @param aIcon             the icon to display on the button
   * @param aStyleFilter      the filter that is enabled/disabled by this action
   *//*
  public StyleFilterLayerAction( ILcdTreeLayered aLayered,
                                 String aPropertyName,
                                 String aShortDescription,
                                 ILcdIcon aIcon,
                                 ILspStyleFilter aStyleFilter ) {
    super( aLayered, aPropertyName, aIcon );
    setShortDescription( aShortDescription );
    fStyleFilter = aStyleFilter;
  }

  */

  /**
   * Gets all style capable painters set on the given layer.
   *
   * @param aLayer the layer to consider
   *
   * @return all style capable painters set on the given layer
   *//*
  private Collection<ILspStylablePainter> getPainters( ILspInteractivePaintableLayer aLayer ) {
    TLcdIdentityHashSet<ILspStylablePainter> result = new TLcdIdentityHashSet<ILspStylablePainter>();

    Collection<TLspPaintRepresentation> paintReps
        = aLayer.getPaintRepresentations();

    for ( TLspPaintRepresentation paintRep : paintReps ) {
      ILspPainter painter = aLayer.getPainter( paintRep );
      if ( painter instanceof ILspStylablePainter) {
        result.add( (ILspStylablePainter) painter );
      }
    }

    return result;
  }*/
  @Override
  protected boolean layerSupportsProperty(ILcdLayer aLayer) {
    return false;
  }

  @Override
  protected void setLayerProperty(boolean aNewValue, ILcdLayer aLayer) {
  }

  @Override
  protected boolean getLayerProperty(ILcdLayer aLayer) {
    return false;
  }
}
