package net.doobler.doobmysqlcmd;

public class DooBCmd {
	private final int CMD_ID;
	private final String COMMAND;
	private final String PLAYER_NAME;
	
	
	public DooBCmd(int id, String player, String cmd) {
		this.CMD_ID = id;
		this.COMMAND = cmd;
		this.PLAYER_NAME = player;
	}
	
	
	public int getId() {
		return this.CMD_ID;
	}
	
	public String getCmd() {
		return this.COMMAND;
	}
	
	public String getPlayer() {
		return this.PLAYER_NAME;
	}
	
	@Override
	public String toString() {
		
		return Integer.toString(this.CMD_ID) + " - " + this.PLAYER_NAME + " - " + this.COMMAND;
	}
}
