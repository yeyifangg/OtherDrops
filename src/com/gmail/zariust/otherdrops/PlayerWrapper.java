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

package com.gmail.zariust.otherdrops;

import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Achievement;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Instrument;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Note;
import org.bukkit.Server;
import org.bukkit.Statistic;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Egg;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.map.MapView;
import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

public class PlayerWrapper implements Player
{
	private Player caller;
	private ConsoleCommandSender console = Bukkit.getConsoleSender();
	private boolean suppress, override;
	
	public PlayerWrapper(Player player, boolean opOverride, boolean suppressMessages)
	{
		this.caller = player;
		this.suppress = suppressMessages;
		this.override = opOverride;
	}
	
	private CommandSender getSender() {
		return suppress ? console : caller;
	}
	
	private Permissible getPermissible() {
		return override ? console : caller;
	}

	@Override
	public boolean isOp()
	{
		return getPermissible().isOp();
	}

	@Override // TODO: Could returning null cause issues?
	public PermissionAttachment addAttachment(Plugin plugin)
	{
		return null;
	}

	@Override // Special case for time-limited permissions; always go to the caller
	public PermissionAttachment addAttachment(Plugin plugin, int time)
	{
		return caller.addAttachment(plugin, time);
	}

	@Override // TODO: Could returning null cause issues?
	public PermissionAttachment addAttachment(Plugin plugin, String perm, boolean val)
	{
		return null;
	}

	@Override // Special case for time-limited permissions; always go to the caller
	public PermissionAttachment addAttachment(Plugin plugin, String perm, boolean val, int time)
	{
		return caller.addAttachment(plugin, perm, val, time);
	}

	@Override
	public Set<PermissionAttachmentInfo> getEffectivePermissions()
	{
		return getPermissible().getEffectivePermissions();
	}

	@Override
	public boolean hasPermission(String perm)
	{
		return getPermissible().hasPermission(perm);
	}

	@Override
	public boolean hasPermission(Permission perm)
	{
		return getPermissible().hasPermission(perm);
	}

	@Override
	public boolean isPermissionSet(String perm)
	{
		return getPermissible().isPermissionSet(perm);
	}

	@Override
	public boolean isPermissionSet(Permission perm)
	{
		return getPermissible().isPermissionSet(perm);
	}

	@Override
	public void recalculatePermissions()
	{
		getPermissible().recalculatePermissions();
	}

	@Override
	public void removeAttachment(PermissionAttachment attached) {}

	@Override
	public void setOp(boolean is)
	{
		getPermissible().setOp(is);
	}

	// CommandSender methods; getName() may not be declared in CommandSender,
	// but it's used for any CommandSender that actually defines it
	@Override
	public String getName()
	{
		return caller.getName();
	}

	@Override
	public Server getServer()
	{
		return caller.getServer();
	}

	@Override
	public void sendMessage(String msg)
	{
		getSender().sendMessage(msg);
	}

	// Player, HumanEntity, LivingEntity, Entity methods... ugh, there are so many of these...
	@Override
	public PlayerInventory getInventory() {
		return caller.getInventory();
	}

	@Override
	public ItemStack getItemInHand() {
		return caller.getItemInHand();
	}

	@Override
	public void setItemInHand(ItemStack item) {
		caller.setItemInHand(item);
	}

	@Override
	public boolean isSleeping() {
		return caller.isSleeping();
	}

	@Override
	public int getSleepTicks() {
		return caller.getSleepTicks();
	}

	@Override
	public int getHealth() {
		return caller.getHealth();
	}

	@Override
	public void setHealth(int health) {
		caller.setHealth(health);
	}

	@Override
	public double getEyeHeight() {
		return caller.getEyeHeight();
	}

	@Override
	public double getEyeHeight(boolean ignoreSneaking) {
		return caller.getEyeHeight(ignoreSneaking);
	}

	@Override
	public Location getEyeLocation() {
		return caller.getEyeLocation();
	}

	@Override
	public List<Block> getLineOfSight(HashSet<Byte> transparent, int maxDistance) {
		return caller.getLineOfSight(transparent, maxDistance);
	}

	@Override
	public Block getTargetBlock(HashSet<Byte> transparent, int maxDistance) {
		return caller.getTargetBlock(transparent, maxDistance);
	}

	@Override
	public List<Block> getLastTwoTargetBlocks(HashSet<Byte> transparent, int maxDistance) {
		return caller.getLastTwoTargetBlocks(transparent, maxDistance);
	}

	@Override
	public Egg throwEgg() {
		return caller.throwEgg();
	}

	@Override
	public Snowball throwSnowball() {
		return caller.throwSnowball();
	}

	@Override
	public Arrow shootArrow() {
		return caller.shootArrow();
	}

	@Override
	public boolean isInsideVehicle() {
		return caller.isInsideVehicle();
	}

	@Override
	public boolean leaveVehicle() {
		return caller.leaveVehicle();
	}

	@Override
	public Vehicle getVehicle() {
		return caller.getVehicle();
	}

	@Override
	public int getRemainingAir() {
		return caller.getRemainingAir();
	}

