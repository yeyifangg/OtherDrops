package com.gmail.zariust.bukkit.otherblocks.options;

import org.bukkit.util.config.ConfigurationNode;

public class Comparative {
	private int compare;
	private int val;
	
	public Comparative(int v) {
		this(v, 0);
	}
	
	public Comparative(int v, int cmp) {
		val = v;
		compare = cmp;
	}
	
	public boolean matches(int v) {
		return Integer.valueOf(v).compareTo(val) == compare;
	}
	
	public static Comparative parse(String cmp) {
		if(cmp == null) return null;
		try {
			switch(cmp.charAt(0)) {
			case '<':
				return new Comparative(Integer.valueOf(cmp.substring(1)), -1);
			case '>':
				return new Comparative(Integer.valueOf(cmp.substring(1)), 1);
			case '=':
				return new Comparative(Integer.valueOf(cmp.substring(1)));
			default:
				return new Comparative(Integer.valueOf(cmp));
			}
		} catch(NumberFormatException e) {
			throw new IllegalArgumentException(e);
		}
	}

	public static Comparative parseFrom(ConfigurationNode node, String key) {
		return parse(node.getString(key));
	}
}
