package com.gmail.zariust.otherdrops.listener;

// Example plugin
import com.gmail.zariust.otherdrops.OtherDrops;
import com.gmail.zariust.register.payment.Methods;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;

import de.diddiz.LogBlock.LogBlock;

// Imports for Register

// Bukkit Imports
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.event.server.ServerListener;
import org.bukkit.plugin.Plugin;

import me.taylorkelly.bigbrother.BigBrother;

public class OdServerListener extends ServerListener {
	private OtherDrops parent;
	private Methods methods = null;

	public OdServerListener(OtherDrops plugin) {
		this.parent = plugin;
		this.methods = new Methods();
	}

	@Override
	public void onPluginDisable(PluginDisableEvent event) {
		// Check to see if the plugin thats being disabled is the one we are using
		if (this.methods != null && this.methods.hasMethod()) {
			Boolean check = this.methods.checkDisabled(event.getPlugin());
			if(check) {
				OtherDrops.method = null;
				System.out.println("[" + parent.info.getName() + "] Payment method was disabled. No longer accepting payments.");
			}
		}

	}

	@Override
	public void onPluginEnable(PluginEnableEvent event) {
		// Check to see if we need a payment method
		if (!this.methods.hasMethod()) {
			if(this.methods.setMethod(event.getPlugin())) {
				OtherDrops.method = this.methods.getMethod();
				System.out.println("[Otherblocks] Payment method found (" + OtherDrops.method.getName() +
					" version: " + OtherDrops.method.getVersion() + ")");
				return;
			}
		}
		// Maybe it's some other dependency that has loaded; we have them as softdepends in plugin.yml,
		// but that doesn't guarantee they load before us
		Plugin plugin = event.getPlugin();
		String name = plugin.getDescription().getName();
		if(name.equalsIgnoreCase("WorldGuard"))
			OtherDrops.worldguardPlugin = (WorldGuardPlugin)plugin;
		else if(name.equalsIgnoreCase("LogBlock"))
			OtherDrops.lbconsumer = ((LogBlock)plugin).getConsumer();
		else if(name.equalsIgnoreCase("BigBrother"))
			OtherDrops.bigBrother = (BigBrother)plugin;
	}
}