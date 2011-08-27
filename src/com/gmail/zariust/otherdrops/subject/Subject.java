package com.gmail.zariust.otherdrops.subject;

import org.bukkit.Location;

/**
 * A subject which may be either a target or agent
 * @see Target Agent
 */
public interface Subject {
	/**
	 * The category of subject represented
	 */
	public enum ItemCategory {
		/**
		 * Represents a block or block-like entity.
		 */
		BLOCK,
		/**
		 * Represents a living entity that is not a player.
		 */
		CREATURE,
		/**
		 * Represents a player.
		 */
		PLAYER,
		/**
		 * Represents a projectile.
		 */
		PROJECTILE,
		/**
		 * Represents a source of damage.
		 */
		DAMAGE,
		/**
		 * Represents an explosion.
		 */
		EXPLOSION,
		/**
		 * Something that doesn't fit any of the others.
		 */
		SPECIAL,
	}

	/**
	 * @return The basic type of the target
	 */
	ItemCategory getType(); // TODO: Is this even necessary? It's not used anywhere.
	
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
