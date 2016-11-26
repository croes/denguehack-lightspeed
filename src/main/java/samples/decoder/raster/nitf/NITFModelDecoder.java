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
package samples.decoder.raster.nitf;

import com.luciad.format.raster.TLcdNITFModelDecoder;
import com.luciad.model.ILcdModelDecoder;
import com.luciad.reference.TLcdGeodeticReference;
import com.luciad.reference.TLcdModelReferenceDecoder;
import com.luciad.shape.shape2D.TLcdLonLatBounds;
import com.luciad.util.TLcdSharedBuffer;
import com.luciad.util.service.LcdService;

@LcdService( service = ILcdModelDecoder.class )
public class NITFModelDecoder extends TLcdNITFModelDecoder {

  public NITFModelDecoder() {
    // Create a model decoder. In this example, we also provide a model
    // reference decoder, a default model reference, and default raster bounds,
    // in case a decoded raster doesn't specify them.
    super( TLcdSharedBuffer.getBufferInstance(),
           new TLcdModelReferenceDecoder(),
           new TLcdGeodeticReference(),
           new TLcdLonLatBounds(0, 0, 10, 10) );

    // Allow a raster to be positioned in an approximate orthographic reference
    // that better suits the specified corner points. This is typically useful
    // when decoding aerial imagery that is only positioned approximately.
    setAllowOrthographicReferencing( true );

    // Allow a large residual error when positioning the raster based on its
    // corner points. If the residual error is larger than this value, the
    // raster is rejected with an IOException.
    setMaximumResidualTiePointError( 1000.0 );
  }
}
