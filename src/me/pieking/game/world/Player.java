package me.pieking.game.world;

import java.awt.AWTException;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.dynamics.Force;
import org.dyn4j.dynamics.Torque;
import org.dyn4j.dynamics.joint.WeldJoint;
import org.dyn4j.geometry.Mass;
import org.dyn4j.geometry.MassType;
import org.dyn4j.geometry.Vector2;

import com.studiohartman.jamepad.ControllerState;

import me.pieking.game.FileSystem;
import me.pieking.game.Game;
import me.pieking.game.Location;
import me.pieking.game.Utils;
import me.pieking.game.gfx.Fonts;
import me.pieking.game.gfx.Render;
import me.pieking.game.gfx.ShipFileAccessory;
import me.pieking.game.gfx.ShipFileView;
import me.pieking.game.menu.ComponentInfoMenu;
import me.pieking.game.menu.SelectComponentMenu;
import me.pieking.game.net.packet.PlayerUpdatePacket;
import me.pieking.game.net.packet.ShipAddComponentPacket;
import me.pieking.game.net.packet.ShipComponentActivatePacket;
import me.pieking.game.net.packet.ShipDataPacket;
import me.pieking.game.net.packet.ShipRemoveComponentPacket;
import me.pieking.game.robot.Robot;
import me.pieking.game.robot.component.ActivatableComponent;
import me.pieking.game.robot.component.ClawGrabberComponent;
import me.pieking.game.robot.component.Component;
import me.pieking.game.robot.component.ComponentBumberCorner;
import me.pieking.game.robot.component.ComponentBumberSide;
import me.pieking.game.robot.component.ComputerComponent;
import me.pieking.game.robot.component.StructureComponentSlope;
import me.pieking.game.robot.component.StructureComponentSquare;
import me.pieking.game.sound.Sound;
import me.pieking.game.sound.SoundClip;
import me.pieking.game.world.Balance.Team;

public class Player {

	public static SoundClip s_explode = Sound.loadSound("explode.ogg");
	
	public Robot robot;
	public GameObject base;
	public String name;
	public int shootTimer = 30;
	
	public Point2D mPosTrans = new Point2D.Float();
	public Point hoverGrid = new Point(-1, -1);
	public Point selectedGrid = null;
	public Component selectedComponent = null;
	
	public boolean wasLeftPressed = false;
	public boolean wasRightPressed = false;
	
	private ComponentInfoMenu cm;
	private SelectComponentMenu scm;
	
	public HashMap<Class<? extends Component>, Integer> inventory = new HashMap<Class<? extends Component>, Integer>();
	
	public Class<? extends Component> buildSelected;
	public Component buildPreview;
	private List<GameObject> bods;
	
	public List<WeldJoint> joints = new ArrayList<WeldJoint>();
	
	public boolean dead = false;
	
	public boolean noClip = false;
	
	public Team team = Team.NONE;
	
	private List<Torque> torquequeue = new ArrayList<Torque>();
	private List<Force> forceQueue = new ArrayList<Force>();
	
	private boolean wasAPressed = false;
	
	private double height = 0f;
	
	private boolean climbing = false;
	
	public Player(String name, double x, double y, Team team) {
		this.team = team;
		this.name = name;
		
		base = new GameObject();
		base.setAutoSleepingEnabled(false);
		base.color = Color.RED;
		org.dyn4j.geometry.Rectangle r = new org.dyn4j.geometry.Rectangle(1,1);
		r.translate(x, y);
		BodyFixture bf = new BodyFixture(r);
		bf.setFilter(new PlayerFilter(this));
		bf.setRestitution(0);
		base.setMass(new Mass(base.getMass().getCenter(), 100, 100));
		base.addFixture(bf);
		base.setMass(MassType.NORMAL);
		
		base.setAngularDamping(GameWorld.getAngularDamping());
		base.setLinearDamping(GameWorld.getLinearDamping());
		
		inventory.put(StructureComponentSquare.class, 20);
		inventory.put(StructureComponentSlope.class, 8);
		inventory.put(ComputerComponent.class, 4);
//		inventory.put(RadarComponent.class, 5);
//		inventory.put(ShieldsComponent.class, 5);
		inventory.put(ClawGrabberComponent.class, 5);
		inventory.put(ComponentBumberSide.class, 20);
		inventory.put(ComponentBumberCorner.class, 20);
		
//		List<Component> comp = new ArrayList<Component>();
//		comp.add(new StructureComponentSlope(1, 0, -90));
//		comp.add(new StructureComponentSlope(3, 0, 0));
//		
//		comp.add(new StructureComponentSquare(1, 1, -90));
//		comp.add(new StructureComponentSquare(2, 1, 0));
//		comp.add(new StructureComponentSquare(3, 1, 0));
//
//		tc = new ThrusterComponent(0, 1, 90);
//		tc.actKeys.add(KeyEvent.VK_D);
//		tc.deactKeys.add(KeyEvent.VK_D);
//		comp.add(tc);
//		bc = new BlasterComponent(0, 2, 0);
//		bc.actKeys.add(KeyEvent.VK_SPACE);
//		bc.deactKeys.add(KeyEvent.VK_SPACE);
//		comp.add(bc);
//		comp.add(new StructureComponentSquare(1, 2, 0));
//		comp.add(new StructureComponentSquare(2, 2, 0));
//		comp.add(new StructureComponentSquare(3, 2, 0));
//		tc = new ThrusterComponent(4, 1, -90);
//		tc.actKeys.add(KeyEvent.VK_A);
//		tc.deactKeys.add(KeyEvent.VK_A);
//		comp.add(tc);
//		bc = new BlasterComponent(4, 2, 0);
//		bc.actKeys.add(KeyEvent.VK_SPACE);
//		bc.deactKeys.add(KeyEvent.VK_SPACE);
//		comp.add(bc);
//		
//		comp.add(new StructureComponentSlope(1, 3, 180));
//		comp.add(new StructureComponentSlope(3, 3, 90));
//		
//		tc = new ThrusterComponent(2, 3, 0);
//		tc.actKeys.add(KeyEvent.VK_W);
//		tc.deactKeys.add(KeyEvent.VK_W);
//		comp.add(tc);
//		
//		ship = new Ship(comp, this);
		
	}
	
