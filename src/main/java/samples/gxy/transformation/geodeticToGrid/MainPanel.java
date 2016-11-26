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
package samples.gxy.transformation.geodeticToGrid;

import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;

import javax.swing.JPanel;

import com.luciad.geodesy.ILcdGeodeticDatum;
import com.luciad.geodesy.TLcdGeodeticDatum;
import com.luciad.gui.TLcdSymbol;
import com.luciad.model.TLcdModelDescriptor;
import com.luciad.model.TLcdVectorModel;
import com.luciad.projection.TLcdEquidistantCylindrical;
import com.luciad.reference.ILcdGridReference;
import com.luciad.reference.TLcdGeodeticReference;
import com.luciad.reference.TLcdGridReference;
import com.luciad.shape.shape3D.TLcdLonLatHeightPoint;
import com.luciad.util.ILcdFireEventMode;
import com.luciad.view.gxy.TLcdGXYLayer;
import com.luciad.view.gxy.TLcdGXYShapePainter;
import com.luciad.view.map.TLcdGeodeticPen;

import samples.gxy.common.GXYSample;
import samples.gxy.common.layers.GXYLayerUtil;

/**
 * This sample demonstrates the ability to display a geodetic location which can
 * be expressed based on different ILcdGeodeticDatums and express its coordinates
 * in some commonly used grid reference systems and some lon lat formats.
 * <p/>
 * There are two text fields which contain the geodetic latitude and longitude
 * respectively. The latitude and the longitude are represented in a certain
 * format which can be changed. Values have to be entered in this format too.
 * The geodetic coordinate is with respect to a certain geodetic datum which
 * can be altered.
 * Below are the (x,y) grid coordinates of this geodetic LatLon coordinate
 * according to some grid coordinate systems in use.
 * <p/>
 * Coordinates can be entered in any of the fields. Pressing enter then results
 * in an update of all values in the other fields.
 * Changing the geodetic datum or the UTM zone however will affect only the
 * geodetic coordinate or the UTM coordinate only.
 * Since the geodetic datum of the projection in the map component is kept
 * the same as the geodetic datum of the grid calculator, the icon will move
 * slightly too.
 */
public class MainPanel extends GXYSample {

  private TLcdVectorModel fModel = new TLcdVectorModel();

  @Override
  protected void createGUI() {

    // sets the geodetic datum of the model point (WGS_1984)
    // the model point will connect the map component and the
    // coordinate conversion component
    TLcdGeodeticDatum datum = new TLcdGeodeticDatum();
    fModel.setModelReference(new TLcdGeodeticReference(datum));
    fModel.setModelDescriptor(new TLcdModelDescriptor(
        "Location for which the coordinates are calculated.",     // source name (is used as tooltip text)
        "Location",     // data type
        "Location"      // display name (user)
    ));

    TLcdLonLatHeightPoint location = new TLcdLonLatHeightPoint();
    fModel.addElement(location, ILcdFireEventMode.NO_EVENT);

    super.createGUI();

    // sets the initial projectionReference of the ILcdGXYView
    TLcdGridReference projectionRef = new TLcdGridReference(
        new TLcdGeodeticDatum(),
        new TLcdEquidistantCylindrical(),
        0.0, 0.0, 1.0, 1.0, 0.0
    );
    getView().setXYWorldReference(projectionRef);
  }

  @Override
  protected JPanel createBottomPanel() {
    // initializes the grid calculation component (coordinate conversions)
    // with the model point fModel
    // the model point can be changed by the coordinate conversion component
    // register the map component for changes of the geodetic datum within the
    // grid calculator
    GridCalculation grid_calc = new GridCalculation(fModel);
    grid_calc.addPropertyChangeListener("geodeticDatum", new MyGeodeticDatumPropertyChangeListener());
    fModel.addModelListener(grid_calc);
    return grid_calc;
  }

  @Override
  protected void addData() throws IOException {
    super.addData();

    // Creation of a layer that will draw the model point
    TLcdGXYLayer layer = new TLcdGXYLayer(fModel);
    layer.setSelectable(true);
    layer.setEditable(true);
    layer.setGXYPen(new TLcdGeodeticPen());

    TLcdGXYShapePainter painter = new TLcdGXYShapePainter();
    TLcdSymbol icon = new TLcdSymbol(TLcdSymbol.FILLED_CIRCLE, 9, Color.white, Color.red);
    painter.setIcon(icon);
    painter.setSelectedIcon(icon);
    painter.setAntiAliased(true);

    layer.setGXYPainterProvider(painter);
    layer.setGXYEditorProvider(painter);

    // we intend to move the point, so it is not a background layer.
    GXYLayerUtil.addGXYLayer(getView(), layer, true, false);
  }

  /**
   * Class that listens to a change of geodetic datums.
   * It is used to listen if this is changed within the coordinate conversion
   * component and sets the geodetic datum of the world reference of the map
   * component to the same geodetic datum.
   */
  class MyGeodeticDatumPropertyChangeListener implements PropertyChangeListener {
    @Override
    public void propertyChange(PropertyChangeEvent event) {
      ILcdGeodeticDatum datum = (ILcdGeodeticDatum) event.getNewValue();
      ILcdGridReference oldReference = (ILcdGridReference) getView().getXYWorldReference();
      TLcdGridReference newReference = new TLcdGridReference();
      newReference.setGeodeticDatum(datum);
      newReference.setProjection(oldReference.getProjection());
      newReference.setFalseEasting(oldReference.getFalseEasting());
      newReference.setFalseNorthing(oldReference.getFalseNorthing());
      newReference.setScale(oldReference.getScale());
      newReference.setUnitOfMeasure(oldReference.getUnitOfMeasure());
      newReference.setRotation(oldReference.getRotation());
      getView().setXYWorldReference(newReference);
    }
  }

  public static void main(final String[] aArgs) {
    startSample(MainPanel.class, "Grid systems");
  }
}
