package me.pieking.game.net.packet;

import me.pieking.game.Game;

public class SwitchOrientationPacket extends Packet {

	boolean[] orientation = new boolean[3];
	
	public SwitchOrientationPacket(String switchR, String scale, String switchB) {
		orientation[0] = Boolean.parseBoolean(switchR);
		orientation[1] = Boolean.parseBoolean(scale);
		orientation[2] = Boolean.parseBoolean(switchB);
	}

	@Override
	public String format() {
		return orientation[0] + "|" + orientation[1] + "|" + orientation[2];
	}

	@Override
	public void doAction() {
		Game.getWorld().setSwitchOrientation(orientation);
	}
	
}
