package me.pieking.game.net.packet;

import me.pieking.game.Game;
import me.pieking.game.world.Player;

public class ChoseAutonPacket extends Packet{

	String user;
	
	public ChoseAutonPacket(String username) {
		this.user = username;
	}

	@Override
	public String format() {
		return user;
	}
	
	@Override
	public void doAction() {
		Player p = Game.getWorld().getPlayer(user);
		Game.gameplay.readyToStart(p);
	}
	
}
