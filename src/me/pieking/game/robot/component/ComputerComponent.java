package me.pieking.game.robot.component;

import java.awt.Color;
import java.awt.Point;

import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.Mass;
import org.dyn4j.geometry.MassType;

import me.pieking.game.gfx.AnimatedImage;
import me.pieking.game.gfx.Sprite;
import me.pieking.game.gfx.Spritesheet;
import me.pieking.game.scripting.LuaScript;
import me.pieking.game.world.GameObject;
import me.pieking.game.world.Player;
import me.pieking.game.world.PlayerFilter;

public class ComputerComponent extends ActivatableComponent {

	public static Sprite sprOff = Spritesheet.tiles.subTile(0, 0);
	public static AnimatedImage sprOn;
	static {
		Point[] onPts = new Point[12];
		for(int i = 0; i < onPts.length; i++){
			onPts[i] = new Point(i + 1, 0);
		}
		sprOn = Spritesheet.tiles.animatedTile(onPts);
		sprOn.speed = 5f;
	}
	
	public LuaScript script;
	
	public ComputerComponent(int x, int y, int rot) {
		super(x, y, 1, 1, rot, 100);
		sprite = sprOff;
	}
	
	@Override
	public GameObject createBody(Player player){
		
		GameObject base = new GameObject();
		base.setAutoSleepingEnabled(false);
		base.color = Color.GRAY;
		org.dyn4j.geometry.Rectangle r = new org.dyn4j.geometry.Rectangle(bounds.width * unitSize, bounds.width * unitSize);
		BodyFixture bf = new BodyFixture(r);
		bf.setFilter(new PlayerFilter(player));
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
		
		if(activated && (script == null || !script.isRunning())){
			deactivate();
		}
	}
	
	@Override
	public String getDisplayName() {
		return "Computer";
	}

	@Override
	public void activate() {
		super.activate();

		if(script != null) script.run();
		sprite = sprOn;
	}
	
	@Override
	public void deactivate() {
		super.deactivate();

		if(script != null) script.stop();
		sprite = sprOff;
	}
	
	@Override
	public String getTooltip() {
		return "Script: " + (script != null ? script.name : "null");
	}
	
}
