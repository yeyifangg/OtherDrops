package com.gmail.zariust.bukkit.otherblocks.drops;

import java.util.HashMap;
import java.util.Map;

import com.gmail.zariust.bukkit.otherblocks.options.Action;
import com.gmail.zariust.bukkit.otherblocks.subject.Target;

public class DropsMap {
	private Map<Action, Map<String, DropsList>> blocksHash = new HashMap<Action, Map<String, DropsList>>();
	
	public void addDrop(CustomDrop drop) {
		if(!blocksHash.containsKey(drop.getAction()))
			blocksHash.put(drop.getAction(), new HashMap<String, DropsList>());
		Map<String, DropsList> actionHash = blocksHash.get(drop.getAction());
		for(Target target : drop.getTarget().canMatch()) {
			String key = target.getKey();
			if(key == null) continue; // shouldn't happen though...?
			if(!actionHash.containsKey(key)) actionHash.put(key, new DropsList());
			DropsList drops = actionHash.get(key);
			drops.list.add(drop);
		}
	}
	
	public DropsList getList(Action action, Target target) {
		if(!blocksHash.containsKey(action)) return null;
		return blocksHash.get(action).get(target.getKey());
	}

	public void clear() {
		blocksHash.clear();
	}
}
