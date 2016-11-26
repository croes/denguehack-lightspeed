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
package samples.metadata;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractButton;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;

import com.luciad.gui.ILcdAction;
import com.luciad.gui.swing.TLcdSWAction;

import samples.common.action.ShowReadMeAction;

/**
 * A JToolBar extension to use in the meta data samples.
 */
public class MetadataToolBar extends JToolBar {

  public MetadataToolBar( ILcdAction aOpenAction, boolean aShowReadMe, Component aParent ) {
    addAction( aOpenAction );

    ShowReadMeAction showReadme = ShowReadMeAction.createForSample(aParent);
    if ( showReadme != null ) {
      addAction( showReadme );
      // Display an info panel when running standalone.
      if ( aShowReadMe ) {
        showReadme.actionPerformed( null );
      }
    }
  }

  private void addAction( ILcdAction aAction ) {
    insertButton( aAction, -1, false );
  }

  private void insertButton( ILcdAction aAction, int aIndex, boolean aToggle ) {
    AbstractButton button = new MyToggleButton( aToggle );
    button.setAction( new TLcdSWAction(aAction) );
    // we don't want the text of the action to clutter the button
    button.setText( "" );
    add( button, aIndex );
  }


  // JToggleButton extension to handle Toggle buttons as regular buttons.
  private static class MyToggleButton extends JToggleButton {

    boolean fRealToggleButton = true;

    public MyToggleButton( boolean aRealToggleButton ) {
      fRealToggleButton = aRealToggleButton;
    }

    public void addActionListener( ActionListener aActionListener ) {
      if ( !fRealToggleButton ) {
        super.addActionListener( new MyActionListener( aActionListener ) );
      } else {
        super.addActionListener( aActionListener );
      }
    }

    private class MyActionListener implements ActionListener {

      ActionListener fActionListener;

      public MyActionListener( ActionListener aActionListener ) {
        fActionListener = aActionListener;
      }

      public void actionPerformed( ActionEvent e ) {
        fActionListener.actionPerformed( e );
        MyToggleButton.this.setSelected( false );
      }
    }
  }
}
