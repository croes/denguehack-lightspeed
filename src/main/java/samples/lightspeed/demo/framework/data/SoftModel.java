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
package samples.lightspeed.demo.framework.data;

import java.lang.ref.SoftReference;
import java.util.Enumeration;

import com.luciad.model.ALcdModel;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelDecoder;
import com.luciad.model.ILcdModelDescriptor;
import com.luciad.model.ILcdModelReference;
import com.luciad.util.enumeration.TLcdEmptyEnumeration;
import com.luciad.util.logging.ILcdLogger;
import com.luciad.util.logging.TLcdLoggerFactory;

/**
 * A soft non-bounds indexed model, similar to {@code ILcd2DBoundsIndexedModel}.
 */
class SoftModel extends ALcdModel {

  private static final ILcdLogger sLogger = TLcdLoggerFactory.getLogger(SoftModel.class);

  // Fields that are initialized at construction time.
  private String fSource;
  private ILcdModelDecoder fModelDecoder;

  // Fields for (hard) caching some of the model's properties.
  private ILcdModelReference fModelReference;
  private ILcdModelDescriptor fModelDescriptor;

  // Field for (soft) caching the model.
  // It should always reference an ILcd2DBoundsIndexedModel.
  private SoftReference<ILcdModel> fModelSoftReference;

  public SoftModel(String aSource, ILcdModelDecoder aModelDecoder) {
    fSource = aSource;
    fModelDecoder = aModelDecoder;
  }

  public Enumeration elements() {
    // We'll have to get the model.
    ILcdModel model = getModel();

    // Delegate to the model, if we got it.
    return model == null ?
           TLcdEmptyEnumeration.getInstance() :
           model.elements();
  }

  public String getSource() {
    return fSource;
  }

  public ILcdModelDecoder getModelDecoder() {
    return fModelDecoder;
  }

  /**
   * Sets the {@code ILcdModelReference} of this {@code ILcdModel}. Optional property used for
   * optimized usage of this {@code TLcdSoft2DBoundsIndexedModel}.
   *
   * @param aAnticipatedModelReference the anticipated model reference of the model.
   */
  public void setAnticipatedModelReference(ILcdModelReference aAnticipatedModelReference) {
    fModelReference = aAnticipatedModelReference;
  }

  /**
   * Sets the {@code ILcdModelDescriptor} of this {@code ILcdModel}. Optional property used for
   * optimized usage of this {@code TLcdSoft2DBoundsIndexedModel}.
   *
   * @param aAnticipatedModelDescriptor the anticipated model descriptor of the model.
   */
  public void setAnticipatedModelDescriptor(ILcdModelDescriptor aAnticipatedModelDescriptor) {
    fModelDescriptor = aAnticipatedModelDescriptor;
  }

  // Implementations for ILcdModel.

  public ILcdModelReference getModelReference() {
    // See if we already have a cached model reference.
    // Otherwise get the model, and the model reference as a side effect.
    if (fModelReference == null) {
      getModel();
    }

    return fModelReference;
  }

  public ILcdModelDescriptor getModelDescriptor() {
    // See if we already have a cached model descriptor.
    // Otherwise get the model, and the model descriptor as a side effect.
    if (fModelDescriptor == null) {
      getModel();
    }

    return fModelDescriptor;
  }

  /**
   * Retrieves and caches the model. As a side effect, put the ILcdModelDescriptor in
   * fModelDescriptor
   */
  private synchronized ILcdModel getModel() {
    ILcdModel model = null;

    if ((fModelSoftReference == null ||
         (model = fModelSoftReference.get()) == null) &&
        fModelDecoder != null) {

      try {
        if (sLogger.isTraceEnabled()) {
          sLogger.trace("Trying to load lazy model [" + getSource() + "]...", this);
        }

        synchronized (fModelDecoder) {
          // Use the model decoder to obtain the model.
          model = fModelDecoder.decode(getSource());
        }

        // Hard cache some of the model's properties.
        fModelReference = model.getModelReference();
        fModelDescriptor = model.getModelDescriptor();

        // Soft cache the model itself.
        fModelSoftReference = new SoftReference(model);

        if (sLogger.isTraceEnabled()) {
          sLogger.trace("Lazy model [" + getSource() + "] loaded", this);
        }
      } catch (Exception ex) {
        // IOException, ClassCastException
        // For some reason we couldn't get a proper model.
        // Don't try loading it ever again.
        fSource = null;
        fModelDecoder = null;
        fModelSoftReference = null;

        sLogger.error(ex.getMessage(), this);
      }
    } else {
      if (sLogger.isTraceEnabled()) {
        sLogger.trace("Lazy model [" + getSource() + "] was still in memory", this);
      }
    }

    return model;
  }
}
