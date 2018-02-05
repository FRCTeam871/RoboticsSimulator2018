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
//		System.out.println(p);
		if(c != null){
			
//			float rot = Game.getTime() / 60f;
			
			c.base.translateToOrigin();
			c.setRotation(rot);
			c.base.translateToOrigin();
			c.base.translate(x, y);
			
//			tr.rotate(rot, p.base.getWorldCenter());
//			p.base.translateToOrigin();
//			p.base.rotateAboutCenter(rot);
////			p.base.translate(x, y);
////			p.base.setTransform(new Transform());
////			p.base.getTransform().setRotation(rot);
//			p.base.translate(x, y);
//			System.out.println(p.base.getWorldCenter());
			c.base.setLinearVelocity(xa, ya);
			c.base.setAngularVelocity(rotA);
			
//			p.constructShip();
			
//			System.out.println(rotA);
		}
	}
	
}
