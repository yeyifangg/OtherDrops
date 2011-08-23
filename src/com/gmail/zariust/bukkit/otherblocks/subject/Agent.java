package com.gmail.zariust.bukkit.otherblocks.subject;

/**
 * An agent which may affect or act on a target to produce a drop.
 */
public interface Agent extends Subject {
	/**
	 * Do some damage to this agent.
	 * @param amount The amount of damage.
	 */
	public void damage(int amount);

	/**
	 * Do some damage to this agent's tool, if it has one.
	 * @param amount The amount of damage.
	 */
	public void damageTool(short amount);
	
	/**
	 * Do a default amount of damage to this agent's tool, if it has one. Usually this will be
	 * 1 or 0.
	 * @see #damageTool(short)
	 */
	public void damageTool();
}
