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

import net.milkbowl.vault.item.ItemInfo;
import net.milkbowl.vault.item.Items;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class EnderPostUtil {

	public static long curTime() {
		return System.currentTimeMillis()/1000;
	}
	
	public static void tellPlayer(Player player, String message) {
		if (player != null && player.isOnline()) {
			player.sendMessage("[" + ChatColor.YELLOW + "EnderPost" + ChatColor.WHITE + "] " + message);
		}
	}
	
	public static String getItemName(ItemStack item) {
		ItemInfo itemi = Items.itemByStack(item);
		if (itemi == null) itemi = Items.itemById(item.getTypeId());
		if (itemi != null) return itemi.getName();
		else return item.getData().getItemType().toString();
	}
	
}
