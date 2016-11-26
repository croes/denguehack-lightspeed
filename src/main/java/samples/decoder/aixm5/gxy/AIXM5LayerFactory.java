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
package samples.decoder.aixm5.gxy;

import java.util.Collection;

import com.luciad.datamodel.TLcdDataType;
import com.luciad.format.aixm5.model.TLcdAIXM5DataTypes;
import com.luciad.format.aixm5.model.TLcdAIXM5ModelDescriptor;
import com.luciad.format.aixm5.view.gxy.TLcdAIXM5GXYPainterEditorProvider;
import com.luciad.format.aixm51.model.TLcdAIXM51DataTypes;
import com.luciad.gui.ILcdIcon;
import com.luciad.gui.TLcdImageIcon;
import com.luciad.gui.TLcdVectorIcon;
import com.luciad.model.ILcdModel;
import com.luciad.util.service.LcdService;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.ILcdGXYLayerFactory;
import com.luciad.view.gxy.TLcdGXYLayer;
import com.luciad.view.map.TLcdGeodeticPen;

/**
 * A layer factory for AIXM 5.0 models. 
 * All models created by the AIXM 5.0 decoder are supported.
 */
@LcdService
public class AIXM5LayerFactory implements ILcdGXYLayerFactory {
  private static final int DEFAULT_ICON_SIZE = 17;

  public ILcdGXYLayer createGXYLayer(ILcdModel aModel) {
    TLcdGXYLayer layer;
    if (!(aModel.getModelDescriptor() instanceof TLcdAIXM5ModelDescriptor)) {
      return null;
    }
    TLcdAIXM5ModelDescriptor descriptor = (TLcdAIXM5ModelDescriptor) aModel.getModelDescriptor();
    ILcdIcon icon = getIcon(descriptor);
    layer = new TLcdGXYLayer();
    layer.setModel(aModel);
    layer.setIcon(icon);
    layer.setLabel(getLabel(descriptor));
    layer.setGXYPen(new TLcdGeodeticPen(false));

    TLcdAIXM5GXYPainterEditorProvider painterEditorProvider = new TLcdAIXM5GXYPainterEditorProvider();
    layer.setGXYPainterProvider(painterEditorProvider);
    layer.setGXYEditorProvider(painterEditorProvider);
    layer.setGXYLabelPainterProvider(painterEditorProvider);
    layer.setGXYLabelEditorProvider(painterEditorProvider);

    layer.setLabelsEditable(true);

    return layer;
  }

