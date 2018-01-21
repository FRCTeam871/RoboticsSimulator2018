package me.pieking.game.ship.component;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;

import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.AABB;
import org.dyn4j.geometry.Mass;
import org.dyn4j.geometry.MassType;
import org.dyn4j.geometry.Rectangle;
import org.dyn4j.geometry.Transform;
import org.dyn4j.geometry.Vector2;

import com.sun.org.apache.bcel.internal.generic.NEWARRAY;
import com.sun.swing.internal.plaf.basic.resources.basic;

import me.pieking.game.Game;
import me.pieking.game.gfx.AnimatedImage;
import me.pieking.game.gfx.Sprite;
import me.pieking.game.gfx.Spritesheet;
import me.pieking.game.world.GameObject;
import me.pieking.game.world.GameObjectFilter;
import me.pieking.game.world.GameObjectFilter.FilterType;
import me.pieking.game.world.Player;
import me.pieking.game.world.PlayerFilter;
import me.pieking.game.world.PowerCube;
import me.pieking.game.world.ScalePlatform;

public class ClawGrabberComponent extends ActivatableComponent {

	public PowerCube dummyCube = new PowerCube(0, 0, 0);
	
	public static Sprite sprOff = Spritesheet.tiles.subTile(0, 14, 2, 1);
	public static Sprite sprOn = Spritesheet.tiles.subTile(2, 14, 2, 1);
	
	public boolean hasCube = false;
	public Player pl;
	
	public boolean closeToPlatform = false;
	
	double holdingAngle;
	
	public ClawGrabberComponent(int x, int y, int rot) {
		super(x, y, 2, 1, rot, 100);
		sprite = sprOff;
	}
	
	@Override
	public GameObject createBody(Player player){
		this.pl = player; 
		
		float xSize = unitSize * 2;
		float ySize = unitSize;
		
		GameObject base = new GameObject();
		base.setAutoSleepingEnabled(false);
		base.color = Color.GRAY;
		
		Rectangle r = new Rectangle(unitSize * 2, unitSize / 8);
		r.translate(unitSize/2, unitSize/2);
		BodyFixture bf = new BodyFixture(r);
		bf.setFilter(new PlayerFilter(pl));
		base.addFixture(bf);
		
		r = new Rectangle(unitSize / 8, unitSize * .75);
		r.translate(-unitSize/2, unitSize / 8);
		bf = new BodyFixture(r);
		bf.setFilter(new PlayerFilter(pl));
		base.addFixture(bf);
		
		r = new Rectangle(unitSize / 8, unitSize * .75);
		r.translate(unitSize/2 + unitSize, unitSize / 8);
		bf = new BodyFixture(r);
		bf.setFilter(new PlayerFilter(pl));
		base.addFixture(bf);
		
		base.setMass(new Mass(base.getMass().getCenter(), 0, 0));
		base.setMass(MassType.NORMAL);
		
		base.setAngularDamping(0);
		base.setLinearDamping(0);
		
		return base;
	}
	
	@Override
	public void tick(Player pl) {
		super.tick(pl);
		
		if(activated){
			
		}else{
			double nearestPlatform = Double.MAX_VALUE;
			
			AABB aabb = lastBody.createAABB();
			
			boolean coll = false;
			
			for(ScalePlatform plat : Game.getWorld().getScalePlatforms()){
				AABB ab2 = plat.base.createAABB();
				ab2.expand(1);
				if(aabb.getIntersection(ab2).getArea() > 0){
					coll = true;
				}
				nearestPlatform = Math.min(plat.base.getWorldCenter().distance(lastBody.getWorldCenter()), nearestPlatform);
			}

			if(closeToPlatform != coll){
				closeToPlatform = coll;
				updateCollision();
			}
			
//			if(closeToPlatform){
//				if(nearestPlatform > 3){
//					closeToPlatform = false;
//					updateCollision();
//				}
//			}else{
//				if(nearestPlatform < 2){
//					closeToPlatform = true;
//					updateCollision();
//				}
//			}
			
		}
		
	}
	
	@Override
	public String getDisplayName() {
		return "Grabber";
	}
	
