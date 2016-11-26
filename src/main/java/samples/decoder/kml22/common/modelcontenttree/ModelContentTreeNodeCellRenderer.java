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
package samples.decoder.kml22.common.modelcontenttree;

import com.luciad.format.kml22.model.TLcdKML22DynamicModel;
import com.luciad.format.kml22.model.TLcdKML22Kml;
import com.luciad.format.kml22.model.TLcdKML22RenderableModel;
import com.luciad.format.kml22.model.feature.TLcdKML22AbstractContainer;
import com.luciad.format.kml22.model.feature.TLcdKML22AbstractFeature;
import com.luciad.format.kml22.model.feature.TLcdKML22Document;
import com.luciad.format.kml22.model.feature.TLcdKML22Folder;
import com.luciad.format.kml22.model.feature.TLcdKML22GroundOverlay;
import com.luciad.format.kml22.model.feature.TLcdKML22NetworkLink;
import com.luciad.format.kml22.model.feature.TLcdKML22PaintableGroundOverlay;
import com.luciad.format.kml22.model.feature.TLcdKML22PhotoOverlay;
import com.luciad.format.kml22.model.feature.TLcdKML22ScreenOverlay;
import com.luciad.format.kml22.model.style.ELcdKML22StyleState;
import com.luciad.format.kml22.model.style.TLcdKML22IconStyle;
import com.luciad.format.kml22.model.style.TLcdKML22LineStyle;
import com.luciad.format.kml22.model.style.TLcdKML22PolyStyle;
import com.luciad.format.kml22.model.style.TLcdKML22Style;
import com.luciad.format.kml22.util.ELcdKML22ResourceStatus;
import com.luciad.format.kml22.util.ILcdKML22ResourceListener;
import com.luciad.format.kml22.util.TLcdKML22ResourceDescriptor;
import com.luciad.format.kml22.util.TLcdKML22ResourceProvider;
import com.luciad.format.kml22.util.TLcdKML22StyleProvider;
import com.luciad.gui.ILcdIcon;
import com.luciad.gui.TLcdAWTUtil;
import com.luciad.gui.TLcdImageIcon;
import com.luciad.gui.TLcdResizeableIcon;
import com.luciad.gui.TLcdSymbol;
import com.luciad.gui.swing.TLcdSWIcon;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelTreeNode;
import com.luciad.shape.ILcdComplexPolygon;
import com.luciad.shape.ILcdPoint;
import com.luciad.shape.ILcdPolyline;
import com.luciad.shape.ILcdShape;
import com.luciad.shape.ILcdShapeList;
import com.luciad.shape.ILcdSurface;
import com.luciad.util.ILcdFireEventMode;
import com.luciad.util.ILcdStatusListener;
import com.luciad.util.TLcdStatusEvent;
import com.luciad.util.logging.ILcdLogger;
import com.luciad.util.logging.TLcdLoggerFactory;
import com.luciad.view.ILcdLayer;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.Timer;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.ref.WeakReference;
import java.util.WeakHashMap;

/**
 * <p>A custom model content tree cell renderer that can use a custom style provider to generate correct
 * icons for any given KML abstract feature. It also features a checkbox per cell to set the
 * visibility of an abstract feature.</p>
 */
public class ModelContentTreeNodeCellRenderer implements TreeCellRenderer {
  private static ILcdLogger sLogger = TLcdLoggerFactory.getLogger( ModelContentTreeNodeCellRenderer.class.getName() );

