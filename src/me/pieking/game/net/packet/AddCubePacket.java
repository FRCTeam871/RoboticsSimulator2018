package me.pieking.game.net.packet;

import me.pieking.game.Game;
import me.pieking.game.world.PowerCube;

public class AddCubePacket extends Packet {

	int id;
	float x;
	float y;
	PowerCube created = null;
	
	public AddCubePacket(String id, String x, String y) {
		this.id = Integer.parseInt(id);
		this.x = Float.parseFloat(x);
		this.y = Float.parseFloat(y);
	}

	@Override
	public String format() {
		return id + "|" + x + "|" + y;
	}

	@Override
	public void doAction() {
		if(Game.getWorld().getPowerCube(id) == null){
			PowerCube pc = new PowerCube(x, y, 0, id);
			Game.getWorld().addPowerCube(pc);
			created = pc;
		}
	}
	
	public PowerCube getCreated(){
		return created;
	}
	
}
