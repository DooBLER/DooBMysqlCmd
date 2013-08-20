package net.doobler.doobmysqlcmd.tasks;


import net.doobler.doobmysqlcmd.DooBCmd;
import net.doobler.doobmysqlcmd.DooBMysqlCmd;

import org.bukkit.Server;
import org.bukkit.scheduler.BukkitRunnable;


public class ExecuteCmdTask extends BukkitRunnable {

	private static DooBMysqlCmd plugin;
	
	
	public ExecuteCmdTask(DooBMysqlCmd plugin) {
		ExecuteCmdTask.plugin = plugin;
	}
	
	
	@Override
	public void run() {
		// ustawienie flagi informującej, że wykonywanie komend jest w toku
		if(!ExecuteCmdTask.plugin.execFlag) {
			ExecuteCmdTask.plugin.execFlag = true;
		}
		
		// jeśli coś czeka w kolejce
		if(!ExecuteCmdTask.plugin.cmdQueue.isEmpty()) {
			
			// to w teorii nie powinno dawać wyjątku...
			try {
				DooBCmd dcmd = this.plugin.cmdQueue.take();
				
				ExecuteCmdTask.plugin.getLogger().info("Executing command (id: " +
											  dcmd.getId() +", cmd: " +
											  dcmd.getCmd() + ")");
				
				Server serv = ExecuteCmdTask.plugin.getServer();
				
				serv.dispatchCommand(serv.getConsoleSender(), dcmd.getCmd());
				
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			// TODO: zapytanie do bazy z ustawieniem daty wykonania
			
			new ExecuteCmdTask(ExecuteCmdTask.plugin).runTaskLater(ExecuteCmdTask.plugin, 10);
			
		} else {
			ExecuteCmdTask.plugin.execFlag = false;
		}
		
	}
	
	

}
