package me.pieking.game.net.packet;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;

import me.pieking.game.Game;
import me.pieking.game.robot.Robot;
import me.pieking.game.world.Player;

public class ShipDataPacket extends Packet {

	String user;
	String data;
	
	public ShipDataPacket(String username, String data) throws UnsupportedEncodingException {
		this.user = username;
		this.data = data;
	}

	@Override
	public String format() {
		return user + "|" + data;
	}

	@Override
	public void doAction() {
		System.out.println(Game.getWorld().getPlayer(user));
		
		long start = System.currentTimeMillis();
		while(Game.getWorld().getPlayer(user) == null && System.currentTimeMillis() - start < 1000) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {}
		}
		
		if(Game.getWorld().getPlayer(user) != null){
			Player pl = Game.getWorld().getPlayer(user);
			
			System.out.println("makeShip for " + user);
			
			try {
				pl.loadShip(Robot.loadData(data, pl));
			}catch (ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | IOException e) {
				e.printStackTrace();
			}
			
		}
	}
	
}
