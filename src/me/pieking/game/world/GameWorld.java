package me.pieking.game.world;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.dynamics.World;
import org.dyn4j.geometry.Rectangle;
import org.dyn4j.geometry.Triangle;
import org.dyn4j.geometry.Vector2;

import me.pieking.game.Game;
import me.pieking.game.Gameplay.GameState;
import me.pieking.game.Rand;
import me.pieking.game.Scheduler;
import me.pieking.game.Vars;
import me.pieking.game.Vault;
import me.pieking.game.gfx.Images;
import me.pieking.game.gfx.LEDStrip;
import me.pieking.game.gfx.Sprite;
import me.pieking.game.net.ServerStarter;
import me.pieking.game.net.packet.AddCubePacket;
import me.pieking.game.net.packet.SwitchOrientationPacket;
import me.pieking.game.net.packet.UpdateScorePacket;
import me.pieking.game.robot.Robot;
import me.pieking.game.robot.component.Component;
import me.pieking.game.world.Balance.Team;

public class GameWorld {
	public static final int BOOST_TIME = 10; //TODO: move power up properties to their own class
	
	private static final double ANGULAR_DAMPING = 50;
	private static final double LINEAR_DAMPING = 16;

	private static final Sprite field = Images.getSprite("fieldnew.png");
	private static boolean robotAligned = false;

	public static final float FIELD_SCALE = 1.1f;
	
	private World world;
	
	private double xOffset = 0;
	private double yOffset = 0;
	
	private Player selfPlayer;
	private List<Player> players = new ArrayList<Player>();
	
	private List<GameObject> toRemove = new ArrayList<GameObject>();
	private List<GameObject> walls = new ArrayList<GameObject>();
	private List<GameObject> particles = new ArrayList<GameObject>();
	private List<PowerCube> cubes = new ArrayList<PowerCube>();
	private List<PowerCube> exchanging = new ArrayList<PowerCube>();
	private List<ScalePlatform> scalePlatforms = new ArrayList<ScalePlatform>();
	private List<Balance> scales = new ArrayList<Balance>();
	
	private GameObject autoLineColl;
	
	private GameObject redBarColl;
	private GameObject redPlatformColl;
	private GameObject redPowerCubeZoneColl;
	private List<PowerCube> redPowerCubeZonePowerCubes = new ArrayList<PowerCube>();
	
	private GameObject blueBarColl;
	private GameObject bluePlatformColl;
	private GameObject bluePowerCubeZoneColl;
	private List<PowerCube> bluePowerCubeZonePowerCubes = new ArrayList<PowerCube>();

	private Scale scale;
	
	private double fieldXofs = 28.1;
	private double fieldYofs = 11;

	private HashMap<Team, TeamProperties> teamProperties = new HashMap<Balance.Team, TeamProperties>();
	
	private static Vector2 mouseWorldPos = new Vector2();
	
	//TODO: move power up properties to their own class
	
	private PowerUp boost = new PowerUp();
	private PowerUp force = new PowerUp();
	private PowerUp levitate = new PowerUp();
	
	private boolean cameraCentered = false;
	
	private WorldUpdateThread worldThread;
	
	public GameWorld(){
		initializeWorld();
		getWorld().addListener(new GameListener());
	}
	