  private static final ILcdIcon POINT_ICON = new TLcdImageIcon( "kml/pushpin/ylw-pushpin.png" );
  private static final ILcdIcon DOCUMENT_ICON = new TLcdImageIcon( "images/icons/globe_16.png");
  private static final ILcdIcon OPEN_FOLDER_ICON = new TLcdImageIcon( "images/icons/folder_expanded_16.png" );
  private static final ILcdIcon CLOSED_FOLDER_ICON = new TLcdImageIcon( "images/icons/folder_collapsed_16.png" );
  private static final ILcdIcon OPEN_NETWORKLINK_ICON = new TLcdImageIcon( "images/icons/folder_network_expanded_16.png" );
  private static final ILcdIcon CLOSED_NETWORKLINK_ICON = new TLcdImageIcon( "images/icons/folder_network_collapsed_16.png" );
  private static final ILcdIcon OPEN_ERROR_ICON = new TLcdImageIcon( "images/icons/folder_network_error_expanded_16.png" );
  private static final ILcdIcon CLOSED_ERROR_ICON = new TLcdImageIcon( "images/icons/folder_network_error_closed_16.png" );
  private static final ILcdIcon OPEN_FETCHING0_ICON = new TLcdImageIcon( "images/icons/folder_network_fetching_expanded_0_16.png" );
  private static final ILcdIcon OPEN_FETCHING1_ICON = new TLcdImageIcon( "images/icons/folder_network_fetching_expanded_1_16.png" );
  private static final ILcdIcon OPEN_FETCHING2_ICON = new TLcdImageIcon( "images/icons/folder_network_fetching_expanded_2_16.png" );
  private static final ILcdIcon CLOSED_FETCHING0_ICON = new TLcdImageIcon( "images/icons/folder_network_fetching_collapsed_0_16.png" );
  private static final ILcdIcon CLOSED_FETCHING1_ICON = new TLcdImageIcon( "images/icons/folder_network_fetching_collapsed_1_16.png" );
  private static final ILcdIcon CLOSED_FETCHING2_ICON = new TLcdImageIcon( "images/icons/folder_network_fetching_collapsed_2_16.png" );
  private static final AnimatedIcon OPEN_FETCHING_ANIMATED_ICON = new AnimatedIcon( new ILcdIcon[]{OPEN_FETCHING0_ICON,OPEN_FETCHING1_ICON,OPEN_FETCHING2_ICON} );
  private static final AnimatedIcon CLOSED_FETCHING_ANIMATED_ICON = new AnimatedIcon( new ILcdIcon[]{CLOSED_FETCHING0_ICON,CLOSED_FETCHING1_ICON,CLOSED_FETCHING2_ICON} );
  private static final ILcdIcon GROUNDOVERLAY_ICON = new TLcdImageIcon( "images/icons/terrain_layer_16.png" );
  private static final ILcdIcon SCREENOVERLAY_ICON = new TLcdImageIcon( "images/icons/fixed_image_layer_16.png" );
  private static final ILcdIcon PHOTOOVERLAY_ICON = new TLcdImageIcon( "images/icons/image_layer_16.png" );
  private static final ILcdIcon EMPTY_ICON = new MyEmptyIcon();
  private static final String NO_NAME = "no_name";

  private TLcdKML22ResourceProvider fResourceProvider;
  private int fIconWidth;
  private int fIconHeight;

  private static final int DELAY = 200;
  private MyIconUpdater fIconUpdater = new MyIconUpdater( DELAY);
  private MyResourceListener fResourceListener = new MyResourceListener(this);

  private DefaultTreeCellRenderer fDelegateRenderer = new DefaultTreeCellRenderer();

  /**
   * <p>The abstract feature which should be rendered. Can be null.</p>
   */
  private WeakReference<TLcdKML22AbstractFeature> fCurrentAbstractFeature;
  /**
   * <p>The model tree node that should be rendered. Is null whenever a leaf element is being rendered.</p>
   */
  private WeakReference<TLcdKML22DynamicModel> fCurrentModelTreeNode;
  /**
   * <p>The tree row of the abstract feature currently being rendered.</p>
   */
  private int fCurrentAbstractFeatureRow;

  /**
   * <p>Timer used to trigger a repaint for multiple changes at once, so that the tree should not be repainted for every
   * change (e.g. changing the visibility of 1000 layers would otherwise require 1000 repaints)</p>
   */
  private Timer fTimer;

  /**
   * <p>The delay of the timer, expressed in milliseconds</p>
   */
  private final int fTimerDelay = 20;

  /**
   * <p>The tree on which this renderer is used</p>
   */
  private JTree fTree;

  /**
   * A list of all dynamic models that we are listening to.
   */
  private WeakHashMap<TLcdKML22DynamicModel,NetworkLinkStateListener> fListenerList = new WeakHashMap<TLcdKML22DynamicModel,NetworkLinkStateListener>( );
  private MyTreeSelectionListener fTreeSelectionListener;
  private WeakHashMap<Object,JCheckBox> fCheckboxMap = new WeakHashMap<Object, JCheckBox>( );

  /**
   * <p>Create a new renderer for an abstract feature with the specified icon height and width.</p>
   *
   * @param aResourceProvider the style provider
   * @param aIconWidth     the width of the icon
   * @param aIconHeight    the height of the icon
   */
  public ModelContentTreeNodeCellRenderer( TLcdKML22ResourceProvider aResourceProvider,
                                           int aIconWidth,
                                           int aIconHeight ) {
    fResourceProvider = aResourceProvider;
    fIconWidth = aIconWidth;
    fIconHeight = aIconHeight;
    fDelegateRenderer.setEnabled( true );
    fTreeSelectionListener = new MyTreeSelectionListener();
  }

