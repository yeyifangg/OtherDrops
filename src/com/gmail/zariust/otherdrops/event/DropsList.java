package com.gmail.zariust.otherdrops.event;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
}
