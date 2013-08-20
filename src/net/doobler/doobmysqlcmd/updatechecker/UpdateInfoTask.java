package net.doobler.doobmysqlcmd.updatechecker;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;


public class UpdateInfoTask extends BukkitRunnable {
	private final JavaPlugin plugin;
	String version;
	String link;
	
	public UpdateInfoTask(JavaPlugin plugin, String ver, String link) {
		this.plugin = plugin;
		this.version = ver;
		this.link = link;
	}

	@Override
	public void run() {
		this.plugin.getLogger().info("----------------------------------------------------");
		this.plugin.getLogger().info("A new version is available: "+this.version);
		this.plugin.getLogger().info("Get it from: "+this.link);
		this.plugin.getLogger().info("----------------------------------------------------");
		
	}

}
