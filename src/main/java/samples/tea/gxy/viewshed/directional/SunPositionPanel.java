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

import com.luciad.text.TLcdAltitudeFormat;
import com.luciad.text.TLcdDistanceFormat;
import com.luciad.util.ILcdParser;
import com.luciad.util.TLcdLonLatFormatter;
import com.luciad.util.TLcdLonLatParser;

import com.luciad.text.TLcdAltitudeFormat;
import com.luciad.text.TLcdDistanceFormat;
import com.luciad.util.ILcdParser;
import com.luciad.util.TLcdLonLatFormatter;
import com.luciad.util.TLcdLonLatParser;

import com.luciad.text.TLcdAltitudeFormat;
import com.luciad.text.TLcdDistanceFormat;
import com.luciad.util.ILcdParser;
import com.luciad.util.TLcdLonLatFormatter;
import com.luciad.util.TLcdLonLatParser;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.text.JTextComponent;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;


/**
 * The sun position panel. This class is used to visualize the settings
 * of the viewshed calculations.
 */
class SunPositionPanel extends JPanel {
  private JTextField fLon;
  private JTextField fLat;
  private JTextField fTerrainStepSize;
  private JTextField fSamplingHeightOffset;
  private JButton fUpdateButton;
  private SunPositionPanelModel fPanelModel;
  private TLcdLonLatFormatter fLonLatFormatter = new TLcdLonLatFormatter(  );
  private TLcdLonLatParser fLonLatParser = new TLcdLonLatParser(  );
  private TLcdAltitudeFormat fAltitudeFormat = new TLcdAltitudeFormat(  );
  private TLcdDistanceFormat fDistanceFormat = new TLcdDistanceFormat(  );

  public SunPositionPanel( SunPositionPanelModel aPanelModel, CreateViewshedAction aCreateViewshedAction ) {
    fPanelModel = aPanelModel;
    initComponents( aPanelModel );
    fUpdateButton.setAction( aCreateViewshedAction );
    initListeners( aPanelModel );
  }

  private void initListeners( SunPositionPanelModel aPanelModel ) {
    aPanelModel.addPropertyChangeListener( new MyPropertyChangeListener() );
    fLon.addFocusListener( new ParsingFocusListener(aPanelModel,SunPositionPanelModel.SUN_LONGITUDE_PROPERTY_NAME, fLonLatParser) );
    fLat.addFocusListener( new ParsingFocusListener(aPanelModel,SunPositionPanelModel.SUN_LATITUDE_PROPERTY_NAME, fLonLatParser ) );
    fSamplingHeightOffset.addFocusListener( new ParsingFocusListener(aPanelModel,SunPositionPanelModel.SAMPLING_HEIGHT_OFFSET_PROPERTY_NAME, fAltitudeFormat ) );
    fTerrainStepSize.addFocusListener( new ParsingFocusListener(aPanelModel,SunPositionPanelModel.STEPSIZE_PROPERTY_NAME, fDistanceFormat ) );
  }

  public SunPositionPanelModel getPanelModel() {
    return fPanelModel;
  }

  public void setPanelModel( SunPositionPanelModel aPanelModel ) {
    fPanelModel = aPanelModel;
  }

