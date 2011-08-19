package com.gmail.zariust.bukkit.otherblocks.options.drop;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Location;

public class ExclusiveDropGroup extends DropType {
	private List<DropType> group;
	
	public ExclusiveDropGroup(DropType... drops) {
		super(DropCategory.GROUP);
		group = Arrays.asList(drops);
	}
	
	public List<DropType> getGroup() {
		return group;
	}

	@Override
	protected void performDrop(Location where, DropFlags flags) {
		DropType drop = group.get(flags.rng.nextInt(group.size()));
		drop.drop(where, 1, flags.recipient, flags.naturally, flags.spread, flags.rng);
	}
}
