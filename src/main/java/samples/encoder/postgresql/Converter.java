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
package samples.encoder.postgresql;

import com.luciad.format.postgresql.TLcdPostGISModelEncoder;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelDecoder;
import com.luciad.model.ILcdModelEncoder;
import com.luciad.model.TLcdCompositeModelDecoder;

import samples.common.serviceregistry.ServiceLoaderRegistry;

/**
 * This utility decodes a shape file and encodes it in a new PostgreSQL PostGIS
 * table. The source file is specified by its filename. The source file must
 * contain a valid primary key feature, it should be "not null" and "unique".
 * The destination table is specified by an <code>pgs</code> property file.
 * <p/>
 * Usage: <code>java samples.encoder.postgresql.Converter</code> &lt;shp_file|pgs_file&gt; &lt;pgs_file&gt;
 * <p/>
 * The JDBC driver to access the PostgreSQL database (org.postgresql.Driver)
 * must be present in the class path.
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

    ILcdModelEncoder modelEncoder = new TLcdPostGISModelEncoder();
    modelEncoder.export(model, destination);
  }

  private static void usage(String aErrorMessage) {
    System.err.println(aErrorMessage);
    System.err.println("Decodes a vector file and encodes it in a new PostgreSQL PostGIS table");
    System.err.println("Arguments: <input-file-name> <destination-file-name>");
    System.err.println("The destination file should have the pgs extension.");
    System.err.println("Supported input file types:");
    for (ILcdModelDecoder decoder : modelDecoder.getModelDecoders()) {
      System.err.println("   - " + decoder.getDisplayName());
    }
  }
}
