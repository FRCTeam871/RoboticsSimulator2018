package me.pieking.game.net.packet;

import me.pieking.game.Game;
import me.pieking.game.robot.component.ClawGrabberComponent;
import me.pieking.game.robot.component.Component;
import me.pieking.game.world.Player;
import me.pieking.game.world.PowerCube;

public class DropCubePacket extends Packet {

	String username;
	
	public DropCubePacket(String username) {
		this.username = username;
	}

	@Override
	public String format() {
		return username;
	}

	@Override
	public void doAction() {
		Player p = Game.getWorld().getPlayer(username);
		if(p != null){
			for(Component co : p.getRobot().getComponents()) {
				if(co instanceof ClawGrabberComponent) {
					((ClawGrabberComponent) co).setHasCube(false);
				}
			}
		}
	}
}
