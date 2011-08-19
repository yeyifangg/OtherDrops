package com.gmail.zariust.bukkit.otherblocks.options.drop;

import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.CreatureType;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Pig;
import org.bukkit.entity.PigZombie;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Slime;
import org.bukkit.entity.Wolf;

import com.gmail.zariust.bukkit.common.CommonEntity;

public class CreatureDrop extends DropType {
	private CreatureType type;
	private int data;
	private int quantity;
	
	public CreatureDrop(CreatureType mob) {
		this(1, mob, 0);
	}
	
	public CreatureDrop(int amount, CreatureType mob) {
		this(amount, mob, 0);
	}
	
	public CreatureDrop(CreatureType mob, int mobData) {
		this(1, mob, mobData);
	}
	
	public CreatureDrop(int amount, CreatureType mob, int mobData) {
		super(DropCategory.CREATURE);
		type = mob;
		data = mobData;
		quantity = amount;
	}
	
	public CreatureDrop(Entity e) {
		this(CommonEntity.getCreatureType(e), CommonEntity.getCreatureData(e));
	}

	public CreatureType getCreature() {
		return type;
	}

	public int getCreatureData() {
		return data;
	}
	
	public int getQuantity() {
		return quantity;
	}

	@Override
	protected void performDrop(Location where, DropFlags flags) {
		int amount = quantity;
		while(amount-- > 0) {
			World in = where.getWorld();
			LivingEntity mob = in.spawnCreature(where, type);
			switch(type) {
			case CREEPER:
				if(data == 1) ((Creeper)mob).setPowered(true);
				break;
			case PIG:
				if(data == 1) ((Pig)mob).setSaddle(true);
				break;
			case SHEEP:
				if(data >= 16) ((Sheep)mob).setSheared(true);
				data -= 16;
				((Sheep)mob).setColor(DyeColor.getByData((byte) data));
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
					((Wolf)mob).setOwner(flags.recipient);
					break;
				}
				break;
			case PIG_ZOMBIE:
				if(data > 0) ((PigZombie)mob).setAnger(data);
				break;
			default:
			}
		}
	}
}
