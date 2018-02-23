package me.pieking.game.world;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.Mass;
import org.dyn4j.geometry.MassType;
import org.dyn4j.geometry.Rectangle;

import me.pieking.game.Game;
import me.pieking.game.Location;
import me.pieking.game.Vars;
import me.pieking.game.gfx.Sprite;
import me.pieking.game.gfx.Spritesheet;
import me.pieking.game.net.ServerStarter;
import me.pieking.game.net.packet.CubeUpdatePacket;
import me.pieking.game.robot.component.Component;
import me.pieking.game.world.GameObject.BodyType;
import me.pieking.game.world.GameObjectFilter.FilterType;

public class PowerCube {

	private static int ID = 0;
	
	public static final double SIZE = 1.8 * Component.unitSize;
	
	public static Sprite spr = Spritesheet.tiles.subTile(0, 12, 2, 2);

	public GameObject base;
	
	private int id;
	
	private List<Player> transitiveControllers = new ArrayList<>();

	public boolean holding = false;
	
	public PowerCube(double x, double y, double angle) {
		this(x, y, angle, ID++);
	}
	
	public PowerCube(double x, double y, double angle, int id) {
		this.id = id;
		
		x *= GameWorld.FIELD_SCALE;
		y *= GameWorld.FIELD_SCALE;
		
		base = new GameObject();
//		base.setBullet(true);
		base.type = BodyType.BULLET;
		base.setAutoSleepingEnabled(false);
		base.color = Color.YELLOW;
		Rectangle r = new Rectangle(SIZE, SIZE);
		r.translate(Component.unitSize/2, Component.unitSize/2);
		r.rotateAboutCenter(angle);
		BodyFixture bf = new BodyFixture(r);
		bf.setDensity(0.5);
		bf.setFilter(new GameObjectFilter(FilterType.POWER_CUBE));
//		bf.setFilter(new BulletFilter(this));
		base.setMass(new Mass(base.getMass().getCenter(), 0, 0));
		base.addFixture(bf);
		base.setMass(MassType.NORMAL);
		
		base.setAngularDamping(GameWorld.getAngularDamping());
		base.setLinearDamping(GameWorld.getLinearDamping());
		base.translate(x, y);
		
//		float speed = 50f;
//		Point2D.Float pt = Utils.polarToCartesian((float) Math.toRadians(Math.toDegrees(angle) + 90), speed);
////		System.out.println(pt);
//		Vector2 vec = base.getWorldCenter().subtract(base.getWorldCenter().copy().add(pt.x, pt.y));
//		base.applyForce(vec);
		
	}
	
	public void tick(){
		if(Game.getTime() % 30 == 0 && Game.isServer()) {
			sendServerMotion();
		}
	}

	public Location getLocation() {
		return new Location((float)base.getWorldCenter().x, (float) base.getWorldCenter().y);
	}
	
	public void render(Graphics2D g){
		if(Vars.showCollision) {
			if(transitiveControllers.isEmpty()) {
				base.color = Color.YELLOW;
			}else {
				base.color = transitiveControllers.get(0).team.color;
			}
			base.render(g, new BasicStroke(1f));
		}else {
			base.render(g, new BasicStroke(1f), spr, (int)(Component.unitSize * 4), (int)(Component.unitSize * 4));
		}
	}
	
	public int getId() {
		return id;
	}
	
	public void sendServerMotion() {
		CubeUpdatePacket pack = new CubeUpdatePacket(id + "", base.getWorldCenter().x + "", base.getWorldCenter().y + "", base.getLinearVelocity().x + "", base.getLinearVelocity().y + "", base.getTransform().getRotation() + "", base.getAngularVelocity() + "");
		ServerStarter.serverStarter.sendToAll(pack);
	}
	
	public void setRotation(double rot) {
		double currRot = base.getTransform().getRotation();
		base.rotate(rot - currRot, base.getWorldCenter().x, base.getWorldCenter().y);
	}
	
	public void clearTransitives() {
		transitiveControllers.clear();
	}
	
	public void addTransitive(Player pl) {
		transitiveControllers.add(pl);
	}
	
	/**
	 * fun fact: i did this correctly on the first try
	 */
	public static void updateTransitiveCollisions() {
		List<PowerCube> cubes = Game.getWorld().getCubes();
		List<Player> players = Game.getWorld().getPlayers();
		
		for(PowerCube c : cubes) {
			c.clearTransitives();
		}
		
		List<PowerCube> unProcessed = new ArrayList<PowerCube>();
		
		for(Player p : players) {
			for(PowerCube c : cubes) {
				if(p.isInContact(c)) {
					unProcessed.clear();
					unProcessed.addAll(cubes);
					checkTransitive(p, c, unProcessed);
					c.addTransitive(p);
				}
			}
		}
	}
	
	public static List<PowerCube> checkTransitive(Player pl, PowerCube curr, List<PowerCube> unprocessed){
		unprocessed.remove(curr);
		List<PowerCube> chk = new ArrayList<PowerCube>();
		chk.addAll(unprocessed);
		for(PowerCube c : chk) {
			if(c == curr) continue;
			if(!unprocessed.contains(c)) continue;
			
			if(c.base.isInContact(curr.base)) {
				checkTransitive(pl, c, unprocessed);
				c.addTransitive(pl);
			}
		}
		
		return unprocessed;
	}

	public List<Player> getTransitives() {
		return transitiveControllers;
	}
	
}
