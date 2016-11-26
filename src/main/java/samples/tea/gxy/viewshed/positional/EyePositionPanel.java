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
 * The eye position panel. This class is used to visualize the settings
 * of the viewshed calculations.
 */
class EyePositionPanel extends JPanel {
  private JTextField fLon;
  private JTextField fLat;
  private JTextField fTerrainStepSize;
  private JTextField fSamplingHeightOffset;
  private JTextField fEyeHeightOffset;
  private JButton fUpdateButton;
  private EyePositionPanelModel fPanelModel;
  private TLcdLonLatFormatter fLonLatFormatter = new TLcdLonLatFormatter(  );
  private TLcdLonLatParser fLonLatParser = new TLcdLonLatParser(  );
  private TLcdAltitudeFormat fAltitudeFormat = new TLcdAltitudeFormat(  );
  private TLcdDistanceFormat fDistanceFormat = new TLcdDistanceFormat(  );

  public EyePositionPanel( EyePositionPanelModel aPanelModel, CreateViewshedAction aCreateViewshedAction ) {
    fPanelModel = aPanelModel;
    initComponents( aPanelModel );
    fUpdateButton.setAction( aCreateViewshedAction );
    initListeners( aPanelModel );
  }

  private void initListeners( EyePositionPanelModel aPanelModel ) {
    aPanelModel.addPropertyChangeListener( new MyPropertyChangeListener() );
    fLon.addFocusListener( new ParsingFocusListener(aPanelModel, EyePositionPanelModel.EYE_LONGITUDE_PROPERTY_NAME, fLonLatParser ) );
    fLat.addFocusListener( new ParsingFocusListener(aPanelModel, EyePositionPanelModel.EYE_LATITUDE_PROPERTY_NAME, fLonLatParser ) );
    fSamplingHeightOffset.addFocusListener( new ParsingFocusListener(aPanelModel, EyePositionPanelModel.SAMPLING_HEIGHT_OFFSET_PROPERTY_NAME, fAltitudeFormat) );
    fTerrainStepSize.addFocusListener( new ParsingFocusListener(aPanelModel, EyePositionPanelModel.STEPSIZE_PROPERTY_NAME, fDistanceFormat ) );
    fEyeHeightOffset.addFocusListener( new ParsingFocusListener(aPanelModel, EyePositionPanelModel.EYE_HEIGHT_OFFSET_PROPERTY_NAME, fAltitudeFormat) );
  }

  public EyePositionPanelModel getPanelModel() {
    return fPanelModel;
  }

  public void setPanelModel( EyePositionPanelModel aPanelModel ) {
    fPanelModel = aPanelModel;
  }

  @SuppressWarnings("unchecked")
  private void initComponents( EyePositionPanelModel aPanelModel ) {
    JPanel eyeLocatorPanel = new JPanel();
    eyeLocatorPanel.setBorder( BorderFactory.createTitledBorder( "Viewshed settings" ) );

    JLabel longitudeLabel            = new JLabel( "Eye longitude" );
    JLabel latitudeLabel             = new JLabel( "Eye latitude" );
    JLabel eyeHeightOffsetLabel      = new JLabel( "Eye height" );
    JLabel samplingHeightOffsetLabel = new JLabel( "Sampling height" );
    JLabel terrainStepSizeLabel      = new JLabel( "Terrain step size" );

    fLon                  = new JTextField( fLonLatFormatter.formatLon( aPanelModel.getEyePosition().getX() ) );
    fLat                  = new JTextField( fLonLatFormatter.formatLat( aPanelModel.getEyePosition().getY() ) );
    fEyeHeightOffset      = new JTextField( fAltitudeFormat.format(  aPanelModel.getEyeHeightOffset() ));
    fSamplingHeightOffset = new JTextField( fAltitudeFormat.format( aPanelModel.getTargetSamplingHeightOffset() ));
    fTerrainStepSize      = new JTextField( fDistanceFormat.format(  aPanelModel.getStepSize() ) );
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

    eyeLocatorPanel.setLayout( new GridBagLayout() );

    eyeLocatorPanel.add( longitudeLabel, labelGBC );
    eyeLocatorPanel.add( fLon, fieldGBC);

    eyeLocatorPanel.add( latitudeLabel, labelGBC );
    eyeLocatorPanel.add( fLat, fieldGBC);

    eyeLocatorPanel.add( eyeHeightOffsetLabel, labelGBC );
    eyeLocatorPanel.add( fEyeHeightOffset, fieldGBC);

    eyeLocatorPanel.add( samplingHeightOffsetLabel, labelGBC );
    eyeLocatorPanel.add( fSamplingHeightOffset, fieldGBC);

    eyeLocatorPanel.add( terrainStepSizeLabel, labelGBC );
    eyeLocatorPanel.add( fTerrainStepSize, fieldGBC);

    eyeLocatorPanel.add( fUpdateButton, fieldGBC);

    add( eyeLocatorPanel );
  }

   /**
    * A focus listener that parses the value of its source when focus is lost, and checks whether the
    * value is a correct double. It then applies the resulting property value to the matching
    * property.
    */
  private static class ParsingFocusListener extends FocusAdapter {
    private EyePositionPanelModel fPanelModel;
    private String fProperty;
    private ILcdParser fParser;
    private Object fTempObject;

    public ParsingFocusListener( EyePositionPanelModel aPanelModel, String aProperty, ILcdParser aParser ) {
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
      if(evt.getPropertyName().equals( EyePositionPanelModel.EYE_LONGITUDE_PROPERTY_NAME )){
        fLon.setText( fLonLatFormatter.formatLon( ( Double ) evt.getNewValue() ) );
      }else if(evt.getPropertyName().equals( EyePositionPanelModel.EYE_LATITUDE_PROPERTY_NAME )){
        fLat.setText( fLonLatFormatter.formatLat( ( Double ) evt.getNewValue() ) );
      }else if(evt.getPropertyName().equals( EyePositionPanelModel.SAMPLING_HEIGHT_OFFSET_PROPERTY_NAME )){
        fSamplingHeightOffset.setText( fAltitudeFormat.format( evt.getNewValue() ) );
      }else if(evt.getPropertyName().equals( EyePositionPanelModel.STEPSIZE_PROPERTY_NAME)){
        fTerrainStepSize.setText( fDistanceFormat.format( evt.getNewValue() ) );
      }else if(evt.getPropertyName().equals( EyePositionPanelModel.EYE_HEIGHT_OFFSET_PROPERTY_NAME )){
        fEyeHeightOffset.setText( fAltitudeFormat.format( evt.getNewValue() ) );
      }
    }
  }
 }
