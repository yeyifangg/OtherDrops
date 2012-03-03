package com.gmail.zariust.otherdrops.parameters.conditions;

import com.gmail.zariust.otherdrops.event.OccurredEvent;
import com.gmail.zariust.otherdrops.parameters.Parameter;

public abstract class Condition extends Parameter {
	
	public abstract boolean check(OccurredEvent occurrence);

}
