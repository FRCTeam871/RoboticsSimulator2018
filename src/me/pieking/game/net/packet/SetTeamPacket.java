package me.pieking.game.net.packet;

import me.pieking.game.Game;
import me.pieking.game.world.Balance.Team;

public class SetTeamPacket extends Packet {

	String user;
	Team team;
	
	public SetTeamPacket(String username, String team) {
		this.user = username;
		this.team = Team.valueOf(team);
	}

	@Override
	public String format() {
		return user + "|" + team;
	}

	@Override
	public void doAction() {
		System.out.println("make2 player " + user);
		if(Game.getWorld().getPlayer(user) != null){
			Game.getWorld().getPlayer(user).team = team;
		}
	}
	
}
