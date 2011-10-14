// OtherDrops - a Bukkit plugin
// Copyright (C) 2011 Robert Sargant, Zarius Tularial, Celtic Minstrel
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

package com.gmail.zariust.otherdrops.event;

import static com.gmail.zariust.common.Verbosity.*;

import java.util.Collections;
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
import org.bukkit.event.Cancellable;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.painting.PaintingBreakByEntityEvent;
import org.bukkit.event.painting.PaintingBreakEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.inventory.ItemStack;

import com.gmail.zariust.otherdrops.OtherDrops;
import com.gmail.zariust.otherdrops.event.AbstractDropEvent;
import com.gmail.zariust.otherdrops.options.ConfigOnly;
import com.gmail.zariust.otherdrops.options.Weather;
import com.gmail.zariust.otherdrops.options.Action;
import com.gmail.zariust.otherdrops.subject.*;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

/**
 * An actual drop that has occurred and may match one of the configured drops.
 */
public class OccurredDropEvent extends AbstractDropEvent implements Cancellable
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
	private Cancellable event;

	// Constructors
	public OccurredDropEvent(BlockBreakEvent evt) {
		super(new BlockTarget(evt.getBlock()),Action.BREAK);
		event = evt;
		Block block = evt.getBlock();
		setLocationWorldBiomeLight(block);
		setWeatherTimeHeight();
		attackRange = location.distance(evt.getPlayer().getLocation());
		setTool(evt.getPlayer());
		setRegions();
	}
	public OccurredDropEvent(final EntityDeathEvent evt) {
		super(getEntityTarget(evt.getEntity()),Action.BREAK);
		event = new Cancellable() {
			// Storing as an array is a crude way to get a copy
			private ItemStack[] drops = evt.getDrops().toArray(new ItemStack[0]);
			@Override
			public boolean isCancelled() {
				return evt.getDrops().isEmpty();
			}
			@Override
			public void setCancelled(boolean cancel) {
				if(cancel) {
					if (evt.getEntity() instanceof Player) {
						// FIXME: need to find out a way of determining if "drop: NOTHING" is set so we can clear the drops. 
					} else {
						evt.getDrops().clear();
					}
				}
				else Collections.addAll(evt.getDrops(), drops);
			}
		};
		Entity e = evt.getEntity();
		setLocationWorldBiomeLight(e);
		setWeatherTimeHeight();
		setTool(evt.getEntity().getLastDamageCause());
		if (tool.getLocation() == null) { // damage is environmental?
			attackRange = 0;
		} else {
			OtherDrops.logInfo("Measuring attack range for entity death! Attacker location: " + tool.getLocation() + "; target location: " + location, HIGH);
			attackRange = location.distance(tool.getLocation());
		}
		setRegions();
	}
	public OccurredDropEvent(EntityDamageEvent evt) {
		super(getEntityTarget(evt.getEntity()),Action.LEFT_CLICK);
		event = evt;
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
	public OccurredDropEvent(PaintingBreakEvent evt) {
		super(new VehicleTarget(evt.getPainting()),Action.BREAK);
		event = evt;
		Painting canvas = evt.getPainting();
		setLocationWorldBiomeLight(canvas);
		setWeatherTimeHeight();
		if(evt instanceof PaintingBreakByEntityEvent) {
			PaintingBreakByEntityEvent evt2 = (PaintingBreakByEntityEvent) evt;
			Entity remover = evt2.getRemover();
			attackRange = location.distance(remover.getLocation());
			setTool(remover);
		} else {
			switch(evt.getCause()) {
			case ENTITY:
				setTool(DamageCause.ENTITY_ATTACK);
				break;
			case FIRE:
				setTool(DamageCause.FIRE_TICK);
				break;
			case OBSTRUCTION:
				setTool(DamageCause.SUFFOCATION);
				break;
			case PHYSICS:
				setTool(DamageCause.CONTACT);
				break;
			case WATER:
				setTool(DamageCause.DROWNING);
				break;
			}
		}
		setRegions();
	}
	public OccurredDropEvent(LeavesDecayEvent evt) {
		super(new BlockTarget(evt.getBlock()),Action.LEAF_DECAY);
		event = evt;
		setLocationWorldBiomeLight(evt.getBlock());
		setWeatherTimeHeight();
		tool = null;
		setRegions();
	}
	public OccurredDropEvent(VehicleDestroyEvent evt) {
		super(new VehicleTarget(evt.getVehicle()),Action.BREAK);
		event = evt;
		setLocationWorldBiomeLight(evt.getVehicle());
		setWeatherTimeHeight();
		// environmental attacks (eg. burning) do not have a location, so range is not valid.
		if (evt.getAttacker() instanceof Player) {
			attackRange = location.distance(evt.getAttacker().getLocation());
		} else {
			attackRange = 0;
		}
		
		setTool(evt.getAttacker());
		setRegions();
	}
	public OccurredDropEvent(PlayerInteractEvent evt) {
		super(new BlockTarget(evt.getClickedBlock()),Action.fromInteract(evt.getAction()));
		event = evt;
		setLocationWorldBiomeLight(evt.getClickedBlock());
		face = evt.getBlockFace();
		setWeatherTimeHeight();
		attackRange = location.distance(evt.getPlayer().getLocation());
		setTool(evt.getPlayer());
		setRegions();
	}
	public OccurredDropEvent(PlayerInteractEntityEvent evt) {
		super(getEntityTarget(evt.getRightClicked()),Action.RIGHT_CLICK);
		event = evt;
		setLocationWorldBiomeLight(evt.getRightClicked());
		setWeatherTimeHeight();
		attackRange = location.distance(evt.getPlayer().getLocation());
		setTool(evt.getPlayer());
		setRegions();
	}
	public OccurredDropEvent(BlockFromToEvent evt) {
		super(new BlockTarget(evt.getToBlock()),Action.BREAK);
		event = evt;
		setLocationWorldBiomeLight(evt.getToBlock());
		setWeatherTimeHeight();
		tool = new EnvironmentAgent(DamageCause.CUSTOM);
		setRegions();
	}
	public OccurredDropEvent(EntityExplodeEvent evt, Block block) {
		super(new BlockTarget(block),Action.BREAK);
		event = evt;
		setLocationWorldBiomeLight(block);
		setWeatherTimeHeight();
		tool = new ExplosionAgent(evt.getEntity());
		setRegions();
	}
	// Generic constructors
	/**
	 * Create a drop with a block as its target.
	 * @param block The block.
	 * @param action The action that led to this drop (usually your custom action).
	 * @param agent The agent which caused this drop.
	 * @throws DropCreateException If you try to use a wildcard target or agent.
	 */
	public OccurredDropEvent(Block block, Action action, Agent agent) throws DropCreateException {
		super(new BlockTarget(block), action);
		event = null;
		setLocationWorldBiomeLight(block);
		setWeatherTimeHeight();
		setTool(agent);
		setRegions();
	}
	/**
	 * Create a drop with a block as its target and an entity agent.
	 * @param block The block.
	 * @param action The action that led to this drop (usually your custom action).
	 * @param agent The agent which caused this drop.
	 */
	public OccurredDropEvent(Block block, Action action, Entity agent) {
		super(new BlockTarget(block), action);
		event = null;
		setLocationWorldBiomeLight(block);
		setWeatherTimeHeight();
		setTool(agent);
		setRegions();
	}
	/**
	 * Create a cancellable drop with a block as its target.
	 * @param block The block.
	 * @param action The action that led to this drop (usually your custom action).
	 * @param agent The agent which caused this drop.
	 * @param evt An interface through which the default behaviour of this drop may be cancelled.
	 * @throws DropCreateException If you try to use a wildcard target or agent.
	 */
	public OccurredDropEvent(Block block, Action action, Agent agent, Cancellable evt) throws DropCreateException {
		this(block, action, agent);
		event = evt;
	}
	/**
	 * Create a cancellable drop with a block as its target and an entity agent.
	 * @param block The block.
	 * @param action The action that led to this drop (usually your custom action).
	 * @param agent The agent which caused this drop.
	 * @param evt An interface through which the default behaviour of this drop may be cancelled.
	 */
	public OccurredDropEvent(Block block, Action action, Entity agent, Cancellable evt) {
		this(block, action, agent);
		event = evt;
	}
	/**
	 * Create a drop with an entity as its target.
	 * @param entity The entity.
	 * @param action The action that led to this drop (usually your custom action).
	 * @param agent The agent which caused this drop.
	 * @throws DropCreateException If you try to use a wildcard target or agent.
	 */
	public OccurredDropEvent(Entity entity, Action action, Agent agent) throws DropCreateException {
		super(getEntityTarget(entity), action);
		event = null;
		setLocationWorldBiomeLight(entity);
		setTool(agent);
		setRegions();
	}
	/**
	 * Create a drop with an entity as its target and an entity agent.
	 * @param entity The entity.
	 * @param action The action that led to this drop (usually your custom action).
	 * @param agent The entity which caused this drop.
	 */
	public OccurredDropEvent(Entity entity, Action action, Entity agent) {
		super(getEntityTarget(entity), action);
		event = null;
		setLocationWorldBiomeLight(entity);
		setTool(agent);
		setRegions();
	}
	/**
	 * Create a cancellable drop with an entity as its target.
	 * @param entity The entity.
	 * @param action The action that led to this drop (usually your custom action).
	 * @param agent The agent which caused this drop.
	 * @param evt An interface through which the default behaviour of this drop may be cancelled.
	 * @throws DropCreateException If you try to use a wildcard target or agent.
	 */
	public OccurredDropEvent(Entity entity, Action action, Agent agent, Cancellable evt) throws DropCreateException {
		this(entity, action, agent);
		event = evt;
	}
	/**
	 * Create a cancellable drop with an entity as its target and an entity agent.
	 * @param entity The entity.
	 * @param action The action that led to this drop (usually your custom action).
	 * @param agent The entity which caused this drop.
	 * @param evt An interface through which the default behaviour of this drop may be cancelled.
	 */
	public OccurredDropEvent(Entity entity, Action action, Entity agent, Cancellable evt) {
		this(entity, action, agent);
		event = evt;
	}
	/**
	 * Create a drop with an arbitrary target.
	 * @param targ The target which was the source of this drop.
	 * @param action The action that led to this drop (usually your custom action).
	 * @param agent The agent which caused this drop.
	 * @throws DropCreateException If you try to use a wildcard target or agent.
	 */
	public OccurredDropEvent(Target targ, Action action, Agent agent) throws DropCreateException {
		super(targ, action, true);
		event = null;
		setLocationWorldBiomeLight(targ);
		setTool(agent);
		setRegions();
	}
	/**
	 * Create a drop with an arbitrary target and an entity agent.
	 * @param targ The target which was the source of this drop.
	 * @param action The action that led to this drop (usually your custom action).
	 * @param agent The entity which caused this drop.
	 * @throws DropCreateException If you try to use a wildcard target or agent.
	 */
	public OccurredDropEvent(Target targ, Action action, Entity agent) throws DropCreateException {
		super(targ, action, true);
		event = null;
		setLocationWorldBiomeLight(targ);
		setTool(agent);
		setRegions();
	}
	/**
	 * Create a cancellable drop with an arbitrary target.
	 * @param targ The target which was the source of this drop.
	 * @param action The action that led to this drop (usually your custom action).
	 * @param agent The agent which caused this drop.
	 * @param evt An interface through which the default behaviour of this drop may be cancelled.
	 * @throws DropCreateException If you try to use a wildcard target or agent.
	 */
	public OccurredDropEvent(Target targ, Action action, Agent agent, Cancellable evt) throws DropCreateException {
		this(targ, action, agent);
		event = evt;
	}
	/**
	 * Create a cancellable drop with an arbitrary target and an entity agent.
	 * @param targ The target which was the source of this drop.
	 * @param action The action that led to this drop (usually your custom action).
	 * @param agent The entity which caused this drop.
	 * @param evt An interface through which the default behaviour of this drop may be cancelled.
	 * @throws DropCreateException If you try to use a wildcard target or agent.
	 */
	public OccurredDropEvent(Target targ, Action action, Entity agent, Cancellable evt) throws DropCreateException {
		this(targ, action, agent);
		event = evt;
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
	private void setLocationWorldBiomeLight(Target targ) {
		location = targ.getLocation();
		world = location.getWorld();
		biome = world.getBiome(location.getBlockX(), location.getBlockZ());
		lightLevel = world.getBlockAt(location).getLightLevel();
	}
	private void setRegions() {
		regions = new HashSet<String>();
		if(OtherDrops.worldguardPlugin == null) return;
		WorldGuardPlugin wg = OtherDrops.worldguardPlugin;
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
	private void setTool(Agent agent) throws DropCreateException {
		if(agent.getClass().isAnnotationPresent(ConfigOnly.class)) {
			ConfigOnly annotate = agent.getClass().getAnnotation(ConfigOnly.class);
			throw new DropCreateException(agent.getClass(), annotate.value());
		}
		tool = agent;
	}
	private void setTool(Entity damager) {
		if(damager instanceof Player)
			tool = new PlayerSubject((Player) damager);
		else if(damager instanceof Projectile)
			tool = new ProjectileAgent((Projectile) damager);
		else if(damager instanceof LightningStrike)
			// TODO: Is there any use in passing the lightning entity through here?
			tool = new EnvironmentAgent(DamageCause.LIGHTNING);
		else if(damager instanceof LivingEntity)
			tool = new CreatureSubject((LivingEntity) damager);
	}
	private void setTool(EntityDamageEvent lastDamage) {
		// This is for EntityDeathEvent
		// Check if the damager is a player - if so, weapon is the held tool
		if(lastDamage instanceof EntityDamageByEntityEvent) {
			EntityDamageByEntityEvent e = (EntityDamageByEntityEvent) lastDamage;
			if(e.getDamager() instanceof Player) {
				tool = new PlayerSubject((Player) e.getDamager());
				return;
			} else if (e.getDamager() instanceof Projectile) {
				tool = new ProjectileAgent((Projectile) e.getDamager()); 
				return;
			} else if(e.getDamager() instanceof LivingEntity) {
				tool = new CreatureSubject((LivingEntity) e.getDamager());
				return;
			} else {
				// The only other one I can think of is lightning, which would be covered by the non-entity code
				// But just in case, log it.
				OtherDrops.logInfo("A " + lastDamage.getEntity().getClass().getSimpleName() + " was damaged by a "
						+ e.getDamager().getClass().getSimpleName(), HIGHEST);
			}
		}
		// Damager was not a person - check damage types
		DamageCause cause = lastDamage.getCause();
		if(cause == DamageCause.CUSTOM) return; // We don't handle custom damage
		// Used to ignore void damage as well, but since events were added I can see some use for it.
		// For example, a lightning strike when someone falls off the bottom of the map.
		tool = new EnvironmentAgent(cause);
	}
	private static Target getEntityTarget(Entity what) {
		if(what instanceof Player) return new PlayerSubject((Player) what);
		else if(what instanceof LivingEntity) return new CreatureSubject((LivingEntity) what);
		else if(what instanceof Vehicle) return new VehicleTarget((Vehicle) what);
		else if(what instanceof Painting) return new VehicleTarget((Painting) what);
		OtherDrops.logWarning("Error: unknown entity target ("+what.toString()+") - please let the developer know.");
		return null; // Ideally this return is unreachable
	}
	
	// Accessors
	/**
	 * @return The agent that caused this event.
	 */
	public Agent getTool() {
		return tool;
	}
	/**
	 * @return The location at which the event occurred.
	 */
	public Location getLocation() {
		return location;
	}
	public void setLocation(Location newLocation) {
		location = newLocation;
	}
	/**
	 * @return The world in which the event occurred.
	 */
	public World getWorld() {
		return world;
	}
	/**
	 * @return The set of WorldGuard regions that contain the location of the event.
	 */
	public Set<String> getRegions() {
		return regions;
	}
	/**
	 * @return The weather conditions at the time of the event.
	 */
	public Weather getWeather() {
		return weather;
	}
	/**
	 * @return The block face that was hit, if applicable, or null otherwise.
	 */
	public BlockFace getFace() {
		return face;
	}
	/**
	 * @return The biome in which the event occurred.
	 */
	public Biome getBiome() {
		return biome;
	}
	/**
	 * @return The (in-game) time of day at which the event occurred.
	 */
	public long getTime() {
		return time;
	}
	/**
	 * @return The height above bedrock at which the event occurred.
	 */
	public int getHeight() {
		return height;
	}
	/**
	 * @return The distance the agent was from the target at the time of the event.
	 */
	public double getAttackRange() {
		return attackRange;
	}
	/**
	 * @return The light level at the location of the event when it occurred.
	 */
	public int getLightLevel() {
		return lightLevel;
	}
	
	// Matching!
	@Override
	public boolean matches(AbstractDropEvent other) {
		if(other instanceof OccurredDropEvent) {
			return equals(other);
		} else if(other instanceof CustomDropEvent || other instanceof GroupDropEvent) {
			return other.matches(this);
		}
		return false;
	}
	
	@Override
	public String getLogMessage() {
		// TODO: Hm, how should this log message go? It would be used if you were logging actual event firing
		return getAction().toString() + " on " + 
			((getTarget() == null) ? "<no block>" : getTarget().toString() + " with " + 
			((getTool() == null) ? "<no tool> " : getTool().toString()));
	}
	
	@Override
	public boolean isCancelled() {
		if(event != null) return event.isCancelled();
		return false;
	}
	
	@Override
	public void setCancelled(boolean cancel) {
		if(event != null) event.setCancelled(cancel);
	}
	
	public Cancellable getEvent() {
		return this.event;
	}
}
