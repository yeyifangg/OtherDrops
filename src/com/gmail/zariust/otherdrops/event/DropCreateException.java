package com.gmail.zariust.otherdrops.event;

import java.util.ArrayList;
import java.util.List;

/**
 * Thrown when you try to create an OccurredDrop with a target or agent that cannot fully
 * represent the drop. Such targets or agents may be identified by the ConfigOnly annotation.
 */
public class DropCreateException extends Exception {
	private static final long serialVersionUID = 6912695040940051952L;

	public DropCreateException(Class<?> error, Class<?>[] suggest) {
		super("Can't use the class " + error.getSimpleName() + " as the target or agent of an OccurredDrop; " +
			"try one of " + toList(suggest) + " instead.");
	}

	private static List<String> toList(Class<?>[] suggest) {
		List<String> suggestions = new ArrayList<String>();
		for(Class<?> clazz : suggest) suggestions.add(clazz.getSimpleName());
		return suggestions;
	}
}
