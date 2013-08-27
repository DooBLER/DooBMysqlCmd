package net.doobler.doobmysqlcmd;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
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
	 * Aktualizuje stan w bazie danych na "wykonane" dla podanej listy komend
	 * @param executedCmds
	 * @throws SQLException
	 */
	public void updateExecutedCmds(HashSet<Integer> executedCmds) {
		
		Date curdate = new Date();
		Timestamp curtimestamp = new Timestamp(curdate.getTime());
		
		Iterator<Integer> executedIterator = executedCmds.iterator();
		
		PreparedStatement prest = this.getPreparedStatement("updateCommandStatus");
		try {
			prest.clearBatch();
			
			while(executedIterator.hasNext()) {
				prest.setTimestamp(1, curtimestamp);
				prest.setInt(2, executedIterator.next());
				prest.addBatch();
			}
			prest.executeBatch();
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}
	

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
