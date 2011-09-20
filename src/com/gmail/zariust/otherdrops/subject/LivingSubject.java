package com.gmail.zariust.otherdrops.subject;

import com.gmail.zariust.otherdrops.OtherDrops;

import org.bukkit.block.Block;
import org.bukkit.entity.Entity;

public abstract class LivingSubject implements Agent, Target {
	private Entity entity;
	
	protected LivingSubject(Entity e) {
		entity = e;
	}
	
	@Override
	public void setTo(BlockTarget replacement) {
		if(entity == null) {
			OtherDrops.logWarning("LivingSubject had a null entity; could not remove it and replace with blocks.");
			return;
		}
		// TODO: A way to replace the blocks in all the locations they occupy?
		Block bl = entity.getLocation().getBlock();
		new BlockTarget(bl).setTo(replacement);
		entity.remove();
	}
	
	public Entity getEntity() {
		return entity;
	}
}
