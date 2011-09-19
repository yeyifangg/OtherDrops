package com.gmail.zariust.otherdrops.drop;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.CreatureType;

import com.gmail.zariust.common.CreatureGroup;
import com.gmail.zariust.common.MaterialGroup;

public class ExclusiveDropGroup extends DropType {
	private List<DropType> group;
	
	public ExclusiveDropGroup(DropType... drops) {
		this(Arrays.asList(drops));
	}
	
	public ExclusiveDropGroup(List<DropType> drops) {
		super(DropCategory.GROUP);
		group = drops;
	}
	
	public ExclusiveDropGroup(List<Material> materials, int defaultData, int amount, double chance) {
		this(materialsToDrops(materials, defaultData, amount, chance));
	}

	public ExclusiveDropGroup(List<CreatureType> creatures, int amount, double chance) {
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
		DropType drop = group.get(flags.rng.nextInt(group.size()));
		drop.drop(where, 1, flags.recipient, flags.victim, flags.naturally, flags.spread, flags.rng);
	}

	public static DropType parse(List<String> dropList, String defaultData) {
		List<DropType> drops = new ArrayList<DropType>();
		for(String dropName : dropList) {
			DropType drop = DropType.parse(dropName, defaultData);
			if(drop != null) drops.add(drop);
		}
		return new ExclusiveDropGroup(drops);
	}

	public static DropType parse(String drop, String data, int amount, double chance) {
		drop = drop.toUpperCase();
		MaterialGroup group = MaterialGroup.get(drop);
		if(group == null) {
			if(drop.equals("ANY_CREATURE"))
				return new ExclusiveDropGroup(CreatureGroup.CREATURE_ANY.creatures(), amount, chance);
			return null;
		}
		int intData = 0;
		try {
			intData = Integer.parseInt(data);
		} catch(NumberFormatException e) {}
		if (group.materials().size() == 1) {
			// FIXME: for single item material groups parse as an item or creature?
			//return new ItemDrop.parse(group.materials().get(0).toString(), intData, amount, chance);

			return new ExclusiveDropGroup(group.materials(), intData, amount, chance);
		} else {
			return new ExclusiveDropGroup(group.materials(), intData, amount, chance);
		}
	}

	@Override
	public String toString() {
		return group.toString().replace('[', '{').replace(']', '}');
	}

	@Override
	public double getAmount() {
		return 1;
	}
}
