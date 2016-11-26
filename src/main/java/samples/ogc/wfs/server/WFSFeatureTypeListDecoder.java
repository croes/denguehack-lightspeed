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
package samples.ogc.wfs.server;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import com.luciad.datamodel.TLcdDataType;
import com.luciad.io.TLcdIOUtil;
import com.luciad.model.ILcdDataModelDescriptor;
import com.luciad.model.ILcdModel;
import com.luciad.ogc.common.ILcdInitializationConfig;
import com.luciad.ogc.common.ILcdOGCModelProvider;
import com.luciad.ogc.wfs.ILcdWFSFeatureType;
import com.luciad.ogc.wfs.ILcdWFSFeatureTypeList;
import com.luciad.ogc.wfs.ILcdWFSFeatureTypeListDecoder;
import com.luciad.ogc.wfs.TLcdWFSFeatureTypeList;

/**
 * An example <code>ILcdWFSFeatureTypeListDecoder</code> implementation. Loads
 * a list of SHP files from an XML file. The path of this XML file is
 * specified in the servlet configuration, by setting the
 * <code>wfs.featureTypeList.cfg</code> parameter in web.xml.
 */
class WFSFeatureTypeListDecoder implements ILcdWFSFeatureTypeListDecoder {

  private ILcdOGCModelProvider fModelProvider;

  public WFSFeatureTypeListDecoder(ILcdOGCModelProvider aModelProvider) {
    super();
    fModelProvider = aModelProvider;
  }


  public ILcdWFSFeatureTypeList decodeWFSFeatureTypeList(ILcdInitializationConfig aConfig)
      throws IOException {

    // Get the name of the feature type list file from the servlet parameters.
    String featureTypeListSource = aConfig.getParameter("wfs.featureTypeList.cfg");

    // Parse the XML file.
    SAXBuilder saxBuilder = new SAXBuilder();
    Document doc;
    try {
      TLcdIOUtil ioUtil = new TLcdIOUtil();
      ioUtil.setSourceName(featureTypeListSource);
      doc = saxBuilder.build(ioUtil.retrieveInputStream());
    } catch (JDOMException e) {
      throw new IOException("Error parsing " + featureTypeListSource);
    }

    boolean enableLocking = false;
    String lockingParam = aConfig.getParameter("wfs.enableLocking");
    if (lockingParam != null && lockingParam.equals("true")) {
      enableLocking = true;
    }

    boolean enableTransactions = false;
    String transactionsParam = aConfig.getParameter("wfs.enableTransactions");
    if (transactionsParam != null && transactionsParam.equals("true")) {
      enableTransactions = true;
    }
    Element root = doc.getRootElement();

    // Find all <FeatureType> elements and add them to the feature type list.
    TLcdWFSFeatureTypeList list = new TLcdWFSFeatureTypeList();
    List featureTypes = root.getChildren("FeatureType");

    for (int i = 0; i < featureTypes.size(); i++) {
      Element featureType = (Element) featureTypes.get(i);
      String featureTypeSource = featureType.getText();
      final ILcdModel model = fModelProvider.getModel(featureTypeSource, null);

      String featureTypeName = featureType.getAttributeValue("name", model.getModelDescriptor().getDisplayName());
      String featureTypeTitle = featureType.getAttributeValue("title", (String) null);
      String uniqueFeatureTypeName = featureTypeName;
      int index = 1;
      while (list.getFeatureType(uniqueFeatureTypeName) != null) {
        uniqueFeatureTypeName = featureTypeName + index;
        index++;
      }
      featureTypeName = uniqueFeatureTypeName;
      // Replace any spaces in the name with underscores. The feature type name
      // must be valid for use as an XML element name!
      featureTypeName = featureTypeName.replaceAll(" ", "_");

      // Retrieve the TLcdDataType from the model's descriptor, if it is an ILcdDataModelDescriptor
      TLcdDataType dataType = null;
      if (model.getModelDescriptor() instanceof ILcdDataModelDescriptor) {
        //assumes each model contains at most one data type.
        ILcdDataModelDescriptor dataModelDescriptor = (ILcdDataModelDescriptor) model.getModelDescriptor();
        Set<TLcdDataType> modelElementTypes = dataModelDescriptor.getModelElementTypes();
        if (modelElementTypes != null) {
          dataType = (TLcdDataType) modelElementTypes.toArray()[0];
        }
      }
      list.addFeatureType(decodeWFSFeatureType(featureTypeSource, featureTypeName, featureTypeTitle, dataType, enableTransactions, enableLocking));
    }

    return list;
  }

  private ILcdWFSFeatureType decodeWFSFeatureType(String aSource, String aName, String aTitle, TLcdDataType aDataType, boolean aEnableTransactions, boolean aEnableLocking) {

    // Create a new WFSFeatureType to be added to the list.
    WFSFeatureType type = new WFSFeatureType();
    type.setName(aName);
    if (aTitle != null) {
      type.setTitle(aTitle);
    }
    type.setSource(aSource);
    type.setLockingEnabled(aEnableLocking);
    type.setTransactionEnabled(aEnableTransactions);
    // Feature ID retrievers are used to write the "fid" attribute in GML files.
    type.setFeatureIDRetriever(new WFSFeatureIDRetriever());
    type.setDataType(aDataType);
    return type;
  }
}
