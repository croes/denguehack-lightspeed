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
import com.luciad.view.opengl.controller.touch.TLcdGLTouchRulerController;
import samples.gxy.touch.editing.TouchNewController;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * This touch ruler controller extends from TLcdGLTouchRulerController, and adds a button panel
 * over the view for advanced interaction, from top to bottom: undo, redo, commit and cancel.
 */
public class TouchGLRulerController extends TLcdGLTouchRulerController {

  private Container fContainer;
  private MyButtonPanel fButtonPanel = new MyButtonPanel();

  public TouchGLRulerController( Container aContainer ) {
    fContainer = aContainer;
  }

  private void updatePanel() {
    fButtonPanel.update( canCommit(), canCancel(), canUndo(), canRedo() );
  }

  @Override
  protected void startInteractionImpl( ILcdGLView aGLView ) {
    super.startInteractionImpl( aGLView );
    if ( fContainer != null ) {
      fContainer.add( fButtonPanel, TLcdOverlayLayout.Location.NORTH_WEST );
      revalidateContainer();
    }
    updatePanel();
  }

  @Override
  protected void terminateInteractionImpl( ILcdGLView aGLView ) {
    super.terminateInteractionImpl( aGLView );
    if ( fContainer != null ) {
      fContainer.remove( fButtonPanel );
      revalidateContainer();
    }
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

  @Override
  public void handleAWTEvent( AWTEvent aEvent ) {
    super.handleAWTEvent( aEvent );
    updatePanel();
  }

  private class MyButtonPanel extends JPanel {

    private JButton fCommitButton;
    private JButton fCancelButton;
    private JButton fUndoButton;
    private JButton fRedoButton;

    private MyButtonPanel() {
      init();
    }

    private void init() {
      setLayout( new BorderLayout() );
      fCommitButton = new TouchNewController.MyActionButton( "images/gui/touchicons/commit_64.png" );
      fCommitButton.addActionListener( new ActionListener() {
        public void actionPerformed( ActionEvent e ) {
          doCommit();
          updatePanel();
        }
      });

      fCancelButton = new TouchNewController.MyActionButton( "images/gui/touchicons/cancel_64.png" );
      fCancelButton.addActionListener( new ActionListener() {
        public void actionPerformed( ActionEvent e ) {
          doCancel();
          updatePanel();
        }
      });

      fUndoButton = new TouchNewController.MyActionButton( "images/gui/touchicons/undo_64.png" );
      fUndoButton.addActionListener( new ActionListener() {
        public void actionPerformed( ActionEvent e ) {
          doUndo();
          updatePanel();
        }
      });

      fRedoButton = new TouchNewController.MyActionButton( "images/gui/touchicons/redo_64.png" );
      fRedoButton.addActionListener( new ActionListener() {
        public void actionPerformed( ActionEvent e ) {
          doRedo();
          updatePanel();
        }
      });

      setLayout( new BoxLayout( this, BoxLayout.Y_AXIS ) );

      setOpaque( false, this );

      add( fUndoButton );
      add( fRedoButton );
      add( fCommitButton );
      add( fCancelButton );
      update( canCommit(), canCancel(), canUndo(), canRedo() );

      setVisible( true );
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
 
    public void update( boolean aCommit, boolean aCancel, boolean aUndo, boolean aRedo ) {
      fCommitButton.setEnabled( aCommit );
      fCancelButton.setEnabled( aCancel );
      fUndoButton.setEnabled( aUndo );
      fRedoButton.setEnabled( aRedo );
    }
  }
}
