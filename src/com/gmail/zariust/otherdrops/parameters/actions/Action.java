package com.gmail.zariust.otherdrops.parameters.actions;

import com.gmail.zariust.otherdrops.event.CustomDrop;
import com.gmail.zariust.otherdrops.event.OccurredEvent;
import com.gmail.zariust.otherdrops.parameters.Parameter;

public abstract class Action extends Parameter {

	public abstract boolean act(CustomDrop drop, OccurredEvent occurence);
}
