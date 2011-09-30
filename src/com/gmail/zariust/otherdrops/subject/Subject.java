package com.gmail.zariust.otherdrops.subject;

import com.gmail.zariust.otherdrops.data.Data;

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
	 * @return The basic type of the subject
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
	 * The data associated with the subject, if any.
	 * @return Some data
	 */
	Data getData();
	
	/**
	 * @return The subject's location.
	 */
	Location getLocation();
	
	class HashCode {
		private Subject what;
		private Object dataObj;
		public HashCode(Subject subj) {
			what = subj;
			dataObj = what.getData();
		}
		public HashCode setData(Object data) {
			dataObj = data;
			return this;
		}
		public int get(Object info) {
			ItemCategory type = what.getType();
			int data = dataObj == null ? 0 : dataObj.hashCode();
			int v = info == null ? 0 : info.hashCode();
			int t = type == null ? (short) 0 : (short) type.hashCode();
			return (v << 16) | t | (data << 3);
		}
	}
}
