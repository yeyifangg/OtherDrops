package com.gmail.zariust.bukkit.otherblocks.subject;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.CreatureType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Explosive;
import org.bukkit.entity.LivingEntity;

import com.gmail.zariust.bukkit.common.CommonEntity;
import com.gmail.zariust.bukkit.otherblocks.data.CreatureData;
import com.gmail.zariust.bukkit.otherblocks.drops.AbstractDrop;

public class ExplosionAgent implements Agent {
	private CreatureSubject creature;
	private Material explosive;
	//private Explosive bomb; // Creeper doesn't implement Explosive yet...
	private Entity bomb;
	
	public ExplosionAgent() { // Wildcard
		this(null, (Material)null);
	}
	
	public ExplosionAgent(CreatureType boom) { // Creature explosion
		this(new CreatureSubject(boom), null);
	}
	
	public ExplosionAgent(CreatureType boom, int data) {
		this(new CreatureSubject(boom, data), null);
	}
	
	public ExplosionAgent(CreatureType boom, CreatureData data) {
		this(new CreatureSubject(boom, data), null);
	}
	
	public ExplosionAgent(Material boom) { // Non-creature explosion
		this(null, boom);
	}
	
	// TODO: Entity -> Explosive (if the API changes so Creeper implements Explosive)
	public ExplosionAgent(Entity boom) { // Actual explosion
		this(new CreatureSubject(CommonEntity.getCreatureType(boom)), CommonEntity.getExplosiveType(boom));
		bomb = boom;
	}
	
	private ExplosionAgent(CreatureSubject agent, Material mat) { // Rome
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
		return AbstractDrop.hashCode(ItemCategory.EXPLOSION, creature == null ? 0 : creature.hashCode(), explosive == null ? null : explosive.hashCode());
	}
	
	@Override
	public boolean matches(Subject other) {
		if(!(other instanceof ExplosionAgent)) return false;
		if(creature == null && explosive == null) return true;
		if(explosive == null) return creature.equals(((ExplosionAgent)other).creature);
		return explosive == ((ExplosionAgent)other).explosive;
	}
	
	@Override
	public ItemCategory getType() {
		return ItemCategory.EXPLOSION;
	}

	public static Agent parse(String name, String data) {
		name = name.toUpperCase().replace("EXPLOSION_", "");
		if(name.equals("TNT")) return new ExplosionAgent(Material.TNT);
		else if(name.equals("FIRE") || name.equals("FIREBALL"))
			return new ExplosionAgent(Material.FIRE);
		// TODO: Zarius said fromName didn't work?
		CreatureType creature = CreatureType.fromName(name);
		CreatureData cdata = CreatureData.parse(creature, data);
		if(cdata != null) return new ExplosionAgent(creature, cdata);
		return new ExplosionAgent(creature);
	}
	
	@Override public void damage(int amount) {}
	
	@Override public void damageTool(short amount) {}
	
	@Override public void damageTool() {}

	@Override
	public Location getLocation() {
		if(bomb != null) return bomb.getLocation();
		return null;
	}

	@Override
	public String toString() {
		if(creature != null) return creature.toString().replace("CREATURE_", "EXPLOSION_");
		if(explosive != null) return "EXPLOSION_" + explosive.toString();
		return "ANY_EXPLOSION";
	}
}
