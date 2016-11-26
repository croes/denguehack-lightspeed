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
package samples.opengl.common;

import com.luciad.view.opengl.ILcdGLCamera;
import samples.gxy.common.TitledPanel;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.text.NumberFormat;
import java.util.Hashtable;

/**
 * A slider that changes the Z scale on a <code>ILcdGLCamera</code>,
 * values between 1 and 50.
 */
public class ZSlider extends JPanel {

  private static double FACTOR = 100;

  private JSlider fSlider;
  private ILcdGLCamera fCamera;

  // before refactoring, there were 2 versions :
  //      this( aOrientation, aCamera, 1, 10, 0.5,  1 );
  //      this( aOrientation, aCamera, 1, 50, 5  , 10 );

  public ZSlider( int aOrientation, ILcdGLCamera aCamera ) {
    this( aOrientation, aCamera, 1, 20, 0.5, 2.5 );
  }

  public ZSlider( int           aOrientation,
                  ILcdGLCamera  aCamera,
                  double        aMinimum,
                  double        aMaximum,
                  double        aMinorTickSpacing,
                  double        aMayorTickSpacing ) {
    fSlider = new JSlider( aOrientation, toSliderValue( aMinimum ), toSliderValue( aMaximum ), toSliderValue( aMinimum ) );

    NumberFormat format = NumberFormat.getNumberInstance();
    format.setMinimumFractionDigits( aMayorTickSpacing == ( ( int ) aMayorTickSpacing ) ? 0 : 1 );
    format.setMaximumFractionDigits( aMayorTickSpacing == ( ( int ) aMayorTickSpacing ) ? 0 : 1 );

    fSlider.setMajorTickSpacing( toSliderValue( aMayorTickSpacing ) );
    fSlider.setMinorTickSpacing( toSliderValue( aMinorTickSpacing ) );
    fSlider.setPaintTicks( true );
    fSlider.setSnapToTicks( false );

    Hashtable label_table = new Hashtable();
    label_table.put( new Integer( fSlider.getMinimum() ), new JLabel( format.format( new Double( aMinimum ) ) ) );
    for ( int i = 0; i < fSlider.getMaximum() ; i += fSlider.getMajorTickSpacing() ) {
      label_table.put( new Integer( i ), new JLabel( format.format( new Double( fromSliderValue( i ) ) ) ) );
    }
    label_table.put( new Integer( fSlider.getMaximum() ), new JLabel( format.format( new Double( aMaximum ) ) ) );
    fSlider.setLabelTable( label_table );
    fSlider.setPaintLabels( true );

    fCamera = aCamera;
    fSlider.setValue( toSliderValue( fCamera.getAltitudeExaggerationFactor() ) );

    fSlider.addChangeListener( new ZScaleSetter() );

    setLayout( new BorderLayout() );
    add( BorderLayout.CENTER, TitledPanel.createTitledPanel( "Z scale", fSlider ) );
  }

  @Override
  public Dimension getPreferredSize() {
    return new Dimension( 70, 0 );
  }

  private static int toSliderValue( double aValue ) {
    return (int) ( aValue * FACTOR );
  }

  private static double fromSliderValue( int aValue ) {
    return aValue / FACTOR;
  }

  private class ZScaleSetter implements ChangeListener {

    public void stateChanged( ChangeEvent e ) {
      fCamera.setAltitudeExaggerationFactor( fromSliderValue( fSlider.getValue() ) );
    }
  }
}
