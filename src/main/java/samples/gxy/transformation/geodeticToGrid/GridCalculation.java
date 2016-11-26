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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.text.NumberFormat;
import java.util.Enumeration;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.luciad.geodesy.ILcdGeodeticDatum;
import com.luciad.geodesy.TLcdGeodeticDatum;
import com.luciad.geodesy.TLcdNIMA8350GeodeticDatumFactory;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelListener;
import com.luciad.model.TLcdModelChangedEvent;
import com.luciad.reference.ILcdGeoReference;
import com.luciad.reference.ILcdGeodeticReference;
import com.luciad.reference.ILcdGridReference;
import com.luciad.reference.TLcdGeodeticReference;
import com.luciad.reference.TLcdLambert1972BelgiumGridReference;
import com.luciad.reference.TLcdLambertFrenchGridReference;
import com.luciad.reference.TLcdRD1918DutchGridReference;
import com.luciad.reference.TLcdUTMGrid;
import com.luciad.shape.shape3D.ILcd3DEditablePoint;
import com.luciad.shape.shape3D.TLcdLonLatHeightPoint;
import com.luciad.shape.shape3D.TLcdXYZPoint;
import com.luciad.transformation.TLcdGeoReference2GeoReference;
import com.luciad.util.ILcdFireEventMode;
import com.luciad.util.TLcdAliasNames;
import com.luciad.util.TLcdLonLatFormatter;
import com.luciad.util.TLcdLonLatParser;
import com.luciad.util.TLcdOutOfBoundsException;

import samples.gxy.common.TitledPanel;

/**
 * This example implements a JPanel for performing coordinate conversions
 * between lon-lat coordinates and grid coordinates for one or more grid systems.
 * <p/>
 * There are two text fields which contain the geodetic latitude and longitude
 * respectively. The latitude and the longitude are represented in a certain
 * format which can be changed. Values have to be entered in this format too.
 * The geodetic coordinate is with respect to a certain geodetic datum which
 * can be altered.
 * Below are the <code>(x,y)</code> grid coordinates of this geodetic LatLon
 * coordinate according to some grid coordinate systems in use.
 * <p/>
 * Coordinates can be entered in any of the fields. Pressing enter then results
 * in an update of all values in the other fields.
 * Changing the geodetic datum or the UTM zone however will affect only the
 * geodetic coordinate or the UTM coordinate only.
 * Since the geodetic datum of the projection in the map component is kept
 * the same as the geodetic datum of the grid calculator, the icon will move
 * slightly too.
 */
class GridCalculation extends JPanel implements ILcdModelListener {

  // reference point (for intermediate stage of coordinate conversions)
  private ILcd3DEditablePoint fReferenceLonLatHeightPoint;

  // geodetic reference using the reference datum (WGS_1984)
  private ILcdGeodeticReference fReference = new TLcdGeodeticReference(new TLcdGeodeticDatum());

  // used to transform points from one reference to another, the source reference is always set to fReference
  private TLcdGeoReference2GeoReference fTransformation = new TLcdGeoReference2GeoReference();

  // temps
  private TLcdLonLatHeightPoint fLonLatCoordinate = new TLcdLonLatHeightPoint();
  private ILcd3DEditablePoint fGridCoordinate = new TLcdXYZPoint();

  // geodetic datums available in JComboBox + geodetic reference based on the chosen datum
  private TLcdNIMA8350GeodeticDatumFactory fGeodeticDatumFactory = new TLcdNIMA8350GeodeticDatumFactory();
  private ILcdGeodeticReference fChosenGeodeticReference = new TLcdGeodeticReference();

  // grid systems
  private NumberFormat fGridNumberFormat = NumberFormat.getInstance();
  private TLcdUTMGrid fUTMGrid = new TLcdUTMGrid();
  private TLcdLambertFrenchGridReference fFrenchGrid = new TLcdLambertFrenchGridReference();
  private ILcdGridReference[] fGridSystems = {
      new TLcdLambert1972BelgiumGridReference(),
      new TLcdRD1918DutchGridReference(),
      fFrenchGrid,
      fUTMGrid
  };

