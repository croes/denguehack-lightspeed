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
package samples.geometry.util;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import com.luciad.shape.ILcdComplexPolygon;
import com.luciad.shape.ILcdPolygon;
import com.luciad.shape.TLcdFeaturedShapeList;
import com.luciad.view.gxy.ILcdGXYLabelPainter;
import com.luciad.view.gxy.ILcdGXYLabelPainterProvider;
import com.luciad.view.gxy.ILcdGXYPainter;
import com.luciad.view.gxy.ILcdGXYPainterProvider;
import com.luciad.view.gxy.TLcdGXYDataObjectLabelPainter;
import com.luciad.view.gxy.TLcdGXYLabelPainter;
import com.luciad.view.gxy.TLcdGXYPainterColorStyle;
import com.luciad.view.gxy.TLcdGXYShapePainter;
import com.luciad.view.gxy.painter.ALcdGXYAreaPainter;

/**
 * This painter provider will draw all the objects of one or more layers in different colors depending
 * on the relation the objects are set to. The relation of each object has to be set after topological
 * relations of the objects were calculated. This painter provider can be added to multiple layers.
 * All objects of the layer must be TLcdFeaturedShapeList instances.
 */
public class TopologyPainterProvider implements ILcdGXYPainterProvider, ILcdGXYLabelPainterProvider {

  //colors to represent the various relations
  public static final Color COLOR_DEFAULT = Color.white;
  public static final Color COLOR_SELECTED = Color.green;
  public static final Color COLOR_CROSSES = Color.yellow;
  public static final Color COLOR_TOUCHES = Color.red;
  public static final Color COLOR_CONTAIN_WITHIN = Color.blue;
  public static final Color COLOR_INTERSECTS = Color.cyan;

  private final TLcdGXYShapePainter fShapePainter = new TLcdGXYShapePainter();
  private TLcdGXYLabelPainter fPolygonLabelPainter = createPolygonLabelPainter();
  private TLcdGXYLabelPainter fPolylineLabelPainter = createPolylineLabelPainter();

  private final Map<Object, Relation> fRelations = new HashMap<Object, Relation>();

  /**
   * This represents the relationship of this object with the currently selected object. It
   * determines the color with which the object will be painted.
   */
  public enum Relation {
    DEFAULT, //disjoint relation or no relation at all
    SELECTED, //it's a selected object
    TOUCHES, //touches selected object
    CROSSES, //crosses selected object
    CONTAIN_WITHIN,  //contains or within selected object
    INTERSECTS, //not disjoint to (intersects) selected object. This is overridden by touches or crosses.
  }

  public TopologyPainterProvider() {
    fShapePainter.setAntiAliased(true);
    fShapePainter.setSelectionMode(ALcdGXYAreaPainter.OUTLINED);
  }

  private static boolean isFilledShape(TLcdFeaturedShapeList aShapeList) {
    return aShapeList.getShapeCount() > 0 && (aShapeList.getShape( 0 ) instanceof ILcdPolygon || aShapeList.getShape( 0 ) instanceof ILcdComplexPolygon );
  }

  public ILcdGXYPainter getGXYPainter( Object aObject ) {

    Relation relation = fRelations.containsKey( aObject ) ? fRelations.get(aObject) : Relation.DEFAULT;
    setPainterColor( getRelationColor( relation ) );

    fShapePainter.setObject(aObject);
    return fShapePainter;
  }

  public ILcdGXYLabelPainter getGXYLabelPainter( Object aObject ) {
    Relation relation = fRelations.containsKey( aObject ) ? fRelations.get(aObject) : Relation.DEFAULT;
    setLabelPainterColor( getRelationColor(relation ));

    if(isFilledShape( (TLcdFeaturedShapeList)aObject)) {
      fPolygonLabelPainter.setObject( aObject );
      return fPolygonLabelPainter;
    } else {
      fPolylineLabelPainter.setObject( aObject );
      return fPolylineLabelPainter;
    }
  }

