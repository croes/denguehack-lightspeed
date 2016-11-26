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
package samples.decoder.kml22.common.timetoolbar;

import com.luciad.shape.ILcdTimeBounds;
import com.luciad.shape.TLcdTimeBounds;
import com.luciad.util.logging.ILcdLogger;
import com.luciad.util.logging.TLcdLoggerFactory;
import com.luciad.view.ILcdLayered;
import com.luciad.view.ILcdView;
import samples.decoder.kml22.common.timetoolbar.common.SimulatorModel;
import samples.decoder.kml22.common.timetoolbar.common.TimeMediator;
import samples.decoder.kml22.common.timetoolbar.simple.SimpleTimeToolbar;

import javax.swing.JPanel;
import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * <p>A factory for generating time toolbars.</p>
 * <p>This class will check whether or not the RealTime optional module is available. If it is,
 * it will generate an advanced time toolbar that features support for simulating time. If the
 * RealTime optional module is not present, it will return a simple time toolbar that can
 * set the time globally, but cannot simulate it.</p>
 */
public class TimeToolbarFactory {  
  private static ILcdLogger sLogger = TLcdLoggerFactory.getLogger( TimeToolbarFactory.class.getName() );

  /**
   * Creates a time toolbar based on reflection. It will create an advanced time toolbar if the RealTime
   * optional module is available, or return a simple toolbar if it isn't.
   * @param aView a view for which a timetoolbar should be created.
   * @return a JPanel that represents a time toolbar
   */
  public JPanel createTimeToolbar( ILcdView aView ){

    if ( !( aView instanceof ILcdLayered ) ) {
      throw new IllegalArgumentException( "The given view in createTimeToolbar must implement the com.luciad.view.ILcdLayered interface. If it doesn't, please invoke the createTimeToolbar with a layered explicitly." );
    }

    if ( !( aView instanceof Component ) ) {
      throw new IllegalArgumentException( "The given view in createTimeToolbar must extend java.awt.Component. If it doesn't please invoke createTimeToolbar with a component explicitly." );
    }

    return createTimeToolbar( ( ( ILcdLayered ) aView ), ( ( Component ) aView ) );
  }

  /**
   * Creates a time toolbar based on reflection. It will create an advanced time toolbar if the RealTime
   * optional module is available, or return a simple toolbar if it isn't.
   * @param aLayered a layered for which a timetoolbar should be created. This is generally the view.
   * @param aComponent a component for which a timetoolbar should be created. This is either the view itself or the host component.
   * @return a JPanel that represents a time toolbar
   */
  public JPanel createTimeToolbar(ILcdLayered aLayered, Component aComponent){
    try {
         //check if ILcdSimulatorModel class is available
         Class.forName( "com.luciad.realtime.ILcdSimulatorModel" );
         Method createAdvancedTimeToolbar = Class.forName( "samples.decoder.kml22.common.timetoolbar.advanced.AdvancedTimeToolbar" ).getMethod( "createAdvancedTimeToolbar",ILcdLayered.class, Component.class );
         return ( JPanel ) createAdvancedTimeToolbar.invoke( null, aLayered, aComponent );
       } catch ( ClassNotFoundException e ) {
         return SimpleTimeToolbar.createSimpleTimeToolbar( aLayered, aComponent );
       } catch ( NoSuchMethodException e ) {
         sLogger.trace( "Couldn't find method createAdvancedTimeToolbar() in com.luciad.realtime.ILcdSimulatorModel" );
       } catch ( InvocationTargetException e ) {
         sLogger.trace( "Couldn't invoke method createAdvancedTimeToolbar() in com.luciad.realtime.ILcdSimulatorModel" );
       } catch ( IllegalAccessException e ) {
         sLogger.trace( "Couldn't access method createAdvancedTimeToolbar() in com.luciad.realtime.ILcdSimulatorModel" );
       }
       return null;
  }

  /**
   * <p>Changes the visibility of the time toolbar based on the availability of time data.</p>
   */
  public static class TimeToolbarVisibilityUpdater implements PropertyChangeListener {
    private JPanel fAdvancedTimeToolbar;

    public TimeToolbarVisibilityUpdater( JPanel aTimeToolbar ) {
      fAdvancedTimeToolbar = aTimeToolbar;
    }

    public void propertyChange( PropertyChangeEvent evt ) {
      if ( "hasTimeData".equals( evt.getPropertyName() ) ) {
        fAdvancedTimeToolbar.setVisible( ( Boolean ) evt.getNewValue() );
      }
    }
  }

  /**
   * Notifies the given <code>TimeMediator</code> whenever a change occurs
   * in the <code>SimulatorModel</code>.
   */
  public static class ViewUpdateListener implements PropertyChangeListener {
    private TimeMediator fTimeMediator;
    private SimulatorModel fSimulatorModel;

    public ViewUpdateListener( TimeMediator aTimeMediator, SimulatorModel aSimulatorModel ) {
      fTimeMediator = aTimeMediator;
      fSimulatorModel = aSimulatorModel;
    }

    public void propertyChange( PropertyChangeEvent evt ) {
      if ( fSimulatorModel.hasTimeData() ) {
        fTimeMediator.setTimeBounds( new TLcdTimeBounds( fSimulatorModel.getDate().getTime() - fSimulatorModel.getIntervalLength(),
                                                         ILcdTimeBounds.Boundedness.BOUNDED,
                                                         fSimulatorModel.getDate().getTime(),
                                                         ILcdTimeBounds.Boundedness.BOUNDED ) );
      }
    }
  }

}
