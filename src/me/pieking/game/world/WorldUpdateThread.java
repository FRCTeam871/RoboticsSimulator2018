package me.pieking.game.world;

import org.dyn4j.dynamics.World;

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
    			sleep(100);
    		} catch (InterruptedException e) {}
		}
		
	}
	
	public void queueUpdate() {
//		updates++;
		try{
			world.update(1d/60d);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public void kill() {
		this.kill = true;
	}
	
}