	public void tick(){
		if(!dead){
    		if(this == Game.getWorld().getSelfPlayer()){
    			tickControls();
    		}else {
    			base.setLinearVelocity(activeLinearX * 5, activeLinearY * 5);
    			base.setAngularVelocity(activeAngularRot * 3.5);
    		}

//    		base.applyForce(new Vector2(1000, 0));
    		
    		List<Torque> torque = new ArrayList<Torque>();
    		torque.addAll(torquequeue);
    		for(Torque t : torque) {
    			if(t == null) continue; 
    			base.applyTorque(t);
    			torquequeue.remove(t);
    		}
    		
    		List<Force> Force = new ArrayList<Force>();
    		Force.addAll(forceQueue);
    		for(Force t : Force) {
    			if(t == null) continue; 
    			base.applyForce(t);
    			forceQueue.remove(t);
    		}
    		
//    		if(robot.getAutonScript() != null && robot.getAutonScript().isRunning()) {
//    			LuaThread ltr = robot.getAutonScript().thread;
//    			new Thread(() -> {
//    				ltr.state.lua_yield(LuaValue.NIL);
//    				try {
//						Thread.sleep(100);
//					} catch (InterruptedException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//    				ltr.state.lua_resume(ltr, LuaValue.NIL);
//    			}).start();
//    		}
    
//    		if(Game.getTime() % 600 == 0 && robot != null){
//    			constructShip();
//    		}
    		
    		if(getLocation().x < 0 || getLocation().x > 60 || getLocation().y < 0 || getLocation().y > 30) {
    			constructShip();
    			setLocation(new Point2D.Float(12, 10), 0);
    		}
    		
    		if(robot != null) robot.tick();
		}
	}
	
