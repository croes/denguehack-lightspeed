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
package samples.lightspeed.demo.application.data.maritime;

import java.io.IOException;
import java.util.concurrent.ExecutorService;

import com.luciad.io.ILcdInputStreamFactory;
import com.luciad.io.ILcdInputStreamFactoryCapable;
import com.luciad.io.TLcdInputStreamFactory;
import com.luciad.model.ILcd2DBoundsInteractable;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelDecoder;
import com.luciad.model.ILcdModelReference;
import com.luciad.model.ILcdTileDecoder;
import com.luciad.model.TLcd2DBoundsIndexedModel;

import samples.lightspeed.demo.application.data.maritime.ExactAISModelDescriptor.ShipDescriptor;

/**
 * A decoder for ExactEarth AIS data.
 */
public abstract class ExactAISModelDecoder implements ILcdModelDecoder, ILcdTileDecoder, ILcdInputStreamFactoryCapable {

  private ILcdInputStreamFactory fInputStreamFactory = new TLcdInputStreamFactory();

  protected ExactAISModelDecoder() {
  }

  protected ExactAISModelDecoder(ILcdInputStreamFactory aInputStreamFactory) {
    fInputStreamFactory = aInputStreamFactory;
  }

  @Override
  public void setInputStreamFactory(ILcdInputStreamFactory aInputStreamFactory) {
    fInputStreamFactory = aInputStreamFactory;
  }

  public ILcdInputStreamFactory getInputStreamFactory() {
    return fInputStreamFactory;
  }

  /**
   * Streams the data at the specified location.
   * <p/>
   * This method streams the data to the specified callback. Note that this method is synchronous. The callback has
   * received all data upon return.
   *
   * @param aSourceName      the source to stream from
   * @param aCallback        the callback that receives the data
   * @param aExecutorService an optional executor that can be used for concurrent decoding; this does not affect the
   *                         result or its order
   *
   * @throws IOException if an error occurs
   */
  public abstract void stream(String aSourceName, Callback aCallback, ExecutorService aExecutorService) throws IOException;

  @Override
  public ILcdModel decode(String aSourceName) throws IOException {
    final TLcd2DBoundsIndexedModel model = new TLcd2DBoundsIndexedModel();
    final ExactAISModelDescriptor modelDescriptor = new ExactAISModelDescriptor(aSourceName);
    model.setModelDescriptor(modelDescriptor);
    stream(aSourceName, new Callback() {
      @Override
      public void modelReference(ILcdModelReference aModelReference) {
        model.setModelReference(aModelReference);
      }

      @Override
      public void shipDescriptor(ShipDescriptor aShipDescriptor) {
        modelDescriptor.putShipDescriptor(aShipDescriptor);
      }

      @Override
      public void plot(int aMessageId, AISPlot aAISPlot) {
        model.addElement(aAISPlot, ILcdModel.NO_EVENT);
      }

      @Override
      public void timeRange(long aMin, long aMax) {
        modelDescriptor.setMinTime(aMin);
        modelDescriptor.setMaxTime(aMax);
      }
    }, null);
    return model;
  }

  @Override
  public ILcd2DBoundsInteractable decodeTile(String aTileSourceName) throws IOException {
    return (ILcd2DBoundsInteractable) decode(aTileSourceName);
  }

  public interface Callback {
    void modelReference(ILcdModelReference aModelReference) throws IOException;

    void shipDescriptor(ShipDescriptor aShipDescriptor) throws IOException;

    void plot(int aMessageId, AISPlot aAISPlot) throws IOException;

    void timeRange(long aMin, long aMax) throws IOException;
  }
}
