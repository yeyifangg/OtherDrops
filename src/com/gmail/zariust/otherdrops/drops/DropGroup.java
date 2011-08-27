package com.gmail.zariust.otherdrops.drops;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.gmail.zariust.otherdrops.options.Action;
import com.gmail.zariust.otherdrops.subject.Target;

public class DropGroup extends CustomDrop {
	private String name;
	private List<SimpleDrop> list = null;

	public DropGroup(Target targ, Action act) {
		super(targ, act);
		setDrops(new ArrayList<SimpleDrop>());
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

	public void setDrops(List<SimpleDrop> drops) {
		this.list = drops;
	}

	public List<SimpleDrop> getDrops() {
		return list;
	}
	
	public void add(SimpleDrop drop) {
		list.add(drop);
	}

	@Override
	public boolean isDefault() {
		return false;
	}

	@Override
	public void run() {
		Set<String> exclusives = new HashSet<String>();
		for(SimpleDrop drop : list) {
			if(!drop.matches(event)) continue;
			if(drop.willDrop(exclusives)) drop.perform(event);
		}
	}
}