	/**
	 * Initializes the world.<br>
	 * Specifically, this method:
	 * <p><ul>
	 * <li>Creates and sets properties of {@link #world}.
	 * <li>Creates and adds field collision.
	 * <li>Creates and adds the exchange sensors.
	 * <li>Creates and adds the {@link #scale} and switches.
	 * <li>Creates and adds {@link PowerCube}s.
	 * <li>Initializes {@link #teamProperties}.
	 * </ul></p>
	 */
	public void initializeWorld() {
		if(this.world == null) {
			this.world = new World();
			
			if(worldThread != null) {
				worldThread.kill();
			}
			
			worldThread = new WorldUpdateThread(this.world);
			worldThread.start();
			
		}
		getWorld().setGravity(new Vector2(0, 0));
		
		double scale = 50;
		
		// top
		GameObject floor = new GameObject();
		floor.color = new Color(0f, 0.5f, 0f, 1f);
		double w = (getFieldImage().getWidth() * scale * 0.05) / scale * FIELD_SCALE;
		double h = (getFieldImage().getHeight() * scale * 0.05) / scale * FIELD_SCALE;
		Rectangle floorRect = new Rectangle(w, 40 / scale * FIELD_SCALE);
		floorRect.translate(w/2, Component.unitSize * 2 * FIELD_SCALE);
		BodyFixture f1 = new BodyFixture(floorRect);
		f1.setDensity(0.5f);
		floor.addFixture(f1);
		
		getWorld().addBody(floor);
		
		// bottom
		GameObject floor2 = new GameObject();
		floor2.color = new Color(0f, 0.5f, 0f, 1f);
		Rectangle floor2Rect = new Rectangle(w, 40 / scale * FIELD_SCALE);
		floor2Rect.translate(w/2, h - Component.unitSize * 2 * FIELD_SCALE);
		BodyFixture f2 = new BodyFixture(floor2Rect);
		f2.setDensity(0.5f);
		floor2.addFixture(f2);
		
		getWorld().addBody(floor2);
		
		double holePos = 0.5;
		
		System.out.println("scale = " + scale);
		// left top
		GameObject floor3 = new GameObject();
		floor3.color = new Color(0f, 0.5f, 0f, 1f);
		Rectangle floor3Rect = new Rectangle(40 / scale * FIELD_SCALE, h * holePos);
		floor3Rect.translate(Component.unitSize * 16 * FIELD_SCALE, (h * 0.3)/2);
		BodyFixture f3 = new BodyFixture(floor3Rect);
		f3.setDensity(0.5f);
		floor3.addFixture(f3);
		getWorld().addBody(floor3);
		
		// left bottom
		GameObject floor3B = new GameObject();
		floor3B.color = new Color(0f, 0.5f, 0f, 1f);
		Rectangle floor3BRect = new Rectangle(40 / scale * FIELD_SCALE, h * holePos);
		floor3BRect.translate(Component.unitSize * 16 * FIELD_SCALE, (h * 0.3)/2 +  h * holePos + (PowerCube.SIZE * 1.2));
		BodyFixture f3b = new BodyFixture(floor3BRect);
		f3b.setDensity(0.5f);
		floor3B.addFixture(f3b);
		getWorld().addBody(floor3B);
		walls.add(floor3B);
		
		// left exchange
		GameObject redExchangeSensor = new GameObject();
		redExchangeSensor.color = new Color(0.5f, 0.2f, 0.2f, 0.5f);
		Rectangle redExchangeSensorRect = new Rectangle(Component.unitSize * 5 * FIELD_SCALE, Component.unitSize * 5 * FIELD_SCALE);
		redExchangeSensorRect.translate(Component.unitSize * 13.5 * FIELD_SCALE, (h * 0.3)/2 + (h * holePos)/2);
		BodyFixture res = new BodyFixture(redExchangeSensorRect);
		res.setSensor(true);
		res.setDensity(0.5f);
		redExchangeSensor.addFixture(res);
		getWorld().addBody(redExchangeSensor);
		walls.add(redExchangeSensor);
		
		double holePos2 = 0.82;
		
		// right top
		GameObject floor4 = new GameObject();
		floor4.color = new Color(0f, 0.5f, 0f, 1f);
		Rectangle floor4Rect = new Rectangle(40 / scale * FIELD_SCALE, h * holePos2);
		floor4Rect.translate(w - Component.unitSize * 16.5 * FIELD_SCALE, (h * 0.3)/2);
		BodyFixture f4 = new BodyFixture(floor4Rect);
		f4.setDensity(0.5f);
		floor4.addFixture(f4);
		getWorld().addBody(floor4);
		
		// right bottom
		GameObject floor4B = new GameObject();
		floor4B.color = new Color(0f, 0.5f, 0f, 1f);
		Rectangle floor4BRect = new Rectangle(40 / scale * FIELD_SCALE, h * holePos2);
		floor4BRect.translate(w - Component.unitSize * 16.5 * FIELD_SCALE, (h * 0.3)/2 +  h * holePos2 + (PowerCube.SIZE * 1.2));
		BodyFixture f4b = new BodyFixture(floor4BRect);
		f4b.setDensity(0.5f);
		floor4B.addFixture(f4b);
		getWorld().addBody(floor4B);
		walls.add(floor4B);
		
		
		// right exchange
		GameObject blueExchangeSensor = new GameObject();
		blueExchangeSensor.color = new Color(0.2f, 0.2f, 0.5f, 0.5f);
		Rectangle blueExchangeSensorRect = new Rectangle(Component.unitSize * 5 * FIELD_SCALE, Component.unitSize * 5 * FIELD_SCALE);
		blueExchangeSensorRect.translate(w - Component.unitSize * 14 * FIELD_SCALE, (h * 0.3)/2 + (h * holePos2)/2);
		BodyFixture bes = new BodyFixture(blueExchangeSensorRect);
		bes.setSensor(true);
		bes.setDensity(0.5f);
		blueExchangeSensor.addFixture(bes);
		getWorld().addBody(blueExchangeSensor);
		walls.add(blueExchangeSensor);
		
		walls.add(floor);
		walls.add(floor2);
		walls.add(floor3);
		walls.add(floor4);
		
		GameObject slopeUL = new GameObject();
		Triangle t1 = new Triangle(new Vector2(-1 * FIELD_SCALE, -1 * FIELD_SCALE), new Vector2(3 * FIELD_SCALE, -1 * FIELD_SCALE), new Vector2(-1 * FIELD_SCALE, 2 * FIELD_SCALE));
		t1.translate(Component.unitSize * 16 * FIELD_SCALE + (40 / scale * FIELD_SCALE), 40 / scale * FIELD_SCALE + (Component.unitSize * 2 * FIELD_SCALE));
		BodyFixture tbf1 = new BodyFixture(t1);
		slopeUL.addFixture(tbf1);
		getWorld().addBody(slopeUL);
		walls.add(slopeUL);
		
		GameObject slopeBL = new GameObject();
		Triangle t2 = new Triangle(new Vector2(-1 * FIELD_SCALE, -1 * FIELD_SCALE), new Vector2(-1 * FIELD_SCALE, -4 * FIELD_SCALE), new Vector2(3 * FIELD_SCALE, -1 * FIELD_SCALE));
		t2.translate((Component.unitSize * 16 * FIELD_SCALE + (40 / scale * FIELD_SCALE)), h - (40 / scale * FIELD_SCALE + (Component.unitSize * 2 * FIELD_SCALE)) + (2 * FIELD_SCALE));
		BodyFixture tbf2 = new BodyFixture(t2);
		slopeBL.addFixture(tbf2);
		getWorld().addBody(slopeBL);
		walls.add(slopeBL);
		
		GameObject slopeUR = new GameObject();
		Triangle t3 = new Triangle(new Vector2(-1 * FIELD_SCALE, -1 * FIELD_SCALE), new Vector2(3 * FIELD_SCALE, -1 * FIELD_SCALE), new Vector2(3 * FIELD_SCALE, 2 * FIELD_SCALE));
		t3.translate(Component.unitSize * 16 * FIELD_SCALE + (40 / scale * FIELD_SCALE), 40 / scale * FIELD_SCALE + (Component.unitSize * 2 * FIELD_SCALE));
		t3.translate(w * 0.669, 0);
		BodyFixture tbf3 = new BodyFixture(t3);
		slopeUR.addFixture(tbf3);
		getWorld().addBody(slopeUR);
		walls.add(slopeUR);
		
		GameObject slopeBR = new GameObject();
		Triangle t4 = new Triangle(new Vector2(-1 * FIELD_SCALE, -1 * FIELD_SCALE), new Vector2(3 * FIELD_SCALE, -4 * FIELD_SCALE), new Vector2(3 * FIELD_SCALE, -1 * FIELD_SCALE));
		t4.translate((Component.unitSize * 16 * FIELD_SCALE + (40 / scale * FIELD_SCALE)), h - (40 / scale * FIELD_SCALE + (Component.unitSize * 2 * FIELD_SCALE)) + (2 * FIELD_SCALE));
		t4.translate(w * 0.669, 0);
		BodyFixture tbf4 = new BodyFixture(t4);
		slopeBR.addFixture(tbf4);
		getWorld().addBody(slopeBR);
		walls.add(slopeBR);
		
		
		autoLineColl = new GameObject();
		Rectangle aRect = new Rectangle(29.15, 50);
		aRect.translate(32.38, 0);
		BodyFixture aBf = new BodyFixture(aRect);
		aBf.setSensor(true);
		autoLineColl.addFixture(aBf);
		getWorld().addBody(autoLineColl);
		
		redPlatformColl = new GameObject();
		Rectangle rpRect = new Rectangle(5, 9.4);
		rpRect.translate(30.40, 13.15);
		BodyFixture rpBf = new BodyFixture(rpRect);
		rpBf.setSensor(true);
		redPlatformColl.addFixture(rpBf);
		getWorld().addBody(redPlatformColl);
		
		redBarColl = new GameObject();
		Rectangle rbRect = new Rectangle(.5, .5);
		rbRect.translate(31, 13.15);
		BodyFixture rbBf = new BodyFixture(rbRect);
		rbBf.setSensor(true);
		redBarColl.addFixture(rbBf);
		getWorld().addBody(redBarColl);
		
		redPowerCubeZoneColl = new GameObject();
		Rectangle rpczRect = new Rectangle(3, 3);
		rpczRect.translate(17.8, 13.15);
		BodyFixture rpczBf = new BodyFixture(rpczRect);
		rpczBf.setSensor(true);
		redPowerCubeZoneColl.addFixture(rpczBf);
		getWorld().addBody(redPowerCubeZoneColl);
		
		bluePlatformColl = new GameObject();
		Rectangle bpRect = new Rectangle(5, 9.4);
		bpRect.translate(34.45, 13.15);
		BodyFixture bpBf = new BodyFixture(bpRect);
		bpBf.setSensor(true);
		bluePlatformColl.addFixture(bpBf);
		getWorld().addBody(bluePlatformColl);
		
		blueBarColl = new GameObject();
		Rectangle bbRect = new Rectangle(1.5, 1.3);
		bbRect.translate(33, 13.15);
		BodyFixture bbBf = new BodyFixture(bbRect);
		bbBf.setSensor(true);
		blueBarColl.addFixture(bbBf);
		getWorld().addBody(blueBarColl);
		
		bluePowerCubeZoneColl = new GameObject();
		Rectangle bpczRect = new Rectangle(3, 3);
		bpczRect.translate(46.9, 13.15);
		BodyFixture bpczBf = new BodyFixture(bpczRect);
		bpczBf.setSensor(true);
		bluePowerCubeZoneColl.addFixture(bpczBf);
		getWorld().addBody(bluePowerCubeZoneColl);

		// place power cubes on the field
		
		if(Game.isServer() || !Game.isConnected()) {
    		for(int i = 0; i < 6; i++){
    			addPowerCube(new PowerCube(-6.8 + fieldXofs, ((i-2) * 1.825) + fieldYofs - 0.15, 0));
    			addPowerCube(new PowerCube(9.0 + fieldXofs, ((i-2) * 1.825) + fieldYofs - 0.15, 0));
    		}
    		
    		addPowerCubeIntoZone(Team.RED, new PowerCube(-13 + fieldXofs, 0.79 + fieldYofs, 0));
    		addPowerCubeIntoZone(Team.RED, new PowerCube(-13 + PowerCube.SIZE + fieldXofs, 0.79 - PowerCube.SIZE/2 + fieldYofs, 0));
    		addPowerCubeIntoZone(Team.RED, new PowerCube(-13 + PowerCube.SIZE + fieldXofs, 0.79 + PowerCube.SIZE/2 + fieldYofs, 0));
    		addPowerCubeIntoZone(Team.RED, new PowerCube(-13 + PowerCube.SIZE + PowerCube.SIZE + fieldXofs, 0.79 + PowerCube.SIZE/2 + PowerCube.SIZE/2 + fieldYofs, 0));
    		addPowerCubeIntoZone(Team.RED, new PowerCube(-13 + PowerCube.SIZE + PowerCube.SIZE + fieldXofs, 0.79 + PowerCube.SIZE/2 - PowerCube.SIZE/2 + fieldYofs, 0));
    		addPowerCubeIntoZone(Team.RED, new PowerCube(-13 + PowerCube.SIZE + PowerCube.SIZE + fieldXofs, 0.79 - PowerCube.SIZE/2 - PowerCube.SIZE/2 + fieldYofs, 0));
    		
    		addPowerCubeIntoZone(Team.BLUE, new PowerCube(15.3 + fieldXofs, 0.79 + fieldYofs, 0));
    		addPowerCubeIntoZone(Team.BLUE, new PowerCube(15.3 - PowerCube.SIZE + fieldXofs, 0.79 - PowerCube.SIZE/2 + fieldYofs, 0));
    		addPowerCubeIntoZone(Team.BLUE, new PowerCube(15.3 - PowerCube.SIZE + fieldXofs, 0.79 + PowerCube.SIZE/2 + fieldYofs, 0));
    		addPowerCubeIntoZone(Team.BLUE, new PowerCube(15.3 - PowerCube.SIZE - PowerCube.SIZE + fieldXofs, 0.79 + PowerCube.SIZE/2 + PowerCube.SIZE/2 + fieldYofs, 0));
    		addPowerCubeIntoZone(Team.BLUE, new PowerCube(15.3 - PowerCube.SIZE - PowerCube.SIZE + fieldXofs, 0.79 + PowerCube.SIZE/2 - PowerCube.SIZE/2 + fieldYofs, 0));
    		addPowerCubeIntoZone(Team.BLUE, new PowerCube(15.3 - PowerCube.SIZE - PowerCube.SIZE + fieldXofs, 0.79 - PowerCube.SIZE/2 - PowerCube.SIZE/2 + fieldYofs, 0));
		}
		
		teamProperties.put(Team.RED, new TeamProperties());
		teamProperties.put(Team.BLUE, new TeamProperties());
		
		getProperties(Team.RED).setExchangeSensor(redExchangeSensor);
		getProperties(Team.RED).setVault(new Vault(Team.RED));
		
		getProperties(Team.BLUE).setExchangeSensor(blueExchangeSensor);
		getProperties(Team.BLUE).setVault(new Vault(Team.BLUE));
		
		// create the switches with a random one of the possible orientations
		
		boolean[] switchOrientation = getRandomSwitchOrientation();
		
		setSwitchOrientation(switchOrientation);
		
	}
	
