package com.gmail.zariust.bukkit.otherblocks.options.drop;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public class SimpleDropGroup extends DropType {
	private List<DropType> group;
	
	public SimpleDropGroup(DropType... drops) {
		super(DropCategory.GROUP);
		group = Arrays.asList(drops);
	}
	
	public List<DropType> getGroup() {
		return group;
	}

	@Override
	protected void performDrop(Location where, Player recipient, boolean naturally, boolean spread) {
		for(DropType drop : group)
			drop.drop(where, 1, recipient, naturally, spread);
	}

}
