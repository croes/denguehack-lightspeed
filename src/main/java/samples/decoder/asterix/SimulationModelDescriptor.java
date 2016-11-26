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
package samples.decoder.asterix;

import java.util.HashSet;
import java.util.Set;

import com.luciad.datamodel.TLcdDataModel;
import com.luciad.datamodel.TLcdDataProperty;
import com.luciad.datamodel.TLcdDataType;
import com.luciad.datamodel.TLcdUnknownDataTypes;
import com.luciad.model.ILcdDataModelDescriptor;
import com.luciad.model.ILcdModelDescriptor;

/**
 * Model descriptor used to identify the track models.
 */
public class SimulationModelDescriptor implements ILcdModelDescriptor, ILcdDataModelDescriptor {
  private TLcdDataType fDataType;
  private final String fSourceName;
  private final String fTypeName;
  private final String fDisplayName;

  /**
   * Creates a new instance of this model descriptor.
   *
   * @param aSourceName         The source name. This name should point to the source from which the
   *                            trajectories were decoded.
   * @param aTypeName           The type name. This name should indicate what kind of objects will be
   *                            contained in the model.
   * @param aDisplayName        The display name. This name could for instance be used in the label of the
   *                            layer.
   * @param aDataType           The data type that is used by all elements of this model
   */
  public SimulationModelDescriptor(
      String aSourceName, String aTypeName, String aDisplayName,
      TLcdDataType aDataType) {

    fSourceName = aSourceName;
    fTypeName = aTypeName;
    fDisplayName = aDisplayName;
    fDataType = aDataType;
  }

  public String getSourceName() {
    return fSourceName;
  }

  public String getTypeName() {
    return fTypeName;
  }

  public String getDisplayName() {
    return fDisplayName;
  }

  public TLcdDataModel getDataModel() {
    if (fDataType != null) {
      return fDataType.getDataModel();
    } else {
      return TLcdUnknownDataTypes.getDataModel();
    }
  }

  public Set<TLcdDataType> getModelElementTypes() {
    HashSet<TLcdDataType> dataTypeHashSet = new HashSet<TLcdDataType>();
    if (fDataType != null) {
      dataTypeHashSet.add(fDataType);
    } else {
      dataTypeHashSet.add(TLcdUnknownDataTypes.UNKNOWN_TYPE);
    }
    return dataTypeHashSet;
  }

  public Set<TLcdDataType> getModelTypes() {
    if (fDataType != null) {
      return getModelTypes(fDataType);
    } else {
      return getModelTypes(TLcdUnknownDataTypes.UNKNOWN_TYPE);
    }
  }

  /**
   * Recursively gets all data types for a given root data type.
   * @param aRootType a root <code>TLcdDataType</code>
   * @return a set of data types that was found recursively in the given
   * data type.
   */
  private static Set<TLcdDataType> getModelTypes(TLcdDataType aRootType) {
    Set<TLcdDataType> result = new HashSet<TLcdDataType>();
    addTypes(result, aRootType);
    return result;
  }

  /**
   * Adds all data declared data types from a root data type into a set.
   * @param aResult The set in which to add the data type
   * @param aRootType The root type which will be iterated recursively
   */
  private static void addTypes(Set<TLcdDataType> aResult, TLcdDataType aRootType) {
    if (aResult.add(aRootType)) {
      for (TLcdDataProperty p : aRootType.getProperties()) {
        addTypes(aResult, p.getType());
      }
    }
  }
}
