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
package samples.decoder.aixm51.transformation;

import com.luciad.format.aixm.decoder.TLcdAIXMAerodromeHandler;
import com.luciad.format.aixm.decoder.TLcdAIXMAirspaceHandler;
import com.luciad.format.aixm.decoder.TLcdAIXMModelDecoder;
import com.luciad.io.ILcdInputStreamFactory;
import com.luciad.io.ILcdInputStreamFactoryCapable;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelDecoder;
import com.luciad.util.service.LcdService;

import java.io.IOException;

/**
 * <p>An {@code ILcdModelDecoder} which can read an AIXM4.5 model (by delegating to an
 * {@code TLcdAIXMModelDecoder}) and converts the AIXM4.5 model to an AIXM5.1 model
 * (using the {@code AIXM45To51Transformation}).</p>
 *
 * <p>The model decoder only supports the types which are supported by the {@code AIXM45To51Transformation}.</p>
 *
 */
@LcdService
public class AIXM45To51ModelDecoder implements ILcdModelDecoder, ILcdInputStreamFactoryCapable{
  private final AIXM45To51Transformation fAIXM45To51Transformation = new AIXM45To51Transformation();
  private final TLcdAIXMModelDecoder fModelDecoder;

  public AIXM45To51ModelDecoder() {
    fModelDecoder = new TLcdAIXMModelDecoder();
    //the transformation only supports airspaces and aerodromes
    fModelDecoder.addHandlerForTypeToBeDecoded( new TLcdAIXMAirspaceHandler() );
    fModelDecoder.addHandlerForTypeToBeDecoded( new TLcdAIXMAerodromeHandler() );
  }

  @Override
  public String getDisplayName() {
    return "AIXM4.5 to AIXM5.1 model decoder";
  }

  @Override
  public boolean canDecodeSource( String aSourceName ) {
    return fModelDecoder.canDecodeSource( aSourceName );
  }

  @Override
  public ILcdModel decode( String aSourceName ) throws IOException {
    ILcdModel aixmModel = fModelDecoder.decode( aSourceName );
    return fAIXM45To51Transformation.transformToAIXM51( aixmModel );
  }

  @Override
  public void setInputStreamFactory( ILcdInputStreamFactory aInputStreamFactory ) {
    fModelDecoder.setInputStreamFactory( aInputStreamFactory );
  }

  @Override
  public ILcdInputStreamFactory getInputStreamFactory() {
    return fModelDecoder.getInputStreamFactory();
  }
}
