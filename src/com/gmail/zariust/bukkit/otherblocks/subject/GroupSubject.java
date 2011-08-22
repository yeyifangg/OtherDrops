package com.gmail.zariust.bukkit.otherblocks.subject;

import java.util.Collections;
import java.util.List;

import org.bukkit.Location;

import com.gmail.zariust.bukkit.otherblocks.OtherBlocks;
import com.gmail.zariust.bukkit.otherblocks.options.ConfigOnly;

@ConfigOnly(PlayerSubject.class)
public class GroupSubject implements LivingSubject {
	private String group;

	public GroupSubject(String grp) {
		group = grp;
	}
	
	public String getGroup() {
		return group;
	}
	
	@Override
	public boolean equals(Object other) {
		if(!(other instanceof GroupSubject)) return false;
		GroupSubject targ = (GroupSubject) other;
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
		if(!(other instanceof PlayerSubject)) return false;
		PlayerSubject player = (PlayerSubject) other;
		List<String> playerGroups = OtherBlocks.plugin.getGroups(player.getPlayer());
		return playerGroups.contains(group);
	}

	@Override
	public boolean matches(Agent other) {
		if(!(other instanceof PlayerSubject)) return false;
		PlayerSubject player = (PlayerSubject) other;
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
		return Collections.singletonList((Target) new PlayerSubject());
	}

	@Override
	public String getKey() {
		return null;
	}
}
