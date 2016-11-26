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
package samples.lucy.search.service.location;

import java.util.HashSet;
import java.util.Set;

import com.luciad.datamodel.TLcdCoreDataTypes;
import com.luciad.datamodel.TLcdDataModel;
import com.luciad.datamodel.TLcdDataModelBuilder;
import com.luciad.datamodel.TLcdDataProperty;
import com.luciad.datamodel.TLcdDataType;
import com.luciad.datamodel.TLcdDataTypeBuilder;
import com.luciad.lucy.util.language.TLcyLang;
import com.luciad.model.TLcdDataModelDescriptor;
import com.luciad.shape.ILcdShape;
import com.luciad.util.TLcdHasGeometryAnnotation;

/**
 * Model descriptor for the layer that will contain the search results.
 */
final class SearchResultModelDescriptor extends TLcdDataModelDescriptor {
  private static final String SEARCH_ADD_ON = "searchAddOn";

  private static final TLcdDataModel DATA_MODEL;
  static final TLcdDataProperty DESCRIPTION_PROPERTY;
  static final TLcdDataProperty GEOMETRY_PROPERTY;

  static final TLcdDataType SEARCH_RESULT_TYPE;


  static {
    TLcdDataModelBuilder builder = new TLcdDataModelBuilder("samples.lucy.search.service.location.SearchResultModelDescriptor");
    TLcdDataTypeBuilder searchResult = builder.typeBuilder("SearchResult");
    searchResult.addProperty("Description", TLcdCoreDataTypes.STRING_TYPE);

    TLcdDataTypeBuilder geometryType = builder.typeBuilder("GeometryType");
    geometryType.primitive(true).instanceClass(ILcdShape.class);
    searchResult.addProperty("Geometry", geometryType);

    DATA_MODEL = builder.createDataModel();
    SEARCH_RESULT_TYPE = DATA_MODEL.getDeclaredType("SearchResult");
    DESCRIPTION_PROPERTY = DATA_MODEL.getDeclaredType("SearchResult").getProperty("Description");
    GEOMETRY_PROPERTY = DATA_MODEL.getDeclaredType("SearchResult").getProperty("Geometry");
    SEARCH_RESULT_TYPE.addAnnotation(new TLcdHasGeometryAnnotation(GEOMETRY_PROPERTY));
  }

  SearchResultModelDescriptor() {
    super(SEARCH_ADD_ON,
          SEARCH_ADD_ON,
          TLcyLang.getString("Search results"),
          DATA_MODEL,
          convertIntoSet(SEARCH_RESULT_TYPE),
          convertIntoSet(SEARCH_RESULT_TYPE)
    );
  }

  private static Set<TLcdDataType> convertIntoSet(TLcdDataType aDataType) {
    Set<TLcdDataType> result = new HashSet<>();
    result.add(aDataType);
    return result;
  }
}
