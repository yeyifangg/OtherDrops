package com.gmail.zariust.otherdrops.subject;

import java.util.List;

/**
 * A target which may be affected or acted on in some way to produce a drop.
 */
public interface Target extends Subject {
	/**
	 * Whether this target should drop its default drop even if a configured drop has
	 * been triggered with a 100% chance. If it returns false, the only way to prevent
	 * the target from producing its default drops is to provide a specific NOTHING drop
	 * at 100%; if true, any drop at 100% cancels the default drops.
	 * @return True or false.
	 */
	abstract boolean overrideOn100Percent();
	
	/**
	 * A list of targets that this target can match; if it's not a wildcard, it should return a singleton
	 * list. Otherwise, it should return a list of non-equal targets such that {@code this.matches(target)}
	 * will return true for each target on the list and false for any target not on the list.
	 * @return A list of targets
	 */
	List<Target> canMatch();

	/**
	 * Gets a key representing the basic substance of the target, sans any accompanying data. If
	 * this is a wildcard target, it can safely just return null.
	 * @return A key for storage in a hash map.
	 */
	String getKey();
}
