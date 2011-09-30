package com.gmail.zariust.otherdrops.event;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.gmail.zariust.otherdrops.data.Data;
import com.gmail.zariust.otherdrops.options.Flag;

public class DropsList implements Iterable<CustomDropEvent> {
	private List<CustomDropEvent> list;
	private Map<String,Map<Data,Double>> keys;

	public DropsList() {
		list = new ArrayList<CustomDropEvent>();
	}
	
	@Override
	public String toString() {
		return list.toString();
	}
	
	@Override
	public int hashCode() {
		return list.hashCode();
	}
	
	@Override
	public boolean equals(Object other) {
		if(!(other instanceof DropsList)) return false;
		return list.equals(((DropsList)other).list);
	}

	public void add(CustomDropEvent drop) {
		list.add(drop);
	}

	@Override
	public Iterator<CustomDropEvent> iterator() {
		return list.listIterator();
	}

	public void sort() {
		// If we want to apply other sorting to the drops list, here is the place to do so.
		Collections.sort(list, new UniqueSorter());
		// We also build up the exclusive keys data here
		keys = new HashMap<String,Map<Data,Double>>();
		for(CustomDropEvent event : list) {
			String key = event.getExclusiveKey();
			if(!keys.containsKey(key)) {
				keys.put(key, new HashMap<Data,Double>());
				keys.get(key).put(null, 0.0);
			}
			Data data = event.getTarget().getData();
			if(!keys.get(key).containsKey(data)) keys.get(key).put(data, 0.0);
			double cumul = keys.get(key).get(data) + event.getChance();
			keys.get(key).put(data, cumul);
		}
		for(String key : keys.keySet()) {
			double cumul = keys.get(key).containsKey(null) ? keys.get(key).get(null) : 0.0;
			for(Data data : keys.get(key).keySet()) {
				keys.get(key).put(data, cumul + keys.get(key).get(data));
				if(keys.get(key).get(data) < 100) keys.get(key).put(data, 100.0);
			}
		}
	}
	
	public double getExclusiveTotal(String key, Data data) {
		if(!keys.containsKey(key)) return 0;
		if(!keys.get(key).containsKey(data)) return keys.get(key).get(null);
		return keys.get(key).get(data);
	}
	
	public class UniqueSorter implements Comparator<CustomDropEvent> {
		@Override
		public int compare(CustomDropEvent lhs, CustomDropEvent rhs) {
			boolean leftUnique = lhs.hasFlag(Flag.UNIQUE);
			boolean rightUnique = rhs.hasFlag(Flag.UNIQUE);
			if(leftUnique == rightUnique) return 0;
			else if(leftUnique) return -1;
			else if(rightUnique) return 1;
			return 0;
		}
	}
}
