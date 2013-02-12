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

package com.gmail.zariust.otherdrops.data;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.gmail.zariust.common.Verbosity.*;

import com.gmail.zariust.otherdrops.Log;
import com.gmail.zariust.otherdrops.OtherDrops;
import com.gmail.zariust.otherdrops.data.entities.AgeableData;
import com.gmail.zariust.otherdrops.data.entities.CreeperData;
import com.gmail.zariust.otherdrops.data.entities.OcelotData;
import com.gmail.zariust.otherdrops.data.entities.PigData;
import com.gmail.zariust.otherdrops.data.entities.SkeletonData;
import com.gmail.zariust.otherdrops.data.entities.VillagerData;
import com.gmail.zariust.otherdrops.data.entities.ZombieData;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.entity.*;
import org.bukkit.material.MaterialData;

// Range only allowed for SHEEP, SLIME, and PIG_ZOMBIE
public class CreatureData implements Data, RangeableData {
	
	// Create a map of entity types against data objects
	private static final Map<EntityType, Class> DATAMAP;
    
	// Map of EntityTypes to new class based creature data, for ease of lookup later on
	// note: there should be only one line per entity, or things could get messy
    static {
		Map <EntityType, Class> aMap = new HashMap<EntityType, Class>();

		aMap.put(EntityType.VILLAGER, VillagerData.class);
		aMap.put(EntityType.ZOMBIE,   ZombieData.class);
		aMap.put(EntityType.CREEPER,  CreeperData.class);
		aMap.put(EntityType.OCELOT,   OcelotData.class);

		aMap.put(EntityType.CHICKEN,  AgeableData.class);
		aMap.put(EntityType.COW,      AgeableData.class);
		aMap.put(EntityType.MUSHROOM_COW, AgeableData.class);


		aMap.put(EntityType.SKELETON, SkeletonData.class);
		aMap.put(EntityType.PIG, PigData.class);

        DATAMAP = Collections.unmodifiableMap(aMap);
    }
	public int data;
	private Boolean sheared;
	
	public CreatureData(int mobData) {
		this(mobData, null);
	}

