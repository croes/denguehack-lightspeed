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
import java.awt.EventQueue;
import java.io.File;
import java.io.InterruptedIOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import com.luciad.gui.TLcdAWTUtil;
import com.luciad.io.ILcdInputStreamFactory;
import com.luciad.io.ILcdInputStreamFactoryCapable;
import com.luciad.io.TLcdIOUtil;
import com.luciad.io.TLcdInputStreamFactory;
import com.luciad.io.TLcdStatusInputStreamFactory;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelDecoder;
import com.luciad.model.ILcdModelProducer;
import com.luciad.model.ILcdModelProducerListener;
import com.luciad.model.ILcdModelTreeNode;
import com.luciad.model.TLcdCompositeModelDecoder;
import com.luciad.model.TLcdModelProducerEvent;
import com.luciad.util.ILcdStatusListener;
import com.luciad.util.ILcdStatusSource;
import com.luciad.util.TLcdStatusEvent;
import com.luciad.util.TLcdStatusEventSupport;
import com.luciad.util.logging.ILcdLogger;
import com.luciad.util.logging.TLcdLoggerFactory;

import samples.common.serviceregistry.ServiceRegistry;

/**
 * Customizable GUI support for decoding data files based on a service registry or a list of model decoders.
 * It features a progress bar, a model decoder selection dialog and an error dialog.
 * <p/>
 * To do something with the decoded models either override the {@link #modelDecoded}
 * method or add an ILcdModelProducerListener.
 * <p/>
 * The composite model decoder can be populated by making use of the ServiceRegistry look-up mechanism.
 * <p/>
 * All protected methods are called on a background thread.
 *
 * @see ILcdModelDecoder
 * @see ServiceRegistry
 */
public class OpenSupport implements ILcdModelProducer, ILcdStatusSource {

  private static final ILcdLogger LOG = TLcdLoggerFactory.getLogger(OpenSupport.class.getName());

  private final CopyOnWriteArrayList<ILcdModelProducerListener> fModelProducerListeners = new CopyOnWriteArrayList<ILcdModelProducerListener>();
  private final TLcdCompositeModelDecoder fModelDecoders;
  private final Component fParentComponent;
  private final Decoder fDecoder = new Decoder();
  private final TLcdStatusEventSupport fStatusEventSupport = new TLcdStatusEventSupport();

  public OpenSupport(Component aParentComponent) {
    this(
        aParentComponent,
        ServiceRegistry.getInstance().query(ILcdModelDecoder.class)
    );
    prefetch(fModelDecoders.getModelDecoders(), "model decoders");
  }

  public OpenSupport(Component aParentComponent, Iterable<? extends ILcdModelDecoder> aModelDecoders) {
    fParentComponent = aParentComponent;
    fModelDecoders = new TLcdCompositeModelDecoder(aModelDecoders);
  }

  /**
   * Called if no georeference could be found for the given model.
   * @param aModel the model without a georeference
   */
  public static boolean noModelReference(final Component aComponent, final ILcdModel aModel) {
    boolean isEmptyModelTreeNode = aModel instanceof ILcdModelTreeNode && ((ILcdModelTreeNode) aModel).isEmpty();
    if (!isEmptyModelTreeNode && aModel.getModelReference() == null) {
      LOG.warn("Could not georeference \"" + aModel.getModelDescriptor().getDisplayName() + "\"");
      TLcdAWTUtil.invokeLater(new Runnable() {
        @Override
        public void run() {
          JOptionPane.showMessageDialog(
              aComponent,
              "Could not find a georeference for  \"" + aModel.getModelDescriptor().getDisplayName() + "\".\n" +
              "The data was decoded successfully but its location in the view cannot be established.\n" +
              "Please make sure that the original file is accompanied by a reference file.",
              "Cannot visualize decoded data",
              JOptionPane.WARNING_MESSAGE);
        }
      });
      return true;
    }

    return false;
  }

  public void openSource(String aSource) {
    openSource(aSource, null);
  }

  public void openSource(final String aSource, final FileFilter aFileFilter) {
    if (aSource == null) {
      throw new NullPointerException("Cannot open null source");
    }

    fDecoder.decodeSource(aSource, aFileFilter);
  }

