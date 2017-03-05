package com.bavelsoft.broccolies.util;

import java.util.HashMap;

public class HashMapAndLast<K,V> {
	private HashMap<K,V> map = new HashMap<>();
	private V last;

	public void put(K k, V v) {
		map.put(k, v);
		last = v;
	}

	public V get(K k) {
		return map.get(k);
	}

	public V getLast() {
		return last;
	}

	public void clear() {
		map.clear();
		last = null;
	}
}
