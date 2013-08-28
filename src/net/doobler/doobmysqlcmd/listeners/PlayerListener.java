package net.doobler.doobmysqlcmd.listeners;



import net.doobler.doobmysqlcmd.DooBMysqlCmd;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;


public class PlayerListener implements Listener {
	
	public DooBMysqlCmd plugin;
	
	public PlayerListener(DooBMysqlCmd plugin) {
	    this.plugin = plugin;
	}
	
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerJoin(PlayerJoinEvent event) {
		
		// TODO: sprawdzenie czy gracz ma jakieś itemy dla dopen itd
		
		return;
		
	}
	
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerQuit(PlayerQuitEvent event) {
		
		// TODO: wywalenie gracza z HashMapy z itemami itd
	}
	

	
}
