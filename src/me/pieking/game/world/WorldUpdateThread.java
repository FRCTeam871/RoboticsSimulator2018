package me.pieking.game.world;

import org.dyn4j.dynamics.World;

import me.pieking.game.Game;

public class WorldUpdateThread extends Thread {

	private World world;
	private int updates = 0;
	private boolean kill = false;
	
	public WorldUpdateThread(World world) {
		this.world = world;
	}
	
	@Override
	public void run() {
		super.run();
		
		while(!kill) {
    		if(updates > 0) {
        		// update the physics world
        		try{
        			world.update(1d/60d);
        		}catch(Exception e){
        			e.printStackTrace();
        		}
        		
        		updates = 0;
    		}
    		
    		try {
    			sleep(5);
    		} catch (InterruptedException e) {}
		}
		
	}
	
	public void queueUpdate() {
		if(Game.isServer()) {
			updates++;
		}else {
			try{
				world.update(1d/60d);
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}
	
	public void kill() {
		this.kill = true;
	}
	
}
