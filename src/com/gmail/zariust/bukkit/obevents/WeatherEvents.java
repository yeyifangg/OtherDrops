package com.gmail.zariust.bukkit.obevents;

import java.util.Arrays;
import java.util.List;

import com.gmail.zariust.bukkit.otherblocks.OtherBlocks;
import com.gmail.zariust.bukkit.otherblocks.options.event.DropEvent;
import com.gmail.zariust.bukkit.otherblocks.options.event.DropEventHandler;

public class WeatherEvents extends DropEventHandler {
	private OtherBlocks otherblocks;
	
	public WeatherEvents(OtherBlocks plugin) {
		otherblocks = plugin;
	}
	
	@Override
	public DropEvent getNewEvent(String name) {
		if(name.equalsIgnoreCase("LIGHTNING")) return new LightningEvent(this);
		else if(name.equalsIgnoreCase("STORM")) return new StormEvent(this);
		else if(name.equalsIgnoreCase("THUNDER")) return new ThunderEvent(this);
		return null;
	}
	
	@Override
	public void onLoad() {
		setVersion(info.getProperty("version"));
		logInfo("Weather v" + getVersion() + " loaded.");
	}
	
	@Override
	public List<String> getEvents() {
		return Arrays.asList("LIGHTNING", "STORM", "THUNDER");
	}
	
	@Override
	public String getName() {
		return "Weather";
	}
	
}
