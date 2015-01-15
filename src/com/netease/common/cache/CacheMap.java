package com.netease.common.cache;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CacheMap <K, V>{
	private Map<K, Entry<K, V>> mCache = new ConcurrentHashMap<K, Entry<K, V>>();
	private ReferenceQueue<V> mQueue = new ReferenceQueue<V>();
	 
	private static class Entry<K, V> extends SoftReference<V> {
		K mKey;
		
		public Entry(K key, V value, ReferenceQueue<V> queue) {
			super(value, queue);
			mKey = key;
		}
	}
	
	private void cleanUpWeakMap() {
        Entry<K, V> entry = (Entry<K, V>) mQueue.poll();
        while (entry != null) {
        	mCache.remove(entry.mKey);
            entry = (Entry<K, V>) mQueue.poll();
        }
    }

    public boolean containsKey(K key) {
        cleanUpWeakMap();
        return mCache.containsKey(key);
    }

    public V put(K key, V value) {
        cleanUpWeakMap();
        Entry<K, V> entry = mCache.put(
                key, new Entry<K, V>(key, value, mQueue));
        return entry == null ? null : entry.get();
    }

    public V get(K key) {
        cleanUpWeakMap();
        Entry<K, V> entry = mCache.get(key);
        return entry == null ? null : entry.get();
    }

    public void clear() {
        mCache.clear();
    }
    
    public int size(){
    	return mCache.size();
    }
}
