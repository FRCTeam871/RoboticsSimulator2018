package me.pieking.game.net;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.JOptionPane;

import com.jmr.wrapper.common.Connection;
import com.jmr.wrapper.common.exceptions.NNCantStartServer;
import com.jmr.wrapper.server.Server;

import me.pieking.game.Game;
import me.pieking.game.Scheduler;
import me.pieking.game.Settings;
import me.pieking.game.Logger.ExitState;
import me.pieking.game.net.packet.JoinPacket;
import me.pieking.game.net.packet.KickPacket;
import me.pieking.game.net.packet.LeavePacket;
import me.pieking.game.net.packet.Packet;
import me.pieking.game.net.packet.SetTeamPacket;
import me.pieking.game.net.packet.ShipComponentHealthPacket;
import me.pieking.game.net.packet.ShipDataPacket;
import me.pieking.game.net.packet.UpdateSettingsPacket;
import me.pieking.game.robot.component.Component;
import me.pieking.game.world.Player;
import me.pieking.game.world.Balance.Team;

public class ServerStarter {

	public static final int DEFAULT_PORT = 50001;
	
	public static ServerStarter serverStarter;
	
	private Server server;
	
	public HashMap<Connection, Player> connections = new HashMap<>();
	
	public List<Connection> awaitingInitialData = new ArrayList<Connection>();

	public static boolean isServer = false;
	
	private ServerStarter() {
		try {
			server = new Server(DEFAULT_PORT, DEFAULT_PORT);
			server.setListener(new ServerListener(this));
			if (server.isConnected()) {
				System.out.println("Started server successfully.");
				System.out.println(server.getUdpSocket().isConnected() + " " + server.getUdpPort());
			}
		} catch (NNCantStartServer e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "Could not start server:\n" + e.getClass().getSimpleName() + ": " + e.getMessage());
			Game.stop(ExitState.SERVER_RUN_ERROR.code);
		}
	}
	
	public static void main(String[] args) {
		isServer = true;
		new Thread(() -> Game.runGame(args)).start();
		serverStarter = new ServerStarter();
	}
	
	public Server getServer(){
		return server;
	}

	public void removeConnection(Connection con) {
		Game.getWorld().removePlayer(connections.get(con));
		connections.remove(con);
	}
	
	public void recieve(String msg, Connection from) throws Exception {
		String[] args = msg.split("\\|");
		
		//System.out.println(msg);
//		System.out.println("server got " + msg);
		
		String className = args[0];
		Class<?> clazz = Class.forName("me.pieking.game.net.packet." + className);
		
		String[] otherArgs = new String[args.length-1];
		for(int i = 1; i < args.length; i++) otherArgs[i-1] = args[i];
		
		Class<?>[] types = new Class<?>[otherArgs.length];
		for(int i = 0; i < types.length; i++) types[i] = otherArgs[i].getClass();
		
		//System.out.println(types);
		
		Packet p = (Packet) clazz.getConstructor(types).newInstance((Object[]) otherArgs);
		
		if(p instanceof JoinPacket){
			JoinPacket jp = (JoinPacket)p;
			
			boolean correctVersion = checkVersion(jp.getVersion());
			
			if(!correctVersion) {
				KickPacket kp = new KickPacket("Incorrect version. The server requires \"" + Game.getVersion() + "\" but you have \"" + jp.getVersion() + "\".");
				writePacket(from, kp);
				return;
			}
			
			p.doAction();
			
			Player pl = jp.getCreated();
			if(pl != null) {
				connections.put(from, pl);
				awaitingInitialData.remove(from);
				pl.team = connections.size() % 2 == 0 ? Team.RED : Team.BLUE;
				SetTeamPacket stp = new SetTeamPacket(pl.name, pl.team.toString());
				sendToAll(stp);
				
				UpdateSettingsPacket usp = new UpdateSettingsPacket(Settings.save());
				writePacket(from, usp);
			}
			
//			if(Game.startGameTimer == -1 && Game.getWorld().getPlayers().size() > 1){
//				Game.startGameTimer = 60 * 10;
//			}
			
			for(Player pl2 : Game.getWorld().getPlayers()){
				if(pl2 != pl){
					JoinPacket jp2 = new JoinPacket(pl2.name, pl2.getLocation().getX()+"", pl2.getLocation().getY()+"", Game.getVersion());
					writePacket(from, jp2);
					
					Scheduler.delayedTask(() -> {
						try{
    						String decoded = "null";
    						try {
    							decoded = new String(pl2.robot.saveData(), "ISO-8859-1");
    						}catch (UnsupportedEncodingException e) {
    							e.printStackTrace();
    						}
    					    
    						ShipDataPacket sdp = new ShipDataPacket(pl2.name, decoded);
    						writePacket(from, sdp);
    						
    						SetTeamPacket stp = new SetTeamPacket(pl2.name, pl2.team.toString());
    						writePacket(from, stp);
    						
    						Scheduler.delayedTask(() -> {
    							for(Component c : pl2.robot.getComponents()){
        							ShipComponentHealthPacket schp = new ShipComponentHealthPacket(pl2.name, c.bounds.x + "", c.bounds.y + "", c.health + "");
        							writePacket(from, schp);
        						}
    						}, 120);
    						
    						
						}catch(Exception e){
							e.printStackTrace();
						} 
					}, 120);
					
				}
			}
			
			//pl.respawn();
			
		}else if(p instanceof LeavePacket){
			p.doAction();
			from.close();
		}else {
			Game.queuePacket(p);
		}
		
//		if(p instanceof SetLocationPacket){
//			SetLocationPacket pa = (SetLocationPacket) p;
//			if(pa.format().startsWith("Player1")) {
//				//System.out.println(clients.size());
//				//System.out.println(from.pl.getName());
//				//System.out.println(msg);
//				//System.out.println(pa.format());
//			}
//		}
		
		sendToAllBut(from, p);
	}

	private boolean checkVersion(String version) {
		return version.equals(Game.getVersion());
	}

	private void writePacket(Connection from, Packet p) {
		String className = p.getClass().getSimpleName();
		
		from.sendTcp(className + "|" + p.format());
	}

	public void sendToAllBut(Connection from, Packet p) {
		List<Connection> cn = new ArrayList<Connection>();
		cn.addAll(connections.keySet());
		for(Connection handl : cn){
			if(handl != from) writePacket(handl, p);
		}
	}
	
	public void sendToAll(Packet p) {
		for(Connection handl : connections.keySet()){
			writePacket(handl, p);
		}
	}
	
}