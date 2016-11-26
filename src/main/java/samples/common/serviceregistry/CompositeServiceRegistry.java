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
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * <p>Service registry that is composed of other service registries.</p>
 *
 * <p>Note that priorities are not fully respected when using this class. The order in which the
 * iterator returns the services is determined by
 * <ul>
 *   <li>The order in which the delegate service registries were added.</li>
 *   <li>Per delegate service registry: the order in which the services are returned.</li>
 * </ul>
 * </p>
 */
public class CompositeServiceRegistry extends ServiceRegistry {

  private final Collection<ServiceRegistry> fServiceRegistries = new ArrayList<ServiceRegistry>();

  /**
   * Creates a new composite service registry from the given collection of service registries.
   * @param aServiceRegistries the delegate service registries.
   */
  public CompositeServiceRegistry(Collection<? extends ServiceRegistry> aServiceRegistries) {
    fServiceRegistries.addAll(aServiceRegistries);
  }

  /**
   * Creates a new composite service registry from the given collection of service registries.
   * @param aServiceRegistries the delegate service registries.
   */
  public CompositeServiceRegistry(ServiceRegistry... aServiceRegistries) {
    fServiceRegistries.addAll(Arrays.asList(aServiceRegistries));
  }

  @Override
  public <T> Iterable<T> query(Class<T> aClass) {
    // Return a composite iterable of all iterables returned by the delegate service registries
    List<Iterable<T>> delegates = getDelegateIterables(aClass);
    return new CompositeIterable<T>(delegates);
  }

  private <T> List<Iterable<T>> getDelegateIterables(Class<T> aClass) {
    List<Iterable<T>> iterables = new ArrayList<Iterable<T>>();

    for (ServiceRegistry serviceRegistry : fServiceRegistries) {
      Iterable<T> iterable = serviceRegistry.query(aClass);
      if (iterable != null) {
        iterables.add(iterable);
      }
    }
    return iterables;
  }

  private static class CompositeIterable<T> implements Iterable<T> {

    private final List<Iterable<T>> fDelegates;

    public CompositeIterable(List<Iterable<T>> aDelegates) {
      fDelegates = aDelegates;
    }

    @Override
    public Iterator<T> iterator() {
      return new CompositeIterator<T>(fDelegates);
    }
  }

  /**
   * Iterator that composes a list of other iterators.
   */
  private static class CompositeIterator<T> implements Iterator<T> {

    private final List<Iterable<T>> fIterables;
    private int fNextIteratorIndex = 0;
    private Iterator<T> fCurrentIterator = null;

    public CompositeIterator(List<Iterable<T>> aIterables) {
      fIterables = aIterables;
    }

    @Override
    public boolean hasNext() {
      return hasNextImpl();
    }

    @Override
    public T next() {
      if (!hasNextImpl()) {
        throw new NoSuchElementException("hasNext() returns false. next() should not be called.");
      }
      return fCurrentIterator.next();
    }

    private boolean hasNextImpl() {
      while (fCurrentIterator == null || !fCurrentIterator.hasNext()) {
        if (fNextIteratorIndex >= fIterables.size()) {
          return false;
        }
        fCurrentIterator = fIterables.get(fNextIteratorIndex).iterator();
        fNextIteratorIndex++;
      }
      return true;
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException("Removing elements using this iterator is not supported, use ServiceRegistry methods instead.");
    }
  }

}