	/**
	 * Returns a boolean array of length 3, representing the upper team color for the switches and scales.<br>
	 * The orientation is picked randomly from the list of possible orientations as per the manual.
	 * @return a boolean array of length 3, representing the upper team color for the red switch, scale, and blue switch (in that order),
	 * where <code>true</code> is blue and <code>false</code> is red.<br>
	 */
	private boolean[] getRandomSwitchOrientation() {
		
		List<boolean[]> poss = new ArrayList<boolean[]>();
		poss.add(new boolean[]{true, false, true});
		poss.add(new boolean[]{false, true, false});
		poss.add(new boolean[]{true, true, true});
		poss.add(new boolean[]{false, false, false});
		
		return poss.get(Rand.range(0, poss.size()-1));
	}

	public void setSwitchOrientation(boolean[] switchOrientation) {
		
		
//		scales.add(balance);
//		getWorld().addBody(balance.walls);
//		walls.add(balance.walls);
//		scalePlatforms.add(balance.getRedPlatform());
//		scalePlatforms.add(balance.getBluePlatform());
//		getWorld().addBody(balance.getBluePlatform().base);
//		getWorld().addBody(balance.getRedPlatform().base);
		
		if(Game.isServer()) {
			SwitchOrientationPacket sop = new SwitchOrientationPacket(switchOrientation[0] + "", switchOrientation[1] + "", switchOrientation[2] + "");
			ServerStarter.serverStarter.sendToAll(sop);
		}
		
		List<Balance> bal = new ArrayList<Balance>();
		bal.addAll(scales);
		for(Balance b : bal) {
			scales.remove(b);
			getWorld().removeBody(b.walls);
			walls.remove(b.walls);
			scalePlatforms.remove(b.getBluePlatform());
			scalePlatforms.remove(b.getRedPlatform());
			getWorld().removeBody(b.getBluePlatform().base);
			getWorld().removeBody(b.getRedPlatform().base);
		}
		
		LEDStrip srr = new LEDStrip(50);
		LEDStrip srb = new LEDStrip(50);
		LEDStrip sbr = new LEDStrip(50);
		LEDStrip sbb = new LEDStrip(50);
		LEDStrip sr = new LEDStrip(50);
		LEDStrip sb = new LEDStrip(50);
		
		Switch blueSwitch = new Switch(10.15 + fieldXofs, 0 + fieldYofs, switchOrientation[0], sbr, sbb);
		addScale(blueSwitch);
		this.scale = new Scale(0.07 + fieldXofs, 0 + fieldYofs, switchOrientation[1], sr, sb);
		addScale(this.scale);
		Switch redSwitch = new Switch(-10.15 + fieldXofs, 0 + fieldYofs, switchOrientation[2], srr, srb);
		addScale(redSwitch);
		
		getProperties(Team.RED).setSwitch(redSwitch);
		getProperties(Team.BLUE).setSwitch(blueSwitch);
	}
	
