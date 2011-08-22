package com.gmail.zariust.bukkit.otherblocks.subject;

import java.util.List;

public interface Target {
	ItemType getType();

	abstract boolean overrideOn100Percent();

	boolean matches(Target block);
	
	List<Target> canMatch();

	String getKey();
}