  public JLabel getLabel() {
    return fDelegateRenderer;
  }

  public Component getTreeCellRendererComponent( JTree aTree, Object aValue,
                                                 boolean aSelected, boolean aExpanded,
                                                 boolean aLeaf, int aRow, boolean aHasFocus ) {
    if ( fTree == null ) {
      fTree = aTree;
      fTree.getSelectionModel().addTreeSelectionListener( fTreeSelectionListener );
    }

    if(aValue instanceof TreeModelObject){
      aValue = ( ( TreeModelObject ) aValue ).getTreeModelObject();
    }

    if(aValue instanceof TLcdKML22DynamicModel &&
       ( ( TLcdKML22DynamicModel ) aValue ).getKMLNetworkLink()!=null &&
       !fListenerList.containsKey( ( ( TLcdKML22DynamicModel ) aValue ) )){
      NetworkLinkStateListener listener = new NetworkLinkStateListener( this );
      ( ( TLcdKML22DynamicModel ) aValue ).addStatusListener( listener );
      fListenerList.put( ( TLcdKML22DynamicModel ) aValue, listener );
    }

    fCurrentAbstractFeatureRow = aRow;
    fCurrentModelTreeNode = null;

    //retrieve the text to be displayed. Also sets the fCurrentAbstractFeature and fCurrentModelTreeNode fields.
    String displayText = retrieveDisplayText( aValue );

    //Call the delegate renderer to create a cell component for the given displayText
    Component component = fDelegateRenderer.getTreeCellRendererComponent( aTree, displayText, aSelected, aExpanded, aLeaf, aRow, aHasFocus );
    CheckBoxPanel panel = new CheckBoxPanel();
    panel.add(component,BorderLayout.CENTER);
    panel.setOpaque( false );
    panel.setFocusable( false );

    JCheckBox checkBox;
    if ( fCheckboxMap.containsKey(aValue) ) {
      checkBox = fCheckboxMap.get( aValue );
    }
    else {
      checkBox = new JCheckBox( "" );
      checkBox.setOpaque( false );
      checkBox.setBorderPaintedFlat( true );
      checkBox.setMargin( new Insets( 1, 0, 1, 2 ) );
      checkBox.setFocusable( false ); //nothing in the editor needs focus, will only prevent the tree from getting focus
      checkBox.addActionListener( new VisibilityActionListener(checkBox) );
      fCheckboxMap.put(aValue,checkBox);
    }
    panel.setCheckbox( checkBox );

    //process the checkbox so that it is correctly displayed for the found abstract feature
    processCheckBox(checkBox, fCurrentAbstractFeature==null?null:fCurrentAbstractFeature.get(), aHasFocus);

    //transparent background for the renderer
    fDelegateRenderer.setBackgroundNonSelectionColor( new Color( 0, 0, 0, 0 ) );

    //retrieve the icon for this abstract feature
    ILcdIcon icon = retrieveIcon(fCurrentAbstractFeature==null?null:fCurrentAbstractFeature.get(), aExpanded, aValue);

    //Wrap the icon in a TLcdSWIcon to be able to add it to the delegate renderer
    TLcdSWIcon swingIcon = new TLcdSWIcon( new TLcdResizeableIcon( icon, fIconWidth, fIconHeight ) );
    fDelegateRenderer.setIcon( swingIcon );
    fDelegateRenderer.setDisabledIcon( swingIcon );

    //Update the cell with a description snippet
    ModelContentTreeNodeCellRendererUtil.updateRendererFromAbstractFeature(getLabel(), fCurrentAbstractFeature==null?null:fCurrentAbstractFeature.get());

    return panel;
  }

  /**
   * Makes the parents in the given tree selection path visible
   * @param aPathElements a path of elements that should be made visible
   */
  private void makeParentsVisible( Object[] aPathElements ) {
    for ( Object pathElement : aPathElements ) {
      if ( pathElement instanceof TreeModelObject ) {
        pathElement = ( ( TreeModelObject ) pathElement ).getTreeModelObject();
        if ( pathElement instanceof TLcdKML22DynamicModel ) {
          Object featureObject = ( ( TLcdKML22DynamicModel ) pathElement ).getKMLNetworkLink() == null ? ( ( TLcdKML22DynamicModel ) pathElement ).getKMLModel() : ( ( TLcdKML22DynamicModel ) pathElement ).getKMLNetworkLink();
          if ( featureObject instanceof TLcdKML22AbstractFeature ) {
            ( ( TLcdKML22AbstractFeature ) featureObject ).setVisibility( true );
            if ( ( ( TLcdKML22DynamicModel ) pathElement ).getKMLNetworkLink() != null &&
                 ( ( TLcdKML22DynamicModel ) pathElement ).getKMLModel().modelCount() > 0 &&
                 ( ( TLcdKML22DynamicModel ) pathElement ).getKMLModel().getModel( 0 ) instanceof TLcdKML22AbstractContainer ) {
              ( ( TLcdKML22AbstractContainer ) ( ( TLcdKML22DynamicModel ) pathElement ).getKMLModel().getModel( 0 ) ).setVisibility( true );
            }
          }
        }
      }
    }
  }

