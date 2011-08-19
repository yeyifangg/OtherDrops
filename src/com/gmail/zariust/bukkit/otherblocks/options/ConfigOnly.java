package com.gmail.zariust.bukkit.otherblocks.options;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

// Specifies a config-only, meaning it cannot (on its own) represent an actual drop.
// Generally this means it represents a single aspect of a larger drop.
@Retention(RetentionPolicy.SOURCE)
public @interface ConfigOnly {
	Class<?> value(); // The non-virtual agent class it matches against
}