	@Override
	public void setRemainingAir(int ticks) {
		caller.setRemainingAir(ticks);
	}

	@Override
	public int getMaximumAir() {
		return caller.getMaximumAir();
	}

	@Override
	public void setMaximumAir(int ticks) {
		caller.setMaximumAir(ticks);
	}

	@Override
	public void damage(int amount) {
		caller.damage(amount);
	}

	@Override
	public void damage(int amount, Entity source) {
		caller.damage(amount, source);
	}

	@Override
	public int getMaximumNoDamageTicks() {
		return caller.getMaximumNoDamageTicks();
	}

	@Override
	public void setMaximumNoDamageTicks(int ticks) {
		caller.setMaximumNoDamageTicks(ticks);
	}

	@Override
	public int getLastDamage() {
		return caller.getLastDamage();
	}

	@Override
	public void setLastDamage(int damage) {
		caller.setLastDamage(damage);
	}

	@Override
	public int getNoDamageTicks() {
		return caller.getNoDamageTicks();
	}

	@Override
	public void setNoDamageTicks(int ticks) {
		caller.setNoDamageTicks(ticks);
	}

	@Override
	public Location getLocation() {
		return caller.getLocation();
	}

	@Override
	public void setVelocity(Vector velocity) {
		caller.setVelocity(velocity);
	}

	@Override
	public Vector getVelocity() {
		return caller.getVelocity();
	}

	@Override
	public World getWorld() {
		return caller.getWorld();
	}

	@Override
	public boolean teleport(Location location) {
		return caller.teleport(location);
	}

	@Override
	public boolean teleport(Entity destination) {
		return caller.teleport(destination);
	}

	@Override
	public List<Entity> getNearbyEntities(double x, double y, double z) {
		return caller.getNearbyEntities(x, y, z);
	}

	@Override
	public int getEntityId() {
		return caller.getEntityId();
	}

	@Override
	public int getFireTicks() {
		return caller.getFireTicks();
	}

	@Override
	public int getMaxFireTicks() {
		return caller.getMaxFireTicks();
	}

	@Override
	public void setFireTicks(int ticks) {
		caller.setFireTicks(ticks);
	}

	@Override
	public void remove() {
		caller.remove();
	}

	@Override
	public boolean isDead() {
		return caller.isDead();
	}

	@Override
	public Entity getPassenger() {
		return caller.getPassenger();
	}

	@Override
	public boolean setPassenger(Entity passenger) {
		return caller.setPassenger(passenger);
	}

	@Override
	public boolean isEmpty() {
		return caller.isEmpty();
	}

	@Override
	public boolean eject() {
		return caller.eject();
	}

	@Override
	public float getFallDistance() {
		return caller.getFallDistance();
	}

	@Override
	public void setFallDistance(float distance) {
		caller.setFallDistance(distance);
	}

	@Override
	public void setLastDamageCause(EntityDamageEvent event) {
		caller.setLastDamageCause(event);
	}

	@Override
	public EntityDamageEvent getLastDamageCause() {
		return caller.getLastDamageCause();
	}

	@Override
	public UUID getUniqueId() {
		return caller.getUniqueId();
	}

	@Override
	public boolean isOnline() {
		return caller.isOnline();
	}

	@Override
	public String getDisplayName() {
		return caller.getDisplayName();
	}

	@Override
	public void setDisplayName(String name) {
		caller.setDisplayName(name);
	}

	@Override
	public void setCompassTarget(Location loc) {
		caller.setCompassTarget(loc);
	}

	@Override
	public Location getCompassTarget() {
		return caller.getCompassTarget();
	}

	@Override
	public InetSocketAddress getAddress() {
		return caller.getAddress();
	}

	@Override // TODO: What on earth does this even do? Should it be sent to the console if suppress is true?
	public void sendRawMessage(String message) {
		caller.sendRawMessage(message);
	}

	@Override
	public void kickPlayer(String message) {
		caller.kickPlayer(message);
	}

	@Override
	public void chat(String msg) {
		caller.chat(msg);
	}

	@Override
	public boolean performCommand(String command) {
		return caller.performCommand(command);
	}

	@Override
	public boolean isSneaking() {
		return caller.isSneaking();
	}

	@Override
	public void setSneaking(boolean sneak) {
		caller.setSneaking(sneak);
	}

	@Override
	public void saveData() {
		caller.saveData();
	}

	@Override
	public void loadData() {
		caller.loadData();
	}

	@Override
	public void setSleepingIgnored(boolean isSleeping) {
		caller.setSleepingIgnored(isSleeping);
	}

	@Override
	public boolean isSleepingIgnored() {
		return caller.isSleepingIgnored();
	}

	@Override
	public void playNote(Location loc, byte instrument, byte note) {
		caller.playNote(loc, instrument, note);
	}

	@Override
	public void playNote(Location loc, Instrument instrument, Note note) {
		caller.playNote(loc, instrument, note);
	}

	@Override
	public void playEffect(Location loc, Effect effect, int data) {
		caller.playEffect(loc, effect, data);
	}