	/**
	 * Adds a {@link Player} to the world.
	 * @param pl - the {@link Player} to add.
	 */
	public void addPlayer(Player pl) {
		players.add(pl);
//		pl.constructShip();
//		getWorld().addBody(pl.base);
	}

	/**
	 * Renders the world (including the field base, {@link PowerCube PowerCubes}, {@link Balance Switches}, {@link Scale Scales}, {@link Player Players}, etc.).<br>
	 * Also {@link #renderHUD(Graphics2D) renders the HUD}.
	 * @param g - the {@link Graphics2D} object to render onto.
	 */
	public void render(Graphics2D g){
		
		AffineTransform ot = g.getTransform();
		
		AffineTransform yFlip = AffineTransform.getScaleInstance(1, 1);
		AffineTransform move = AffineTransform.getTranslateInstance(0, 0);
		g.transform(yFlip);
		g.transform(move);
		
		if(isRobotAligned()) g.rotate(-getSelfPlayer().base.getTransform().getRotation(), Game.getWidth()/2, Game.getHeight()/2);
		AffineTransform ofs = AffineTransform.getTranslateInstance(xOffset, yOffset);
		g.transform(ofs);
		
		
		g.drawImage(field.getImage(), 0, 0, (int)(getFieldImage().getWidth() * GameObject.SCALE * 0.05 * FIELD_SCALE), (int)(getFieldImage().getHeight() * GameObject.SCALE * 0.05 * FIELD_SCALE), null);
		
		// render the things!!!
		// each list is cached before iteration to avoid ConcurrentModificationExceptions
		
		List<Body> bodies = new ArrayList<Body>();
		bodies.addAll(getWorld().getBodies());
		for(Body b : bodies){
			GameObject o = (GameObject) b;
			if(Vars.showCollision) o.render(g);
		}

		if(Vars.showCollision){
    		List<GameObject> wal = new ArrayList<GameObject>();
    		wal.addAll(walls);
    		for(GameObject o : wal){
    			o.render(g);
    		}
		}
		
		List<GameObject> par = new ArrayList<GameObject>();
		par.addAll(particles);
		for(GameObject o : par){
			o.render(g);
		}
		
		List<Balance> sca = new ArrayList<Balance>();
		sca.addAll(scales);
		for(Balance o : sca){
			if(o != null) o.render(g);
		}
		
		List<PowerCube> cub = new ArrayList<PowerCube>();
		cub.addAll(cubes);
		for(PowerCube o : cub){
			if(o != null) o.render(g);
		}
		
		if(Game.gameplay.getState() != GameState.WAITING_FOR_PLAYERS){
    		List<Player> pl = new ArrayList<Player>();
    		pl.addAll(players);
    		for(Player p : pl){
    			if(p != null) p.render(g);
    		}
		}
		
		if(Vars.showCollision){
    		g.setColor(Color.GREEN);
    		g.drawRect(-5, -5, 10, 10);
		}
		
		g.setTransform(ot);
		
		Game.gameplay.renderHUD(g);
	}
	
