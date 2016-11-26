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
package samples.tea.gxy.los;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JSeparator;

import com.luciad.gui.ALcdAction;
import com.luciad.gui.TLcdImageIcon;
import com.luciad.tea.ALcdTerrainElevationProvider;

/**
 * Action that displays a panel to compute point-to-point intervisibility.
 */
class ShowP2PPanelAction extends ALcdAction {

  private P2PPanel fP2PPanel;
  private Frame fOwnerFrame;
  private JDialog fDialog;

  // buttons
  private JButton fComputeButton = new JButton( "Compute" );
  private JButton fCloseButton = new JButton( "Close" );

  ShowP2PPanelAction( P2PPanel aP2PPanel,
                      Frame aOwnerFrame,
                      ALcdTerrainElevationProvider aTerrainElevationProvider ) {
    fP2PPanel = aP2PPanel;
    fOwnerFrame = aOwnerFrame;

    fComputeButton.addActionListener( new CreateP2PAction( fP2PPanel, aTerrainElevationProvider ) );
    fCloseButton.addActionListener( new ActionListener() {
      public void actionPerformed( ActionEvent e ) {
        close();
      }
    } );

    setIcon(new TLcdImageIcon("images/gui/i16_eyes.gif"));
    setName( "Compute point-to-point intervisibility" );
    setShortDescription( "Compute point-to-point intervisibility" );
  }

  public void actionPerformed( ActionEvent event ) {
    if ( fDialog == null ) {
      JPanel separator_panel = new JPanel( new BorderLayout() );
      separator_panel.setBorder( BorderFactory.createEmptyBorder( 0, 3, 0, 4 ) );
      separator_panel.add( new JSeparator( JSeparator.HORIZONTAL ), BorderLayout.CENTER );

      fDialog = new JDialog( fOwnerFrame, "Point-to-point settings" );
      fDialog.getContentPane().setLayout( new BorderLayout() );
      fDialog.getContentPane().add( fP2PPanel, BorderLayout.NORTH );
      fDialog.getContentPane().add( separator_panel, BorderLayout.CENTER );
      fDialog.getContentPane().add( buildButtonPanel(), BorderLayout.SOUTH );
      fDialog.pack();
      if ( fOwnerFrame != null ) {
        // position the dialog next to the frame
        Point owner_frame_location = fOwnerFrame.getLocationOnScreen();
        Point dialog_location = new Point( (int) ( owner_frame_location.getX() - fDialog.getWidth() ),
                (int) owner_frame_location.getY() );
        fDialog.setLocation( dialog_location );
      }
    }

    if ( !fDialog.isVisible() ) {
      fDialog.setVisible( true );
    }
    fDialog.toFront();
  }

  private void close() {
    fDialog.setVisible( false );
  }

  private JPanel buildButtonPanel() {
    JPanel button_panel = new JPanel( new FlowLayout( FlowLayout.RIGHT ) );
    button_panel.add( fComputeButton );
    button_panel.add( fCloseButton );
    return button_panel;
  }
}
