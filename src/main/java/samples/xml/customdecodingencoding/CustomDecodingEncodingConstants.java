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

import javax.xml.namespace.QName;

import com.luciad.format.xml.bind.schema.TLcdXMLSchemaElementIdentifier;
import com.luciad.format.xml.bind.schema.TLcdXMLSchemaTypeIdentifier;

class CustomDecodingEncodingConstants {

  public static final String NAMESPACE_URI = "http://www.luciad.com/samples.xml.customdecodingencoding";

  public static final TLcdXMLSchemaTypeIdentifier EXTENDED_ADDRESS_TYPE =
      TLcdXMLSchemaTypeIdentifier.newGlobalTypeInstance(new QName(NAMESPACE_URI, "ExtendedAddressType"));

  public static final TLcdXMLSchemaTypeIdentifier POINT_TYPE_ID =
      TLcdXMLSchemaTypeIdentifier.newGlobalTypeInstance(new QName(NAMESPACE_URI, "PointType"));

  public static final TLcdXMLSchemaElementIdentifier LOCATION_ELEMENT_ID =
      TLcdXMLSchemaElementIdentifier.newGlobalElementInstance(new QName(NAMESPACE_URI, "location"));

  public static final TLcdXMLSchemaTypeIdentifier COLOR_TYPE_ID =
      TLcdXMLSchemaTypeIdentifier.newGlobalTypeInstance(new QName(NAMESPACE_URI, "ColorType"));

  public static final TLcdXMLSchemaElementIdentifier COLOR_ELEMENT_ID =
      TLcdXMLSchemaElementIdentifier.newInstance(EXTENDED_ADDRESS_TYPE.getTypeName(),
                                                 new QName[]{new QName(NAMESPACE_URI, "color")});

}
