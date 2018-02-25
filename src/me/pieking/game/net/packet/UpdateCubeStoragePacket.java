package me.pieking.game.net.packet;

import me.pieking.game.Game;
import me.pieking.game.world.Balance.Team;

public class UpdateCubeStoragePacket extends Packet {

	int redCubes;
	int blueCubes;
	
	public UpdateCubeStoragePacket(String redCubes, String blueCubes) {
		this.redCubes = Integer.parseInt(redCubes);
		this.blueCubes = Integer.parseInt(blueCubes);
	}

	@Override
	public String format() {
		return redCubes + "|" + blueCubes;
	}

	@Override
	public void doAction() {
//		System.out.println(redCubes + " " + blueCubes);
		Game.getWorld().getProperties(Team.RED).setCubeStorage(redCubes);
		Game.getWorld().getProperties(Team.BLUE).setCubeStorage(blueCubes);
	}
	
}