  /**
   * <p>Processes the visibility and selection of the checkbox, based on the given {@link TLcdKML22AbstractFeature} and focus</p>
   * <p>If the model content tree node gets focus, it should also be made visible.</p>
   * @param aCheckBox A checkbox which will have its properties adjusted
   * @param aCurrentAbstractFeature a {@link TLcdKML22AbstractFeature} to which the properties of the checkbox will change. Can be null.
   * @param aHasFocus Whether or not this abstract feature has focus
   */
  private void processCheckBox( JCheckBox aCheckBox, TLcdKML22AbstractFeature aCurrentAbstractFeature, boolean aHasFocus ) {
    if ( aCurrentAbstractFeature != null ) {
      //If the current abstract feature is not null, then we can inlclude a checkbox. (Only KML
      //entities are not abstract features in the JTree)
      aCheckBox.setSelected( aCurrentAbstractFeature.getVisibility() );
      aCheckBox.setEnabled( true );
      aCheckBox.setVisible( true );
    }
    else {
      aCheckBox.setSelected( false );
      aCheckBox.setEnabled( false );
      aCheckBox.setVisible( false );
    }
  }

  /**
   * <p>Retrieves the display text of a given node to be rendered</p>
   *
   * <p><b>Note:</b>As a side effect, this method will also set the {@link #fCurrentAbstractFeature} and {@link #fCurrentModelTreeNode} fields
   * to indicate which element is being rendered</p>
   * @param aValue a node to be rendered
   * @return a String value that represents the displayable text of the tree node cell.
   */
  private String retrieveDisplayText( Object aValue ) {
    String displayText = null;

    if ( aValue instanceof TLcdKML22DynamicModel ) {
      //if aValue is a container we set it as the current model tree node
      TLcdKML22DynamicModel currentModelTreeNode = (TLcdKML22DynamicModel) aValue;
      fCurrentModelTreeNode = new WeakReference<TLcdKML22DynamicModel>(currentModelTreeNode);
      Object featureObject = currentModelTreeNode.getKMLNetworkLink() == null ? currentModelTreeNode.getKMLModel() : currentModelTreeNode.getKMLNetworkLink();
      if ( featureObject instanceof TLcdKML22AbstractFeature ) {
        //if the model tree node contains an abstract feature as kml entity, we set the current abstract feature
        //to the kml entity itself.
        fCurrentAbstractFeature = new WeakReference<TLcdKML22AbstractFeature>((TLcdKML22AbstractFeature) featureObject);
      }
      displayText = currentModelTreeNode.getName();
      if ( displayText == null && featureObject instanceof TLcdKML22NetworkLink ) {
        if (currentModelTreeNode.modelCount() > 0 && currentModelTreeNode.getModel(0).getKMLModel() instanceof TLcdKML22Document) {
          //if the display text was not found in the model tree node, it might be because it wasn't given
          //in the networklink. In this case, the displayText value should be determined by its underlying
          //Document element.
          displayText = ((TLcdKML22Document) currentModelTreeNode.getModel(0).getKMLModel()).getName();
        }
      }
    }
    else if ( aValue instanceof TLcdKML22AbstractFeature ) {
      //If aValue is not a container, it must be an element.
      TLcdKML22AbstractFeature currentAbstractFeature = (TLcdKML22AbstractFeature) aValue;
      fCurrentAbstractFeature = new WeakReference<TLcdKML22AbstractFeature>(currentAbstractFeature);
      displayText = currentAbstractFeature.getName();
    } else if (aValue instanceof TLcdKML22PaintableGroundOverlay) {
      //If aValue is not an abstract feature, it might be a wrapper for a groundoverlay.
      TLcdKML22PaintableGroundOverlay paintableGroundOverlay = ( TLcdKML22PaintableGroundOverlay ) aValue;
      TLcdKML22GroundOverlay currentAbstractFeature = paintableGroundOverlay.getGroundOverlay();
      fCurrentAbstractFeature = new WeakReference<TLcdKML22AbstractFeature>(currentAbstractFeature);
      displayText = currentAbstractFeature.getName();
    }
    if ( displayText == null ) {
      //default display text
      displayText = NO_NAME;
    }
    return displayText;
  }

