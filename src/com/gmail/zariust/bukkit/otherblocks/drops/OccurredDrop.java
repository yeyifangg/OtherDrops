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
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.	 See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program.	 If not, see <http://www.gnu.org/licenses/>.

package com.gmail.zariust.bukkit.otherblocks.drops;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LightningStrike;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Painting;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.painting.PaintingBreakByEntityEvent;
import org.bukkit.event.painting.PaintingBreakEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;

import com.gmail.zariust.bukkit.otherblocks.OtherBlocks;
import com.gmail.zariust.bukkit.otherblocks.drops.AbstractDrop;
import com.gmail.zariust.bukkit.otherblocks.options.Weather;
import com.gmail.zariust.bukkit.otherblocks.options.action.Action;
import com.gmail.zariust.bukkit.otherblocks.options.target.*;
import com.gmail.zariust.bukkit.otherblocks.options.tool.*;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class OccurredDrop extends AbstractDrop
{
	private Agent tool;
	private World world;
	private Set<String> regions;
	private Weather weather;
	private BlockFace face;
	private Biome biome;
	private long time;
	private int height;
	private double attackRange;
	private int lightLevel;
	private Location location;
//	private String dropped;
//	
//	private Range<Short> originalData;
//	private Range<Short> dropData;

	// Constructors
	public OccurredDrop(BlockBreakEvent evt) {
		super(new BlockTarget(evt.getBlock()),Action.BREAK);
		Block block = evt.getBlock();
		setLocationWorldBiomeLight(block);
		setWeatherTimeHeight();
		attackRange = location.distance(evt.getPlayer().getLocation());
		setTool(evt.getPlayer());
		setRegions();
	}
	public OccurredDrop(EntityDeathEvent evt) {
		super(getEntityTarget(evt.getEntity()),Action.BREAK);
		Entity e = evt.getEntity();
		setLocationWorldBiomeLight(e);
		setWeatherTimeHeight();
		tool = OtherBlocks.plugin.damagerList.get(evt.getEntity());
		attackRange = location.distance(tool.getLocation());
		setRegions();
	}
	public OccurredDrop(EntityDamageEvent evt) {
		super(getEntityTarget(evt.getEntity()),Action.LEFT_CLICK);
		Entity e = evt.getEntity();
		setLocationWorldBiomeLight(e);
		setWeatherTimeHeight();
		if(evt instanceof EntityDamageByEntityEvent) {
			EntityDamageByEntityEvent evt2 = (EntityDamageByEntityEvent) evt;
			attackRange = location.distance(evt2.getDamager().getLocation());
			setTool(evt2.getDamager());
		} else setTool(evt.getCause());
		setRegions();
	}
	public OccurredDrop(PaintingBreakEvent evt) {
		super(new BlockTarget(evt.getPainting()),Action.BREAK);
		Painting canvas = evt.getPainting();
		setLocationWorldBiomeLight(canvas);
		setWeatherTimeHeight();
		if(evt instanceof PaintingBreakByEntityEvent) {
			PaintingBreakByEntityEvent evt2 = (PaintingBreakByEntityEvent) evt;
			Entity remover = evt2.getRemover();
			attackRange = location.distance(remover.getLocation());
			setTool(remover);
		} else {
			// TODO: Determining cause is difficult; any ideas?
		}
		setRegions();
	}
	public OccurredDrop(LeavesDecayEvent evt) {
		// TODO: Actually, shouldn't the target include the block?
		super(new BlockTarget(evt.getBlock()),Action.BREAK);
		setLocationWorldBiomeLight(evt.getBlock());
		setWeatherTimeHeight();
		tool = null;
		setRegions();
	}
	public OccurredDrop(VehicleDestroyEvent evt) {
		super(new BlockTarget(evt.getVehicle()),Action.BREAK);
		setLocationWorldBiomeLight(evt.getVehicle());
		setWeatherTimeHeight();
		attackRange = location.distance(evt.getAttacker().getLocation());
		setTool(evt.getAttacker());
		setRegions();
	}
	public OccurredDrop(PlayerInteractEvent evt) {
		super(new BlockTarget(evt.getClickedBlock()),Action.fromInteract(evt.getAction()));
		setLocationWorldBiomeLight(evt.getClickedBlock());
		face = evt.getBlockFace();
		setWeatherTimeHeight();
		attackRange = location.distance(evt.getPlayer().getLocation());
		setTool(evt.getPlayer());
		setRegions();
	}
	public OccurredDrop(PlayerInteractEntityEvent evt) {
		super(getEntityTarget(evt.getRightClicked()),Action.RIGHT_CLICK);
		setLocationWorldBiomeLight(evt.getRightClicked());
		setWeatherTimeHeight();
		attackRange = location.distance(evt.getPlayer().getLocation());
		setTool(evt.getPlayer());
		setRegions();
	}
	public OccurredDrop(BlockFromToEvent evt) {
		super(new BlockTarget(evt.getToBlock()),Action.BREAK);
		setLocationWorldBiomeLight(evt.getToBlock());
		setWeatherTimeHeight();
		tool = new EnvironmentAgent(DamageCause.CUSTOM);
		setRegions();
	}
	
	// Constructor helpers
	private void setWeatherTimeHeight() {
		weather = Weather.match(biome, world.hasStorm(), world.isThundering());
		time = world.getTime();
		height = location.getBlockY();
	}
	private void setLocationWorldBiomeLight(Block block) {
		location = block.getLocation();
		world = block.getWorld();
		biome = block.getBiome();
		lightLevel = block.getLightLevel();
	}
	private void setLocationWorldBiomeLight(Entity e) {
		location = e.getLocation();
		world = e.getWorld();
		biome = world.getBiome(location.getBlockX(), location.getBlockZ());
		lightLevel = world.getBlockAt(location).getLightLevel();
	}
	private void setRegions() {
		regions = new HashSet<String>();
		if(OtherBlocks.worldguardPlugin == null) return;
		WorldGuardPlugin wg = OtherBlocks.worldguardPlugin;
		Map<String, ProtectedRegion> regionMap = wg.getGlobalRegionManager().get(world).getRegions();
		Vector vec = new Vector(location.getX(), location.getY(), location.getZ());
		for(String region : regionMap.keySet()) {
			if(regionMap.get(region).contains(vec))
				regions.add(region);
		}
	}
	private void setTool(DamageCause cause) {
		tool = new EnvironmentAgent(cause);
	}
	private void setTool(Entity damager) {
		if(damager instanceof Player)
			tool = new PlayerAgent((Player) damager);
		else if(damager instanceof Projectile)
			tool = new ProjectileAgent((Projectile) damager);
		else if(damager instanceof LightningStrike) // TODO: Is there any use in passing the lightning entity through here?
			tool = new EnvironmentAgent(DamageCause.LIGHTNING);
		else if(damager instanceof LivingEntity)
			tool = new CreatureAgent((LivingEntity) damager);
	}
	private static Target getEntityTarget(Entity what) {
		if(what instanceof Player) return new PlayerAgent((Player) what);
		else if(what instanceof LivingEntity) return new CreatureAgent((LivingEntity) what);
		else if(what instanceof Vehicle) return new BlockTarget((Vehicle) what);
		else if(what instanceof Painting) return new BlockTarget((Painting) what);
		// TODO: Are there any other cases to handle?
		return null; // Ideally this return is unreachable
	}
	
	// Accessors
	public Agent getTool() {
		return tool;
	}
	public Location getLocation() {
		return location;
	}
	public World getWorld() {
		return world;
	}
	public Set<String> getRegions() {
		return regions;
	}
	public Weather getWeather() {
		return weather;
	}
	public BlockFace getFace() {
		return face;
	}
	public Biome getBiome() {
		return biome;
	}
	public long getTime() {
		return time;
	}
	public int getHeight() {
		return height;
	}
	public double getAttackRange() {
		return attackRange;
	}
	public int getLightLevel() {
		return lightLevel;
	}
	
	// Matching!
	@Override
	public boolean matches(AbstractDrop other) {
		if(other instanceof OccurredDrop) {
			return equals(other);
		} else if(other instanceof CustomDrop || other instanceof DropGroup) {
			return other.matches(this);
		}
		return false;
	}
	@Override
	public String getLogMessage() {
		// TODO Hm, how should this log message go?
		return null;
	}
}
