package com.gmail.zariust.bukkit.otherblocks.subject;

import org.bukkit.Location;

/**
 * A subject which may be either a target or agent
 * @see Target Agent
 */
public interface Subject {
	/**
	 * @return The basic type of the target
	 */
	ItemType getType(); // TODO: Is this even necessary? It's not used anywhere.
	
	/**
	 * Whether this subject matches the other one. If this is not a wildcard, it should return whether the
	 * two are equal.
	 * @param other The subject to match against.
	 * @return Whether they match.
	 */
	boolean matches(Subject other);

	/**
	 * @return The target's location.
	 */
	Location getLocation();
}
