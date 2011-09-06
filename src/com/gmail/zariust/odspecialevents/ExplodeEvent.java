package com.gmail.zariust.odspecialevents;

import java.util.List;

import com.gmail.zariust.otherdrops.event.OccurredDropEvent;
import com.gmail.zariust.otherdrops.event.SimpleDropEvent;
import com.gmail.zariust.otherdrops.special.SpecialResult;

public class ExplodeEvent extends SpecialResult {
	private float power = 4.0f;
	private boolean fire = false;

	public ExplodeEvent(ExplosionEvents source) {
		super("EXPLOSION", source);
	}

	@Override
	public void executeAt(OccurredDropEvent event) {
		event.getWorld().createExplosion(event.getLocation(), power, fire);
	}
	
	@Override
	public void interpretArguments(List<String> args) {
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
	public boolean canRunFor(SimpleDropEvent drop) {
		return true;
	}
	
	@Override
	public boolean canRunFor(OccurredDropEvent drop) {
		return true;
	}
	
}