	public void tickControls(){
		
		if(inventory.get(buildSelected) == null){
			buildSelected = null;
		}
		
		if((scm == null || !scm.isFocused()) && !(cm != null && !cm.isFocused())){
    		boolean nowLeftPressed = Game.mouseHandler().isLeftPressed();
    		
    		if(nowLeftPressed && !wasLeftPressed){
    			
    			if(Robot.buildMode && new Rectangle(Game.getWidth() - 100, Game.getHeight() - 100, 100, 100).contains(Game.mouseLoc())){
    				scm = new SelectComponentMenu(createPreviewComponents());
    				Render.showMenu(scm);
    				hoverGrid = new Point(-1, -1);
    			}else if((cm == null || !cm.insideMenu(Game.mouseLoc())) && Robot.buildMode){
    				Point prevGrid = selectedGrid;
    				selectedGrid = hoverGrid;
    				Component cmp = robot.getComponent(selectedGrid);
    				
    				if(cmp == null){
    					selectedGrid = null;
    					Render.hideMenu(cm);
    					cm = null;
    					selectedComponent = null;
    				}else if(cmp == selectedComponent){
    					if(Game.keyHandler().isPressed(KeyEvent.VK_CONTROL)){
    						if(selectedComponent instanceof ActivatableComponent){
    							selectedGrid = prevGrid;
    							ActivatableComponent ac = (ActivatableComponent) selectedComponent;
    							if(ac.activated){
    								ac.deactivate();
    							}else{
    								ac.activate();
    							}
    							
    							ShipComponentActivatePacket scap = new ShipComponentActivatePacket(name, ac.bounds.x + "", ac.bounds.y + "", ac.activated + "");
    							Game.sendPacket(scap);
    						}
    					}else{
        					selectedGrid = null;
        					Render.hideMenu(cm);
        					cm = null;
        					selectedComponent = null;
    					}
    				}else{
    					Render.hideMenu(cm);
    					cm = null;
    					selectedComponent = cmp;
    					if(Game.keyHandler().isPressed(KeyEvent.VK_CONTROL)){
    						if(selectedComponent instanceof ActivatableComponent){
    							selectedGrid = prevGrid;
    							ActivatableComponent ac = (ActivatableComponent) selectedComponent;
    							
    							if(ac.activated){
    								ac.deactivate();
    							}else{
    								ac.activate();
    							}
    							
    							ShipComponentActivatePacket scap = new ShipComponentActivatePacket(name, ac.bounds.x + "", ac.bounds.y + "", ac.activated + "");
    							Game.sendPacket(scap);
    						}
    					}else if(Game.keyHandler().isPressed(KeyEvent.VK_SHIFT)){
    						deleteSelected();
    					}else{
        					cm = new ComponentInfoMenu(selectedComponent);
        					Render.showMenu(cm);
    					}
    				}
    			}
    		}
    		
    		wasLeftPressed = nowLeftPressed;
    		
    		boolean nowRightPressed = Game.mouseHandler().isRightPressed();
    		
    		if(nowRightPressed && !wasRightPressed && Robot.buildMode){
    			if(buildPreview != null && buildSelected != null && hoverGrid != null){
    				try {
    					ShipAddComponentPacket sacp = new ShipAddComponentPacket(name, buildSelected.getSimpleName(), hoverGrid.x + "", hoverGrid.y + "", buildPreview.rot + "");
						Game.sendPacket(sacp);
    					
    					Component c = createComponent(buildSelected, hoverGrid.x, hoverGrid.y, buildPreview.rot);
    					if(robot.addComponent(c)){
    						invSubtract(buildSelected, 1);
    					}
    					constructShip();
    				}catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | UnsupportedEncodingException | ClassNotFoundException e) {
    					e.printStackTrace();
    				}
    			}
    		}
    		
    		wasRightPressed = nowRightPressed;
		}
		
//		Class<? extends Component> cl = Game.getTime() % 60 >= 30 ? StructureComponentSquare.class : StructureComponentSlope.class;
//		
//		cl = StructureComponentSlope.class;
//		
//		if(buildSelected != cl){
//			try {
//				selectBuildItem(cl, 0);
//			}catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
//				e.printStackTrace();
//			}
//		}
		
//		double rot = Math.round(base.getTransform().getRotation());
//		
//		System.out.println(rot);
//		if(Game.getTime() % 60 == 0){
//			Vector2 trans = base.getTransform().getTranslation().copy();
//			base.translateToOrigin();
//			base.translate(trans);
//		}
		
