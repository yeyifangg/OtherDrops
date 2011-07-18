package com.sargant.bukkit.otherblocks;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.CreatureType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.event.vehicle.VehicleListener;
import org.bukkit.material.Colorable;

import com.gmail.zarius.common.CommonEntity;

public class OtherBlocksVehicleListener extends VehicleListener {
	private OtherBlocks parent;
	
	public OtherBlocksVehicleListener(OtherBlocks instance)
	{
		parent = instance;
	}
	
	@Override
	public void onVehicleDestroy(VehicleDestroyEvent event) {
		Entity attacker = event.getAttacker();
		Entity victim = event.getVehicle();
		Material victimType = CommonEntity.getVehicleType(victim);

		if(victimType == null) return;

		// Before we do anything else, grab the relevant collection of dropgroups, if any
		Integer victimIdInt = Material.getMaterial(victimType.toString()).getId();
		String victimId = victimIdInt.toString(); // Note: hash is by string, so toString() is important
		OBContainer_DropGroups dropGroups = parent.config.blocksHash.get(victimId); 

		parent.logInfo("VEHICLEDESTROY: before check. VictimType: "+victimType.toString()+"("+victimId+")", 3);

		// loop through dropgroups, exit if none.
		if (dropGroups == null) {
			parent.logInfo("VEHICLEDESTROY: dropGroups is null - no drops.", 4);
			return;
		}

		
		String weapon;
		if(attacker instanceof Player)
			weapon = ((Player) attacker).getItemInHand().getType().toString();
		else {
			CreatureType creatureType = CommonEntity.getCreatureType(attacker);
			if(creatureType == null) return;
			weapon = "CREATURE_" + creatureType.toString();
		}
		
		
		Player player = null;
		if(attacker instanceof Player) {
			player = (Player) attacker;
		}
		
		Location location = victim.getLocation();
//		List<OB_Drop> drops = new ArrayList<OB_Drop>();
		boolean doDefaultDrop = false;
	    Short dataVal = (victim instanceof Colorable) ? ((short) ((Colorable) victim).getColor().getData()) : null;
		boolean denyBreak = false;
		boolean doDrop = true;
		String exclusive = null;
		Integer maxAttackerDamage = 0;
		
		//TODO: properly support creatures by integer value (for new itemcraft creatures)
		List<OB_Drop> toBeDropped = new ArrayList<OB_Drop>();

		for (OBContainer_Drops dropGroup : dropGroups.list) {
			if(!dropGroup.compareTo(
		            victimType.toString(), 
		            dataVal,
		            weapon,
		            victim.getWorld(),
		            player,
		            parent.permissionHandler)) {
		        
		        continue;
		    }
			
			// Loop through drops
			for (OB_Drop drop : dropGroup.list) {
				parent.logInfo("VEHICLEDESTROY: Before compareto", 3);

				if(!drop.compareTo(
		            victimType.toString(), 
		            dataVal,
		            weapon,
		            victim.getWorld(),
		            player,
		            parent)) {
		        
		        continue;
		    }

			// Check probability is great than the RNG
			if(parent.rng.nextDouble() > (drop.chance.doubleValue()/100)) continue;

			if (drop.exclusive != null) {
				if (exclusive == null) { 
					exclusive = drop.exclusive;
					toBeDropped.clear();
				}
			}

			if (exclusive != null)
			{
				if (drop.exclusive.equals(exclusive))
				{
					doDrop = true;
				} else {
					doDrop = false;
				}
			} else {
				doDrop = true;
			}

			if (!doDrop) continue;

			if(drop.dropped.equalsIgnoreCase("DEFAULT")) {
				doDefaultDrop = true;
			}

			if(drop.dropped.equalsIgnoreCase("DENY")) { 
				denyBreak = true;
			} else {
				toBeDropped.add(drop);
			}

			Integer currentAttackerDamage = drop.getRandomAttackerDamage();
			maxAttackerDamage = (maxAttackerDamage < currentAttackerDamage) ? currentAttackerDamage : maxAttackerDamage;
		}
	}

		// Now do the drops
		if(attacker instanceof Player) player = (Player)event.getAttacker();
		
		for(OB_Drop obc : toBeDropped) OtherBlocks.performDrop(location, obc, player);

		//for(OB_Drop obc : toBeDropped) OtherBlocks.performDrop(target.getLocation(), obc, event.getPlayer());

	if(toBeDropped.size() > 0 && doDefaultDrop == false) {
			    // remove default drop
			    event.setCancelled(true);
				victim.remove();

				// Deal player damage if set
				if (player != null) {
					player.damage(maxAttackerDamage);
				}

	}
		
	}
}
