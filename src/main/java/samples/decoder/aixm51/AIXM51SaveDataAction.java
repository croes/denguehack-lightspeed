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
package samples.decoder.aixm51;

import static javax.swing.JOptionPane.WARNING_MESSAGE;
import static javax.swing.JOptionPane.showMessageDialog;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import javax.swing.JDialog;
import javax.swing.JFileChooser;

import com.luciad.format.aixm51.xml.TLcdAIXM51ModelEncoder;
import com.luciad.gui.ALcdAction;
import com.luciad.gui.TLcdIconFactory;
import com.luciad.io.TLcdStatusOutputStreamFactory;
import com.luciad.util.ILcdStatusListener;
import com.luciad.util.collections.ILcdCollection;
import com.luciad.view.ILcdLayer;

import samples.gxy.common.ProgressUtil;

/**
 * This action uses a <code>JFileChooser</code> to save the currently selected layer.
 * The action uses the <code>TLcdAIXM51ModelEncoder</code> to check whether the selected layer contains an
 * AIXM 5.1 model that can be saved, and subsequently saves the model to the selected file.
 */
public class AIXM51SaveDataAction extends ALcdAction {

  private final JFileChooser fFileChooser = new JFileChooser();
  private final String fBaseDirectory;
  private final Component fParentComponent;
  private final ILcdCollection<ILcdLayer> fLayerControl;
  private final TLcdAIXM51ModelEncoder fModelEncoder = new TLcdAIXM51ModelEncoder();

  public AIXM51SaveDataAction( String aBaseDirectory, Component aParentComponent, ILcdCollection<ILcdLayer> aLayerControl ) {
    super("Save current layer...", TLcdIconFactory.create(TLcdIconFactory.SAVE_ICON));
    fBaseDirectory = aBaseDirectory;
    fParentComponent = aParentComponent;
    fLayerControl = aLayerControl;
  }

  public void actionPerformed( ActionEvent aActionEvent ) {
    Iterator<ILcdLayer> selectedObjects = fLayerControl.iterator();
    if (!selectedObjects.hasNext()) {
      showMessageDialog( fParentComponent, "No layer selected, please select a layer to save.", "No layer selected", WARNING_MESSAGE );
      return;
    }
    final ILcdLayer layer = selectedObjects.next();
    //Check if we can export this file, with a temporary file ( nothing will be exported yet )
    if ( !fModelEncoder.canExport( layer.getModel(), "temp" ) ){
      showMessageDialog( fParentComponent, "The selected layer can not be saved. It is either not an AIXM51 model, or " +
                                           "not a leaf node in the layer structure.", "Selected layer invalid", WARNING_MESSAGE );
      return;
    }
    // Set a default directory.
    fFileChooser.setCurrentDirectory( new File( fBaseDirectory ) );

    // Set a file filter.
    fFileChooser.setFileFilter(new AIXM51FileFilter());

    // Set a default file name, based upon the layer label.
    fFileChooser.setSelectedFile( new File(layer.getModel().getModelDescriptor().getDisplayName() + "." + AIXM51FileFilter.DEFAULT_EXTENSION));

    if ( fFileChooser.showSaveDialog( fParentComponent ) == JFileChooser.APPROVE_OPTION ) {
      final File selectedFile = fFileChooser.getSelectedFile();
      final String source_path_with_extension;
      boolean hasExtension = new AIXM51FileFilter().accept( selectedFile );
      if ( !hasExtension ) {
        source_path_with_extension = selectedFile.getAbsolutePath() + "." + AIXM51FileFilter.DEFAULT_EXTENSION;
      }
      else {
        source_path_with_extension = selectedFile.getAbsolutePath();
      }

      // Check whether the layer can be encoded to an AIXM 5.1 file.
      if ( fModelEncoder.canExport( layer.getModel(), source_path_with_extension ) ) {

        // Start a thread that saves the AIXM 5.1 file.
        Thread save_data_thread = new Thread( new Runnable() {
          public void run() {
            // Add a progress bar to the model encoder.
            JDialog progress = ProgressUtil.createProgressDialog( fParentComponent, "Saving AIXM 5.1 data..." );
            final TLcdStatusOutputStreamFactory statusOutputStreamFactory = new TLcdStatusOutputStreamFactory();
            if ( progress instanceof ILcdStatusListener ) {
              statusOutputStreamFactory.addStatusEventListener( ( ILcdStatusListener ) progress );
              fModelEncoder.setOutputStreamFactory( statusOutputStreamFactory );
            }
            try {
              fModelEncoder.export( layer.getModel(), source_path_with_extension );
            }
            catch ( IOException ioe ) {
              showMessageDialog( fParentComponent, "Could not encode to an AIXM 5.1 data file.\n"+ioe.getMessage(), "Could not encode.", WARNING_MESSAGE );
            }

            // Remove progress bar from the model encoder and dispose the dialog.
            if ( progress instanceof ILcdStatusListener ) {
              statusOutputStreamFactory.removeStatusEventListener( ( ILcdStatusListener ) progress );
            }
            progress.dispose();
          }
        });
        save_data_thread.setPriority( Thread.MIN_PRIORITY );
        save_data_thread.start();
      }
      else {
        showMessageDialog( fParentComponent, "The selected layer does not contain a valid AIXM 5.1 model.", "Invalid layer", WARNING_MESSAGE );
      }
    }
  }
}