	@Override
	public void activate() {
		super.activate();
		sprite = sprOn;
		
		for(PowerCube c : Game.getWorld().getCubes()){
			if(!Game.getWorld().isCubeOnScale(c)) {
				if(c.base.getWorldCenter().distance(lastBody.getWorldCenter()) < unitSize * 1.3){
					
					double myAngle = Math.toDegrees(lastBody.getTransform().getRotation());
					
					for(int angleOfs = -360; angleOfs < 360; angleOfs += 90){
    					double cubeAngle = Math.toDegrees(c.base.getTransform().getRotation()) + angleOfs;
    					cubeAngle = cubeAngle % 360;
    					
    					System.out.println(myAngle + " " + cubeAngle);
    					if(Math.abs(myAngle - cubeAngle) <= 10){
    						holdingAngle = angleOfs;
        					Game.getWorld().removeCube(c);
        					setHasCube(true);
        					break;
    					}
					}
				}
			}
		}
		
//		setHasCube(true);
	}
	
	@Override
	public void deactivate() {
		super.deactivate();
		sprite = sprOff;
		
		if(hasCube){
			Transform tra = lastBody.getTransform().copy();
			double r = tra.getRotation() - Math.toRadians(90);
			dummyCube.base.applyForce(new Vector2(unitSize * Math.cos(r), unitSize * Math.sin(r)).multiply(500));
			Game.getWorld().addPowerCube(dummyCube);
			setHasCube(false);
		}
		
		updateCollision();
		
		dummyCube = new PowerCube(0, 0, 0);
		
	}
	
	public void setHasCube(boolean cube){
		this.hasCube = cube;
		updateCollision();
	}
	
	public void updateCollision(){
		lastBody.removeAllFixtures();
		if(hasCube){
			Rectangle r = new Rectangle(unitSize * 2, unitSize / 8);
			r.translate(unitSize/2, unitSize/2);
			BodyFixture bf = new BodyFixture(r);
			bf.setFilter(new PlayerFilter(pl));
			lastBody.addFixture(bf);
			
//			r = new Rectangle(unitSize / 8, unitSize * .75);
//			r.translate(-unitSize/2, unitSize / 8);
//			bf = new BodyFixture(r);
//			bf.setFilter(new PlayerFilter(pl));
//			lastBody.addFixture(bf);
//			
//			r = new Rectangle(unitSize / 8, unitSize * .75);
//			r.translate(unitSize/2 + unitSize, unitSize / 8);
//			bf = new BodyFixture(r);
//			bf.setFilter(new PlayerFilter(pl));
//			lastBody.addFixture(bf);
			
			Rectangle r2 = new Rectangle((unitSize * 2) * 0.8, (unitSize * 2) * 0.8);
			r2.translate(unitSize/2, unitSize/2 - unitSize);
			BodyFixture bf2 = new BodyFixture(r2);
			bf2.setFilter(new GameObjectFilter(FilterType.POWER_CUBE_HOLDING));
			lastBody.addFixture(bf2);
			
		}else{
			Rectangle r = new Rectangle(unitSize * 2, unitSize / 8);
			r.translate(unitSize/2, unitSize/2);
			BodyFixture bf = new BodyFixture(r);
			bf.setFilter(new PlayerFilter(pl));
			lastBody.addFixture(bf);
			
			if(!closeToPlatform){
    			r = new Rectangle(unitSize / 8, unitSize * .75);
    			r.translate(-unitSize/2, unitSize / 8);
    			bf = new BodyFixture(r);
    			bf.setFilter(new PlayerFilter(pl));
    			lastBody.addFixture(bf);
    			
    			r = new Rectangle(unitSize / 8, unitSize * .75);
    			r.translate(unitSize/2 + unitSize, unitSize / 8);
    			bf = new BodyFixture(r);
    			bf.setFilter(new PlayerFilter(pl));
    			lastBody.addFixture(bf);
			}
		}
	}
	
	@Override
	public void render(Graphics2D g) {
		super.render(g);
		
		if(hasCube){
			Transform tra = lastBody.getTransform().copy();
			double r = tra.getRotation() - Math.toRadians(90);
			tra.translate(unitSize * Math.cos(r), unitSize * Math.sin(r));
			dummyCube.base.setTransform(tra);
			dummyCube.base.rotate(-Math.toRadians(holdingAngle), dummyCube.base.getWorldCenter());
			dummyCube.render(g);
		}
		
	}

}
