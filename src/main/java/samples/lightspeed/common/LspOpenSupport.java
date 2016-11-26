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
package samples.lightspeed.common;

import static com.luciad.util.TLcdStatusEvent.startIndeterminateProgress;

import java.awt.Container;
import java.awt.EventQueue;
import java.awt.Frame;
import java.util.Collection;
import java.util.Collections;

import javax.swing.JComponent;

import com.luciad.gui.TLcdAWTUtil;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelDecoder;
import com.luciad.model.ILcdModelProducerListener;
import com.luciad.model.TLcdModelProducerEvent;
import com.luciad.util.TLcdStatusEvent;
import com.luciad.util.logging.ILcdLogger;
import com.luciad.util.logging.TLcdLoggerFactory;
import com.luciad.view.lightspeed.ILspAWTView;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.ILspLayerFactory;

import samples.common.formatsupport.GXYOpenSupport;
import samples.common.formatsupport.OpenSupport;
import samples.common.formatsupport.OpenTransferHandler;
import samples.common.serviceregistry.ServiceRegistry;
import samples.lightspeed.fundamentals.step1.Main;

/**
 * Decodes data files and adds them to an ILspView.<p/>
 * Example usages:
 * <pre> <code>
 * // enable drag and drop of data files into a view
 * new LspOpenSupport( view );
 *
 * // programmatically open a data file in a view
 * new LspOpenSupport( view ).openSource( myFile );
 *
 * // create a button with a file chooser dialog
 * new JButton( new TLcdSWAction( new OpenAction( new LspOpenSupport( view ) ) ) );
 *
 * // create a button with a text field dialog asking for a URL
 * new TLcdSWAction( new OpenURLAction( new LspOpenSupport( view ) ) ) );
 *
 * </code> </pre>
 *
 * The class builds on OpenSupport by passing decoded models to the view's composite
 * layer factory.
 * The model decoders can be passed directly or they can be retrieved using the ServiceRegistry look-up mechanism.
 * <p/>
 * For a step-by-step explanation of how to load and visualize models in a view, refer to the {@link Main fundamentals samples}
 * and the developer's guide.
 */
public class LspOpenSupport extends OpenSupport {

  private static final ILcdLogger LOG = TLcdLoggerFactory.getLogger(LspOpenSupport.class.getName());

  private ILspAWTView fView;
  private Iterable<? extends ILcdModelDecoder> fModelDecoders;

  /**
   * Creates an instance for the given view that delegates to the model decoders found in the ServiceRegistry.
   * @see ServiceRegistry
   */
  public LspOpenSupport(ILspAWTView aView) {
    this(aView, ServiceRegistry.getInstance().query(ILcdModelDecoder.class));
    prefetch(fModelDecoders, "model decoders");
  }

  /**
   * Creates an instance for the given view that delegates to the given model decoders.
   */
  public LspOpenSupport(ILspAWTView aView, Iterable<? extends ILcdModelDecoder> aModelDecoders) {
    super(aView.getHostComponent(), aModelDecoders);
    fView = aView;
    fModelDecoders = aModelDecoders;
    addModelProducerListener(new MyModelProducerListener());
    // Configure drag and drop.
    Container overlayComponent = aView.getOverlayComponent();
    if (overlayComponent instanceof JComponent) {
      ((JComponent) overlayComponent).setTransferHandler(new OpenTransferHandler(this));
    }
  }

  /* ----------------------------------- helper classes ---------------------------------- */

  private class MyModelProducerListener implements ILcdModelProducerListener {
    private Frame fFrame;

    @Override
    public void modelProduced(final TLcdModelProducerEvent aEvent) {
      try (TLcdStatusEvent.Progress ignored = startIndeterminateProgress(asListener(), this, "Creating layers.")) {
        ILspLayerFactory layerFactory = fView.getLayerFactory();
        final ILcdModel model = aEvent.getModel();
        final Collection<ILspLayer> layers = layerFactory.canCreateLayers(model) ?
                                             layerFactory.createLayers(model) : Collections.<ILspLayer>emptyList();

        if (noModelReference(getParentComponent(), aEvent.getModel())) {
          return;
        }
        EventQueue.invokeLater(new Runnable() {
          @Override
          public void run() {
            if (layers.isEmpty()) {
              GXYOpenSupport.noLayerFactory(getParentComponent(), model);
            }
            for (ILspLayer layer : layers) {
              fView.addLayer(layer);
            }
            fFrame = TLcdAWTUtil.findParentFrame(fView.getOverlayComponent());
            FitUtil.fitOnLayers(fFrame, fView, true, layers.toArray(new ILspLayer[layers.size()]));
          }
        });
      }
    }
  }

}
