package com.gmail.zariust.bukkit.otherblocks.options.tool;

import org.bukkit.Material;
import org.bukkit.entity.CreatureType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.inventory.Inventory;

import com.gmail.zariust.bukkit.common.CommonEntity;

public class ProjectileAgent extends Agent {
	private CreatureType creature;
	int data;
	private Material mat;
	Projectile agent;
	
	public ProjectileAgent(Material missile, CreatureType shooter) {
		super(ToolType.PROJECTILE);
		mat = missile;
		creature = shooter;
	}
	
	public ProjectileAgent(Projectile missile) {
		this(CommonEntity.getProjectileType(missile), CommonEntity.getCreatureType(missile.getShooter()));
		agent = missile;
	}
	
	@Override
	protected boolean matches(Agent other) {
		if(other instanceof ProjectileAgent) return matches((ProjectileAgent) other);
		return false;
	}
	
	private boolean matches(ProjectileAgent other) {
		if(creature == null || other.creature == null || mat == null || other.mat == null) return true;
		return creature == other.creature && data == other.data && mat == other.mat && data == other.data;
	}
	
	@Override
	protected int getIdHash() {
		return (mat == null ? 0 : mat.getId()) ^ (creature == null ? 0 : creature.hashCode());
	}
	
	@Override
	protected int getDataHash() {
		return data;
	}
	
	public CreatureType getShooter() {
		return creature;
	}
	
	public int getShooterData() {
		return data;
	}
	
	public Material getProjectile() {
		return mat;
	}
	
	@Override
	public void damageTool(short damage) {
		// TODO: Probably the best move here is to drain items much like a bow drains arrows? But how to know which item?
		// Currently defaulting to the materials associated with each projectile in CommonEntity
		Inventory inven;
		if(agent.getShooter() == null) { // Dispenser!
			// TODO: How to retrieve the source dispenser?
			inven = null;
		} else if(agent.getShooter() instanceof Player) {
			inven = ((Player) agent.getShooter()).getInventory();
		} else return;
		// TODO: Now remove damage-1 of mat from inven
		
		// TODO: Option of failure if damage is greater that the amount remaining?
	}
	
	@Override
	public void damage(int amount) {
		agent.getShooter().damage(amount);
	}
}
