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
package samples.ogc.wfs.server;

import com.luciad.model.ILcdModelReferenceFormatter;
import com.luciad.ogc.common.ILcdOGCModelDecoderFactory;
import com.luciad.ogc.common.ILcdOGCModelProvider;
import com.luciad.ogc.common.TLcdOGCServiceException;
import com.luciad.ogc.ows.model.TLcdOWSAddress;
import com.luciad.ogc.ows.model.TLcdOWSContact;
import com.luciad.ogc.ows.model.TLcdOWSOnlineResource;
import com.luciad.ogc.ows.model.TLcdOWSResponsiblePartySubset;
import com.luciad.ogc.ows.model.TLcdOWSServiceProvider;
import com.luciad.ogc.wfs.ALcdOGCWFSCommandDispatcherFactory;
import com.luciad.ogc.wfs.ILcdWFSClientModelEncoderFactory;
import com.luciad.ogc.wfs.ILcdWFSFeatureTypeList;
import com.luciad.ogc.wfs.ILcdWFSFeatureTypeListDecoder;
import com.luciad.ogc.wfs.ILcdWFSFilteredModelFactory;
import com.luciad.ogc.wfs.ILcdWFSServerModelEncoderFactory;
import com.luciad.ogc.wfs.ILcdWFSServiceMetaData;

/**
 * An example implementation of <code>ALcdOGCWFSCommandDispatcherFactory</code>.
 */
public class WFSCommandDispatcherFactory extends ALcdOGCWFSCommandDispatcherFactory {

  /**
   * Creates the model decoder factory used to integrate with a data backend.
   * This example uses an implementation that is capable to read SHP and GML 3.1 files from disk.
   */
  @Override
  protected ILcdOGCModelDecoderFactory createModelDecoderFactory() {
    return new WFSModelDecoderFactory();
  }

  /**
   * Creates an example service provider instance to document the service provider section in the WFS capabilities.
   */
  @Override
  public TLcdOWSServiceProvider createServiceProvider() {
    final TLcdOWSServiceProvider provider = new TLcdOWSServiceProvider();
    provider.setProviderName("Luciad");
    final TLcdOWSOnlineResource site = new TLcdOWSOnlineResource();
    site.setHref("www.luciad.com");
    provider.setProviderSite(site);
    final TLcdOWSResponsiblePartySubset responsible = new TLcdOWSResponsiblePartySubset();
    final TLcdOWSContact contact = new TLcdOWSContact();
    final TLcdOWSAddress address = new TLcdOWSAddress();
    address.setCountry("Belgium");
    address.setPostalCode("3001");
    contact.setAddress(address);
    responsible.setContactInfo(contact);
    responsible.setIndividualName("YourName");
    provider.setServiceContact(responsible);
    return provider;
  }

  /**
   * Creates an example service metadata instance to document the service metadata section in the WFS capabilities.
   */
  @Override
  protected ILcdWFSServiceMetaData createServiceMetaData() {
    return new WFSServiceMetaData();
  }

  /**
   * Creates the model encoder factory to send data to a client.
   * This sample can encode results to GML and GeoJson.  If you only need to serve data in GML,
   * you can return null here.
   */
  @Override
  protected ILcdWFSClientModelEncoderFactory createClientModelEncoderFactory() {
    //retrieve the model reference formatter.
    //this way, we can configure the ModelEncoder with this formatter.
    ILcdModelReferenceFormatter formatter = this.getModelReferenceFormatter();
    return new WFSClientModelEncoderFactory(formatter);
  }

  /**
   * Create the model encoder factory used for committing transactions back to the data store.
   */
  @Override
  protected ILcdWFSServerModelEncoderFactory createServerModelEncoderFactory() {
    return new WFSServerModelEncoderFactory();
  }

  /**
   * Creates a feature type list decoder, i.e. the class which is responsible for loading the list
   * of feature types (in this case, SHP files) that will be offered by the WFS.
   */
  @Override
  protected ILcdWFSFeatureTypeListDecoder createWFSFeatureTypeListDecoder(ILcdOGCModelProvider aModelProvider) {
    return new WFSFeatureTypeListDecoder(aModelProvider);
  }

  /**
   * Creates a filtered model factory, i.e. the factory that is used to filter models based on
   * client request parameters. This sample uses an extension of the default implementation,
   * to add a custom OGC Filter function.
   */
  @Override
  protected ILcdWFSFilteredModelFactory createFilteredModelFactory(ILcdWFSFeatureTypeList aFeatureTypeList, ILcdOGCModelProvider aModelProvider) throws TLcdOGCServiceException {
    return new WFSFilteredModelFactory(aFeatureTypeList, aModelProvider);
  }

  /**
   * Example implementation of service metadata for usage in the WFS capabilities.
   */
  private static class WFSServiceMetaData implements ILcdWFSServiceMetaData {
    public String getName() {
      return "WFS";
    }

    public String getTitle() {
      return "LuciadLightspeed Web Feature Service";
    }

    public String getAbstract() {
      return "OGC compliant Web Feature Service implementation powered by Luciad";
    }

    public String getFees() {
      return "none";
    }

    public int getKeywordCount() {
      return 1;
    }

    public String getKeyword(int aIndex) throws IndexOutOfBoundsException {
      if (aIndex == 0) {
        return "Luciad";
      } else {
        throw new IndexOutOfBoundsException("Only 1 keyword specified");
      }
    }

    public int getAccessConstraintCount() {
      return 0;
    }

    public String getAccessConstraint(int aIndex) throws IndexOutOfBoundsException {
      throw new IndexOutOfBoundsException("No access constraints specified");
    }
  }
}
