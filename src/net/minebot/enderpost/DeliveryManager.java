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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

public class DeliveryManager {

	private static EnderPostPlugin plugin;
	
	private static int deliveryTask = Integer.MIN_VALUE;
	
	private static List<EnderPostman> postmen;
	
	private static int spawnMin, spawnMax;
	
	public static final BlockFace[] hFaces = {BlockFace.NORTH, BlockFace.NORTH_EAST,
		BlockFace.EAST, BlockFace.SOUTH_EAST, BlockFace.SOUTH, BlockFace.SOUTH_WEST,
		BlockFace.WEST, BlockFace.NORTH_WEST};
	
	public static void init(EnderPostPlugin plugin) {
		DeliveryManager.plugin = plugin;
		
		postmen = new ArrayList<EnderPostman>();
		
		spawnMin = plugin.getConfig().getInt("postman.spawn-min");
		spawnMax = plugin.getConfig().getInt("postman.spawn-max");
		if (spawnMax < spawnMin) spawnMax = spawnMin;
		
		if (plugin.getServer().getOnlinePlayers().length > 0) 
			startDeliveries();
	}
	
	public static void startDeliveries() {
		if (plugin.getServer().getOnlinePlayers().length > 0 && deliveryTask == Integer.MIN_VALUE) {
			int interval = plugin.getConfig().getInt("delivery.task-interval") * 20;
			deliveryTask = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
				public void run() {
					doDeliveries();
			    }
			}, interval, interval);
		}
	}
	
	public static void stopDeliveries() {
		if (deliveryTask != Integer.MIN_VALUE) {
			plugin.getServer().getScheduler().cancelTask(deliveryTask);
			deliveryTask = Integer.MIN_VALUE;
		}
	}
	
	private static void doDeliveries() {
		List<Parcel> dparcels = ParcelManager.getDeliverableParcels();
		for (Parcel parcel : dparcels) {
			Player recipient = plugin.getServer().getPlayerExact(parcel.getRecipient());
			if (recipient == null) continue;
			
			//Make sure they're not in the arena...
			if (EnderPostPlugin.maHandler != null && EnderPostPlugin.maHandler.isPlayerInArena(recipient))
				continue;
			
			Location spawnLoc = findSpawnLocation(recipient);
			if (spawnLoc != null) {
				ParcelManager.deliveryAttempt(parcel);
				//DISPATCH THE POSTMAN!!
				EnderPostman postman = new EnderPostman(plugin, spawnLoc, recipient, parcel);
				postmen.add(postman);
			}
		}
	}
	
	public static void pickup(Player player, Parcel parcel) {
		Location spawnLoc = findSpawnLocation(player);
		if (spawnLoc != null) {
			EnderPostman pickupMan = new EnderPostman(plugin, spawnLoc, parcel);
			postmen.add(pickupMan);
		}
	}
	
	public static EnderPostman getPostmanByEnderman(UUID uuid) {
		for (EnderPostman ep : postmen) {
			if (ep.getEnderman().getUniqueId().equals(uuid))
				return ep;
		}
		return null;
	}
	
	public static EnderPostman getPostmanByParcel(Parcel parcel) {
		for (EnderPostman ep : postmen) {
			if (ep.getParcel() == parcel)
				return ep;
		}
		return null;
	}
	
	public static void removePostman(EnderPostman postman) {
		postmen.remove(postman);
	}
	
	private static Location findSpawnLocation(Player player) {
		//Bad weather?
		if (player.getWorld().hasStorm()) return null;
		
		Block foundBlock = null;
    	
    	List<Block> losBlocks = player.getLineOfSight(null, spawnMax);
    	//System.out.println("FINDING POTENTIAL SPAWNPOINTS...");
    	for (Block block : losBlocks) {
    		//System.out.println("--- Line of sight block (and related blocks) ---");
    		Block potential = findSpawnBlock(player, block, spawnMin, spawnMax);
    		if (potential != null) {
    			foundBlock = potential;
    			break;
    		}
    		for(BlockFace face : hFaces) {
    			potential = findSpawnBlock(player, block.getRelative(face), spawnMin, spawnMax);
        		if (potential != null) {
        			foundBlock = potential;
        			break;
        		}
    		}
    	}
    	if (foundBlock == null) {
    		//Try some surrounding blocks
    		//System.out.println("--- Blocks surrounding player at spawnMin ---");
    		for(BlockFace face : hFaces) {
    			Block potential = findSpawnBlock(player, player.getEyeLocation().getBlock().getRelative(face, spawnMin),
    				spawnMin, spawnMax);
        		if (potential != null) {
        			foundBlock = potential;
        			break;
        		}
    		}
    	}
    	
    	if (foundBlock == null) return null;
    	
    	Location tLoc = foundBlock.getLocation();
    	return new Location(tLoc.getWorld(), tLoc.getBlockX()+0.5, tLoc.getBlockY(), tLoc.getBlockZ()+0.5);
    }
	
	private static Block findSpawnBlock(Player player, Block block, int spawnMin, int spawnMax) {
		//System.out.println("Testing " + block.toString());
		
		//System.out.println("Distance: " + block.getLocation().distance(player.getLocation()));
		
		if (block.getLocation().distance(player.getLocation()) < spawnMin) {
			//System.out.println("Too close.");
			return null;
		}
		if (!block.isEmpty()) {
			//System.out.println("Not empty.");
			return null;
		}
		while(block.getRelative(BlockFace.DOWN, 1).isEmpty()) {
			//System.out.println("Going down...");
			//Keep looking downards until we find a non-air block
			block = block.getRelative(BlockFace.DOWN, 1);
			if (Math.abs(block.getLocation().getBlockY() - player.getLocation().getBlockY()) > spawnMax) {
				//System.out.println("Too far down or up!");
				return null;
			}
		}
		
		if (block.getRelative(BlockFace.DOWN, 1).isLiquid() ||
				!block.getRelative(BlockFace.UP, 1).isEmpty() ||
				!block.getRelative(BlockFace.UP, 2).isEmpty()) {
			//System.out.println("Unsafe spawn place.");
			return null;
		}
		
		return block;
	}
	
}
