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

import java.util.List;
import java.util.Arrays;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

public class EnderPostman {

	private Enderman enderman;
	private Player player;
	private Parcel parcel;
	private EnderPostPlugin plugin;
	private int cleanupTask = Integer.MIN_VALUE;
	
	private final Integer[] cBlocksArray = {1, 2, 3, 4, 5, 6, 7, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 
		29, 30, 31, 32, 33, 35, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 52, 53, 54, 56, 57, 58,
		60, 61, 67, 70, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 91, 92, 95,
		96, 97, 98, 99, 100, 103, 107, 108, 109, 110, 112, 113, 114, 115, 116, 121, 122, 123};
	
	private List<Integer> carryableBlocks = Arrays.asList(cBlocksArray);
	
	public EnderPostman(EnderPostPlugin plugin, Location location, Player player, Parcel parcel) {
		this.plugin = plugin;
		this.player = player;
		this.parcel = parcel;
		
		enderman = (Enderman)location.getWorld().spawnEntity(location, EntityType.ENDERMAN);
		
		if (enderman == null) return;
		
		//Hold the parcel
		holdParcel();
		
		//Notify player
		if (parcel.isReturned())
			EnderPostUtil.tellPlayer(player, "A package you sent to " + parcel.getRealRecipient() + " is being returned.");
		else
			EnderPostUtil.tellPlayer(player, "A package has arrived for you from " + parcel.getSender() + "!");
		
		EnderPostUtil.tellPlayer(player, "\"Says here the package contains " + parcel.getContents().getAmount() + " " +
			EnderPostUtil.getItemName(parcel.getContents()) + ". Right click me to sign for " +
			"the package so I can hand it over.\"");
		
		//Start cleanup task
		setCleanup(1200, "\"Well, I'm bored of waiting for you. We'll try to deliver again later.\"");
	}
	
	public EnderPostman(EnderPostPlugin plugin, Location location, Parcel parcel) {
		this.plugin = plugin;
		this.parcel = parcel;
		
		enderman = (Enderman)location.getWorld().spawnEntity(location, EntityType.ENDERMAN);
		
		holdParcel();
		
		setCleanup(150, null);
	}
	
	private void despawn() {
		enderman.remove();
		DeliveryManager.removePostman(this);
		parcel = null;
	}
	
	private void holdParcel() {
		//Hold the parcel
		if (carryableBlocks.contains(parcel.getContents().getTypeId()))
			enderman.setCarriedMaterial(parcel.getContents().getData());
		else
			enderman.setCarriedMaterial(new MaterialData(Material.CHEST));
	}
	
	@SuppressWarnings("deprecation")
	public void deliverParcel() {
		if (parcel == null) return;
		ItemStack item = player.getItemInHand();
		if (item != null && item.getAmount() > 0) {
			//Hands full, try to insert into inventory
			if (player.getInventory().firstEmpty() != -1) {
				EnderPostUtil.tellPlayer(player, "\"Thanks. I put the package in your inventory.\"");
				player.getInventory().addItem(parcel.getContents());
				player.updateInventory();
			}
			else {
				EnderPostUtil.tellPlayer(player, "\"Thanks. Your inventory's full, so I'll just drop it here for you.\"");
				dropParcel();
			}
		}
		else {
			player.setItemInHand(parcel.getContents());
			EnderPostUtil.tellPlayer(player, "\"Thanks. Here you go!\"");
		}
		ParcelManager.deliverySuccess(parcel);
		finished();
	}
	
	public void gtfo() {
		dropParcel();
		setCleanup(1, null);
	}
	
	public void dropParcel() {
		if (parcel == null) return;
		player.getWorld().dropItemNaturally(enderman.getLocation(), parcel.getContents());
		ParcelManager.deliverySuccess(parcel);
		finished();
	}
	
	private void finished() {
		enderman.setCarriedMaterial(new MaterialData(Material.AIR));
		setCleanup(100, null);
		parcel = null;
	}

	public Enderman getEnderman() {
		return enderman;
	}

	public Player getPlayer() {
		return player;
	}

	public Parcel getParcel() {
		return parcel;
	}
	
	private void setCleanup(long ticks, final String message) {
		plugin.getServer().getScheduler().cancelTask(cleanupTask);
		cleanupTask = plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
			public void run() {
				if (message != null) {
					ParcelManager.deliveryFailed(parcel);
					EnderPostUtil.tellPlayer(player, message);
				}
				despawn();
		    }
		}, ticks);
	}
	
}
