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
package samples.tea.lightspeed.los.gui;

import com.luciad.gui.swing.TLcdColorMapCustomizer;
import com.luciad.tea.lightspeed.los.view.TLspLOSCoverageStyle;
import com.luciad.util.TLcdColorMap;
import com.luciad.view.lightspeed.style.ILspWorldElevationStyle.ElevationMode;
import samples.tea.lightspeed.los.view.LOSCoverageStyler;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * <p>A panel that can change the style properties of a LOS Coverage.</p>
 * <p>
 *   This panel is used in the sample to modify the styling of the LOS output shape.
 *   This panel contains the following
 *   <ul>
 *     <li>A <code>TLcdColorMapCustomizer</code> that can set the color of each
 *     altitude value.</li>
 *     <li>A checkbox that toggles whether or not the LOS Coverage Matrix is draped.</li>
 *   </ul>
 *   This panel modifies the styling of the <code>LOSCoverageStyler</code> set at
 *   construction time.
 * </p>
 */
public class LOSStylePanel extends JPanel {

  private JLabel                 fColorMapLabel          = new JLabel( "Color Map:" );
  private JCheckBox              fDrapedCheckbox         = new JCheckBox( "Draped", false );
  private JLabel                 fUnknownColorTextLabel  = new JLabel( "Unknown Color");
  private JLabel                 fInvisibleColorTextLabel  = new JLabel( "Invisible Color");
  private MyColorPanel           fUnknownColorLabel;
  private MyColorPanel           fInvisibleColorLabel;
  private TLcdColorMapCustomizer fColorMapCustomizer     = new TLcdColorMapCustomizer();
  private MyChangeListener       fDrapedChangeListener   = new MyChangeListener();
  private MyChangeListener       fColorMapChangeListener = new MyChangeListener();
  private Box                    fFillerBox              = new Box( BoxLayout.X_AXIS );
  private Box                    fFillerBox2              = new Box( BoxLayout.X_AXIS );

  private LOSCoverageStyler fLOSStyler;

  /**
   * Creates a new LOS Style Panel for a given <code>LOSCoverageStyler</code>
   * @param aLOSCoverageStyler the LOS coverage styler to manipulate.
   */
  public LOSStylePanel( LOSCoverageStyler aLOSCoverageStyler ) {
    fLOSStyler = aLOSCoverageStyler;
    initPanel();
    updatePanel();
  }

  private void initPanel() {
    fUnknownColorLabel      = new MyColorPanel( fLOSStyler, MyColorPanel.Type.UNKNOWN );
    fInvisibleColorLabel    = new MyColorPanel( fLOSStyler, MyColorPanel.Type.INVISIBLE );
    fColorMapLabel.setHorizontalAlignment( SwingConstants.RIGHT );
    fColorMapCustomizer.setOpaque( true );
    fDrapedCheckbox.setOpaque( true );
    fColorMapCustomizer.setMasterTransparencyVisible( true );
    fUnknownColorTextLabel.setFont( fUnknownColorTextLabel.getFont().deriveFont( Font.PLAIN, fUnknownColorTextLabel.getFont().getSize() - 2 ));
    fInvisibleColorTextLabel.setFont( fInvisibleColorTextLabel.getFont().deriveFont( Font.PLAIN, fInvisibleColorTextLabel.getFont().getSize() - 2 ));
    
    GridBagLayout gridBagLayout = new GridBagLayout();
    setLayout( gridBagLayout );
    GridBagConstraints gridBagConstraints = new GridBagConstraints();


    addComponent( fColorMapCustomizer, 1, 1, 3, 1, gridBagConstraints, gridBagLayout );
    addComponent( fFillerBox, 2, 1, 6, 1, gridBagConstraints, gridBagLayout );
    addComponent( fFillerBox2,     3, 1, 6, 1, gridBagConstraints, gridBagLayout );
    addComponent( fUnknownColorTextLabel,  4, 1, 3, 1, gridBagConstraints, gridBagLayout );
    addComponent( fUnknownColorLabel,  5, 3, 3, 1, gridBagConstraints, gridBagLayout );
    addComponent( fInvisibleColorTextLabel,  6, 1, 3, 1, gridBagConstraints, gridBagLayout );
    addComponent( fInvisibleColorLabel,  7, 3, 3, 1, gridBagConstraints, gridBagLayout );
    addComponent( fDrapedCheckbox,     8, 1, 3, 1, gridBagConstraints, gridBagLayout );
  }

