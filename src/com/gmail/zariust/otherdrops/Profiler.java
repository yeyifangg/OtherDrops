package com.gmail.zariust.otherdrops;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Profiler {
	private HashMap<String, List<Long>> profileMap;
	private boolean nanoSeconds;
	// TODO: store all values as nano seconds and display as either nano or milli depending on "nanoSeconds"
	// note: nanoTime() takes a 100+ cycles and currentTimeMillis() only takes a few - is this relevant?

	public Profiler() {
		profileMap = new HashMap<String, List<Long>>();
		nanoSeconds = false;
	}
	public void startProfiling(ProfilerEntry entry) {
		if(!OtherBlocks.plugin.config.profiling) return;
		//OtherBlocks.logInfo("Starting profile for "+entry.eventName+".",4);
		if(!profileMap.containsKey(entry.eventName)) profileMap.put(entry.eventName, new ArrayList<Long>());
		entry.started = (nanoSeconds) ? System.nanoTime() : System.currentTimeMillis();			
	}

	public void stopProfiling(ProfilerEntry entry) {
		if(!OtherBlocks.plugin.config.profiling) return;
		entry.finished = (nanoSeconds) ? System.nanoTime() : System.currentTimeMillis();			
		long timeTaken = entry.finished - entry.started;
		OtherBlocks.logInfo(entry.eventName+" took " + (timeTaken) + (nanoSeconds?" nanoseconds.":" milliseconds."),4);
		profileMap.get(entry.eventName).add(timeTaken);
	}
	
	public void clearProfiling() {
		for(List<Long> entry : profileMap.values())
			entry.clear();
	}

	public List<Long> getProfiling(String event) {
		if(!profileMap.containsKey(event)) return null;
		return profileMap.get(event);
	}
	
	public boolean getNano() {
		return nanoSeconds;
	}
	public void setNano(boolean nano) {
		nanoSeconds = nano;
	}
}
