package com.gmail.zariust.bukkit.obevents;

import org.bukkit.Location;
import org.bukkit.World;

import com.gmail.zariust.bukkit.otherblocks.drops.OccurredDrop;
import com.gmail.zariust.bukkit.otherblocks.drops.SimpleDrop;
import com.gmail.zariust.bukkit.otherblocks.event.DropEvent;

public class LightningEvent extends DropEvent {
	private boolean harmless, player;
	
	public LightningEvent(WeatherEvents source) {
		super("LIGHTNING", source);
	}

	@Override
	public void executeAt(OccurredDrop event) {
		Location location = null;
		if(player) location = event.getTool().getLocation();
		if(location == null) location = event.getLocation();
		World world = location.getWorld();
		if(harmless) world.strikeLightningEffect(location);
		else world.strikeLightning(location);
	}
	
	@Override
	public void interpretArguments(String... args) {
		for(String arg : args) {
			if(arg.equalsIgnoreCase("HARMLESS")) {
				harmless = true;
				used(args[0]);
			} else if(arg.equalsIgnoreCase("PLAYER")) {
				player = true;
				used(args[0]);
			} else; // TODO: Handling invalid arguments?
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
