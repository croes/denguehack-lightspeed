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

import com.luciad.geodesy.TLcdGeodeticDatum;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelListener;
import com.luciad.model.TLcdModelChangedEvent;
import com.luciad.model.TLcdModelDescriptor;
import com.luciad.model.TLcdVectorModel;
import com.luciad.reference.ILcdGeoReference;
import com.luciad.reference.TLcdGeodeticReference;
import com.luciad.shape.shape3D.TLcdXYZPoint;
import com.luciad.tea.ILcdAltitudeProvider;
import com.luciad.tea.ILcdLOSCoverageMatrix;
import com.luciad.tea.TLcdCoverageAltitudeMode;
import com.luciad.tea.lightspeed.los.TLspLOSCalculator;
import com.luciad.tea.lightspeed.los.TLspLOSProperties;
import com.luciad.tea.lightspeed.los.view.TLspLOSCoveragePainter;
import com.luciad.util.concurrent.TLcdLockUtil;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.ILspLayerFactory;
import com.luciad.view.lightspeed.layer.TLspPaintState;
import com.luciad.view.lightspeed.layer.shape.TLspShapeLayerBuilder;
import com.luciad.view.lightspeed.style.ILspWorldElevationStyle.ElevationMode;
import com.luciad.view.lightspeed.style.TLspFillStyle;
import com.luciad.view.lightspeed.style.TLspLineStyle;
import com.luciad.view.lightspeed.style.styler.ALspStyler;
import com.luciad.view.lightspeed.style.styler.TLspStyler;
import samples.tea.lightspeed.los.model.LOSCoverageInputShape;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * A Layer factory for models that contain a single <code>LOSCoverageInputShape</code>.
 * </p>
 * <p>
 * This layer factory is used by the sample to generate LOS coverages that
 * can be edited at interactive rates.
 * </p>
 * <p>
 * The prerequisite for using this layer factory is that the altitude provider
 * and LOS coverage stylers are set when the {@link #createLayers(ILcdModel)}
 * method is called. These can be set using the {@link #setAltitudeProvider(ILcdAltitudeProvider)}
 * and {@link #setLOSCoverageStyler(ALspStyler)} methods
 * respectively.
 * </p>
 * <p>
 * Internally, this layer factory creates 2 layers: One editable input layer
 * and one non-editable output layer. The output layer will be set up to
 * listen to changes in the input layer, and generate ILcdLOSCoverageMatrix instances
 * accordingly, using the hardware accelerated
 * {@link TLspLOSCalculator TLspLOSCalculator}.
 * A LOS result is calculated whenever a
 * <code>LOSCoverageInputShape</code> is added or modified in the input model.
 * </p>
 */
public class LOSLayerFactory implements ILspLayerFactory {

  public static final String LOS_INPUT_LAYER_LABEL = "LOS Input";
  public static final String LOS_OUTPUT_LAYER_LABEL = "LOS Output";
  private ILcdAltitudeProvider fAltitudeProvider;
  private ALspStyler fLOSCoverageStyler;
  private List<LOSModelEventMediator> fLOSModelEventMediatorList;
  private TLspLOSCalculator fLOSCalculator;

  /**
   * Creates an empty LOS Layer Factory. The altitude provider and the
   * LOS coverage style provider must be set before using this
   * layer factory.
   * @param aLOSCalculator a LOS calculator to use for LOS calculations
   */
  public LOSLayerFactory( TLspLOSCalculator aLOSCalculator ) {
    fLOSCalculator = aLOSCalculator;
    fLOSModelEventMediatorList = new ArrayList<LOSModelEventMediator>();
  }

  /**
   * Disposes the LOS calculator so that it can release its resources
   */
  public void dispose(){
    fLOSCalculator.dispose();
  }

  @Override
  public boolean canCreateLayers( ILcdModel aModel ) {
    return fLOSCoverageStyler != null &&
           fAltitudeProvider != null &&
           aModel.getModelDescriptor().getTypeName().equals( "LOSInputModel" );
  }

  @Override
  public Collection<ILspLayer> createLayers( ILcdModel aModel ) {
    if ( fLOSCoverageStyler == null ) {
      throw new IllegalStateException( "The los coverage style provider must be set before using this layer factory." );
    }

    if ( fAltitudeProvider == null ) {
      throw new IllegalStateException( "The altitude provider must be set before using this layer factory." );
    }

    //We create one layer for input, and one for output
    ILcdModel inputModel = aModel;
    ILspLayer inputLayer = createInputLayer( inputModel );

    TLcdVectorModel outputModel = createOutputModel();
    ILspLayer outputLayer = createOutputLayer( outputModel );

    LOSModelEventMediator losModelEventMediator = new LOSModelEventMediator( inputModel, outputModel, fAltitudeProvider, fLOSCalculator );
    fLOSModelEventMediatorList.add( losModelEventMediator );

    ArrayList<ILspLayer> losLayers = new ArrayList<ILspLayer>();
    losLayers.add( inputLayer );
    losLayers.add( outputLayer );
    return losLayers;
  }

  public ILcdAltitudeProvider getAltitudeProvider() {
    return fAltitudeProvider;
  }

  public void setAltitudeProvider( ILcdAltitudeProvider aAltitudeProvider ) {
    fAltitudeProvider = aAltitudeProvider;
  }

  public ALspStyler getLOSCoverageStyler() {
    return fLOSCoverageStyler;
  }

  public void setLOSCoverageStyler( ALspStyler aStyler ) {
    fLOSCoverageStyler = aStyler;
  }

  /**
   * This method should be called when there is a change in the altitude provider. This method will
   * invalidate output layers generated by the <code>createLayers</code> method.
   */
  public void invalidateGeneratedLOSOutput() {
    for ( LOSModelEventMediator losModelEventMediator : fLOSModelEventMediatorList ) {
      losModelEventMediator.invalidateOutput();
    }
  }

  /**
   * Creates the input layer containing <code>LOSCoverageInputShape</code> instances. This
   * layer will be painted by a <code>TLspShapePainter</code>. The layer will be editable, and
   * can be edited with a <code>LOSCoverageInputShapeEditor</code>.
   * @param aModel a model containing <code>LOSCoverageInputShape</code> instances
   * @return the input layer
   */
  private ILspLayer createInputLayer( final ILcdModel aModel ) {
    TLspShapeLayerBuilder layerBuilder = TLspShapeLayerBuilder.newBuilder();
    layerBuilder.model( aModel )
                .layerType( ILspLayer.LayerType.EDITABLE )
                .label( LOS_INPUT_LAYER_LABEL )
                .selectable( true )
                .bodyEditable( true )
                .bodyEditor( new LOSCoverageInputShapeEditor( true ) )
                .bodyStyler( TLspPaintState.REGULAR,
                             new TLspStyler( TLspLineStyle.newBuilder().color( Color.white ).elevationMode( ElevationMode.ON_TERRAIN ).build(),
                                             TLspFillStyle.newBuilder().color( new Color( 0.2f, 0.2f, 0.2f, 0.4f ) ).elevationMode( ElevationMode.ON_TERRAIN ).build() ) )
                .bodyStyler( TLspPaintState.SELECTED,
                             new TLspStyler( TLspLineStyle.newBuilder().color( Color.red ).elevationMode( ElevationMode.ON_TERRAIN ).build(),
                                             TLspFillStyle.newBuilder().color( new Color( 0.2f, 0.2f, 0.2f, 0.4f ) ).elevationMode( ElevationMode.ON_TERRAIN ).build() ) )
                .bodyStyler( TLspPaintState.EDITED,
                             new TLspStyler( TLspLineStyle.newBuilder().color( Color.green ).elevationMode( ElevationMode.ON_TERRAIN ).width( 2.0f ).build(),
                                             TLspFillStyle.newBuilder().color( new Color( 0.2f, 0.2f, 0.2f, 0.4f ) ).elevationMode( ElevationMode.ON_TERRAIN ).build() ) );
    return layerBuilder.build();
  }

  /**
   * Creates the output model in which LOS Coverages Matrix instances will be placed.
   * @return the output model
   */
  private TLcdVectorModel createOutputModel() {
    TLcdVectorModel vectorModel = new TLcdVectorModel();
    String losDescriptorString = "LOSOutputModel";
    vectorModel.setModelDescriptor( new TLcdModelDescriptor( losDescriptorString, losDescriptorString, losDescriptorString ) );
    vectorModel.setModelReference( new TLcdGeodeticReference( new TLcdGeodeticDatum() ) );
    return vectorModel;
  }

  /**
   * Creates the output layer for the output model. This model should contains <code>ILcdCoverageMatrix</code>
   * instances. This layer will have a <code>TLspLOSCoveragePainter</code> as its body painter, and will
   * not be editable.
   * @param aModel the output model that contains or will contain LOS Coverage Matrices
   * @return the output layer
   */
  private ILspLayer createOutputLayer( ILcdModel aModel ) {
    TLspShapeLayerBuilder layerBuilder = TLspShapeLayerBuilder.newBuilder();
    layerBuilder.model( aModel )
                .layerType( ILspLayer.LayerType.EDITABLE )
                .label( LOS_OUTPUT_LAYER_LABEL )
                .selectable( false )
                .selectableSupported( false )
                .bodyEditable( false )
                .bodyPainter( new TLspLOSCoveragePainter() )
                .bodyStyler( TLspPaintState.REGULAR, fLOSCoverageStyler )
                .culling( false );
    return layerBuilder.build();
  }

  ////////////////////////////////////////////////////////////////////////////////
  ///// LOS MEDIATOR                                                      ////////
  ////////////////////////////////////////////////////////////////////////////////

  /**
   * The LOS model event mediator listens to changes in an input model, and converts
   * these changes to changes in the output model.
   *
   * <ul>
   * <li>If any elements were removed from the input model, the mediator
   * will remove matching elements in the output model</li>
   * <li>If any elements were added to the input model, the mediator
   * will create matching elements in the output model</li>
   * <li>If any elements were changed in the input model, the mediator
   * will update the matching elements of the output model.</li>
   * </ul>
   */
  private static class LOSModelEventMediator {
    private ILcdModel fInputModel;
    private ILcdModel fOutputModel;
    private ILcdAltitudeProvider fAltitudeProvider;
    private InputModelListener fInputModelListener = new InputModelListener();
    private Map<LOSCoverageInputShape, ILcdLOSCoverageMatrix> fInputToOutputMap;
    private TLspLOSCalculator fLOSCalculator;

    /**
     * <p>Creates a new LOS model event mediator for the given parameters</p>
     * <p>This constructor registers all existent elements in the input model and
     * creates output elements in the given output model. All necessary listeners will
     * be registered so that changes in the input model result in changes to the
     * output model. The models are kept in sync.</p>
     *
     * @param aInputModel       The input model that will be listened to
     * @param aOutputModel      The output model that will be kept in sync with the input
     * @param aAltitudeProvider The altitude provider used in the LOS calculations
     * @param aLOSCalculator    The LOS calculator to be used by model event mediator
     */
    private LOSModelEventMediator( ILcdModel aInputModel,
                                   ILcdModel aOutputModel,
                                   ILcdAltitudeProvider aAltitudeProvider,
                                   TLspLOSCalculator aLOSCalculator ) {
      fLOSCalculator = aLOSCalculator;
      fInputToOutputMap = new IdentityHashMap<LOSCoverageInputShape, ILcdLOSCoverageMatrix>();
      fInputModel = aInputModel;
      fOutputModel = aOutputModel;
      fAltitudeProvider = aAltitudeProvider;
      fInputModel.addModelListener( fInputModelListener );
      initialize();
    }

    /**
     * Initializes this <code>LOSModelEventMediator</code>.
     */
    private void initialize() {
      Enumeration inputElements = fInputModel.elements();
      while ( inputElements.hasMoreElements() ) {
        Object inputElement = inputElements.nextElement();
        registerNewInputShape( inputElement );
      }
      fOutputModel.fireCollectedModelChanges();
    }

    /**
     * <p>
     * Registers a new object that gas been added to the input model.
     * </p>
     * <p>
     * This method should be called at initialization and whenever a new object has been
     * added.
     * </p>
     *
     * @param aInputElement An input element that was not yet previously registered.
     */
    private void registerNewInputShape( Object aInputElement ) {
      try {
        TLcdLockUtil.writeLock( fOutputModel );
        if ( aInputElement instanceof LOSCoverageInputShape ) {
          ILcdLOSCoverageMatrix losOutputObject = createLOSOutputObject( ( LOSCoverageInputShape ) aInputElement );
          if ( losOutputObject != null ) {
            fOutputModel.addElement( losOutputObject, ILcdModel.FIRE_LATER );
            fInputToOutputMap.put( ( LOSCoverageInputShape ) aInputElement, losOutputObject );
          }
        }
      }
      finally {
        TLcdLockUtil.writeUnlock( fOutputModel );
      }
    }
    /**
     * This method converts the given <code>LOSCoverageInputShape</code> into
     * a <code>TLspLOSProperties</code>, which it then passes to the <code>TLspLOSCalculator</code>
     * of this object. The resulting <code>ILcdLOSCoverageMatrix</code> is returned.
     *
     * @param aLOSCoverageInputShape A LOS coverage input shape that contains all necessary parameters
     *
     * @return An <code>ILcdLOSCoverageMatrix</code> that can be painted with <code>TLspLOSCoveragePainter</code>,
     *         based on the parameters of the input shape.
     */
    private ILcdLOSCoverageMatrix createLOSOutputObject( LOSCoverageInputShape aLOSCoverageInputShape ) {
      TLspLOSProperties losProperties = createLOSProperties( aLOSCoverageInputShape );
      return fLOSCalculator.calculateLOS( losProperties, fAltitudeProvider, ( ILcdGeoReference ) fOutputModel.getModelReference() );
    }

    /**
     * Updates the output object that was constructed using the given input shape.
     * @param aLOSCoverageInputShape An input shape that was previously used to create a LOS output object.
     */
    private void updateLOSOutputObject( LOSCoverageInputShape aLOSCoverageInputShape ){
      TLspLOSProperties losProperties = createLOSProperties( aLOSCoverageInputShape );
      ILcdLOSCoverageMatrix losOutputObject = fInputToOutputMap.get( aLOSCoverageInputShape );
      if ( losOutputObject!=null ) {
        fLOSCalculator.updateLOSCalculationSFCT( losProperties, fAltitudeProvider,( ILcdGeoReference ) fOutputModel.getModelReference(), losOutputObject );
      }
    }
    /**
     * Converts a <code>LOSCoverageInputShape</code> to a {@link TLspLOSProperties TLspLOSProperties}
     * object.
     * @param aLOSCoverageInputShape an input shape to
     * @return a properties object based on the given input shape.
     */
    private TLspLOSProperties createLOSProperties( LOSCoverageInputShape aLOSCoverageInputShape ) {
      TLspLOSProperties losProperties = new TLspLOSProperties();
      losProperties.setAngleArc( aLOSCoverageInputShape.getArcAngle() );
      double angleStart = 90.0 - ( aLOSCoverageInputShape.getStartAngle() + aLOSCoverageInputShape.getArcAngle() );
      losProperties.setAngleStart( angleStart ); // Small correct because an arc band's start angle is w.r.t. 3 o'clock
      losProperties.setAngleStep( aLOSCoverageInputShape.getAngleStep() );
      losProperties.setMinVerticalAngle( aLOSCoverageInputShape.getMinVerticalAngle() );
      losProperties.setMaxVerticalAngle( aLOSCoverageInputShape.getMaxVerticalAngle() );

      double centerPointAltitude = fAltitudeProvider.retrieveAltitudeAt( aLOSCoverageInputShape.getCenter(), ( ILcdGeoReference ) fInputModel.getModelReference() );
      if ( !fAltitudeProvider.getAltitudeDescriptor().isSpecialValue( centerPointAltitude ) ) {
        centerPointAltitude += aLOSCoverageInputShape.getCenterPointHeightOffset();
      }
      else {
        centerPointAltitude = aLOSCoverageInputShape.getCenterPointHeightOffset();
      }
      losProperties.setCenterPoint( new TLcdXYZPoint( aLOSCoverageInputShape.getCenter().getX(),
                                                      aLOSCoverageInputShape.getCenter().getY(),
                                                      centerPointAltitude ) );

      //The input shape is added to the input model, so it is defined in the same reference
      losProperties.setCenterPointReference( ( ILcdGeoReference ) fInputModel.getModelReference() );
      losProperties.setRadiusStep( aLOSCoverageInputShape.getRadiusStep() );
      losProperties.setRadius( aLOSCoverageInputShape.getMaxRadius() );
      losProperties.setCenterPointAltitudeMode( TLcdCoverageAltitudeMode.ABOVE_ELLIPSOID );
      return losProperties;
    }
    /**
     * <p>When called, recalculates LOS for all LOSCoverageInputShapes.</p>
     */
    public void invalidateOutput() {
      try {
        TLcdLockUtil.writeLock( fOutputModel );
        IdentityHashMap<LOSCoverageInputShape, ILcdLOSCoverageMatrix> newHashMap = new IdentityHashMap<LOSCoverageInputShape, ILcdLOSCoverageMatrix>();
        for ( Map.Entry<LOSCoverageInputShape, ILcdLOSCoverageMatrix> entry : fInputToOutputMap.entrySet() ) {
          //remove old entries
          LOSCoverageInputShape inputShape = entry.getKey();
          ILcdLOSCoverageMatrix oldOutput = entry.getValue();
          fOutputModel.removeElement( oldOutput, ILcdModel.FIRE_LATER );
          fLOSCalculator.disposeLOSCalculation( oldOutput );
          //create new entries
          ILcdLOSCoverageMatrix newOutput = createLOSOutputObject( inputShape );
          fOutputModel.addElement( newOutput, ILcdModel.FIRE_LATER );
          newHashMap.put( inputShape, newOutput );
        }
        fInputToOutputMap = newHashMap;
        fOutputModel.fireCollectedModelChanges();
      } finally {
        TLcdLockUtil.writeUnlock( fOutputModel );
      }
    }

    /**
     * <p>A listener that listens to changes in the input model and propagates
     * them to the output model in an efficient way, and as documented in the javadoc
     * of {@link LOSModelEventMediator
     * LOSModelEventMediator}</p>
     */
    private class InputModelListener implements ILcdModelListener {
      @Override
      public void modelChanged( TLcdModelChangedEvent aEvent ) {
        try {
          TLcdLockUtil.writeLock( fOutputModel );
          TLcdLockUtil.readLock( fInputModel );
          applyModelChange( aEvent );
        } finally {
          TLcdLockUtil.writeUnlock( fOutputModel );
          fOutputModel.fireCollectedModelChanges();
          TLcdLockUtil.readUnlock( fInputModel );
        }
      }

      private void applyModelChange( TLcdModelChangedEvent aEvent ) {
        if ((aEvent.getCode() & TLcdModelChangedEvent.ALL_OBJECTS_REMOVED) != 0) {
          fInputToOutputMap.clear();
          fOutputModel.removeAllElements(ILcdModel.FIRE_LATER);
        }

        Enumeration elements = aEvent.elements();
        while (elements.hasMoreElements()) {
          Object element = elements.nextElement();
          int change = aEvent.retrieveChange(element);
          switch (change) {
          case TLcdModelChangedEvent.OBJECT_ADDED:
            registerNewInputShape( element );
            break;
          case TLcdModelChangedEvent.OBJECT_REMOVED:
            if (element instanceof LOSCoverageInputShape) {
              LOSCoverageInputShape removedInputShape = (LOSCoverageInputShape) element;
              Object toRemoveOutputObject = fInputToOutputMap.get(removedInputShape);
              if (toRemoveOutputObject != null) {
                fOutputModel.removeElement(toRemoveOutputObject, ILcdModel.FIRE_LATER);
              }
            }
            break;
          case TLcdModelChangedEvent.OBJECT_CHANGED:
            if ( element instanceof LOSCoverageInputShape ) {
              LOSCoverageInputShape changedInputShape = ( LOSCoverageInputShape ) element;
              ILcdLOSCoverageMatrix changedOutputObject = fInputToOutputMap.get( changedInputShape );
              if ( changedOutputObject != null ) {
                updateLOSOutputObject( changedInputShape );
                fOutputModel.elementChanged( changedOutputObject, ILcdModel.FIRE_LATER );
              }

            }
            break;
          }
        }
      }
    }

  }

}
