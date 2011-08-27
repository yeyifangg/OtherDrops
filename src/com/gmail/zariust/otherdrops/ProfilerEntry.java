package com.gmail.zariust.otherdrops;

public class ProfilerEntry {
	public long started;
	public long finished;
	public String eventName;

	public ProfilerEntry (String eName) {
		eventName = eName;
	}
}
