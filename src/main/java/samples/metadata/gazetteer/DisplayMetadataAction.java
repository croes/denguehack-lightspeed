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
package samples.metadata.gazetteer;

import com.luciad.format.metadata.model.metadataentityset.TLcdISO19115Metadata;
import com.luciad.gui.TLcdAWTUtil;
import com.luciad.view.gxy.ILcdGXYView;
import com.luciad.view.gxy.controller.TLcdGXYEditController2;

import samples.metadata.MetadataTree;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JDialog;
import javax.swing.JScrollPane;

/**
 * An action which displays the contents of a <code>ILcdISO19115Metadata</code> objects in
 * a tree.
 */
class DisplayMetadataAction extends AbstractAction {

  private ILcdGXYView fGXYView;
  private TLcdGXYEditController2 fEditController;

  private JDialog fDialog;
  private MetadataTree fMetadataTree = new MetadataTree();
  private TouchedMetadataUtil fTouchedMetadataUtil = new TouchedMetadataUtil();
  private boolean fMetadataSet = false;

  public DisplayMetadataAction( ILcdGXYView aGXYView, TLcdGXYEditController2 aEditController ) {
    fGXYView = aGXYView;
    fEditController = aEditController;
  }

  private JDialog buildDialog() {
    JScrollPane scroll_pane = new JScrollPane( fMetadataTree );

    JDialog dialog = new JDialog( TLcdAWTUtil.findParentFrame( (Component) fGXYView ) );
    dialog.setTitle( "Metadata information" );
    dialog.getContentPane().setLayout( new BorderLayout() );
    dialog.getContentPane().add( scroll_pane, BorderLayout.CENTER );

    return dialog;
  }

  public void actionPerformed( ActionEvent e ) {

    if ( fDialog == null ) {
      fDialog = buildDialog();
    }

    int x = fEditController.lastXPressed();
    int y = fEditController.lastYPressed();

    TLcdISO19115Metadata metadata =
            fTouchedMetadataUtil.findTouchedMetadata( x,y, fGXYView );

    fMetadataSet |= ( metadata != null );

    // test whether a dataset was ever set and whether the dataset is different from null.
    // this ensures that there will be something in the metadata tree panel.
    if ( fMetadataSet && ( metadata != null ) ) {
      // set the selected meta data object if not null
      fMetadataTree.setMetadata( metadata );
      if ( !fDialog.isVisible() ) {
        fDialog.pack();
        fDialog.setVisible( true );
      }
      if ( !fDialog.isShowing() ) {
        fDialog.toFront();
      }
    }
  }

  protected Object clone() throws CloneNotSupportedException {
    return super.clone();
  }
}