	/**
	 * Same as {@link #render(Graphics2D)}, but can be scaled and offset.
	 * @param scale - the scale to render at (default is {@link GameObject#SCALE})
	 * @see #render(Graphics2D)
	 */
	//TODO: make this method call render(Graphics2D) instead of copy-pasting it
	public void render(Graphics2D g, double xOffset2, double yOffset2, double scale){
		
		double realScale = GameObject.SCALE;
		GameObject.SCALE = scale;
		
		//System.out.println("r3");
		AffineTransform ot = g.getTransform();
		
		AffineTransform yFlip = AffineTransform.getScaleInstance(1, 1);
		AffineTransform move = AffineTransform.getTranslateInstance(0, 0);
		g.transform(yFlip);
		g.transform(move);
		
		AffineTransform ofs = AffineTransform.getTranslateInstance(xOffset2, yOffset2);
		g.transform(ofs);
		
		g.drawImage(getFieldImage().getImage(), 0, 0, (int)(getFieldImage().getWidth() * GameObject.SCALE * 0.05 * FIELD_SCALE), (int)(getFieldImage().getHeight() * GameObject.SCALE * 0.05 * FIELD_SCALE), null);
		
		// render the things!!!
		// each list is cached before iteration to avoid ConcurrentModificationExceptions
		
		List<Body> bodies = new ArrayList<Body>();
		bodies.addAll(getWorld().getBodies());
		for(Body b : bodies){
			GameObject o = (GameObject) b;
			if(Vars.showCollision) o.render(g);
		}

		if(Vars.showCollision){
    		List<GameObject> wal = new ArrayList<GameObject>();
    		wal.addAll(walls);
    		for(GameObject o : wal){
    			o.render(g);
    		}
		}
		
		List<GameObject> par = new ArrayList<GameObject>();
		par.addAll(particles);
		for(GameObject o : par){
			o.render(g);
		}
		
		List<Balance> sca = new ArrayList<Balance>();
		sca.addAll(scales);
		for(Balance o : sca){
			if(o != null) o.render(g);
		}
		
		List<PowerCube> cub = new ArrayList<PowerCube>();
		cub.addAll(cubes);
		for(PowerCube o : cub){
			if(o != null) o.render(g);
		}
		
		List<Player> pl = new ArrayList<Player>();
		pl.addAll(players);
		for(Player p : pl){
			if(p != null) p.render(g);
		}
		
		if(Vars.showCollision){
    		g.setColor(Color.GREEN);
    		g.drawRect(-5, -5, 10, 10);
		}
		
		g.setTransform(ot);
		
		Game.gameplay.renderHUD(g);
		
		GameObject.SCALE = realScale;
	}
	
	/**
	 * @return The transform from pixel coordinates to world coordinates 
	 */
	public AffineTransform getTransform(){
		
		AffineTransform trans = new AffineTransform();
		
		AffineTransform yFlip = AffineTransform.getScaleInstance(1, 1);
		AffineTransform move = AffineTransform.getTranslateInstance(0, 0);
		trans.concatenate(yFlip);
		trans.concatenate(move);
		
		AffineTransform ofs = AffineTransform.getTranslateInstance(xOffset, yOffset);
		trans.concatenate(ofs);
		
		return trans;
	}

	/**
	 * Updates the world, and also updates rendering zoom and offsets.
	 */
	public void tick(){
		
		// update render scale
		
		GameObject.DESIRED_SCALE = (Math.round((float)(GameObject.DESIRED_SCALE*100f))/100f);
		GameObject.SCALE += (GameObject.DESIRED_SCALE - GameObject.SCALE + GameObject.ZOOM_SCALE)/4f;
		GameObject.SCALE = (Math.round((float)(GameObject.SCALE*10f))/10f);
		
		if(GameObject.SCALE < 0.1) {
			GameObject.ZOOM_SCALE *= 0.95;
		}
		
		// update render offset
		
		if(Game.isServer()){
			xOffset = -5 * GameObject.SCALE + Game.getDisp().realWidth/2;
    		yOffset = -5 * GameObject.SCALE + Game.getDisp().realHeight/2;
		}else{
    		xOffset = -selfPlayer.base.getWorldCenter().x * GameObject.SCALE + Game.getWidth()/2;
    		yOffset = -selfPlayer.base.getWorldCenter().y * GameObject.SCALE + Game.getHeight()/2;
		}
		
		if(!Game.isServer()) getProperties(Game.getWorld().getSelfPlayer().team).getVault().tick();
		
		if(cameraCentered || Game.isServer()){
			
//			GameObject.SCALE = 24;
			
			int fw = (int)(getFieldImage().getWidth() * GameObject.SCALE * 0.05 * FIELD_SCALE);
			int fh = (int)(getFieldImage().getHeight() * GameObject.SCALE * 0.05 * FIELD_SCALE);
			
			xOffset = -fw/2;
			xOffset += Game.getWidth()/2;
			yOffset = -fh/2;
			yOffset += Game.getHeight()/2;
		}
		
		xOffset = (Math.round((float)(xOffset*100f))/100f);
		yOffset = (Math.round((float)(yOffset*100f))/100f);
		
		// power ups
		
		if(Game.gameplay.getState() == GameState.AUTON || Game.gameplay.getState() == GameState.TELEOP) {
			PowerCube.updateTransitiveCollisions();
			
			List<PowerCube> redZoneCubes = new ArrayList<PowerCube>();
			redZoneCubes.addAll(redPowerCubeZonePowerCubes);
			for(PowerCube c : redZoneCubes) {
				boolean inside = redPowerCubeZoneColl.isInContact(c.base);
				
				if(!inside) {
					redPowerCubeZonePowerCubes.remove(c);
					List<Player> contacting = c.getTransitives();
					for(Player p : contacting) {
						if(p.team != Team.BLUE) {
							getProperties(Team.BLUE).addPenalty(Pentalty.FOUL, 1);
						}
					}
				}
				
			}
			
			List<PowerCube> blueZoneCubes = new ArrayList<PowerCube>();
			blueZoneCubes.addAll(bluePowerCubeZonePowerCubes);
			for(PowerCube c : blueZoneCubes) {
				boolean inside = bluePowerCubeZoneColl.isInContact(c.base);
				
				if(!inside) {
					bluePowerCubeZonePowerCubes.remove(c);
					List<Player> contacting = c.getTransitives();
					for(Player p : contacting) {
						if(p.team != Team.RED) {
							getProperties(Team.RED).addPenalty(Pentalty.FOUL, 1);
						}
					}
				}
				
			}
			
    		if(boost.getTimer() > 0){
    			if((boost.getLevel()-1) % 2 == 0){ // 0 or 2
        			if(boost.getUsing() == Team.RED) getProperties(Team.RED).setSwitchScoreMod(2);
        			if(boost.getUsing() == Team.BLUE) getProperties(Team.BLUE).setSwitchScoreMod(2);
    			}
    			
    			if((boost.getLevel()-1) >= 1){ // 1 or 2
    				if(boost.getUsing() == Team.RED) getProperties(Team.RED).setScaleScoreMod(2);
        			if(boost.getUsing() == Team.BLUE) getProperties(Team.BLUE).setScaleScoreMod(2);
    			}
    			
    			boost.setTimer(boost.getTimer() - 1);
    			if(boost.getTimer() == 0){
    				getProperties(Team.RED).setSwitchScoreMod(2);
    				getProperties(Team.RED).setScaleScoreMod(2);
    				getProperties(Team.BLUE).setSwitchScoreMod(2);
    				getProperties(Team.BLUE).setScaleScoreMod(2);
    				
    				boost.setUsing(Team.NONE);
    				if(boost.getUsing() != Team.NONE){
    					forceBoost(boost.getQueued());
    					boost.setQueued(Team.NONE);
    				}
    			}
    		}
    		
    		if(force.getTimer() > 0){
    			if((force.getLevel()-1) % 2 == 0){ // 0 or 2
    				if(force.getUsing() == Team.RED) getSwitch(Team.RED).setOwnerOverride(Team.RED);
    				if(force.getUsing() == Team.BLUE) getSwitch(Team.BLUE).setOwnerOverride(Team.BLUE);
    			}
    			
    			if((force.getLevel()-1) >= 1){ // 1 or 2
    				scale.setOwnerOverride(force.getUsing());
    			}
    			
    			force.setTimer(force.getTimer() - 1);
    			if(force.getTimer() == 0){
    				force.setUsing(Team.NONE);
    				
    				getSwitch(Team.RED).setOwnerOverride(Team.NONE);
    				getSwitch(Team.BLUE).setOwnerOverride(Team.NONE);
    				scale.setOwnerOverride(Team.NONE);
    				
    				if(force.getQueued() != Team.NONE){
    					forceForce(force.getQueued());
    					force.setQueued(Team.NONE);
    				}
    			}
    		}

    		// if a power cube is in a team's exchange, increment their cube storage, add some velocity as a fun little animation, and destroy the physical cube after a bit
    		List<PowerCube> cub = new ArrayList<PowerCube>();
    		cub.addAll(cubes);
    		for(PowerCube c : cub){
    			if(exchanging.contains(c)) continue;
    			for(Team team : getPlayingTeams()){
    				if(getExchangeSensor(team) != null && getExchangeSensor(team).contains(c.base.getWorldCenter())){
    					getProperties(team).addCubeStorage(1);
    					c.base.destructionTime = System.currentTimeMillis() + 500;
    					c.base.creationTime = Game.getTime();
    					exchanging.add(c);
    					c.base.setLinearVelocity(team == Team.RED ? -40 : 40, 0); // red's exchange is facing left while blue's is facing right
    					Scheduler.delayedTask(() -> {
    						removeCube(c);
    					}, 30);
    				}
    			}
    		}
    		
    		// update the scores for the switches and scale every second (once every 60 ticks)
    		if(Game.isServer() || !Game.isConnected()) {
        		if(Game.getTime() % 60 == 0){
        			for(Team team : getPlayingTeams()){
        				if(getSwitch(team).getOwner() == team){
        					TeamProperties tp = getProperties(team);
        					tp.addScore(tp.getSwitchScoreMod());
        	    		}
        			}
            		
        			if(scale.getOwner() != Team.NONE){
            			TeamProperties tp = getProperties(scale.getOwner());
            			tp.addScore(tp.getScaleScoreMod());
        			}
        			
        			if(Game.isServer()) {
        				UpdateScorePacket usp = new UpdateScorePacket(getProperties(Team.RED).getScore() + "", getProperties(Team.BLUE).getScore() + "");
        				ServerStarter.serverStarter.sendToAll(usp);
        			}
        		}
    		}
		}
		// update the players
		for(Player p : players){
			if(p != null) p.tick();
		}
		
		// remove particles that are done living
		List<GameObject> par = new ArrayList<GameObject>();
		par.addAll(particles);
		for(GameObject o : par){
			if(System.currentTimeMillis() >= o.destructionTime){
				if(getWorld().removeBody(o)){
					particles.remove(o);
				}
			}
		}
		
		// tick the scale and switches
		List<Balance> sca = new ArrayList<Balance>();
		sca.addAll(scales);
		for(Balance o : sca){
			if(o != null) o.tick();
		}
		
		List<PowerCube> pcs = new ArrayList<PowerCube>();
		pcs.addAll(cubes);
		for(PowerCube c : pcs){
			if(c != null) c.tick();
		}
		
		// remove any miscellaneous game objects
		List<GameObject> rem = new ArrayList<GameObject>();
		rem.addAll(toRemove);
		for(GameObject o : rem){
			getWorld().removeBody(o);
			toRemove.remove(o);
		}
		
		worldThread.queueUpdate();
		
	}

