package com.gmail.zariust.bukkit.otherblocks.options.target;

import java.util.List;

import com.gmail.zariust.bukkit.otherblocks.OtherBlocks;
import com.gmail.zariust.bukkit.otherblocks.options.ConfigOnly;
import com.gmail.zariust.bukkit.otherblocks.options.drop.ItemType;

@ConfigOnly(PlayerTarget.class)
public class GroupTarget implements Target {
	private String group;

	public GroupTarget(String grp) {
		group = grp;
	}
	
	public String getGroup() {
		return group;
	}
	
	@Override
	public boolean equals(Object other) {
		if(!(other instanceof GroupTarget)) return false;
		GroupTarget targ = (GroupTarget) other;
		return group.equals(targ.group);
	}
	
	@Override
	public int hashCode() {
		return group.hashCode();
	}

	@Override
	public boolean overrideOn100Percent() {
		return false;
	}
	
	@Override
	public boolean matches(Target other) {
		if(!(other instanceof PlayerTarget)) return false;
		PlayerTarget player = (PlayerTarget) other;
		List<String> playerGroups = OtherBlocks.plugin.getGroups(player.getPlayer());
		return playerGroups.contains(group);
	}

	@Override
	public ItemType getType() {
		return ItemType.PLAYER;
	}
}