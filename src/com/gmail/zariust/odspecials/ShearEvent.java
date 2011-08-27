package com.gmail.zariust.odspecials;

import java.util.List;

import org.bukkit.entity.Sheep;

import com.gmail.zariust.otherdrops.event.OccurredDropEvent;
import com.gmail.zariust.otherdrops.event.SimpleDropEvent;
import com.gmail.zariust.otherdrops.special.SpecialResult;
import com.gmail.zariust.otherdrops.subject.CreatureSubject;

public class ShearEvent extends SpecialResult {
	private Boolean state;

	public ShearEvent(SheepEvents source, Boolean b) {
		super(b == null ? "SHEARTOGGLE" : (b ? "" : "UN") + "SHEAR", source);
		state = b;
	}

	@Override
	public void executeAt(OccurredDropEvent event) {
		CreatureSubject target = (CreatureSubject) event.getTarget();
		Sheep sheep = (Sheep) target.getAgent();
		boolean newState;
		if(state == null) newState = !sheep.isSheared();
		else newState = state;
		sheep.setSheared(newState);
	}
	
	@Override public void interpretArguments(List<String> args) {}
	
	@Override
	public boolean canRunFor(SimpleDropEvent drop) {
		return SheepEvents.canRunFor(drop);
	}
	
	@Override
	public boolean canRunFor(OccurredDropEvent drop) {
		return SheepEvents.canRunFor(drop);
	}
	
}
