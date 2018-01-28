package me.pieking.game.world;

import java.awt.Color;
import java.awt.Graphics2D;

import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.MassType;
import org.dyn4j.geometry.Rectangle;

import me.pieking.game.Game;
import me.pieking.game.Utils;
import me.pieking.game.gfx.LEDStrip;
import me.pieking.game.gfx.LEDStrip.StripMode;
import me.pieking.game.robot.component.Component;

public class Balance {

	protected ScalePlatform red;
	protected ScalePlatform blue;
	protected Team ownership = Team.NONE;
	protected Team ownership_override = Team.NONE;
	
	public GameObject walls;
	
	public Balance(double x, double y, boolean blueTop, LEDStrip ledStripTop, LEDStrip ledStripBottom) {
		red = new ScalePlatform(x-.19, y - 3.5 - .25, 7, 5.5, ledStripTop);
		blue = new ScalePlatform(x-.19, y + 3.5, 7, 5.5, ledStripBottom);
		
		if(blueTop){
			ScalePlatform temp = red;
			red = blue;
			blue = temp;
		}
		
		red.strip.setMode(StripMode.SOLID_RED_FULL);
		blue.strip.setMode(StripMode.SOLID_BLUE_FULL);
		
		x *= GameWorld.FIELD_SCALE;
		y *= GameWorld.FIELD_SCALE;
		
		walls = new GameObject();
//		walls.type = BodyType.DEFAULT;
		walls.color = Color.GREEN;
		
		Rectangle r = new Rectangle(Component.unitSize * (red.sizeW + 0.9), Component.unitSize * (red.sizeH*2));
		r.translate(Component.unitSize * (red.sizeW/2 - 0.58), Component.unitSize * (red.sizeH/2 - 0.5));
		BodyFixture bf = new BodyFixture(r);
//		bf.setFilter(new GameObjectFilter(FilterType.DEFAULT));
		walls.setMass(MassType.INFINITE);
		walls.addFixture(bf);
		
		walls.setAngularDamping(GameWorld.getAngularDamping());
		walls.setLinearDamping(GameWorld.getLinearDamping());
		walls.translate(x, y);
		
	}
	
	public void render(Graphics2D g) {
		red.render(g);
		blue.render(g);
	}

	public void tick() {
		blue.tick();
		red.tick();
		
		if(red.numCubes() == blue.numCubes()) {
			ownership = Team.NONE;
			red.strip.setMode(StripMode.SOLID_RED_FULL);
			blue.strip.setMode(StripMode.FILL_BLUE);
		}else if(red.numCubes() > blue.numCubes()) {
			ownership = Team.RED;
			red.strip.setMode(StripMode.PULSE_RED);
			blue.strip.setMode(StripMode.SOLID_BLUE_WEAK);
		}else {
			ownership = Team.BLUE;
			red.strip.setMode(StripMode.SOLID_RED_WEAK);
			blue.strip.setMode(StripMode.PULSE_BLUE);
		}
		
		if(Game.getWorld().getBoost().getUsing() != Team.NONE) {
    		int boostLevel = Game.getWorld().getBoost().getLevel();
    		System.out.println(boostLevel);
    		boolean boostL = false;
    		if(this instanceof Scale) {
    			if(boostLevel >= 2) boostL = true;
    		}else if(this == Game.getWorld().getProperties(Game.getWorld().getBoost().getUsing()).getSwitch()) {
    			if(boostLevel % 2 == 1) boostL = true;
    		}
    		
    		if(boostL) {
        		if(Game.getWorld().getBoost().getUsing() == Team.RED){
        			red.strip.setMode(StripMode.CHASE_RED);
        		}else if(Game.getWorld().getBoost().getUsing() == Team.BLUE){
        			blue.strip.setMode(StripMode.CHASE_BLUE);
        		}
    		}
		}
		
		if(Game.getWorld().getForce().getUsing() != Team.NONE) {
    		int forceLevel = Game.getWorld().getForce().getLevel();
    		System.out.println(forceLevel);
    		boolean forceL = false;
    		if(this instanceof Scale) {
    			if(forceLevel >= 2) forceL = true;
    		}else if(this == Game.getWorld().getProperties(Game.getWorld().getForce().getUsing()).getSwitch()) {
    			if(forceLevel % 2 == 1) forceL = true;
    		}
    		
    		if(forceL) {
        		if(Game.getWorld().getForce().getUsing() == Team.RED){
        			red.strip.setMode(StripMode.PULSE_RED_BLUE_CORNERS);
        		}else if(Game.getWorld().getForce().getUsing() == Team.BLUE){
        			blue.strip.setMode(StripMode.PULSE_BLUE_RED_CORNERS);
        		}
    		}
		}
		
		red.base.color = Utils.fade(Color.GRAY, Team.RED.color, 0.2f);
		blue.base.color = Utils.fade(Color.GRAY, Team.BLUE.color, 0.2f);
		
		if(getOwner() == Team.RED) red.base.color = Team.RED.color;
		if(getOwner() == Team.BLUE) blue.base.color = Team.BLUE.color;
	}

	public ScalePlatform getRedPlatform() {
		return red;
	}
	
	public ScalePlatform getBluePlatform() {
		return blue;
	}
	
	public Team getOwner() {
		return ownership_override != Team.NONE ? ownership_override : ownership;
	}
	
	public void setOwnerOverride(Team team){
		ownership_override = team;
	}
	
	public Team getTopTeam() {
		return blue.y > red.y ? Team.RED : Team.BLUE;
	}
	
	public static enum Team {
		NONE (Color.GRAY),
		BLUE (Color.decode("#0066b3")),
		RED  (Color.decode("#ed1c24"));
		
		public final Color color;
		
		private Team(Color col) {
			color = col;
		}
		
		public Team getOpposite() {
			return this == BLUE ? RED : (this == RED ? BLUE : NONE);
		}
		
	}
	
}
