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
package samples.lightspeed.demo.application.data.airplots;

import java.io.IOException;

import com.luciad.model.ILcdModel;
import com.luciad.util.logging.ILcdLogger;
import com.luciad.util.logging.TLcdLoggerFactory;

import samples.lightspeed.demo.framework.data.AbstractModelFactory;

/**
 * Model factory for generated track models.
 */
public class AirPlotsModelFactory extends AbstractModelFactory {

  private static final ILcdLogger sLogger = TLcdLoggerFactory.getLogger(AirPlotsModelFactory.class);

  public AirPlotsModelFactory(String aType) {
    super(aType);
  }

  @Override
  public ILcdModel createModel(String aSource) throws IOException {
    long startTime = System.currentTimeMillis();

    //TLcdSHPModelDecoder modelDecoder = new TLcdSHPModelDecoder();
    AirPlotsBinModelDecoder modelDecoder = new AirPlotsBinModelDecoder();
    ILcdModel model = modelDecoder.decode(aSource);

    // Not necessary, AirplotsBinModelDecoder already accumulates annotations while decoding
    // TLcdDataModel dataModel = ( (ILcdDataModelDescriptor) model.getModelDescriptor() ).getDataModel();
    // TLcdDataType dataType = dataModel.getDeclaredTypes().iterator().next();
    // MainPanel.annotateDataModel(model, dataType);

    if (sLogger.isDebugEnabled()) {
      sLogger.debug("Created model for " + aSource + " in " + (System.currentTimeMillis() - startTime) + " ms");
    }

    return model;
  }
}
