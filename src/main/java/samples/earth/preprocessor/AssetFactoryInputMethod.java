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
package samples.earth.preprocessor;

import java.io.File;

import com.luciad.format.raster.ILcdMultilevelRaster;
import com.luciad.model.ILcdModel;
import com.luciad.model.ILcdModelDecoder;

/**
 * An interface used by the {@link AssetFactory} to retrieve
 * input.
 */
public interface AssetFactoryInputMethod {

  /**
   * Returns a model decoder for the specified file from a list.
   *
   * @param aFile          the file that will be decoded
   * @param aModelDecoders the candidate model decoders
   *
   * @return a model decoder or <code>null</code> to skip the file.
   */
  public ILcdModelDecoder chooseModelDecoder(File aFile, ILcdModelDecoder[] aModelDecoders);

  /**
   * Chooses the maximum raster level
   *
   * @param aFile   the source file
   * @param aModel  the model
   * @param aRaster the raster
   *
   * @return the maximum raster level.
   */
  public int chooseRasterLevel(File aFile, ILcdModel aModel, ILcdMultilevelRaster aRaster);

}