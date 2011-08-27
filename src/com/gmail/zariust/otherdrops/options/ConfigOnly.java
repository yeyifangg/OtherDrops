package com.gmail.zariust.otherdrops.options;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies a config-only option, meaning it cannot (on its own) represent an actual drop.
 * Generally this means it represents a single aspect of a larger drop.
 */
@Retention(RetentionPolicy.RUNTIME)@Target(ElementType.TYPE)
public @interface ConfigOnly {
	/**
	 * If making an OccurredDrop, you can use any of these classes instead of the annotated class.
	 * @return The non-virtual agent classes it can match against
	 */
	Class<?>[] value();
}
