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
package samples.opengl.stereo;

import com.luciad.view.opengl.TLcdGLViewCanvasStereo;
import samples.gxy.common.TitledPanel;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;

/**
 * A GUI component for editing the eye separation and focal length of a
 * TLcdGLViewCanvasStereo.
 */
class StereoControlPanel extends JPanel {

  public StereoControlPanel( final TLcdGLViewCanvasStereo aCanvas ) {
    JLabel l_sep = new JLabel( "Eye separation:" );
    JTextField t_sep = new JTextField( Double.toString( aCanvas.getEyeSeparation() ) );
    t_sep.getDocument().addDocumentListener( new SeparationListener( aCanvas ) );

    JLabel l_foc = new JLabel( "Focal length:" );
    JTextField t_foc = new JTextField( Double.toString( aCanvas.getFocalLength() ) );
    t_foc.getDocument().addDocumentListener( new FocalLengthListener( aCanvas ) );

    JPanel content = new JPanel( new GridLayout( 4, 1, 2, 2 ) );
    content.setBorder( BorderFactory.createEmptyBorder( 0, 5, 0, 5 ) );
    content.add( l_sep );
    content.add( t_sep );
    content.add( l_foc );
    content.add( t_foc );

    setLayout( new BorderLayout() );
    add( BorderLayout.CENTER, TitledPanel.createTitledPanel( "Stereo settings", content ) );
  }

  @Override
  public Dimension getPreferredSize() {
    Dimension size = super.getPreferredSize();
    return new Dimension( size.width + 8, size.height );
  }

  @Override
  public Dimension getMinimumSize() {
    return getPreferredSize();
  }

  private static class FocalLengthListener implements DocumentListener {
    private final TLcdGLViewCanvasStereo fCanvas;

    public FocalLengthListener( TLcdGLViewCanvasStereo aCanvas ) {
      fCanvas = aCanvas;
    }

    public void insertUpdate( DocumentEvent e ) {
      try {
        String s = e.getDocument().getText( 0, e.getDocument().getLength() );
        fCanvas.setFocalLength( Double.valueOf( s ).doubleValue() );
        fCanvas.repaint();
      } catch ( Exception e1 ) {
        // do nothing...
      }
    }

    public void removeUpdate( DocumentEvent e ) {
      try {
        String s = e.getDocument().getText( 0, e.getDocument().getLength() );
        fCanvas.setFocalLength( Double.valueOf( s ).doubleValue() );
        fCanvas.repaint();
      } catch ( Exception e1 ) {
        // do nothing...
      }
    }

    public void changedUpdate( DocumentEvent e ) {
      try {
        String s = e.getDocument().getText( 0, e.getDocument().getLength() );
        fCanvas.setFocalLength( Double.valueOf( s ).doubleValue() );
        fCanvas.repaint();
      } catch ( Exception e1 ) {
        // do nothing...
      }
    }
  }

  private static class SeparationListener implements DocumentListener {
    private final TLcdGLViewCanvasStereo fCanvas;

    public SeparationListener( TLcdGLViewCanvasStereo aCanvas ) {
      fCanvas = aCanvas;
    }

    public void insertUpdate( DocumentEvent e ) {
      try {
        String s = e.getDocument().getText( 0, e.getDocument().getLength() );
        fCanvas.setEyeSeparation( Double.valueOf( s ).doubleValue() );
        fCanvas.repaint();
      } catch ( Exception e1 ) {
        // do nothing...
      }
    }

    public void removeUpdate( DocumentEvent e ) {
      try {
        String s = e.getDocument().getText( 0, e.getDocument().getLength() );
        fCanvas.setEyeSeparation( Double.valueOf( s ).doubleValue() );
        fCanvas.repaint();
      } catch ( Exception e1 ) {
        // do nothing...
      }
    }

    public void changedUpdate( DocumentEvent e ) {
      try {
        String s = e.getDocument().getText( 0, e.getDocument().getLength() );
        fCanvas.setEyeSeparation( Double.valueOf( s ).doubleValue() );
        fCanvas.repaint();
      } catch ( Exception e1 ) {
        // do nothing...
      }
    }
  }
}
