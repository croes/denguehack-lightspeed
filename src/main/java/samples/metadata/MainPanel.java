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

import com.luciad.format.metadata.model.metadataentityset.TLcdISO19115Metadata;
import com.luciad.format.metadata.xml.TLcdISO19139MetadataDecoder;
import com.luciad.gui.ALcdAction;
import com.luciad.gui.TLcdAWTUtil;
import com.luciad.gui.TLcdIconFactory;
import com.luciad.io.TLcdStatusInputStreamFactory;
import samples.common.SamplePanel;
import samples.common.LuciadFrame;
import samples.gxy.common.TitledPanel;

import javax.swing.BorderFactory;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.filechooser.FileFilter;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import static samples.gxy.common.ProgressUtil.*;

/**
 * This sample demonstrates how to load and display data in ISO19139 format using the
 * class <code>TLcdISO19139MetadataDecoder</code>.
 */
public class MainPanel extends SamplePanel {

  private MetadataTree fMetadataTree = new MetadataTree();
  private MyErrorMessageSupport fErrorMessageSupport = new MyErrorMessageSupport();

  protected void createGUI() {
    // Create toolbar
    MetadataToolBar tool_bar = new MetadataToolBar( new ISO19139OpenAction( this ), true, this );

    // Top component
    JScrollPane tree_panel = new JScrollPane( fMetadataTree );
    tree_panel.setBackground( new Color( 255, 255, 255 ) );
    tree_panel.setHorizontalScrollBarPolicy( JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED );
    tree_panel.setVerticalScrollBarPolicy( JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED );

    JPanel titled_tree = TitledPanel.createTitledPanel( "Metadata structure", tree_panel );
    JPanel top_component = new JPanel( new BorderLayout() );
    top_component.add( titled_tree, BorderLayout.CENTER );

    // Bottom component
    JScrollPane error_display_panel = new JScrollPane( fErrorMessageSupport );
    error_display_panel.setHorizontalScrollBarPolicy( JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED );
    error_display_panel.setVerticalScrollBarPolicy( JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED );

    JPanel bottom_component = TitledPanel.createTitledPanel( "Decoding log", error_display_panel );

    // Split pane
    JSplitPane split_pane = new JSplitPane( JSplitPane.VERTICAL_SPLIT );
    split_pane.setTopComponent( top_component );
    split_pane.setBottomComponent( bottom_component );
    split_pane.setDividerLocation( 0.75 );
    split_pane.setBorder( BorderFactory.createEmptyBorder( 5, 5, 5, 5 ) );

    setLayout( new BorderLayout() );
    add( tool_bar, BorderLayout.NORTH );
    add( split_pane, BorderLayout.CENTER );
  }

  private class ISO19139OpenAction extends ALcdAction {

    private JFileChooser fFileChooser = new JFileChooser();
    private Component fParentComponent;

    private TLcdISO19139MetadataDecoder fMetadataDecoder;

    public ISO19139OpenAction( Component aParentComponent ) {
      super( "Open ISO19139 data file...", TLcdIconFactory.create(TLcdIconFactory.OPEN_ICON) );
      fParentComponent = aParentComponent;
      fMetadataDecoder = new TLcdISO19139MetadataDecoder();

      fFileChooser.setCurrentDirectory(new File("./resources/Data/metadata/iso19139"));
      fFileChooser.setFileFilter(new FileFilter() {
        public boolean accept(File aFile) {
          return aFile.isDirectory() || aFile.getName().toLowerCase().endsWith(".xml");
        }

        public String getDescription() {
          return "XML files (*.xml)";
        }
      });
    }

    public void actionPerformed( ActionEvent aEvent ) {

      final Frame parent_frame = TLcdAWTUtil.findParentFrame( aEvent );

      if ( fFileChooser.showOpenDialog( parent_frame ) == JFileChooser.APPROVE_OPTION ) {
        // Start a thread that decodes the ISO19139 data file.
        Thread load_data_thread = new Thread( new Runnable() {
          public void run() {
            // Add a progress bar to the model decoder.
            ProgressDialog progress = createProgressDialog( fParentComponent, "Loading ISO19139 data..." );
            TLcdStatusInputStreamFactory input_stream_factory = new TLcdStatusInputStreamFactory();
            input_stream_factory.addStatusEventListener( progress );
            fMetadataDecoder.setInputStreamFactory( input_stream_factory );

            String source_path = fFileChooser.getSelectedFile().getAbsolutePath();
            fErrorMessageSupport.clear();
            try {
              TLcdISO19115Metadata metadata = fMetadataDecoder.decodeMetadata( source_path );

              // Update tree
              fMetadataTree.setMetadata( metadata );
            }
            catch (FileNotFoundException exc) {
              JOptionPane.showMessageDialog(parent_frame, "The file '" + source_path + "' could not be found.", "File not found", JOptionPane.WARNING_MESSAGE);
            }
            catch ( IOException exc ) {
              JOptionPane.showMessageDialog( parent_frame, "Invalid ISO19139 data file. Cannot decode file.", "Cannot decode", JOptionPane.WARNING_MESSAGE );
              fErrorMessageSupport.addErrorReport( exc.getMessage() );
            }

            // Remove progress bar from model decoder and dispose dialog.
            input_stream_factory.removeStatusEventListener( progress );
            progress.dispose();
          }
        } );
        load_data_thread.setPriority( Thread.MIN_PRIORITY );
        load_data_thread.start();
      }
    }

  }

  private static class MyErrorMessageSupport extends JTextArea {

    private int fErrorCount = 0;

    public MyErrorMessageSupport() {
      setLineWrap( true );
      setWrapStyleWord( true );
    }

    public void addErrorReport( String aErrorReport ) {
      if ( getLineCount() == 0 ) {
        setText( "The following errors occurred while decoding:\n" );
      }
      append( aErrorReport );
      if (fErrorCount == 0) {
        setText( "The following errors occurred while decoding:\n" );
      }
      append( aErrorReport );
      fErrorCount++;
    }

    public void clear() {
      setText( "" );
      fErrorCount = 0;
    }
  }

  // Main method
  public static void main( final String[] aArgs ) {
    EventQueue.invokeLater( new Runnable() {
      public void run() {
        JPopupMenu.setDefaultLightWeightPopupEnabled( false );
        new LuciadFrame(new MainPanel(), "Decoding ISO19139 metadata" );
      }
    } );
  }
}
