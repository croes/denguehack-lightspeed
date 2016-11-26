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
package samples.geometry.topology.interactive;

import com.luciad.geometry.ILcd2DAdvancedBinaryTopology;
import com.luciad.geometry.ILcdIntersectionMatrixPattern;
import com.luciad.shape.ILcdShape;

import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.GridLayout;

/**
 * Displays topology relations for two shapes.
 */
public class TopologyRelationProvider extends JPanel {

  private ILcd2DAdvancedBinaryTopology fTopology;

  private ILcdShape fShape1;
  private ILcdShape fShape2;

  private JLabel[] fLabels = new JLabel[18];


  public TopologyRelationProvider( ILcd2DAdvancedBinaryTopology aTopology ) {
    fTopology = aTopology;
    setLayout( new GridLayout( 9, 1 ) );

    for ( int i = 0; i < 18; i++ ) {
      fLabels[ i ] = new JLabel();
      add( fLabels[ i ] );
    }

    fLabels[ 0 ].setText( "Intersects:" );
    fLabels[ 2 ].setText( "Contains:" );
    fLabels[ 4 ].setText( "Within:" );
    fLabels[ 6 ].setText( "Covers:" );
    fLabels[ 8 ].setText( "Covered_by:" );
    fLabels[ 10 ].setText( "Touches:" );
    fLabels[ 12 ].setText( "Crosses:" );
    fLabels[ 14 ].setText( "Overlaps:" );
    fLabels[ 16 ].setText( "Equals:" );
  }

  public void setShapes( ILcdShape aShape1, ILcdShape aShape2 ) {
    fShape1 = aShape1;
    fShape2 = aShape2;
  }

  public void updateRelations() {
    if ( fShape1 != null && fShape2 != null ) {
      setLabel(fLabels[1], fTopology.checkTopology( fShape1, fShape2, ILcdIntersectionMatrixPattern.INTERSECTS));
      setLabel(fLabels[3], fTopology.checkTopology( fShape1, fShape2, ILcdIntersectionMatrixPattern.CONTAINS));
      setLabel(fLabels[5], fTopology.checkTopology( fShape1, fShape2, ILcdIntersectionMatrixPattern.WITHIN));
      setLabel(fLabels[7], fTopology.checkTopology( fShape1, fShape2, ILcdIntersectionMatrixPattern.COVERS));
      setLabel(fLabels[9], fTopology.checkTopology( fShape1, fShape2, ILcdIntersectionMatrixPattern.COVERED_BY));
      setLabel(fLabels[11], fTopology.checkTopology( fShape1, fShape2, ILcdIntersectionMatrixPattern.TOUCHES));
      setLabel(fLabels[13], fTopology.checkTopology( fShape1, fShape2, ILcdIntersectionMatrixPattern.CROSSES));
      setLabel(fLabels[15], fTopology.checkTopology( fShape1, fShape2, ILcdIntersectionMatrixPattern.OVERLAPS));
      setLabel(fLabels[17], fTopology.checkTopology( fShape1, fShape2, ILcdIntersectionMatrixPattern.EQUALS));
    }

  }

  private void setLabel( JLabel aLabel, boolean aBoolean ) {
    if(aBoolean){
        aLabel.setForeground( Color.GREEN );
        aLabel.setText( "TRUE" );
      } else {
        aLabel.setForeground( Color.RED );
        aLabel.setText( "FALSE" );
      }
  }

  public void setDefaultText() {
    for ( int i = 0; i < 9; i++ ) {
      fLabels[ 2 * i + 1 ].setText( "" );
    }
  }

}
