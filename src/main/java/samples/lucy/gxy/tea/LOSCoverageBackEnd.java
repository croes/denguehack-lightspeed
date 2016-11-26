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
package samples.lucy.gxy.tea;

import com.luciad.lucy.ILcyLucyEnv;
import com.luciad.lucy.addons.tea.loscoverage.TLcyLOSCoverageBackEnd;
import com.luciad.lucy.util.properties.ALcyProperties;
import com.luciad.tea.ILcdLOSPropagationFunction;
import com.luciad.tea.ILcdMatrixRasterValueMapper;

/**
 * Sample back-end extension, adding an extra propagation function.
 */
public class LOSCoverageBackEnd extends TLcyLOSCoverageBackEnd {

  /**
   * The property name for our custom propagation function.
   */
  public static final String PROPAGATION_CUSTOM_ID = "customPropagation";

  public LOSCoverageBackEnd(ILcyLucyEnv aLucyEnv) {
    super(aLucyEnv);
  }

  @Override
  public ILcdLOSPropagationFunction createPropagationFunction(ALcyProperties aProperties) {
    if (PROPAGATION_CUSTOM_ID.equals(aProperties.getString(PROPAGATION_FUNCTION_KEY, ""))) {
      return new LOSPropagationFunction();
    }
    return super.createPropagationFunction(aProperties);
  }

  @Override
  public String retrievePropagationFunctionLabel(String aPropagationFunctionKey) {
    if (PROPAGATION_CUSTOM_ID.equals(aPropagationFunctionKey)) {
      return "Custom Propagation";
    }
    return super.retrievePropagationFunctionLabel(aPropagationFunctionKey);
  }

  @Override
  public ILcdMatrixRasterValueMapper createPropagationMatrixRasterValueMapper(String aPropagationFunctionKey) {
    if (PROPAGATION_CUSTOM_ID.equals(aPropagationFunctionKey)) {
      // the normal mapper will do just fine
      return super.createPropagationMatrixRasterValueMapper(TLcyLOSCoverageBackEnd.PROPAGATION_FUNCTION_VALUE_NORMAL);
    }
    return super.createPropagationMatrixRasterValueMapper(aPropagationFunctionKey);
  }
}
