package com.gmail.zariust.otherdrops.droptype;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.CreatureType;

import com.gmail.zariust.common.CreatureGroup;
import com.gmail.zariust.common.MaterialGroup;

public class SimpleDropGroup extends DropType {
	private List<DropType> group;
	
	public SimpleDropGroup(DropType... drops) {
		this(Arrays.asList(drops));
	}
	
	public SimpleDropGroup(List<DropType> drops) {
		super(DropCategory.GROUP);
		group = drops;
	}
	
	public SimpleDropGroup(List<Material> materials, int defaultData, int amount, double chance) {
		this(materialsToDrops(materials, defaultData, amount, chance));
	}

	public SimpleDropGroup(List<CreatureType> creatures, int amount, double chance) {
		this(creaturesToDrops(creatures, amount, chance));
	}

	private static DropType[] materialsToDrops(List<Material> materials, int defaultData, int amount, double chance) {
		DropType[] drops = new DropType[materials.size()];
		for(int i = 0; i < drops.length; i++) {
			drops[i] = new ItemDrop(amount, materials.get(i), defaultData, chance);
		}
		return drops;
	}

	private static DropType[] creaturesToDrops(List<CreatureType> creatures, int amount, double chance) {
		DropType[] drops = new DropType[creatures.size()];
		for(int i = 0; i < drops.length; i++) {
			drops[i] = new CreatureDrop(amount, creatures.get(i), chance);
		}
		return drops;
	}

	public List<DropType> getGroup() {
		return group;
	}

	@Override
	protected void performDrop(Location where, DropFlags flags) {
		for(DropType drop : group)
			drop.drop(where, 1, flags.recipient, flags.naturally, flags.spread, flags.rng);
	}

	public static DropType parse(List<String> dropList, String defaultData) {
		List<DropType> drops = new ArrayList<DropType>();
		for(String dropName : dropList) {
			DropType drop = DropType.parse(dropName, defaultData);
			if(drop != null) drops.add(drop);
		}
		return new SimpleDropGroup(drops);
	}

	public static DropType parse(String drop, String data, int amount, double chance) {
		drop = drop.toUpperCase();
		MaterialGroup group = MaterialGroup.get(drop.substring(1));
		if(group == null) {
			if(drop.equals("^ANY_CREATURE"))
				return new SimpleDropGroup(CreatureGroup.CREATURE_ANY.creatures(), amount, chance);
			return null;
		}
		int intData = 0;
		try {
			intData = Integer.parseInt(data);
		} catch(NumberFormatException e) {}
		return new SimpleDropGroup(group.materials(), intData, amount, chance);
	}

	@Override
	public String toString() {
		return group.toString();
	}
}
