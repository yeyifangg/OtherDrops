package com.gmail.zariust.bukkit.otherblocks.options;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Specifies a config-only option, meaning it cannot (on its own) represent an actual drop.
 * Generally this means it represents a single aspect of a larger drop.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface ConfigOnly {
	/**
	 * If making an OccurredDrop, you can use any of these classes instead of the annotated class.
	 * @return The non-virtual agent classes it can match against
	 */
	Class<?>[] value();
}
