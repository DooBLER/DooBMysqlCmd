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
		} else {
			return;
		}
		
		// jeśli coś czeka w kolejce
		if(!ExecuteCmdTask.plugin.cmdQueue.isEmpty()) {
			
			// to w teorii nie powinno dawać wyjątku...
			try {
				DooBCmd dcmd = ExecuteCmdTask.plugin.cmdQueue.take();
				
				ExecuteCmdTask.plugin.getLogger().info("Executing command (id: " +
											  dcmd.getId() +", cmd: " +
											  dcmd.getCmd() + ")");
				
				Server serv = ExecuteCmdTask.plugin.getServer();
				
				serv.dispatchCommand(serv.getConsoleSender(), dcmd.getCmd());
				
				ExecuteCmdTask.plugin.executedCmds.add(dcmd.getId());
				
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			// jeśli wykonano 10 komend pod rząd, to update ich stanu w bazie
			if(ExecuteCmdTask.plugin.executedCmds.size() >= 10) {
				ExecuteCmdTask.plugin.db.updateExecutedCmds(ExecuteCmdTask.plugin.executedCmds);
			}
			
			new ExecuteCmdTask(ExecuteCmdTask.plugin).runTaskLater(ExecuteCmdTask.plugin, 10);
			
		} else {
			// Jeśli nie ma co wykonać to resetuje flagę.
			ExecuteCmdTask.plugin.execFlag = false;
			
			// Jeśli były wykonane jakieś komendy to aktualizacja ich stanu w bazie
			if(ExecuteCmdTask.plugin.executedCmds.size() > 0) {
				ExecuteCmdTask.plugin.db.updateExecutedCmds(ExecuteCmdTask.plugin.executedCmds);
			}
		}
		
	}
	
	

}
