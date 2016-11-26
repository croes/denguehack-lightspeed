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
package samples.ogc.wcs.server;

import java.io.IOException;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import com.luciad.io.TLcdIOUtil;
import com.luciad.ogc.common.ILcdInitializationConfig;
import com.luciad.ogc.wcs.ALcdOGCWCSCommandDispatcherFactory;
import com.luciad.ogc.wcs.ILcdWCSServiceMetadata;
import com.luciad.ogc.wcs.model.ILcdCoverageOfferingList;
import com.luciad.util.logging.ILcdLogger;
import com.luciad.util.logging.TLcdLoggerFactory;

/**
 * An example implementation of <code>ALcdOGCWCSCommandDispatcherFactory</code>.
 */
public class WCSCommandDispatcherFactory extends ALcdOGCWCSCommandDispatcherFactory {

  private static ILcdLogger sLogger = TLcdLoggerFactory.getLogger(WCSCommandDispatcherFactory.class.getName());

  /**
   * Load the coverage offering list from a configuration file.
   */
  protected ILcdCoverageOfferingList createCoverageOfferingList(ILcdInitializationConfig aConfig) {
    try {
      // Create a coverage provider, which will contain / specify the coverages offered by this WCS.
      WCSCoverageProvider coverageProvider = createCoverageProvider();

      // Read the configuration file specifying the data sources.
      String configurationFile = aConfig.getParameter("wcs.coverages.cfg");
      SAXBuilder builder = new SAXBuilder();
      Document xmlDocument = null;
      try {
        TLcdIOUtil ioUtil = new TLcdIOUtil();
        ioUtil.setSourceName(configurationFile);
        xmlDocument = builder.build(ioUtil.retrieveInputStream());
      } catch (JDOMException e) {
        sLogger.error("Could not parse the WCS configuration file " + configurationFile, e);
      } catch (IOException e) {
        sLogger.error("Could not read the WCS configuration file " + configurationFile, e);
      }

      if (xmlDocument != null) {
        Element xmlRoot = xmlDocument.getRootElement();
        List coverages = xmlRoot.getChildren("CoverageOffering");
        for (Object coverage : coverages) {
          Element coverageElement = (Element) coverage;
          String coverageName = coverageElement.getAttributeValue("name");
          String coverageSource = getCoverageSourceFrom(coverageElement);
          coverageProvider.addCoverage(coverageSource, coverageName);
        }
      }

      return coverageProvider;
    } catch (IOException e) {
      sLogger.error("Could not create the WCS coverage offering list", e);
      return null;
    }
  }

  /**
   * Return a new instance of WCSCoverageProvider. This method is protected to allow you
   * to override the default behavior of WCSCoverageProvider.
   *
   * @return a WCSCoverageProvider
   */
  protected WCSCoverageProvider createCoverageProvider() {
    return new WCSCoverageProvider();
  }

  protected String getCoverageSourceFrom(Element aCoverageElement) {
    return aCoverageElement.getTextTrim();
  }

  /**
   * Create a service metadata object. This object provides the information returned in the
   * "service" section of the WCS capabilities.
   */
  protected ILcdWCSServiceMetadata createServiceMetaData(ILcdInitializationConfig aConfig) {
    return new WCSServiceMetadata();
  }
}
