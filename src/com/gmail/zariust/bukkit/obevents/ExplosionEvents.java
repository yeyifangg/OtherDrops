package com.gmail.zariust.bukkit.obevents;

import java.util.Arrays;
import java.util.List;

import com.gmail.zariust.bukkit.otherblocks.OtherBlocks;
import com.gmail.zariust.bukkit.otherblocks.options.event.DropEvent;
import com.gmail.zariust.bukkit.otherblocks.options.event.DropEventHandler;

public class ExplosionEvents extends DropEventHandler {
	private OtherBlocks otherblocks;
	
	public ExplosionEvents(OtherBlocks plugin) {
		otherblocks = plugin;
	}
	
	@Override
	public DropEvent getNewEvent(String name) {
		if(name.equalsIgnoreCase("EXPLOSION")) return new ExplodeEvent(this);
		return null;
	}
	
	@Override
	public void onLoad() {
		setVersion(info.getProperty("version"));
		logInfo("Explosions v" + getVersion() + " loaded.");
	}
	
	@Override
	public List<String> getEvents() {
		return Arrays.asList("Explosion");
	}
	
	@Override
	public String getName() {
		return "Explosions";
	}
	
}
