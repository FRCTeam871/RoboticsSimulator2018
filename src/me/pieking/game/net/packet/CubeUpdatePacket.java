package me.pieking.game.net.packet;

import me.pieking.game.Game;
import me.pieking.game.world.Player;
import me.pieking.game.world.PowerCube;

public class CubeUpdatePacket extends Packet {

	int id;
	float x;
	float y;
	float xa;
	float ya;
	float rot;
	float rotA;
	
	public CubeUpdatePacket(String id, String x, String y, String xa, String ya, String rot, String rotA) {
		this.id = Integer.parseInt(id);
		this.x = Float.parseFloat(x);
		this.y = Float.parseFloat(y);
		this.xa = Float.parseFloat(xa);
		this.ya = Float.parseFloat(ya);
		this.rot = Float.parseFloat(rot);
		this.rotA = Float.parseFloat(rotA);
	}

	@Override
	public String format() {
		return id + "|" + x + "|" + y + "|" + xa + "|" + ya + "|" + rot + "|" + rotA;
	}

	@Override
	public void doAction() {
		PowerCube c = Game.getWorld().getPowerCube(id);
		
		if(c == null) {
			PowerCube pc = new PowerCube(x, y, 0, id);
			Game.getWorld().addPowerCube(pc);
			c = pc;
		}
//		System.out.println(p);
		if(c != null){
			
			c.lastUpdate = System.currentTimeMillis();
			if(c.holding) return;
			
//			float rot = Game.getTime() / 60f;
			
			c.base.translateToOrigin();
			c.setRotation(rot);
			c.base.translateToOrigin();
			c.base.translate(x, y);
			c.base.setLinearVelocity(xa, ya);
			c.base.setAngularVelocity(rotA);
			
//			p.constructShip();
			
//			System.out.println(rotA);
		}
	}
	
}