  // model
  private ILcdModel fModel;

  // GUI

  // geodetic systems
  private JTextField fLonField = new JTextField(11);
  private JTextField fLatField = new JTextField(11);
  private JComboBox fLonLatFormatJComboBox = new JComboBox();
  private JComboBox fGeodeticDatumJComboBox = new JComboBox();
  private TLcdLonLatFormatter fLonLatFormat;
  private TLcdLonLatParser fLonLatParser;

  // grid systems
  private JPanel fUTMJPanel = new JPanel();
  private JPanel fFrenchJPanel = new JPanel();
  private JComboBox fUTMZoneIDJComboBox = new JComboBox();
  private JComboBox fFrenchZoneIDJComboBox = new JComboBox();
  private JTextField[] fXFields;
  private JTextField[] fYFields;

  /**
   * Constructor with an <code>ILcdModel</code> <code>aModel</code>.
   * @param aModel an <code>ILcdModel</code>
   */
  public GridCalculation(ILcdModel aModel) {
    fModel = aModel;
    Enumeration enumeration = fModel.elements();
    fReferenceLonLatHeightPoint = (TLcdLonLatHeightPoint) enumeration.nextElement();
    fTransformation.setSourceReference(fReference);
    initGUI();
  }

  /**
   * Initialization of the class instance and construction of the GUI.
   */
  private void initGUI() {

    // geodetic coordinate JPanel

    // JComboBox item for longitude/latitude format
    // with default selection to floating point decimal degrees
    fLonLatFormatJComboBox.addItem(TLcdLonLatFormatter.DEC_DEG_5);
    fLonLatFormatJComboBox.addItem(TLcdLonLatFormatter.NSWE2);
    fLonLatFormatJComboBox.setSelectedItem(TLcdLonLatFormatter.DEC_DEG_5);
    fLonLatFormat = new TLcdLonLatFormatter(TLcdLonLatFormatter.DEC_DEG_5);
    fLonLatParser = new TLcdLonLatParser();

    // add action listeners which will perform the necessary operations if one
    // of the fields changes or one of the selections changes
    fLatField.addActionListener(new MyLonLatFieldListener());
    fLonField.addActionListener(new MyLonLatFieldListener());
    fLonLatFormatJComboBox.addItemListener(new MyLonLatFormatJComboBoxListener());
    fGeodeticDatumJComboBox.addItemListener(new MyGeodeticDatumJComboBoxListener());

    JPanel geodetic_coord_JPanel = new JPanel(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints(
        0, 0, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 5, 4, 5), 0, 0
    );
    gbc.gridx = 0;
    geodetic_coord_JPanel.add(new JLabel("Latitude"), gbc);
    gbc.gridx++;
    geodetic_coord_JPanel.add(fLatField, gbc);

    gbc.gridx = 0;
    gbc.gridy++;
    geodetic_coord_JPanel.add(new JLabel("Longitude"), gbc);
    gbc.gridx++;
    geodetic_coord_JPanel.add(fLonField, gbc);

    gbc.gridx = 0;
    gbc.gridy++;
    geodetic_coord_JPanel.add(new JLabel("Format"), gbc);
    gbc.gridx++;
    geodetic_coord_JPanel.add(fLonLatFormatJComboBox, gbc);

    gbc.gridx = 0;
    gbc.gridy++;
    geodetic_coord_JPanel.add(new JLabel("Datum"), gbc);
    gbc.gridx++;
    geodetic_coord_JPanel.add(fGeodeticDatumJComboBox, gbc);

    JPanel geodetic_coord_JPanel_wrapper = new JPanel(new BorderLayout());
    geodetic_coord_JPanel_wrapper.add(BorderLayout.NORTH, geodetic_coord_JPanel);

    JPanel geodetic_coord_JPanel_titled = TitledPanel.createTitledPanel(
        "Lat-Lon Geodetic Coordinate", geodetic_coord_JPanel_wrapper, TitledPanel.NORTH | TitledPanel.EAST
                                                                       );

    // construct JComboBox object with available geodetic datums from the geodetic
    // datum factory
    TLcdAliasNames geodetic_alias_names = fGeodeticDatumFactory.getAliasNames();
    for (int i = 0; i < geodetic_alias_names.getCount(); i++) {
      fGeodeticDatumJComboBox.addItem(geodetic_alias_names.findName(i));
    }
    fGeodeticDatumJComboBox.setSelectedItem("WGS_1984");
    ILcdGeodeticDatum chosen_geodetic_datum = fGeodeticDatumFactory.createGeodeticDatum(TLcdNIMA8350GeodeticDatumFactory.WGS_1984);
    fChosenGeodeticReference = new TLcdGeodeticReference(chosen_geodetic_datum);

    // grid coordinate JPanel

    JPanel grid_coord_JPanel = new JPanel(new GridBagLayout());

    JPanel grid_coord_JPanel_wrapper = new JPanel(new BorderLayout());
    grid_coord_JPanel_wrapper.add(BorderLayout.NORTH, grid_coord_JPanel);

    JPanel grid_coord_JPanel_titled = TitledPanel.createTitledPanel(
        "Grid Coordinates", grid_coord_JPanel_wrapper
                                                                   );

    // number format for grid coordinates
    fGridNumberFormat.setMinimumFractionDigits(2);
    fGridNumberFormat.setMaximumFractionDigits(2);
    fGridNumberFormat.setGroupingUsed(false);

    // fields for (x,y) grid coordinates
    fXFields = new JTextField[fGridSystems.length];
    fYFields = new JTextField[fGridSystems.length];
    for (int i = 0; i < fGridSystems.length; i++) {
      fXFields[i] = new JTextField();
      fYFields[i] = new JTextField();
    }

    JLabel label_utm = new JLabel("UTM", JLabel.LEFT);
    JLabel label_french = new JLabel("French Lambert", JLabel.LEFT);
    JLabel[] fGridJLabels = {
        new JLabel("Belgian Lambert 1972", JLabel.LEFT),
        new JLabel("Dutch Rijksdienst", JLabel.LEFT),
        label_french,
        label_utm
    };

    MyGridXYFieldListener grid_xy_field_listener = new MyGridXYFieldListener();
    for (int i = 0, n = fGridJLabels.length; i < n; i++) {
      // UTM grid is a bit different as there are many zones
      if (fGridJLabels[i].equals(label_utm)) {
        fUTMJPanel.setLayout(new BorderLayout());
        fUTMJPanel.add(label_utm, BorderLayout.WEST);
        JPanel temp_JPanel = new JPanel(new GridLayout(1, 2));
        temp_JPanel.add(new JLabel("Zone:"));
        temp_JPanel.add(fUTMZoneIDJComboBox);
        fUTMJPanel.add(temp_JPanel, BorderLayout.EAST);
        makeCell(grid_coord_JPanel, fUTMJPanel, 0, i + 1, 1, 1, 0.0, 0.0);
      }
      // French grid is a bit different as there are 4 zones
      else if (fGridJLabels[i].equals(label_french)) {
        fFrenchJPanel.setLayout(new BorderLayout());
        fFrenchJPanel.add(label_french, BorderLayout.WEST);
        JPanel temp_JPanel = new JPanel(new GridLayout(1, 2));
        temp_JPanel.add(new JLabel("Zone:"));
        temp_JPanel.add(fFrenchZoneIDJComboBox);
        fFrenchJPanel.add(temp_JPanel, BorderLayout.EAST);
        makeCell(grid_coord_JPanel, fFrenchJPanel, 0, i + 1, 1, 1, 0.0, 0.0);
      } else {
        fGridJLabels[i].setAlignmentX(JLabel.LEFT);
        makeCell(grid_coord_JPanel, fGridJLabels[i], 0, i + 1, 1, 1, 0.0, 0.0);
      }
      makeCell(grid_coord_JPanel, fXFields[i], 1, i + 1, 1, 1, 1.0, 0.0);
      makeCell(grid_coord_JPanel, fYFields[i], 2, i + 1, 1, 1, 1.0, 0.0);
      fXFields[i].addActionListener(grid_xy_field_listener);
      fYFields[i].addActionListener(grid_xy_field_listener);
    }

    // add all the UTM zones to the JComboBox list and select the middle one
    for (int i = 1; i < 61; i++) {
      fUTMZoneIDJComboBox.addItem("" + i);
    }
    fUTMZoneIDJComboBox.setSelectedItem("30");
    fUTMZoneIDJComboBox.addItemListener(new MyUTMZoneIDListener());
    fUTMGrid.setZoneID(fUTMZoneIDJComboBox.getSelectedIndex() + 1);

    // add the 4 zone of the French grid system and select zone II
    for (int i = 1; i < 5; i++) {
      fFrenchZoneIDJComboBox.addItem("" + i);
    }
    fFrenchZoneIDJComboBox.setSelectedItem("2");
    fFrenchZoneIDJComboBox.addItemListener(new MyFrenchZoneIDListener());
    fFrenchGrid.setZoneID(fFrenchZoneIDJComboBox.getSelectedIndex() + 1);

    calculateValues();

    setLayout(new BorderLayout());
    add(geodetic_coord_JPanel_titled, BorderLayout.WEST);
    add(grid_coord_JPanel_titled, BorderLayout.CENTER);
  }