  /**
   * <p>Retrieves a valid icon given an abstract feature.</p>
   * @param aAbstractFeature an abstract feature
   * @param aExpanded true if the node is expanded, false otherwise
   * @param aValue The value of the node
   * @return A valid icon that can be used to visualize the abstract feature in a tree.
   */
  public ILcdIcon retrieveIcon( TLcdKML22AbstractFeature aAbstractFeature, boolean aExpanded, Object aValue ) {
    if(aAbstractFeature == null){
      //If the given value is null, return an empty icon.
      return EMPTY_ICON;
    }
    if ( aAbstractFeature instanceof TLcdKML22Document) {
      return DOCUMENT_ICON;
    }
    if( aAbstractFeature instanceof TLcdKML22Folder ){
      if( aExpanded ){
        return OPEN_FOLDER_ICON;
      }else{
        return CLOSED_FOLDER_ICON;
      }
    }
    if ( aAbstractFeature instanceof TLcdKML22NetworkLink) {
      if(aValue instanceof TLcdKML22DynamicModel){
        TLcdKML22DynamicModel dynamicModel = ( TLcdKML22DynamicModel ) aValue;
        ELcdKML22ResourceStatus networklinkStatus = dynamicModel.getStatus();
        switch ( networklinkStatus ) {
          case CACHED:
            return aExpanded?OPEN_NETWORKLINK_ICON:CLOSED_NETWORKLINK_ICON;
          case NOT_CACHED:
            return aExpanded?OPEN_NETWORKLINK_ICON:CLOSED_NETWORKLINK_ICON;
          case LOADING:
            fIconUpdater.ensureRepaint();
            return aExpanded?OPEN_FETCHING_ANIMATED_ICON:CLOSED_FETCHING_ANIMATED_ICON;
          case FAULTY:
            return aExpanded?OPEN_ERROR_ICON:CLOSED_ERROR_ICON;
        }
      }
    }
    if(aAbstractFeature instanceof TLcdKML22GroundOverlay ){
      //if the abstract feature is a groundoverlay, return a groundoverlay icon
      return GROUNDOVERLAY_ICON;
    }
    if(aAbstractFeature instanceof TLcdKML22ScreenOverlay ){
      //if the abstract feature is a screenoverlay, return a screenoverlay icon
      return SCREENOVERLAY_ICON;
    }
    if(aAbstractFeature instanceof TLcdKML22PhotoOverlay ){
      //if the abstract feature is a photooverlay, return a photooverlay icon
      return PHOTOOVERLAY_ICON;
    }
    if ( aAbstractFeature instanceof ILcdShapeList
         && ( ( ILcdShapeList ) aAbstractFeature ).getShapeCount() == 1 ) {
      //if shapelist has a single shape, use that as a reference
      ILcdShape shape = ( ( ILcdShapeList ) aAbstractFeature ).getShape( 0 );

      ELcdKML22ResourceStatus resourceStatus = fResourceProvider.getStyleProvider().getStyleStatus( aAbstractFeature, ELcdKML22StyleState.NORMAL );
      TLcdKML22Style style = null;
      switch ( resourceStatus ) {
        case CACHED:
          style = fResourceProvider.getStyleProvider().retrieveStyle( aAbstractFeature, ELcdKML22StyleState.NORMAL );
          break;
        case NOT_CACHED:
          fResourceProvider.getStyleProvider().retrieveStyle( aAbstractFeature, ELcdKML22StyleState.NORMAL,fResourceListener );
        case LOADING:
          return EMPTY_ICON;
        case FAULTY:
      }

      if ( shape instanceof ILcdPoint ) {
        //if shape is a point, it has a custom icon
        return retrieveIconForPoint( style );
      }
      if ( shape instanceof ILcdPolyline ) {
        //if shape is a polyline, it might have a linestyle
        Color line_color = retrieveLineStyleColor( style, Color.white );
        return new TLcdSymbol( TLcdSymbol.POLYLINE, 15, line_color );
      }
      if ( shape instanceof ILcdSurface || shape instanceof ILcdComplexPolygon || shape instanceof ILcdShapeList ) {
        //if shape can be filled, it might have a polystyle
        boolean filled = isPolyStyleFilled( style, true );
        boolean outlined = isPolyStyleOutlined( style, true );
        int symbol = filled && outlined ? TLcdSymbol.OUTLINED_AREA : filled ? TLcdSymbol.AREA : TLcdSymbol.POLYGON;

        Color line_color = retrieveLineStyleColor( style, Color.white );
        Color fill_color = retrievePolyStyleColor( style, Color.darkGray );
        return new TLcdSymbol( symbol, 15, line_color, fill_color );
      }
    }
    return EMPTY_ICON;
  }

