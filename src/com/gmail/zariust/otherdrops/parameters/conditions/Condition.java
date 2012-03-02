package com.gmail.zariust.otherdrops.parameters.conditions;

import com.gmail.zariust.otherdrops.event.OccurredEvent;

public abstract class Condition {
	
	public abstract boolean check(OccurredEvent occurrence);
	public abstract boolean parse(Object parseMe);

}