  public List<FileFilter> getFileFilters() {
    ArrayList<DecoderFileFilter> decoderFilters = new ArrayList<DecoderFileFilter>();
    for (ILcdModelDecoder decoder : fModelDecoders.getModelDecoders()) {
      decoderFilters.add(new DecoderFileFilter(decoder));
    }
    Collections.sort(decoderFilters, new Comparator<DecoderFileFilter>() {
      public int compare(DecoderFileFilter o1, DecoderFileFilter o2) {
        if (o1 == o2) {
          return 0;
        }
        return o1.getDescription().compareTo(o2.getDescription());
      }
    });

    ArrayList<FileFilter> filters = new ArrayList<FileFilter>();
    filters.add(new CompositeFileFilter("All supported files", decoderFilters));
    filters.addAll(decoderFilters);

    return filters;
  }

  public ILcdModelDecoder getModelDecoder(FileFilter aFilter) {
    return aFilter instanceof DecoderFileFilter ? ((DecoderFileFilter) aFilter).getModelDecoder() : null;
  }

  public Component getParentComponent() {
    return fParentComponent;
  }

  public static ILcdStatusSource retrieveStatusSource(ILcdModelDecoder aModelDecoder) {
    ILcdStatusSource aStatusSource = null;
    if (aModelDecoder instanceof ILcdStatusSource) {
      aStatusSource = (ILcdStatusSource) aModelDecoder;
    } else if (aModelDecoder instanceof ILcdInputStreamFactoryCapable) {
      aStatusSource = retrieveStatusInputStreamFactory((ILcdInputStreamFactoryCapable) aModelDecoder);
    }
    return aStatusSource;
  }

  public static ILcdStatusSource retrieveStatusInputStreamFactory(ILcdInputStreamFactoryCapable aInputStreamFactoryCapable) {
    ILcdInputStreamFactory factory = aInputStreamFactoryCapable.getInputStreamFactory();
    if (!(factory instanceof ILcdStatusSource)) {
      factory = new TLcdStatusInputStreamFactory(factory == null ? new TLcdInputStreamFactory() : factory);
      aInputStreamFactoryCapable.setInputStreamFactory(factory);
    }
    return ((ILcdStatusSource) factory);
  }

  // Implementations for ILcdModelProducer

  public void addModelProducerListener(ILcdModelProducerListener aModelProducerListener) {
    fModelProducerListeners.add(aModelProducerListener);
  }

  public void removeModelProducerListener(ILcdModelProducerListener aModelProducerListener) {
    fModelProducerListeners.remove(aModelProducerListener);
  }

  // Overridable methods

  /**
   * Called to choose one of the given model decoders.
   * @param aSource            the source for which to select the correct model decoder
   * @param aCandidateDecoders the candidate model decoders
   * @return the chosen model decoder
   */
  protected ILcdModelDecoder chooseDecoder(final String aSource, final List<ILcdModelDecoder> aCandidateDecoders) {
    final ILcdModelDecoder decoder[] = {null};

    TLcdAWTUtil.invokeAndWait(new Runnable() {
      @Override
      public void run() {
        HashMap<String, ILcdModelDecoder> map = new HashMap<String, ILcdModelDecoder>();
        for (ILcdModelDecoder decoder : aCandidateDecoders) {
          map.put(decoder.getDisplayName(), decoder);
        }
        Object o = JOptionPane.showInputDialog(
            fParentComponent,
            "Select file type",
            "Opening " + TLcdIOUtil.getFileName(aSource),
            JOptionPane.QUESTION_MESSAGE,
            null,
            map.keySet().toArray(),
            aCandidateDecoders.get(0));

        if (o != null) {
          decoder[0] = map.get(o);
        }
      }
    });

    return decoder[0];
  }

  /**
   * Is called before determining the correct decoder.
   *
   * @param aSource the file that is being decoded.
   */
  protected void prepareLoading(String aSource) {
  }

  /**
   * Is called just before trying to open a file.
   *
   * @param aSource       the file that is being decoded.
   * @param aModelDecoder the decoder assigned to decode the source.
   */
  protected void startLoading(String aSource, ILcdModelDecoder aModelDecoder) {
    ILcdStatusSource statusSource = retrieveStatusSource(aModelDecoder);
    if (statusSource != null) {
      statusSource.addStatusListener(asListener());
    }
  }

  /**
   * Called right after the model is decoded.
   *
   * @param aSource the decoded source
   * @param aModel  the resulting model
   */
  protected void modelDecoded(String aSource, ILcdModel aModel) {
  }

