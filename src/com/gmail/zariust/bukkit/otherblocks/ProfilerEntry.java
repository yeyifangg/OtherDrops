package com.gmail.zariust.bukkit.otherblocks;

public class ProfilerEntry {
	public long started;
	public long finished;
	public String eventName;

	public ProfilerEntry (String eName) {
		eventName = eName;
	}
}
