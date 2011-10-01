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

package com.gmail.zariust.otherdrops.listener;

// Example plugin
import com.gmail.zariust.common.Verbosity;
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

@SuppressWarnings("static-access")
public class OdServerListener extends ServerListener {
	@SuppressWarnings("unused")
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
				System.out.println("[OtherDrops] Payment method was disabled. No longer accepting payments.");
			}
		}

	}

	@Override
	public void onPluginEnable(PluginEnableEvent event) {
		// Check to see if we need a payment method
		if (!this.methods.hasMethod()) {
			if(this.methods.setMethod(OtherDrops.plugin.getServer().getPluginManager())) {
				OtherDrops.method = this.methods.getMethod();
				OtherDrops.logInfo("Payment method found (" + OtherDrops.method.getName() +
					" version: " + OtherDrops.method.getVersion() + ")", Verbosity.NORMAL);
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