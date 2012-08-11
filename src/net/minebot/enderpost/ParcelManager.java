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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

public class ParcelManager {

	private static ArrayList<Parcel> parcels;
	private static ArrayList<Parcel> outForDelivery;
	private static String parcelFilename;
	
	private static EnderPostPlugin plugin;
	
	public static void init(EnderPostPlugin plugin, String parcelFilename) {
		ParcelManager.plugin = plugin;
		ParcelManager.parcelFilename = parcelFilename;
		
		parcels = new ArrayList<Parcel>();
		outForDelivery = new ArrayList<Parcel>();
		
		YamlConfiguration parcelFile = new YamlConfiguration();
		try {
			parcelFile.load(parcelFilename);
		} catch (FileNotFoundException e) {
			plugin.getLogger().info("No parcel file found.");
			return;
		} catch (IOException e) {
			plugin.getLogger().warning("Could not load parcel file!");
			return;
		} catch (InvalidConfigurationException e) {
			plugin.getLogger().warning("Could not load parcel file!");
			return;
		}
		
		for(String id : parcelFile.getKeys(false)) {
			Parcel parcel = Parcel.loadFromConf(parcelFile, id);
			if (parcel != null)
				parcels.add(parcel);
		}
		
		plugin.getLogger().info(parcels.size() + " parcels loaded.");
	}
	
	public static Parcel createParcel(ItemStack contents, String sender, String recipient) {
		Parcel newparcel = new Parcel(contents, sender, recipient);
		parcels.add(newparcel);
		saveParcels();
		return newparcel;
	}
	
	public static void returnParcel(Parcel parcel) {
		parcel.setReturned(true);
		saveParcels();
	}
	
	public static void deliveryAttempt(Parcel parcel) {
		parcel.deliveryAttempted();
		outForDelivery.add(parcel);
	}
	
	public static void deliveryFailed(Parcel parcel) {
		outForDelivery.remove(parcel);
	}
	
	public static void deliverySuccess(Parcel parcel) {
		parcels.remove(parcel);
		outForDelivery.remove(parcel);
		saveParcels();
	}
	
	public static boolean isOutForDelivery(Parcel parcel) {
		return outForDelivery.contains(parcel);
	}
	
	public static List<Parcel> getDeliverableParcels() {
		ArrayList<Parcel> dparcels = new ArrayList<Parcel>();
		int retryInterval = plugin.getConfig().getInt("delivery.retry-interval");
		for (Parcel parcel : parcels) {
			if (parcel.canBeDelivered(retryInterval) && !outForDelivery.contains(parcel) &&
					plugin.getServer().getOfflinePlayer(parcel.getRecipient()).isOnline())
				dparcels.add(parcel);
		}
		
		return dparcels;
	}
	
	public static void saveParcels() {
		YamlConfiguration newParcelFile = new YamlConfiguration();
		newParcelFile.options().header("This file contains serialized parcels for EnderPost. DO NOT EDIT!");
		for(Parcel parcel : parcels) {
			parcel.saveToConf(newParcelFile);
		}
		
		try {
			newParcelFile.save(parcelFilename);
		} catch (IOException e) {
			plugin.getLogger().warning("Could not load parcel file!");
		}
	}
	
}
