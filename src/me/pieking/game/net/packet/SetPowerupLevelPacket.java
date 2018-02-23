package me.pieking.game.net.packet;

import me.pieking.game.Game;
import me.pieking.game.world.TeamProperties;
import me.pieking.game.world.Balance.Team;

public class SetPowerupLevelPacket extends Packet {

	String powerup;
	Team team;
	int level;
	
	public SetPowerupLevelPacket(String powerup, String team, String level) {
		this.powerup = powerup;
		this.team = Team.valueOf(team);
		this.level = Integer.parseInt(level);
	}

	@Override
	public String format() {
		return powerup + "|" + team + "|" + level;
	}

	@Override
	public void doAction() {
		TeamProperties prop = Game.getWorld().getProperties(team);
		switch(powerup) {
			case "boost":
				if(prop.getBoostLevel() < level) {
					prop.setBoostLevel(level);
					prop.addScore(5);
				}
				break;
			case "force":
				if(prop.getForceLevel() < level) {
					prop.setForceLevel(level);
					prop.addScore(5);
				}
				break;
			case "levitate":
				if(prop.getLevitateLevel() < level) {
					prop.setLevitateLevel(level);
					prop.addScore(5);
				}
				break;
		}
	}
	
}
