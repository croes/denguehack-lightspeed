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
package samples.tea.gxy.viewshed.directional;

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
 * <p>This class models the different settings of the viewshed in the sample.</p>
 * <p>
 *   The following properties are modelled:
 *   <ul>
 *     <li>The position of the sun</li>
 *     <li>The reference of the sun</li>
 *     <li>The step size in which terrain is sampled</li>
 *     <li>The fixed height offset in which the viewshed should be sampled</li>
 *     <li>The center of the viewshed</li>
 *     <li>The reference in which the center of the viewshed is defined</li>
 *   </ul>
 * </p>
 */
class SunPositionPanelModel{
  public static final String SUN_POSITION_PROPERTY_NAME = "SunPosition";
  public static final String SUN_POSITION_REFERENCE_PROPERTY_NAME = "SunPositionReference";
  public static final String SUN_LONGITUDE_PROPERTY_NAME = "SunLongitude";
  public static final String SUN_LATITUDE_PROPERTY_NAME = "SunLatitude";
  public static final String STEPSIZE_PROPERTY_NAME = "Stepsize";
  public static final String SAMPLING_HEIGHT_OFFSET_PROPERTY_NAME = "SamplingHeightOffset";
  public static final String VIEWSHED_CENTER_POSITION_PROPERTY_NAME = "ViewshedCenter";
  public static final String VIEWSHED_CENTER_REFERENCE_PROPERTY_NAME = "ViewshedCenterReference";

  private ILcd2DEditablePoint fSunPosition = new TLcdLonLatHeightPoint(  );
  private double fStepSize;
  private double fTargetSamplingHeightOffset;
  private PropertyChangeSupport fPropertyChangeSupport = new PropertyChangeSupport( this );
  private ILcdGeoReference fSunPositionReference;

  private ILcd2DEditablePoint fCenterPosition;
  private ILcdGeoReference fCenterPositionReference;

  private ILcdModel fPointModel;

  public SunPositionPanelModel( TLcdLonLatPoint aSunPosition, ILcdGeoReference aSunPositionReference, double aStepSize, double aTargetSamplingHeightOffset, ILcd2DEditablePoint aCenterPosition, ILcdGeoReference aCenterPositionReference ) {
    fSunPositionReference = aSunPositionReference;
    fCenterPosition = aCenterPosition;
    fCenterPositionReference = aCenterPositionReference;
    fSunPosition.move2D( aSunPosition );
    fTargetSamplingHeightOffset = aTargetSamplingHeightOffset;
    fStepSize = aStepSize;
  }

  public ILcdModel getPointModel() {
    return fPointModel;
  }

  public void setPointModel( ILcdModel aPointModel, final Object aPointObject ) {
    fPointModel = aPointModel;
    fPointModel.addModelListener( new MyPointModelListener( aPointObject ) );
  }

  public ILcdPoint getSunPosition() {
    return fSunPosition;
  }

  public void setProperty(String aProperty, Object aValue){
    if(aProperty.equals( SUN_POSITION_PROPERTY_NAME )){
      ILcdPoint value = ( ILcdPoint ) aValue;
      setSunPosition( value.getX(),value.getY() );
    }else if(aProperty.equals( SUN_LONGITUDE_PROPERTY_NAME )){
      setSunLongitude( ( Double ) aValue );
    }else if(aProperty.equals( SUN_LATITUDE_PROPERTY_NAME )){
      setSunLatitude( ( Double ) aValue );
    }else if(aProperty.equals(STEPSIZE_PROPERTY_NAME)){
      setStepSize((Double) aValue);
    }else if(aProperty.equals( SAMPLING_HEIGHT_OFFSET_PROPERTY_NAME )){
      setTargetSamplingHeightOffset( ( Double ) aValue );
    }else if(aProperty.equals( SUN_POSITION_REFERENCE_PROPERTY_NAME )){
      setSunPositionReference( ( ILcdGeoReference ) aValue );
    }else{
      throw new IllegalArgumentException( "Could not find property "+aProperty );
    }
  }

