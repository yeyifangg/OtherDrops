package com.gmail.zariust.otherdrops.event;

import java.util.Random;

import com.gmail.zariust.otherdrops.OtherDrops;
import com.gmail.zariust.otherdrops.options.Action;
import com.gmail.zariust.otherdrops.options.ConfigOnly;
import com.gmail.zariust.otherdrops.subject.Target;

public abstract class AbstractDropEvent {
	private Target block;
	private Action action;
	protected Random rng;

	public AbstractDropEvent(Target targ, Action act) {
		block = targ;
		action = act;
		rng = OtherDrops.rng;
	}
	
	/**
	 * @param diff A flag whose value doesn't matter but whose presence means "validate the target". 
	 */
	protected AbstractDropEvent(Target targ, Action act, boolean diff) throws DropCreateException {
		this(targ, act);
		if(targ.getClass().isAnnotationPresent(ConfigOnly.class)) {
			ConfigOnly annotate = targ.getClass().getAnnotation(ConfigOnly.class);
			throw new DropCreateException(targ.getClass(), annotate.value());
		}
	}
	
	public abstract boolean matches(AbstractDropEvent other);

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

	public boolean basicMatch(AbstractDropEvent other) {
		if(!block.matches(other.block)) {
			OtherDrops.logInfo("AbstractDrop - basicMatch/target - failed. this.target="+block.toString()+" other.target="+other.block.toString(),5);
			return false;
		}
		if(!action.equals(other.action)) {
			OtherDrops.logInfo("AbstractDrop - basicMatch/action - failed. this.action="+action.toString()+" other.action="+other.action.toString(),5);
			return false;
		}
		return true;
	}
	
	static public int hashCode(Object type, int v, int data) 	{
		short t = type == null ? (short) 0 : (short) type.hashCode();
		return (v << 16) | t | (data << 3);
	}
}
