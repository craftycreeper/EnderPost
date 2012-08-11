package net.minebot.enderpost;

import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredServiceProvider;

import net.milkbowl.vault.economy.Economy;

public class Cashier {

	public static Economy economy;
	private static EnderPostPlugin plugin;
	private static int smallPrice, largePrice;
	
	public boolean isEnabled() {
		return economy == null;
	}

	public static void init(EnderPostPlugin instance) {
		RegisteredServiceProvider<Economy> economyProvider = 
				instance.getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        if (economyProvider != null) {
            economy = economyProvider.getProvider();
        }
        if (economy == null) {
        	instance.getLogger().warning("Failed to enable economy support!");
        	return;
        }
        instance.getLogger().info("Using " + economy.getName() + " for economy support.");
        plugin = instance;
        largePrice = plugin.getConfig().getInt("economy.price.large");
        smallPrice = plugin.getConfig().getInt("economy.price.small");
	}

	public static boolean charge(String player, ItemStack item) {
		if (economy == null) return true;
		int cost = calculateValue(item);
		if (economy.has(player, cost)) {
			economy.withdrawPlayer(player, cost);
			return true;
		}
		else return false;
	}
	
	public static boolean refund(String player, ItemStack item) {
		if (economy == null) return false;
		economy.depositPlayer(player, calculateValue(item));
		return true;
	}
	
	public static int calculateValue(ItemStack item) {
		if (economy == null) return 0;
		if (item.getAmount() <= 32) return smallPrice;
		else return largePrice;
	}
	
}
