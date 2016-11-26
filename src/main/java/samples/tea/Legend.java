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
package samples.tea;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.luciad.gui.TLcdImageIcon;

import samples.gxy.common.TitledPanel;

/**
 * This is a class to represent the legend used to paint the
 * <code>ILcdLineOfSightCoverage</code>.
 *
 * @see <code>ILcdLineOfSightCoverage</code>
 */
public class Legend extends JPanel {

  /**
   * Creates a new legend panel.
   *
   * @param aColors the legend color list.
   * @param aLabels the legend label list.
   * @param aUp the legend order
   */
  public Legend( Color[] aColors, String[] aLabels, boolean aUp ) {
    this( aColors, aLabels, aUp, false );
  }

  /**
   * Creates a new legend panel.
   *
   * @param aColors the legend color list.
   * @param aLabels the legend label list.
   * @param aUp the legend order
   * @param aLabelLastEntry a flag indicating if the last entry should be labeled.
   */
  public Legend( Color[] aColors, String[] aLabels, boolean aUp, boolean aLabelLastEntry ) {
    super();
    JPanel color_panel = new JPanel( new GridLayout( aColors.length, 1, 0, 2 ) );
    color_panel.setBorder( BorderFactory.createEmptyBorder( 0, 5, 0, 5 ) );
    if ( aUp ) {
      int last_index = aColors.length - 1;
      for ( int i = 0; i < last_index ; i++ ) {
        color_panel.add( new LegendEntry( aColors[ i ], aLabels[ i ], true ) );
      }
      color_panel.add( new LegendEntry( aColors[ aColors.length - 1 ], aLabels[ last_index ], aLabelLastEntry ) );
    } else {
      for ( int i = aColors.length - 1; i >= 0 ; i-- ) {
        color_panel.add( new LegendEntry( aColors[ i ], aLabels[ i ], true ) );
      }
    }

    JScrollPane scroll_pane = new JScrollPane( color_panel );
    scroll_pane.setBorder( null );

    JPanel legend_panel = new JPanel( new BorderLayout() );
    legend_panel.add( BorderLayout.NORTH, color_panel );
    legend_panel.add( BorderLayout.CENTER, Box.createGlue() );

    setLayout( new BorderLayout() );
    add( BorderLayout.CENTER, TitledPanel.createTitledPanel( "Legend", legend_panel ) );
  }

  /**
   * A class to represent a single entry in the legend.
   */
  class LegendEntry extends Container {
    JLabel fIconLabel;
    JLabel fTextLabel;
    MyLegendIcon fIcon;

    public LegendEntry( Color aColor, String aLabel, boolean aWithAltitude ) {

      fIconLabel = new JLabel();
      fIcon = new MyLegendIcon( 40, 20 );
      fIcon.setColor( aColor );
      fIconLabel.setIcon( fIcon );
      fTextLabel = new JLabel( aWithAltitude ? aLabel : "" );

      setLayout( new BorderLayout( 5, 0 ) );
      add( BorderLayout.WEST, fIconLabel );
      add( BorderLayout.CENTER, fTextLabel );
    }

    public Color getColor() {
      return fIcon.getColor();
    }
  }

  /**
   * A class to represent the legend icon.
   */
  class MyLegendIcon
          extends TLcdImageIcon
          implements Icon {

    private int fHeight = -1;
    private int fWidth = -1;
    private Color fColor;

    public MyLegendIcon() {
    }

    public MyLegendIcon( int aWidth, int aHeight ) {
      fWidth = aWidth;
      fHeight = aHeight;
    }

    public void setWidth( int aWidth ) {
      fWidth = aWidth;
    }

    public int getWidth() {
      return fWidth;
    }

    public void setHeight( int aHeight ) {
      fHeight = aHeight;
    }

    public int getHeight() {
      return fHeight;
    }

    public void setColor( Color aColor ) {
      fColor = aColor;
    }

    public Color getColor() {
      return fColor;
    }

    public void paintIcon( Component c, Graphics g, int x, int y ) {
      g.setColor( fColor );
      g.fillRect( 3, 3, fWidth - 8, fHeight - 8 );
      g.setColor( Color.black );
      g.drawRect( 3, 3, fWidth - 8, fHeight - 8 );

    }

    public int getIconWidth() {
      if ( fWidth > 0 )
        return fWidth;
      return 0;
    }

    public int getIconHeight() {
      if ( fHeight > 0 )
        return fHeight;
      return 0;
    }

    public Object clone() {
      return new MyLegendIcon();
    }
  }
}
