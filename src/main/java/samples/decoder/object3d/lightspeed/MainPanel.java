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
package samples.decoder.object3d.lightspeed;

import java.awt.Dialog;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.luciad.earth.model.TLcdEarthRepositoryModelDecoder;
import com.luciad.format.object3d.obj.TLcdOBJModelDecoder;
import com.luciad.format.object3d.openflight.TLcdOpenFlightModelDecoder;
import com.luciad.gui.TLcdAWTUtil;
import com.luciad.io.TLcdStatusInputStreamFactory;
import com.luciad.model.ILcdModelDecoder;
import com.luciad.model.ILcdModelProducerListener;
import com.luciad.model.TLcdModelProducerEvent;
import com.luciad.model.TLcdOpenAction;
import com.luciad.util.ILcdStatusListener;
import com.luciad.view.lightspeed.ILspAWTView;
import com.luciad.view.lightspeed.controller.ILspController;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.services.asynchronous.TLspTaskExecutorRunnable;

import samples.gxy.common.ProgressUtil;
import samples.lightspeed.common.CreateAndEditToolBar;
import samples.lightspeed.common.FitUtil;
import samples.lightspeed.common.LightspeedSample;
import samples.lightspeed.common.ToolBar;

public class MainPanel extends LightspeedSample implements DropTargetListener {

  private TLcdOpenAction fOpenAction;
  private DropTarget fDropTarget;
  private TLcdStatusInputStreamFactory fInputStreamFactory;

  // Custom toolbar which adds editing and creation controllers to the default Lightspeed sample toolbar
  private CreateAndEditToolBar fCreateAndEditToolBar;

  @Override
  protected ToolBar[] createToolBars( ILspAWTView aView) {
    final ToolBar regularToolBar = new ToolBar( aView, this, false, true );
    if ( fCreateAndEditToolBar == null ) {
      fCreateAndEditToolBar = new CreateAndEditToolBar( aView, this, regularToolBar.getButtonGroup()) {
        @Override
        protected ILspController createDefaultController() {
            return regularToolBar.getDefaultController();
        }
      };
    }
    return new ToolBar[]{regularToolBar, fCreateAndEditToolBar};
  }

  @Override
  protected void createGUI() {
    super.createGUI();

    if ( fInputStreamFactory == null ) {
      fInputStreamFactory = new TLcdStatusInputStreamFactory();
    }

    Dialog progressDialog = ProgressUtil
        .createProgressDialog( TLcdAWTUtil.findParentFrame( getView() ), "Loading data..." );
    if ( progressDialog instanceof ILcdStatusListener ) {
      fInputStreamFactory.addStatusEventListener( ( ILcdStatusListener ) progressDialog );
    }

    fOpenAction = new TLcdOpenAction( TLcdAWTUtil.findParentFrame( getView() ) );
    fOpenAction.setFirstInitialPath( "Data" );
    ArrayList<ILcdModelDecoder> modelDecoders = createModelDecoders();
    fOpenAction.setModelDecoder( modelDecoders.toArray( new ILcdModelDecoder[ modelDecoders.size() ] ) );
    fOpenAction.addModelProducerListener( new ILcdModelProducerListener() {
      @Override
      public void modelProduced( TLcdModelProducerEvent aEvent ) {
        Collection<ILspLayer> layers = getView().addLayersFor( aEvent.getModel() );
        FitUtil.fitOnLayers(MainPanel.this, layers);
      }
    } );
    fOpenAction.setShortDescription( "Decode a data file and add it as a layer in the view" );

    getToolBars()[ 0 ].addAction( fOpenAction, ToolBar.FILE_GROUP );

    fDropTarget = new DropTarget( getView().getHostComponent(), this );
  }

  private ArrayList<ILcdModelDecoder> createModelDecoders() {
    TLcdOpenFlightModelDecoder fltDecoder = new TLcdOpenFlightModelDecoder();
    fltDecoder.setInputStreamFactory( fInputStreamFactory );
    TLcdOBJModelDecoder objDecoder = new TLcdOBJModelDecoder();
    objDecoder.setInputStreamFactory( fInputStreamFactory );

    ArrayList<ILcdModelDecoder> decoders = new ArrayList<ILcdModelDecoder>();
    decoders.add( new TLcdEarthRepositoryModelDecoder() );
    decoders.add( fltDecoder );
    decoders.add( objDecoder );
    return decoders;
  }

  @Override
  protected void addData() throws IOException {
    super.addData();
    getView().addLayer(fCreateAndEditToolBar.getCreationLayer());
  }

  @Override
  public void dragEnter( DropTargetDragEvent dtde ) {
  }

  @Override
  public void dragOver( DropTargetDragEvent dtde ) {
  }

  @Override
  public void dropActionChanged( DropTargetDragEvent dtde ) {
  }

  @Override
  public void dragExit( DropTargetEvent dte ) {
  }

  @Override
  public void drop( final DropTargetDropEvent dtde ) {
    // Check if the dropped object is a file list
    Transferable tr = dtde.getTransferable();
    DataFlavor[] flavors = tr.getTransferDataFlavors();
    for ( int i = 0; i < flavors.length; i++ ) {
      if ( flavors[ i ].isFlavorJavaFileListType() ) {
        // If so, feed the files into the TLcdOpenAction
        dtde.acceptDrop( DnDConstants.ACTION_COPY_OR_MOVE );

        try {
          final List<File> list = ( List<File> ) tr.getTransferData( flavors[ i ] );
          getView().getServices().getTaskExecutor().execute( new TLspTaskExecutorRunnable( this, new Runnable() {
            @Override
            public void run() {
              for ( int j = 0; j < list.size(); j++ ) {
                fOpenAction.loadFile( list.get( j ).getAbsolutePath() );
              }
              dtde.dropComplete( true );
            }
          }, true ) );
        } catch ( UnsupportedFlavorException e ) {
          e.printStackTrace();
        } catch ( IOException e ) {
          e.printStackTrace();
        }
      }
    }
  }
}
