// OtherDrops - a Bukkit plugin
// Copyright (C) 2011 Robert Sargant, Zarius Tularial, Celtic Minstrel
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.	 See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program.	 If not, see <http://www.gnu.org/licenses/>.

package com.gmail.zariust.otherdrops;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.gmail.zariust.common.Verbosity.*;

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
		if(!OtherDrops.plugin.config.profiling) return;
		Log.logInfo("Starting profile for "+entry.eventName+".",EXTREME);
		if(!profileMap.containsKey(entry.eventName)) profileMap.put(entry.eventName, new ArrayList<Long>());
		entry.started = (nanoSeconds) ? System.nanoTime() : System.currentTimeMillis();			
	}

	public void stopProfiling(ProfilerEntry entry) {
		if(!OtherDrops.plugin.config.profiling) return;
		entry.finished = (nanoSeconds) ? System.nanoTime() : System.currentTimeMillis();
		long timeTaken = entry.finished - entry.started;
		// TODO: Should this be LOW or no verbosity specified since it's only shown on request?
		Log.logInfo(entry.eventName+" took " + (timeTaken) + (nanoSeconds?" nanoseconds.":" milliseconds."),HIGHEST);
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
