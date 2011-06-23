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

import com.sargant.bukkit.common.CommonEntity;

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

		String weapon;
		if(attacker instanceof Player)
			weapon = ((Player) attacker).getItemInHand().getType().toString();
		else {
			CreatureType creatureType = CommonEntity.getCreatureType(attacker);
			if(creatureType == null) return;
			weapon = "CREATURE_" + creatureType.toString();
		}
		
		Material victimType = CommonEntity.getVehicleType(victim);
		if(victimType == null) return;
		
		Location location = victim.getLocation();
		List<OtherBlocksContainer> drops = new ArrayList<OtherBlocksContainer>();
		boolean doDefaultDrop = false;
		
		for(OtherBlocksContainer obc : parent.transformList) {
			
		    Short dataVal = (victim instanceof Colorable) ? ((short) ((Colorable) victim).getColor().getData()) : null;
			
		    if(!obc.compareTo(
		            victimType.toString(), 
		            dataVal,
		            weapon,
		            victim.getWorld().getName())) {
		        
		        continue;
		    }

			// Check probability is great than the RNG
			if(parent.rng.nextDouble() > (obc.chance.doubleValue()/100)) continue;

			if(obc.dropped.equalsIgnoreCase("DEFAULT")) {
			    doDefaultDrop = true;
			} else {
			    drops.add(obc);
				
			    // remove default drop
			    event.setCancelled(true);
				victim.remove();
			}
		}
		
		// Now do the drops
		if(attacker instanceof Player) {
			for(OtherBlocksContainer obc : drops) OtherBlocks.performDrop(location, obc, (Player)event.getAttacker());
		} else {
			for(OtherBlocksContainer obc : drops) OtherBlocks.performDrop(location, obc, null);
			
		}
	}
}
