package com.gmail.zariust.bukkit.otherblocks.options.action;

import org.bukkit.util.config.ConfigurationNode;

public abstract class Action {
	// TODO: This is currently just a thrown-together skeleton; needs major fleshing out
	public final static Action BREAK = new Action() {
		@Override
		public int hashCode() {
			return 0;
		}
	};
	public final static Action LEFT_CLICK = new Action() {
		@Override
		public int hashCode() {
			return 42;
		}
	};
	public final static Action RIGHT_CLICK = new Action() {
		@Override
		public int hashCode() {
			return -42;
		}
	};
	
	@Override
	public boolean equals(Object other) {
		if(!(other instanceof Action)) return false;
		return this == other;
	}
	
	@Override
	public abstract int hashCode();
	
	public static Action fromInteract(org.bukkit.event.block.Action action) {
		switch(action) {
		case LEFT_CLICK_AIR:
		case LEFT_CLICK_BLOCK:
			return LEFT_CLICK;
		case RIGHT_CLICK_AIR:
		case RIGHT_CLICK_BLOCK:
			return RIGHT_CLICK;
		default:
			return null;
		}
	}

	public static Action parseFrom(ConfigurationNode dropNode) {
		// TODO: Genericize
		String action = dropNode.getString("action", "BREAK");
		if(action.equalsIgnoreCase("BREAK")) return BREAK;
		else if(action.equalsIgnoreCase("LEFT_CLICK")) return LEFT_CLICK;
		else if(action.equalsIgnoreCase("RIGHT_CLICK")) return RIGHT_CLICK;
		else return null;
	}
}
