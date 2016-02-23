package com.ecchilon.sadpanda.api;

import android.util.LruCache;
import rx.Observable;

public class ObservableLruCache<K, V> {

	private final LruCache<K, V> cache;

	public ObservableLruCache(int maxSize) {
		cache = new LruCache<>(maxSize);
	}

	public Observable<V> get(K key) {
		V value = cache.get(key);
		return value != null ? Observable.just(value) : Observable.empty();
	}

	public void put(K key, V value) {
		cache.put(key, value);
	}

	public V remove(K key) {
		return cache.remove(key);
	}
}
