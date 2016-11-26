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
package samples.metadata.gazetteer;

import com.luciad.datamodel.ILcdDataObject;
import com.luciad.datamodel.TLcdDataProperty;
import com.luciad.datamodel.TLcdDataType;
import com.luciad.datamodel.expression.TLcdDataObjectExpressionLanguage;
import com.luciad.format.gml32.model.TLcdGML32AbstractGeometry;
import com.luciad.format.metadata.model.extent.TLcdISO19115BoundingPolygon;
import com.luciad.format.metadata.model.extent.TLcdISO19115Extent;
import com.luciad.format.metadata.model.extent.TLcdISO19115GeographicBoundingBox;
import com.luciad.format.metadata.model.extent.TLcdISO19115GeographicExtent;
import com.luciad.format.metadata.model.identification.TLcdISO19115DataIdentification;
import com.luciad.format.metadata.model.metadataentityset.TLcdISO19115Metadata;
import com.luciad.shape.ILcdBounded;
import com.luciad.shape.ILcdBounds;
import com.luciad.shape.shape2D.ILcd2DEditableBounds;

import java.util.List;

/**
 * A <code>ILcdBounded</code> whose limits are retrieved from a metadata object.
 */
class MetadataBounded implements ILcdBounded, ILcdDataObject {

  private TLcdISO19115Metadata fMetadata;
  private ILcdBounds fCachedBounds;

  public MetadataBounded( TLcdISO19115Metadata aMetadata ) {
    fMetadata = aMetadata;
    // we cache the bounds, so we don't have to look for them over and over.
    fCachedBounds = findBounds();
  }

  public ILcdBounds getBounds() {
    return fCachedBounds;
  }

  public TLcdISO19115Metadata getMetadata() {
    return fMetadata;
  }

  private ILcdBounds findBounds() {
    TLcdISO19115DataIdentification data_identification_info = (TLcdISO19115DataIdentification) fMetadata.getIdentificationInfo().get( 0 );
    if ( data_identification_info.getExtent().isEmpty() ) {
      throw new NullPointerException( "No extent defined for data: " + fMetadata.getFileIdentifier() );
    }
    int extent_index = 0;
    while ( extent_index < data_identification_info.getExtent().size() ) {
      TLcdISO19115Extent extent = data_identification_info.getExtent().get( extent_index );
      if ( !extent.getGeographicElement().isEmpty() ) {
        TLcdISO19115GeographicExtent geographic_extent = extent.getGeographicElement().get( extent_index );
        if ( geographic_extent instanceof TLcdISO19115BoundingPolygon ) {
          TLcdISO19115BoundingPolygon bounding_polygon = (TLcdISO19115BoundingPolygon) geographic_extent;
          if ( bounding_polygon.getPolygon().size() == 1 ) {
            return  bounding_polygon.getPolygon().get( 0 ).getBounds();
          } else {
            ILcd2DEditableBounds bounds =  bounding_polygon.getPolygon().get( 0 ).getBounds().cloneAs2DEditableBounds();
            for ( TLcdGML32AbstractGeometry p : bounding_polygon.getPolygon() ) {
              bounds.setTo2DUnion(  p.getBounds() );
            }
            return bounds;
          }
        } else if ( geographic_extent instanceof TLcdISO19115GeographicBoundingBox ) {
          return (TLcdISO19115GeographicBoundingBox) geographic_extent;
        }
      }
      extent_index++;
    }
    return null;
  }

  public String getDisplayName() {
    List<Object> label_objects = (List<Object>) new TLcdDataObjectExpressionLanguage().evaluate( "identificationInfo.AbstractMD_Identification.citation.CI_Citation.title.CharacterString.value", getMetadata() );
    if ( label_objects == null || label_objects.isEmpty() ) {
      return "Unknown";
    }
    else {
      return label_objects.get(0).toString();
    }
  }

  public String toString() {
    return getDisplayName();
  }

  public TLcdDataType getDataType() {
    return fMetadata.getDataType();
  }

  public Object getValue( TLcdDataProperty aProperty ) {
    return fMetadata.getValue( aProperty );
  }

  public Object getValue( String aPropertyName ) {
    return fMetadata.getValue( aPropertyName );
  }

  public void setValue( TLcdDataProperty aProperty, Object aValue ) {
    fMetadata.setValue( aProperty, aValue );
  }

  public void setValue( String aPropertyName, Object aValue ) {
    fMetadata.setValue( aPropertyName, aValue );
  }

  public boolean hasValue( TLcdDataProperty aProperty ) {
    return fMetadata.hasValue( aProperty );
  }

  public boolean hasValue( String aPropertyName ) {
    return fMetadata.hasValue( aPropertyName );
  }  
  
}
