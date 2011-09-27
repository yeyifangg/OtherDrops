package com.gmail.zariust.otherdrops.event;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import com.gmail.zariust.otherdrops.options.Flag;

public class DropsList implements Iterable<CustomDropEvent> {
	private List<CustomDropEvent> list;

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
		Comparator<CustomDropEvent> unique = new UniqueSorter();
		Collections.sort(list, unique);
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
