package com.gmail.zariust.bukkit.otherblocks.options.tool;

import org.bukkit.Material;
import org.bukkit.entity.CreatureType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.inventory.Inventory;

import com.gmail.zariust.bukkit.common.CommonEntity;
import com.gmail.zariust.bukkit.otherblocks.drops.AbstractDrop;
import com.gmail.zariust.bukkit.otherblocks.options.drop.ItemType;

public class ProjectileAgent implements Agent {
	private LivingAgent creature;
	private boolean dispenser;
	private Material mat;
	Projectile agent;
	
	public ProjectileAgent() { // The wildcard
		this(null, false);
	}
	
	public ProjectileAgent(Material missile, boolean isDispenser) { // True = dispenser, false = partial wildcard
		this(missile, null, isDispenser);
	}
	
	public ProjectileAgent(Material missile, CreatureType shooter) { // Shot by a creature
		this(missile, new CreatureAgent(shooter), false);
	}
	
	public ProjectileAgent(Material missile, String shooter) { // Shot by a player
		this(missile, new PlayerAgent(shooter), false);
	}
	
	public ProjectileAgent(Projectile missile) { // For actual drops that have already occurred
		this( // Sorry, this is kinda complex here; why must Java insist this() be on the first line?
			getProjectileType(missile), // Get the Material representing the type of projectile
			getShooterAgent(missile), // Get the LivingAgent representing the shooter
			missile.getShooter() == null // If shooter is null, it's a dispenser
		);
	}
	
	private ProjectileAgent(Material missile, LivingAgent shooter, boolean isDispenser) { // The Rome constructor
		mat = missile;
		creature = shooter;
		dispenser = isDispenser;
	}

	private static Material getProjectileType(Projectile missile) {
		return CommonEntity.getProjectileType(missile);
	}
	
	private static LivingAgent getShooterAgent(Projectile missile) {
		// Get the LivingAgent representing the shooter, which could be null, a CreatureAgent, or a PlayerAgent
		LivingEntity shooter = missile.getShooter();
		if(shooter == null) return null;
		else if(shooter instanceof Player) return new PlayerAgent((Player) shooter);
		else return new CreatureAgent(getShooterType(shooter));
		
	}

	private static int getShooterData(LivingEntity shooter) {
		return CommonEntity.getCreatureData(shooter);
	}

	private static CreatureType getShooterType(LivingEntity shooter) {
		return CommonEntity.getCreatureType(shooter);
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
		if(dispenser && tool.dispenser) return true;
		else return isEqual(tool);
	}

	@Override
	public int hashCode() {
		return AbstractDrop.hashCode(ItemType.PROJECTILE, mat == null ? 0 : mat.getId(), creature == null ? 0 : creature.hashCode());
	}
	
	public LivingAgent getShooter() {
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

	public CreatureType getCreature() {
		return getShooterType(agent.getShooter());
	}

	public int getCreatureData() {
		return getShooterData(agent.getShooter());
	}

	@Override
	public ItemType getType() {
		return ItemType.PROJECTILE;
	}

	@Override public void damageTool() {}

	public static Agent parse(String name, String data) {
		name = name.substring(11);
		Material mat;
		if(name.equals("FIRE") || name.equals("FIREBALL"))
			mat = Material.FIRE;
		else if(name.equals("SNOW_BALL"))
			mat = Material.SNOW_BALL;
		else if(name.equals("EGG"))
			mat = Material.EGG;
		else if(name.equals("FISH") || name.equals("FISHING_ROD"))
			mat = Material.FISHING_ROD;
		else if(name.equals("ARROW"))
			mat = Material.ARROW;
		else return null;
		// Parse data, which is one of the following
		// - A CreatureType constant (note that only GHAST and SKELETON will actually do anything
		//   unless there's some other plugin making entities shoot things)
		// - One of the special words PLAYER or DISPENSER
		// - Something else, which is taken to be a player name
		// - Nothing
		if(data.isEmpty()) return new ProjectileAgent(mat, false); // Specific projectile, any shooter
		CreatureType creature = CreatureType.fromName(data);
		if(creature != null) return new ProjectileAgent(mat, creature);
		if(data.equalsIgnoreCase("DISPENSER")) return new ProjectileAgent(mat, true);
		else if(data.equalsIgnoreCase("PLAYER")) return new ProjectileAgent(mat, (String) null);
		return new ProjectileAgent(mat, data);
	}
}
