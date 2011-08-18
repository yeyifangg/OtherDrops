package com.gmail.zariust.bukkit.otherblocks.options.tool;

import org.bukkit.Material;
import org.bukkit.entity.CreatureType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.inventory.Inventory;

import com.gmail.zariust.bukkit.common.CommonEntity;

public class ProjectileAgent extends Agent {
	private CreatureAgent creature;
	private Material mat;
	Projectile agent;
	
	public ProjectileAgent() {
		this(null, null);
	}
	
	public ProjectileAgent(Material missile, CreatureType shooter) {
		this(missile, shooter, null);
	}
	
	public ProjectileAgent(Material missile, CreatureType shooter, Integer data) {
		super(ToolType.PROJECTILE);
		mat = missile;
		creature = new CreatureAgent(shooter, data);
	}
	
	public ProjectileAgent(Projectile missile) {
		this(CommonEntity.getProjectileType(missile), CommonEntity.getCreatureType(missile.getShooter()));
		agent = missile;
	}
	
	private ProjectileAgent equalsHelper(Object other) {
		if(!(other instanceof ProjectileAgent)) return null;
		return (ProjectileAgent) other;
	}

	private boolean isEqual(ProjectileAgent tool) {
		if(tool == null) return false;
		return creature == tool.creature && mat == tool.mat;
	}

	@Override
	public boolean equals(Object other) {
		ProjectileAgent tool = equalsHelper(other);
		return isEqual(tool);
	}

	@Override
	public boolean matches(Agent other) {
		ProjectileAgent tool = equalsHelper(other);
		if(mat == null) return true;
		else return isEqual(tool);
	}
	
	@Override
	protected int getIdHash() {
		return mat == null ? 0 : mat.getId();
	}
	
	@Override
	protected int getDataHash() {
		return creature == null ? 0 : creature.hashCode();
	}
	
	public CreatureAgent getShooter() {
		return creature;
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