	@Override
	public void sendBlockChange(Location loc, Material material, byte data) {
		caller.sendBlockChange(loc, material, data);
	}

	@Override
	public boolean sendChunkChange(Location loc, int sx, int sy, int sz, byte[] data) {
		return caller.sendChunkChange(loc, sx, sy, sz, data);
	}

	@Override
	public void sendBlockChange(Location loc, int material, byte data) {
		caller.sendBlockChange(loc, material, data);
	}

	@Override
	public void sendMap(MapView map) {
		caller.sendMap(map);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void updateInventory() {
		caller.updateInventory();
	}

	@Override
	public void awardAchievement(Achievement achievement) {
		caller.awardAchievement(achievement);
	}

	@Override
	public void incrementStatistic(Statistic statistic) {
		caller.incrementStatistic(statistic);
	}

	@Override
	public void incrementStatistic(Statistic statistic, int amount) {
		caller.incrementStatistic(statistic, amount);
	}

	@Override
	public void incrementStatistic(Statistic statistic, Material material) {
		caller.incrementStatistic(statistic, material);
	}

	@Override
	public void incrementStatistic(Statistic statistic, Material material, int amount) {
		caller.incrementStatistic(statistic, material, amount);
	}

	@Override
	public void setPlayerTime(long time, boolean relative) {
		caller.setPlayerTime(time, relative);
	}

	@Override
	public long getPlayerTime() {
		return caller.getPlayerTime();
	}

	@Override
	public long getPlayerTimeOffset() {
		return caller.getPlayerTimeOffset();
	}

	@Override
	public boolean isPlayerTimeRelative() {
		return caller.isPlayerTimeRelative();
	}

	@Override
	public void resetPlayerTime() {
		caller.resetPlayerTime();
	}

	@Override
	public GameMode getGameMode() {
		return caller.getGameMode();
	}

	@Override
	public void setGameMode(GameMode mode) {
		caller.setGameMode(mode);
	}

	@Override
	public boolean isBanned() {
		return caller.isBanned();
	}

	@Override
	public boolean isWhitelisted() {
		return caller.isWhitelisted();
	}

	@Override
	public void setBanned(boolean ban) {
		caller.setBanned(ban);
	}

	@Override
	public void setWhitelisted(boolean wl) {
		caller.setWhitelisted(wl);
	}

	@Override
	public float getExhaustion() {
		return caller.getExhaustion();
	}

	@SuppressWarnings("deprecation")
	@Override
	public int getExperience() {
		return caller.getExperience();
	}

	@Override
	public int getFoodLevel() {
		return caller.getFoodLevel();
	}

	@Override
	public int getLevel() {
		return caller.getLevel();
	}

	@Override
	public float getSaturation() {
		return caller.getSaturation();
	}

	@Override
	public int getTotalExperience() {
		return caller.getTotalExperience();
	}

	@Override
	public void setExhaustion(float exhaustion) {
		caller.setExhaustion(exhaustion);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void setExperience(int xp) {
		caller.setExperience(xp);
	}

	@Override
	public void setFoodLevel(int food) {
		caller.setFoodLevel(food);
	}

	@Override
	public void setLevel(int lvl) {
		caller.setLevel(lvl);
	}

	@Override
	public void setSaturation(float saturation) {
		caller.setSaturation(saturation);
	}

	@Override
	public void setTotalExperience(int xp) {
		caller.setTotalExperience(xp);
	}

	@Override
	public Location getBedSpawnLocation() {
		return caller.getBedSpawnLocation();
	}

	@Override
	public boolean isSprinting() {
		return caller.isSprinting();
	}

	@Override
	public void setSprinting(boolean run) {
		caller.setSprinting(run);
	}

	@Override
	public int getTicksLived() {
		return caller.getTicksLived();
	}

	@Override
	public void setTicksLived(int value) {
		caller.setTicksLived(value);
	}

	@Override
	public Player getPlayer() {
		return caller.getPlayer();
	}

	@Override
	public Map<String,Object> serialize() {
		return caller.serialize();
	}

	@Override
	public String getPlayerListName() {
		return caller.getPlayerListName();
	}

	@Override
	public void setPlayerListName(String name) {
		caller.setPlayerListName(name);
	}

	@Override
	public int getMaxHealth() {
		return caller.getMaxHealth();
	}

	@Override
	public boolean teleport(Location location, TeleportCause cause) {
		return caller.teleport(location, cause);
	}

	@Override
	public boolean teleport(Entity destination, TeleportCause cause) {
		return caller.teleport(destination, cause);
	}

	@Override
	public void giveExp(int amount) {
		caller.giveExp(amount);
	}

	@Override
	public float getExp() {
		return caller.getExp();
	}

	@Override
	public void setExp(float exp) {
		caller.setExp(exp);
	}

	@Override
	public Player getKiller() {
		return caller.getKiller();
	}

	@Override
	public long getFirstPlayed() {
		return caller.getFirstPlayed();
	}

	@Override
	public long getLastPlayed() {
		return caller.getLastPlayed();
	}

	@Override
	public boolean hasPlayedBefore() {
		return caller.hasPlayedBefore();
	}
}