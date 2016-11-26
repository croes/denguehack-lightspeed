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
package samples.decoder.bingmaps.gxy;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import com.luciad.format.bingmaps.ELcdBingMapsMapStyle;
import com.luciad.format.bingmaps.ILcdBingMapsDataSource;
import com.luciad.format.bingmaps.TLcdBingMapsModelDecoder;
import com.luciad.format.bingmaps.TLcdBingMapsModelDescriptor;
import com.luciad.format.bingmaps.TLcdCustomBingMapsDataSourceBuilder;
import com.luciad.format.bingmaps.copyright.gxy.TLcdBingMapsGXYCopyrightIcon;
import com.luciad.geodesy.TLcdGeodeticDatum;
import com.luciad.gui.ILcdIcon;
import com.luciad.gui.swing.TLcdOverlayLayout;
import com.luciad.gui.swing.TLcdSWIcon;
import com.luciad.model.ILcdModel;
import com.luciad.projection.TLcdPseudoMercator;
import com.luciad.reference.TLcdGridReference;
import com.luciad.util.ELcdHorizontalAlignment;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.asynchronous.TLcdGXYAsynchronousLayerWrapper;

import samples.decoder.bingmaps.ChangeListenerLabel;
import samples.decoder.bingmaps.DataSourceFactory;
import samples.decoder.bingmaps.LayerVisibilityLabel;
import samples.decoder.bingmaps.OpenURLAction;
import samples.gxy.common.GXYSample;
import samples.gxy.common.TitledPanel;
import samples.gxy.common.layers.GXYLayerUtil;
import samples.gxy.projections.ProjectionComboBox;

/**
 * This sample shows how to show a Bing Maps layer, and the corresponding Logo and copyright
 * information, in a 2D view.
 */
public class MainPanel extends GXYSample {

  private int fCount = 0;
  private ILcdGXYLayer fBingLayer;
  private BingMapsGXYLayerFactory fLayerFactory = new BingMapsGXYLayerFactory();

  public void createGUI() {
    super.createGUI();

    // Map setup.
    TLcdGridReference worldReference =
        new TLcdGridReference( new TLcdGeodeticDatum(),
                               new TLcdPseudoMercator() // optimal projection for Bing Maps
        );
    getView().setXYWorldReference( worldReference );

    getToolBars()[0].addComponent(new ProjectionComboBox(getView(), -1));
      getToolBars()[0].addSpace();
      getToolBars()[0].addAction(new MyUrlOpenAction());

    // Adds a Bing Maps layer.
    ILcdModel model = DataSourceFactory.createDefaultBingModel( ELcdBingMapsMapStyle.AERIAL, this );

    if ( model != null ) {

      BingMapsGXYLayerFactory layerFactory = new BingMapsGXYLayerFactory();
      fBingLayer = new TLcdGXYAsynchronousLayerWrapper( layerFactory.createGXYLayer( model ) );
      GXYLayerUtil.addGXYLayer( getView(), fBingLayer );

      // Add copyright icon and logo.
      TLcdBingMapsModelDescriptor modelDescriptor = ( TLcdBingMapsModelDescriptor ) model.getModelDescriptor();
      ILcdIcon logoIcon = modelDescriptor.getLogo();

      // Shows a Bing Maps logo, linked to the layer's visibility.
      if ( logoIcon != null ) {
        LayerVisibilityLabel logoLabel = new LayerVisibilityLabel( new TLcdSWIcon( logoIcon ), getView(),
                                                                   TLcdBingMapsModelDescriptor.class );
        getOverlayPanel().add( logoLabel, TLcdOverlayLayout.Location.NORTH_WEST );
      }

      // Shows Bing Maps attribution strings, linked to what is shown in the map
      TLcdBingMapsGXYCopyrightIcon copyrightIcon = new TLcdBingMapsGXYCopyrightIcon( getView() );
      copyrightIcon.setAlignment( ELcdHorizontalAlignment.RIGHT );
      ChangeListenerLabel copyrightLabel = new ChangeListenerLabel( new TLcdSWIcon( copyrightIcon ), copyrightIcon );
      getOverlayPanel().add( copyrightLabel, TLcdOverlayLayout.Location.NORTH_WEST );
    }
  }

  @Override
  protected void addData() throws IOException {
    // nothing to do, we already add our data in the createGUI.
  }
  
  @Override
  protected JPanel createSettingsPanel() {
    JRadioButton aerialRadio = new JRadioButton();
    aerialRadio.setAction( new AbstractAction( "Aerial" ) {
      @Override
      public void actionPerformed( ActionEvent e ) {
        setBingLayer( ELcdBingMapsMapStyle.AERIAL );
      }
    } );
    aerialRadio.setSelected( true );
    JRadioButton roadsRadio = new JRadioButton();
    roadsRadio.setAction( new AbstractAction( "Roads" ) {
      @Override
      public void actionPerformed( ActionEvent e ) {
        setBingLayer( ELcdBingMapsMapStyle.ROAD );
      }
    } );
    JRadioButton aerialWithLabels = new JRadioButton();
    aerialWithLabels.setAction( new AbstractAction( "Labeled aerial" ) {
      @Override
      public void actionPerformed( ActionEvent e ) {
        setBingLayer( ELcdBingMapsMapStyle.AERIAL_WITH_LABELS );
      }
    } );

    ButtonGroup group = new ButtonGroup();
    group.add( aerialRadio );
    group.add( roadsRadio );
    group.add( aerialWithLabels );

    JPanel gridTypePanel = new JPanel( new GridLayout( 3, 1 ) );
    gridTypePanel.add( aerialRadio );
    gridTypePanel.add( roadsRadio );
    gridTypePanel.add( aerialWithLabels );
    return TitledPanel.createTitledPanel( "Map type", gridTypePanel );
  }

  private void setBingLayer( ELcdBingMapsMapStyle aMapStyle ) {
    GXYLayerUtil.removeGXYLayer( getView(), fBingLayer, false );
    ILcdModel roads = DataSourceFactory.createDefaultBingModel(
        aMapStyle, this );
    fBingLayer = new TLcdGXYAsynchronousLayerWrapper( fLayerFactory.createGXYLayer( roads ) );
    GXYLayerUtil.addGXYLayer( getView(), fBingLayer );
  }

  public static void main( final String[] args ) {
    startSample( MainPanel.class, "BingMaps 2D" );
  }

  private class MyUrlOpenAction extends OpenURLAction {
    private MyUrlOpenAction(  ) {
      super( MainPanel.this );
    }

    @Override
    protected void loadModel( String aUrl ) throws IOException {
      fCount++;
      ILcdBingMapsDataSource source = new TLcdCustomBingMapsDataSourceBuilder( aUrl ).build();
      ILcdModel model = new TLcdBingMapsModelDecoder().decodeSource( source );
      // Create a layer for the model.
      ILcdGXYLayer layer = new BingMapsGXYLayerFactory().createGXYLayer( model );
      layer.setLabel( "Bing Compatible layer " + fCount );
      TLcdGXYAsynchronousLayerWrapper wrapper = new TLcdGXYAsynchronousLayerWrapper( layer );
      // Add layer to view.
      GXYLayerUtil.addGXYLayer( getView(), wrapper );
    }
  }
}

