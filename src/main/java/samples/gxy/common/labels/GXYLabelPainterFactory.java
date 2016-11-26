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
package samples.gxy.common.labels;

import java.awt.Font;
import java.util.Set;

import com.luciad.datamodel.TLcdCoreDataTypes;
import com.luciad.datamodel.TLcdDataProperty;
import com.luciad.datamodel.TLcdDataType;
import samples.common.MapColors;
import com.luciad.model.ILcdDataModelDescriptor;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelDescriptor;
import com.luciad.view.gxy.TLcdGXYDataObjectLabelPainter;
import com.luciad.view.gxy.TLcdGXYLabelPainter;

public class GXYLabelPainterFactory {

  public static TLcdGXYLabelPainter createGXYLabelPainter(ILcdModel aModel, boolean aWithPin) {
    TLcdGXYLabelPainter labelPainter;
    if (aModel.getModelDescriptor() instanceof ILcdDataModelDescriptor) {
      ILcdDataModelDescriptor dataModelDescriptor = (ILcdDataModelDescriptor) aModel.getModelDescriptor();
      TLcdGXYDataObjectLabelPainter dataObjectLabelPainter = new TLcdGXYDataObjectLabelPainter();
      TLcdDataProperty labelProperty = getDataModelLabelProperty(dataModelDescriptor);
      if (labelProperty != null) {
        dataObjectLabelPainter.setExpressions(labelProperty.getName());
      }
      labelPainter = dataObjectLabelPainter;
    } else {
      labelPainter = new TLcdGXYLabelPainter();
    }
    Font font = new Font("Dialog", Font.PLAIN, 12);
    labelPainter.setFont(font);
    labelPainter.setAntiAliased(true);
    labelPainter.setHaloEnabled(true);
    labelPainter.setForeground(MapColors.LABEL);
    labelPainter.setSelectionColor(MapColors.SELECTION);
    labelPainter.setHaloColor(MapColors.LABEL_HALO);
    labelPainter.setWithPin(aWithPin);
    labelPainter.setSelectedPinColor(MapColors.SELECTION);
    if (!aWithPin) {
      // If we're not displaying a pin, restrict the possible locations to make it more clear
      // to which object object the label belongs. More advanced labeling behavior can be achieved
      // by plugging in an ILcdGXYLabelingAlgorithm.
      labelPainter.setPositionList(new int[]{TLcdGXYLabelPainter.NORTH});
    }
    return labelPainter;
  }

  /**
   * Looks for a suitable property to use for labels:
   * <ol>
   *   <li>The first String property that equals "name"</li>
   *   <li>The first String property that contains "name"</li>
   *   <li>Otherwise, the first String property</li>
   *   <li>Otherwise, the first ID property</li>
   *   <li>Otherwise, the first property</li>
   * </ol>
   */
  public static TLcdDataProperty getDataModelLabelProperty(ILcdModelDescriptor aDescriptor) {
    if (aDescriptor instanceof ILcdDataModelDescriptor) {
      ILcdDataModelDescriptor dataModelDescriptor = (ILcdDataModelDescriptor) aDescriptor;
      Set<TLcdDataType> types = dataModelDescriptor.getModelElementTypes();
      // model element types is allowed to return null
      if (types == null) {
        return null;
      }
      return getDataTypesLabelProperty(types);
    }
    return null;
  }

  public static TLcdDataProperty getDataTypesLabelProperty(Set<TLcdDataType> aTypes) {
    // "name" String properties
    for (TLcdDataType dataType : aTypes) {
      for (TLcdDataProperty property : dataType.getProperties()) {
        // a "name" string property is a good candidate for labeling
        if (property.getName().toLowerCase().equals("name") &&
            TLcdCoreDataTypes.STRING_TYPE.equals(property.getType())) {
          return property;
        }
      }
    }

    // containing "name" String properties
    for (TLcdDataType dataType : aTypes) {
      for (TLcdDataProperty property : dataType.getProperties()) {
        // containing "name" is a good candidate for labeling too
        if (property.getName().toLowerCase().contains("name") &&
            TLcdCoreDataTypes.STRING_TYPE.equals(property.getType())) {
          return property;
        }
      }
    }

    // any String property
    for (TLcdDataType dataType : aTypes) {
      for (TLcdDataProperty property : dataType.getProperties()) {
        if (TLcdCoreDataTypes.STRING_TYPE.equals(property.getType())) {
          return property;
        }
      }
    }

    for (TLcdDataType dataType : aTypes) {
      for (TLcdDataProperty property : dataType.getProperties()) {
        if (property.getName().toLowerCase().contains("id")) {
          return property;
        }
      }
    }

    for (TLcdDataType dataType : aTypes) {
      if (!dataType.getProperties().isEmpty()) {
        return dataType.getProperties().get(0);
      }
    }
    return null;
  }
}
