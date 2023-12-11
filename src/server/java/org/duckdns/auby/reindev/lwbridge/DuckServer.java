package org.duckdns.auby.reindev.lwbridge;

import java.util.logging.*;
import java.net.*;
import java.io.*;

import com.fox2code.foxloader.loader.ServerMod;
import net.minecraft.server.MinecraftServer;
import net.minecraft.src.server.packets.Packet3Chat;

public class DuckServer extends DuckMod implements ServerMod {
	private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
	private final static MinecraftServer theMinecraft = MinecraftServer.getInstance();
	private int retry_conn = 0;
	private Socket ducksock;
	private BufferedReader duckin;
	private OutputStreamWriter duckout;
	private static DuckServer theduck;
	private String host;
	private int port;

	private void connect() {
		try {
			ducksock = new Socket(host, port);
			duckin = new BufferedReader(new InputStreamReader(ducksock.getInputStream()));
			duckout = new OutputStreamWriter(ducksock.getOutputStream());
			LOGGER.log(Level.INFO, "yay we did it");
		} catch (UnknownHostException e) {
			LOGGER.log(Level.WARNING, "Failed to resolve teh IP address of teh bridge, " + e.getMessage());
			retry_conn = 80;
			this.sock_null();
		} catch (IOException e) {
			LOGGER.log(Level.WARNING, "Failed to connect to teh bridge, " + e.getMessage());
			retry_conn = 80;
			this.sock_null();
		}
	}

	public static DuckServer get_instance() {
		return theduck;
	}

	private void sock_null() {
		LOGGER.log(Level.WARNING, "Disconnected from teh bridge.");
		if(ducksock != null) try { ducksock.close(); } catch(IOException e) {}
		ducksock = null;
		duckin = null;
		duckout = null;
	}



	@Override
	public void onPreInit() {
		host = System.getProperty("lwbridge.host");
		if(host == null) host = "127.0.0.1";
		try {
			String ps = System.getProperty("lwbridge.port");
			if(ps == null) ps = "48217";
			port = Integer.parseInt(ps, 10);
		} catch(NumberFormatException e) {
			LOGGER.log(Level.SEVERE, "Failed to parse -Dlwbridge.port: "+e.getMessage());
			System.exit(1);
		}
		if(port < 0 || port > 65535) {
			LOGGER.log(Level.SEVERE, "Value of -Dlwbridge.port is out of range");
			System.exit(1);
		}
		LOGGER.log(Level.INFO, "Connecting to bridge at "+host+":"+port);
		theduck = this;
		this.connect();
	}

	@Override
	public void onTick() {
		try {
			while(duckin != null && duckin.ready()) {
				String a = duckin.readLine();
				if(a == null) this.sock_null();
				else {
					String[] b = a.split("\0", 2);
					if(b.length == 2) {
						String yo = "[bridge] <" + b[0] + "> " + b[1];
						if(b[0] == "") yo = "[bridge] " + b[1];
						LOGGER.log(Level.INFO, yo);
						theMinecraft.configManager.sendPacketToAllPlayers(new Packet3Chat(yo));
					}
				}
			}
		} catch (IOException e) { retry_conn = 10; this.sock_null(); }
		if(retry_conn > 0) retry_conn--;
		if(ducksock == null && retry_conn == 0) {
			this.connect();
		}
	}

	public void send_into_bridge(String msg) {
		if(duckout != null) {
			try {
				duckout.write(msg + "\n");
				duckout.flush();
			} catch (IOException e) { this.sock_null(); }
		}
	}
}