  private Color retrievePolyStyleColor( TLcdKML22Style aStyle, Color aDefaultValue ) {
    TLcdKML22PolyStyle polyStyle = aStyle==null?null:aStyle.getPolyStyle();
    if ( polyStyle != null ) {
      return polyStyle.getColor();
    }
    return aDefaultValue;
  }

  private boolean isPolyStyleOutlined( TLcdKML22Style aStyle, boolean aDefaultValue ) {
    TLcdKML22PolyStyle polyStyle = aStyle==null?null:aStyle.getPolyStyle();
    if ( polyStyle != null ) {
      return polyStyle.getOutline();
    }
    return aDefaultValue;
  }

  private boolean isPolyStyleFilled( TLcdKML22Style aStyle, boolean aDefaultValue ) {
    TLcdKML22PolyStyle polyStyle = aStyle==null?null:aStyle.getPolyStyle();
    if ( polyStyle != null ) {
      return polyStyle.getFill();
    }
    return aDefaultValue;
  }

  private Color retrieveLineStyleColor( TLcdKML22Style aStyle, Color aDefaultColor ) {
    TLcdKML22LineStyle lineStyle = aStyle==null?null:aStyle.getLineStyle();
    if ( lineStyle != null ) {
      return lineStyle.getColor();
    }
    return aDefaultColor;
  }

  /**
   * <p>Uses the {@link TLcdKML22StyleProvider} to obtain an icon, given a style.</p>
   * @param aStyle a valid style.
   * @return a valid icon. This can be either the one provided in the style, or a default icon.
   */
  private ILcdIcon retrieveIconForPoint( TLcdKML22Style aStyle ) {
    TLcdKML22IconStyle iconStyle = aStyle==null?null:aStyle.getIconStyle();
    ILcdIcon icon = null;
    if ( iconStyle != null ) {
      ELcdKML22ResourceStatus resourceStatus = fResourceProvider.getIconProvider().getIconStatus( iconStyle );
      switch ( resourceStatus ) {
        case CACHED:
          icon = fResourceProvider.getIconProvider().retrieveIcon( iconStyle );
          break;
        case NOT_CACHED:
          fResourceProvider.getIconProvider().retrieveIcon( iconStyle,fResourceListener );
        case LOADING:
          return EMPTY_ICON;
        case FAULTY:
      }
    }
    if ( icon == null ) {
      icon = POINT_ICON;
    }
    return icon;
  }

  /**
   * <p>Creates the timer used to trigger a repaint</p>
   */
  private void createRepaintTimer() {
    if ( fTimer == null ) {
      fTimer = new Timer( fTimerDelay, new ActionListener() {
        public void actionPerformed( ActionEvent e ) {
          fTree.repaint();
        }
      } );
      fTimer.setRepeats( false );
      fTimer.start();
    }
  }

  /**
   * <p>Causes the timer to be reset, delaying its original task.</p>
   */
  private void restartTimer() {
    if ( fTree != null ) {
      if ( fTimer == null ) {
        createRepaintTimer();
      }
      fTimer.restart();
    }
  }

  /**
   * <p>Inner class to represent an empty icon</p>
   */
  private static class MyEmptyIcon implements ILcdIcon {
    public void paintIcon( Component aComponent, Graphics aGraphics, int aX, int aY ) {
    }

    public int getIconWidth() {
      return 16;
    }

    public int getIconHeight() {
      return 16;
    }

    public Object clone() {
      MyEmptyIcon clone;
      try {
        clone = ( MyEmptyIcon ) super.clone();
      }
      catch ( CloneNotSupportedException e ) {
        sLogger.error( e.getMessage(), e );
        clone = new MyEmptyIcon();
      }
      return clone;

    }
  }

  /**
   * Listener that is notified when the value of a checkbox changes. It updates the visibility of
   * the element in question, and fires a single event. It propagates visibilities down the tree. It
   * also does so up the tree, but only to make elements visible.
   */
  private class VisibilityActionListener implements ActionListener {
    private WeakReference<JCheckBox> fWeakCheckbox;

    public VisibilityActionListener( JCheckBox aCheckBox ) {
      fWeakCheckbox = new WeakReference<JCheckBox>(aCheckBox);
    }

