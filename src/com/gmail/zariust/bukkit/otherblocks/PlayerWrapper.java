package com.gmail.zariust.bukkit.otherblocks;

import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;

public class PlayerWrapper implements CommandSender
{
	Player caller;
	ConsoleCommandSender console = new ConsoleCommandSender(Bukkit.getServer());
	boolean suppress;
	
	public PlayerWrapper(Player player, boolean suppressMessages)
	{
		this.caller = player;
		this.suppress = suppressMessages;
	}

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
	public boolean isOp()
	{
		return true;
	}

	@Override
	public void sendMessage(String msg)
	{
		if(suppress) console.sendMessage(msg);
		else caller.sendMessage(msg);
	}

	@Override
	public PermissionAttachment addAttachment(Plugin plugin)
	{
		return caller.addAttachment(plugin);
	}

	@Override
	public PermissionAttachment addAttachment(Plugin plugin, int time)
	{
		return caller.addAttachment(plugin, time);
	}

	@Override
	public PermissionAttachment addAttachment(Plugin plugin, String perm, boolean val)
	{
		return caller.addAttachment(plugin, perm, val);
	}

	@Override
	public PermissionAttachment addAttachment(Plugin plugin, String perm, boolean val, int time)
	{
		return caller.addAttachment(plugin, perm, val, time);
	}

	@Override
	public Set<PermissionAttachmentInfo> getEffectivePermissions()
	{
		return caller.getEffectivePermissions();
	}

	@Override
	public boolean hasPermission(String perm)
	{
		return console.hasPermission(perm);
	}

	@Override
	public boolean hasPermission(Permission perm)
	{
		return console.hasPermission(perm);
	}

	@Override
	public boolean isPermissionSet(String perm)
	{
		return caller.isPermissionSet(perm);
	}

	@Override
	public boolean isPermissionSet(Permission perm)
	{
		return caller.isPermissionSet(perm);
	}

	@Override
	public void recalculatePermissions()
	{
		caller.recalculatePermissions();
	}

	@Override
	public void removeAttachment(PermissionAttachment attached)
	{
		caller.removeAttachment(attached);
	}

	@Override
	public void setOp(boolean is)
	{
		caller.setOp(is);
	}
}