  /*
   * Sets the cell with index (aX,aY) of the <code>Container</code> <code>aCont</code>
   * with the <code>Component</code> <code>aComp</code>.
   */
  private void makeCell(Container aCont, Component aComp, int aX, int aY, int aW, int aH, double aWx, double aWy) {
    GridBagLayout gbl = (GridBagLayout) aCont.getLayout();
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(0, 5, 4, 5);
    gbc.anchor = GridBagConstraints.WEST;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.gridx = aX;
    gbc.gridy = aY;
    gbc.gridwidth = aW;
    gbc.gridheight = aH;
    gbc.weightx = aWx;
    gbc.weighty = aWy;
    aCont.add(aComp);
    gbl.setConstraints(aComp, gbc);
  }

  /**
   * Listens if the model has changed and recalculates the reference point
   * into the different coordinate systems.
   */
  public void modelChanged(TLcdModelChangedEvent aEvent) {
    if (aEvent.getSource() != this) {
      calculateValues();
    }
  }

  /**
   * Calculates the reference coordinate value into the other coordinate systems.
   * The reference coordinate is a WGS_1984 coordinate.
   */
  private void calculateValues() {
    // set the geodetic longitude/latitude in the chosen geodetic datum
    try {
      fTransformation.setDestinationReference(fChosenGeodeticReference);
      fTransformation.sourcePoint2destinationSFCT(fReferenceLonLatHeightPoint,
                                                  fLonLatCoordinate);
    } catch (TLcdOutOfBoundsException e) {
      fLonLatCoordinate.move2D(Double.NaN, Double.NaN);
    }

    fUTMGrid.setNorthernHemisphere(!(fLonLatCoordinate.getY() < 0));
    fLonField.setText(fLonLatFormat.formatLon(fLonLatCoordinate.getX()));
    fLatField.setText(fLonLatFormat.formatLat(fLonLatCoordinate.getY()));
    // grid x,y coordinates
    // update each of the grid systems
    fTransformation.setSourceReference(fReference);
    for (int i = 0; i < fGridSystems.length; i++) {
      fTransformation.setDestinationReference(fGridSystems[i]);
      try {
        // transform the coordinate in the reference geodetic datum to the
        // geodetic datum of the grid reference
        // transform the geodetic coordinate into a grid coordinate
        fTransformation.sourcePoint2destinationSFCT(fReferenceLonLatHeightPoint, fGridCoordinate);
        // update the text fields with the resulting values
        fXFields[i].setText(fGridNumberFormat.format(fGridCoordinate.getX()));
        fYFields[i].setText(fGridNumberFormat.format(fGridCoordinate.getY()));
      } catch (TLcdOutOfBoundsException ob) {
        fXFields[i].setText("out of bounds");
        fYFields[i].setText("out of bounds");
      }
    }
  }

