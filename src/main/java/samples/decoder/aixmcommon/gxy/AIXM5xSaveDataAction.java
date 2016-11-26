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
package samples.decoder.aixmcommon.gxy;

import static javax.swing.JOptionPane.WARNING_MESSAGE;
import static javax.swing.JOptionPane.showMessageDialog;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.JDialog;
import javax.swing.JFileChooser;

import com.luciad.format.aixm5.xml.TLcdAIXM5ModelEncoder;
import com.luciad.format.aixm51.xml.TLcdAIXM51ModelEncoder;
import com.luciad.gui.ALcdAction;
import com.luciad.gui.TLcdIconFactory;
import com.luciad.io.TLcdStatusOutputStreamFactory;
import com.luciad.model.ILcdModelEncoder;
import com.luciad.util.ILcdStatusListener;
import com.luciad.util.collections.ILcdCollection;
import com.luciad.view.ILcdLayer;

import samples.gxy.common.ProgressUtil;

/**
 * This action uses a <code>JFileChooser</code> to save the currently selected
 * layer. The action uses the <code>TLcdAIXM51ModelEncoder</code> and
 * <code>TLcdAIXM5ModelEncoder</code> to check whether the selected layer
 * contains an AIXM 5.x model that can be saved, and subsequently saves the
 * model to the selected file.
 */
public class AIXM5xSaveDataAction extends ALcdAction {

  private final JFileChooser fFileChooser = new JFileChooser();
  private final String fBaseDirectory;
  private final Component fParentComponent;
  private final ILcdCollection<ILcdLayer> fSelectedLayers;
  private final TLcdAIXM51ModelEncoder f51ModelEncoder = new TLcdAIXM51ModelEncoder();
  private final TLcdAIXM5ModelEncoder f50ModelEncoder = new TLcdAIXM5ModelEncoder();

  public AIXM5xSaveDataAction( String aBaseDirectory, Component aParentComponent, ILcdCollection<ILcdLayer> aSelectedLayers) {
    super("Save current layer...", TLcdIconFactory.create(TLcdIconFactory.SAVE_ICON));
    fBaseDirectory = aBaseDirectory;
    fParentComponent = aParentComponent;
    fSelectedLayers = aSelectedLayers;
  }

  public void actionPerformed( ActionEvent aActionEvent ) {
    if (fSelectedLayers.isEmpty()) {
      showMessageDialog( fParentComponent, "No layer selected, please select a layer to save.", "No layer selected", WARNING_MESSAGE );
      return;
    }
    final ILcdLayer layer = fSelectedLayers.iterator().next();

    // Set a default directory.
    fFileChooser.setCurrentDirectory( new File( fBaseDirectory ) );

    // Set a file filter.
    fFileChooser.setFileFilter(new AIXM5xFileFilter());

    // Set a default file name, based upon the layer label.
    fFileChooser.setSelectedFile( new File(layer.getModel().getModelDescriptor().getDisplayName() + "." + AIXM5xFileFilter.DEFAULT_EXTENSION));

    if ( fFileChooser.showSaveDialog( fParentComponent ) == JFileChooser.APPROVE_OPTION ) {
      final File selectedFile = fFileChooser.getSelectedFile();
      final String source_path_with_extension;
      boolean hasExtension = new AIXM5xFileFilter().accept( selectedFile );
      if ( !hasExtension ) {
        source_path_with_extension = selectedFile.getAbsolutePath() + "." + AIXM5xFileFilter.DEFAULT_EXTENSION;
      }
      else {
        source_path_with_extension = selectedFile.getAbsolutePath();
      }
      final ILcdModelEncoder encoder;

      final String dataFormatName;
      // Check whether the layer can be encoded to an AIXM 5.x file.
      if ( f51ModelEncoder.canExport( layer.getModel(), source_path_with_extension ) ) {
        dataFormatName = "AIXM 5.1";
        encoder = f51ModelEncoder;
      } else if ( f50ModelEncoder.canExport( layer.getModel(), source_path_with_extension ) ) {
        dataFormatName = "AIXM 5.0";
        encoder = f50ModelEncoder;
      } else {
        dataFormatName = "AIXM 5.x";
        encoder = null;
      }

      if ( encoder != null ) {
        // Start a thread that saves the AIXM 5.x file.
        Thread save_data_thread = new Thread( new Runnable() {
          public void run() {
            // Add a progress bar to the model encoder.
            JDialog progress = ProgressUtil.createProgressDialog( fParentComponent, "Saving " + dataFormatName + " data..." );
            final TLcdStatusOutputStreamFactory statusOutputStreamFactory = new TLcdStatusOutputStreamFactory();
            statusOutputStreamFactory.addStatusEventListener((ILcdStatusListener) progress);
            f51ModelEncoder.setOutputStreamFactory(statusOutputStreamFactory);
            try {
              f51ModelEncoder.export( layer.getModel(), source_path_with_extension );
            } catch (IOException ioe) {
              ioe.printStackTrace();
              showMessageDialog( fParentComponent, "Could not encode to an " + dataFormatName + " data file.\n" + ioe.getMessage(), "Could not encode.", WARNING_MESSAGE );
            } finally {
              // Remove progress bar from the model encoder and dispose the dialog.
              statusOutputStreamFactory.removeStatusEventListener((ILcdStatusListener) progress);
              progress.dispose();
            }
          }
        });
        save_data_thread.setPriority( Thread.MIN_PRIORITY );
        save_data_thread.start();
      }
      else {
        showMessageDialog( fParentComponent, "The selected layer does not contain a valid AIXM 5.1 or AIXM 5.0 model.", "Invalid layer", WARNING_MESSAGE );
      }
    }
  }
}
