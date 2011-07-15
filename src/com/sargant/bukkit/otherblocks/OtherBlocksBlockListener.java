// OtherBlocks - a Bukkit plugin
// Copyright (C) 2011 Robert Sargant
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program.  If not, see <http://www.gnu.org/licenses/>.

package com.sargant.bukkit.otherblocks;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.*; 
import org.bukkit.block.Block;
import org.bukkit.event.block.*;
import org.bukkit.inventory.ItemStack;

public class OtherBlocksBlockListener extends BlockListener
{
	private OtherBlocks parent;

	public OtherBlocksBlockListener(OtherBlocks instance) {
		parent = instance;
	}
	
	@Override
	public void onLeavesDecay(LeavesDecayEvent event) {
		
		if(event.isCancelled()) return;

		boolean successfulComparison = false;
		boolean doDefaultDrop = false;
		boolean denyBreak = false;
		Block target = event.getBlock();
		
		String exclusive = null;
		Boolean doDrop = true;

	    // Get the leaf's data value
        // Beware of the 0x4 bit being set - use a bitmask of 0x3
        Short leafData = (short) ((0x3) & event.getBlock().getData());

		List<OB_Drop> toBeDropped = new ArrayList<OB_Drop>();
		
                parent.logInfo("LEAFDECAY: before check.", 3);

		// grab the relevant collection of dropgroups
		OBContainer_DropGroups dropGroups = parent.config.blocksHash.get("SPECIAL_LEAFDECAY");

		// loop through dropgroups
		if (dropGroups == null) {
			parent.logWarning("LEAFDECAY: warning - dropGroups is null!", 2);
			return;
		}
		for (OBContainer_Drops dropGroup : dropGroups.list) {
			if(!dropGroup.compareTo(
					"SPECIAL_LEAFDECAY", 
					leafData, 
					Material.AIR.toString(), 
					target.getWorld(),
					null,
					parent.permissionHandler)) {
				continue;
			}


			// Loop through drops
			for (OB_Drop drop : dropGroup.list) {
                                parent.logInfo("LEAFDECAY: before compareto.", 5);
				if(!drop.compareTo(
				"SPECIAL_LEAFDECAY", 
				leafData, 
				Material.AIR.toString(), 
				target.getWorld(),
				null,
				parent.permissionHandler)) {
					continue;
				}
                                parent.logInfo("LEAFDECAY: after compareto.", 5);

				// Check RNG is OK
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
			}
		}

		// Note: for leafdecay it's dangerous to combine drops and denybreak
		// as this can lead to a full set of drops each time the leaves are checked
		// which can be simulated by placing a log or leaf next to another and destroying it
		// Hence: we disable drops here if denybreak is true.
		if (!denyBreak) {
			for(OB_Drop obc : toBeDropped) OtherBlocks.performDrop(target.getLocation(), obc, null);
		} else {
			if (toBeDropped.size() > 1)
				parent.logWarning("LEAFDECAY: DENYBREAK combined with drops on leaf decay is dangerous - disabling drops.", 2);
		}

		if(toBeDropped.size() > 0 && doDefaultDrop == false) {
			// Convert the target block
			event.setCancelled(true);
			if (!denyBreak) {
				target.setType(Material.AIR);
			} else {
				// set data to make sure leafs don't keep trying to decay
				target.setData(leafData.byteValue());
			}
		}
			
	}

