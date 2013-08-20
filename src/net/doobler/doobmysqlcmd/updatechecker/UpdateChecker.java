package net.doobler.doobmysqlcmd.updatechecker;

import net.doobler.doobmysqlcmd.DooBMysqlCmd;


public class UpdateChecker {
	
	private DooBMysqlCmd plugin;
	private String filesFeed;
	
	public UpdateChecker(DooBMysqlCmd plugin, String url) {
		this.plugin = plugin;
		this.filesFeed = url;

		new UpdateCheckerTask(this.plugin, this.filesFeed).runTaskAsynchronously(this.plugin);
	}
	
}
