package com.gmail.zariust.otherdrops.subject;

import java.util.Collections;
import java.util.List;

import org.bukkit.Location;

import com.gmail.zariust.otherdrops.OtherDrops;
import com.gmail.zariust.otherdrops.options.ConfigOnly;

@ConfigOnly(PlayerSubject.class)
public class GroupSubject extends LivingSubject {
	private String group;

	public GroupSubject(String grp) {
		super(null);
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
	public boolean matches(Subject other) {
		if(!(other instanceof PlayerSubject)) return false;
		PlayerSubject player = (PlayerSubject) other;
		List<String> playerGroups = OtherDrops.plugin.getGroups(player.getPlayer());
		return playerGroups.contains(group);
	}

	@Override
	public ItemCategory getType() {
		return ItemCategory.PLAYER;
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

	@Override
	public String toString() {
		if(group == null) return "PLAYERGROUP"; // shouldn't happen though
		return "PLAYERGROUP@" + group;
	}
}
