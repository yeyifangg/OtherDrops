package com.gmail.zariust.otherdrops.subject;

import java.util.Random;

import com.gmail.zariust.otherdrops.options.ToolDamage;

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
	 * @param rng Random number generator
	 */
	public void damageTool(ToolDamage amount, Random rng);
}
