package com.gmail.zariust.bukkit.otherblocks.options.tool;

import org.bukkit.Material;
import org.bukkit.entity.CreatureType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Explosive;
import org.bukkit.entity.LivingEntity;

import com.gmail.zariust.bukkit.common.CommonEntity;
import com.gmail.zariust.bukkit.otherblocks.drops.AbstractDrop;
import com.gmail.zariust.bukkit.otherblocks.options.drop.ItemType;

public class ExplosionAgent implements Agent {
	private CreatureAgent creature;
	private Material explosive;
	//private Explosive bomb; // Creeper doesn't implement Explosive yet...
	private Entity bomb;
	
	public ExplosionAgent() { // Wildcard
		this(null, null);
	}
	
	public ExplosionAgent(CreatureType boom) { // Creature explosion
		this(new CreatureAgent(boom), null);
	}
	
	public ExplosionAgent(Material boom) { // Non-creature explosion
		this(null, boom);
	}
	
	// TODO: Entity -> Explosive
	public ExplosionAgent(Entity boom) { // Actual explosion
		this(new CreatureAgent(CommonEntity.getCreatureType(boom)), CommonEntity.getExplosiveType(boom));
	}
	
	private ExplosionAgent(CreatureAgent agent, Material mat) { // Rome
		creature = agent;
		explosive = mat;
	}

	public boolean isCreature() {
		return bomb instanceof LivingEntity;
	}
	
	public CreatureType getCreature() {
		return creature == null ? null : creature.getCreature();
	}
	
	public Material getExplosiveType() {
		return explosive;
	}
	
	@Override
	public boolean equals(Object other) {
		if(!(other instanceof ExplosionAgent)) return false;
		if(creature == null && explosive == null) return true;
		ExplosionAgent tool = (ExplosionAgent) other;
		if(creature == null) return explosive == tool.explosive;
		return creature.matches((Agent) tool.creature);
	}
	
	@Override
	public int hashCode() {
		return AbstractDrop.hashCode(ItemType.EXPLOSION, creature == null ? 0 : creature.hashCode(), explosive == null ? null : explosive.hashCode());
	}
	
	@Override
	public boolean matches(Agent other) {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public ItemType getType() {
		return ItemType.EXPLOSION;
	}
	
	@Override public void damage(int amount) {}
	
	@Override public void damageTool(short amount) {}
	
	@Override public void damageTool() {}
	
}