  @SuppressWarnings("unchecked")
  private void initComponents( SunPositionPanelModel aPanelModel ) {
    JPanel sunLocatorPanel = new JPanel();
    sunLocatorPanel.setBorder( BorderFactory.createTitledBorder( "Viewshed settings" ) );

    JLabel longitudeLabel            = new JLabel( "Sun longitude" );
    JLabel latitudeLabel             = new JLabel( "Sun latitude" );
    JLabel samplingHeightOffsetLabel = new JLabel( "Sampling height" );
    JLabel terrainStepSizeLabel      = new JLabel( "Terrain step size" );

    fLon                  = new JTextField( fLonLatFormatter.formatLon( aPanelModel.getSunPosition().getX() ) );
    fLat                  = new JTextField( fLonLatFormatter.formatLat( aPanelModel.getSunPosition().getY() ) );
    fSamplingHeightOffset = new JTextField( fAltitudeFormat.format( aPanelModel.getTargetSamplingHeightOffset() ) );
    fTerrainStepSize      = new JTextField( fDistanceFormat.format( aPanelModel.getStepSize() ) );
    fUpdateButton         = new JButton( "Update" );

    GridBagConstraints labelGBC = new GridBagConstraints(
            -1, -1,
            1, 1,
            1, 1,
            GridBagConstraints.WEST,
            GridBagConstraints.HORIZONTAL,
            new Insets( 2, 4, 2, 4 ),
            2, 4
    );

    GridBagConstraints fieldGBC = new GridBagConstraints(
            -1, -1,
            GridBagConstraints.REMAINDER, 1,
            1, 1,
            GridBagConstraints.WEST,
            GridBagConstraints.HORIZONTAL,
            new Insets( 2, 4, 2, 4 ),
            2, 4
    );

    sunLocatorPanel.setLayout( new GridBagLayout() );

    sunLocatorPanel.add( longitudeLabel, labelGBC );
    sunLocatorPanel.add( fLon, fieldGBC);

    sunLocatorPanel.add( latitudeLabel, labelGBC );
    sunLocatorPanel.add( fLat, fieldGBC);

    sunLocatorPanel.add( samplingHeightOffsetLabel, labelGBC );
    sunLocatorPanel.add( fSamplingHeightOffset, fieldGBC);

    sunLocatorPanel.add( terrainStepSizeLabel, labelGBC );
    sunLocatorPanel.add( fTerrainStepSize, fieldGBC);

    sunLocatorPanel.add( fUpdateButton, fieldGBC);

    add( sunLocatorPanel );
  }

  /**
     * A focus listener that parses the value of its source when focus is lost, and checks whether the
     * value is a correct double. It then applies the resulting property value to the matching
     * property.
     */
   private static class ParsingFocusListener extends FocusAdapter {
     private SunPositionPanelModel fPanelModel;
     private String fProperty;
     private ILcdParser fParser;
     private Object fTempObject;

     public ParsingFocusListener( SunPositionPanelModel aPanelModel, String aProperty, ILcdParser aParser ) {
       fPanelModel = aPanelModel;
       fProperty = aProperty;
       fParser = aParser;
     }

     public void focusGained( FocusEvent aEvent ) {
       JTextComponent source = ( JTextComponent ) aEvent.getSource();
       fTempObject = source.getText();
     }

     public void focusLost( FocusEvent aEvent ) {
       JTextComponent source = ( JTextComponent ) aEvent.getSource();
       try {
         Object newValue = fParser.parse( source.getText() );
         fPanelModel.setProperty(fProperty,newValue);
       } catch ( Exception e ) {
         source.setText( String.valueOf(fTempObject) );
       }
     }
   }

  /**
   * Listens to changes in property, and applies them to the matching text area fields.
   */
  private class MyPropertyChangeListener implements PropertyChangeListener {
    public void propertyChange( PropertyChangeEvent evt ) {
      if(evt.getPropertyName().equals( SunPositionPanelModel.SUN_LONGITUDE_PROPERTY_NAME )){
        fLon.setText( fLonLatFormatter.formatLon( ( Double ) evt.getNewValue() ) );
      }else if(evt.getPropertyName().equals( SunPositionPanelModel.SUN_LATITUDE_PROPERTY_NAME )){
        fLat.setText( fLonLatFormatter.formatLat( ( Double ) evt.getNewValue() ) );
      }else if(evt.getPropertyName().equals( SunPositionPanelModel.SAMPLING_HEIGHT_OFFSET_PROPERTY_NAME )){
        fSamplingHeightOffset.setText( fAltitudeFormat.format( evt.getNewValue() ) );
      }else if(evt.getPropertyName().equals( SunPositionPanelModel.STEPSIZE_PROPERTY_NAME)){
        fTerrainStepSize.setText( fDistanceFormat.format( evt.getNewValue() ) );
      }
    }
  }
}
