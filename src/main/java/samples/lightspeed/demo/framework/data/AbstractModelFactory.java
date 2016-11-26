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
package samples.lightspeed.demo.framework.data;

import java.io.IOException;
import java.util.Properties;

import com.luciad.model.ILcdModel;

/**
 * Base class for model factory objects, which are responsible for creating models of a certain type.
 */
public abstract class AbstractModelFactory {

  private String fType;

  protected AbstractModelFactory(String aType) {
    fType = aType;
  }

  /**
   * Returns the type of models that can be created by the model factory.
   * @return the type of models that can be created by the model factory
   */
  public String getType() {
    return fType;
  }

  /**
   * Creates an ILcdModel.
   *
   * @param aSource the file path to the data, can be null if no source data is required
   * @return an instance of ILcdModel
   */
  public abstract ILcdModel createModel(String aSource) throws IOException;

  /**
   * Configures the model factory with given properties.
   *
   * Model factories that rely on user defined properties can overwrite this method to initialize
   * those properties. The properties contained by the given Properties object are those that are
   * defined in the xml file of the associated dataset.
   *
   * @param aProperties the properties used to configure the model factory
   */
  public void configure(Properties aProperties) {
    // Does nothing by default
  }
}
