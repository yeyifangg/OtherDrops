package com.gmail.zariust.bukkit.otherblocks.data;

import com.gmail.zariust.bukkit.common.CommonEntity;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.entity.CreatureType;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Pig;
import org.bukkit.entity.PigZombie;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Slime;
import org.bukkit.entity.Wolf;

public class CreatureData implements Data {
	private int data;

	public CreatureData(int mobData) {
		data = mobData;
	}

	public CreatureData() {
		this(0);
	}

	@Override
	public int getData() {
		return data;
	}
	
	@Override
	public void setData(int d) {
		data = d;
	}
	
	@Override
	public boolean matches(Data d) {
		if(!(d instanceof CreatureData)) return false;
		return data == d.getData();
	}
	
	public String get(CreatureType type) {
		switch(type) {
		case CREEPER:
			if(data > 1) break;
			return data == 1 ? "POWERED" : "UNPOWERED";
		case PIG:
			if(data > 1) break;
			return data == 1 ? "SADDLED" : "UNSADDLED";
		case WOLF:
			if(data > 2) break;
			if(data == 2) return "TAME";
			return data == 1 ? "ANGRY" : "WILD";
		case SLIME:
			if(data == 0) throw new IllegalArgumentException("Invalid data for " + type + ".");
			if(data == 1) return "TINY";
			if(data == 2) return "SMALL";
			if(data == 3) return "BIG";
			if(data == 4) return "HUGE";
			// Fallthrough intentional
		case PIG_ZOMBIE:
			return Integer.toString(data);
		case SHEEP:
			if(data > 48) break; // Highest valid sheep data: 32 + 16 = 48
			String result = "";
			if(data > 32) {
				result += "SHEARED";
				data -= 32;
			}
			if(data > 16) break;
			if(data > 0) {
				if(!result.isEmpty()) result += "/";
				result += DyeColor.getByData((byte)(data - 1));
			}
			return result;
		default:
			if(data > 0) throw new IllegalArgumentException("Invalid data for " + type + ".");
		}
		return "";
	}

	@Override
	public void setOn(Entity mob, Player owner) {
		switch(CommonEntity.getCreatureType(mob)) {
		case CREEPER:
			if(data == 1) ((Creeper)mob).setPowered(true);
			break;
		case PIG:
			if(data == 1) ((Pig)mob).setSaddle(true);
			break;
		case SHEEP:
			if(data >= 32) ((Sheep)mob).setSheared(true);
			data -= 32;
			if(data > 0) ((Sheep)mob).setColor(DyeColor.getByData((byte) (data - 1)));
			break;
		case SLIME:
			if(data > 0) ((Slime)mob).setSize(data);
			break;
		case WOLF:
			switch(data) {
			case 1:
				((Wolf)mob).setAngry(true);
				break;
			case 2:
				((Wolf)mob).setTamed(true);
				((Wolf)mob).setOwner(owner);
				break;
			}
			break;
		case PIG_ZOMBIE:
			if(data > 0) ((PigZombie)mob).setAnger(data);
			break;
		default:
		}
	}
	
	@Override // Nothing to do here; creatures aren't a material
	public String get(Material mat) {
		return "";
	}

	@Override // No creature has a block state, so nothing to do here.
	public void setOn(BlockState state) {}

	@SuppressWarnings("incomplete-switch")
	public static CreatureData parse(CreatureType creature, String state) {
		switch(creature) {
		case CREEPER:
			if(state.equalsIgnoreCase("POWERED")) return new CreatureData(1);
			else if(state.equalsIgnoreCase("UNPOWERED")) return new CreatureData(0);
			break;
		case PIG:
			if(state.equalsIgnoreCase("SADDLED")) return new CreatureData(1);
			else if(state.equalsIgnoreCase("UNSADDLED")) return new CreatureData(0);
			break;
		case SHEEP:
			// For sheep we have 1 as white so on so that 0 can (hopefully) mean the default of a random natural colour
			String[] split = state.split("/");
			if(split.length <= 2) {
				String colour = "", wool = "";
				if(split[0].endsWith("SHEARED")) {
					wool = split[0];
					if(split.length == 2) colour = split[1];
				} else if(split.length == 2 && split[1].endsWith("SHEARED")) {
					wool = split[1];
					colour = split[0];
				} else colour = split[0];
				if(!colour.isEmpty() || !wool.isEmpty()) {
					boolean success;
					int data = 0;
					if(!colour.isEmpty()) {
						try {
							data = DyeColor.valueOf(colour).getData() + 1;
							success = true;
						} catch(IllegalArgumentException e) {
							success = false;
						}
						// Or numbers
						try {
							int clr = Integer.parseInt(colour);
							if(clr < 16) data = clr + 1;
						} catch(NumberFormatException e) {}
					} else success = true;
					if(wool.equalsIgnoreCase("SHEARED")) return new CreatureData(data + 32);
					else if(success || wool.equalsIgnoreCase("UNSHEARED")) return new CreatureData(data);
				}
			}
			break;
		case SLIME:
			if(state.equalsIgnoreCase("TINY")) return new CreatureData(1);
			else if(state.equalsIgnoreCase("SMALL")) return new CreatureData(2);
			else if(state.equalsIgnoreCase("BIG")) return new CreatureData(3);
			else if(state.equalsIgnoreCase("HUGE")) return new CreatureData(4);
			// Fallthrough intentional
		case PIG_ZOMBIE:
			try {
				int sz = Integer.parseInt(state);
				return new CreatureData(sz);
			} catch(NumberFormatException e) {}
			break;
		case WOLF:
			if(state.equalsIgnoreCase("TAME") || state.equalsIgnoreCase("TAMED"))
				return new CreatureData(2);
			else if(state.equalsIgnoreCase("WILD") || state.equalsIgnoreCase("NEUTRAL"))
				return new CreatureData(0);
			else if(state.equalsIgnoreCase("ANGRY")) return new CreatureData(1);
			break;
		}
		return new CreatureData();
	}
}
