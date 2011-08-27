package com.gmail.zariust.otherdrops.event;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.gmail.zariust.otherdrops.options.Action;
import com.gmail.zariust.otherdrops.subject.Target;

public class GroupDropEvent extends CustomDropEvent {
	private String name;
	private List<SimpleDropEvent> list = null;

	public GroupDropEvent(Target targ, Action act) {
		super(targ, act);
		setDrops(new ArrayList<SimpleDropEvent>());
	}

	public void setName(String newName) {
		this.name = newName;
	}

	public String getName() {
		return name;
	}
	
	@Override
	public String getDropName() {
		return "Dropgroup " + name;
	}

	public void setDrops(List<SimpleDropEvent> drops) {
		this.list = drops;
	}

	public List<SimpleDropEvent> getDrops() {
		return list;
	}
	
	public void add(SimpleDropEvent drop) {
		list.add(drop);
	}

	@Override
	public boolean isDefault() {
		return false;
	}

	@Override
	public void run() {
		Set<String> exclusives = new HashSet<String>();
		for(SimpleDropEvent drop : list) {
			if(!drop.matches(event)) continue;
			if(drop.willDrop(exclusives)) drop.perform(event);
		}
	}
}
