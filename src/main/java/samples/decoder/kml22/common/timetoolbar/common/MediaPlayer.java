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
package samples.decoder.kml22.common.timetoolbar.common;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * Class to model the state of a <code>MediaPlayerSlider</code>.
 */
public class MediaPlayer {

  private double fMinimum;
  private double fMaximum;
  private double fCurrentPercentage = 0;
  private double fIntervalSize = -1;

  private PropertyChangeSupport fPropertyChangeSupport;

  /**
   * Creates a new <code>MediaPlayer</code> with given range.
   * @param aMinimum a minimum value
   * @param aMaximum a maximum value
   */
  public MediaPlayer( double aMinimum, double aMaximum) {
    fMinimum = aMinimum;
    fMaximum = aMaximum;
    fCurrentPercentage = fMinimum;
  }

  /**
   * Returns the maximum value which can be represented by this slider.
   *
   * @return The maximum value of this slider.
   */
  public double getMaximum() {
    return fMaximum;
  }


  /**
   * Sets the maximum value of this slider. The maximum should always be greater than the minimum.
   * @param aMaximum The new maximum value for this slider.
   */
  public void setMaximum( double aMaximum ) {
    double oldValue = fMaximum;
    fMaximum = aMaximum;
    firePropertyChange( "maximum", oldValue, fMaximum );
  }

  /**
   * Returns the minimum value which can be represented by this media player.
   * @return The minimum value of this media player.
   */
  public double getMinimum() {
    return fMinimum;
  }

  /**
   * Sets the minimum value of this media player. The minimum should always be smaller than the maximum.
   *
   * @param aMinimum The new minimum value for this media player.
   */
  public void setMinimum( double aMinimum ) {
    double oldValue = fMinimum;
    fMinimum = aMinimum;
    firePropertyChange( "minimum", oldValue, fMinimum );
  }

  /**
   * Returns the actual value of this slider, given as a number between the minimum and
   * maximum values.
   * @return the value of this <code>MediaPlayer</code>
   */
  public double getValue() {
    double returnValue = fMinimum;
    returnValue += fCurrentPercentage * ( fMaximum - fMinimum );
    return returnValue;
  }

  /**
   * Sets the current slider value, <code>aValue</code> should be larger than
   * <code>getMinimum()</code> and smaller than <code>getMaximum()</code>.
   * Values outside this range will be clamped. Setting the value will
   * automatically update the slider.
   *
   * @param aValue The new value of this slider.
   */
  public void setValue( double aValue ) {
    double oldValue = getValue();
    if ( aValue < fMinimum ) {
      aValue = fMinimum;
    }
    if ( aValue > fMaximum ) {
      aValue = fMaximum;
    }
    fCurrentPercentage = (aValue-fMinimum)/(fMaximum-fMinimum);
    firePropertyChange( "value", oldValue, getValue() );
  }

  /**
   * Returns the relative current value, expressed in a percentage.
   *
   * @return the relative current value. Value returned is a double in the range of [0,1], with
   *         0 indicating the value is equal to the minimum value, and 1 indicating the value
   *         is equal to the maximum value.
   */
  public double getRelativePosition() {
    return fCurrentPercentage;
  }

  /**
   * Returns the interval size of this slider. The interval allows the slider to cover a range of values. When the
   * interval is null, the slider corresponds to a single value.
   *
   * @return The interval size of the slider.
   */
  public double getIntervalSize() {
    return fIntervalSize;
  }

  public void setIntervalSize( double aIntervalSize ) {
    double oldValue = fIntervalSize;
    fIntervalSize = aIntervalSize;
    firePropertyChange( "interval", oldValue, getIntervalSize() );
  }


  /**
   * Adds a listener to this media player.
   * @param aListener The listener to add.
   */
  public void addPropertyChangeListener( PropertyChangeListener aListener ) {
    if ( fPropertyChangeSupport == null ) {
      fPropertyChangeSupport = new PropertyChangeSupport( this );
    }
    fPropertyChangeSupport.addPropertyChangeListener( aListener );
  }

  /**
   * Removes the listener from this media player.
   * @param aListener The listener to remove.
   */
  public void removePropertyChangeListener( PropertyChangeListener aListener ) {
    if ( fPropertyChangeSupport != null ) {
      fPropertyChangeSupport.removePropertyChangeListener( aListener );
    }

  }

  /**
   * Fires the given property change.
   * @param aPropertyName a property name
   * @param aOldValue an old value
   * @param aNewValue a new value
   */
  private void firePropertyChange( String aPropertyName, Object aOldValue, Object aNewValue ) {
    if ( fPropertyChangeSupport == null ) {
      fPropertyChangeSupport = new PropertyChangeSupport( this );
    }
    fPropertyChangeSupport.firePropertyChange( aPropertyName, aOldValue, aNewValue );
  }

  /**
   * Sets the relative value of this media player, expressed in a percentage.
   * @param aValue a Value given in percentages. Should be in the range of [0,1], with values
   * outside of this range clamped to the maximum value.
   */
  public void setRelativeValue( double aValue ) {
    double oldValue = getValue();
    if ( aValue < 0d ) {
      aValue = 0d;
    }
    if ( aValue > 1d ) {
      aValue = 1d;
    }
    fCurrentPercentage = aValue;
    firePropertyChange( "value", oldValue, getValue() );
  }
}
