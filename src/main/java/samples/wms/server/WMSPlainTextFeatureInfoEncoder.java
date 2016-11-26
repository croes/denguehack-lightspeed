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
package samples.wms.server;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.ServletException;

import com.luciad.datamodel.ILcdDataObject;
import com.luciad.datamodel.TLcdDataProperty;
import com.luciad.view.gxy.ILcdGXYLayerSubsetList;
import com.luciad.view.gxy.ILcdGXYView;
import com.luciad.wms.server.ILcdWMSGetFeatureInfoRequestEncoder;
import com.luciad.wms.server.TLcdWMSRequestContext;

import samples.gxy.common.labels.GXYLabelPainterFactory;

/**
 * Sample implementation of ILcdWMSGetFeatureInfoRequestEncoder that encodes all objects as a plain text String.
 */
public class WMSPlainTextFeatureInfoEncoder implements ILcdWMSGetFeatureInfoRequestEncoder {

  private static final String CONTENT_TYPE = "text/plain";

  @Override
  public String getContentType() {
    return CONTENT_TYPE;
  }

  @Override
  public void encode(ILcdGXYLayerSubsetList aSelectionSublist,
                     OutputStream aOutputStream,
                     ILcdGXYView aGXYView,
                     TLcdWMSRequestContext aWMSRequestContext) throws ServletException {

    List<String> strings = new ArrayList<>();
    for (Enumeration elements = aSelectionSublist.elements(); elements.hasMoreElements(); ) {
      Object element = elements.nextElement();
      if (element instanceof ILcdDataObject) {
        ILcdDataObject dataObject = (ILcdDataObject) element;
        TLcdDataProperty property = GXYLabelPainterFactory.getDataTypesLabelProperty(Collections.singleton(dataObject.getDataType()));
        if (property != null) {
          Object value = dataObject.getValue(property);
          if (value != null) {
            strings.add(value.toString());
          }
        } else {
          strings.add(element.toString());
        }
      } else {
        strings.add(element.toString());
      }
    }
    if (strings.isEmpty()) {
      return;
    }

    String result = Arrays.toString(strings.toArray());
    result = result.substring(1, result.length() - 1);
    try {
      aOutputStream.write(result.getBytes(StandardCharsets.ISO_8859_1));
      aOutputStream.flush();
    } catch (IOException e) {
      throw new ServletException(e);
    }
  }
}
