package me.pieking.game.net.packet;

import me.pieking.game.Game;

public class SetGameTimePacket extends Packet {

	int time;
	
	public SetGameTimePacket(String time) {
		this.time = Integer.parseInt(time);
	}

	@Override
	public String format() {
		return time + "";
	}

	@Override
	public void doAction() {
		Game.gameplay.setGameTime(time);
	}
	
}
