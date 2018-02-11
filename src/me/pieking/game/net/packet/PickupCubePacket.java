package me.pieking.game.net.packet;

import me.pieking.game.Game;
import me.pieking.game.robot.component.ClawGrabberComponent;
import me.pieking.game.robot.component.Component;
import me.pieking.game.world.Player;
import me.pieking.game.world.PowerCube;

public class PickupCubePacket extends Packet {

	int id;
	String username;
	
	public PickupCubePacket(String id, String username) {
		this.id = Integer.parseInt(id);
		this.username = username;
	}

	@Override
	public String format() {
		return id + "|" + username;
	}

	@Override
	public void doAction() {
		Player p = Game.getWorld().getPlayer(username);
		if(p != null){
			PowerCube c = Game.getWorld().getPowerCube(id);
			if(c != null){
//				Game.getWorld().removeCube(c);
				for(Component co : p.getRobot().getComponents()) {
					if(co instanceof ClawGrabberComponent) {
						((ClawGrabberComponent) co).grabCube(c);
					}
				}
			}
		}
	}
}
