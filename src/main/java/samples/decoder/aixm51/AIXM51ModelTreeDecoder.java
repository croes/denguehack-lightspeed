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
package samples.decoder.aixm51;

import com.luciad.datamodel.TLcdDataModel;
import com.luciad.datamodel.TLcdDataType;
import com.luciad.format.aixm51.model.TLcdAIXM51ModelDescriptor;
import com.luciad.format.aixm51.model.abstractfeature.TLcdAIXM51AbstractAIXMMessage;
import com.luciad.format.aixm51.util.TLcdAIXM51MessageUtil;
import com.luciad.format.aixm51.xml.TLcdAIXM51ModelDecoder;
import com.luciad.format.xml.bind.schema.ALcdXMLModelDecoder;
import com.luciad.io.ILcdInputStreamFactory;
import com.luciad.io.ILcdInputStreamFactoryCapable;
import com.luciad.model.ILcdDataModelDescriptor;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelDecoder;
import com.luciad.model.ILcdModelTreeNode;
import com.luciad.model.TLcdModelTreeNode;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * A simple wrapper around a {@link TLcdAIXM51ModelDecoder} that splits the resulting model into one
 * model per feature type, and returns an {@link ILcdModelTreeNode}.
 */
public class AIXM51ModelTreeDecoder implements ILcdModelDecoder, ILcdInputStreamFactoryCapable {
  private ALcdXMLModelDecoder fDelegateModelDecoder;

  public AIXM51ModelTreeDecoder() {
    fDelegateModelDecoder = new TLcdAIXM51ModelDecoder();
  }

  @Override
  public String getDisplayName() {
    return fDelegateModelDecoder.getDisplayName();
  }

  @Override
  public boolean canDecodeSource( String aSourceName ) {
    return fDelegateModelDecoder.canDecodeSource( aSourceName );
  }

  @Override
  public ILcdInputStreamFactory getInputStreamFactory() {
    return fDelegateModelDecoder.getInputStreamFactory();
  }

  @Override
  public void setInputStreamFactory( ILcdInputStreamFactory aInputStreamFactory ) {
    fDelegateModelDecoder.setInputStreamFactory( aInputStreamFactory );
  }

  @Override
  public ILcdModel decode( String source ) throws IOException {
    ILcdModel returnValue;
    ILcdModel model = fDelegateModelDecoder.decode( source );
    if ( model instanceof TLcdAIXM51AbstractAIXMMessage ) {
      //split the model into one model per feature type
      final List<? extends ILcdModel> aixmMessages = TLcdAIXM51MessageUtil.separateFeatureTypes( ( TLcdAIXM51AbstractAIXMMessage ) model );

      //create a model tree node
      final TLcdModelTreeNode modelTreeNode = new TLcdModelTreeNode();
      TLcdDataModel dataModel = ( ( ILcdDataModelDescriptor ) model.getModelDescriptor() ).getDataModel();
      modelTreeNode.setModelDescriptor( new TLcdAIXM51ModelDescriptor( model.getModelDescriptor().getSourceName(),
                                                                       model.getModelDescriptor().getDisplayName(),
                                                                       dataModel,
                                                                       Collections.<TLcdDataType>emptySet() ) );
      modelTreeNode.setModelReference( model.getModelReference() );
      for ( ILcdModel aixmMessage : aixmMessages ) {
        //add listener to detect changes
        modelTreeNode.addModel( aixmMessage );
      }
      returnValue = modelTreeNode;
    }
    else {
      returnValue = model;
    }

    return returnValue;
  }
}