  /**
   * Called after the model is either successfully decoded, or after canceling, or after an error occurred.
   * By default, removes the progress monitor installed in startLoading if the model decoder is an ILcdStatusSource.
   *
   * @param aSource       the file that is being decoded.
   * @param aModelDecoder the decoder assigned to decode the source.
   */
  protected void loadingTerminated(String aSource, ILcdModelDecoder aModelDecoder) {
    ILcdStatusSource statusSource = retrieveStatusSource(aModelDecoder);
    if (statusSource != null) {
      statusSource.removeStatusListener(asListener());
    }
  }

  /**
   * Called after the whole process, including sending events, has finished.
   * @param aSource       the file that was being decoded.
   * @param aModelDecoder the decoder assigned to decode the source.
   */
  protected void stopLoading(String aSource, ILcdModelDecoder aModelDecoder) {
  }

  /**
   * Called if no decoder could be found for the given source.
   *
   * @param aSource the source for which no decoder could be found
   */
  protected void decoderMissing(final String aSource) {
    EventQueue.invokeLater(new Runnable() {
      @Override
      public void run() {
        JOptionPane.showMessageDialog(
            fParentComponent,
            "Could not decode " + aSource + ".\nNo suitable decoder could be found.",
            "Could not decode " + TLcdIOUtil.getFileName(aSource),
            JOptionPane.INFORMATION_MESSAGE,
            null
        );
      }
    });
  }

  /**
   * Called if an exception occurred during decoding.
   *
   * @param e the exception
   * @param aSource the source for which an exception occurred
   */
  protected void decodeException(final Throwable e, final String aSource) {
    EventQueue.invokeLater(new Runnable() {
      @Override
      public void run() {
        JOptionPane.showMessageDialog(null, "Could not decode " + TLcdIOUtil.getFileName(aSource) + ":\n" +
                                            (null == e.getMessage() ? "an unknown error occurred" : e.getLocalizedMessage()) +
                                            "\n\nSee the error log for more information.");
      }
    });
    TLcdLoggerFactory.getLogger("com.luciad").warn("Could not decode file [" + aSource + "]", e);
  }

  // helper methods

  private ILcdModelDecoder findModelDecoder(String aSource, FileFilter aFileFilter) {
    TLcdStatusEvent.Progress progress = TLcdStatusEvent.startIndeterminateProgress(asListener(), this, "Detecting file format.");
    final ILcdModelDecoder decoder;
    if (aFileFilter instanceof DecoderFileFilter) {
      decoder = ((DecoderFileFilter) aFileFilter).getModelDecoder();
    } else if (aFileFilter == null || aFileFilter instanceof CompositeFileFilter) {
      List<ILcdModelDecoder> decoders = new ArrayList<ILcdModelDecoder>();
      HashSet<String> displayNames = new HashSet<String>();

      for (ILcdModelDecoder decoder1 : fModelDecoders.findModelDecoders(aSource)) {
        if (decoder1 != null && !displayNames.contains(decoder1.getDisplayName())) {
          decoders.add(decoder1);
          // Don't present the user twice with the same display name.
          // We rely on the model decoder sort order instead.
          displayNames.add(decoder1.getDisplayName());
        }
      }
      ILcdModelDecoder result = null;
      if (decoders.size() > 1) {
        result = chooseDecoder(aSource, decoders);
      } else if (decoders.size() > 0) {
        result = decoders.get(0);
      }
      decoder = result;
      if (decoder == null) {
        progress.end("No decoder found.");
      } else {
        progress.end("File format: " + decoder.getDisplayName());
      }
    } else {
      decoder = null;
      progress.end("Unsupported file.");
    }
    return decoder;
  }

  protected ILcdStatusListener asListener() {
    return fStatusEventSupport.asListener();
  }

  // Iterates over the given iterable. Increases start-up time but reduces delay for subsequent accesses.
  protected void prefetch(Iterable<?> aIterable, final String aServiceNameForWarning) {
    int count = 0;
    for (Object ignored : aIterable) {
      count++;
    }
    if (count == 0 && aServiceNameForWarning != null) {
      TLcdAWTUtil.invokeLater(new Runnable() {
        @Override
        public void run() {
          JOptionPane.showMessageDialog(
              fParentComponent,
              "The sample could not discover any " + aServiceNameForWarning + ".\n" +
              "If you want to make use of the service registry,\n" +
              "enable annotation processing in your IDE/compiler.\n\n" +
              "For more information, refer to the developer's guide.\n",
              "Services not found",
              JOptionPane.ERROR_MESSAGE);
        }
      });
    }
  }