  @SuppressWarnings("CloneDoesntDeclareCloneNotSupportedException")
  public Object clone() {
    try {
      return super.clone();
    } catch ( CloneNotSupportedException e ) {
      throw new RuntimeException( e );
    }
  }

  public void setPolygonLabelPainter( TLcdGXYLabelPainter aPainter ) {
    fPolygonLabelPainter = aPainter;
  }

  public void setPolylineLabelPainter( TLcdGXYLabelPainter aPainter ) {
    fPolylineLabelPainter = aPainter;
  }

  //convenience method to get a slightly darker color
  public static Color multiplyColor( Color aColor, double aFactor ) {
    return new Color( ( int ) ( aColor.getRed() * aFactor ), ( int ) ( aColor.getGreen() * aFactor ), ( int ) ( aColor.getBlue() * aFactor ) );
  }

  private void setPainterColor( Color aColor ) {
    fShapePainter.setLineStyle( new TLcdGXYPainterColorStyle(  multiplyColor(aColor, 0.8), COLOR_SELECTED ) );
    fShapePainter.setFillStyle( new TLcdGXYPainterColorStyle(  multiplyColor(aColor, 0.6), COLOR_SELECTED ) );
  }

  private void setLabelPainterColor( Color aColor ) {
    fPolygonLabelPainter.setForeground( aColor );
    fPolygonLabelPainter.setSelectionColor( aColor );
    fPolygonLabelPainter.setHaloColor( Color.black );
    fPolygonLabelPainter.setWithPin( false );
    fPolylineLabelPainter.setForeground( aColor );
    fPolylineLabelPainter.setSelectionColor( aColor );
    fPolylineLabelPainter.setHaloColor( Color.black );
    fPolylineLabelPainter.setWithPin( false );
  }

  private Color getRelationColor(Relation aRelation) {
    switch ( aRelation ) {
      case DEFAULT:
        return COLOR_DEFAULT;
      case SELECTED:
        return COLOR_SELECTED;
      case INTERSECTS:
        return COLOR_INTERSECTS;
      case TOUCHES:
        return COLOR_TOUCHES;
      case CROSSES:
        return COLOR_CROSSES;
      case CONTAIN_WITHIN:
        return COLOR_CONTAIN_WITHIN;
      default:
        return Color.black;
    }
  }

  /**
   * Set the topological relation the object currently has. The result is that the object will be assigned
   * a new color depending on the relation.
   * @param aRelation The relation to set this object to.
   * @param aObject An object that is part of a layer that has been registered.
   */
  public void setRelation( Relation aRelation, Object aObject ) {
    fRelations.put(aObject, aRelation);
  }

  /**
   * Clear the relation of all objects to Relation.DEFAULT.
   */
  public void clearRelations() {
    fRelations.clear();
  }

  public static TLcdGXYLabelPainter createPolygonLabelPainter( String... aPropertyNamesForLabel ) {
    TLcdGXYDataObjectLabelPainter gxy_label_painter = new TLcdGXYDataObjectLabelPainter();
    if ( aPropertyNamesForLabel != null ) gxy_label_painter.setExpressions( aPropertyNamesForLabel );
    gxy_label_painter.setFrame( true );
    gxy_label_painter.setFilled( false );
    gxy_label_painter.setHaloEnabled( true );
    gxy_label_painter.setHaloThickness( 1 );
    return gxy_label_painter;
  }

  public static TLcdGXYLabelPainter createPolylineLabelPainter( String... aPropertyNamesForLabel ) {
    TLcdGXYDataObjectLabelPainter gxy_label_painter = new TLcdGXYDataObjectLabelPainter();
    if ( aPropertyNamesForLabel != null ) gxy_label_painter.setExpressions( aPropertyNamesForLabel );
    gxy_label_painter.setHaloEnabled( true );
    gxy_label_painter.setHaloThickness( 1 );
    return gxy_label_painter;
  }
}