	@Override
	public void onBlockBreak(BlockBreakEvent event)
	{
		if (event.isCancelled()) return;

		boolean otherblocksActive = true;

		if (parent.permissionsPlugin != null) {
			if (!(parent.permissionHandler.has(event.getPlayer(), "otherblocks.active"))) {
				otherblocksActive = false;
			}			
		}

		if (otherblocksActive) {

			Block target  = event.getBlock();
			ItemStack tool = event.getPlayer().getItemInHand();
			Integer maxDamage = 0;
			boolean doDefaultDrop = false;
			boolean denyBreak = false;
			boolean doDrop = true;
			String exclusive = null;
			Integer maxAttackerDamage = 0;

			List<OB_Drop> toBeDropped = new ArrayList<OB_Drop>();
			
			parent.logInfo("BLOCKBREAK: before check.", 3);

			// grab the relevant collection of dropgroups
			Integer blockInt = event.getBlock().getTypeId();
			OBContainer_DropGroups dropGroups = parent.config.blocksHash.get(blockInt.toString());

			// loop through dropgroups
			if (dropGroups == null) {
				parent.logWarning("BLOCKBREAK("+event.getBlock().getType().toString()+"): warning - dropGroups is null!", 3);
				return;
			}
			for (OBContainer_Drops dropGroup : dropGroups.list) {
				if(!dropGroup.compareTo(
						event.getBlock(),
						(short) event.getBlock().getData(),
						tool.getType().toString(), 
						target.getWorld(),
						event.getPlayer(),
						parent.permissionHandler)) {

					continue;
				}

				// Loop through drops
				for (OB_Drop drop : dropGroup.list) {
					parent.logInfo("BLOCKBREAK: before compareto.", 5);
					if(!drop.compareTo(
							event.getBlock(),
							(short) event.getBlock().getData(),
							tool.getType().toString(), 
							target.getWorld(),
							event.getPlayer(),
							parent.permissionHandler)) {

						continue;
					}
					parent.logInfo("BLOCKBREAK: after compareto.", 5);

					// Check probability is great than the RNG
					if(parent.rng.nextDouble() > (drop.chance.doubleValue()/100)) continue;

					// At this point, the tool and the target block match
					//successfulComparison = true;
					//if(obc.dropped.equalsIgnoreCase("DEFAULT")) doDefaultDrop = true;

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


					maxDamage = (maxDamage < drop.damage) ? drop.damage : maxDamage;

					Integer currentAttackerDamage = drop.getRandomAttackerDamage();
					maxAttackerDamage = (maxAttackerDamage < currentAttackerDamage) ? currentAttackerDamage : maxAttackerDamage;
				}
			}

			for(OB_Drop obc : toBeDropped) OtherBlocks.performDrop(target.getLocation(), obc, event.getPlayer());

			if(toBeDropped.size() > 0 && doDefaultDrop == false) {
				// save block name for later
				String blockName = event.getBlock().getType().toString();
				// give a chance for logblock (if available) to log the block destruction
				OtherBlocks.queueBlockBreak(event.getPlayer().getName(), event.getBlock().getState());

				// Convert the target block
				event.setCancelled(true);
				if (!denyBreak) target.setType(Material.AIR);

				// Deal player damage if set
				if (event.getPlayer() != null) {
					event.getPlayer().damage(maxAttackerDamage);
				}

				// Check the tool can take wear and tear
				if(tool.getType().getMaxDurability() < 0 || tool.getType().isBlock()) return;

				// Now adjust the durability of the held tool
				parent.logInfo("BLOCKBREAK("+blockName+"): doing "+maxDamage+" damage to tool.", 3);
				tool.setDurability((short) (tool.getDurability() + maxDamage));

				// Manually check whether the tool has exceed its durability limit
				if(tool.getDurability() >= tool.getType().getMaxDurability()) {
					event.getPlayer().setItemInHand(null);
				}
			}
		}

	}
	
	@Override
	public void onBlockFromTo(BlockFromToEvent event) {
/*//temp disabled - not working anyway
		if (event.isCancelled()) return;
		if(event.getBlock().getType() != Material.WATER && event.getBlock().getType() != Material.STATIONARY_WATER)
			return;
		if(event.getToBlock().getType() == Material.AIR) return;

		Block target  = event.getToBlock();
		Integer maxDamage = 0;
		boolean successfulComparison = false;
		boolean doDefaultDrop = false;

		for(OB_Drop obc : parent.transformList) {
		    
		    if(!obc.compareTo(
		            event.getBlock().getType().toString(),
		            (short) event.getBlock().getData(),
		            "DAMAGE_WATER", 
		            target.getWorld(),
		            null,
		            parent.permissionHandler)) {
		        
		        continue;
		    }

		    // Check probability is great than the RNG
			if(parent.rng.nextDouble() > (obc.chance.doubleValue()/100)) continue;

			// At this point, the tool and the target block match
			successfulComparison = true;
			if(obc.dropped.equalsIgnoreCase("DEFAULT")) doDefaultDrop = true;
			OtherBlocks.performDrop(target.getLocation(), obc, null);
			maxDamage = (maxDamage < obc.damage) ? obc.damage : maxDamage;
		}

		if(successfulComparison && !doDefaultDrop) {

			// Convert the target block
			event.setCancelled(true);
			target.setType(Material.AIR);
		}*/
	}
}

