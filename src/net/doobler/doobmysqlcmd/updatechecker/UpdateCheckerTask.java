package net.doobler.doobmysqlcmd.updatechecker;


import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import javax.xml.parsers.DocumentBuilderFactory;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class UpdateCheckerTask extends BukkitRunnable {

	private final JavaPlugin plugin;
	private URL filesFeed;
	private URL pluginStats;
	private String statParams;
	private String version;
	private String link;
	
	public UpdateCheckerTask(JavaPlugin plugin, String url) {
		this.plugin = plugin;
		
		try {
			this.filesFeed = new URL(url);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		
		try {
			this.pluginStats = new URL("http://apps.doobler.net/bukkitplg/");
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		
		try {
			this.statParams = "plgname=" + URLEncoder.encode(plugin.getDescription().getName(), "UTF-8") + 
					"&plgver=" + URLEncoder.encode(plugin.getDescription().getVersion(), "UTF-8") + 
					"&srvname=" + URLEncoder.encode(plugin.getServer().getServerName(), "UTF-8") +
					"&srvmotd=" + URLEncoder.encode(plugin.getServer().getMotd(), "UTF-8") +
					"&srvip=" + URLEncoder.encode(plugin.getServer().getIp(), "UTF-8") +
					"&srvport=" + plugin.getServer().getPort();
		} catch (UnsupportedEncodingException e) {
			if(this.plugin.getConfig().getBoolean("debug"))
			{
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public void run() {
		
		if(this.plugin.getConfig().getBoolean("pluginStats", true))
		{
			try {
				HttpURLConnection connection = (HttpURLConnection)this.pluginStats.openConnection();
				connection.setRequestMethod("POST");
				connection.setDoOutput(true);
				connection.setDoInput(true);
				connection.setUseCaches(false);
				connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=utf-8;");
				connection.setRequestProperty("Content-Length", Integer.toString(this.statParams.length()));
				
				// SEND REQUEST
				DataOutputStream outStream = new DataOutputStream (connection.getOutputStream());
				outStream.writeBytes(this.statParams);
				outStream.flush();
				outStream.close();
				
				connection.getInputStream();

			} catch (Exception e1) {
				if(this.plugin.getConfig().getBoolean("debug"))
				{
					e1.printStackTrace();
				}
			}
		}
		
		
		
		if(this.plugin.getConfig().getBoolean("checkVersion", true))
		{
			try {
				InputStream input = this.filesFeed.openConnection().getInputStream();
				
				Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(input);
				
				Node latestFile = document.getElementsByTagName("item").item(0);

				NodeList children = latestFile.getChildNodes();
				
				this.version = children.item(1).getTextContent().replaceAll("[ \na-zA-Z_-]", "");
				this.link = children.item(3).getTextContent();
				
				if(!this.plugin.getDescription().getVersion().equalsIgnoreCase(this.version)) {
					new UpdateInfoTask(this.plugin, this.version, this.link).runTask(this.plugin);
				}
				
			} catch (Exception e) {
				if(this.plugin.getConfig().getBoolean("debug"))
				{
					e.printStackTrace();
				}
			}
		}
		
	}

}
