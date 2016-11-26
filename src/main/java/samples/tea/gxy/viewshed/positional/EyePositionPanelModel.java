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
package samples.tea.gxy.viewshed.positional;

import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelListener;
import com.luciad.model.TLcdModelChangedEvent;
import com.luciad.reference.ILcdGeoReference;
import com.luciad.shape.ILcdPoint;
import com.luciad.shape.shape2D.ILcd2DEditablePoint;
import com.luciad.shape.shape2D.TLcdLonLatPoint;
import com.luciad.shape.shape3D.TLcdLonLatHeightPoint;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 *<p>This class models the different settings of the viewshed in the sample.</p>
 * <p>
 *   The following properties are modelled:
 *   <ul>
 *     <li>The position of the eye point</li>
 *     <li>The reference of the eye point</li>
 *     <li>The fixed height offset above terrain of the eye point</li>
 *     <li>The step size in which terrain is sampled</li>
 *     <li>The fixed height offset in which the viewshed should be sampled</li>
 *   </ul>
 * </p>
 */
class EyePositionPanelModel {
  public static final String EYE_POSITION_PROPERTY_NAME = "EyePosition";
  public static final String EYE_POSITION_REFERENCE_PROPERTY_NAME = "EyePositionReference";
  public static final String EYE_LONGITUDE_PROPERTY_NAME = "EyeLongitude";
  public static final String EYE_LATITUDE_PROPERTY_NAME = "EyeLatitude";
  public static final String STEPSIZE_PROPERTY_NAME = "Stepsize";
  public static final String SAMPLING_HEIGHT_OFFSET_PROPERTY_NAME = "SamplingHeightOffset";
  public static final String EYE_HEIGHT_OFFSET_PROPERTY_NAME = "EyeHeightOffset";

  private ILcd2DEditablePoint fEyePosition = new TLcdLonLatHeightPoint(  );
  private double fStepSize;
  private double fTargetSamplingHeightOffset;
  private double fEyeHeightOffset;
  private PropertyChangeSupport fPropertyChangeSupport = new PropertyChangeSupport( this );
  private ILcdGeoReference fEyePositionReference;


  private ILcdModel fPointModel;

  public EyePositionPanelModel( TLcdLonLatPoint aEyePosition, ILcdGeoReference aEyePositionReference, double aSamplingDensity, double aTargetSamplingHeightOffset, double aEyeHeightOffset ) {
    fEyePositionReference = aEyePositionReference;
    fEyeHeightOffset = aEyeHeightOffset;
    fEyePosition.move2D( aEyePosition );
    fTargetSamplingHeightOffset = aTargetSamplingHeightOffset;
    fStepSize = aSamplingDensity;
  }

  public ILcdModel getPointModel() {
    return fPointModel;
  }

  public double getEyeHeightOffset() {
    return fEyeHeightOffset;
  }

  public void setEyeHeightOffset( double aEyeHeightOffset ) {
    double oldValue = fEyeHeightOffset;
    fEyeHeightOffset = aEyeHeightOffset;
    fPropertyChangeSupport.firePropertyChange( EYE_HEIGHT_OFFSET_PROPERTY_NAME,oldValue,fEyeHeightOffset );
  }

  public void setPointModel( ILcdModel aPointModel, final Object aPointObject ) {
    fPointModel = aPointModel;
    fPointModel.addModelListener( new MyPointModelListener( aPointObject ) );
  }

  public ILcdPoint getEyePosition() {
    return fEyePosition;
  }

  public void setProperty(String aProperty, Object aValue){
    if(aProperty.equals( EYE_POSITION_PROPERTY_NAME )){
      ILcdPoint value = ( ILcdPoint ) aValue;
      setSunPosition( value.getX(),value.getY() );
    }else if(aProperty.equals( EYE_LONGITUDE_PROPERTY_NAME )){
      setSunLongitude( ( Double ) aValue );
    }else if(aProperty.equals( EYE_LATITUDE_PROPERTY_NAME )){
      setSunLatitude( ( Double ) aValue );
    }else if(aProperty.equals(STEPSIZE_PROPERTY_NAME)){
      setStepSize((Double) aValue);
    }else if(aProperty.equals( SAMPLING_HEIGHT_OFFSET_PROPERTY_NAME )){
      setTargetSamplingHeightOffset( ( Double ) aValue );
    }else if(aProperty.equals( EYE_POSITION_REFERENCE_PROPERTY_NAME )){
      setEyePositionReference( ( ILcdGeoReference ) aValue );
    }else if(aProperty.equals( EYE_HEIGHT_OFFSET_PROPERTY_NAME )){
      setEyeHeightOffset( ( Double ) aValue );
    }else{
      throw new IllegalArgumentException( "Could not find property "+aProperty );
    }
  }

