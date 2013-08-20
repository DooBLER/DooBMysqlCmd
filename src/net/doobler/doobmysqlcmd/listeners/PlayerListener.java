package net.doobler.doobmysqlcmd.listeners;


import java.sql.Timestamp;
import java.util.Date;

import net.doobler.doobmysqlcmd.DooBMysqlCmd;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;




public class PlayerListener implements Listener {
	
	public DooBMysqlCmd plugin;
	
	public PlayerListener(DooBMysqlCmd plugin) {
	    this.plugin = plugin;
	}
	
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerJoin(PlayerJoinEvent event) {
		
		
		Date curdate = new Date();
		Timestamp curtimestamp = new Timestamp(curdate.getTime());
		
		
//		PreparedStatement prest = this.plugin.db.getPreparedStatement("getPlayerByName");
//		
//		ResultSet res = null;
//		
//		try {
//			prest.setString(1, event.getPlayer().getName());
//			res = prest.executeQuery();
//		} catch (Exception e1) {
//			e1.printStackTrace();
//		}
		

//		try {
//			// jeśli istnieje następny wiersz to dane zostały pobrane
//    		if(res.next()) {
//    			
//    			PreparedStatement prest2 = this.plugin.db.getPreparedStatement("updatePlayerJoin");
//    			
//    			prest2.setString(1, event.getPlayer().getAddress().getAddress().getHostAddress());
//    			prest2.setString(2, res.getString("this_login"));
//    			prest2.setTimestamp(3, curtimestamp);
//    			prest2.setInt(4, res.getInt("id"));
//    			prest2.executeUpdate();
//        		
//        		// dodanie gracza do listy obecnych na serwerze graczy
//        		plugin.playerslist.put(res.getString("player_name").toLowerCase(),
//        							   new DooBStatPlayerData(res.getInt("id"), 
//        									   	              res.getString("player_name"),
//        									   	              curdate));
//    			
//    		} else {    			
//    			int newid = this.plugin.db.addNewPlayer(
//    					event.getPlayer().getName(),
//    					curtimestamp,
//    					event.getPlayer().getAddress().getAddress().getHostAddress());
//        		
//        		// dodanie danych gracza do listy obecnych na serwerze graczy
//        		plugin.playerslist.put(event.getPlayer().getName().toLowerCase(),
//        							   new DooBStatPlayerData(newid, 
//        									   				  event.getPlayer().getName(),
//        									   	              curdate));
//    		}
//    		
//
//    	} catch(SQLException e) {
//            e.printStackTrace();
//        }
		
	}
	
	
//	@EventHandler(priority = EventPriority.MONITOR)
//	public void onPlayerQuit(PlayerQuitEvent event) {
//		
//		DooBStatPlayerData playerData = plugin.playerslist.get(event.getPlayer().getName().toLowerCase());
//		
//		// wykonaj tylko jeśli gracz istnieje w tabeli
//		if(playerData != null) {
//			
//			plugin.playerslist.remove(event.getPlayer().getName().toLowerCase());
//	
//			this.plugin.db.savePlayerData(playerData);
//			
//		}
//	}
	

	
}