	public CreatureData(int mobData, Boolean sheared) {
		data = mobData;
		this.sheared = sheared;
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
	public Boolean getSheared() {
		return sheared;
	}

	@Override
	public boolean matches(Data d) {
		if(!(d instanceof CreatureData)) return false;
		//OtherDrops.logInfo("Checking data = "+data+" this.getsheared: "+sheared+" othersheared:"+d.getSheared());
		if (data == -2) { // for sheep with no color specified
			if (d.getSheared() == null) return true; // null is like a wildcard
			return sheared == d.getSheared(); 
		}
		
		boolean shearMatch = false;
		if (sheared == null) shearMatch = true;
		else if (sheared == d.getSheared()) shearMatch = true;
		return (data == d.getData() && shearMatch);
	}
	
	@Override
	public String get(Enum<?> creature) {
		if(creature instanceof EntityType) return get((EntityType)creature);
		return "";
	}
	
	private String get(EntityType type) {
		switch(type) {
		case PIG:
			if(data > 1) break;
			return data == 1 ? "SADDLED" : "UNSADDLED";
		case WOLF:
			if(data > 2) break;
			if(data == 2) return "TAME";
			return data == 1 ? "ANGRY" : "WILD";
		case SLIME:
		case MAGMA_CUBE:
			if(data == 0) return "TINY";
			if(data == 1) return "TINY";
			if(data == 2) return "SMALL";
			if(data == 3) return "BIG";
			if(data == 4) return "HUGE";
			// Fallthrough intentional
		case PIG_ZOMBIE:
			return Integer.toString(data);
		case SHEEP:
			if(data >= 48) break; // Highest valid sheep data: 32 + 15 = 47
			String result = "";
			if(data > 32) {
				result += "SHEARED";
				data -= 32;
			}
			if(data >= 16) break;
			if(data >= 0) {
				if(!result.isEmpty()) result += "/";
				result += DyeColor.getByData((byte)data);
			}
			return result;
		case ENDERMAN:
			if(data > 0) {
				int id = data & 0xF, d = data >> 8;
				Material material = Material.getMaterial(id);
				@SuppressWarnings("hiding")
				Data data = new SimpleData(d);
				String dataStr = data.get(material);
				result = material.toString();
				if(!dataStr.isEmpty()) result += "/" + dataStr;
				return result;
			}
			break;
		default:
			if(data > 0) throw new IllegalArgumentException("Invalid data for " + type + ".");
		}
		return "";
	}
	
	@Override
	public void setOn(Entity mob, Player owner) {
		switch(mob.getType()) {
		case PIG:
			if(data == 1) ((Pig)mob).setSaddle(true);
			break;
		case SHEEP:
			if(data >= 32) ((Sheep)mob).setSheared(true);
			data -= 32;
			if(data >= 0) ((Sheep)mob).setColor(DyeColor.getByData((byte)data));
			break;
		case SLIME:
			if(data > 0) ((Slime)mob).setSize(data);
			break;
		case MAGMA_CUBE:
			if(data > 0) ((MagmaCube)mob).setSize(data);
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
		case ENDERMAN:
			if(data > 0) {
				int id = data & 0xF, d = data >> 8;
				MaterialData md = Material.getMaterial(id).getNewData((byte)d);
				((Enderman)mob).setCarriedMaterial(md);
			}
			break;
		default:
		}
	}

	@Override // No creature has a block state, so nothing to do here.
	public void setOn(BlockState state) {}

	@SuppressWarnings("incomplete-switch")
	public static Data parse(EntityType creature, String state) {
		if(state == null || state.isEmpty()) return null;
		
		state = state.toUpperCase().replaceAll("[ _-]", "");
		
		if (DATAMAP.get(creature) != null) {
			CreatureData cData = null;
			try {
				cData = (CreatureData)DATAMAP.get(creature).getMethod("parseFromString", String.class).invoke(null, state);
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			};
			if (cData == null) return new CreatureData(0);
			return cData;

		} else {
		String[] split;
		switch(creature) {
		case PIG:
			if(state.equalsIgnoreCase("SADDLED")) return new CreatureData(1);
			else if(state.equalsIgnoreCase("UNSADDLED")) return new CreatureData(0);
			break;
		case SHEEP:
			if(state.startsWith("RANGE")) return RangeData.parse(state);
			split = state.split("[\\\\/]",2);
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
					Integer data = null;
					if(!colour.isEmpty()) {
						try {
							data = (int)DyeColor.valueOf(colour).getData() ;
							success = true;
						} catch(IllegalArgumentException e) {
							success = false;
						}
						// Or numbers
						try {
							int clr = Integer.parseInt(colour);
							if(clr < 16) data = clr;
							success = true;
						} catch(NumberFormatException e) {}
					} else success = true;
					
					Boolean sheared = null;
						if (wool.equalsIgnoreCase("SHEARED")) {
							sheared = true;
						} else if (wool.equalsIgnoreCase("UNSHEARED")) {
							sheared = false;
						}
					if(wool.equalsIgnoreCase("SHEARED")) {
						if (data == null) return new CreatureData(-2, sheared);
						else return new CreatureData(data + 32, sheared);
					}
					else if(success || wool.equalsIgnoreCase("UNSHEARED")) {
						if (data == null) return new CreatureData(-2, sheared);
						else return new CreatureData(data, sheared);
					}
				}
			}
			break;
		case SLIME:
		case MAGMA_CUBE:
			if(state.equalsIgnoreCase("TINY")) return new CreatureData(1);
			else if(state.equalsIgnoreCase("SMALL")) return new CreatureData(2);
			else if(state.equalsIgnoreCase("BIG")) return new CreatureData(3);
			else if(state.equalsIgnoreCase("HUGE")) return new CreatureData(4);
			// Fallthrough intentional
		case PIG_ZOMBIE:
			if(state.startsWith("RANGE")) return RangeData.parse(state);
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
		case ENDERMAN:
			split = state.split("/");
			Material material = Material.getMaterial(split[0]);
			if (material == null) {
				try {
					material = Material.getMaterial(Integer.parseInt(split[0]));
				} catch(NumberFormatException e) {
					return new CreatureData(0);
				}
			}
			Data data = new SimpleData();
			if(split.length > 1)
				data = SimpleData.parse(material, split[1]);
			else data = new SimpleData();
			int md = (data.getData() << 8) | material.getId();
			return new CreatureData(md);
		}
		}
		return new CreatureData();
	}
	
	public static CreatureData parseFromString(String state) {
		Log.logInfo("CreatureData: parseFromString, shouldn't be here (should be in specific mob data) - please let developer know.");
		return null;
	}

	public static CreatureData parseFromEntity(Entity entity) {
		Log.logInfo("CreatureData: parseFromEntity, shouldn't be here (should be in specific mob data) - please let developer know.");


		
		return null;
	}

	@Override
	public String toString() {
		// TODO: Should probably make sure this is not used, and always use the get method instead
		Log.logWarning("CreatureData.toString() was called! Is this right?", EXTREME);
		OtherDrops.stackTrace();
		return String.valueOf(data);
	}
	
	@Override
	public int hashCode() {
		return data;
	}


	
	public static Data parse(LivingEntity entity) {
		if(entity == null) return new CreatureData(0);
		EntityType creatureType = entity.getType();
		if(creatureType == null) return new CreatureData(0);
		if (DATAMAP.get(entity.getType()) != null) {
			CreatureData cData = null;
			try {
				cData = (CreatureData)DATAMAP.get(entity.getType()).getMethod("parseFromEntity", Entity.class).invoke(null, entity);
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			};
			if (cData == null) return new CreatureData(0);

			if (cData == null) {
				return new CreatureData(0);
			} else {}
				//Log.logInfo(cData.toString());
			return cData;
		} else {
		switch(creatureType) {
		case PIG:
			return ((Pig)entity).hasSaddle() ? new CreatureData(1) : new CreatureData(0);
		case SHEEP:
			return new CreatureData(((Sheep)entity).getColor().getData(), (((Sheep)entity).isSheared() ? true : false));
		case SLIME:
			return new CreatureData(((Slime)entity).getSize());
		case MAGMA_CUBE:
			return new CreatureData(((MagmaCube)entity).getSize());
		case WOLF:
			return new CreatureData(((Wolf)entity).isAngry() ? 1 : (((Wolf)entity).isTamed() ? 2 : 0));
		case PIG_ZOMBIE:
			return new CreatureData(((PigZombie)entity).getAnger());
		case ENDERMAN:
			MaterialData data = ((Enderman)entity).getCarriedMaterial();
			if(data == null) return new CreatureData(0);
			return new CreatureData(data.getItemTypeId() | (data.getData() << 8));
		default:
			return new CreatureData(0);
		}
		}
	}
}