  public Object getProperty(String aProperty){
    if(aProperty.equals( SUN_POSITION_PROPERTY_NAME )){
      return getSunPosition( );
    }else if(aProperty.equals( SUN_LONGITUDE_PROPERTY_NAME )){
      return getSunPosition().getX();
    }else if(aProperty.equals( SUN_LATITUDE_PROPERTY_NAME )){
      return getSunPosition().getY();
    }else if(aProperty.equals(STEPSIZE_PROPERTY_NAME)){
      return getStepSize();
    }else if(aProperty.equals( SAMPLING_HEIGHT_OFFSET_PROPERTY_NAME )){
      return getTargetSamplingHeightOffset();
    }else if(aProperty.equals( SUN_POSITION_REFERENCE_PROPERTY_NAME )){
      return getSunPositionReference();
    }else{
      throw new IllegalArgumentException( "Could not find property "+aProperty );
    }
  }

  public void setSunPosition( double aLon, double aLat ) {
    ILcd2DEditablePoint oldValue = ( ILcd2DEditablePoint ) fSunPosition.clone();
    fSunPosition.move2D( aLon,aLat);
    fPropertyChangeSupport.firePropertyChange( SUN_POSITION_PROPERTY_NAME, oldValue, fSunPosition );
    fPropertyChangeSupport.firePropertyChange( SUN_LONGITUDE_PROPERTY_NAME, oldValue.getX(), fSunPosition.getX() );
    fPropertyChangeSupport.firePropertyChange( SUN_LATITUDE_PROPERTY_NAME, oldValue.getY(), fSunPosition.getY() );
  }

  public void setSunLongitude(double aLon){
    ILcd2DEditablePoint oldValue = ( ILcd2DEditablePoint ) fSunPosition.clone();
    fSunPosition.move2D( aLon,fSunPosition.getY());
    fPropertyChangeSupport.firePropertyChange( SUN_LONGITUDE_PROPERTY_NAME,oldValue.getX() ,fSunPosition.getX() );
  }

  public void setSunLatitude(double aLat){
    ILcd2DEditablePoint oldValue = ( ILcd2DEditablePoint ) fSunPosition.clone();
    fSunPosition.move2D( fSunPosition.getX(),aLat);
    fPropertyChangeSupport.firePropertyChange( SUN_LATITUDE_PROPERTY_NAME,oldValue.getY() ,fSunPosition.getY() );
  }

  public ILcd2DEditablePoint getCenterPosition() {
    return fCenterPosition;
  }

  public void setCenterPosition( ILcd2DEditablePoint aCenterPosition ) {
    ILcd2DEditablePoint oldValue = ( ILcd2DEditablePoint ) fCenterPosition.clone();
    fCenterPosition = aCenterPosition;
    fPropertyChangeSupport.firePropertyChange(VIEWSHED_CENTER_POSITION_PROPERTY_NAME ,oldValue ,aCenterPosition );
  }
  public ILcdGeoReference getCenterPositionReference() {
    return fCenterPositionReference;
  }

  public void setCenterPositionReference( ILcdGeoReference aCenterPositionReference ) {
    ILcdGeoReference oldValue = ( ILcdGeoReference ) fCenterPositionReference.clone();
    fCenterPositionReference = aCenterPositionReference;
    fPropertyChangeSupport.firePropertyChange( VIEWSHED_CENTER_REFERENCE_PROPERTY_NAME,oldValue,aCenterPositionReference );
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

  public void setSunPositionReference( ILcdGeoReference aSunPositionReference ) {
    ILcdGeoReference oldValue = fSunPositionReference;
    fSunPositionReference = aSunPositionReference;
    fPropertyChangeSupport.firePropertyChange( SUN_POSITION_REFERENCE_PROPERTY_NAME,oldValue,fTargetSamplingHeightOffset );
  }

  public ILcdGeoReference getSunPositionReference() {
    return fSunPositionReference;
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