  @Override
  public void addStatusListener(ILcdStatusListener aListener) {
    fStatusEventSupport.addStatusListener(aListener);
  }

  @Override
  public void removeStatusListener(ILcdStatusListener aListener) {
    fStatusEventSupport.removeStatusListener(aListener);
  }

  /**
   * A custom file filter that delegates to an ILcdModelDecoder.
   */
  private static class DecoderFileFilter extends FileFilter {

    private ILcdModelDecoder fModelDecoder;

    public DecoderFileFilter(ILcdModelDecoder aModelDecoder) {
      fModelDecoder = aModelDecoder;
    }

    public String getDescription() {
      return fModelDecoder.getDisplayName();
    }

    public boolean accept(File aFile) {
      return fModelDecoder.canDecodeSource(aFile.getPath());
    }

    public ILcdModelDecoder getModelDecoder() {
      return fModelDecoder;
    }
  }

  /**
   * A custom file filter that combines a number of other file filters.
   */
  private static class CompositeFileFilter extends FileFilter {
    private final List<? extends FileFilter> fFileFilters;
    private final String fDescription;

    public CompositeFileFilter(String aDescription, List<? extends FileFilter> aFileFilters) {
      fFileFilters = aFileFilters;
      fDescription = aDescription;
    }

    @Override
    public boolean accept(File f) {
      for (FileFilter fileFilter : fFileFilters) {
        if (fileFilter.accept(f)) {
          return true;
        }
      }
      return false;
    }

    @Override
    public String getDescription() {
      return fDescription;
    }
  }

  private class Decoder {

    private final Object fLock = new ReentrantReadWriteLock();
    private final List<String> fSourcesToDecode = new ArrayList<String>();
    private final List<FileFilter> fFileFilters = new ArrayList<FileFilter>();
    private final DecoderRunnable fDecoderRunnable;
    private boolean fDecoding = false;

    private Decoder() {
      fDecoderRunnable = new DecoderRunnable();
    }

    public void decodeSource(String aSource, FileFilter aFileFilter) {
      synchronized (fLock) {
        fSourcesToDecode.add(aSource);
        fFileFilters.add(aFileFilter);
        if (!fDecoding) {
          fDecoding = true;
          Thread decoderThread = new Thread(fDecoderRunnable, "samples-open-action");
          decoderThread.setPriority(Thread.MIN_PRIORITY);
          decoderThread.start();
        }
      }
    }

    private class DecoderRunnable implements Runnable {

      public void run() {
        while (true) {
          String sourceToDecode;
          FileFilter fileFilter;
          synchronized (fLock) {
            if (!fSourcesToDecode.isEmpty() && !Thread.currentThread().isInterrupted()) {
              sourceToDecode = fSourcesToDecode.remove(0);
              fileFilter = fFileFilters.remove(0);
            } else {
              fDecoding = false;
              return;
            }
          }

          if (sourceToDecode != null) {
            decodeSource(sourceToDecode, fileFilter);
          }
        }
      }

      private void decodeSource(String aSource, FileFilter aFileFilter) {
        prepareLoading(aSource);
        ILcdModelDecoder decoder = findModelDecoder(aSource, aFileFilter);
        if (decoder == null) {
          decoderMissing(aSource);
          return;
        }

        startLoading(aSource, decoder);
        try {
          ILcdModel model = null;
          try {
            model = decoder.decode(aSource);
            if (Thread.currentThread().isInterrupted()) {
              return;
            }
          } catch (InterruptedIOException ignored) {
            // Cancellations should not be reported.
          } catch (Throwable e) {
            decodeException(e, aSource);
          } finally {
            loadingTerminated(aSource, decoder);
          }
          if (model != null) {
            modelDecoded(aSource, model);
            TLcdModelProducerEvent event = new TLcdModelProducerEvent(
                OpenSupport.this, TLcdModelProducerEvent.MODEL_PRODUCED, model);
            for (ILcdModelProducerListener listener : fModelProducerListeners) {
              listener.modelProduced(event);
            }
          }
        } finally {
          stopLoading(aSource, decoder);
        }
      }
    }
  }
}
