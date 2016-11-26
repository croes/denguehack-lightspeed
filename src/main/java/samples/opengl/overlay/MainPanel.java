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
package samples.opengl.overlay;

import com.luciad.geodesy.TLcdGeodeticDatum;
import com.luciad.gui.swing.TLcdOverlayLayout;
import com.luciad.gui.swing.navigationcontrols.ALcdCompassNavigationControl;
import com.luciad.gui.swing.navigationcontrols.TLcdMouseOverGroup;
import com.luciad.reference.ILcdGeocentricReference;
import com.luciad.reference.TLcdGeocentricReference;
import com.luciad.util.height.ILcdModelHeightProviderFactory;
import com.luciad.view.opengl.ILcdGLLayerFactory;
import com.luciad.view.opengl.ILcdGLView;
import com.luciad.view.opengl.TLcdGLViewCanvas;
import com.luciad.view.opengl.swing.navigationcontrols.TLcdGLGeocentricCompassNavigationControl;
import com.luciad.view.opengl.swing.navigationcontrols.TLcdGLGridCompassNavigationControl;
import com.luciad.view.opengl.swing.navigationcontrols.TLcdGLPanNavigationControl;
import com.luciad.view.opengl.swing.navigationcontrols.TLcdGLZoomNavigationControl;
import samples.opengl.common.Abstract3DPanel;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.border.TitledBorder;
import javax.swing.event.MouseInputAdapter;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.io.IOException;

/**
 * This implementation of Abstract3DPanel, redefines the addOverlayComponents method. The navigation
 * controls are broken up and added in different locations. Also a draggable panel is added to illustrate
 * the NO_LAYOUT option of TLcdOverlayLayout.
 */
class MainPanel extends Abstract3DPanel {

  public MainPanel() {
    super(false);  
  }

  public ILcdGLLayerFactory getGLLayerFactory() {
    return new LayerFactory();
  }

  protected TLcdGLViewCanvas createCanvas() {
    TLcdGLViewCanvas canvas = super.createCanvas();
    canvas.setXYZWorldReference(new TLcdGeocentricReference(new TLcdGeodeticDatum()));
    Abstract3DPanel.setupCamera( canvas );
    Abstract3DPanel.setupLights( canvas );
    //Abstract3DPanel.setupFog   ( canvas );
    return canvas;
  }

  @Override
  protected void addOverlayComponents() {
    String imagePath = "images/gui/navigationcontrols/small/";
    Component comp = createCompass( getCanvas(), imagePath, true, null );
    if ( comp != null ) {
      getOverlayPanel().add( comp, TLcdOverlayLayout.Location.NORTH_WEST );
    }
    comp = createPan( getCanvas(), imagePath, false, null );
    if ( comp != null ) {
      getOverlayPanel().add( comp, TLcdOverlayLayout.Location.SOUTH_WEST );
    }
    comp = createZoom( getCanvas(), imagePath, false, null );
    if ( comp != null ) {
      getOverlayPanel().add( comp, TLcdOverlayLayout.Location.EAST );
    }
    comp = createDraggableOverlay();
    if ( comp != null ) {
      getOverlayPanel().add( comp, TLcdOverlayLayout.Location.NO_LAYOUT );
    }
  }

  protected void addData() {
    ILcdGLView canvas = getCanvas();
    canvas.addModel( samples.opengl.overlay.ModelFactory.createGridModel() );
  }

  private Component createDraggableOverlay() {
    // The panel that will contain the actual content
    JPanel content = new JPanel() {
      @Override
      public Dimension getPreferredSize() {
        return new Dimension( 150, 150 );
      }
    };
    content.setLayout( new BoxLayout( content, BoxLayout.Y_AXIS ) );

    // Add a title around the content
    JPanel titled = new JPanel();
    titled.setLayout( new BoxLayout( titled, BoxLayout.Y_AXIS ) );
    TitledBorder border = BorderFactory.createTitledBorder(
        BorderFactory.createEmptyBorder( 1, 1, 1, 1 ), "Drag Me!", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.BELOW_TOP );
    titled.setBorder( border );
    titled.add( content );

    // Add another panel around it, that contains the border
    final JPanel bordered = new JPanel( new BorderLayout() ) {
      @Override
      protected void paintComponent( Graphics g ) {
        Color c = g.getColor();
        g.setColor( new Color( 255, 255, 255, 180 ) );

        g.fillRect( 0, 0, getWidth(), getHeight() );
        g.setColor( c );
        super.paintComponent( g );
      }

      @Override
      public void paint( Graphics g ) {
        Graphics2D graphics2D = ( Graphics2D ) g;
        Object aa = graphics2D.getRenderingHint( RenderingHints.KEY_ANTIALIASING );
        graphics2D.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
        super.paint( g );
        if ( aa == null ) {
          graphics2D.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_DEFAULT  );
        }
        else {
          graphics2D.setRenderingHint( RenderingHints.KEY_ANTIALIASING, aa );
        }
      }
    };
    MouseInputAdapter mIA = new MouseInputAdapter() {

      int fPrevX;
      int fPrevY;
      int fOrigX;
      int fOrigY;

      @Override
      public void mousePressed( MouseEvent e ) {
        fOrigX = bordered.getX();
        fOrigY = bordered.getY();
        fPrevX = e.getX() + fOrigX;
        fPrevY = e.getY() + fOrigY;
      }

      @Override
      public void mouseDragged( MouseEvent e ) {
        int x = bordered.getX() + e.getX() - fPrevX;
        int y = bordered.getY() + e.getY() - fPrevY;
        bordered.setLocation( fOrigX + x, fOrigY + y );
      }
    };
    bordered.addMouseListener( mIA );
    bordered.addMouseMotionListener( mIA );

