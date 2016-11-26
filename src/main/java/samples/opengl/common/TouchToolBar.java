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
import javax.swing.SwingUtilities;

import com.luciad.gui.ILcdAction;
import com.luciad.gui.TLcdGUIIcon;
import com.luciad.gui.TLcdImageIcon;
import com.luciad.gui.TLcdUserDialog;
import com.luciad.gui.swing.TLcdDialogManagerSW;
import com.luciad.gui.swing.TLcdSWAction;
import com.luciad.input.ILcdAWTEventListener;
import com.luciad.input.touch.TLcdTouchDevice;
import com.luciad.view.TLcdDeleteSelectionAction;
import com.luciad.view.opengl.ILcdGLView;
import com.luciad.view.opengl.TLcdGLViewLocation;
import com.luciad.view.opengl.action.TLcdGLSetControllerAction;
import com.luciad.view.opengl.controller.ALcdGLChainableController;
import com.luciad.view.opengl.controller.ILcdGLController;
import com.luciad.view.opengl.controller.touch.TLcdGLTouchNavigateController;
import com.luciad.view.opengl.controller.touch.TLcdGLTouchSelectController;

import samples.common.action.ShowReadMeAction;
import samples.gxy.common.touch.TouchUtil;
import samples.opengl.touch.TouchGLRulerController;
import samples.opengl.touch.TouchGLSelectController;

/**
 * A simple toolbar providing the user with the ability to select a touch controller for the 3D view.
 */
public class TouchToolBar extends JToolBar {

  // controller to button mapping
  private Hashtable<ILcdGLController, AbstractButton> fControllerButtonTable = new Hashtable<ILcdGLController, AbstractButton>();
  private Hashtable<ILcdGLController, ILcdGLController> fWrappedControllerTable = new Hashtable<ILcdGLController, ILcdGLController>();

  private ILcdGLView fGLView;

  // default controllers
  private ALcdGLChainableController fSelectController;
  private ALcdGLChainableController fNavigateController;
  private ALcdGLChainableController fRulerController;

  // toggle button group
  private ButtonGroup fButtonGroup = new ButtonGroup();

  private int fIndexCustomButtons = 0;

  private boolean fShowReadmeAtStartup = true;
  private boolean fTouchSupported;

  public TouchToolBar( ILcdGLView aGLView, Component aParent ) {
    this(aGLView, aParent, TLcdGLViewLocation.LocationMode.SEA_LEVEL );
  }

  public TouchToolBar( ILcdGLView aGLView, Component aParent, boolean aShowReadme ) {
    this(aGLView, aParent, TLcdGLViewLocation.LocationMode.SEA_LEVEL );
    fShowReadmeAtStartup = aShowReadme;
  }

  public TouchToolBar( ILcdGLView aGLView, Component aParent, TLcdGLViewLocation.LocationMode aMouseLocationMode ) {
    fGLView = aGLView;
    fGLView.addPropertyChangeListener( new MyControllerListener() );
    fTouchSupported = TLcdTouchDevice.getInstance().getTouchDeviceStatus() == TLcdTouchDevice.Status.READY;

    TLcdGLTouchSelectController selectController;
    if ( aParent instanceof Abstract3DPanel ) {
      selectController = new TouchGLSelectController( ( ( Abstract3DPanel )aParent).getOverlayPanel() );
    }
    else {
      selectController = new TLcdGLTouchSelectController();
    }
    selectController.setIcon( new TLcdImageIcon( "images/gui/touchicons/select_32.png") );
    selectController.setTouchAndHoldIcon( new TLcdImageIcon( "images/gui/touchicons/pressandhold_80.png" ) );
    ILcdAction[] actions = new ILcdAction[]
        {new ClearSelectionAction(fGLView), new TLcdDeleteSelectionAction(fGLView)};
    ShowPopupAction show_popup = new ShowPopupAction( actions, fGLView );
    selectController.setPostTouchAndHoldAction( show_popup );

     fSelectController = selectController;

    fNavigateController = new TLcdGLTouchNavigateController();
    fNavigateController.setIcon( new TLcdImageIcon( "images/gui/touchicons/navigate_32.png") );
    fSelectController.appendController( fNavigateController );

    if ( aParent instanceof Abstract3DPanel ) {
      fRulerController = new TouchGLRulerController( ( ( Abstract3DPanel )aParent).getOverlayPanel() );
      fRulerController.setIcon( new TLcdGUIIcon( TLcdGUIIcon.RULER2_32 ) );
      fRulerController.appendController( fNavigateController );
    }

    addController( fSelectController );
    addController( fNavigateController );

    if ( fRulerController != null ) addController(fRulerController);
    addSpace();
    addComponent( Box.createHorizontalGlue(), -1 );

    addComponent( new MouseLocationComponent( fGLView, aMouseLocationMode ), -1 );
    addSpace( -1 );

    // Let all LuciadLightspeed dialogs use Swing.
    TLcdUserDialog.setDialogManager( new TLcdDialogManagerSW() );

    // Add action to display information on how to use this sample
    if ( aParent != null ) {
      // Add action to display information on how to use this sample
      ShowReadMeAction showReadme = ShowReadMeAction.createForSample(aParent);
      if ( showReadme != null ) {
        addAction( showReadme, -1 );
        if( fShowReadmeAtStartup ) {
          showReadme.actionPerformed( null );
        }
      }
    }

    // Set our select as the initial ILcdGXYController
    fGLView.setController( getWrappedController( fSelectController ) );
    SwingUtilities.invokeLater( new Runnable() {
      public void run() {
        TouchUtil.checkTouchDevice( null );
      }
    });
  }

  public void addController( ILcdGLController aController ) {
    addController( aController, fIndexCustomButtons++ );
  }

  public ILcdGLController getWrappedController( ILcdGLController aController ) {
    return fWrappedControllerTable.get( aController );
  }

  private void addController( ILcdGLController aController, int aIndex ) {
    ILcdGLController controllerWrapper = fTouchSupported || !( aController instanceof ILcdAWTEventListener ) ?
                                          aController :
                                          new MouseToTouchGLControllerWrapper( aController );
    fWrappedControllerTable.put( aController, controllerWrapper );
    insertController( controllerWrapper, aIndex );
  }

  private void insertController( ILcdGLController aController, int aIndex ) {
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

  public ALcdGLChainableController getCompositeControllerSelect() {
    return fSelectController;
  }

  public ALcdGLChainableController getCompositeControllerNavigate() {
    return fNavigateController;
  }

  // Listen to the view to set the button corresponding to the active
  // controller selected.
  private class MyControllerListener implements PropertyChangeListener {
    public void propertyChange( PropertyChangeEvent aEvent ) {
      if ( "Controller".equals( aEvent.getPropertyName() ) ) {
        AbstractButton button = fControllerButtonTable.get( aEvent.getNewValue() );
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
}