    public void actionPerformed( ActionEvent e ) {
      JCheckBox checkBox = fWeakCheckbox.get();
      if ( checkBox!=null ) {
        //disable the listener before changing the visibility
        checkBox.removeActionListener( this );
        TreePath path = fTree.getPathForRow( fCurrentAbstractFeatureRow );
        Object modelTreeNode = path.getParentPath().getLastPathComponent();
        ILcdLayer layer  = null;
        if(modelTreeNode instanceof TreeModelObject){
          layer = ( ( TreeModelObject ) modelTreeNode ).getLayer();
          modelTreeNode = ( ( TreeModelObject ) modelTreeNode ).getTreeModelObject();
        }

        if(layer!=null && !checkBox.isSelected()){
          //selection should be cleared when a checkbox was ticked off
          layer.clearSelection( ILcdFireEventMode.FIRE_NOW );
        }

        if(checkBox.isSelected()){
          makeParentsVisible( path.getPath() );
        }

        if ( fCurrentModelTreeNode != null ) {
          TLcdKML22DynamicModel currentModelTreeNode = fCurrentModelTreeNode.get();
          //if the currently selected item is a model tree node, set its visibility
          if (currentModelTreeNode!=null) {
            currentModelTreeNode.setHierarchicalVisibility(checkBox.isSelected());
          }
        } else if (fCurrentAbstractFeature != null) {
          //if the currently selected item is an abstract feature (non-container), set its visibility
          TLcdKML22AbstractFeature currentAbstractFeature = fCurrentAbstractFeature.get();
          if (currentAbstractFeature!=null) {
            currentAbstractFeature.setVisibility(checkBox.isSelected());
          }
          if (!(currentAbstractFeature instanceof ILcdModelTreeNode)) {
            //Signal an element changed event to the parent container to trigger view updates.
            if ( modelTreeNode instanceof TLcdKML22DynamicModel ) {
              final ILcdModelTreeNode staticModel = ( ( TLcdKML22DynamicModel ) modelTreeNode ).getKMLModel();
              staticModel.elementChanged(currentAbstractFeature, ILcdFireEventMode.FIRE_LATER);
              if ( staticModel instanceof TLcdKML22AbstractContainer ) {
                if ( checkBox.isSelected() && !( ( TLcdKML22AbstractContainer ) staticModel ).getVisibility() ) {
                  ( ( TLcdKML22AbstractContainer ) staticModel ).setVisibility( true );
                }
                ( ( TLcdKML22AbstractContainer ) staticModel ).fireCollectedModelChanges( false );
              }
              else if ( staticModel instanceof TLcdKML22Kml ) {
                ( ( TLcdKML22Kml ) staticModel ).fireCollectedModelChanges( false );
              }
            }
            else if ( modelTreeNode == null ) {
              //Special case when kml file does not contain document, just a single feature
              Object lastPathComponent = path.getLastPathComponent();
              if ( lastPathComponent instanceof TreeModelObject ) {
                ILcdModel model = ( ( TreeModelObject ) lastPathComponent ).getModel();
                if ( model instanceof TLcdKML22RenderableModel ) {
                  ( ( ( TLcdKML22RenderableModel ) model ).getDelegateModel().getKMLModel() ).elementChanged( ( ( TreeModelObject ) lastPathComponent ).getTreeModelObject(), ILcdModel.FIRE_NOW );
                }
              }
            }
          }
        }

        // Start timer to repaint tree.
        restartTimer();

        //enable the listener again
        checkBox.addActionListener( this );
      }else{
        if(e.getSource() instanceof JCheckBox){
          ( ( JCheckBox ) e.getSource() ).removeActionListener( this );
        }
      }
    }
  }

  private static class NetworkLinkStateListener implements ILcdStatusListener{
    private WeakReference<ModelContentTreeNodeCellRenderer> fModelContentTreeNodeCellRenderer;

    public NetworkLinkStateListener( ModelContentTreeNodeCellRenderer aModelContentTreeNodeCellRenderer ) {
      fModelContentTreeNodeCellRenderer = new WeakReference<ModelContentTreeNodeCellRenderer>(aModelContentTreeNodeCellRenderer);
    }

    public void statusChanged( TLcdStatusEvent aStatusEvent ) {
      TLcdKML22DynamicModel source = ( TLcdKML22DynamicModel ) aStatusEvent.getSource();
      ModelContentTreeNodeCellRenderer nodeCellRenderer = fModelContentTreeNodeCellRenderer.get();
      if ( nodeCellRenderer!=null ) {
        nodeCellRenderer.fTree.repaint(  );
      }else{
        //the cell renderer is garbage collected, so remove the status listener
        source.removeStatusListener( this );
      }
    }
  }

  /**
   * Animates the busy icons.
   */
  private class MyIconUpdater {

