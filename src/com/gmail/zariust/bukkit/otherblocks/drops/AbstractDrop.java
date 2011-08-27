package com.gmail.zariust.bukkit.otherblocks.drops;

import java.util.Random;

import com.gmail.zariust.bukkit.otherblocks.OtherBlocks;
import com.gmail.zariust.bukkit.otherblocks.options.Action;
import com.gmail.zariust.bukkit.otherblocks.options.ConfigOnly;
import com.gmail.zariust.bukkit.otherblocks.subject.Target;

public abstract class AbstractDrop {
	private Target block;
	private Action action;
	protected Random rng;

	public AbstractDrop(Target targ, Action act) {
		block = targ;
		action = act;
		rng = OtherBlocks.rng;
	}
	
	/**
	 * @param diff A flag whose value doesn't matter but whose presence means "validate the target". 
	 */
	protected AbstractDrop(Target targ, Action act, boolean diff) throws DropCreateException {
		this(targ, act);
		if(targ.getClass().isAnnotationPresent(ConfigOnly.class)) {
			ConfigOnly annotate = targ.getClass().getAnnotation(ConfigOnly.class);
			throw new DropCreateException(targ.getClass(), annotate.value());
		}
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
	
	public int getRandom(int limit) {
		return rng.nextInt(limit);
	}
	
	@Override
	public String toString() {
		return action.toString() + " on " + ((block == null) ? "<no block>" : block.toString());
	}
	
	public abstract String getLogMessage();

	public boolean basicMatch(AbstractDrop other) {
		if(!block.matches(other.block)) {
			OtherBlocks.logInfo("AbstractDrop - basicMatch/target - failed. this.target="+block.toString()+" other.target="+other.block.toString(),5);
			return false;
		}
		if(!action.equals(other.action)) {
			OtherBlocks.logInfo("AbstractDrop - basicMatch/action - failed. this.action="+action.toString()+" other.action="+other.action.toString(),5);
			return false;
		}
		return true;
	}
	
	static public int hashCode(Object type, int v, int data) 	{
		short t = type == null ? (short) 0 : (short) type.hashCode();
		return (v << 16) | t | (data << 3);
	}
}
