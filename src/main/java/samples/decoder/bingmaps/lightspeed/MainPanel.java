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
package samples.decoder.bingmaps.lightspeed;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.Collection;

import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import com.luciad.format.bingmaps.ELcdBingMapsMapStyle;
import com.luciad.format.bingmaps.ILcdBingMapsDataSource;
import com.luciad.format.bingmaps.TLcdBingMapsModelDecoder;
import com.luciad.format.bingmaps.TLcdBingMapsModelDescriptor;
import com.luciad.format.bingmaps.TLcdCustomBingMapsDataSourceBuilder;
import com.luciad.format.bingmaps.copyright.lightspeed.TLspBingMapsCopyrightIcon;
import com.luciad.geodesy.TLcdGeodeticDatum;
import com.luciad.gui.ILcdAction;
import com.luciad.gui.ILcdIcon;
import com.luciad.gui.swing.TLcdOverlayLayout;
import com.luciad.gui.swing.TLcdSWIcon;
import com.luciad.model.ILcdModel;
import com.luciad.projection.TLcdPseudoMercator;
import com.luciad.reference.TLcdGridReference;
import com.luciad.util.ELcdHorizontalAlignment;
import com.luciad.view.lightspeed.ILspAWTView;
import com.luciad.view.lightspeed.layer.ILspLayer;
import com.luciad.view.lightspeed.layer.ILspLayerFactory;
import com.luciad.view.lightspeed.util.TLspViewTransformationUtil;

import samples.decoder.bingmaps.ChangeListenerLabel;
import samples.decoder.bingmaps.DataSourceFactory;
import samples.decoder.bingmaps.LayerVisibilityLabel;
import samples.decoder.bingmaps.OpenURLAction;
import samples.gxy.common.TitledPanel;
import samples.lightspeed.common.FitUtil;
import samples.lightspeed.common.LightspeedSample;
import samples.lightspeed.common.ToolBar;

/**
 * This sample shows how to show a Bing Maps layer, and the corresponding Logo and copyright
 * information, in a Lightspeed view.
 */
public class MainPanel extends LightspeedSample {

  private ILspLayer fBingLayer;

  @Override
  protected ILspAWTView createView() {
    ILspAWTView view = super.createView();
    // The Bing Maps imagery is defined in pseudo-mercator.
    // Setting the view in this reference gives the best possible visual quality.
    TLspViewTransformationUtil
        .setup2DView( view, new TLcdGridReference( new TLcdGeodeticDatum(),
                                                   new TLcdPseudoMercator() ), false );
    return view;
  }

  @Override
  protected ILspLayerFactory createLayerFactory() {
    return new LspBingMapsLayerFactory();
  }

  @Override
  protected void addData() throws IOException {
    super.addData();

    // Adds a Bing Maps layer
    ILcdModel model = DataSourceFactory.createDefaultBingModel( ELcdBingMapsMapStyle.AERIAL, null );

    if ( model != null ) {

      // Creates and adds a Bing Maps layer
      Collection<ILspLayer> layers = getView().addLayersFor( model );
      fBingLayer = layers.iterator().next();

      TLcdBingMapsModelDescriptor modelDescriptor = ( TLcdBingMapsModelDescriptor ) model.getModelDescriptor();
      ILcdIcon logoIcon = modelDescriptor.getLogo();

      // Shows a Bing Maps logo, linked to the layer's visibility.
      if ( logoIcon != null ) {
        LayerVisibilityLabel logoLabel = new LayerVisibilityLabel( new TLcdSWIcon( logoIcon ), getView(),
                                                                   TLcdBingMapsModelDescriptor.class );
        getOverlayPanel().add( logoLabel, TLcdOverlayLayout.Location.NORTH_WEST );
      }

      // Shows Bing Maps attribution strings, linked to what is shown in the map
      TLspBingMapsCopyrightIcon copyrightIcon = new TLspBingMapsCopyrightIcon( getView() );
      copyrightIcon.setAlignment( ELcdHorizontalAlignment.RIGHT );
      ChangeListenerLabel copyrightLabel = new ChangeListenerLabel( new TLcdSWIcon( copyrightIcon ), copyrightIcon );
      getOverlayPanel().add( copyrightLabel, TLcdOverlayLayout.Location.NORTH_WEST );

      FitUtil.fitOnLayers(this, fBingLayer);
    }
  }

  @Override
  protected void createGUI() {
    super.createGUI();
    addComponentToRightPanel( createGridTypePanel() );
    ILcdAction action = new MyUrlOpenAction();
    getToolBars()[ 0 ].addAction( action, ToolBar.FILE_GROUP );
  }

  private JPanel createGridTypePanel() {
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
    if ( fBingLayer != null ) {
      getView().removeLayer( fBingLayer );
    }
    ILcdModel model = DataSourceFactory.createDefaultBingModel( aMapStyle, this );
    if ( model != null ) {
      fBingLayer = getView().addLayersFor( model ).iterator().next();
    }
  }

  private class MyUrlOpenAction extends OpenURLAction {
    public MyUrlOpenAction(  ) {
      super( MainPanel.this );
    }

    @Override
    protected void loadModel( String aURL ) throws IOException {
      ILcdBingMapsDataSource source = new TLcdCustomBingMapsDataSourceBuilder( aURL ).build();
      ILcdModel model = new TLcdBingMapsModelDecoder().decodeSource( source );
      getView().addLayersFor(model);
    }
  }

  public static void main( final String[] aArgs ) {
    startSample(MainPanel.class, "BingMaps Lightspeed sample");
  }
}