		if(robot.isEnabled()){
			if(robot.canMove()) {
        		if(Game.keyHandler().isPressed(KeyEvent.VK_UP)){
        			float speed = 10f;
        			
        			if(Game.keyHandler().isPressed(KeyEvent.VK_SHIFT)) speed *= 5; 
        			
        			Point2D.Float pt = Utils.polarToCartesian((float) Math.toRadians(Math.toDegrees(base.getTransform().getRotation()) + 90), speed);
        			Vector2 vec = base.getWorldCenter().subtract(base.getWorldCenter().copy().add(pt.x, pt.y));
        			base.applyForce(vec);
        		}
			}
    		
    		double speedMultiplier = Game.keyHandler().isPressed(KeyEvent.VK_SHIFT) ? 5 : 1;
    		speedMultiplier *= 5;
    		
    		double mechPower = 110 * speedMultiplier;
    		
    		ControllerState cont = Game.controllerState();
    		
    		if(cont.isConnected) {
    			double rt = cont.rightTrigger;
    			if(rt < 0.05) rt = 0;
    			double lt = cont.leftTrigger;
    			if(rt < 0.05) rt = 0;
    			
    			float liftFactor = 48f;
    			
    			if(isClimbing()) liftFactor *= 4;
    			
    			double newHeight = Math.max(0f, Math.min(height + (rt / liftFactor) - (lt / liftFactor), 1f));
    			
    			ClawGrabberComponent claw = null;
				for(Component c : robot.getComponents()) {
					if(c instanceof ClawGrabberComponent) {
						claw = (ClawGrabberComponent)c;
					}
				}
				
				if(claw != null) {
    				if(Game.getWorld().isInClimbRange(claw, team)) {
    					if(newHeight < height && height >= 0.98) {
    						setClimbing(true);
    					}else if(newHeight > height && newHeight >= 0.98) {
    						setClimbing(false);
    					}
    				}else {
    					setClimbing(false);
    				}
				}
    			
    			height = newHeight; 
    			
//    			mechPower *= (rt * 5)+1;
    			
    			double mag = cont.leftStickMagnitude;
    			if(mag < 0.25) mag = 0;
    			
    			mag *= mechPower;
    			
    			if(robot.canMove()) {
        			Vector2 v = new Vector2(Math.toRadians(-cont.leftStickAngle));
        			v.setMagnitude(mag);
        			base.applyForce(v);
    			
        			double rot = cont.rightStickX;
        			if(Math.abs(rot) < 0.25) rot = 0;
        			base.applyTorque(200 * rot * speedMultiplier);
    			}
    			
    			boolean nowAPressed = cont.rb;
    			
    			if(nowAPressed && !wasAPressed) {
    				java.awt.Robot r;
					try {
						r = new java.awt.Robot();
						r.keyPress(KeyEvent.VK_SPACE);
					} catch (AWTException e) {
						e.printStackTrace();
					}
    			}else if(!nowAPressed && wasAPressed) {
    				java.awt.Robot r;
					try {
						r = new java.awt.Robot();
						r.keyRelease(KeyEvent.VK_SPACE);
					} catch (AWTException e) {
						e.printStackTrace();
					}
    			}
    			
    			wasAPressed = nowAPressed;
    			
    			if(cont.rightStickClick) {
    				double nz = GameObject.ZOOM_SCALE + (cont.rightStickY/2) * (GameObject.SCALE/10);
    				GameObject.ZOOM_SCALE = Math.max(-50, Math.min(nz, 50));
    			}
    		}
    		
    		if(robot.canMove()) {
        		if(Game.keyHandler().isPressed(KeyEvent.VK_W)){
        			Vector2 vec = base.getWorldCenter().subtract(base.getWorldCenter().copy().add(0, mechPower));
        			base.applyForce(vec);
        		}
        		
        		if(Game.keyHandler().isPressed(KeyEvent.VK_S)){
        			Vector2 vec = base.getWorldCenter().subtract(base.getWorldCenter().copy().add(0, -mechPower));
        			base.applyForce(vec);
        		}
        		
        		if(Game.keyHandler().isPressed(KeyEvent.VK_A)){
        			Vector2 vec = base.getWorldCenter().subtract(base.getWorldCenter().copy().add(mechPower, 0));
        			base.applyForce(vec);
        		}
        		
        		if(Game.keyHandler().isPressed(KeyEvent.VK_D)){
        			Vector2 vec = base.getWorldCenter().subtract(base.getWorldCenter().copy().add(-mechPower, 0));
        			base.applyForce(vec);
        		}
        		
        		if(Game.keyHandler().isPressed(KeyEvent.VK_LEFT)){
        			base.applyTorque(-200 * speedMultiplier);
        		}else if(Game.keyHandler().isPressed(KeyEvent.VK_RIGHT)){
        			base.applyTorque(200 * speedMultiplier);
        		}else{
//        			base.setAngularVelocity(base.getAngularVelocity() * 0.7);
        		}
    		}
    		
    		// claw height
    		
			float liftFactor = 48f;
			
			if(isClimbing()) liftFactor *= 4;
			
			double newHeight = height;
			
			if(Game.keyHandler().isPressed(KeyEvent.VK_UP)){
				newHeight = Math.max(0f, Math.min(height + (1 / liftFactor), 1f));
			}else if(Game.keyHandler().isPressed(KeyEvent.VK_DOWN)){
				newHeight = Math.max(0f, Math.min(height - (1 / liftFactor), 1f));
			}
			
//			System.out.println(newHeight);
			
			ClawGrabberComponent claw = null;
			for(Component c : robot.getComponents()) {
				if(c instanceof ClawGrabberComponent) {
					claw = (ClawGrabberComponent)c;
				}
			}
			
			if(claw != null) {
				if(Game.getWorld().isInClimbRange(claw, team)) {
					if(newHeight < height && height >= 0.98) {
						setClimbing(true);
					}else if(newHeight > height && newHeight >= 0.98) {
						setClimbing(false);
					}
				}else {
					setClimbing(false);
				}
			}
			
			height = newHeight; 
    		
		}
//		
		if(Game.getTime() % 5 == 0 && !dead){
			sendServerMotion();
		}
//		
//		if(shootTimer > 0) shootTimer--;
//		
//		if(shootTimer == 0 && Game.keyHandler().isPressed(KeyEvent.VK_SPACE)){
//			Bullet b = new Bullet(name, getLocation().x, getLocation().y, base.getTransform().getRotation());
//			Game.getWorld().world.addBody(b.base);
//			shootTimer = 60;
//		}
		
