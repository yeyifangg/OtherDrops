package com.gmail.zariust.odspecials;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.World;

import com.gmail.zariust.otherdrops.event.OccurredDropEvent;
import com.gmail.zariust.otherdrops.event.SimpleDropEvent;
import com.gmail.zariust.otherdrops.special.SpecialResult;

public class LightningEvent extends SpecialResult {
	private boolean harmless, player;
	
	public LightningEvent(WeatherEvents source) {
		super("LIGHTNING", source);
	}

	@Override
	public void executeAt(OccurredDropEvent event) {
		Location location = null;
		if(player) location = event.getTool().getLocation();
		if(location == null) location = event.getLocation();
		World world = location.getWorld();
		if(harmless) world.strikeLightningEffect(location);
		else world.strikeLightning(location);
	}
	
	@Override
	public void interpretArguments(List<String> args) {
		for(String arg : args) {
			if(arg.equalsIgnoreCase("HARMLESS")) {
				harmless = true;
				used(arg);
			} else if(arg.equalsIgnoreCase("PLAYER")) {
				player = true;
				used(arg);
			}
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
