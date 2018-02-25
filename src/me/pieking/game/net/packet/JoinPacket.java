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
	
	public JoinPacket(String username, String x, String y, String version) {
		this.user = username;
		this.x = Integer.parseInt(x);
		this.y = Integer.parseInt(y);
		this.version = version;
	}

	@Override
	public String format() {
		return user + "|" + x + "|" + y + "|" + version;
	}

	@Override
	public void doAction() {
		System.out.println("make2 player " + user);
		if(Game.getWorld().getPlayer(user) == null){
			Player pl = new Player(user, x, y, Team.RED);
			System.out.println(pl);
			Game.getWorld().addPlayer(pl);
			created = pl;
		}
		System.out.println("created = " + created);
	}
	
	public Player getCreated(){
		return created;
	}

	public String getVersion() {
		return version;
	}
	
}
