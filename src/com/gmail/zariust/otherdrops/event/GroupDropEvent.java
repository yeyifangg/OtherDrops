package com.gmail.zariust.otherdrops.event;

import java.util.Map;

import com.gmail.zariust.otherdrops.options.Action;
import com.gmail.zariust.otherdrops.subject.Target;

public class GroupDropEvent extends CustomDropEvent {
	private String name;
	private DropsList list = null;

	public GroupDropEvent(Target targ, Action act) {
		super(targ, act);
		setDrops(new DropsList());
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

	public void setDrops(DropsList drops) {
		this.list = drops;
	}

	public DropsList getDrops() {
		return list;
	}
	
	public void add(CustomDropEvent drop) {
		list.add(drop);
	}

	@Override
	public boolean isDefault() {
		return false;
	}
	
	public void sort() {
		list.sort();
	}

	@Override
	public void run() {
		Map<String,ExclusiveKey> exclusives = CustomDropEvent.newExclusiveMap(list);
		for(CustomDropEvent drop : list) {
			if(!drop.matches(currentEvent)) continue;
			if(drop.willDrop(exclusives)) drop.perform(currentEvent);
		}
	}
}