  public Object getProperty(String aProperty){
    if(aProperty.equals( EYE_POSITION_PROPERTY_NAME )){
      return getEyePosition();
    }else if(aProperty.equals( EYE_LONGITUDE_PROPERTY_NAME )){
      return getEyePosition().getX();
    }else if(aProperty.equals( EYE_LATITUDE_PROPERTY_NAME )){
      return getEyePosition().getY();
    }else if(aProperty.equals(STEPSIZE_PROPERTY_NAME)){
      return getStepSize();
    }else if(aProperty.equals( SAMPLING_HEIGHT_OFFSET_PROPERTY_NAME )){
      return getTargetSamplingHeightOffset();
    }else if(aProperty.equals( EYE_POSITION_REFERENCE_PROPERTY_NAME )){
      return getEyePositionReference();
    }else if(aProperty.equals( EYE_HEIGHT_OFFSET_PROPERTY_NAME )){
      return getEyeHeightOffset();
    }else{
      throw new IllegalArgumentException( "Could not find property "+aProperty );
    }
  }

  public void setSunPosition( double aLon, double aLat ) {
    ILcd2DEditablePoint oldValue = ( ILcd2DEditablePoint ) fEyePosition.clone();
    fEyePosition.move2D( aLon,aLat);
    fPropertyChangeSupport.firePropertyChange( EYE_POSITION_PROPERTY_NAME, oldValue, fEyePosition );
    fPropertyChangeSupport.firePropertyChange( EYE_LONGITUDE_PROPERTY_NAME, oldValue.getX(), fEyePosition.getX() );
    fPropertyChangeSupport.firePropertyChange( EYE_LATITUDE_PROPERTY_NAME, oldValue.getY(), fEyePosition.getY() );
  }

  public void setSunLongitude(double aLon){
    ILcd2DEditablePoint oldValue = ( ILcd2DEditablePoint ) fEyePosition.clone();
    fEyePosition.move2D( aLon, fEyePosition.getY());
    fPropertyChangeSupport.firePropertyChange( EYE_LONGITUDE_PROPERTY_NAME,oldValue.getX() , fEyePosition.getX() );
  }

  public void setSunLatitude(double aLat){
    ILcd2DEditablePoint oldValue = ( ILcd2DEditablePoint ) fEyePosition.clone();
    fEyePosition.move2D( fEyePosition.getX(),aLat);
    fPropertyChangeSupport.firePropertyChange( EYE_LATITUDE_PROPERTY_NAME,oldValue.getY() , fEyePosition.getY() );
  }

  public double getStepSize() {
    return fStepSize;
  }

  public void setStepSize(double aStepSize) {
    double oldValue = fStepSize;
    fStepSize = aStepSize;
    fPropertyChangeSupport.firePropertyChange(STEPSIZE_PROPERTY_NAME,oldValue, fStepSize);
  }

  public double getTargetSamplingHeightOffset() {
    return fTargetSamplingHeightOffset;
  }

  public void setTargetSamplingHeightOffset( double aTargetSamplingHeightOffset ) {
    double oldValue = fTargetSamplingHeightOffset;
    fTargetSamplingHeightOffset = aTargetSamplingHeightOffset;
    fPropertyChangeSupport.firePropertyChange( SAMPLING_HEIGHT_OFFSET_PROPERTY_NAME,oldValue,fTargetSamplingHeightOffset );
  }

  public void setEyePositionReference( ILcdGeoReference aEyePositionReference ) {
    ILcdGeoReference oldValue = fEyePositionReference;
    fEyePositionReference = aEyePositionReference;
    fPropertyChangeSupport.firePropertyChange( EYE_POSITION_REFERENCE_PROPERTY_NAME,oldValue,fTargetSamplingHeightOffset );
  }

  public ILcdGeoReference getEyePositionReference() {
    return fEyePositionReference;
  }

  public void removePropertyChangeListener( PropertyChangeListener listener ) {
    fPropertyChangeSupport.removePropertyChangeListener( listener );
  }

  public void addPropertyChangeListener( PropertyChangeListener listener ) {
    fPropertyChangeSupport.addPropertyChangeListener( listener );
  }

  private class MyPointModelListener implements ILcdModelListener {
    private final Object fPointObject;

    public MyPointModelListener( Object aPointObject ) {
      fPointObject = aPointObject;
    }

    public void modelChanged( TLcdModelChangedEvent aEvent ) {
      if(aEvent.containsElement( fPointObject )){
        ILcdPoint pointObject = ( ILcdPoint ) fPointObject;
        setSunPosition( pointObject.getX(),pointObject.getY() );
      }
    }
  }
}
