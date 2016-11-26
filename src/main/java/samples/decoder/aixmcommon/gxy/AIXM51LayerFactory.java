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
package samples.decoder.aixmcommon.gxy;

import java.util.Set;

import com.luciad.datamodel.TLcdDataType;
import com.luciad.format.aixm51.model.TLcdAIXM51DataTypes;
import com.luciad.format.aixm51.model.TLcdAIXM51ModelDescriptor;
import com.luciad.format.aixm51.view.gxy.TLcdAIXM51GXYPainterEditorProvider;
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
 * A layer factory for AIXM 5.1 models.
 * All models created by the AIXM 5.1 decoder are supported.
 */
@LcdService
public class AIXM51LayerFactory implements ILcdGXYLayerFactory {
  private static final int DEFAULT_ICON_SIZE = 17;

  public ILcdGXYLayer createGXYLayer(ILcdModel aModel) {
    if (!(aModel.getModelDescriptor() instanceof TLcdAIXM51ModelDescriptor )) {
      return null;
    }

    TLcdAIXM51ModelDescriptor descriptor = (TLcdAIXM51ModelDescriptor) aModel.getModelDescriptor();

    ILcdIcon icon = getIcon(descriptor);
    TLcdGXYLayer layer = new TLcdGXYLayer();
    layer.setModel(aModel);
    layer.setIcon(icon);
    layer.setLabel(getLabel(descriptor));
    layer.setGXYPen(new TLcdGeodeticPen(false));

    TLcdAIXM51GXYPainterEditorProvider painterEditorProvider = new TLcdAIXM51GXYPainterEditorProvider();
    layer.setGXYPainterProvider(painterEditorProvider);
    layer.setGXYEditorProvider(painterEditorProvider);
    layer.setGXYLabelPainterProvider(painterEditorProvider);
    layer.setGXYLabelEditorProvider( painterEditorProvider );

    layer.setLabelsEditable(true);

    return layer;
  }

  /**
   * Defers an <code>ILcdIcon</code> from the given
   * <code>TLcdAIXM51ModelDescriptor</code>.
   * 
   * @param aDescriptor Model descriptor from which the icon will be deferred.
   * @return An icon related to the type of features described by the
   *         {@link TLcdAIXM51ModelDescriptor},
   *         or <code>null</code> if no suitable icon is available.
   */
  private ILcdIcon getIcon(TLcdAIXM51ModelDescriptor aDescriptor) {
    Set<TLcdDataType> featureTypes = aDescriptor.getModelElementTypes();
    ILcdIcon icon = null;
    if (featureTypes == null || featureTypes.size() > 1 || featureTypes.size() == 0) {
      // no specific icon
    }
    else {
      TLcdDataType featureType = featureTypes.iterator().next();
      if (TLcdAIXM51DataTypes.AirspaceType.equals( featureType )) {
        icon = new TLcdVectorIcon("icons/airspace.ims", DEFAULT_ICON_SIZE, DEFAULT_ICON_SIZE );
      }
      else if (TLcdAIXM51DataTypes.InstrumentApproachProcedureType.equals( featureType )) {
        icon = new TLcdImageIcon("icons/route.gif");
      }
      else if (TLcdAIXM51DataTypes.StandardInstrumentDepartureType.equals( featureType )) {
        icon = new TLcdImageIcon("icons/route.gif");
      }
      else if (TLcdAIXM51DataTypes.NavaidType.equals( featureType )) {
        icon = new TLcdVectorIcon("icons/vor.ims", DEFAULT_ICON_SIZE, DEFAULT_ICON_SIZE );
      }
      else if ( TLcdAIXM51DataTypes.NavaidType.equals( featureType ) ) {
        icon = new TLcdVectorIcon( "icons/dme.ims", DEFAULT_ICON_SIZE, DEFAULT_ICON_SIZE );
      } else if ( TLcdAIXM51DataTypes.VORType.equals( featureType )) {
        icon = new TLcdVectorIcon( "icons/vor.ims", DEFAULT_ICON_SIZE, DEFAULT_ICON_SIZE );
      } else if ( TLcdAIXM51DataTypes.NDBType.equals( featureType ) ) {
        icon = new TLcdVectorIcon( "icons/ndb.ims", DEFAULT_ICON_SIZE, DEFAULT_ICON_SIZE );
      } else if ( TLcdAIXM51DataTypes.TACANType.equals( featureType ) ) {
        icon = new TLcdVectorIcon( "icons/tacan.ims", DEFAULT_ICON_SIZE, DEFAULT_ICON_SIZE );
      } else if ( TLcdAIXM51DataTypes.LocalizerType.equals( featureType ) ) {
        icon = new TLcdVectorIcon( "icons/localizer.ims", DEFAULT_ICON_SIZE, DEFAULT_ICON_SIZE );
      }
      else if (TLcdAIXM51DataTypes.MarkerBeaconType.equals( featureType )) {
        icon = new TLcdVectorIcon( "icons/marker.ims", DEFAULT_ICON_SIZE, DEFAULT_ICON_SIZE );
      }
      else if (TLcdAIXM51DataTypes.AirportHeliportType.equals( featureType )) {
        icon = new TLcdVectorIcon("icons/airport_civil.ims", DEFAULT_ICON_SIZE, DEFAULT_ICON_SIZE );
      }
      else if (TLcdAIXM51DataTypes.AirportHeliportUsageType.equals( featureType )) {
        // no specific icon
      }
      else if (TLcdAIXM51DataTypes.RunwayElementType.equals( featureType )) {
        icon = new TLcdImageIcon("icons/runway.gif");
      }
      else if (TLcdAIXM51DataTypes.TaxiwayElementType.equals( featureType )) {
        icon = new TLcdImageIcon("icons/runway.gif");
      }
      else if (TLcdAIXM51DataTypes.VerticalStructureType.equals( featureType )) {
        icon = new TLcdVectorIcon("icons/obstacle.ims", DEFAULT_ICON_SIZE, DEFAULT_ICON_SIZE );
      }
      else if (TLcdAIXM51DataTypes.DesignatedPointType.equals( featureType )) {
        icon = new TLcdVectorIcon("icons/vor.ims", DEFAULT_ICON_SIZE, DEFAULT_ICON_SIZE );
      }
      else if (TLcdAIXM51DataTypes.RouteSegmentType.equals( featureType )) {
        icon = new TLcdImageIcon("icons/route.gif");
      }
      else if (TLcdAIXM51DataTypes.ApronElementType.equals( featureType )) {
        icon = new TLcdImageIcon("icons/runway.gif");
      }
      else if (TLcdAIXM51DataTypes.GuidanceLineType.equals( featureType )) {
        icon = new TLcdImageIcon("icons/runway.gif");
      }
      else if (TLcdAIXM51DataTypes.RunwayCentrelinePointType.equals( featureType )) {
        icon = new TLcdImageIcon("icons/runway.gif");
      }
      else if (TLcdAIXM51DataTypes.UnitType.equals( featureType )) {
        // no specific icon
      }
    }
    return icon;
  }

  /**
   * Defers a label from a given AIXM 5.1 <code>ILcdModelDescriptor</code>.
   *
   * @param aModelDescriptor model descriptor for which a label should be returned.
   * @return A label for the given model.
   */
  private String getLabel( TLcdAIXM51ModelDescriptor aModelDescriptor) {
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
