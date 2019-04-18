package io.github.paul1365972.rhythmofnature.server.util;

import java.util.HashMap;
import java.util.Set;

public class BiMap<K1, K2> {
	
	private HashMap<K1, K2> aMap;
	private HashMap<K2, K1> bMap;
	
	public BiMap() {
		aMap = new HashMap<>();
		bMap = new HashMap<>();
	}
	
	public void put(K1 a, K2 b) {
		this.aMap.put(a, b);
		this.bMap.put(b, a);
	}
	
	public K2 getA(K1 a) {
		return aMap.get(a);
	}
	
	public K1 getB(K2 b) {
		return bMap.get(b);
	}
	
	public boolean containsA(K1 a) {
		return aMap.containsKey(a);
	}
	
	public boolean containsB(K2 b) {
		return bMap.containsKey(b);
	}
	
	public K2 removeA(K1 a) {
		K2 removed = aMap.remove(a);
		if (removed != null)
			bMap.remove(removed);
		return removed;
	}
	
	public K1 removeB(K2 b) {
		K1 removed = bMap.remove(b);
		if (removed != null)
			aMap.remove(removed);
		return removed;
	}
	
	public Set<K1> keySetA() {
		return aMap.keySet();
	}
	
	public Set<K2> keySetB() {
		return bMap.keySet();
	}
	
	public int size() {
		return aMap.size();
	}
	
	public void clear() {
		aMap.clear();
		bMap.clear();
	}
	
}