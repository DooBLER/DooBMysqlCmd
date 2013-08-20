package net.doobler.doobmysqlcmd;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Klasa Data Access Object
 * 
 * Rozszerza klasę dostępu do bazy MySQL.
 * Zawiera SQL dla preparedStatement itp.
 * 
 * @author DooBLER
 *
 */
public class DooBMysqlCmdDAO extends MySQL {
	
	// Zmienne do przechowywania zapytań i gotowych PreparedStatements
	private Map<String, String> prepSQL = new HashMap<String, String>();
	private Map<String, PreparedStatement> prepStat = new HashMap<String, PreparedStatement>();
	
	public DooBMysqlCmdDAO(DooBMysqlCmd plugin, String hostname, String portnmbr,
			String database, String username, String password, String tblprefix) {
		super(plugin, hostname, portnmbr, database, username, password, tblprefix);
		
		
		// sprawdzenie czy trzeba utworzyć tabele w bazie
		if(!this.tableExists(this.getPrefixed("commands"))) {
			this.createTables();
		}
		
//		int dbver = this.plugin.getConfig().getInt("dbversion", 0);
//		
//		switch(dbver) {
//			case 0:
//				this.update0to1();
//			case 1:
//				this.update1to2();
//				break;
//		}
		
		
		// dodaje SQL do listy dla Prepared statement
		this.addPrepSQL();
	}

	
	/**
	 * Zwraca prepared statement po nazwie
	 * 
	 * Zwraca wcześniej utworzony prepared statement, jeśli nie ma to tworzy.
	 * 
	 * @param name
	 * @return
	 */
	public PreparedStatement getPreparedStatement(String name) {
		
		Connection conn = this.getConn();
		
		boolean exists = this.prepStat.containsKey(name);
		boolean isClosed = true;
		boolean isConn = false;
		
		PreparedStatement prest = null;

		// jeśli istnieje
		if(exists) {
			
			prest = this.prepStat.get(name);
			
			try {
                isClosed = prest.isClosed();
            }
            catch (SQLException e) {
                isClosed = true;
            }
			
			try {
				isConn = (prest.getConnection() == conn);
            }
            catch (SQLException e) {
            	isConn = false;
            }
		}
		
		
		if (!exists || !isConn || isClosed) {
			try {
				prest = conn.prepareStatement(this.prepSQL.get(name));
				this.prepStat.put(name, prest);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		try {
			prest.clearParameters();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return prest; 
	}
	
	
	/**
	 * Dodaje SQL do listy, z której powstaną PreparedStatement 
	 * 
	 * @param name Nazwa pod jaką będzie dostępny PreparedStatement
	 * @param sql SQL który zostanie użyty do stworzenia PreparedStatement
	 */
	public void addStatementSQL(String name, String sql) {
		this.prepSQL.put(name, sql);
	}
	
	
	/**
	 * Funkcja istnieje aby zebrać w jednym miejscu SQL dla PreparedStatement
	 */
	private void addPrepSQL() {
		
		// pobiera komendy do wykonania
		this.addStatementSQL("getCommands",
				"SELECT `id`, `player_name`, `cmd` " +
				"FROM " + this.getPrefixed("commands") + " " +
				"WHERE `id` >= ? " +
				"AND `executed` = 0 " +
				"ORDER BY id");
		
		// aktualizuje stan komendy po wukonaniu
        this.addStatementSQL("updateCommandStatus",
        		"UPDATE " + this.getPrefixed("commands") + " " +
				"SET " +
				"`executed` = 1," +
				"`exec_date` = ? " +
				"WHERE id = ?");
		
//		// aktualizuje dane gracza przy wychodzeniu z serwera
//        this.addStatementSQL("updatePlayerQuit",
//        		"UPDATE `" + this.getPrefixed("players") + "` " +
//				"SET " +
//				"online = 0, " +
//				"last_logout = ?, " +
//				"num_secs_loggedon = num_secs_loggedon + ? " +
//				"WHERE id = ?");
//        
//        // aktualizuje statystyki gracza przy wychodzeniu z serwera
//        this.addStatementSQL("updatePlayerStatQuit",
//				"UPDATE `" + this.getPrefixed("morestats") + "` " +
//				"SET " +
//				"dist_foot = dist_foot + ?, " +
//				"dist_fly = dist_fly + ?, " +
//				"dist_swim = dist_swim + ?, " +
//				"dist_pig = dist_pig + ?, " +
//				"dist_cart = dist_cart + ?, " +
//				"dist_boat = dist_boat + ?, " +
//				"dist_horse = dist_horse + ?, " +
//				"bed_enter = bed_enter + ?, " +
//				"fish = fish + ?, " +
//				"block_place = block_place + ?, " +
//				"block_break = block_break + ?, " +
//				"death_count = death_count + ?, " +
//				"pvp_deaths = pvp_deaths + ?, " +
//				"pvp_killer = ?, " +
//				"pvp_kills = pvp_kills + ?, " +
//				"pvp_victim = ? " +
//				"WHERE id = ?");
	}
	
	
	public List<DooBCmd> getNewCmds() {
		return this.getNewCmds(1);
	}
	
	public List<DooBCmd> getNewCmds(int id) {
		
		PreparedStatement prest = this.getPreparedStatement("getCommands");
		
		ResultSet res = null;
		
		List<DooBCmd> out = new LinkedList<DooBCmd>();
		
		try {
			prest.setInt(1, id);
			res = prest.executeQuery();
			
			while(res.next()) {
				out.add(new DooBCmd(res.getInt(1), res.getString(2), res.getString(3)));
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
		return out;
	}
	
	
	
	
	/**
	 * Dodaje nowego gracza do bazy i zwraca id
	 * 
	 * @param name - player name
	 * @param curtimestamp - timestamp
	 * @param ip - player ip
	 * @return database id
	 */
	public int addNewPlayer(String name, Timestamp curtimestamp, String ip) {
		Connection conn = this.getConn();
		
		// dodanie nowego gracza do bazy
		// PreparedStatemnt nie jest zapisany, bo dodawanie nowych graczy
		// występuje relatywnie dużo rzadziej 
		
		// players table
		String sql = "INSERT INTO " + plugin.db.getPrefixed("players") + " "  +
			  "SET " +
			  "player_name = ?, " +
			  "player_ip = ?," +
			  "online = 1, " +
			  "firstever_login = ?, " +
			  "last_login = ?, " +
			  "num_logins = 1, " +
			  "this_login = ?, " +
			  "num_secs_loggedon = 1";
		
		PreparedStatement prest;
		int newid = 0;
		try {
			prest = conn.prepareStatement(sql,
					Statement.RETURN_GENERATED_KEYS);
			prest.setString(1, name);
			prest.setString(2, ip);
			prest.setTimestamp(3, curtimestamp);
			prest.setTimestamp(4, curtimestamp);
			prest.setTimestamp(5, curtimestamp);
			prest.executeUpdate();
			
			ResultSet rs = prest.getGeneratedKeys();
			
			if (rs.next()){
			    newid = rs.getInt(1);
			}
			
			prest.close();	
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		// morestats table
		sql = "INSERT INTO " + plugin.db.getPrefixed("morestats") + " "  +
				"SET " +
				"id = ?, " +
				"dist_foot = 0, " +
				"dist_fly = 0, " +
				"dist_swim = 0, " +
				"dist_pig = 0, " +
				"dist_cart = 0, " +
				"dist_boat = 0, " +
				"dist_horse = 0, " +
				"bed_enter = 0, " +
				"fish = 0, " +
				"block_place = 0, " +
				"block_break = 0, " +
				"death_count = 0, " +
				"pvp_deaths = 0, " +
				"pvp_killer = '', " +
				"pvp_kills = 0, " +
				"pvp_victim = ''";		
		try {
			prest = conn.prepareStatement(sql);
			prest.setInt(1, newid);
			prest.executeUpdate();
			
			prest.close();	
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return newid;
	}
	
	
	/**
	 * Zwraca listę nazw wszystkich graczy
	 * 
	 * @return
	 */
	public List<String> getAllNames() {
		List<String> player_names = new ArrayList<String>();
		
		Connection conn = this.getConn();
		
		String sql = "SELECT `player_name` FROM " + this.getPrefixed("players") + " " +
				"WHERE 1";
		
		try {
			Statement st = conn.createStatement();
			ResultSet players_set = st.executeQuery(sql);
			
			while(players_set.next()) {
				player_names.add(players_set.getString("player_name"));
			}
	
			st.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return player_names;
	}
	

	
	
	/**
	 * Funkcja usuwa danego gracza z bazy DooBStat
	 * 
	 * @param player_name
	 */
	public boolean removePlayer(String player_name) {
		Connection conn = this.getConn();
		
		String sql = "DELETE t1, t2 " +
				"FROM " + this.getPrefixed("players") + " AS t1 " +
				"INNER JOIN " + this.getPrefixed("morestats") +" AS t2 " +
				"WHERE t1.player_name=? " +
				"AND t1.id=t2.id";
		
		int delrows = 0;
		try {
			PreparedStatement prest = conn.prepareStatement(sql);
			prest.setString(1, player_name);
			
			this.plugin.getLogger().info(prest.toString());
			
			delrows = prest.executeUpdate();
			
			
			prest.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		if(delrows < 1) {
			return false;
		}
		
		return true;
	}
	
	
	/**
	 * Zapisuje zebrane dane gracza
	 */
//	public void savePlayerData(DooBStatPlayerData playerData) {
//		
//		Map<String, DooBStatPlayerData> tmp = new HashMap<String, DooBStatPlayerData>();
//		tmp.put(playerData.getPlayerName().toLowerCase(), playerData);
//		
//		this.savePlayersData(tmp);
//		
//	}
	
//	public void savePlayersData(Map<String, DooBStatPlayerData> data_map)
//	{
//		Date curdate = new Date();
//		Timestamp curtimestamp = new Timestamp(curdate.getTime());
//		
//		PreparedStatement prest = this.getPreparedStatement("updatePlayerQuit");
//		PreparedStatement prest2 = this.getPreparedStatement("updatePlayerStatQuit");
//		
//		Iterator<Map.Entry<String, DooBStatPlayerData>> wpisy = data_map.entrySet().iterator();
//		while (wpisy.hasNext()) {
//		    Map.Entry<String, DooBStatPlayerData> wpis = wpisy.next();
//		    try {
//		    	DooBStatPlayerData playerData = wpis.getValue();
//		    	
//				prest.setTimestamp(1, curtimestamp);
//				prest.setInt(2, (int)((curdate.getTime() - playerData.getLoginDate().getTime())/1000));
//				prest.setInt(3, playerData.getPlayerId());
//				prest.addBatch();
//				
//				prest2.setInt(1, (int)playerData.getDist(DooBStatPlayerData.FOOT));
//				prest2.setInt(2, (int)playerData.getDist(DooBStatPlayerData.FLY));
//				prest2.setInt(3, (int)playerData.getDist(DooBStatPlayerData.SWIM));
//				prest2.setInt(4, (int)playerData.getDist(DooBStatPlayerData.PIG));
//				prest2.setInt(5, (int)playerData.getDist(DooBStatPlayerData.CART));
//				prest2.setInt(6, (int)playerData.getDist(DooBStatPlayerData.BOAT));
//				prest2.setInt(7, (int)playerData.getDist(DooBStatPlayerData.HORSE));
//				prest2.setInt(8, playerData.getBedEnter());
//				prest2.setInt(9, playerData.getFish());
//				prest2.setInt(10, playerData.getBlockPlace());
//				prest2.setInt(11, playerData.getBlockBreak());
//				prest2.setInt(12, playerData.getDeath());
//				prest2.setInt(13, playerData.getPvpDeath());
//				prest2.setString(14, playerData.getPvpKiller());
//				prest2.setInt(15, playerData.getPvpKill());
//				prest2.setString(16, playerData.getPvpVictim());
//				prest2.setInt(17, playerData.getPlayerId());
//				prest2.addBatch();
//			} catch (SQLException e) {
//				e.printStackTrace();
//			}
//		    wpisy.remove();
//		}
//		
//		try {
//			prest.executeBatch();
//			prest.clearBatch();
//			prest2.executeBatch();
//			prest2.clearBatch();
//		} catch (SQLException e) {
//			e.printStackTrace();
//		}
//		
//	}
	
	
	/**
	 * Tworzy strukturę tabel pluginu
	 */
	public void createTables() {
		
		Connection conn = this.getConn();
		
		// commands
		String sql = "CREATE TABLE IF NOT EXISTS `" + this.getPrefixed("commands") + "` (" +
				"`id` int(11) NOT NULL AUTO_INCREMENT, " +
				"`player_name` varchar(16) NOT NULL, " +
				"`cmd` text NOT NULL, " +
				"`executed` tinyint(1) NOT NULL, " +
				"`add_date` datetime DEFAULT NULL, " +
				"`exec_date` datetime DEFAULT NULL, " +
				 
				"PRIMARY KEY (`id`) " +
				") ENGINE=InnoDB DEFAULT CHARSET=utf8 AUTO_INCREMENT=1";
		try {
			Statement statement = conn.createStatement();
			statement.executeUpdate(sql);
			statement.close();
			this.plugin.getLogger().info("DB table created: 'commands'");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		

	}
	

	
	

	

}
