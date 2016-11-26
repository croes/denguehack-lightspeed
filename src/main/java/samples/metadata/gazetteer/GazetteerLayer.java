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
package samples.metadata.gazetteer;

import com.luciad.model.ILcdModel;
import com.luciad.view.gxy.TLcdGXYDataObjectLabelPainter;
import com.luciad.view.gxy.TLcdGXYLayer;
import com.luciad.view.map.TLcdGeodeticPen;

/**
 * A layer which contains bounded wrappers for metadata objects.
 */
class GazetteerLayer extends TLcdGXYLayer {

  public GazetteerLayer( ILcdModel aModel ) {
    setLabel( "Gazetteer" );
    setModel( aModel );
    setGXYPainterProvider( new MetadataBoundedPainter() );
    setEditable( false );
    setSelectable( true );
    TLcdGeodeticPen pen = new TLcdGeodeticPen();
    pen.setStraightLineMode( false );
    setGXYPen( pen );
    setSelectionLabeled( true );
    setVisible( true );
    TLcdGXYDataObjectLabelPainter labelPainter = new TLcdGXYDataObjectLabelPainter();
    labelPainter.setExpressions( "identificationInfo.AbstractMD_Identification.citation.CI_Citation.title.CharacterString.value" );
    setGXYLabelPainterProvider( labelPainter );
  }
}
