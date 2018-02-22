package me.pieking.game.net.packet;

import me.pieking.game.Game;
import me.pieking.game.world.Balance.Team;

public class UpdateScorePacket extends Packet {

	int redScore;
	int blueScore;
	
	public UpdateScorePacket(String redScore, String blueScore) {
		this.redScore = Integer.parseInt(redScore);
		this.blueScore = Integer.parseInt(blueScore);
	}

	@Override
	public String format() {
		return redScore + "|" + blueScore;
	}

	@Override
	public void doAction() {
		System.out.println(redScore + " " + blueScore);
		Game.getWorld().getProperties(Team.RED).setScore(redScore);
		Game.getWorld().getProperties(Team.BLUE).setScore(blueScore);
	}
	
}
