package me.pieking.game.world;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.util.List;

import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.MassType;
import org.dyn4j.geometry.Rectangle;

import me.pieking.game.Game;
import me.pieking.game.Location;
import me.pieking.game.Vars;
import me.pieking.game.gfx.LEDStrip;
import me.pieking.game.gfx.Sprite;
import me.pieking.game.gfx.Spritesheet;
import me.pieking.game.robot.component.Component;
import me.pieking.game.world.GameObject.BodyType;
import me.pieking.game.world.GameObjectFilter.FilterType;

public class ScalePlatform {

	private static final boolean CIRCLE_LEDS = true;

	public static Sprite spr = Spritesheet.tiles.subTile(0, 15, 2, 2);

	public GameObject base;
	
	double sizeW = 6.2;
	double sizeH = 5;
	
	LEDStrip strip;
	
	double x;
	double y;
	
	double ledYofs = 0;
	double ledXofs = 0;
	
	public ScalePlatform(double x, double y, double sizeW, double sizeH, LEDStrip ledStrip, boolean scale) {
		
		this.x = x;
		this.y = y;
		
		this.strip = ledStrip;
		
		this.sizeW = sizeW;
		this.sizeH = sizeH;
		
		x *= GameWorld.FIELD_SCALE;
		y *= GameWorld.FIELD_SCALE;
		
		sizeW *= GameWorld.FIELD_SCALE;
		sizeH *= GameWorld.FIELD_SCALE;
		
		base = new GameObject();
		base.type = BodyType.SCALE_PLATFORM;
		base.setAutoSleepingEnabled(false);
		base.color = Color.GREEN;
		
		Rectangle r = new Rectangle(Component.unitSize * sizeW, Component.unitSize * sizeH);
		r.translate(Component.unitSize * (sizeW/2 - 0.5), Component.unitSize * (sizeH/2 - 0.5));
		BodyFixture bf = new BodyFixture(r);
		bf.setFilter(new GameObjectFilter(scale ? FilterType.SCALE_PLATFORM_CENTER : FilterType.SWITCH_PLATFORM_CENTER));
		base.setMass(MassType.INFINITE);
		base.addFixture(bf);
		
		r = new Rectangle(Component.unitSize * sizeW, Component.unitSize * 0.25);
		r.translate(Component.unitSize * (sizeW/2 - 0.5), Component.unitSize * (sizeH/2 - 0.5) - (Component.unitSize * sizeH) + (Component.unitSize * (sizeH/2 - 0.5)) + Component.unitSize*0.625);
		bf = new BodyFixture(r);
		bf.setFilter(new GameObjectFilter(scale ? FilterType.SCALE_PLATFORM : FilterType.SWITCH_PLATFORM));
		base.setMass(MassType.INFINITE);
		base.addFixture(bf);
		
		r = new Rectangle(Component.unitSize * sizeW, Component.unitSize * 0.25);
		r.translate(Component.unitSize * (sizeW/2 - 0.5), Component.unitSize * (sizeH/2 - 0.5) + (Component.unitSize * sizeH) - (Component.unitSize * (sizeH/2 - 0.5)) - Component.unitSize*0.625);
		bf = new BodyFixture(r);
		bf.setFilter(new GameObjectFilter(scale ? FilterType.SCALE_PLATFORM : FilterType.SWITCH_PLATFORM));
		base.setMass(MassType.INFINITE);
		base.addFixture(bf);
		
		r = new Rectangle(Component.unitSize * 0.25, Component.unitSize * sizeH);
		r.translate(Component.unitSize * (sizeW/2 - 0.5) - (Component.unitSize * sizeW/2), Component.unitSize * (sizeH/2 - 0.5) + (Component.unitSize * sizeH) - (Component.unitSize * (sizeH/2 - 0.5)) - Component.unitSize*0.625 - (Component.unitSize * sizeH/2) + (Component.unitSize * 0.25/2));
//		r.rotate(Math.toRadians(90), r2.getCenter());
		bf = new BodyFixture(r);
		bf.setFilter(new GameObjectFilter(scale ? FilterType.SCALE_PLATFORM : FilterType.SWITCH_PLATFORM));
		base.setMass(MassType.INFINITE);
		base.addFixture(bf);
		
		r = new Rectangle(Component.unitSize * 0.25, Component.unitSize * sizeH);
		r.translate(Component.unitSize * (sizeW/2 - 0.5) + (Component.unitSize * sizeW/2), Component.unitSize * (sizeH/2 - 0.5) + (Component.unitSize * sizeH) - (Component.unitSize * (sizeH/2 - 0.5)) - Component.unitSize*0.625 - (Component.unitSize * sizeH/2) + (Component.unitSize * 0.25/2));
//		r.rotate(Math.toRadians(-90), r2.getCenter());
		bf = new BodyFixture(r);
		bf.setFilter(new GameObjectFilter(scale ? FilterType.SCALE_PLATFORM : FilterType.SWITCH_PLATFORM));
		base.setMass(MassType.INFINITE);
		base.addFixture(bf);
		
		base.setAngularDamping(GameWorld.getAngularDamping());
		base.setLinearDamping(GameWorld.getLinearDamping());
		base.translate(x, y);
	}

