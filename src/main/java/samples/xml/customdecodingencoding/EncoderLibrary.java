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
package samples.xml.customdecodingencoding;

import java.awt.Color;

import com.luciad.datamodel.TLcdDataModel;
import com.luciad.format.xml.bind.schema.TLcdXMLSchemaBasedEncoder;
import com.luciad.format.xml.bind.schema.dataobject.TLcdXMLDataObjectEncoderLibrary;
import com.luciad.shape.shape2D.ILcd2DEditablePoint;

/**
 * Encoder library for the custom decoding encoding data model. This library extends the default 
 * implementation provided by {@link TLcdXMLDataObjectEncoderLibrary}. It registers appropriate
 * marshallers for the Color, Point and ExtendedAddress type.
 */
class EncoderLibrary extends TLcdXMLDataObjectEncoderLibrary {

  public EncoderLibrary(TLcdDataModel aDataModel) {
    super(aDataModel, "custom");
  }

  @Override
  protected void doConfigureEncoder(TLcdXMLSchemaBasedEncoder aEncoder) {
    super.doConfigureEncoder(aEncoder);
    aEncoder.getTypeMarshallerProvider().registerDatatypeMarshaller(CustomDecodingEncodingConstants.COLOR_TYPE_ID,
                                                                    Color.class,
                                                                    new ColorDatatypeAdapter());
    aEncoder.getTypeMarshallerProvider().registerTypeMarshaller(CustomDecodingEncodingConstants.POINT_TYPE_ID,
                                                                ILcd2DEditablePoint.class,
                                                                new PointTypeAdapter());
    aEncoder.getTypeMarshallerProvider().registerTypeMarshaller(CustomDecodingEncodingConstants.EXTENDED_ADDRESS_TYPE,
                                                                ExtendedAddress.class,
                                                                new ExtendedAddressTypeAdapter(aEncoder));
  }

}
