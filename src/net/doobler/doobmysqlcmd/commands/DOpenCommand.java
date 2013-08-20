package net.doobler.doobmysqlcmd.commands;


import net.doobler.doobmysqlcmd.DooBMysqlCmd;


import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class DOpenCommand implements CommandExecutor {

	private DooBMysqlCmd plugin;
	 
	public DOpenCommand(DooBMysqlCmd plugin) {
		this.plugin = plugin;
	}
	
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label,
			String[] args) {
		
		if (sender instanceof Player) {
            Player player = (Player) sender;
            
            Inventory aaa = Bukkit.createInventory(player, 54, this.plugin.dInvName);
            
            player.openInventory(aaa);
            
        } else {
            sender.sendMessage("You must be a player!");
            return false;
        }
		
		
		// https://forums.bukkit.org/threads/creating-a-dummy-inventory.77329/
		
		// http://jd.bukkit.org/beta/doxygen/d4/da9/interfaceorg_1_1bukkit_1_1Server.html
		
		// https://github.com/zonedabone/VirtualChest/blob/master/src/com/zone/vchest/objects/VirtualChest.java
		
		// http://wiki.bukkit.org/Plugin_Tutorial#Commands
		
		return false;
	}

}