    bordered.add( titled, BorderLayout.CENTER );
    bordered.setBorder( BorderFactory.createMatteBorder( 1, 1, 1, 1, Color.BLACK ) );

    //Make the titled panel and its children transparent (optional step)
    setOpaque( false, titled );


    // Now add the opaque content (JTree doesn't very well support being transparent)
    JTree tree = new JTree();
    content.add( new JScrollPane( tree ) );

    // This panel stays opaque, but has a transparent background color.
    // All its children that aren't opaque (e.g. 'titled') will therefore have this
    // background color as well (optional step)
    bordered.setOpaque( false );
    bordered.setBackground( new Color( 255, 255, 255, 180 ) ); //transparent white

    bordered.setBounds( 40, 70, 200, 200 );
    return bordered;
  }

  private void setOpaque( boolean aOpaque, Component aComponent ) {
    if ( aComponent instanceof JComponent ) {
      JComponent jComponent = ( JComponent ) aComponent;
      jComponent.setOpaque( aOpaque );

      for ( Component child : jComponent.getComponents() ) {
        setOpaque( aOpaque, child );
      }
    }
  }

  public Component createCompass( ILcdGLView aGLView, String aImagePath,
                                  boolean aAlwaysActive, ILcdModelHeightProviderFactory aHeightProviderFactory) {
    Component compass;
    try {
      if ( aGLView.getXYZWorldReference() instanceof ILcdGeocentricReference ) {
        compass = new TLcdGLGeocentricCompassNavigationControl(
            aImagePath + ALcdCompassNavigationControl.COMPASS_COMPONENT_DIR, aGLView,
            aHeightProviderFactory );
      }
      else {
        compass = new TLcdGLGridCompassNavigationControl(
            aImagePath + ALcdCompassNavigationControl.COMPASS_COMPONENT_DIR, aGLView,
            aHeightProviderFactory );
      }
    } catch ( IOException ignored ) {
      compass = null;
    }
    if ( compass != null ) {
      TLcdMouseOverGroup mOG = new TLcdMouseOverGroup( aAlwaysActive );
      mOG.add( compass );
    }
    return compass;
  }

  public Component createPan( ILcdGLView aGLView, String aImagePath,
                                                  boolean aAlwaysActive, ILcdModelHeightProviderFactory aHeightProviderFactory ) {
    Component pan;
    try {
      pan = new TLcdGLPanNavigationControl(
          aImagePath + TLcdGLPanNavigationControl.PAN_COMPONENT_DIR, aGLView,
          aHeightProviderFactory );
    } catch ( IOException ignored ) {
      pan = null;
    }
    TLcdMouseOverGroup mOG = new TLcdMouseOverGroup( aAlwaysActive );
    mOG.add( pan );
    return pan;
  }

  public Component createZoom( ILcdGLView aGLView, String aImagePath,
                               boolean aAlwaysActive, ILcdModelHeightProviderFactory aHeightProviderFactory ) {
    Component zoom;
    try {
      zoom = new TLcdGLZoomNavigationControl(
          aImagePath + TLcdGLZoomNavigationControl.ZOOM_COMPONENT_DIR, aGLView,
          aHeightProviderFactory );
    } catch ( IOException ignored ) {
      zoom = null;
    }
    TLcdMouseOverGroup mOG = new TLcdMouseOverGroup( aAlwaysActive );
    mOG.add( zoom );
    return zoom;
  }

}