		if(scm == null || !scm.isFocused()){
    		int gridSize = robot.getGridSize();
    		float gridBoxSize = Component.unitSize;
    		
    		AffineTransform trans = new AffineTransform();
    		
    		trans.concatenate(Game.getWorld().getTransform());
    		trans.scale(GameObject.SCALE, GameObject.SCALE);
    		if(!GameWorld.isRobotAligned()) trans.rotate(base.getTransform().getRotation(), getLocation().x, getLocation().y);
    		trans.translate(getLocation().x, getLocation().y);
    		trans.translate(-0.5 * gridBoxSize * gridSize, -0.5 * gridBoxSize * gridSize);
    		
    		Point mPos = Game.mouseLoc();
    		
    		Point pt = new Point();
    		try {
    			AffineTransform inv = trans.createInverse();
    			inv.transform(mPos, mPosTrans);
    			pt = new Point((int)(mPosTrans.getX() / gridBoxSize), (int)( mPosTrans.getY() / gridBoxSize));
    		}catch (NoninvertibleTransformException e) {
    			e.printStackTrace();
    		}
    	
    		hoverGrid = pt;
		}
		
	}

	public List<Component> previewComponents = createPreviewComponents();

	private float activeLinearX;

	private float activeLinearY;

	private float activeAngularRot;
	
	public List<Component> createPreviewComponents() {
		
		if(previewComponents != null) return previewComponents;
		
		List<Component> comp = new ArrayList<Component>();
		
		List<Class<? extends Component>> classes = new ArrayList<Class<? extends Component>>();
		
		classes.add(StructureComponentSquare.class);
		classes.add(StructureComponentSlope.class);
		classes.add(ComputerComponent.class);
		classes.add(ClawGrabberComponent.class);
		classes.add(ComponentBumberSide.class);
		classes.add(ComponentBumberCorner.class);
		
		for(Class<? extends Component> cl : classes){
			try {
				Component c = createComponent(cl, 0, 0, 0);
				c.lastBody.personalScale = 80;
				comp.add(c);
			}catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				e.printStackTrace();
			}
		}
