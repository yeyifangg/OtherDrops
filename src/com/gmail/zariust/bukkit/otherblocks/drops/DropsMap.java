package com.gmail.zariust.bukkit.otherblocks.drops;

import java.util.HashMap;
import java.util.Map;

import com.gmail.zariust.bukkit.otherblocks.options.action.Action;
import com.gmail.zariust.bukkit.otherblocks.options.target.Target;

public class DropsMap {
	private static Map<Action, Map<Target, DropsList>> blocksHash = new HashMap<Action, Map<Target, DropsList>>();
	
	public static void addDrop(Action action, Target target, CustomDrop drop) {
		if(!blocksHash.containsKey(action)) blocksHash.put(action, new HashMap<Target, DropsList>());
		Map<Target, DropsList> actionHash = blocksHash.get(action);
		if(!actionHash.containsKey(target)) actionHash.put(target, new DropsList());
		DropsList drops = actionHash.get(target);
		drops.list.add(drop);
	}
	
	public static DropsList getList(Action action, Target target) {
		if(!blocksHash.containsKey(action)) return null;
		return blocksHash.get(action).get(target);
	}

	public void clear() {
		blocksHash.clear();
	}
}