  /**
   * Listens to actions performed on the longitude or latitude text field.
   */
  private class MyLonLatFieldListener implements ActionListener {
    public void actionPerformed(ActionEvent aEvent) {
      double orig_lon = fReferenceLonLatHeightPoint.getX();
      double orig_lat = fReferenceLonLatHeightPoint.getY();
      double orig_height = fReferenceLonLatHeightPoint.getZ();

      try {
        double lon = fLonLatParser.londdmmssAsDouble(fLonField.getText());
        double lat = fLonLatParser.latddmmssAsDouble(fLatField.getText());
        fLonLatCoordinate.move2D(lon, lat);
        // transform LonLatPoint to WGS_1984 geodetic datum
        fTransformation.setSourceReference(fReference);
        fTransformation.setDestinationReference(fChosenGeodeticReference);
        fTransformation.destinationPoint2sourceSFCT(fLonLatCoordinate,
                                                    fReferenceLonLatHeightPoint);
      } catch (NumberFormatException nfe) {
        // restore the old state
      } catch (TLcdOutOfBoundsException e) {
        // restore the old state
        fReferenceLonLatHeightPoint.move3D(orig_lon, orig_lat, orig_height);
      }
      calculateValues();
      fModel.elementChanged(fReferenceLonLatHeightPoint, ILcdFireEventMode.FIRE_NOW);
    }
  }

