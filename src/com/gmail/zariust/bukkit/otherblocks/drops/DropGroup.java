package com.gmail.zariust.bukkit.otherblocks.drops;

import java.util.ArrayList;
import java.util.List;

import com.gmail.zariust.bukkit.otherblocks.options.Action;
import com.gmail.zariust.bukkit.otherblocks.options.Target;

public class DropGroup extends AbstractDrop {
	private String name;
	private double chance;
	private List<CustomDrop> list = null;

	public DropGroup(Target targ, Action act) {
		super(targ, act);
		list = new ArrayList<CustomDrop>();
	}

	@Override
	public boolean matches(AbstractDrop other) {
		if(other instanceof OccurredDrop) {
			OccurredDrop drop = (OccurredDrop) other;
		}
		return false;
	}
}