  /**
   * Defers an <code>ILcdIcon</code> from the given <code>TLcdAIXM5ModelDescriptor</code>.
   *
   * @param aDescriptor Model descriptor from which the icon will be deferred.
   * @return An icon related to the type of features described by the {@link TLcdAIXM5ModelDescriptor},
   *         or <code>null</code> if no suitable icon is available.
   */
  private ILcdIcon getIcon(TLcdAIXM5ModelDescriptor aDescriptor) {
    Collection<TLcdDataType> featureTypes = aDescriptor.getModelElementTypes();
    if (featureTypes == null || featureTypes.size() > 1 || featureTypes.size() == 0) {
      return null;
    }
    TLcdDataType featureType = featureTypes.iterator().next();
    if ( TLcdAIXM5DataTypes.AirspaceType.equals( featureType ) ) {
      return new TLcdVectorIcon("icons/airspace.ims", DEFAULT_ICON_SIZE, DEFAULT_ICON_SIZE );
    }
    else if ( TLcdAIXM5DataTypes.InstrumentApproachProcedureType.equals( featureType ) ) {
      return new TLcdImageIcon("icons/route.gif");
    }
    else if ( TLcdAIXM5DataTypes.StandardInstrumentDepartureType.equals( featureType ) ) {
      return new TLcdImageIcon("icons/route.gif");
    }
    else if ( TLcdAIXM5DataTypes.AirspaceUsageType.equals( featureType ) ) {
      return new TLcdVectorIcon("icons/airspace.ims", DEFAULT_ICON_SIZE, DEFAULT_ICON_SIZE );
    }
    else if ( TLcdAIXM5DataTypes.NavaidType.equals( featureType ) ) {
      return new TLcdVectorIcon("icons/vor.ims", DEFAULT_ICON_SIZE, DEFAULT_ICON_SIZE );
    }
    else if ( TLcdAIXM5DataTypes.NavaidType.equals( featureType ) ) {
      return new TLcdVectorIcon( "icons/dme.ims", DEFAULT_ICON_SIZE, DEFAULT_ICON_SIZE );
    }
    else if ( TLcdAIXM5DataTypes.VORType.equals( featureType ) ) {
      return new TLcdVectorIcon( "icons/vor.ims", DEFAULT_ICON_SIZE, DEFAULT_ICON_SIZE );
    }
    else if ( TLcdAIXM5DataTypes.NDBType.equals( featureType ) ) {
      return new TLcdVectorIcon( "icons/ndb.ims", DEFAULT_ICON_SIZE, DEFAULT_ICON_SIZE );
    }
    else if ( TLcdAIXM5DataTypes.TACANType.equals( featureType ) ) {
      return new TLcdVectorIcon( "icons/tacan.ims", DEFAULT_ICON_SIZE, DEFAULT_ICON_SIZE );
    }
    else if ( TLcdAIXM5DataTypes.LocalizerType.equals( featureType ) ) {
      return new TLcdVectorIcon( "icons/localizer.ims", DEFAULT_ICON_SIZE, DEFAULT_ICON_SIZE );
    }
    else if ( TLcdAIXM51DataTypes.MarkerBeaconType.equals( featureType ) ) {
      return new TLcdVectorIcon( "icons/marker.ims", DEFAULT_ICON_SIZE, DEFAULT_ICON_SIZE );
    }
    else if ( TLcdAIXM5DataTypes.AirportHeliportType.equals( featureType ) ) {
      return new TLcdVectorIcon("icons/airport_civil.ims", DEFAULT_ICON_SIZE, DEFAULT_ICON_SIZE );
    }
    else if ( TLcdAIXM5DataTypes.AirportHeliportUsageType.equals( featureType ) ) {
      // no specific icon
    }
    else if ( TLcdAIXM5DataTypes.RunwayElementType.equals( featureType ) ) {
      return new TLcdImageIcon("icons/runway.gif");
    }
    else if ( TLcdAIXM5DataTypes.TaxiwayElementType.equals( featureType ) ) {
      return new TLcdImageIcon("icons/runway.gif");
    }
    else if ( TLcdAIXM5DataTypes.VerticalStructureType.equals( featureType ) ) {
      return new TLcdVectorIcon("icons/obstacle.ims", DEFAULT_ICON_SIZE, DEFAULT_ICON_SIZE );
    }
    else if ( TLcdAIXM5DataTypes.DesignatedPointType.equals( featureType ) ) {
      return new TLcdVectorIcon("icons/vor.ims", DEFAULT_ICON_SIZE, DEFAULT_ICON_SIZE );
    }
    else if ( TLcdAIXM5DataTypes.RouteSegmentType.equals( featureType ) ) {
      return new TLcdImageIcon("icons/route.gif");
    }
    else if ( TLcdAIXM5DataTypes.ApronElementType.equals( featureType ) ) {
      return new TLcdImageIcon("icons/runway.gif");
    }
    else if ( TLcdAIXM5DataTypes.GuidanceLineType.equals( featureType ) ) {
      return new TLcdImageIcon("icons/runway.gif");
    }
    else if ( TLcdAIXM5DataTypes.RunwayCentrelinePointType.equals( featureType ) ) {
      return new TLcdImageIcon("icons/runway.gif");
    }
    else if ( TLcdAIXM5DataTypes.UnitType.equals( featureType ) ) {
      // no specific icon
    }
    return null;
  }

  /**
   * Defers a label from a given AIXM 5.0 <code>ILcdModelDescriptor</code>.
   *
   * @param aModelDescriptor model descriptor for which a label should be returned.
   * @return A label for the given model.
   */
  private String getLabel( TLcdAIXM5ModelDescriptor aModelDescriptor ) {
    if ( aModelDescriptor != null ) {
      if ( aModelDescriptor.isSnapshotModel() ) {
        return aModelDescriptor.getDisplayName() + " (snapshot)";
      }
      else {
        return aModelDescriptor.getDisplayName();
      }
    }
    return null;
  }
}
