package org.getspout.spout.gui.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

import net.minecraft.src.FontRenderer;

import org.bukkit.ChatColor;
import org.getspout.spout.client.SpoutClient;
import org.lwjgl.opengl.GL11;
import org.spoutcraft.spoutcraftapi.gui.ListWidget;
import org.spoutcraft.spoutcraftapi.gui.ListWidgetItem;
import org.spoutcraft.spoutcraftapi.gui.RenderUtil;
import org.spoutcraft.spoutcraftapi.packet.PacketUtil;

public class ServerItem implements ListWidgetItem {

	ListWidget widget;

	String ip;
	int port;
	String title;
	
	int databaseId = -1;
	int ping = PING_POLLING;
	boolean polling = false;
	long pollStart;
	String motd = "A minecraft server";
	int players = 0, maxPlayers = 0;

	private PollThread currentThread;
	
	public static final int PING_POLLING = -1;
	public static final int PING_UNKNOWN = -2;
	public static final int PING_TIMEOUT = -3;
	public static final int PING_BAD_MESSAGE = -4;
	
	protected static int numPolling = 0;
	
	public ServerItem(String title, String ip, int port, int dbId) {
		this.ip = ip;
		this.port = port;
		this.title = title;
		this.databaseId = dbId;
		poll();
	}
	
	public void setListWidget(ListWidget widget) {
		this.widget = widget;
	}

	public ListWidget getListWidget() {
		return widget;
	}

	public int getHeight() {
		return 33;
	}

	public void render(int x, int y, int width, int height) {
		FontRenderer font = SpoutClient.getHandle().fontRenderer;
		
		font.drawStringWithShadow(title, x + 2, y + 2, 0xffffff);
		String sMotd = "";
		if(polling) {
			sMotd = ChatColor.BLUE+"Polling...";
		} else {
			switch(ping) {
			case PING_POLLING: 
				
				break;
			case PING_UNKNOWN:
				sMotd = ChatColor.RED + "Unknown Host!";
				break;
			case PING_TIMEOUT:
				sMotd = ChatColor.RED + "Operation timed out!";
				break;
			case PING_BAD_MESSAGE:
				sMotd = ChatColor.RED + "Bad Message (Server version likely outdated)!";
				break;
			default: 
				synchronized (motd) {
					sMotd = ChatColor.GREEN + motd;
				}
				break;
			}
		}
		
		font.drawStringWithShadow(sMotd, x+2, y + 11, 0xffffff);
		
		if(ping > 0 && !polling) {
			String sping = ping + " ms";
			int pingwidth = font.getStringWidth(sping);
			font.drawStringWithShadow(sping, width - pingwidth - 20, y + 2, 0xaaaaaa);
			String sPlayers = players + " / "+maxPlayers + " players";
			int playerswidth = font.getStringWidth(sPlayers);
			font.drawStringWithShadow(sPlayers, width - playerswidth - 5, y+11, 0xaaaaaa);
		}
		
		GL11.glColor4f(1f, 1f, 1f, 1f);
		
		//FANCY ICONS!
		int xOffset = 0;
		int yOffset = 0;
		if(polling) {
			xOffset = 1;
			yOffset = (int)(System.currentTimeMillis() / 100L + (long)(pollStart * 2) & 7L);
			if(yOffset > 4) {
				yOffset = 8 - yOffset;
			}
		} else {
			xOffset = 0;
			if(ping < 0L) {
				yOffset = 5;
			} else if(ping < 150L) {
				yOffset = 0;
			} else if(ping < 300L) {
				yOffset = 1;
			} else if(ping < 600L) {
				yOffset = 2;
			} else if(ping < 1000L) {
				yOffset = 3;
			} else {
				yOffset = 4;
			}
		}
		SpoutClient.getHandle().renderEngine.bindTexture(SpoutClient.getHandle().renderEngine.getTexture("/gui/icons.png"));
		RenderUtil.drawTexturedModalRectangle(width - 5 - 10, y + 2, 0 + xOffset * 10, 176 + yOffset * 8, 10, 8, 0f);
		if(port != 25565) {
			font.drawStringWithShadow(ip + ":" +port, x+2, y+20, 0xaaaaaa);
		} else {
			font.drawStringWithShadow(ip, x+2, y+20, 0xaaaaaa);
		}
	}

	public void onClick(int x, int y, boolean doubleClick) {
		if(doubleClick) {
			SpoutClient.getInstance().getServerManager().join(this);
		}
	}
	
	public void poll() {
		if(currentThread != null) {
			currentThread.interrupt();
		}
		pollStart = System.currentTimeMillis();
		boolean wasSandboxed = SpoutClient.isSandboxed();
		if(wasSandboxed) SpoutClient.disableSandbox();
		currentThread = new PollThread();
		currentThread.start();
		if(wasSandboxed) SpoutClient.enableSandbox();
	}
	
	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public int getDatabaseId() {
		return databaseId;
	}

	public void setDatabaseId(int databaseId) {
		this.databaseId = databaseId;
	}
	
	protected class PollThread extends Thread {
		
		@Override
		public void run() {
			while(numPolling >= 2) {
				try {
					sleep(10);
				} catch (InterruptedException e) {}
			}
			numPolling ++;
			polling = true;
			Socket sock = null;
			DataInputStream input = null;
			DataOutputStream output = null;
			try {
				long start = System.currentTimeMillis();
				sock = new Socket();
				sock.setSoTimeout(3000);
				sock.connect(new InetSocketAddress(ip, port), 3000);
				sock.setTcpNoDelay(true);
				sock.setTrafficClass(18);
				
				input = new DataInputStream(sock.getInputStream());
				output = new DataOutputStream(sock.getOutputStream());
				
				//Packet id is 254!
				output.write(254);
				
				//Server will return a packet 255 with the data as string
				if(input.read() != 255) {
					ping = PING_BAD_MESSAGE;
					return;
				}
				
				String sPacket = PacketUtil.readString(input, 256);
				
				long end = System.currentTimeMillis();
				ping = (int) (end - start);
				String split[] = sPacket.split("\u00a7");
				synchronized (motd) {
					motd = split[0];
				}
				players = Integer.valueOf(split[1]);
				maxPlayers = Integer.valueOf(split[2]);
				
			} catch(java.net.UnknownHostException e) {
				ping = PING_UNKNOWN;
			} catch(IOException e) {
				ping = PING_TIMEOUT;
			} catch (Exception e) {
				ping = PING_BAD_MESSAGE;
			} finally {
				polling = false;
				numPolling--;
				try {
					sock.close();
					input.close();
					output.close();
				} catch(Exception e) {}
			}
		}
		
	}
}