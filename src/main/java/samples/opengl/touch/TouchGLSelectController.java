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
package samples.opengl.touch;

import com.luciad.gui.swing.TLcdOverlayLayout;
import com.luciad.view.opengl.ILcdGLView;
import com.luciad.view.opengl.controller.TLcdGLSelectControllerModel;
import com.luciad.view.opengl.controller.touch.TLcdGLTouchSelectController;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.EnumSet;

import static com.luciad.view.opengl.controller.TLcdGLSelectControllerModel.SelectHowMode;
import static samples.gxy.touch.editing.TouchSelectEditController.MyModeButton;

/**
 * A 3d touch based select controller. It extends from TLcdGLTouchSelectController and adds
 * a panel with some buttons over the view for advanced interaction.
 * <ul>
 * <li> The first button can be used to toggle between dragging a rectangle to select and touching objects</li>
 * <li> The second button changes the select how mode between FIRST_TOUCHED, ADD and REMOVE.</li>
 * <li> The third button toggles select how choose. When active a popup menu will appear allowing the user
 * to choose which object is to be selected.</li>
 * </ul>
 */
public class TouchGLSelectController extends TLcdGLTouchSelectController {
  
  private Container fContainer;
  private MyButtonPanel fButtonPanel = new MyButtonPanel();
  private boolean fChoose = false;
  private EnumSet<SelectHowMode> fHowSelect;

  public TouchGLSelectController( Container aContainer ) {
    fContainer = aContainer;
    fHowSelect = EnumSet.of( SelectHowMode.FIRST_TOUCHED );
  }

  @Override
  protected void startInteractionImpl( ILcdGLView aGLView ) {
    super.startInteractionImpl( aGLView );
    if ( fContainer != null ) {
      fContainer.add( fButtonPanel, TLcdOverlayLayout.Location.NORTH_WEST );
      revalidateContainer();
    }
  }

  @Override
  protected void terminateInteractionImpl( ILcdGLView aGLView ) {
    super.terminateInteractionImpl( aGLView );
    if ( fContainer != null ) {
      fContainer.remove( fButtonPanel );
      revalidateContainer();
    }
  }

  @Override
  protected EnumSet<SelectHowMode> selectHowMode( Rectangle aSelectionBounds,
                                                                              TLcdGLSelectControllerModel.InputMode aInputMode,
                                                                              EnumSet<TLcdGLSelectControllerModel.SelectByWhatMode> aSelectByWhatMode ) {
    return fHowSelect;
  }

  private void revalidateContainer() {
    if ( fContainer instanceof JComponent ) {
      ( ( JComponent ) fContainer ).revalidate();
    }
    else {
      fContainer.invalidate();
      fContainer.validate();
    }
    fContainer.repaint();
  }

  private void setSelectHowImpl( int aMode ) {
    switch ( aMode ) {
      case 0 :
        fHowSelect = EnumSet.of( SelectHowMode.FIRST_TOUCHED );
        break;
      case 1 :
        fHowSelect = EnumSet.of( SelectHowMode.ADD );
        break;
      case 2 :
        fHowSelect = EnumSet.of( SelectHowMode.REMOVE );
        break;
      default :
        fHowSelect = EnumSet.of( SelectHowMode.FIRST_TOUCHED );
        break;
    }
    if ( fChoose ) {
      fHowSelect.add( SelectHowMode.CHOOSE );
    }
  }

  private class MyButtonPanel extends JPanel {

    private MyButtonPanel() {
      init();
    }

    private void init() {
      setLayout( new BorderLayout() );

      final MyModeButton dragButton = new MyModeButton( new String[] { "images/gui/touchicons/tap_64.png",
                                                                        "images/gui/touchicons/drag_64.png" } );
      dragButton.addActionListener( new ActionListener() {
        public void actionPerformed( ActionEvent e ) {
          dragButton.incrementMode();
          setDragRectangle( dragButton.getMode() == 1 );
        }
      } );

      final MyModeButton selectHowButton = new MyModeButton( new String[]{ "images/gui/touchicons/selecttouch_64.png",
                                                                                   "images/gui/touchicons/selectplus_64.png",
                                                                                   "images/gui/touchicons/selectmin_64.png" } );
      selectHowButton.addActionListener( new ActionListener() {
        public void actionPerformed( ActionEvent e ) {
          selectHowButton.incrementMode();
          setSelectHowImpl( selectHowButton.getMode() );
        }
      } );

      final MyModeButton chooseButton = new MyModeButton( new String[] { "images/gui/touchicons/nochoose_64.png",
                                                                         "images/gui/touchicons/choose_64.png" } );
      chooseButton.addActionListener( new ActionListener() {
        public void actionPerformed( ActionEvent e ) {
          chooseButton.incrementMode();
          fChoose = chooseButton.getMode() == 1;
          setSelectHowImpl( selectHowButton.getMode() );
        }
      } );

      JPanel content = new JPanel();
      content.setLayout( new BoxLayout( content, BoxLayout.Y_AXIS ) );
      content.add( dragButton );
      content.add( selectHowButton );
      content.add( chooseButton );

      setOpaque( false, content );
      add( content, BorderLayout.CENTER );

      setOpaque( false );
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
  }

}
