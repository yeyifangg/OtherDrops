package com.gmail.zariust.bukkit.obevents;

import org.bukkit.entity.Sheep;

import com.gmail.zariust.bukkit.otherblocks.drops.OccurredDrop;
import com.gmail.zariust.bukkit.otherblocks.drops.SimpleDrop;
import com.gmail.zariust.bukkit.otherblocks.event.DropEvent;
import com.gmail.zariust.bukkit.otherblocks.subject.CreatureAgent;

public class ShearEvent extends DropEvent {
	private Boolean state;

	public ShearEvent(SheepEvents source, Boolean b) {
		super(b == null ? "SHEARTOGGLE" : (b ? "" : "UN") + "SHEAR", source);
		state = b;
	}

	@Override
	public void executeAt(OccurredDrop event) {
		CreatureAgent target = (CreatureAgent) event.getTarget();
		Sheep sheep = (Sheep) target.getAgent();
		boolean newState;
		if(state == null) newState = !sheep.isSheared();
		else newState = state;
		sheep.setSheared(newState);
	}
	
	@Override public void interpretArguments(String... args) {}
	
	@Override
	public boolean canRunFor(SimpleDrop drop) {
		return SheepEvents.canRunFor(drop);
	}
	
	@Override
	public boolean canRunFor(OccurredDrop drop) {
		return SheepEvents.canRunFor(drop);
	}
	
}
