package com.gmail.zariust.odspecials;

import java.util.Arrays;
import java.util.List;

import com.gmail.zariust.otherdrops.special.SpecialResult;
import com.gmail.zariust.otherdrops.special.SpecialResultHandler;

public class WeatherEvents extends SpecialResultHandler {
	@Override
	public SpecialResult getNewEvent(String name) {
		if(name.equalsIgnoreCase("LIGHTNING")) return new LightningEvent(this);
		else if(name.equalsIgnoreCase("STORM")) return new StormEvent(this);
		else if(name.equalsIgnoreCase("THUNDER")) return new ThunderEvent(this);
		return null;
	}
	
	@Override
	public void onLoad() {
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
