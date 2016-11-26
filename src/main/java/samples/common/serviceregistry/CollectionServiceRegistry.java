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

import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.luciad.util.concurrent.TLcdLockUtil;

/**
 * This {@linkplain ServiceRegistry service registry} is based on an in-memory collection of
 * services. It is possible to {@linkplain #register(Object) register} and {@linkplain
 * #unregister(Object) unregister} services.
 */
public class CollectionServiceRegistry extends EditableServiceRegistry {

  private final Object fRegistryLock = new ReentrantReadWriteLock();
  private final Map<Class<?>, Collection<Object>> fRegistry = new HashMap<Class<?>, Collection<Object>>();

  private final Object fPriorityLock = new ReentrantReadWriteLock();
  private final Map<Object, Integer> fPriorities = new IdentityHashMap<Object, Integer>();

  @Override
  public void register(Object aService) {
    registerImpl(aService, DEFAULT_PRIORITY);
  }

  @Override
  public void register(Object aService, int aPriority) {
    registerImpl(aService, aPriority);
  }

  @Override
  public void unregister(Object aService) {
    unregisterImpl(aService);
  }

  @Override
  public <T> Iterable<T> query(Class<T> aClass) {
    return new CollectionIterable<T>(this, aClass);
  }

  private void registerImpl(Object aService, int aPriority) {
    registerPriorityImpl(aService, aPriority);
    registerServiceImpl(aService);
  }

  private void registerPriorityImpl(Object aService, int aPriority) {
    TLcdLockUtil.writeLock(fPriorityLock);
    try {
      fPriorities.put(aService, aPriority);
    } finally {
      TLcdLockUtil.writeUnlock(fPriorityLock);
    }
  }

  private void registerServiceImpl(Object aService) {
    TLcdLockUtil.writeLock(fRegistryLock);
    try {
      Collection<Class<?>> types = getAllTypes(aService.getClass());
      for (Class<?> type : types) {
        registerServiceImpl(fRegistry, type, aService);
      }
    } finally {
      TLcdLockUtil.writeUnlock(fRegistryLock);
    }
  }

  private boolean registerServiceImpl(Map<Class<?>, Collection<Object>> aClassServiceMap, Class<?> aType, Object aService) {
    Collection<Object> list = aClassServiceMap.get(aType);
    if (list == null) {
      list = new ArrayList<Object>();
      aClassServiceMap.put(aType, list);
    }
    return list.add(aService);
  }

  private void unregisterImpl(Object aService) {
    unregisterPriorityImpl(aService);
    unregisterServiceImpl(aService);
  }

  private void unregisterPriorityImpl(Object aService) {
    TLcdLockUtil.writeLock(fPriorityLock);
    try {
      fPriorities.remove(aService);
    } finally {
      TLcdLockUtil.writeUnlock(fPriorityLock);
    }
  }

  private void unregisterServiceImpl(Object aService) {
    TLcdLockUtil.writeLock(fRegistryLock);
    try {
      Collection<Class<?>> types = getAllTypes(aService.getClass());
      for (Class<?> type : types) {
        unregisterServiceImpl(fRegistry, type, aService);
      }
    } finally {
      TLcdLockUtil.writeUnlock(fRegistryLock);
    }
  }

  private boolean unregisterServiceImpl(Map<Class<?>, Collection<Object>> aClassServiceMap, Class<?> aType, Object aService) {
    Collection<Object> list = aClassServiceMap.get(aType);
    return list != null && list.remove(aService);
  }

  private <T> Iterator<T> retrieveIterator(Class<T> aClass) {
    TLcdLockUtil.readLock(fRegistryLock);
    TLcdLockUtil.readLock(fPriorityLock);
    try {
      // Retrieve all services
      Collection<Object> services = fRegistry.get(aClass);
      if (services == null || services.isEmpty()) {
        return Collections.<T>emptyList().iterator();
      }

      List<T> sortedServices = new ArrayList<T>();
      for (Object service : services) {
        //noinspection unchecked
        sortedServices.add((T) service);
      }

      // Sort these services according to priorities
      Collections.sort(sortedServices, new Comparator<T>() {
        @Override
        public int compare(T o1, T o2) {
          Integer priority1 = fPriorities.get(o1);
          Integer priority2 = fPriorities.get(o2);
          int diff = (priority1 == null ? FALLBACK_PRIORITY : priority1) -
                     (priority2 == null ? FALLBACK_PRIORITY : priority2);
          if (diff != 0) {
            return diff;
          }

          // Make sure we have a consistent ordering
          diff = o1.getClass().getName().compareTo(o2.getClass().getName());
          if (diff != 0) {
            return diff;
          }
          return o1.hashCode() - o2.hashCode();
        }
      });

      return sortedServices.iterator();
    } finally {
      TLcdLockUtil.readUnlock(fPriorityLock);
      TLcdLockUtil.readUnlock(fRegistryLock);
    }
  }

  private static Collection<Class<?>> getAllTypes(Class<?> aClass) {
    // Collect all types that the given class implements/extends
    Set<Class<?>> types = new HashSet<Class<?>>();
    LinkedList<Class<?>> toVisit = new LinkedList<Class<?>>();
    toVisit.add(aClass);

    while (!toVisit.isEmpty()) {
      Class<?> type = toVisit.removeFirst();
      if (types.add(type)) {
        Class<?> superClass = type.getSuperclass();
        if (superClass != null) {
          toVisit.add(superClass);
        }
        Class<?>[] interfaces = type.getInterfaces();
        Collections.addAll(toVisit, interfaces);
      }
    }
    return types;
  }

  private static class CollectionIterable<T> implements Iterable<T> {

    private final CollectionServiceRegistry fServiceRegistry;
    private final Class<T> fClass;

    private CollectionIterable(CollectionServiceRegistry aServiceRegistry, Class<T> aClass) {
      fServiceRegistry = aServiceRegistry;
      fClass = aClass;
    }

    @Override
    public Iterator<T> iterator() {
      return fServiceRegistry.retrieveIterator(fClass);
    }
  }
}
