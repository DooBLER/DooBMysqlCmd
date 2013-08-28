package net.doobler.doobmysqlcmd.listeners;



import java.util.Iterator;
import java.util.Set;

import net.doobler.doobmysqlcmd.DooBMysqlCmd;


import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;



public class InventoryListener implements Listener {
	
	public DooBMysqlCmd plugin;
	
	public InventoryListener(DooBMysqlCmd plugin) {
	    this.plugin = plugin;
	}
	
	
	@SuppressWarnings("deprecation")
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onInventoryClick(InventoryClickEvent event) {
		
		Player player = (Player) event.getWhoClicked();
		
		// sprawdza czy to inventory tego pluginu
		if(event.getInventory().getName().equalsIgnoreCase(this.plugin.dInvName)) {
			// jeśli to górne inventory to...
			if(event.getRawSlot() < 54) {
				// zezwolone w górnum inv
				if(event.getAction() == InventoryAction.PICKUP_ALL ||
						event.getAction() == InventoryAction.PICKUP_HALF ||
						event.getAction() == InventoryAction.PICKUP_ONE ||
						event.getAction() == InventoryAction.PICKUP_SOME ||
						event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
					return;
				}
				event.setCancelled(true);
				player.updateInventory();
				
			} else {
				// zabronione w dolnym inv
				if(event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
					event.setCancelled(true);
					player.updateInventory();
				}
			}

		}

	}
	
	@SuppressWarnings("deprecation")
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onInventoryDrag(InventoryDragEvent event) {
		
		Player player = (Player) event.getWhoClicked();
		
		// sprawdza czy to inventory tego pluginu
		if(event.getInventory().getName().equalsIgnoreCase(this.plugin.dInvName)) {
			
			Set<Integer> slots = event.getRawSlots();
			
			Iterator<Integer> slotiter = slots.iterator();
			
			while(slotiter.hasNext()) {
				// jesli przy przeciąganiu wjedzie na górne inv
				// to canceluje event
				if(slotiter.next() < 54) {
					event.setCancelled(true);
					player.updateInventory();
					// jeśli znajdzie pierwszy to dalej nie sprawdza
					return;
				}
			}

		}
	}
}
