package com.gmail.zariust.otherdrops.drop;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.CreatureType;

import com.gmail.zariust.common.CreatureGroup;
import com.gmail.zariust.common.MaterialGroup;
import com.gmail.zariust.otherdrops.options.DoubleRange;
import com.gmail.zariust.otherdrops.options.IntRange;
import com.gmail.zariust.otherdrops.subject.Target;

public class SimpleDropGroup extends DropType {
	private List<DropType> group;
	
	public SimpleDropGroup(DropType... drops) {
		this(Arrays.asList(drops));
	}
	
	public SimpleDropGroup(List<DropType> drops) {
		super(DropCategory.GROUP);
		group = drops;
	}
	
	public SimpleDropGroup(List<Material> materials, int defaultData, IntRange amount, double chance) {
		this(materialsToDrops(materials, defaultData, amount, chance));
	}

	public SimpleDropGroup(List<CreatureType> creatures, IntRange amount, double chance) {
		this(creaturesToDrops(creatures, amount, chance));
	}

	private static DropType[] materialsToDrops(List<Material> materials, int defaultData, IntRange amount, double chance) {
		DropType[] drops = new DropType[materials.size()];
		for(int i = 0; i < drops.length; i++) {
			drops[i] = new ItemDrop(amount, materials.get(i), defaultData, chance);
		}
		return drops;
	}

	private static DropType[] creaturesToDrops(List<CreatureType> creatures, IntRange amount, double chance) {
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
	protected void performDrop(Target source, Location where, DropFlags flags) {
		for(DropType drop : group)
			drop.drop(source, where, 1, flags);
	}

	public static DropType parse(List<String> dropList, String defaultData) {
		List<DropType> drops = new ArrayList<DropType>();
		for(String dropName : dropList) {
			DropType drop = DropType.parse(dropName, defaultData);
			if(drop != null) drops.add(drop);
		}
		return new SimpleDropGroup(drops);
	}

	public static DropType parse(String drop, String data, IntRange intRange, double chance) {
		drop = drop.toUpperCase().replace("EVERY_","^ANY_");
		MaterialGroup group = MaterialGroup.get(drop.substring(1));
		if(group == null) {
			if(drop.equals("^ANY_CREATURE"))
				return new SimpleDropGroup(CreatureGroup.CREATURE_ANY.creatures(), intRange, chance);
			return null;
		}
		int intData = 0;
		try {
			intData = Integer.parseInt(data);
		} catch(NumberFormatException e) {}
		return new SimpleDropGroup(group.materials(), intData, intRange, chance);
	}

	@Override
	public String getName() {
		return group.toString();
	}

	@Override
	public double getAmount() {
		// TODO: Should it return group.size()?
		return 1;
	}

	@Override
	public DoubleRange getAmountRange() {
		// TODO: Should it return group.size()?
		return new DoubleRange(1.0);
	}
}
