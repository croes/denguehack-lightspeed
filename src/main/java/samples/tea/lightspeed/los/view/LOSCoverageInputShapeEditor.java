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
package samples.tea.lightspeed.los.view;

import com.luciad.shape.shape2D.ILcd2DEditableArcBand;
import com.luciad.view.lightspeed.editor.TLspArcBandEditor;
import com.luciad.view.lightspeed.editor.TLspEditContext;
import com.luciad.view.lightspeed.editor.handle.ALspEditHandle;
import samples.tea.lightspeed.los.model.LOSCoverageInputShape;

/**
 * <p>An editor for <code>LOSCoverageInputShape</code>. This editor extends the
 * <code>TLspArcBandEditor</code> and adds functionality to
 * <ul>
 *   <li>remove the minimum radius point drag handles</li>
 *   <li>optionally remove all other point-based handles, to prevent resizing.</li>
 * </ul>
 * </p>
 */
public class LOSCoverageInputShapeEditor extends TLspArcBandEditor {

  private boolean fCreatePointHandles;

  /**
   * Creates a new coverage editor for the given max radius size
   * @param aCreatePointHandles if true, will create point-based handles; false will only create the object translation handle.
   */
  public LOSCoverageInputShapeEditor( boolean aCreatePointHandles ) {
    fCreatePointHandles = aCreatePointHandles;
  }

  @Override
  protected ALspEditHandle createCenterHandle( ILcd2DEditableArcBand aArcBand, TLspEditContext aContext ) {
    if ( fCreatePointHandles ) {
      return super.createCenterHandle( aArcBand, aContext );
    }else{
      return null;
    }
  }

  @Override
  protected ALspEditHandle createMaxRadiusStartCornerHandle( ILcd2DEditableArcBand aArcBand, TLspEditContext aContext ) {
    if ( fCreatePointHandles ) {
      return super.createMaxRadiusStartCornerHandle( aArcBand, aContext );
    }else{
      return null;
    }
  }

  @Override
  protected ALspEditHandle createMaxRadiusEndCornerHandle( ILcd2DEditableArcBand aArcBand, TLspEditContext aContext ) {
    if ( fCreatePointHandles ) {
      return super.createMaxRadiusEndCornerHandle( aArcBand, aContext );
    }else{
      return null;
    }
  }

  @Override
  protected ALspEditHandle createMinRadiusHandle( ILcd2DEditableArcBand aArcBand, TLspEditContext aContext ) {
    return null;
  }

  @Override
  protected ALspEditHandle createMaxRadiusHandle( ILcd2DEditableArcBand aArcBand, TLspEditContext aContext ) {
    if ( fCreatePointHandles ) {
      return super.createMaxRadiusHandle( aArcBand, aContext );
    }else{
      return null;
    }
  }

  @Override
  protected ALspEditHandle createMinRadiusEndCornerHandle( ILcd2DEditableArcBand aArcBand, TLspEditContext aContext ) {
    //Return null to prevent min radius corner handle from being created
    return null;
  }

  @Override
  protected ALspEditHandle createMinRadiusStartCornerHandle( ILcd2DEditableArcBand aArcBand, TLspEditContext aContext ) {
    //Return null to prevent min radius corner handle from being created
    return null;
  }

  @Override
  public boolean canEdit( TLspEditContext aContext ) {
    //We can only edit LOS Coverage Input shape objects
    return aContext.getGeometry() instanceof LOSCoverageInputShape;
  }
}
