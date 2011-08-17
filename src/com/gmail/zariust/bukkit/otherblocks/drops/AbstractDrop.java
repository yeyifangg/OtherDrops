package com.gmail.zariust.bukkit.otherblocks.drops;

import java.util.Random;

import org.bukkit.Location;

import com.gmail.zariust.bukkit.otherblocks.options.action.Action;
import com.gmail.zariust.bukkit.otherblocks.options.target.Target;

public abstract class AbstractDrop {
	private Target block;
	private Action action;
	private Location location;
	
	protected static Random rng = new Random();

	public AbstractDrop(Target targ, Action act) {
		block = targ;
		action = act;
	}
	
	public abstract boolean matches(AbstractDrop other);

	public void setTarget(Target targ) {
		this.block = targ;
	}

	public Target getTarget() {
		return block;
	}

	public void setAction(Action act) {
		this.action = act;
	}

	public Action getAction() {
		return action;
	}

	public boolean basicMatch(AbstractDrop other) {
		if(!block.equals(other.block)) return false;
		if(!action.equals(other.action)) return false;
		return true;
	}
}