  /**
   * Listens to change of longitude/latitude format.
   */
  private class MyLonLatFormatJComboBoxListener implements ItemListener {
    public void itemStateChanged(ItemEvent e) {
      if (fLonLatFormatJComboBox.getSelectedIndex() == 0) {
        fLonLatFormat.setPattern(TLcdLonLatFormatter.DEC_DEG_5);
      } else {
        fLonLatFormat.setPattern(TLcdLonLatFormatter.NSWE2);
      }
      try {
        fTransformation.setDestinationReference(fChosenGeodeticReference);
        fTransformation.sourcePoint2destinationSFCT(fReferenceLonLatHeightPoint,
                                                    fLonLatCoordinate);
        fLonField.setText(fLonLatFormat.formatLon(fLonLatCoordinate.getX()));
        fLatField.setText(fLonLatFormat.formatLat(fLonLatCoordinate.getY()));
      } catch (TLcdOutOfBoundsException ex) {
        fLonField.setText("out of bounds");
        fLatField.setText("out of bounds");
      }
    }
  }

  /**
   * Listens to change of geodetic datum coordinate system.
   */
  private class MyGeodeticDatumJComboBoxListener implements ItemListener {
    public void itemStateChanged(ItemEvent aEvent) {
      ILcdGeodeticDatum old_value = fChosenGeodeticReference.getGeodeticDatum();
      String datum_name = (String) (aEvent.getItem());
      // update the chosen geodetic datum instance
      int id = fGeodeticDatumFactory.getAliasNames().findID(datum_name);
      ILcdGeodeticDatum chosen_datum = fGeodeticDatumFactory.createGeodeticDatum(id);
      fChosenGeodeticReference = new TLcdGeodeticReference(chosen_datum);
      // convert the geodetic coordinate into the chosen geodetic reference
      // fReferenceLonLatHeightPoint still contained the geodetic coordinate
      // in the reference geodetic datum
      try {
        fTransformation.setDestinationReference(fChosenGeodeticReference);
        fTransformation.sourcePoint2destinationSFCT(fReferenceLonLatHeightPoint,
                                                    fLonLatCoordinate);
        // update the text fields
        fLonField.setText(fLonLatFormat.formatLon(fLonLatCoordinate.getX()));
        fLatField.setText(fLonLatFormat.formatLat(fLonLatCoordinate.getY()));
      } catch (TLcdOutOfBoundsException ex) {
        fLonField.setText("out of bounds");
        fLatField.setText("out of bounds");
      }
      // fire a property change event
      firePropertyChange("geodeticDatum", old_value, chosen_datum);
    }
  }

