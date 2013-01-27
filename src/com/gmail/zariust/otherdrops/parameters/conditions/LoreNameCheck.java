package com.gmail.zariust.otherdrops.parameters.conditions;

import java.util.ArrayList;
import java.util.List;


import org.bukkit.inventory.ItemStack;

import com.gmail.zariust.common.Verbosity;
import com.gmail.zariust.otherdrops.ConfigurationNode;
import com.gmail.zariust.otherdrops.Log;
import com.gmail.zariust.otherdrops.event.OccurredEvent;
import com.gmail.zariust.otherdrops.subject.PlayerSubject;

public class LoreNameCheck extends Condition {

	String name = "LoreNameCheck";
	private String loreName;
	
	public LoreNameCheck(String loreName) {
		this.loreName = loreName;
	}

	@Override
	public boolean checkInstance(OccurredEvent occurrence) {
		Log.logInfo("Checking for lorename condition..." + "== "+loreName, Verbosity.HIGHEST);
		if (occurrence.getTool() instanceof PlayerSubject) {
			PlayerSubject player = (PlayerSubject) occurrence.getTool();
			ItemStack item = player.getPlayer().getItemInHand();
			Log.logInfo("tool name = "+item.getType().name());
			if (item.hasItemMeta()) {
				String displayName = item.getItemMeta().getDisplayName();
				Log.logInfo("Checking for lorename condition... '" + displayName + "' == '"+loreName+"'", Verbosity.HIGHEST);
				if (displayName.equalsIgnoreCase(loreName)) return true;
			}
		}
		
		return false;
		
	}

	public static List<Condition> parse(ConfigurationNode node) {
		String loreName = node.getString("lorename");
		if (loreName == null) {
			loreName = node.getString("displayname");
			if (loreName == null) return null;
		}

		List<Condition> conditionList = new ArrayList<Condition>();
		conditionList.add(new LoreNameCheck(loreName));
		return conditionList;
	}	

	

}
