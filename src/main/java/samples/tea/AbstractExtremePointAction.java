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
package samples.tea;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.Vector;

import javax.swing.JDialog;
import javax.swing.SwingUtilities;

import com.luciad.gui.ALcdAction;
import com.luciad.gui.TLcdImageIcon;
import com.luciad.model.ILcdModel;
import com.luciad.reference.ILcdGeoReference;
import com.luciad.shape.ILcdShape;
import com.luciad.tea.ILcdAltitudeProvider;
import com.luciad.tea.ILcdReferencedMatrixView;
import com.luciad.tea.TLcdAltitudeMatrixViewFactory;
import com.luciad.tea.TLcdExtremePointFinder;
import com.luciad.util.ILcdFireEventMode;
import com.luciad.util.ILcdStatusListener;
import com.luciad.util.TLcdNoBoundsException;
import com.luciad.util.TLcdOutOfBoundsException;

import samples.gxy.common.ProgressUtil;

/**
 * A sample action which computes the highest or lowest N points that lie inside a polygon.
 */
public abstract class AbstractExtremePointAction extends ALcdAction implements Runnable {

  private Component            fParentComponent;
  private ILcdShape            fShape;
  private ILcdGeoReference     fShapeReference;
  private ILcdAltitudeProvider fAltitudeProvider;

  private TLcdExtremePointFinder fExtremePointFinder = new TLcdExtremePointFinder();
  private ILcdModel fMinimumPointModel;
  private ILcdModel fMaximumPointModel;

  protected boolean fRecomputeMatrix;

  private int     fRequestedPoints    =    5;
  private double  fSeparationDistance = 1000;
  private double  fSeparationHeight   =  200;

  /**
   * @param aParentComponent  Is required only for parenting the progress dialog.
   * @param aShape            The shape within which to search for highest points.
   * @param aShapeReference   The reference of the shape.
   * @param aAltitudeProvider The altitude provider for the given shape.
   */
  public AbstractExtremePointAction(Component aParentComponent,
                                    ILcdShape aShape,
                                    ILcdGeoReference aShapeReference,
                                    ILcdAltitudeProvider aAltitudeProvider) {
    super("Extreme Points", new TLcdImageIcon("images/gui/i16_terrain.gif"));
    fParentComponent  = aParentComponent;
    fShape            = aShape;
    fShapeReference   = aShapeReference;
    fAltitudeProvider = aAltitudeProvider;
    fRecomputeMatrix  = true;
  }

  public void actionPerformed( ActionEvent aActionEvent ) {
    Thread thread = new Thread( this );
    thread.setPriority( Thread.MIN_PRIORITY );
    thread.start();
  }

  public void run() {
    if ( fShape == null )
      return;

    JDialog dialog = null;
    try {
      if ( fRecomputeMatrix ) {
        dialog = ProgressUtil.createProgressDialog(
                fParentComponent, "Creating elevation matrix"
        );
        ProgressUtil.showDialog( dialog );

        TLcdAltitudeMatrixViewFactory factory     = new TLcdAltitudeMatrixViewFactory();
        ILcdReferencedMatrixView matrix_view = createAreaAltitudeMatrixView(factory);
        fExtremePointFinder.setReferencedMatrixView( matrix_view );

        fRecomputeMatrix = false;
      }

      if ( dialog != null ) {
        fExtremePointFinder.addStatusListener( (ILcdStatusListener) dialog );
        ProgressUtil.showDialog( dialog );
      }

      if ( dialog != null ) {
        dialog.setTitle( "Finding minima" );
      }
      List minimum_peaks = fExtremePointFinder.findExtremePoints(
              TLcdExtremePointFinder.Type.LOWEST_POINTS,
              fRequestedPoints,
              fSeparationHeight,
              fSeparationDistance
      );
      final Vector minimum_peak_vector = new Vector( minimum_peaks );
      SwingUtilities.invokeLater( new Runnable() {
        public void run() {
          if ( fMinimumPointModel != null ) {
            fMinimumPointModel.removeAllElements( ILcdFireEventMode.FIRE_LATER );
            fMinimumPointModel.addElements( minimum_peak_vector, ILcdFireEventMode.FIRE_LATER );
            fMinimumPointModel.fireCollectedModelChanges();
          }
        }
      } );

      if ( dialog != null ) {
        dialog.setTitle( "Finding maxima" );
      }
      List maximum_peaks = fExtremePointFinder.findExtremePoints(
              TLcdExtremePointFinder.Type.HIGHEST_POINTS,
              fRequestedPoints,
              fSeparationHeight,
              fSeparationDistance
      );
      final Vector maximum_peak_vector = new Vector( maximum_peaks );
      SwingUtilities.invokeLater( new Runnable() {
        public void run() {
          if ( fMaximumPointModel != null ) {
            fMaximumPointModel.removeAllElements( ILcdFireEventMode.FIRE_LATER );
            fMaximumPointModel.addElements( maximum_peak_vector, ILcdFireEventMode.FIRE_LATER );
            fMaximumPointModel.fireCollectedModelChanges();
          }
        }
      } );
    }
    catch ( Exception e ) {
      throw new RuntimeException( "Could not compute visibility: " + e.getMessage() );
    }
    finally {
      if ( dialog != null ) {
        ProgressUtil.hideDialog( dialog );
        fExtremePointFinder.removeStatusListener( (ILcdStatusListener) dialog );
      }
    }
  }

  protected abstract ILcdReferencedMatrixView createAreaAltitudeMatrixView(TLcdAltitudeMatrixViewFactory aFactory) throws TLcdOutOfBoundsException, TLcdNoBoundsException;

  public ILcdModel getMinimumPointModel() {
    return fMinimumPointModel;
  }

  public void setMinimumPointModel( ILcdModel aMinimumPointModel ) {
    fMinimumPointModel = aMinimumPointModel;
  }

  public ILcdModel getMaximumPointModel() {
    return fMaximumPointModel;
  }

  public void setMaximumPointModel( ILcdModel aMaximumPointModel ) {
    fMaximumPointModel = aMaximumPointModel;
  }

  public int getRequestedPoints() {
    return fRequestedPoints;
  }

  public void setRequestedPoints( int aRequestedPoints ) {
    fRequestedPoints = aRequestedPoints;
  }

  public double getSeparationDistance() {
    return fSeparationDistance;
  }

  public void setSeparationDistance( double aSeparationDistance ) {
    fSeparationDistance = aSeparationDistance;
  }

  public double getSeparationHeight() {
    return fSeparationHeight;
  }

  public void setSeparationHeight( double aSeparationHeight ) {
    fSeparationHeight = aSeparationHeight;
  }

  public ILcdShape getShape() {
    return fShape;
  }

  public void setShape( ILcdShape aShape ) {
    fShape = aShape;
    fRecomputeMatrix = true;
  }

  public ILcdGeoReference getShapeReference() {
    return fShapeReference;
  }

  public void setShapeReference( ILcdGeoReference aShapeReference ) {
    fShapeReference     = aShapeReference;
    fRecomputeMatrix    = true;
  }

  public ILcdAltitudeProvider getAltitudeProvider() {
    return fAltitudeProvider;
  }

  public void setAltitudeProvider(ILcdAltitudeProvider aAltitudeProvider) {
    fAltitudeProvider = aAltitudeProvider;
    fRecomputeMatrix = true;
  }

}