	public ScalePlatform(double x, double y, LEDStrip ledStrip, boolean scale) {
		this(x, y, 6.2, 5, ledStrip, scale);
	}
	
	public int numCubes(){
		int ct = 0;
		List<PowerCube> cub = Game.getWorld().getCubes();
		for(PowerCube p : cub) {
			if(p.holding) continue; // don't count the cube if it's being held
			if(base.contains(p.base.getWorldCenter())) {
				ct++;
			}
		}
		
		return ct;
	}
	
	public void tick(){
		strip.tick();
	}

	public Location getLocation() {
		return new Location((float)base.getWorldCenter().x, (float) base.getWorldCenter().y);
	}
	
	public void render(Graphics2D g){
		
//		if(Game.getTime() % 30 >= 15){
//			base.render(g, new BasicStroke(1f), spr, Component.unitSize * sizeW * 2, Component.unitSize * sizeH * 2);
//		}else{
//			base.render(g);
//		}
		
		if(Vars.showCollision) base.render(g);

		AffineTransform ot = g.getTransform();
		
		g.translate(base.getWorldCenter().x * GameObject.SCALE, base.getWorldCenter().y * GameObject.SCALE);
		g.translate(-Component.unitSize * (sizeW/2) * GameObject.SCALE, -Component.unitSize * (sizeH/2) * GameObject.SCALE);
		
		AffineTransform tr = g.getTransform();
		
		int numPixels = strip.length();
		for(int i = 0; i < numPixels; i++){
			g.setTransform(tr);
			
			int x = 0;
			int y = 0;
			
			if(i < 7){
				x = 7 - i;
			}else if(i < 19){
				y = i - 7;
			}else if(i < 25){
				x = i - 18;
				y = 11;
			}else if(i < 32){
				x = 8 + i - 25;
			}else if(i < 43){
				x = 14;
				y = i - 31;
			}else if(i < 50){
				x = 13 - (i - 43);
				y = 11;
			}else{
				continue;
			}
			
    		g.translate(Component.unitSize*0.5 * GameObject.SCALE * (x + ledXofs), Component.unitSize*0.48 * GameObject.SCALE * (y + ledYofs + 0.5));
    		Rectangle2D r2d = new Rectangle2D.Double(-.06 * GameObject.SCALE, -.06 * GameObject.SCALE, 0.12 * GameObject.SCALE * 1.5, 0.12 * GameObject.SCALE * 1.5);
    		Ellipse2D e2d = new Ellipse2D.Double(-.06 * GameObject.SCALE, -.06 * GameObject.SCALE, 0.12 * GameObject.SCALE * 1.5, 0.12 * GameObject.SCALE * 1.5);
    		g.setColor(strip.getColor(i));
    		if(CIRCLE_LEDS){
    			g.fill(e2d);
    		}else{
    			g.fill(r2d);
    		}
		}
		
		g.setTransform(ot);
	}
	
}
