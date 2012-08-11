package net.minebot.enderpost;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class EnderPostCommand implements CommandExecutor {
	
	private EnderPostPlugin plugin;
	
	public EnderPostCommand(EnderPostPlugin plugin) {
		this.plugin = plugin;
	}

	public boolean onCommand(CommandSender sender, Command command,
			String label, String[] args) {
		
		if (!(sender instanceof Player))
			return true;
		
		Player player = (Player)sender;
		
		if (args.length == 0) commandHelp(player, command.getName());
		else if (args[0].equals("cost")) commandCost(player);
		else if (args[0].equals("send")) commandSend(player, args);
		
		return true;
	}
	
	private void commandHelp(Player player, String command) {
		EnderPostUtil.tellPlayer(player, ChatColor.DARK_GREEN + "Command Help");
		EnderPostUtil.tellPlayer(player, "Hold an item (or stack) and use these commands:");
		if (Cashier.economy != null)
			EnderPostUtil.tellPlayer(player, ChatColor.GOLD + "/" + command + " cost" + ChatColor.WHITE + " - calculate postage for the item");
		EnderPostUtil.tellPlayer(player, ChatColor.GOLD + "/" + command + " send <player>" + ChatColor.WHITE + " - send the item");
	}
	
	private void commandCost(Player player) {
		ItemStack handItem = player.getItemInHand();
		
		if (handItem == null || handItem.getAmount() == 0) {
			EnderPostUtil.tellPlayer(player, "You need to hold an item to calculate the cost of sending it.");
			return;
		}
		
		if (Cashier.calculateValue(handItem) == 0) {
			EnderPostUtil.tellPlayer(player, "Postage is free.");
			return;
		}
		
		EnderPostUtil.tellPlayer(player, "It would cost " + Cashier.economy.format(Cashier.calculateValue(handItem)) +
			" to send " + handItem.getAmount() + " " + EnderPostUtil.getItemName(handItem) + ".");
	}
	
	private void commandSend(Player player, String[] args) {
		if (args.length < 2) {
			EnderPostUtil.tellPlayer(player, "You need to specify a player to send to.");
			return;
		}
		ItemStack item = player.getItemInHand();
		if (item == null || item.getAmount() == 0) {
			EnderPostUtil.tellPlayer(player, "You need to hold an item (or stack) to send it.");
			return;
		}
		
		//Find the player
		OfflinePlayer p = null;
        for(OfflinePlayer o : plugin.getServer().getOfflinePlayers()) {
        	if(o.getName().equalsIgnoreCase(args[1])) {
        		p = o;
        		break;
        	}
        }
        if (p == null) {
        	EnderPostUtil.tellPlayer(player, "Could not find player '" + args[1] + "'.");
			@SuppressWarnings("unchecked")
			List<Player> mplayers = plugin.getServer().matchPlayer(args[1]);
			if (mplayers != null && mplayers.size() > 0) {
				if (mplayers.size() > 5) mplayers = mplayers.subList(0, 5);
				String mps = "";
				for (Player pl : mplayers)
					mps = mps + " " + pl.getName();
				EnderPostUtil.tellPlayer(player, "Did you mean:" + mps);
			}
			return;
        }
        if (p.isBanned() || (plugin.getServer().hasWhitelist() && !p.isWhitelisted())) {
        	EnderPostUtil.tellPlayer(player, p.getName() + " cannot receive parcels right now.");
        	return;
        }
        
		if (Cashier.charge(player.getName(), item)) {
			ItemStack thing = item.clone();
			Parcel parcel = ParcelManager.createParcel(thing, player.getName(), p.getName());
			player.setItemInHand(null);
			
			EnderPostUtil.tellPlayer(player, "Your parcel containing " + thing.getAmount() + " " + EnderPostUtil.getItemName(thing) +
				" has been sent to " + p.getName() + ".");
			
			int price = Cashier.calculateValue(thing);
			if (price > 0)
				EnderPostUtil.tellPlayer(player, "You have been charged " + Cashier.economy.format(price)
						+ " in postage.");
			
			
			DeliveryManager.pickup(player, parcel);
		}
	}

}
