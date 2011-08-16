package com.gmail.zariust.bukkit.otherblocks.options;

public abstract class Action {
	public final static Action BREAK = null;
	public final static Action LEFT_CLICK = null;
	public final static Action RIGHT_CLICK = null;
	
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
}