//		System.out.println("done");
		return comp;
	}

	public void sendServerMotion() {
		System.out.println("serv");
//		System.out.println(base.getTransform().getTranslation().x + " " + base.getTransform().getTranslation().y);
		PlayerUpdatePacket pack = new PlayerUpdatePacket(name, base.getWorldCenter().x + "", base.getWorldCenter().y + "", base.getLinearVelocity().x + "", base.getLinearVelocity().y + "", base.getTransform().getRotation() + "", base.getAngularVelocity()*2 + "");
		System.out.println(pack.toString());
		Game.sendPacket(pack);
	}

	public Location getLocation() {
		return new Location((float)base.getWorldCenter().x, (float) base.getWorldCenter().y);
	}
	
	public void render(Graphics2D g){
		
		if(robot == null) return;
		
		if(!Robot.buildMode) robot.render(g);
		
		if(this == Game.getWorld().getSelfPlayer()){
    		int gridSize = robot.getGridSize();
    		float gridBoxSize = Component.unitSize;
    		
    		AffineTransform oldtrans = g.getTransform();
    		AffineTransform trans = new AffineTransform();
    		
    		trans.concatenate(oldtrans);
    		trans.scale(GameObject.SCALE, GameObject.SCALE);
    		trans.rotate(base.getTransform().getRotation(), getLocation().x, getLocation().y);
    		trans.translate(getLocation().x, getLocation().y);
    		trans.translate(-0.5 * gridBoxSize * gridSize, -0.5 * gridBoxSize * gridSize);
    		
    		g.setTransform(trans);
    		
    		Color c1 = new Color(0.5f, 0.5f, 0.5f, 0.5f);
    		Color c4 = new Color(0.2f, 0.2f, 0.2f, 0.3f);
    		Color c2 = new Color(0.9f, 0.8f, 0.2f, 0.5f);
    		Color c3 = new Color(0.2f, 0.8f, 0.2f, 0.8f);
    		
    		float stroke = 0.05f;
    		
    		String tooltip = null;
    		
    		g.setStroke(new BasicStroke(stroke));
    		if(Robot.buildMode){
        		for(int x = 0; x < gridSize; x++){
        			for(int y = 0; y < gridSize; y++){
        				g.setColor(c1);
        //				g.draw(new Rectangle2D.Float(gridBoxSize * x, gridBoxSize * y, gridBoxSize, gridBoxSize));
        				
    					g.setColor(c4);
    					g.fill(new Rectangle2D.Float(gridBoxSize * x, gridBoxSize * y, gridBoxSize, gridBoxSize));
    					g.setColor(c1);
    					g.draw(new Line2D.Float(gridBoxSize * x, gridBoxSize * y, gridBoxSize * x + gridBoxSize - stroke, gridBoxSize * y));
    					g.draw(new Line2D.Float(gridBoxSize * x, gridBoxSize * y + stroke, gridBoxSize * x, gridBoxSize * y + gridBoxSize - stroke));
        			}
        		}
    		}
    		
    		if(Robot.buildMode){
        		g.draw(new Line2D.Float(0, gridBoxSize * gridSize, gridBoxSize * gridSize, gridBoxSize * gridSize));
        		g.draw(new Line2D.Float(gridBoxSize * gridSize, 0, gridBoxSize * gridSize, gridBoxSize * gridSize - stroke));
        		g.setTransform(oldtrans);
        		robot.render(g);
        		g.setTransform(trans);
    		}
    		
    		for(int x = 0; x < gridSize; x++){
    			for(int y = 0; y < gridSize; y++){
    				g.setColor(c1);
    				
    				if(hoverGrid.x == x && hoverGrid.y == y && Robot.buildMode){
    					
    					Component c = robot.getComponent(hoverGrid);
    					if(c != null){
    						tooltip = c.getTooltip();
    						
    						Color col = c.lastBody.color;
    						
    						c.lastBody.color = c2;
    						
    						g.setTransform(oldtrans);
    						c.lastBody.render(g);
    						g.setTransform(trans);
    						
    						c.lastBody.color = col;
    						
    					}else{
//    						g.setColor(c2);
//        					g.fill(new Rectangle2D.Float(gridBoxSize * x, gridBoxSize * y, gridBoxSize, gridBoxSize));
    					}
    					
    					if(Robot.buildMode && buildPreview != null && buildSelected != null){
    						AffineTransform tr = g.getTransform();
    						g.scale(0.01, 0.01);
    						g.translate((gridBoxSize * x + gridBoxSize/2) / 0.01, (gridBoxSize * y + gridBoxSize/2) / 0.01);
    						buildPreview.render(g);
    						g.setTransform(tr);
    					}
    					
    				}
    			}
    		}
    		
    		if(selectedGrid != null && selectedComponent != null){
    			g.setStroke(new BasicStroke(0.05f));
    			g.setColor(c3);
    			g.draw(new Rectangle2D.Float(gridBoxSize * selectedComponent.bounds.x, gridBoxSize * selectedComponent.bounds.y, gridBoxSize * selectedComponent.bounds.width, gridBoxSize * selectedComponent.bounds.height));
    		}
    		
    //		g.drawRect((int)mPos.getX(), (int)mPos.getY(), 1, 1);
    		
    		g.setStroke(new BasicStroke(0.02f));
    		g.setColor(Color.GREEN);
    		g.draw(new Rectangle2D.Float((float)mPosTrans.getX() - 0.05f, (float)mPosTrans.getY() - 0.05f, 0.1f, 0.1f));
    		
    		g.setTransform(oldtrans);

    		if(tooltip != null && GameObject.SCALE > 20){
        		trans = new AffineTransform();
        		
        		trans.concatenate(oldtrans);
        		trans.scale(GameObject.SCALE, GameObject.SCALE);
        		trans.rotate(base.getTransform().getRotation(), getLocation().x, getLocation().y);
        		trans.translate(getLocation().x, getLocation().y);
        		trans.translate(-0.5 * gridBoxSize * gridSize, -0.5 * gridBoxSize * gridSize);
    		
        		AffineTransform rot = AffineTransform.getRotateInstance(-base.getTransform().getRotation(), gridBoxSize * hoverGrid.x + gridBoxSize/2, gridBoxSize * hoverGrid.y + gridBoxSize/2);
        		
        		if(GameWorld.isRobotAligned()){
        			rot = new AffineTransform();
        		}
        		
        		trans.concatenate(rot);
    
        		g.setTransform(trans);
        		
        		Rectangle2D.Float r = new Rectangle2D.Float(gridBoxSize * hoverGrid.x, gridBoxSize * hoverGrid.y, gridBoxSize, gridBoxSize);
        		
        		double tipX = 0;
        		double tipY = 0;
        		
    			try {
    				Shape s = rot.createInverse().createTransformedShape(r);
    				Rectangle2D r2d = s.getBounds2D();
//    				g.draw(r2d);
    				
    				tipY = -r2d.getHeight()/2;
    				
    			}catch (NoninvertibleTransformException e) {
    				e.printStackTrace();
    			}
        		
    			g.translate(gridBoxSize * hoverGrid.x + gridBoxSize/2 + tipX, gridBoxSize * hoverGrid.y + gridBoxSize/2 + tipY);
    			
    //			System.out.println(GameObject.SCALE);
    
    			double sc = 0.8/GameObject.SCALE;
    			g.scale(sc, sc);
    			
    //    		g.setColor(Color.BLUE);
    //			g.fill(new Rectangle2D.Float(gridBoxSize/2, gridBoxSize/2, gridBoxSize, gridBoxSize));
    		
    			g.setColor(Color.WHITE);
    			g.setFont(Fonts.pixelmix.deriveFont(20f));
    
    			
    			float tx = -g.getFontMetrics().stringWidth(tooltip)/2;
    			
    			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    			
    			g.setColor(new Color(0.3f, 0.3f, 0.3f, 0.9f));
    			RoundRectangle2D.Float rf = new RoundRectangle2D.Float(tx - 10, -g.getFont().getSize() - 4 - 10, g.getFontMetrics().stringWidth(tooltip) + 16, g.getFont().getSize() + 12, 10f, 10f);
    			g.fill(rf);
    			g.setStroke(new BasicStroke(2f));
    			g.setColor(new Color(0.2f, 0.2f, 0.2f, 0.9f));
    			g.draw(rf);
    			
    			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
    			
    			g.setColor(Color.WHITE);
    			g.drawString(tooltip, tx, -10);
    		}
			
			g.setStroke(new BasicStroke(1));
    		g.setTransform(oldtrans);
    		
		}
		
//		base.render(g);
	}
	
	public void selectBuildItem(Class<? extends Component> clazz, int rot) throws NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{
		Constructor<? extends Component> c = clazz.getDeclaredConstructor(int.class, int.class, int.class);
		Component comp = c.newInstance(0, 0, rot);

		comp.lastBody = comp.createBody(this);
		comp.lastBody.rotate(Math.toRadians(comp.rot));
		comp.lastBody.personalScale = 100;
		
		buildSelected = clazz;
		buildPreview = comp;
	}
	
	public Component createComponent(Class<? extends Component> clazz, int x, int y, int rot) throws NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{
		
		
		Constructor<? extends Component> c = clazz.getDeclaredConstructor(int.class, int.class, int.class);
		
		long start = System.currentTimeMillis();

//		System.out.println(clazz.getSimpleName());
		
		Component comp = c.newInstance(x, y, rot);

//		System.out.println(System.currentTimeMillis() - start);
		
		comp.lastBody = comp.createBody(this);
		comp.lastBody.rotate(Math.toRadians(comp.rot));
		comp.lastBody.personalScale = 100;

		return comp;
	}
	
	public void buildRotate(){
//		System.out.println(buildPreview.rot);
		if(buildSelected != null && buildPreview != null){
			try {
				selectBuildItem(buildSelected, (buildPreview.rot + 90) % 360);
			}catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void constructShip(){
		if(bods != null){
			for(Body b : bods){
				Game.getWorld().getWorld().removeBody(b);
			}
		}
		
		joints.clear();
		
		List<BodyFixture> fix = robot.constructFixtures();
		bods = robot.construct();
		GameObject allColl = new GameObject();
		
		for(BodyFixture f : fix) {
			System.out.println(f.createMass().getMass());
			allColl.addFixture(f);
		}
		
		allColl.setMass(MassType.NORMAL);
		
		double rot = getRotation();
//		allColl.translate(base.getWorldCenter());
		if(base != null) {
			
			while(Game.getWorld().getWorld().containsBody(base)) {
				try {
					Game.getWorld().getWorld().removeBody(base);
				}catch(ConcurrentModificationException e) {}
			}
			
			allColl.translateToOrigin();
			allColl.translate(base.getWorldCenter());
		}
		base = allColl;
		base.setAngularDamping(GameWorld.getAngularDamping() * 0.75);
		base.setLinearDamping(GameWorld.getLinearDamping());
		Game.getWorld().getWorld().addBody(base);
		setRotation(0);
//		translateToOrigin();
		
//		System.out.println(bods);
		
//		double rot = base.getTransform().getRotation();
//		
//		System.out.println(rot);
//		base.getTransform().setRotation(0);
		
		for(GameObject b : bods){
			
			for(BodyFixture bf : b.getFixtures()) {
				bf.setSensor(true);
			}
			
			WeldJoint wj = new WeldJoint(b, base, base.getWorldCenter());
			wj.setCollisionAllowed(false);
			wj.setDampingRatio(1);
			wj.setFrequency(0);
			joints.add(wj);
			Game.getWorld().getWorld().addJoint(wj);
			
			for(GameObject b2 : bods){
				if(b2 == b) continue;
				wj = new WeldJoint(b, b2, base.getWorldCenter());
				wj.setCollisionAllowed(false);
				wj.setDampingRatio(1);
				wj.setFrequency(0);
				joints.add(wj);
				Game.getWorld().getWorld().addJoint(wj);
			}
			
			Game.getWorld().getWorld().addBody(b);
		}
		
//		base.getTransform().setRotation(rot);
	}
	
	public void loadShip(Robot sh){
		dead = false;
		
		this.robot = sh;
		constructShip();
	}
	
	public void deleteSelected(){
		if(Robot.buildMode){
			if(selectedComponent != null){
				
				ShipRemoveComponentPacket srcp = new ShipRemoveComponentPacket(name, selectedComponent.bounds.x + "", selectedComponent.bounds.y + "");
				Game.sendPacket(srcp);
				
				robot.removeComponent(selectedComponent);
				invAdd(selectedComponent.getClass(), 1);
				selectedComponent = null;
				selectedGrid = null;
				constructShip();
			}
		}
	}
	
	public void destroyComponent(Component c){
		if(c != null){
			ShipRemoveComponentPacket srcp = new ShipRemoveComponentPacket(name, c.bounds.x + "", c.bounds.y + "");
			Game.sendPacket(srcp);
			
			if(c instanceof ActivatableComponent){
				((ActivatableComponent) c).deactivate();
			}
			
//			System.out.println("remove " + c);
			
			robot.removeComponent(c);
			//invAdd(c.getClass(), 1); // TODO: should the player get the component back?
			constructShip();
			
//			health -= 100;
		}
	}
	
	private void chooseShip() {
		Robot s = selectShip();
	    
	    try {
			ShipDataPacket sdp = new ShipDataPacket(Game.getWorld().getSelfPlayer().name, s.saveDataString());
			Game.doPacket(sdp);
		}catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	public int invSubtract(Class<? extends Component> cl, int count){
		if(inventory.containsKey(cl)){
			int curr = inventory.get(cl);
			if(curr - count <= 0){
				inventory.remove(cl);
			}else{
				inventory.put(cl, inventory.get(cl) - count);
			}
		}
		
		return inventory.get(cl) == null ? 0 : inventory.get(cl); // return 0 if theres no items of this type
	}
	
	public int invAdd(Class<? extends Component> cl, int count){
		if(inventory.containsKey(cl)){
			inventory.put(cl, inventory.get(cl) + count);
		}else{
			inventory.put(cl, count);
		}
		
		return inventory.get(cl) == null ? 0 : inventory.get(cl); // return 0 if theres no items of this type
	}

	public boolean hasFocus() {
		return (scm == null || !scm.isFocused()) && !(cm != null && !cm.isFocused());
	}
	
	public void translate(double x, double y) {
		base.translate(x, y);
		if(robot != null) robot.translate(x, y);
	}
	
	public void rotate(double angle, double x, double y) {
		base.rotate(angle, x, y);
		if(robot != null) robot.rotate(angle, x, y);
	}

	public void translateToOrigin() {
		Vector2 wc = base.getWorldCenter();
		translate(-wc.x, -wc.y);
	}

	public void setRotation(double rot) {
		double currRot = base.getTransform().getRotation();
		rotate(rot - currRot, base.getWorldCenter().x, base.getWorldCenter().y);
	}

	public Robot selectShip() {
		if(Game.QUICK_CONNECT) {
			File f = FileSystem.getFile("robots/default.rob");
			
			try {
	    		return Robot.load(f, this);
			}catch (Exception e) {
				e.printStackTrace();
			}
			
			return robot;
		}else {
    		JFileChooser chooser = new JFileChooser();
    		chooser.setAccessory(new ShipFileAccessory(chooser));
    		chooser.setFileView(new ShipFileView());
    		FileNameExtensionFilter filter = new FileNameExtensionFilter("ROBOT Files", "rob");
    	    chooser.addChoosableFileFilter(filter);
    	    chooser.setCurrentDirectory(FileSystem.getFolder("robots"));
    	    chooser.setAcceptAllFileFilterUsed(true);
    	    chooser.setMultiSelectionEnabled(false);
    	    chooser.setPreferredSize(new Dimension(500, 600));
    	    int returnVal = chooser.showOpenDialog(null);
    	    if(returnVal == JFileChooser.APPROVE_OPTION) {
    	    	try {
    	    		return Robot.load(chooser.getSelectedFile(), this);
    			}catch (Exception e) {
    				e.printStackTrace();
    			}
    	    }else{
    	    	try {
    	    		return Robot.load("new", this);
    			}catch (Exception e) {
    				e.printStackTrace();
    			}
    	    }
    		return robot;
		}
	}

	public Robot getRobot() {
		return robot;
	}

	public void setLocation(Point2D pt, double rot) {
		translateToOrigin();
		translate(pt.getX(), pt.getY());
		setRotation(rot);
	}
	
	public void queueTorque(Torque torque) {
		torquequeue.add(torque);
	}
	
	public void queueForce(Force force) {
		forceQueue.add(force);
	}

	public double getRotation() {
		return base.getTransform().getRotation();
	}

	public void setActiveLinearVelocity(float xa, float ya) {
		this.activeLinearX = xa;
		this.activeLinearY = ya;
	}

	public void setActiveAngularVelocity(float rotA) {
		this.activeAngularRot = rotA;
	}
	
	public double getHeight() {
		return height;
	}
	
	public double getRobotHeight() {
		return height;
	}

	public boolean isClimbing() {
		return climbing;
	}

	public void setClimbing(boolean climbing) {
		if(climbing) {
			base.setLinearVelocity(0, 0);
			base.setAngularVelocity(0);
			
			for(Component c : robot.getComponents()) {
				c.lastBody.setLinearVelocity(0, 0);
				c.lastBody.setAngularVelocity(0);
			}
		}
//		System.out.println(climbing);
		robot.setCanMove(!climbing);
		this.climbing = climbing;
	}

	public boolean isInContact(PowerCube cube) {
		if(robot == null) return false;
		for(Component c : robot.getComponents()) {
			if(c.lastBody.isInContact(cube.base)) {
				return true;
			}
		}
		return false;
	}
	
}
