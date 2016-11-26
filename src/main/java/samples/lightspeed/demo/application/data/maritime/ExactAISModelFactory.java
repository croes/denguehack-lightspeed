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

import com.luciad.io.ILcdInputStreamFactory;
import com.luciad.io.ILcdInputStreamFactoryCapable;
import com.luciad.model.ILcd2DBoundsIndexedModel;
import com.luciad.model.ILcdModel;
import com.luciad.shape.shape2D.TLcdLonLatBounds;
import com.luciad.util.ILcdFunction;

import samples.lightspeed.demo.framework.data.AbstractModelFactory;

/**
 * Model factory for the Exact AIS theme.
 *
 * @since 2012.1
 */
public class ExactAISModelFactory extends AbstractModelFactory implements ILcdInputStreamFactoryCapable {

  private final ExactAISCSVBinModelDecoder fModelDecoder = new ExactAISCSVBinModelDecoder();
  private final ExactAISCSVModelDecoder fCSVModelDecoder = new ExactAISCSVModelDecoder();
  private final ExactAISEarthModelDecoder fEarthModelDecoder = new ExactAISEarthModelDecoder();

  public ExactAISModelFactory(String aType) {
    super(aType);
  }

  @Override
  public void setInputStreamFactory(ILcdInputStreamFactory aInputStreamFactory) {
    fModelDecoder.setInputStreamFactory(aInputStreamFactory);
    fCSVModelDecoder.setInputStreamFactory(aInputStreamFactory);
//    fEarthModelDecoder.setInputStreamFactory(aInputStreamFactory);
  }

  @Override
  public ILcdInputStreamFactory getInputStreamFactory() {
    return fModelDecoder.getInputStreamFactory();
  }

  @Override
  public ILcdModel createModel(String aSourceName) throws IOException {
    ILcdModel model;
    if (fModelDecoder.canDecodeSource(aSourceName)) {
      model = fModelDecoder.decode(aSourceName);
    } else if (fCSVModelDecoder.canDecodeSource(aSourceName)) {
      model = fCSVModelDecoder.decode(aSourceName);
    } else if (fEarthModelDecoder.canDecodeSource(aSourceName)) {
      model = fEarthModelDecoder.decode(aSourceName);
    } else {
      throw new RuntimeException("Cannot decode " + getType() + " model from [" + aSourceName + "]");
    }
    if (model instanceof ILcd2DBoundsIndexedModel) {
      // TLcd2DBoundsIndexedModel creates its spatial index lazily. This call forces that
      // to happen here, so the cost is paid during demo startup rather than when you first
      // activate the theme.
      ((ILcd2DBoundsIndexedModel) model).applyOnInteract2DBounds(
          new TLcdLonLatBounds(-1, -1, 2, 2),
          false,
          new ILcdFunction() {
            @Override
            public boolean applyOn(Object aObject) throws IllegalArgumentException {
              return false;
            }
          },
          0, 0
      );
    }
    return model;
  }
}
