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

import java.awt.Component;
import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Hashtable;

import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;

import com.luciad.gui.ILcdAction;
import com.luciad.gui.TLcdUserDialog;
import com.luciad.gui.swing.TLcdDialogManagerSW;
import com.luciad.gui.swing.TLcdSWAction;
import com.luciad.reference.ILcdGeocentricReference;
import com.luciad.view.opengl.ILcdGLView;
import com.luciad.view.opengl.TLcdGLViewLocation;
import com.luciad.view.opengl.action.TLcdGLSetControllerAction;
import com.luciad.view.opengl.controller.ILcdGLController;
import com.luciad.view.opengl.controller.TLcdGLRulerController;
import com.luciad.view.opengl.controller.TLcdGLSelectionController;
import com.luciad.view.opengl.controller.composite.TLcdGLCompositeController;
import com.luciad.view.opengl.controller.composite.TLcdGLGeocentricPanControllerAction;
import com.luciad.view.opengl.controller.composite.TLcdGLGeocentricRotationControllerAction;
import com.luciad.view.opengl.controller.composite.TLcdGLZoomControllerAction;

import samples.common.action.ShowReadMeAction;

/**
 * A simple toolbar providing the user with the ability to select a controller for the 3D view.
 */
public class Toolbar extends JToolBar {

  // controller to button mapping
  private static Hashtable fControllerButtonTable = new Hashtable();

  private ILcdGLView fGLView;

  // default controllers
  private TLcdGLSelectionController fSelectionController;
  private TLcdGLCompositeController fCompositeController;
  private TLcdGLRulerController fRulerController;

  // toggle button group
  private ButtonGroup fButtonGroup = new ButtonGroup();

  private int fIndexCustomButtons = 0;

  private boolean fMouseCenteredRotation = true;
  private boolean fShowReadmeAtStartup = true;

  public Toolbar( ILcdGLView aGLView, Component aParent, boolean aUseMouseCenteredRotation, boolean aShowReadme ) {
    this(aGLView, aParent, aUseMouseCenteredRotation);
    fShowReadmeAtStartup = aShowReadme;
  }

  public Toolbar( ILcdGLView aGLView, Component aParent, boolean aUseMouseCenteredRotation ) {
    this(aGLView, aParent);
    fMouseCenteredRotation = aUseMouseCenteredRotation;
  }

  public Toolbar( ILcdGLView aGLView, Component aParent ) {
    this( aGLView, aParent, TLcdGLViewLocation.LocationMode.SEA_LEVEL );
  }

  public Toolbar( ILcdGLView aGLView, Component aParent, TLcdGLViewLocation.LocationMode aMouseLocationMode ) {
    fGLView = aGLView;
    fGLView.addPropertyChangeListener( new MyControllerListener() );

    fSelectionController = new TLcdGLSelectionController();
//    fDragCameraController = new TLcdGLDragCameraController();
//    fZoomDistanceController = new ZoomDistanceController();
//    fRotateController = new RotateController();
    fCompositeController = new TLcdGLCompositeController();
    // In case of a geocentric world reference, adapt the pan and rotation actions.
    if (fGLView.getXYZWorldReference() instanceof ILcdGeocentricReference) {
      fCompositeController.setPanAction(new TLcdGLGeocentricPanControllerAction());
      fCompositeController.setRotateAction(new TLcdGLGeocentricRotationControllerAction(fMouseCenteredRotation));
    }
    fCompositeController.setZoomAction(new ReverseZoomControllerAction());

    fRulerController = new TLcdGLRulerController();

    addGLController( fSelectionController );
    addGLController( fCompositeController );
    addGLController( fRulerController );
    addSpace();

    addComponent( Box.createHorizontalGlue(), -1 );
    addComponent( new MouseLocationComponent(fGLView, aMouseLocationMode), -1 );
    addSpace( -1 );

    // Let all LuciadLightspeed dialogs use Swing.
    TLcdUserDialog.setDialogManager( new TLcdDialogManagerSW() );

    // Add action to display information on how to use this sample
    if ( aParent != null ) {
      ShowReadMeAction showReadme = ShowReadMeAction.createForSample(aParent);
      if ( showReadme != null ) {
        addAction( showReadme, -1 );
        // Display an info panel when running standalone.
        if ( fShowReadmeAtStartup ) {
          showReadme.actionPerformed( null );
        }
      }
    }

    // Set our pan as the initial ILcdGXYController
    fGLView.setController( fCompositeController );
  }

  public void addGLController( ILcdGLController aController ) {
    addGLController( aController, fIndexCustomButtons++ );
  }

  private void addGLController( ILcdGLController aController, int aIndex ) {
    insertGLController( aController, aIndex );
  }

