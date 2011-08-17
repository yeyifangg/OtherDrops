package com.gmail.zariust.bukkit.otherblocks.options.target;

public class GroupTarget extends Target {
	private String group;

	public GroupTarget(String grp) {
		super(TargetType.PLAYER);
		group = grp;
	}
	
	public String getPlayer() {
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
}
