package com.gmail.zariust.bukkit.otherblocks.subject;

import java.util.Collections;
import java.util.List;

import org.bukkit.Location;

import com.gmail.zariust.bukkit.otherblocks.OtherBlocks;
import com.gmail.zariust.bukkit.otherblocks.droptype.ItemType;
import com.gmail.zariust.bukkit.otherblocks.options.ConfigOnly;

@ConfigOnly(PlayerAgent.class)
public class GroupTarget implements LivingAgent {
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
		if(!(other instanceof PlayerAgent)) return false;
		PlayerAgent player = (PlayerAgent) other;
		List<String> playerGroups = OtherBlocks.plugin.getGroups(player.getPlayer());
		return playerGroups.contains(group);
	}

	@Override
	public boolean matches(Agent other) {
		if(!(other instanceof PlayerAgent)) return false;
		PlayerAgent player = (PlayerAgent) other;
		List<String> playerGroups = OtherBlocks.plugin.getGroups(player.getPlayer());
		return playerGroups.contains(group);
	}

	@Override
	public ItemType getType() {
		return ItemType.PLAYER;
	}

	@Override
	public void damage(int amount) {}

	@Override
	public void damageTool(short amount) {}

	@Override
	public void damageTool() {}

	@Override
	public Location getLocation() {
		return null;
	}

	@Override
	public List<Target> canMatch() {
		return Collections.singletonList((Target) new PlayerAgent());
	}

	@Override
	public String getKey() {
		return null;
	}
}
