package me.pieking.game.net.packet;

import org.dyn4j.geometry.Vector2;

import me.pieking.game.Game;
import me.pieking.game.world.PowerCube;
import me.pieking.game.world.Balance.Team;

public class PortalEjectPacket extends Packet {

	Team team;
	int pos;
	
	public PortalEjectPacket(String team, String position) {
		this.team = Team.valueOf(team);
		this.pos = Integer.parseInt(position);
	}

	@Override
	public String format() {
		return team.toString() + "|" + pos;
	}

	@Override
	public void doAction() {
		if(Game.isServer() || !Game.isConnected()) {
			if(team == Team.RED) {
        		if(pos == 0) {
        			PowerCube portalCube1 = new PowerCube(49.1, 21.4, 0);
        			portalCube1.base.rotateAboutCenter(Math.toRadians(-45));
        			Game.getWorld().addPowerCube(portalCube1);
        		}else if(pos == 1) {
        			PowerCube exchangeCube = new PowerCube(8, 10, 0);
        			exchangeCube.base.rotateAboutCenter(Math.toRadians(90));
        			exchangeCube.base.applyForce(new Vector2(500, 0));
        			Game.getWorld().addPowerCube(exchangeCube);
        		}else if(pos == 2) {
        			PowerCube portalCube2 = new PowerCube(49.1, 2.2, 0);
        			portalCube2.base.rotateAboutCenter(Math.toRadians(45 + 180));
        			Game.getWorld().addPowerCube(portalCube2);
        		}
			}else if(team == Team.BLUE) {
				if(pos == 0) {
        			PowerCube portalCube1 = new PowerCube(9.2, 21.4, 0);
        			portalCube1.base.rotateAboutCenter(Math.toRadians(45));
        			Game.getWorld().addPowerCube(portalCube1);
        		}else if(pos == 1) {
        			PowerCube exchangeCube = new PowerCube(50.3, 13.8, 0);
        			exchangeCube.base.rotateAboutCenter(Math.toRadians(-90));
        			exchangeCube.base.applyForce(new Vector2(-500, 0));
        			Game.getWorld().addPowerCube(exchangeCube);
        		}else if(pos == 2) {
        			PowerCube portalCube2 = new PowerCube(9.2, 2.2, 0);
        			portalCube2.base.rotateAboutCenter(Math.toRadians(-45 + 180));
        			Game.getWorld().addPowerCube(portalCube2);
        		}
			}
		}
	}
	
}
