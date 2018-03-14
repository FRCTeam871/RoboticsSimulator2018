package me.pieking.game.net.packet;

import java.awt.Color;

import me.pieking.game.Game;
import me.pieking.game.world.Balance.Team;
import me.pieking.game.world.Player;

public class JoinPacket extends Packet {

	String user;
	int x;
	int y;
	Player created = null;
	Color col;
	String version;
	
	boolean noRobotRequest;
	
	public JoinPacket(String username, String x, String y, String version) {
		this.user = username;
		this.x = Integer.parseInt(x);
		this.y = Integer.parseInt(y);
		this.version = version;
	}
	
	public JoinPacket(String username, String x, String y, String version, boolean noRobotRequest) {
		this.user = username;
		this.x = Integer.parseInt(x);
		this.y = Integer.parseInt(y);
		this.version = version;
		
		this.noRobotRequest = noRobotRequest;
	}

	@Override
	public String format() {
		return user + "|" + x + "|" + y + "|" + version;
	}

	@Override
	public void doAction() {
		System.out.println("make2 player " + user);
		if(Game.getWorld().getPlayer(user) == null){
			System.out.println("does not exist");
			Player pl = new Player(user, x, y, Team.RED);
			System.out.println(pl);
			Game.getWorld().addPlayer(pl);
			created = pl;
			
			if(!Game.isServer() && !noRobotRequest) {
				System.out.println("requesting " + created.name);
    			RequestRobotPacket rrp = new RequestRobotPacket(created.name);
    			Game.sendPacket(rrp);
			}
		}
		System.out.println("created = " + created);
	}
	
	public Player getCreated(){
		return created;
	}

	public String getVersion() {
		return version;
	}
	
	public String getUsername() {
		return user;
	}
	
}
