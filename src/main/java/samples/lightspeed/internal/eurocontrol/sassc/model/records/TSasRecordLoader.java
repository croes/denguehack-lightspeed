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
package samples.lightspeed.internal.eurocontrol.sassc.model.records;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import samples.lightspeed.internal.eurocontrol.sassc.TSasEnvironmentFactory;
import samples.lightspeed.internal.eurocontrol.sassc.TSasParameters;

/**
 *
 */
public class TSasRecordLoader {

  private static final String MAX_NB_EXP = "1000";

  public static void loadAll(boolean aClear) throws IOException, FileNotFoundException {

    //First load all the definitions
    Map<String, TSasDbExpressionDefinition> definitionMap = new HashMap<String, TSasDbExpressionDefinition>();
    Properties properties = new Properties();
    properties.load(new FileInputStream(getParameters().getDbExpressionDefinitionsPathName()));
    int maxDbExpressions = parseMaxDbExpressionsProperty(properties);

    for (int i = 0; i < maxDbExpressions; i++) {
      if (TSasDbExpressionDefinition.containsDefinition(properties, i)) {
        TSasDbExpressionDefinition definition = TSasDbExpressionDefinition.load(properties, i);
        boolean containsDefinition = false;
        for (TSasDbExpressionDefinition processedDefinition : definitionMap.values()) {
          containsDefinition |= definition.getName().equals(processedDefinition.getName());
        }
        if (!containsDefinition) {
          definitionMap.put(definition.getName(), definition);
        }
      }
    }

    //Then load the records by type
    TSasRadarRecord.load(definitionMap, aClear);
    TSasTrackRecord.load(definitionMap, aClear);
    TSasADSRecord.load(definitionMap, aClear);
    TSasMLATRecord.load(definitionMap, aClear);
  }

  public static void clearAll() {
    TSasRadarRecord.clear();
    TSasTrackRecord.clear();
    TSasADSRecord.clear();
    TSasMLATRecord.clear();
  }

  private static TSasParameters getParameters() {
    return TSasEnvironmentFactory.getInstance().getEnvironment().getParameters();
  }

  private static int parseMaxDbExpressionsProperty(Properties aProperties) {
    int maxDbExpressions;
    try {
      maxDbExpressions = Integer.parseInt(aProperties.getProperty("MAX_NB_EXP", MAX_NB_EXP));
    } catch (NumberFormatException e) {
      //TODO: add some exception throwing here
      maxDbExpressions = Integer.parseInt(MAX_NB_EXP);
    }
    return maxDbExpressions;
  }
}
