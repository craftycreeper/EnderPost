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

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class Parcel {

	private String id;
	private ItemStack contents;
	private String sender, recipient;
	private long created, lastDeliveryAttempt;
	private boolean returned;
	
	public Parcel(String id, ItemStack contents, String sender, String recipient,
			long created, long lastDeliveryAttempt, boolean returned) {
		this.id = id;
		this.contents = contents;
		this.sender = sender;
		this.recipient = recipient;
		this.created = created;
		this.lastDeliveryAttempt = lastDeliveryAttempt;
		this.returned = returned;
	}

	/**
	 * Make a new Parcel
	 */
	public Parcel(ItemStack contents, String sender, String recipient) {
		this(UUID.randomUUID().toString(), contents, sender, recipient, EnderPostUtil.curTime(), 0, false);
	}
	
	public boolean canBeDelivered(int deliveryInterval) {
		if (lastDeliveryAttempt == 0 || 
				(lastDeliveryAttempt + deliveryInterval) <= EnderPostUtil.curTime())
			return true;
		
		
		return false;
	}
	
	public void deliveryAttempted() {
		lastDeliveryAttempt = EnderPostUtil.curTime();
	}
	
	public String getId() {
		return id;
	}
	
	public ItemStack getContents() {
		return contents;
	}

	public String getSender() {
		return sender;
	}

	public String getRecipient() {
		if (!returned)
			return recipient;
		return sender;
	}
	
	public String getRealRecipient() {
		return recipient;
	}

	public long getCreated() {
		return created;
	}
	
	public boolean isReturned() {
		return returned;
	}
	
	public void setReturned(boolean returned) {
		this.returned = returned; 
	}
	
	public static Parcel loadFromConf(ConfigurationSection conf, String id) {
		ItemStack contents = conf.getItemStack(id + ".contents");
		String sender = conf.getString(id + ".sender");
		String recipient = conf.getString(id + ".recipient");
		if (contents == null || sender == null || recipient == null ||
				!conf.isSet(id + ".created") || !conf.isSet(id + ".lda")
				|| !conf.isSet(id + ".returned")) {
			return null;
		}

		long created = conf.getLong(id + ".created");
		long lastDeliveryAttempt = conf.getLong(id + ".lda");
		boolean returned = conf.getBoolean(id + ".returned");
		
		return new Parcel(id, contents, sender, recipient, created, lastDeliveryAttempt, returned);
	}
	
	public void saveToConf(ConfigurationSection conf) {
		conf.set(id + ".contents", contents);
		conf.set(id + ".sender", sender);
		conf.set(id + ".recipient", recipient);
		conf.set(id + ".created", created);
		conf.set(id + ".lda", lastDeliveryAttempt);
		conf.set(id + ".returned", returned);
	}
	
}
