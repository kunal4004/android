package za.co.woolworths.financial.services.android.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public abstract class MultiMap<K, V> {

	private Map<K, Collection<V>> entries = new LinkedHashMap<>();

	public void put(K key, V value) {
		Collection<V> values = entries.get(key);
		if (values == null) {
			entries.put(key, values = newValueCollection());
		}
		values.add(value);
	}

	// other methods
	// ..

	abstract Collection<V> newValueCollection();

	// Helper methods to create different flavors of MultiMaps

	public static <K, V> MultiMap<K, V> create() {
		return new MultiMap<K, V>() {
			Collection<V> newValueCollection() {
				return new ArrayList<V>();
			}
		};
	}

	public static <K, V> MultiMap<K, V> newHashSetMultiMap() {
		return new MultiMap<K, V>() {
			Collection<V> newValueCollection() {
				return new HashSet<V>();
			}
		};
	}

	public Map<K, Collection<V>> getEntries() {
		return entries;
	}


	public Collection<Collection<V>> values() {
		return entries.values();
	}


	public Set<K> keySet() {
		return entries.keySet();
	}
}