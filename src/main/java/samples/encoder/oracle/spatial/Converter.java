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
package samples.encoder.oracle.spatial;

import com.luciad.format.oracle.spatial.TLcdOracleSpatialModelEncoder;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelDecoder;
import com.luciad.model.ILcdModelEncoder;
import com.luciad.model.TLcdCompositeModelDecoder;

import samples.common.serviceregistry.ServiceLoaderRegistry;

/**
 * This utility decodes a model from a source file (SHP, MIF, MAP, or ORA) and
 * exports it to a new Oracle Spatial Object-Relational table.
 * <p>
 * The spatial table is specified by an ORA properties file.
 * <p>
 * Usage: java samples.encoder.oracle.spatial.Converter source_file ora_file
 * <p>
 * You can remove the newly created table, say 'STATES', using the following
 * SQL statements:
 * <p>
 * <pre>
 * DELETE FROM user_sdo_geom_metadata WHERE table_name = 'STATES'
 * DROP TABLE states
 * </pre>
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

    ILcdModelEncoder modelEncoder = new TLcdOracleSpatialModelEncoder();
    modelEncoder.export(model, destination);
  }

  private static void usage(String aErrorMessage) {
    System.err.println(aErrorMessage);
    System.err.println("Decodes a vector file and encodes it in a new Oracle Spatial Object-Relational table.");
    System.err.println("Arguments: <input-file-name> <destination-file-name>");
    System.err.println("The destination file should have the ora extension.");
    System.err.println("Supported input file types:");
    for (ILcdModelDecoder decoder : modelDecoder.getModelDecoders()) {
      System.err.println("   - " + decoder.getDisplayName());
    }
  }
}
