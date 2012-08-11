package net.minebot.enderpost;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityTeleportEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class EnderPostListener implements Listener {

	private EnderPostPlugin plugin;
	
	public EnderPostListener(EnderPostPlugin plugin) {
		this.plugin = plugin;
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        if(plugin.getServer().getOnlinePlayers().length <= 1) {
            DeliveryManager.stopDeliveries();
        }
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerJoin(PlayerJoinEvent event) {
		if(plugin.getServer().getOnlinePlayers().length <= 1) {
			DeliveryManager.startDeliveries();
        }
	}
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityChangeBlock(EntityChangeBlockEvent event) {
        if(!event.isCancelled() &&
        		DeliveryManager.getPostmanByEnderman(event.getEntity().getUniqueId()) != null)
            event.setCancelled(true);
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityTarget(EntityTargetEvent event) {
        if(!event.isCancelled() &&
        		DeliveryManager.getPostmanByEnderman(event.getEntity().getUniqueId()) != null) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onEndermanTeleport(EntityTeleportEvent event) {
        if(!event.isCancelled() &&
        		DeliveryManager.getPostmanByEnderman(event.getEntity().getUniqueId()) != null) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityDamage(EntityDamageEvent event) {
    	if (event instanceof EntityDamageByEntityEvent) {
    		EntityDamageByEntityEvent ede = (EntityDamageByEntityEvent)event;
        	EnderPostman postman = DeliveryManager.getPostmanByEnderman(event.getEntity().getUniqueId());
        	if (!event.isCancelled() && postman != null && postman.getPlayer() != null && ede.getDamager() instanceof Player) {
        		Player dplayer = (Player)(ede.getDamager());
        		if (postman.getPlayer().getName().equals(dplayer.getName())) {
        			EnderPostUtil.tellPlayer(postman.getPlayer(), "\"Hey! No need to get violent! Take it, sheesh!\"");
        			postman.getEnderman().setHealth(postman.getEnderman().getMaxHealth());
        			postman.gtfo();
        		}
        		else event.setCancelled(true);
        	}
        	else if (!event.isCancelled() && postman != null) {
        		event.setCancelled(true);
        	}
    	}	
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
    	EnderPostman postman = DeliveryManager.getPostmanByEnderman(event.getRightClicked().getUniqueId());
    	if (!event.isCancelled() && postman != null && postman.getPlayer() != null &&
    			postman.getPlayer().getName().equals(event.getPlayer().getName())) {
    		postman.deliverParcel();
    	}
    }
}