  private void insertGLController( ILcdGLController aController, int aIndex ) {
    TLcdSWAction swing_action = new TLcdSWAction( new TLcdGLSetControllerAction( fGLView, aController ) );
    AbstractButton button = new JToggleButton() {
      //Make the button square
      @Override
      public Dimension getPreferredSize() {
        Dimension size = super.getPreferredSize();
        int max = Math.max( size.width, size.height );
        return new Dimension( max, max );
      }

      @Override
      public Dimension getMaximumSize() {
        return getPreferredSize();
      }

      @Override
      public Dimension getMinimumSize() {
        return getPreferredSize();
      }
    };
    button.addActionListener( swing_action );
    button.setIcon( (Icon) swing_action.getValue( Action.SMALL_ICON ) );
    button.setToolTipText( (String) swing_action.getValue( Action.SHORT_DESCRIPTION ) );
    button.setEnabled( swing_action.isEnabled() );
    swing_action.addPropertyChangeListener( new EnabledMediator( button, swing_action ) );
    add( button, aIndex );

    fControllerButtonTable.put( aController, button );
  }

  public void addAction( ILcdAction aAction ) {
    addAction( aAction, fIndexCustomButtons++ );
  }

  public void addAction( ILcdAction aAction, int aIndex ) {
    insertAction( aAction, aIndex );
  }

  private void insertAction( ILcdAction aAction, int aIndex ) {
    TLcdSWAction swing_action = new TLcdSWAction( aAction );
    AbstractButton button = new JButton();
    button.addActionListener( swing_action );
    button.setIcon( (Icon) swing_action.getValue( Action.SMALL_ICON ) );
    button.setToolTipText( (String) swing_action.getValue( Action.SHORT_DESCRIPTION ) );
    button.setText( null );
    swing_action.addPropertyChangeListener( new EnabledMediator( button, swing_action ) );
    add( button, aIndex );
  }

  public void addSpace() {
    addSpace( fIndexCustomButtons++ );
  }

  public void addSpace( int aIndex ) {
    addSpace( 10, 10, aIndex );
  }

  public void addSpace( int aWidth, int aHeight ) {
    addSpace( aWidth, aHeight, fIndexCustomButtons++ );
  }

  public void addSpace( int aWidth, int aHeight, int aIndex ) {
    addComponent( Box.createRigidArea( new Dimension( aWidth, aHeight ) ), aIndex );
  }

  public void setMouseCenteredRotation(boolean aEnabled) {
    if (fMouseCenteredRotation == aEnabled) {
      return;
    }
    fMouseCenteredRotation = aEnabled;
    fCompositeController.setRotateAction(new TLcdGLGeocentricRotationControllerAction(fMouseCenteredRotation));
  }

  public void addComponent( Component aComponent ) {
    addComponent( aComponent, fIndexCustomButtons++ );
  }

  private void addComponent( Component aComponent, int aIndex ) {
    add( aComponent, aIndex );
  }

  protected void addImpl( Component comp, Object constraints, int index ) {
    super.addImpl( comp, constraints, index );
    if ( comp instanceof AbstractButton )
      fButtonGroup.add( ( (AbstractButton) comp ) );
  }

  public TLcdGLSelectionController getSelectionController() {
    return fSelectionController;
  }

  public TLcdGLCompositeController getCompositeController() {
    return fCompositeController;
  }

  // Listen to the view to set the button corresponding to the active
  // controller selected.
  private static class MyControllerListener implements PropertyChangeListener {
    public void propertyChange( PropertyChangeEvent aEvent ) {
      if ( "Controller".equals( aEvent.getPropertyName() ) ) {
        AbstractButton button = (AbstractButton) fControllerButtonTable.get( aEvent.getNewValue() );
        if ( button != null ) {
          button.doClick();
        }
      }
    }
  }

  // Listener that updates the enabled state of the button according to the action.
  private static class EnabledMediator implements PropertyChangeListener {
    private final AbstractButton fButton;
    private final Action fAction;

    public EnabledMediator( AbstractButton aButton, Action aAction ) {
      fButton = aButton;
      fAction = aAction;
    }

    public void propertyChange( PropertyChangeEvent evt ) {
      if ( "enabled".equals( evt.getPropertyName() ) ) {
        fButton.setEnabled( fAction.isEnabled() );
      }
    }
  }

  // Zoom controller action that reverses the zoom direction
  private static class ReverseZoomControllerAction extends TLcdGLZoomControllerAction {
    @Override
    public void doInteraction( ILcdGLView aGLView, double aX, double aY, double aDeltaX, double aDeltaY ) {
      aDeltaY = -aDeltaY;
      super.doInteraction( aGLView, aX, aY, aDeltaX, aDeltaY );
    }
  }
}
