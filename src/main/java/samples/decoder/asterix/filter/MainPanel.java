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
package samples.decoder.asterix.filter;

import java.io.IOException;

import com.luciad.datamodel.ILcdDataObject;
import com.luciad.datamodel.expression.ILcdDataObjectExpression;
import com.luciad.datamodel.expression.TLcdDataObjectExpressionContext;
import com.luciad.datamodel.expression.TLcdDataObjectExpressionLanguage;
import com.luciad.format.asterix.ALcdASTERIXDecoder;
import com.luciad.format.asterix.ALcdASTERIXTransformationProvider;
import com.luciad.format.asterix.ILcdASTERIXRecordFilter;
import com.luciad.format.asterix.TLcdASTERIXDataTypes;
import com.luciad.format.asterix.TLcdASTERIXModelDecoder;
import com.luciad.model.ILcdModel;
import com.luciad.view.gxy.ILcdGXYLayer;

import samples.decoder.asterix.ASTERIXLayerFactory;
import samples.decoder.asterix.AbstractSample;
import samples.gxy.common.layers.GXYLayerUtil;

/**
 * This sample shows how to filter an ASTERIX file while it is being decoded.
 * <p/>
 * It loads a shipped example data set: atx_fake62_cart.astfin. It rejects all tracks with a track
 * number of 30 or more. Try loading that same file in the decoder sample, and see how much less
 * trajectories are shown in this sample. The actual filtering is performed by the
 * TrackNumberRecordFilter class.
 * <p/>
 * Since ASTERIX data usually is relative with respect to a radar, the positions of the radars must
 * be specified. These positions can be entered in the locations.cfg file. If you have your own
 * ASTERIX data you wish to load, you will need to add the locations of your radars to this file.
 */
public class MainPanel extends AbstractSample {

  private TLcdASTERIXModelDecoder fModelDecoder;

  protected void initializeDecoders() {
    ALcdASTERIXTransformationProvider transformationProvider = getTransformationProvider();
    fModelDecoder = new TLcdASTERIXModelDecoder();
    if (transformationProvider != null) {
      fModelDecoder.setTransformationProvider(transformationProvider);
    }
    initializeRecordFilter(fModelDecoder);
  }

  private void initializeRecordFilter(ALcdASTERIXDecoder aDecoder) {
    // Specify the filter that should be used during decoding
    aDecoder.setRecordFilter(new TrackNumberRecordFilter());
  }

  @Override
  public void addData() throws IOException {
    super.addData();

    ASTERIXLayerFactory lf = new ASTERIXLayerFactory();
    // Decode data and add it to the map
    String fileName = "" + "Data/ASTERIX/atx_fake62_cart.astfin";
    ILcdModel model = fModelDecoder.decode(fileName);
    ILcdGXYLayer layer = lf.createGXYLayer(model);
    GXYLayerUtil.addGXYLayer(getView(), layer);

    // Fit on the data
    GXYLayerUtil.fitGXYLayer(getView(), layer);
  }

  /**
   * Record filter that:
   * - Only accepts category 62 tracks with a track number less than 30.
   * - Accepts all other data.
   *
   * This is of course not a real-world scenario, as is using fake data. But this filter
   * does show the possibilities, and can easily be adapted.
   */
  private static class TrackNumberRecordFilter implements ILcdASTERIXRecordFilter {
    // Expression was created using the datamodelviewer sample: select Category62Type and
    // browse for the track number property. It is specified to be an integer.
    private final ILcdDataObjectExpression fTrackNumberExpression =
        new TLcdDataObjectExpressionLanguage().compile("TrackNumber");

    @Override
    public boolean accept(ILcdDataObject aASTERIXRecord) {
      // If we have tracks from category 62
      if (TLcdASTERIXDataTypes.Category62TrackType.equals(aASTERIXRecord.getDataType())) {
        // Fetch the track number if available
        Integer trackNumber = (Integer) fTrackNumberExpression.evaluate(
            new TLcdDataObjectExpressionContext(aASTERIXRecord));

        // Only keep tracks that specify their track number to be less than 30
        return trackNumber != null && trackNumber < 30;
      } else {
        // Accept other data, for example other categories
        return true;
      }
    }
  }

  // Main method
  public static void main(final String[] aArgs) {
    startSample(MainPanel.class, "ASTERIX filtering");
  }
}
