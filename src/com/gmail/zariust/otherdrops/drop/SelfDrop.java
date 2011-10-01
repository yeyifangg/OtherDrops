// OtherDrops - a Bukkit plugin
// Copyright (C) 2011 Robert Sargant, Zarius Tularial, Celtic Minstrel
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.	 See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program.	 If not, see <http://www.gnu.org/licenses/>.

package com.gmail.zariust.otherdrops.drop;

import com.gmail.zariust.common.CommonEntity;
import com.gmail.zariust.otherdrops.options.DoubleRange;
import com.gmail.zariust.otherdrops.options.IntRange;
import com.gmail.zariust.otherdrops.subject.BlockTarget;
import com.gmail.zariust.otherdrops.subject.CreatureSubject;
import com.gmail.zariust.otherdrops.subject.Target;
import com.gmail.zariust.otherdrops.subject.VehicleTarget;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.CreatureType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Painting;
import org.bukkit.entity.Vehicle;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.PistonExtensionMaterial;

public class SelfDrop extends DropType {
	private IntRange count;
	private DropType rolledDrop;

	public SelfDrop() {
		this(100.0);
	}
	
	public SelfDrop(double chance) {
		this(new IntRange(1), chance);
	}
	
	public SelfDrop(IntRange amount) {
		this(amount, 100.0);
	}
	
	public SelfDrop(IntRange intRange, double chance) { // Rome!
		super(DropCategory.DEFAULT, chance);
		count = intRange;
	}

	@Override
	protected void performDrop(Target source, Location from, DropFlags flags) {
		DropType actualDrop;
		if(source instanceof CreatureSubject) {
			LivingEntity mob = ((CreatureSubject)source).getAgent();
			int data = CommonEntity.getCreatureData(mob);
			CreatureType type = CommonEntity.getCreatureType(mob);
			actualDrop = new CreatureDrop(count, type, data);
		} else if(source instanceof VehicleTarget) {
			Entity entity = ((VehicleTarget)source).getVehicle();
			if(entity instanceof Painting) {
				actualDrop = new ItemDrop(count, Material.PAINTING);
			} else if(entity instanceof Vehicle) {
				Material material = CommonEntity.getVehicleType(entity);
				actualDrop = new ItemDrop(count, material);
			} else return;
		} else if(source instanceof BlockTarget) {
			Block block = ((BlockTarget)source).getBlock();
			Material material = block.getType();
			int data = block.getData(), quantity = count.getRandomIn(flags.rng);
			switch(material) {
			case AIR: return;
			case LOG:
			case WOOL:
			case STEP:
				break;
			case SAPLING:
			case LEAVES:
				data = data & 3;
				break;
			case BED_BLOCK:
				data = 0;
				material = Material.BED;
				break;
			case DOUBLE_STEP:
				quantity *= 2;
				break;
			case REDSTONE_WIRE:
				data = 0;
				material = Material.REDSTONE;
				break;
			case SIGN_POST:
			case WALL_SIGN:
				data = 0;
				material = Material.SIGN;
				break;
			case WOODEN_DOOR:
				data = 0;
				material = Material.WOOD_DOOR;
				break;
			case PISTON_EXTENSION:
				data = 0;
				PistonExtensionMaterial ext = (PistonExtensionMaterial)block.getState().getData();
				material = ext.isSticky() ? Material.PISTON_STICKY_BASE : Material.PISTON_BASE;
				break;
			case BURNING_FURNACE:
				data = 0;
				material = Material.FURNACE;
				break;
			case IRON_DOOR_BLOCK:
				data = 0;
				material = Material.IRON_DOOR;
				break;
			case GLOWING_REDSTONE_ORE:
				data = 0;
				material = Material.REDSTONE_ORE;
				break;
			case DIODE_BLOCK_OFF:
			case DIODE_BLOCK_ON:
				data = 0;
				material = Material.DIODE;
				break;
			default: // Most block data doesn't transfer to the item of the same ID
				data = 0;
				break;
			}
			ItemStack stack = new ItemStack(material, quantity, (short)data);
			actualDrop = new ItemDrop(stack);
		} else return; // Just in case!
		rolledDrop = actualDrop;
		actualDrop.drop(source, from, 1, flags);
	}
	
	@Override
	public double getAmount() {
		return rolledDrop.getAmount();
	}
	
	@Override
	public String getName() {
		return "THIS";
	}

	@Override
	public DoubleRange getAmountRange() {
		return count.toDoubleRange();
	}
}
