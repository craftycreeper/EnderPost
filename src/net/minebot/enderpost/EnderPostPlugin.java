/*
 * EnderPost - Send stuff to your friends via Endermen
 * 
 * Copyright (c) 2012 craftycreeper, minebot.net
 * 
 * Basic idea shamelessly stolen from troed's Courier plugin
 * http://dev.bukkit.org/server-mods/courier/
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package net.minebot.enderpost;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import com.garbagemule.MobArena.MobArena;
import com.garbagemule.MobArena.MobArenaHandler;

public class EnderPostPlugin extends JavaPlugin implements Listener {

	private File dataDir;
	
	public static MobArenaHandler maHandler;
	
	@Override
	public void onEnable() {
		dataDir = new File("plugins/" + getDescription().getName());
		
		if (!dataDir.isDirectory()) {
			dataDir.mkdir();
		}
		
		loadConfig();
		
		//Initialize managers
		if (getConfig().getBoolean("economy.enable"))
			Cashier.init(this);
		
		ParcelManager.init(this, dataDir + "/parcels.yml");
		DeliveryManager.init(this);
		
		setupMobArenaHandler();
		
		//Command and event listener
		getCommand("enderpost").setExecutor(new EnderPostCommand(this));
		new EnderPostListener(this);
		
		getLogger().info("Enabled.");
	}
	
	@Override
	public void onDisable() {
		ParcelManager.saveParcels();
		
		getLogger().info("Disabled.");
	}
	
	private void loadConfig() {
		FileConfiguration conf = getConfig();
		try {
			conf = getConfig();
			conf.load(dataDir + "/config.yml");
			conf.save(dataDir + "/config.yml");
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		} catch (InvalidConfigurationException e) {
			getLogger().warning("Cannot load config file!");
		}
		
		conf.options().copyDefaults(true);
		conf.addDefault("delivery.task-interval", 60);
		conf.addDefault("delivery.retry-interval", 900);
		conf.addDefault("postman.spawn-min", 3);
		conf.addDefault("postman.spawn-max", 8);
		conf.addDefault("economy.enable", false);
		conf.addDefault("economy.price.small", 1);
		conf.addDefault("economy.price.large", 2);
		
		try {
			conf.save(dataDir + "/config.yml");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public void setupMobArenaHandler(){
		Plugin maPlugin = (MobArena) getServer().getPluginManager().getPlugin("MobArena");
	    
	    if (maPlugin == null)
	        return;

	    maHandler = new MobArenaHandler();
	}

}