	/**
	 * Returns the {@link Player} with the specified username.
	 * @param name - the username to search for.
	 * @return the {@link Player} with the specified username, or <code>null</code> if they could not be found.
	 */
	public Player getPlayer(String name) {
		for(Player p : players){
			if(p != null && p.name != null) if(p.name.equals(name)) return p;
		}
		return null;
	}

	/**
	 * Returns the {@link Player} with the specified {@link Robot}.
	 * @param rob - the {@link Robot} to search for.
	 * @return the {@link Player} with the specified {@link Robot}, or <code>null</code> if they could not be found.
	 */
	public Player getPlayer(Robot rob) {
		for(Player p : players){
			if(p != null && p.getRobot() != null) if(p.getRobot() == rob) return p;
		}
		return null;
	}
	
	/**
	 * @return a {@link List} of all of the connected {@link Player Players}
	 */
	public List<Player> getPlayers() {
		return players;
	}

	/**
	 * Removes the specified {@link Player} from the world, if they are in it.
	 * @param player - the {@link Player} to remove.
	 */
	public void removePlayer(Player player) {
		players.remove(player);
		if(player.base != null) getWorld().removeBody(player.base);
	}
	
	/**
	 * Adds a particle to the world.
	 * @param part - the particle to add.
	 */
	public void addParticle(GameObject part){
		particles.add(part);
		getWorld().addBody(part);
	}
	
	/**
	 * Adds a {@link PowerCube} to the world.
	 * @param cube - the {@link PowerCube} to add.
	 */
	public void addPowerCube(PowerCube cube){
		if(Game.isServer()) {
			AddCubePacket acp = new AddCubePacket(cube.getId() + "", cube.getLocation().x + "", cube.getLocation().y + "");
			ServerStarter.serverStarter.sendToAll(acp);
		}
		cubes.add(cube);
		getWorld().addBody(cube.base);
	}
	
