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
package samples.encoder.sqlite.geopackage;

import com.luciad.format.sqlite.geopackage.TLcdGeoPackageModelEncoder;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelDecoder;
import com.luciad.model.TLcdCompositeModelDecoder;
import com.luciad.util.ILcdStatusListener;
import com.luciad.util.TLcdStatusEvent;

import samples.common.serviceregistry.ServiceLoaderRegistry;

/**
 * This utility decodes an input file and encodes it in a new GeoPackage file.
 * <p/>
 * Usage: <code>java samples.encoder.sqlite.geopackage.Converter</code> &lt;inputfile&gt; &lt;gpkg file&gt;
 */
public class Converter {

  private static final TLcdCompositeModelDecoder modelDecoder = new TLcdCompositeModelDecoder(new ServiceLoaderRegistry().query(ILcdModelDecoder.class));

  public static void main(String[] aArgs) throws Exception {
    if (aArgs.length != 2) {
      usage("Missing arguments");
      return;
    }

    String source = aArgs[0];
    String destination = aArgs[1];

    ILcdModel model = modelDecoder.decode(source);

    if (model == null) {
      usage("Could not decode " + source);
      return;
    }

    TLcdGeoPackageModelEncoder modelEncoder = new TLcdGeoPackageModelEncoder();
    modelEncoder.addStatusListener(new ProgressDisplay());
    modelEncoder.export(model, destination);
  }

  private static void usage(String aErrorMessage) {
    System.err.println(aErrorMessage);
    System.err.println("Decodes a vector file and encodes it in a new SQLite SpatiaLite table.");
    System.err.println("Arguments: <input-file-name> <destination-file-name>");
    System.err.println("The destination file should have the gpkg extension.");
    System.err.println("Supported input file types:");
    for (ILcdModelDecoder decoder : modelDecoder.getModelDecoders()) {
      System.err.println("   - " + decoder.getDisplayName());
    }
  }

  private static class ProgressDisplay implements ILcdStatusListener {
    private double fProgress;

    @Override
    public void statusChanged(TLcdStatusEvent aStatusEvent) {
      switch (aStatusEvent.getID()) {
      case TLcdStatusEvent.START_BUSY:
        System.out.print("Converting: .");
        break;
      case TLcdStatusEvent.PROGRESS:
        double value = aStatusEvent.getValue();
        while (value - fProgress >= 0.1) {
          System.out.print('.');
          fProgress += 0.1;
        }
        break;
      case TLcdStatusEvent.END_BUSY:
        System.out.println();
      }
    }
  }
}
