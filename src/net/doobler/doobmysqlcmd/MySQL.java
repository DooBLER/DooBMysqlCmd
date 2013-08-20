package net.doobler.doobmysqlcmd;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


public class MySQL {
	
	// Jak długo czekać podczas sprawdzania czy połączenie jest poprawne (default 2 sec)
    private static final int VALID_TIMEOUT = 2;
    
    // Minimalny czas co jaki sprawdzane jest połączenie (w sekundach).
    private static final int MIN_CHECK_TIME = 5;
    private long lastcheck = 0;
	
    // Zmienna przechowuje referencję do głównego obiektu pluginu.
	protected DooBMysqlCmd plugin;
	
	// Dane do logowania mysql
	private String user = ""; 
    protected String database = ""; 
    private String password = ""; 
    private String port = ""; 
    private String hostname = "";
    // Prefix tabel w bazie danych
    private String prefix = "";
    
    // Obiekt połączenia z bazą.
    private Connection c = null;
    

    /**
     * Tworzy instancję klasy MySQL i otwiera połączenie z bazą.
     * 
     * Obiekt tej klasy powinien być tworzony raz w funkcji onEnable pluginu.
     * 
     * @param plugin Referencja do instancji głównej klasy pluginu.
     * @param hostname Adres hosta MySQL (zazwyczaj localhost)
     * @param portnmbr Port MySQL (zazwyczaj 3306)
     * @param database Nazwa bazy danych
     * @param username Nazwa użytkownika
     * @param password Hasło do bazy
     * @param tblprefix Prefix tabel pluginu.
     */
    public MySQL(DooBMysqlCmd plugin, String hostname, String portnmbr, String database, String username, String password, String tblprefix) { 
    	this.plugin = plugin;
    	
    	this.hostname = hostname; 
        this.port = portnmbr; 
        this.database = database; 
        this.user = username; 
        this.password = password; 
        this.prefix = tblprefix;
        
        this.open();
    }
    
    
    /**
     * Tworzy połączenie z bazą danych.
     * 
     * @return Connection - obiekt połączenia z bazą danych.
     */
    protected Connection open() { 
        try { 
            Class.forName("com.mysql.jdbc.Driver"); 
            this.c = DriverManager.getConnection("jdbc:mysql://" + this.hostname +
            									 ":" + this.port + "/" + this.database + 
            									 "?autoReconnect=false", 
            									 this.user, this.password); 
            return c; 
        } catch (SQLException e) { 
            System.out.println("Could not connect to MySQL server! because: " +
            				   e.getMessage()); 
        } catch (ClassNotFoundException e) { 
            System.out.println("JDBC Driver not found!"); 
        } 
        return this.c; 
    } 
    
    
    /**
     * Sprawdza czy tabela o danej nazwie istnieje w bazie.
     * 
     * @param tablename Nazwa tabeli do sprawdzenia, nazwa tabeli powinna być podana z prefixem.
     * @return "true" jeśli tabela istnieje, "false" w przeciwnym przypadku.
     */
    public boolean tableExists(String tablename) {
    	
    	String sql = "SELECT * " +
    				 "FROM information_schema.TABLES " +
    				 "WHERE table_schema = ? " + 
    				 "AND table_name = ?";
    	
    	ResultSet res = null;
    	boolean status = false;
    	
    	try {
    		PreparedStatement prest = this.c.prepareStatement(sql);
    		prest.setString(1, this.database);
    		prest.setString(2, tablename);
    		res = prest.executeQuery();
    		
    		// jeśli jest następny wiersz (czyli pierwszy) to znaczy, że tabela istnieje
    		status = res.next();
    		
            prest.close();
    		
    	} catch(SQLException e) {
            e.printStackTrace();
        }

    	if (status) {
    		return true;
    	}
    	
		return false;
    }
    
    
    /**
     * Zwraca zdefiniowany prefix tabel pluginu.
     * 
     * @return Prefix tabel pluginu.
     */
    public String getPrefix() {
        return this.prefix;
    }
    
    /**
     * Zwraca nazwę podanej tabeli z dodanym prefixem.
     * 
     * @return Nazwa tabeli z prefixem.
     */
    public String getPrefixed(String tblname) {
        return this.prefix + tblname;
    }
    
    
    /**
     * Zwraca obiekt połączenia z bazą.
     * 
     * Jeśli połączenie padło lub nie istnieje to próbuje je naprawić.
     * Inspired by McMMO
     * 
     * @return Connection
     */
    public Connection getConn() {
    	
    	long timer = 0;
    	
    	// DEBUG
    	if(this.plugin.getConfig().getBoolean("debug")) {
    		timer = System.nanoTime();
    	}
    	
    	boolean isClosed = true;
        boolean isValid = false;
        boolean exists = (this.c != null);
        
        
        if (exists) {
        	
        	if (this.lastcheck+(MIN_CHECK_TIME*1000) > System.currentTimeMillis()) {
        		// DEBUG
        		if(this.plugin.getConfig().getBoolean("debug")) {
        			this.plugin.getLogger().info("debug: Time getConn: " + (System.nanoTime() - timer) + "ns");
        		}
        		return this.c;
        	} else {
        		this.lastcheck = System.currentTimeMillis();
        	}
        	
        	
            try {
                isClosed = this.c.isClosed();
            }
            catch (SQLException e) {
                isClosed = true;
                e.printStackTrace();
                printErrors(e);
            }

            if (!isClosed) {
                try {
                    isValid = this.c.isValid(VALID_TIMEOUT);
                }
                catch (SQLException e) {
                    // Don't print stack trace because it's valid to lose idle connections to the server and have to restart them.
                    isValid = false;
                }
            }
        }
        
        // Jeśli wszystko ok to wszystko jest poprawnie.
        if (exists && !isClosed && isValid) {
        	
        	// DEBUG
    		if(this.plugin.getConfig().getBoolean("debug")) {
    			this.plugin.getLogger().info("debug: Time getConn: " + (System.nanoTime() - timer) + "ns");
    		}
        	
            return this.c;
        }

        // Cleanup after ourselves for GC and MySQL's sake
        if (exists && !isClosed) {
            try {
                this.c.close();
            }
            catch (SQLException ex) {
                // This is a housekeeping exercise, ignore errors
            }
        }
        
        // Ponowne łączenie.
     	this.open();
     	
     	// DEBUG
		if(this.plugin.getConfig().getBoolean("debug")) {
			this.plugin.getLogger().info("debug: Time getConn: " + (System.nanoTime() - timer) + "ns");
		}
        
        return this.c; 
    } 
    
    public void closeConnection(Connection c) { 
        c = null; 
    }

	
	
	
	private void printErrors(SQLException ex) {
        this.plugin.getLogger().severe("SQLException: " + ex.getMessage());
        this.plugin.getLogger().severe("SQLState: " + ex.getSQLState());
        this.plugin.getLogger().severe("VendorError: " + ex.getErrorCode());
    }

}