  /**
   * Listens to actions performed on the grid x,<i>y</i> coordinate fields.
   */
  private class MyGridXYFieldListener implements ActionListener {
    public void actionPerformed(ActionEvent aEvent) {
      try {
        // walk over all grid coordinate text fields to find the source of the ActionEvent
        // and more specifically to which grid coordinate system this corresponds
        // convert the grid coordinate to the corresponding geodetic coordinate in the
        // reference geodetic datum (WGS84)
        for (int i = 0; i < fGridSystems.length; i++) {
          if (aEvent.getSource().equals(fXFields[i]) ||
              aEvent.getSource().equals(fYFields[i])) {
            double orig_lon = fReferenceLonLatHeightPoint.getX();
            double orig_lat = fReferenceLonLatHeightPoint.getY();

            fTransformation.setDestinationReference(fGridSystems[i]);
            try {
              double x = Double.valueOf(fXFields[i].getText());
              double y = Double.valueOf(fYFields[i].getText());
              fGridCoordinate.move2D(x, y);
              fTransformation.destinationPoint2sourceSFCT(fGridCoordinate, fReferenceLonLatHeightPoint);
            } catch (TLcdOutOfBoundsException ob) {
              // out of bounds, restore point
              fReferenceLonLatHeightPoint.move2D(orig_lon, orig_lat);
            }
          }
        }
      } catch (NumberFormatException nfe) {
        // reset to the old state
      }
      // update all values from the coordinate in the reference geodetic datum
      calculateValues();
      // inform the model about the change of the reference lon lat height point
      fModel.elementChanged(fReferenceLonLatHeightPoint, ILcdFireEventMode.FIRE_NOW);
    }
  }

  /**
   * Listens to change of JComboBox of UTM zone ID.
   */
  private class MyUTMZoneIDListener implements ItemListener {
    public void itemStateChanged(ItemEvent e) {
      // set the UTM zone ID to the selected zone
      fUTMGrid.setZoneID(fUTMZoneIDJComboBox.getSelectedIndex() + 1);
      // initialize the transformation class with this ILcdModelReference
      fTransformation.setDestinationReference((ILcdGeoReference) fUTMGrid);
      int utm_place = -1;
      for (int i = 0; i < fGridSystems.length; i++) {
        if (fUTMGrid.equals(fGridSystems[i])) {
          utm_place = i;
        }
      }
      if (utm_place >= 0) {
        try {
          // convert the WGS84 reference coordinate into the corresponding UTM grid coordinate
          fTransformation.sourcePoint2destinationSFCT(fReferenceLonLatHeightPoint, fGridCoordinate);
          fXFields[utm_place].setText(fGridNumberFormat.format(fGridCoordinate.getX()));
          fYFields[utm_place].setText(fGridNumberFormat.format(fGridCoordinate.getY()));
        } catch (TLcdOutOfBoundsException ob) {
          fXFields[utm_place].setText("out of bounds");
          fYFields[utm_place].setText("out of bounds");
        }
      }
    }
  }

  /**
   * Listens to change of JComboBox of French grid zone ID.
   */
  private class MyFrenchZoneIDListener implements ItemListener {
    public void itemStateChanged(ItemEvent e) {
      // set the French grid system to the current selected zone
      fFrenchGrid.setZoneID(fFrenchZoneIDJComboBox.getSelectedIndex() + 1);
      // initialize the transformation class with this ILcdModelReference
      fTransformation.setDestinationReference((ILcdGeoReference) fFrenchGrid);
      int french_grid_place = -1;
      for (int i = 0; i < fGridSystems.length; i++) {
        if (fFrenchGrid.equals(fGridSystems[i])) {
          french_grid_place = i;
        }
      }
      if (french_grid_place >= 0) {
        try {
          // convert the WGS84 reference coordinate into the corresponding
          // French grid system coordinate of the selected zone
          fTransformation.sourcePoint2destinationSFCT(fReferenceLonLatHeightPoint, fGridCoordinate);
          fXFields[french_grid_place].setText(fGridNumberFormat.format(fGridCoordinate.getX()));
          fYFields[french_grid_place].setText(fGridNumberFormat.format(fGridCoordinate.getY()));
        } catch (TLcdOutOfBoundsException ob) {
          fXFields[french_grid_place].setText("out of bounds");
          fYFields[french_grid_place].setText("out of bounds");
        }
      }
    }
  }

}

