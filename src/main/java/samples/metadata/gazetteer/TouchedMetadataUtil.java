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

import com.luciad.format.metadata.model.metadataentityset.TLcdISO19115Metadata;
import com.luciad.gui.TLcdUserDialog;
import com.luciad.util.ILcdFormatter;
import com.luciad.view.gxy.ILcdGXYLayer;
import com.luciad.view.gxy.ILcdGXYPainter;
import com.luciad.view.gxy.ILcdGXYView;
import com.luciad.view.gxy.TLcdGXYContext;

import java.awt.Component;
import java.util.Enumeration;
import java.util.Vector;

/**
 * Utility class to find the bounded metadata object which is touched by the mouse when a mouse event
 * is fired.
 */
class TouchedMetadataUtil {

  private MetadataBoundedFormatter fMetadataBoundedFormatter = new MetadataBoundedFormatter();

  public TLcdISO19115Metadata findTouchedMetadata( int aX, int aY, ILcdGXYView aGXYView ) {

    TLcdGXYContext context = new TLcdGXYContext();
    Enumeration<?> layers = aGXYView.layers();
    Vector<MetadataBounded> touched_metadata_bounded = new Vector<MetadataBounded>();
    while ( layers.hasMoreElements() ) {
      ILcdGXYLayer layer = (ILcdGXYLayer) layers.nextElement();
      if ( layer instanceof GazetteerLayer ) {
        context.resetFor( layer, aGXYView );
        context.setX( aX );
        context.setY( aY );
        Enumeration<?> model_objects = layer.getModel().elements();
        while ( model_objects.hasMoreElements() ) {
          MetadataBounded metadata_bounded = (MetadataBounded) model_objects.nextElement();
          ILcdGXYPainter painter = layer.getGXYPainter( metadata_bounded );
          if ( painter.isTouched( null, ILcdGXYPainter.BODY, context ) ) {
            touched_metadata_bounded.addElement( metadata_bounded );
          }
        }
      }
    }
    if ( touched_metadata_bounded.size() > 1 ) {
      MetadataBounded metadata_bounded = (MetadataBounded) TLcdUserDialog.choose(
              touched_metadata_bounded,
              fMetadataBoundedFormatter,
              "Select a metadata object",
              null,
              (Component)aGXYView );
      if(metadata_bounded != null) {
        return metadata_bounded.getMetadata();
      }
      else {
        return null;
      }
    }
    else if (touched_metadata_bounded.size() == 1) {
      MetadataBounded metadata_bounded = (MetadataBounded) touched_metadata_bounded.elementAt(0);
      return metadata_bounded.getMetadata();
    } else {
      return null;
    }
  }

  private class MetadataBoundedFormatter implements ILcdFormatter {

    public String format(Object aObject) {
      return ((MetadataBounded)aObject).getDisplayName();
    }
  }

}
