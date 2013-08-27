package net.doobler.doobmysqlcmd.tasks;


import java.util.List;

import net.doobler.doobmysqlcmd.DooBCmd;
import net.doobler.doobmysqlcmd.DooBMysqlCmd;

import org.bukkit.scheduler.BukkitRunnable;


public class CheckAndRunCmdsTask extends BukkitRunnable {
	
	private final DooBMysqlCmd plugin;
	
	
	public CheckAndRunCmdsTask(DooBMysqlCmd plugin) {
		this.plugin = plugin;
	}

	@Override
	public void run() {
		List<DooBCmd> cmds = plugin.db.getNewCmds(plugin.lastCmdId+1);
		
		int cmdssize = cmds.size();
		
		if(cmdssize > 0) {
			this.plugin.cmdQueue.addAll(cmds);
			this.plugin.lastCmdId = cmds.get(cmdssize-1).getId();
		}
		
		// run task executing command queue
		new ExecuteCmdTask(this.plugin).runTaskLater(this.plugin, 20);
		
	}

}
