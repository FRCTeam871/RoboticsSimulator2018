package me.pieking.game.net.packet;

import java.io.UnsupportedEncodingException;

import me.pieking.game.Game;
import me.pieking.game.Scheduler;
import me.pieking.game.net.ServerStarter;
import me.pieking.game.robot.component.Component;
import me.pieking.game.world.Balance.Team;
import me.pieking.game.world.Player;

public class RequestRobotPacket extends Packet {

	String user;
	
	public RequestRobotPacket(String username) {
		this.user = username;
	}

	@Override
	public String format() {
		return user;
	}

	@Override
	public void doAction() {
		
	}
	
	public void handleRequest(Player pl2) {
		if(Game.isServer()) {
			Player pl = Game.getWorld().getPlayer(user);
			if(pl != null && pl != Game.getWorld().getSelfPlayer()){
				try{
					String decoded = "null";
					try {
						decoded = new String(pl.robot.saveData(), "ISO-8859-1");
					}catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
					
					ShipDataPacket sdp = new ShipDataPacket(pl.name, decoded);
					System.out.println("sending ShipDataPacket for " + pl.name);
					ServerStarter.serverStarter.writePacket(pl2, sdp);
					
					SetTeamPacket stp = new SetTeamPacket(pl.name, pl.team.toString());
					ServerStarter.serverStarter.writePacket(pl2, stp);
					
//					Scheduler.delayedTask(() -> {
//						for(Component c : pl.robot.getComponents()){
//							ShipComponentHealthPacket schp = new ShipComponentHealthPacket(pl.name, c.bounds.x + "", c.bounds.y + "", c.health + "");
//							ServerStarter.serverStarter.writePacket(pl2, schp);
//						}
//					}, 10);
					
					
				}catch(Exception e){
					e.printStackTrace();
				} 
			}
		}
	}
	
}
