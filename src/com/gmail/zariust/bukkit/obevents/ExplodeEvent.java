package com.gmail.zariust.bukkit.obevents;

import com.gmail.zariust.bukkit.otherblocks.drops.OccurredDrop;
import com.gmail.zariust.bukkit.otherblocks.drops.SimpleDrop;
import com.gmail.zariust.bukkit.otherblocks.options.event.DropEvent;

public class ExplodeEvent extends DropEvent {
	private float power = 4.0f;
	private boolean fire = false;

	public ExplodeEvent(ExplosionEvents source) {
		super("EXPLOSION", source);
	}

	@Override
	public void executeAt(OccurredDrop event) {
		event.getWorld().createExplosion(event.getLocation(), power, fire);
	}
	
	@Override
	public void interpretArguments(String... args) {
		boolean havePower = false, haveFire = false;
		for(String arg : args) {
			if(arg.equalsIgnoreCase("FIRE")) {
				haveFire = fire = true;
				used(arg);
			} else try {
				power = Float.parseFloat(arg);
				havePower = true;
				used(arg);
			} catch(NumberFormatException e) {}
			if(haveFire && havePower) break;
		}
	}
	
	@Override
	public boolean canRunFor(SimpleDrop drop) {
		return true;
	}
	
	@Override
	public boolean canRunFor(OccurredDrop drop) {
		return true;
	}
	
}
