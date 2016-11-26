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
package samples.common.formatsupport;

import java.awt.Component;

import javax.swing.JComponent;
import javax.swing.JOptionPane;

import com.luciad.gui.TLcdAWTUtil;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelDecoder;
import com.luciad.model.ILcdModelProducerListener;
import com.luciad.model.TLcdModelProducerEvent;
import com.luciad.util.TLcdStatusEvent;
import com.luciad.util.logging.ILcdLogger;
import com.luciad.util.logging.TLcdLoggerFactory;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.ILcdGXYLayerFactory;
import com.luciad.view.gxy.ILcdGXYView;
import com.luciad.view.gxy.TLcdCompositeGXYLayerFactory;

import samples.common.serviceregistry.ServiceRegistry;
import samples.gxy.common.layers.GXYLayerUtil;
import samples.gxy.concurrent.painting.AsynchronousLayerFactory;
import samples.gxy.fundamentals.step1.Main;

/**
 * Decodes data files and adds them to an ILcdGXYView.<p/>
 * Example usages:
 * <pre><code>
 * // enable drag and drop of data files into a view
 * new GXYOpenSupport( view );
 *
 * // programmatically open a data file in a view
 * new GXYOpenSupport( view ).openSource( myFile );
 *
 * // create a button with a file chooser dialog
 * new JButton( new TLcdSWAction( new OpenAction( new GXYOpenSupport( view ) ) ) );
 *
 * // create a button with a text field dialog asking for a URL
 * new TLcdSWAction( new OpenURLAction( new GXYOpenSupport( view ) ) ) );
 *
 * </code></pre>
 *
 * The class builds on OpenSupport by passing decoded models to a composite
 * ILcdGXYLayerFactory and makes them paint asynchronously.
 * To set up asynchronous painting, create a paint queue manager:
 * <code><pre>new TLcdGXYAsynchronousPaintQueueManager().setGXYView(view);</pre></code>
 * The model decoders and layer factories can be passed directly or they can be retrieved
 * using the ServiceRegistry look-up mechanism.
 * <p/>
 * For a step-by-step explanation of how to load and visualize models in a view, refer to the {@link Main fundamentals samples}
 * and the developer's guide.
 */
public class GXYOpenSupport extends OpenSupport {
  private static final ILcdLogger LOG = TLcdLoggerFactory.getLogger(GXYOpenSupport.class.getName());

  private Iterable<? extends ILcdModelDecoder> fModelDecoders;
  private Iterable<? extends ILcdGXYLayerFactory> fLayerFactories;
  private ILcdGXYView fView;

  /**
   * Creates an instance for the given view that delegates to the model decoders and layer factories
   * found in the ServiceRegistry.
   * @see ServiceRegistry
   */
  public GXYOpenSupport(ILcdGXYView aView) {
    this(aView,
         ServiceRegistry.getInstance().query(ILcdModelDecoder.class),
         ServiceRegistry.getInstance().query(ILcdGXYLayerFactory.class));
    fView = aView;
    prefetch(fModelDecoders, "model decoders");
    prefetch(fLayerFactories, null);
  }

  /**
   * Creates an instance for the given view that delegates to the given model decoders and layer factories.
   */
  public GXYOpenSupport(ILcdGXYView aView,
                        Iterable<? extends ILcdModelDecoder> aModelDecoders,
                        Iterable<? extends ILcdGXYLayerFactory> aLayerFactories) {
    super((Component) aView, aModelDecoders);
    fModelDecoders = aModelDecoders;
    fLayerFactories = aLayerFactories;
    addModelProducerListener(new ModelProducerListener(aView, aLayerFactories));
    // Configure drag and drop.
    ((JComponent) aView).setTransferHandler(new OpenTransferHandler(this));
  }

  public ILcdGXYView getGXYView() {
    return fView;
  }

  /**
   * Called if no layer factory could be found to visualize the given model in the view.
   * @param aModel the model for which no layer factory could be found.
   */
  public static void noLayerFactory(final Component aComponent, final ILcdModel aModel) {
    LOG.warn("Could not find a layer factory for \"" + aModel.getModelDescriptor().getDisplayName() + "\"");
    TLcdAWTUtil.invokeLater(new Runnable() {
      @Override
      public void run() {
        JOptionPane.showMessageDialog(
            aComponent,
            "Could not find a layer factory for \"" + aModel.getModelDescriptor().getDisplayName() + "\".\n" +
            "The data was decoded successfully but could not be visualized.\n" +
            "If you encounter this message after compiling the samples yourself,\n" +
            "make sure to enable annotation processing first.\n" +
            "For more information, refer to the developer's guide or your Java SDK documentation.",
            "Cannot visualize decoded data",
            JOptionPane.WARNING_MESSAGE);
      }
    });
  }

  /* ----------------------------------- helper classes ---------------------------------- */

  private class ModelProducerListener implements ILcdModelProducerListener {
    private final ILcdGXYLayerFactory fLayerFactory;
    private final ILcdGXYView fView;

    public ModelProducerListener(ILcdGXYView aView, Iterable<? extends ILcdGXYLayerFactory> aLayerFactories) {
      fView = aView;
      fLayerFactory = new TLcdCompositeGXYLayerFactory(aLayerFactories);
    }

    @Override
    public void modelProduced(TLcdModelProducerEvent aEvent) {
      try (TLcdStatusEvent.Progress ignored = TLcdStatusEvent.startIndeterminateProgress(asListener(), this, "Creating layers.")) {
        if (noModelReference(getParentComponent(), aEvent.getModel())) {
          return;
        }
        ILcdGXYLayer layer = fLayerFactory.createGXYLayer(aEvent.getModel());
        if (layer == null) {
          noLayerFactory(getParentComponent(), aEvent.getModel());
        } else {
          ILcdGXYLayer asynchronousLayer = AsynchronousLayerFactory.createAsynchronousLayer(layer);
          GXYLayerUtil.addGXYLayer(fView, asynchronousLayer);
          if (asynchronousLayer.isVisible()) {
            GXYLayerUtil.fitGXYLayer(fView, asynchronousLayer);
          }
        }
      }
    }

  }
}
