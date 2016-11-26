package com.luciad.dengue.util;

import com.luciad.internal.util.TLinCollections;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class LRUCache<K, V> implements Map<K, V> {
  private final LinkedHashMap<K, V> fMap;
  private int fMaxEntries;
  private LRUCache.OnRemoveCallback<K, V> fRemoveCallback;

  public LRUCache(int aMaxEntries) {
    this.setMaxEntries(aMaxEntries);
    this.fMap = new LinkedHashMap<K, V>(TLinCollections.getOptimalInitialCapacity(aMaxEntries, 0.75F), 0.75F, true) {
      protected boolean removeEldestEntry(Map.Entry<K, V> aEldest) {
        boolean remove = LRUCache.this.shouldRemoveEntry();
        if(remove) {
          LRUCache.this.onRemoveEntry(aEldest.getKey(), aEldest.getValue());
        }

        return remove;
      }
    };
  }

  public void setMaxEntries(int aMaxEntries) {
    if(aMaxEntries <= 0) {
      throw new IllegalArgumentException("Cannot set the maximum number of entries of this LRU cache to the given value [" + aMaxEntries + "]");
    } else {
      this.fMaxEntries = aMaxEntries;
    }
  }

  public int getMaxEntries() {
    return this.fMaxEntries;
  }

  protected synchronized boolean shouldRemoveEntry() {
    return this.fMap.size() > this.fMaxEntries;
  }

  protected void onRemoveEntry(K aKey, V aValue) {
    if(this.fRemoveCallback != null) {
      this.fRemoveCallback.onRemoveEntry(aKey, aValue);
    }

  }

  public LRUCache.OnRemoveCallback<K, V> getOnRemoveCallback() {
    return this.fRemoveCallback;
  }

  public void setOnRemoveCallback(LRUCache.OnRemoveCallback<K, V> aOnRemoveCallback) {
    this.fRemoveCallback = aOnRemoveCallback;
  }

  public V get(Object key) {
    return this.fMap.get(key);
  }

  public boolean containsKey(Object key) {
    return this.fMap.containsKey(key);
  }

  public void clear() {
    for(Entry<K, V> entry : entrySet()) {
      this.onRemoveEntry(entry.getKey(), entry.getValue());
    }
    this.fMap.clear();
  }

  public int size() {
    return this.fMap.size();
  }

  public V put(K key, V value) {
    return this.fMap.put(key, value);
  }

  public void putAll(Map<? extends K, ? extends V> m) {
    this.fMap.putAll(m);
  }

  public V remove(Object key) {
    V value = this.get(key);
    if(value != null) {
      this.onRemoveEntry((K)key, value);
      this.fMap.remove(key);
    }

    return value;
  }

  public Set<K> keySet() {
    return this.fMap.keySet();
  }

  public Set<Entry<K, V>> entrySet() {
    return this.fMap.entrySet();
  }

  public Collection<V> values() {
    return this.fMap.values();
  }

  public boolean isEmpty() {
    return this.fMap.isEmpty();
  }

  public boolean containsValue(Object aValue) {
    return this.fMap.containsValue(aValue);
  }

  public interface OnRemoveCallback<K, V> {
    void onRemoveEntry(Object var1, V var2);
  }
}