    private Timer fTimer;

    public MyIconUpdater( int aDelay ) {
      fTimer = new Timer( aDelay, new MyBusyAction() );
      fTimer.setRepeats( false );
    }

    public void ensureRepaint() {
      // Don't delay the timer, just ensure a repaint.
      if ( fTree != null ) {
        if ( !fTimer.isRunning() ) {
          fTimer.start();
        }
      }
    }

    private class MyBusyAction implements ActionListener {
      public void actionPerformed( ActionEvent aEvent ) {
        OPEN_FETCHING_ANIMATED_ICON.nextIcon();
        CLOSED_FETCHING_ANIMATED_ICON.nextIcon();
        fTree.repaint();
      }
    }
  }

  private static class MyResourceListener implements ILcdKML22ResourceListener {
    private WeakReference<ModelContentTreeNodeCellRenderer> fModelContentTreeNodeCellRenderer;

    public MyResourceListener( ModelContentTreeNodeCellRenderer aModelContentTreeNodeCellRenderer ) {
      fModelContentTreeNodeCellRenderer = new WeakReference<ModelContentTreeNodeCellRenderer>(aModelContentTreeNodeCellRenderer );
    }

    public void resourceAvailable( TLcdKML22ResourceDescriptor aResourceDescriptor, Object aNewResource ) {
      repaintTree();
    }

    public void resourceNotAvailable( TLcdKML22ResourceDescriptor aResourceDescriptor, String aReason, Exception aException ) {
      repaintTree();
    }

    private void repaintTree() {
      ModelContentTreeNodeCellRenderer nodeCellRenderer = fModelContentTreeNodeCellRenderer.get();
      if(nodeCellRenderer!=null){
        nodeCellRenderer.restartTimer();
      }
    }
  }

  /**
   * A tree selection listener that listens to selections of an element in the tree, so that the
   * element in question can be made visible.
   */
  private class MyTreeSelectionListener implements TreeSelectionListener {
    public void valueChanged( final TreeSelectionEvent e ) {
      if ( ( e.getNewLeadSelectionPath() != null ) && ( e.getNewLeadSelectionPath().getLastPathComponent() != null ) &&
           ( e.getNewLeadSelectionPath().getLastPathComponent() instanceof TreeModelObject ) ) {
        final Object value = e.getNewLeadSelectionPath().getLastPathComponent();
        if ( ( ( ( TreeModelObject ) value ).getTreeModelObject() instanceof TLcdKML22AbstractFeature ) ) {
          TLcdAWTUtil.invokeLater( new Runnable() {
            public void run() {
              ( ( TLcdKML22AbstractFeature ) ( ( TreeModelObject ) value ).getTreeModelObject() ).setVisibility( true );
              makeParentsVisible( e );
            }
          } );
        }
        else if ( ( ( TreeModelObject ) value ).getTreeModelObject() instanceof TLcdKML22PaintableGroundOverlay ) {
          TLcdAWTUtil.invokeLater( new Runnable() {
            public void run() {
              ( ( TLcdKML22PaintableGroundOverlay ) ( ( TreeModelObject ) value ).getTreeModelObject() ).getGroundOverlay().setVisibility( true );
              makeParentsVisible( e );
            }
          } );
        }
      }
    }

    private void makeParentsVisible( TreeSelectionEvent e ) {
      ModelContentTreeNodeCellRenderer.this.makeParentsVisible( e.getNewLeadSelectionPath().getPath() );
      if ( e.getNewLeadSelectionPath().getParentPath().getLastPathComponent() instanceof TreeModelObject &&
           ( ( TreeModelObject ) e.getNewLeadSelectionPath().getParentPath().getLastPathComponent() ).getTreeModelObject() instanceof TLcdKML22DynamicModel ) {
        ( ( TLcdKML22DynamicModel ) ( ( TreeModelObject ) e.getNewLeadSelectionPath().getParentPath().getLastPathComponent() ).getTreeModelObject() ).getKMLModel().elementChanged( ( ( TreeModelObject ) e.getNewLeadSelectionPath().getLastPathComponent() ).getTreeModelObject(), ILcdModel.FIRE_NOW );
      }
      fTree.repaint();
    }
  }

  private static class CheckBoxPanel extends JPanel {
    public JCheckBox fCheckbox;

    public CheckBoxPanel() {
      super( new BorderLayout() );
    }

    public void setCheckbox(JCheckBox aCheckBox){
      add(aCheckBox, BorderLayout.WEST);
      fCheckbox = aCheckBox;
    }

    public JCheckBox getCheckbox() {
      return fCheckbox;
    }
  }
}