	/**
	 * Adds a {@link PowerCube} to the world.
	 * @param cube - the {@link PowerCube} to add.
	 */
	public void addPowerCubeIntoZone(Team team, PowerCube cube){
		if(Game.isServer()) {
			AddCubePacket acp = new AddCubePacket(cube.getId() + "", cube.getLocation().x + "", cube.getLocation().y + "");
			ServerStarter.serverStarter.sendToAll(acp);
		}
		(team == Team.RED ? redPowerCubeZonePowerCubes : bluePowerCubeZonePowerCubes).add(cube);
		cubes.add(cube);
		getWorld().addBody(cube.base);
	}

	/**
	 * Returns the {@link Component} represented by the specified {@link GameObject}.
	 * @param go - the {@link GameObject} to search for.
	 * @return the {@link Component} represented by the specified {@link GameObject}, or <code>null</code> if it could not be found.
	 */
	public Component getComponent(GameObject go) {
		for(Player p : players){
			if(p.robot == null) continue;
			Component c = p.robot.getComponent(go);
			if(c != null) return c;
		}
		return null;
	}

	/**
	 * Removes a {@link Component} from the world, and runs {@link Player#destroyComponent(Component)} on the player who owns this {@link Component}.
	 * @param comp - the {@link Component} to remove
	 * @see Player#destroyComponent(Component)
	 */
	public void removeComponent(Component comp) {
		for(Player p : players){
			Component c = p.robot.getComponent(comp.lastBody);
			if(c != null) {
				p.destroyComponent(c);
			}
		}
	}

	/**
	 * Returns the {@link Player} whose {@link Robot} contains the specified {@link Component}.
	 * @param comp - the {@link Component} to search for.
	 * @return the {@link Player} whose {@link Robot} contains the specified {@link Component}, or <code>null</code> if it could not be found.
	 */
	public Player getPlayer(Component comp) {
		for(Player p : players){
			Component c = p.robot.getComponent(comp.lastBody);
			if(c != null) return p;
		}
		return null;
	}

	/**
	 * Checks whether the specified {@link GameObject} is contained by a {@link PowerCube}.
	 * @param obj - the object to check
	 * @return <code>true<code> if the {@link GameObject} is contained by a {@link PowerCube}.<br>
	 * <code>false</code> otherwise 
	 */
	public boolean isPowerCube(GameObject obj) {
		for(PowerCube b : cubes){
			if(b.base == obj) return true;
		}
		return false;
	}
	
	/**
	 * @return a {@link List} of all of the active {@link PowerCube PowerCubes}.
	 */
	public List<PowerCube> getCubes(){
		List<PowerCube> pc = new ArrayList<PowerCube>();
		pc.addAll(cubes);
		return pc;
	}

	/**
	 * Removes the specified {@link PowerCube} from the world.
	 * @param cube - the {@link PowerCube} to remove.
	 */
	public void removeCube(PowerCube cube) {
		cubes.remove(cube);
		exchanging.remove(cube);
		getWorld().removeBody(cube.base);
	}
	
	/**
	 * Adds a {@link Balance} to the world.
	 * @param balance - the {@link Balance} to add.
	 */
	public void addScale(Balance balance){
		scales.add(balance);
		getWorld().addBody(balance.walls);
		walls.add(balance.walls);
		scalePlatforms.add(balance.getRedPlatform());
		scalePlatforms.add(balance.getBluePlatform());
		getWorld().addBody(balance.getBluePlatform().base);
		getWorld().addBody(balance.getRedPlatform().base);
	}
	
