package me.pieking.game.net.packet;

import me.pieking.game.Game;
import me.pieking.game.world.Player;

public class PlayerUpdatePacket extends Packet {

	String user;
	float x;
	float y;
	float xa;
	float ya;
	float rot;
	float rotA;
	
	public PlayerUpdatePacket(String username, String x, String y, String xa, String ya, String rot, String rotA) {
		this.user = username;
		this.x = Float.parseFloat(x);
		this.y = Float.parseFloat(y);
		this.xa = Float.parseFloat(xa);
		this.ya = Float.parseFloat(ya);
		this.rot = Float.parseFloat(rot);
		this.rotA = Float.parseFloat(rotA);
		
		
		if(Math.abs(this.xa) < 0.01) this.xa = 0;
		if(Math.abs(this.ya) < 0.01) this.ya = 0;
		if(Math.abs(this.rotA) < 0.01) this.rotA = 0;
	}

	@Override
	public String format() {
		return user + "|" + x + "|" + y + "|" + xa + "|" + ya + "|" + rot + "|" + rotA;
	}

	@Override
	public void doAction() {
		Player p = Game.getWorld().getPlayer(user);
//		System.out.println(p);
		if(p != null){
			
//			float rot = Game.getTime() / 60f;
			
			p.translateToOrigin();
			p.setRotation(rot);
			p.translateToOrigin();
			p.translate(x, y);
			
//			tr.rotate(rot, p.base.getWorldCenter());
//			p.base.translateToOrigin();
//			p.base.rotateAboutCenter(rot);
////			p.base.translate(x, y);
////			p.base.setTransform(new Transform());
////			p.base.getTransform().setRotation(rot);
//			p.base.translate(x, y);
//			System.out.println(p.base.getWorldCenter());
			p.setActiveLinearVelocity(xa, ya);
			p.setActiveAngularVelocity(rotA);
//			p.base.setLinearVelocity(xa, ya);
//			p.base.setAngularVelocity(rotA);
			
//			p.constructShip();
			
//			System.out.println(rotA);
		}
	}
	
}
