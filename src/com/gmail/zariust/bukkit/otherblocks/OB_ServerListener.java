package com.gmail.zariust.bukkit.otherblocks;

// Example plugin
import com.gmail.zariust.register.payment.Methods;

// Imports for Register

// Bukkit Imports
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.event.server.ServerListener;

public class OB_ServerListener extends ServerListener {
	private OtherBlocks plugin;
	private Methods Methods = null;

	public OB_ServerListener(OtherBlocks plugin) {
		this.plugin = plugin;
		this.Methods = new Methods();
	}

	@Override
	public void onPluginDisable(PluginDisableEvent event) {
		// Check to see if the plugin thats being disabled is the one we are using
		if (this.Methods != null && this.Methods.hasMethod()) {
			Boolean check = this.Methods.checkDisabled(event.getPlugin());

			if(check) {
				OtherBlocks.method = null;
				System.out.println("[" + plugin.info.getName() + "] Payment method was disabled. No longer accepting payments.");
			}
		}

	}

	@Override
	public void onPluginEnable(PluginEnableEvent event) {
		// Check to see if we need a payment method
		if (!this.Methods.hasMethod()) {
			if(this.Methods.setMethod(event.getPlugin())) {
				// You might want to make this a public variable inside your MAIN class public Method Method = null;
				// then reference it through this.plugin.Method so that way you can use it in the rest of your plugin ;)
				OtherBlocks.method = this.Methods.getMethod();
				System.out.println("[Otherblocks] Payment method found (" + this.plugin.method.getName() + " version: " + this.plugin.method.getVersion() + ")");
//				  System.out.println("[" + plugin.info.getName() + "] Payment method found (" + this.plugin.Method.getName() + " version: " + this.plugin.Method.getVersion() + ")");
			}
		}

	}
}