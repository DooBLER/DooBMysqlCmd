package net.doobler.doobmysqlcmd;


import java.util.concurrent.LinkedBlockingQueue;

import net.doobler.doobmysqlcmd.DooBMysqlCmdDAO;
import net.doobler.doobmysqlcmd.commands.DOpenCommand;
import net.doobler.doobmysqlcmd.listeners.InventoryListener;
import net.doobler.doobmysqlcmd.listeners.PlayerListener;
import net.doobler.doobmysqlcmd.tasks.CheckAndRunCmdsTask;
import net.doobler.doobmysqlcmd.updatechecker.UpdateChecker;

import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;



public final class DooBMysqlCmd extends JavaPlugin {
	
    // obiekt połączenia z bazą.
    public DooBMysqlCmdDAO db = null;
    
    // wyświetlana nazwa inwentory
    public String dInvName = "dOpen Inventory";
    
    // Kolejka komend do wykonania
    public LinkedBlockingQueue<DooBCmd> cmdQueue = new LinkedBlockingQueue<DooBCmd>();
    public int lastCmdId = 0;
    public boolean execFlag = false;
    
	
	public final PlayerListener playerListener = new PlayerListener(this);
	public final InventoryListener inventoryListener = new InventoryListener(this);
	
	
	
	
	
	/**
	 * onEnable
	 */
	@Override
	public void onEnable() {
		
		//zapisanie domyślnego konfigu na dysk
		this.saveDefaultConfig();
		
		PluginManager pm = getServer().getPluginManager();
		
		// jeśli konfig nie jest ustawiony to wyłącza plugin
		if(this.getConfig().getString("mysql.dbname").isEmpty()) {
			this.getLogger().warning("----------------------------------------");
			this.getLogger().warning("Set " +
					this.getDescription().getName() +
					" config, then restart server.");
			this.getLogger().warning("----------------------------------------");
			
			pm.disablePlugin(this);
			
			return;
		}
		
		// nawiązanie połączenia z bazą
		this.db = new DooBMysqlCmdDAO(this,
				 this.getConfig().getString("mysql.host"),
				 this.getConfig().getString("mysql.port"),
				 this.getConfig().getString("mysql.dbname"),
				 this.getConfig().getString("mysql.user"),
				 this.getConfig().getString("mysql.pass"),
				 this.getConfig().getString("mysql.prefix"));
		
		
		
		pm.registerEvents(this.playerListener, this);
		pm.registerEvents(this.inventoryListener, this);
		
		// rejestracja komend
		getCommand("dopen").setExecutor(new DOpenCommand(this));
		
		
		// dodanie tasku sprawdzającego komendy
		new CheckAndRunCmdsTask(this).runTaskTimer(this, 40, 100);
		
		
		
		
		// sprawdzenie czy jest dostępna nowsza wersja i wyświetlenie
		// komunikatu w konsoli
		if(!this.getConfig().getBoolean("debug")) {
			// sprawdzaj update tylko gdy wyłączony jest debug
			new UpdateChecker(this, "http://apps.doobler.net");
		}
		
	}
	
	
	
	
	
}