  //Method addComponent
  public void addComponent( Component component, int row, int column, int width, int height, GridBagConstraints aGridBagConstraints, GridBagLayout aGridBagLayout ) {
    aGridBagConstraints.gridx = column;
    aGridBagConstraints.gridy = row;
    aGridBagConstraints.gridwidth = width;
    aGridBagConstraints.gridheight = height;
    aGridBagLayout.setConstraints( component, aGridBagConstraints );
    add( component );
  }

  private void updatePanel() {
    fDrapedCheckbox.removeChangeListener( fDrapedChangeListener );
    fColorMapCustomizer.removePropertyChangeListener( fColorMapChangeListener );
    fUnknownColorLabel.removePropertyChangeListener( fColorMapChangeListener );
    fInvisibleColorLabel.removePropertyChangeListener( fColorMapChangeListener );
    TLspLOSCoverageStyle losStyle = fLOSStyler.getLOSCoverageStyle();
    if ( losStyle != null ) {
      fDrapedCheckbox.setSelected( losStyle.getElevationMode() == ElevationMode.ON_TERRAIN );
      fColorMapCustomizer.setObject( losStyle.getColorMap().clone() );
      fDrapedCheckbox.addChangeListener( fDrapedChangeListener );
      fColorMapCustomizer.addPropertyChangeListener( fColorMapChangeListener );
      fUnknownColorLabel.setChosenColor( fLOSStyler.getLOSCoverageStyle().getUnknownColor() );
      fInvisibleColorLabel.setChosenColor( fLOSStyler.getLOSCoverageStyle().getInvisibleColor() );
      fInvisibleColorLabel.addPropertyChangeListener( fColorMapChangeListener );
      fUnknownColorLabel.addPropertyChangeListener( fColorMapChangeListener );
    }
  }

  private class MyChangeListener implements PropertyChangeListener, ChangeListener{

    private MyChangeListener() {
    }

    @Override public void propertyChange( PropertyChangeEvent evt ) {
      handleChange();
    }

    @Override public void stateChanged( ChangeEvent e ) {
      handleChange();
    }

    private void handleChange() {
      TLcdColorMap colorMapClone = ( TLcdColorMap ) fColorMapCustomizer.getColorMap().clone();
      ElevationMode elevationMode = fDrapedCheckbox.isSelected() ? ElevationMode.ON_TERRAIN : ElevationMode.ABOVE_ELLIPSOID;
      fLOSStyler.setLOSCoverageStyle(TLspLOSCoverageStyle.newBuilder().colorMap(colorMapClone ).unknownColor(fUnknownColorLabel.getChosenColor()).invisibleColor(fInvisibleColorLabel.getChosenColor()).elevationMode(elevationMode ).build() );
    }
  }


  private static class MyColorPanel extends JButton implements ActionListener {

    private LOSCoverageStyler fLOSStyler;
    private Type fType;

    private enum Type {
      UNKNOWN,
      INVISIBLE
    }

    private MyColorPanel( LOSCoverageStyler aLOSStyler, Type aType ) {
      fType = aType;
      fLOSStyler = aLOSStyler;
      addActionListener( this );
    }

    public void setChosenColor(Color aColor) {
      setIcon(new ColorIcon(aColor));
    }

    public Color getChosenColor() {
      Icon icon = getIcon();
      if (icon instanceof ColorIcon) {
        ColorIcon colorIcon = (ColorIcon) icon;
        return colorIcon.fColor;
      }
      return null;
    }

    @Override
    public Dimension getPreferredSize() {
      return new Dimension(ColorIcon.WIDTH, ColorIcon.HEIGHT);
    }

    @Override
    public void actionPerformed( ActionEvent e ) {
      Color color = null;
      if (fType==Type.UNKNOWN) {
        color = JColorChooser.showDialog( null, "Unknown color chooser", fLOSStyler.getLOSCoverageStyle().getUnknownColor() );
      }
      else if (fType==Type.INVISIBLE) {
        color = JColorChooser.showDialog( null, "Invisible color chooser", fLOSStyler.getLOSCoverageStyle().getInvisibleColor() );
      }
      if ( color!=null ) {
        setIcon(new ColorIcon(color));
      }
    }
  }

  private static class ColorIcon implements Icon {
    private static final int WIDTH = 48;
    private static final int HEIGHT = 20;

    private final Color fColor;

    private ColorIcon(Color aColor) {
      if (aColor == null) {
        throw new IllegalArgumentException("The color for this icon should never be null!");
      }
      fColor = aColor;
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
      g = g.create();
      try {
        g.setColor(fColor);
        g.fillRect(x, y, getIconWidth(), getIconHeight());
      } finally {
        g.dispose();
      }
    }

    @Override
    public int getIconWidth() {
      return WIDTH;
    }

    @Override
    public int getIconHeight() {
      return HEIGHT;
    }
  }
}
