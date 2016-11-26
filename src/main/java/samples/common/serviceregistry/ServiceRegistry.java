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
package samples.common.serviceregistry;

import java.util.Arrays;

import com.luciad.util.service.LcdService;

/**
 * A service registry can be used to query services given a class.
 * By default it is populated using the META-INF/services directory.
 *
 * @see com.luciad.util.service.TLcdServiceLoader
 * @see LcdService
 */
public abstract class ServiceRegistry {

  private static EditableServiceRegistry sInstance = null;

  /**
   * Returns a singleton instance of the ServiceLoader registry.
   * @return a singleton instance of the ServiceLoader registry.
   */
  public static synchronized EditableServiceRegistry getInstance() {
    if ( sInstance == null ) {
      sInstance = new MyServiceRegistry();
    }
    return sInstance;
  }

  /**
   * Instance of ServiceRegistry that composes
   * <ul>
   *   <li>an editable, in-memory service registry, that can be used to register services at runtime</li>
   *   <li>a (non-editable) service loader registry, that loads services from the META-INF/services</li>
   * </ul>
   */
  private static final class MyServiceRegistry extends EditableServiceRegistry {

    private EditableServiceRegistry fEditableServiceRegistry = new CollectionServiceRegistry();
    private ServiceRegistry fCompositeServiceRegistry = new CompositeServiceRegistry(Arrays.asList(
        fEditableServiceRegistry,     // Can be used to register services at runtime
        new ServiceLoaderRegistry()   // Reads services from the META-INF/services
                                                                                                  ));

    @Override
    public void register(Object aService) {
      fEditableServiceRegistry.register(aService);
    }

    @Override
    public void register(Object aService, int aPriority) {
      fEditableServiceRegistry.register(aService, aPriority);
    }

    @Override
    public void unregister(Object aService) {
      fEditableServiceRegistry.unregister(aService);
    }

    @Override
    public <T> Iterable<T> query(Class<T> aClass) {
      return fCompositeServiceRegistry.query(aClass);
    }
  }

  /**
   * High priority.
   */
  public static final int HIGH_PRIORITY = LcdService.HIGH_PRIORITY;
  /**
   * Default priority.
   */
  public static final int DEFAULT_PRIORITY = LcdService.DEFAULT_PRIORITY;
  /**
   * Low priority.
   */
  public static final int LOW_PRIORITY = LcdService.LOW_PRIORITY;
  /**
   * Fallback priority.
   */
  public static final int FALLBACK_PRIORITY = LcdService.FALLBACK_PRIORITY;

  /**
   * Retrieves all instances of the given class, registered in this service registry, as an iterable.
   * Note that the content of the returned <code>Iterable</code> can change when services are
   * added or removed from this service registry.
   *
   * @param aClass the class for which to return an iterable.
   *
   * @return all instances of the given class.
   */
  public abstract <T> Iterable<T> query(Class<T> aClass);
}