	/**
	 * Checks whether the specified {@link PowerCube} is on a {@link ScalePlatform}.
	 * @param cube - the {@link PowerCube} to check.
	 * @return <code>true</code> if the {@link PowerCube} is on a {@link ScalePlatform}.<br>
	 * <code>false</code> otherwise.
	 */
	public boolean isCubeOnScale(PowerCube cube) {
		for(ScalePlatform sp : scalePlatforms) {
			if(sp.base.contains(cube.base.getWorldCenter())) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Causes the specified {@link Team} to use the boost power up.
	 * @param team - the {@link Team} who tries to activate boost.
	 * @return <code>true</code> if the boost power up is now active, or if the boost power up is now queued.<br>
	 * <code>false</code> otherwise.
	 */
	public boolean useBoost(Team team){
		if(force.getUsing() == team) return false;
//		System.out.println("boost " + power_boost_used.contains(team));
		if(boost.getUsed().contains(team)) return false;
		if(getProperties(team).getBoostLevel() < 1) return false;
		
		boost.addUsed(team);
		
		if(boost.getUsing() != Team.NONE) {
			boost.setQueued(team);
			return true;
		}
		
		forceBoost(team);
		
		return true;
	}
	
	/**
	 * Forces the specified {@link Team} to use the boost power up immediately.<br>
	 * If the other {@link Team} is already using it, they are overridden.
	 * @param team - the {@link Team} who will use boost.
	 */
	public void forceBoost(Team team){
		boost.setUsing(team);
		boost.setTimer(60 * BOOST_TIME);
		
		boost.setLevel(getProperties(team).getBoostLevel());
	}
	
	/**
	 * Causes the specified {@link Team} to use the force power up.
	 * @param team - the {@link Team} who will use force.
	 * @return <code>true</code> if the force power up is now active, or if the force power up is now queued.<br>
	 * <code>false</code> otherwise.
	 */
	public boolean useForce(Team team){
		if(boost.getUsing() == team) return false;
		if(force.getUsed().contains(team)) return false;
		if(getProperties(team).getForceLevel() < 1) return false;
		
		force.addUsed(team);
		
		if(force.getUsing() != Team.NONE) {
			force.setQueued(team);
			return true;
		}
		
		forceForce(team);
		
		return true;
	}
	
	/**
	 * Forces the specified {@link Team} to use the force power up immediately.<br>
	 * If the other {@link Team} is already using it, they are overridden.
	 * @param team - the {@link Team} who will use force.
	 */
	public void forceForce(Team team){
		force.setUsing(team);
		force.setTimer(60 * BOOST_TIME);
		
		force.setLevel(getProperties(team).getForceLevel());
	}
	
	/**
	 * Causes the specified {@link Team} to use the levitate power up.
	 * @param team - the {@link Team} who will use levitate.
	 * @return <code>true</code> if levitate was activated by this call.<br>
	 * <code>false</code> otherwise.
	 */
	public boolean useLevitate(Team team){
		if(getProperties(team).getUsedLevitate()) return false;
		if(getProperties(team).getLevitateLevel() < 3) return false;
		getProperties(team).setUsedLevitate(true);
		return true;
	}
	
	/**
	 * Resets all power ups to the starting configuration.
	 */
	public void resetPowerups(){
		boost.reset();
		force.reset();
		
		for(Team t : getPlayingTeams()){
			getProperties(t).setUsedLevitate(false);
			getProperties(t).setSwitchScoreMod(1);
			getProperties(t).setScaleScoreMod(1);
			getProperties(t).setBoostLevel(0);
			getProperties(t).setForceLevel(0);
			getProperties(t).setLevitateLevel(0);
		}
	}
	
	/**
	 * Calculates the score for the specified {@link Team}, taking penalties into account.
	 * @param team - the {@link Team} whose score will be calculated.
	 * @return the {@link Team}'s score.
	 */
	public int getScoreWithPenalites(Team team){
		int base = getProperties(team).getScore();
		Team other = team.getOpposite();
		TeamProperties otherTp = getProperties(other); 
		
		for(Pentalty p : otherTp.getPenalties().keySet()){ // we check the other team's penalties because penalties add to your rival's score
			int num = otherTp.getPenaltyCount(p);
			switch(p){
				case FOUL:
					base += 5 * num;
					break;
				case TECH_FOUL:
					base += 25 * num;
					break;
				default:
					break;
			}
		}
		
		return base;
	}

	/** 
	 * @return <code>true</code> if the renderer is aligned with the player's robot.<br>
	 * <code>false<code> otherwise.
	 */
	public static boolean isRobotAligned() {
		return robotAligned;
	}

	/**
	 * Sets whether or not the renderer is aligned with the player's robot.
	 * @param robotAligned - whether the renderer should be aligned with the player's robot.
	 */
	public static void setRobotAligned(boolean robotAligned) {
		GameWorld.robotAligned = robotAligned;
	}

	/**
	 * @return the suggested global angular damping.
	 * @see Body#setAngularDamping(double)
	 */
	public static double getAngularDamping() {
		return ANGULAR_DAMPING;
	}

	/**
	 * @return the suggested global linear damping.
	 * @see Body#setLinearDamping(double)
	 */
	public static double getLinearDamping() {
		return LINEAR_DAMPING;
	}

	/**
	 * @return the {@link Sprite} of the base field.
	 */
	public static Sprite getFieldImage() {
		return field;
	}

	/**
	 * @return the active physics {@link World}.
	 */
	public World getWorld() {
		return world;
	}

	/**
	 * @return the render xOffset.
	 */
	public double getXOffset() {
		return xOffset;
	}

	/**
	 * @return the render yOffset.
	 */
	public double getYOffset() {
		return yOffset;
	}
	
	/**
	 * Returns the {@link Balance} on the side of the specified {@link Team}.
	 * @param team - the {@link Team} to get the {@link Balance}.
	 * @return the {@link Team}'s {@link Balance}.
	 */
	public Switch getSwitch(Team team){
		return getProperties(team).getSwitch();
	}
	
	/**
	 * Returns the exchange sensor on the side of the specified {@link Team}.
	 * @param team - the {@link Team} to get the exchange sensor.
	 * @return the {@link Team}'s exchange sensor.
	 */
	public GameObject getExchangeSensor(Team team){
		return getProperties(team).getExchangeSensor();
	}
	
	/**
	 * Returns the {@link TeamProperties} related to the specified {@link Team}.
	 * @param team - the {@link Team} to get the {@link TeamProperties}.
	 * @return the {@link Team}'s {@link TeamProperties}.
	 */
	public TeamProperties getProperties(Team team){
		return teamProperties.get(team);
	}
	
	/**
	 * @return an array containing all of the {@link Team}s actually participating in the game (all {@link Team}s except {@link Team#NONE}).
	 */
	public Team[] getPlayingTeams(){
		return new Team[]{Team.RED, Team.BLUE};
	}

	/**
	 * @return a {@link List} containing all of the {@link ScalePlatform}s from the {@link Scale} and two {@link Balance}es
	 */
	public List<ScalePlatform> getScalePlatforms() {
		List<ScalePlatform> sp = new ArrayList<ScalePlatform>();
		for(Team t : getPlayingTeams()){
			TeamProperties tp = getProperties(t);
			sp.add(tp.getSwitch().getRedPlatform());
			sp.add(tp.getSwitch().getBluePlatform());
		}
		
		sp.add(scale.getBluePlatform());
		sp.add(scale.getRedPlatform());
		
		return sp;
	}

	/**
	 * @return the {@link Player} represented by this instance of the game (the real-life person playing).
	 */
	public Player getSelfPlayer(){
		return selfPlayer;
	}
	
	/**
	 * Sets the {@link Player} represented by this instance of the game (the real-life person playing).
	 * @param selfPlayer - the {@link Player} to set.
	 */
	public void setSelfPlayer(Player selfPlayer) {
		this.selfPlayer = selfPlayer;
	}

	/**
	 * @return the world location where the mouse is hovering.
	 */
	public static Vector2 getMouseWorldPos() {
		return mouseWorldPos;
	}

	public void reset() {
		resetPowerups();

		world.removeAllBodies();
		walls.clear();
		cubes.clear();
		scalePlatforms.clear();
		scales.clear();
		exchanging.clear();
		particles.clear();
		
		initializeWorld();
		
		for(Player p : players){
			p.setLocation(new Point2D.Double(0, 0), 0);
			world.addBody(p.base);
			p.constructShip();
		}
	}

	public boolean isCameraCentered() {
		return cameraCentered;
	}

	public void setCameraCentered(boolean cameraCentered) {
		this.cameraCentered = cameraCentered;
	}

	public Scale getScale() {
		return scale;
	}

	public Body getAutoLine() {
		return autoLineColl;
	}

	public PowerUp getBoost() {
		return boost;
	}

	public PowerUp getForce() {
		return force;
	}

	public GameObject getPlatform(Team team) {
		return team == Team.RED ? redPlatformColl : bluePlatformColl;
	}

	public PowerCube getPowerCube(int id) {
		for(PowerCube c : cubes) {
			if(c.getId() == id) return c;
		}
		return null;
	}
	
	public boolean isInClimbRange(Component comp, Team team) {
		return (team == Team.RED ? redBarColl : blueBarColl).contains(comp.lastBody.getWorldCenter());
	}
	
	public boolean isInPowerCubeZone(Robot rob, Team team) {
		GameObject zone = team == Team.RED ? redPowerCubeZoneColl : bluePowerCubeZoneColl;
		for(Component c : rob.getComponents()) {
			if(zone.isInContact(c.lastBody)) {
				return true;
			}
		}
		
		return false;
	}
	
	
}
