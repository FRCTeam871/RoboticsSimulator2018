package me.pieking.game.net.packet;

import me.pieking.game.Game;
import me.pieking.game.world.Balance.Team;

public class UsePowerupPacket extends Packet {

	String powerup;
	Team team;
	
	public UsePowerupPacket(String powerup, String team) {
		this.powerup = powerup;
		this.team = Team.valueOf(team);
	}

	@Override
	public String format() {
		return powerup + "|" + team;
	}

	@Override
	public void doAction() {
		switch(powerup) {
			case "boost":
				Game.getWorld().useBoost(team);
				break;
			case "force":
				Game.getWorld().useForce(team);
				break;
			case "levitate":
				Game.getWorld().useLevitate(team);
				break;
		}
	}
	
}
