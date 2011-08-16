package com.gmail.zariust.bukkit.otherblocks;

import java.util.Set;

import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;

public class PlayerWrapper implements CommandSender
{
	Player caller;
	
	public PlayerWrapper(Player player)
	{
		this.caller = player;
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
	public void sendMessage(String arg0)
	{
		if (OtherBlocksConfig.runCommandsSuppressMessage) {
			OtherBlocks.logInfo("RunCommand response: "+arg0);
		} else {
			caller.sendMessage(arg0);
		}
	}

	@Override
	public PermissionAttachment addAttachment(Plugin arg0)
	{
		return caller.addAttachment(arg0);
	}

	@Override
	public PermissionAttachment addAttachment(Plugin arg0, int arg1)
	{
		return caller.addAttachment(arg0, arg1);
	}

	@Override
	public PermissionAttachment addAttachment(Plugin arg0, String arg1,
			boolean arg2)
	{
		return caller.addAttachment(arg0, arg1, arg2);
	}

	@Override
	public PermissionAttachment addAttachment(Plugin arg0, String arg1,
			boolean arg2, int arg3)
	{
		return caller.addAttachment(arg0, arg1, arg2, arg3);
	}

	@Override
	public Set<PermissionAttachmentInfo> getEffectivePermissions()
	{
		return caller.getEffectivePermissions();
	}

	@Override
	public boolean hasPermission(String arg0)
	{
		return true;
	}

	@Override
	public boolean hasPermission(Permission arg0)
	{
		return true;
	}

	@Override
	public boolean isPermissionSet(String arg0)
	{
		return caller.isPermissionSet(arg0);
	}

	@Override
	public boolean isPermissionSet(Permission arg0)
	{
		return caller.isPermissionSet(arg0);
	}

	@Override
	public void recalculatePermissions()
	{
		caller.recalculatePermissions();
	}

	@Override
	public void removeAttachment(PermissionAttachment arg0)
	{
		caller.removeAttachment(arg0);
	}

	@Override
	public void setOp(boolean arg0)
	{
		caller.setOp(arg0);
	}
}