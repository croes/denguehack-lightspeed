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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ServiceConfigurationError;

import com.luciad.util.logging.ILcdLogger;
import com.luciad.util.logging.TLcdLoggerFactory;
import com.luciad.util.service.TLcdServiceLoader;

/**
 * This {@linkplain ServiceRegistry service registry} is based on {@code com.luciad.util.service.TLcdServiceLoader}.
 */
public class ServiceLoaderRegistry extends ServiceRegistry {

  private static ILcdLogger sLogger = TLcdLoggerFactory.getLogger(ServiceLoaderRegistry.class);

  private ThreadLocal<Map<Class<?>, TLcdServiceLoader<?>>> fServiceLoaderCache = new ThreadLocal<Map<Class<?>, TLcdServiceLoader<?>>>() {
    @Override
    protected Map<Class<?>, TLcdServiceLoader<?>> initialValue() {
      return new HashMap<Class<?>, TLcdServiceLoader<?>>();
    }
  };

  @Override
  public <T> Iterable<T> query(Class<T> aClass) {
    return new ServiceLoaderIterable<T>(this, aClass);
  }

  private <T> Iterator<T> getIterator(Class<T> aClass) {
    List<T> services = collectServices(aClass);
    return services.iterator();
  }

  private <T> List<T> collectServices(Class<T> aClass) {
    Iterator<T> services = retrieveServiceLoader(aClass).iterator();
    List<T> result = new ArrayList<T>();
    while (true) {
      try {
        if (services.hasNext()) {
          T service = services.next();
          result.add(service);
        } else {
          break;
        }
      } catch (ServiceConfigurationError e) {
        // log the error and continue with the other providers. This way, an error in one provider does
        // not prevent the other providers from being created.
        sLogger.error("Service could not be instantiated: " + e.getMessage(), e);
      }
    }
    return result;
  }

  @SuppressWarnings("unchecked")
  private synchronized <T> TLcdServiceLoader<T> retrieveServiceLoader(Class<T> aClass) {
    // Cache the service loader to make sure we don't need to read/parse the META-INF/service files over and over
    // Note that because TLcdServiceLoader is NOT thread-safe, we use a thread local map to cache it.
    Map<Class<?>, TLcdServiceLoader<?>> serviceLoaderCache = fServiceLoaderCache.get();
    TLcdServiceLoader<T> serviceLoader = (TLcdServiceLoader<T>) serviceLoaderCache.get(aClass);
    if (serviceLoader == null) {
      serviceLoader = TLcdServiceLoader.getInstance(aClass);
      serviceLoaderCache.put(aClass, serviceLoader);
    }
    return serviceLoader;
  }

  private static class ServiceLoaderIterable<T> implements Iterable<T> {

    private final ServiceLoaderRegistry fServiceRegistry;
    private final Class<T> fClass;

    private ServiceLoaderIterable(ServiceLoaderRegistry aServiceRegistry, Class<T> aClass) {
      fServiceRegistry = aServiceRegistry;
      fClass = aClass;
    }

    @Override
    public Iterator<T> iterator() {
      return fServiceRegistry.getIterator(fClass);
    }
  }
}
