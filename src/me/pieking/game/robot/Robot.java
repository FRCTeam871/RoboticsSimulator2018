package me.pieking.game.robot;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.Transform;

import me.pieking.game.FileSystem;
import me.pieking.game.Game;
import me.pieking.game.Location;
import me.pieking.game.net.packet.ShipComponentActivatePacket;
import me.pieking.game.robot.component.ActivatableComponent;
import me.pieking.game.robot.component.Component;
import me.pieking.game.scripting.LuaScript;
import me.pieking.game.world.GameObject;
import me.pieking.game.world.Player;

public class Robot {

	public static boolean buildMode = false;
	
	private int gridSize = 5;
	
	List<Component> comp = new ArrayList<>();
	
	Player pl;
	
	private boolean enabled = true;
	
	private LuaScript autonScript;

	private boolean canMove = true;
	
	public Robot(int size, List<Component> comp, Player pl) {
		this.comp = comp;
		this.pl = pl;
		this.gridSize = size;
	}
	
	public List<GameObject> construct(){
		List<GameObject> bods = new ArrayList<GameObject>();
		
		for(Component c : comp){
			GameObject b = c.createBody(pl);
			
			for(BodyFixture bf : b.getFixtures()) {
				bf.setRestitution(0);
				bf.setFriction(1);
			}
			
			b.rotate(Math.toRadians(c.rot));
			b.translate(c.bounds.x * Component.unitSize, c.bounds.y * Component.unitSize);
			b.translate(-c.renderOfs.x * Component.unitSize, -c.renderOfs.y * Component.unitSize);
			b.translate(pl.getLocation().x - (0.5 * gridSize * Component.unitSize) + Component.unitSize/2, pl.getLocation().y - (0.5 * gridSize * Component.unitSize) + Component.unitSize/2);
			b.rotate(pl.base.getTransform().getRotation(), pl.base.getWorldCenter().x, pl.base.getWorldCenter().y);
			bods.add(b);
			c.lastBody = b;
		}
		
		return bods;
	}
	
	public List<BodyFixture> constructFixtures(){
		List<BodyFixture> bods = new ArrayList<BodyFixture>();
		
		for(Component c : comp){
			GameObject b = c.createBody(pl);
			
			for(BodyFixture bf : b.getFixtures()) {
				bf.setRestitution(0);
				bf.setFriction(1);
				bf.getShape().rotate(Math.toRadians(c.rot));
				bf.getShape().translate(c.bounds.x * Component.unitSize, c.bounds.y * Component.unitSize);
				bf.getShape().translate(-c.renderOfs.x * Component.unitSize, -c.renderOfs.y * Component.unitSize);
				bf.getShape().translate(pl.getLocation().x - (0.5 * gridSize * Component.unitSize) + Component.unitSize/2, pl.getLocation().y - (0.5 * gridSize * Component.unitSize) + Component.unitSize/2);
				bf.getShape().rotate(pl.base.getTransform().getRotation(), pl.base.getWorldCenter().x, pl.base.getWorldCenter().y);
				bods.add(bf);
			}
			
			c.lastBody = b;
		}
		
		return bods;
	}
	
	public List<GameObject> construct(Location loc, double rotation){
		List<GameObject> bods = new ArrayList<GameObject>();
		
		for(Component c : comp){
			GameObject b = c.createBody(pl);
			
			for(BodyFixture bf : b.getFixtures()) {
				bf.setRestitution(0);
				bf.setFriction(1);
			}
			
			b.rotate(Math.toRadians(c.rot));
			b.translate(c.bounds.x * Component.unitSize, c.bounds.y * Component.unitSize);
			b.translate(-c.renderOfs.x * Component.unitSize, -c.renderOfs.y * Component.unitSize);
			b.translate(loc.x - (0.5 * gridSize * Component.unitSize) + Component.unitSize/2, loc.y - (0.5 * gridSize * Component.unitSize) + Component.unitSize/2);
			b.rotate(rotation, loc.x, loc.y);
			bods.add(b);
			c.lastBody = b;
//			c.lastBody.setAngularDamping(0.5);
//			c.lastBody.setLinearDamping(0.5);
		}
		
		return bods;
	}
	
	public int getGridSize(){
		return gridSize;
	}
	
	public void keyPressed(KeyEvent e){
		if(pl.dead) return;
		if(pl.hasFocus()){
    		for(Component c : comp){
    			if(c instanceof ActivatableComponent){
    				ActivatableComponent ac = (ActivatableComponent) c;
    				if(ac.actKeys.contains(e.getKeyCode())) {
    					
    					if(ac.toggleMode) {
    						ac.toggle();
    						
    						if(pl == Game.getWorld().getSelfPlayer()){
	    						ShipComponentActivatePacket scap = new ShipComponentActivatePacket(pl.name, ac.bounds.x + "", ac.bounds.y + "", ac.activated + "");
	    						Game.sendPacket(scap);
	    					}
    					}else{
    						ac.activate();
							
							if(pl == Game.getWorld().getSelfPlayer()){
	    						ShipComponentActivatePacket scap = new ShipComponentActivatePacket(pl.name, ac.bounds.x + "", ac.bounds.y + "", true + "");
	    						Game.sendPacket(scap);
	    					}
    					}
    				}
    			}
    		}
		}
	}
	
	public void keyReleased(KeyEvent e){
		if(pl.dead) return;
		if(pl.hasFocus()){
    		for(Component c : comp){
    			if(c instanceof ActivatableComponent){
    				ActivatableComponent ac = (ActivatableComponent) c;
    				if(ac.deactKeys.contains(e.getKeyCode()) && !ac.toggleMode) {
    					ac.deactivate();
    					if(pl == Game.getWorld().getSelfPlayer()){
    						ShipComponentActivatePacket scap = new ShipComponentActivatePacket(pl.name, ac.bounds.x + "", ac.bounds.y + "", false + "");
    						Game.sendPacket(scap);
    					}
    				}
    			}
    		}
		}
	}
	
	public void tick(){
		List<Component> cm = new ArrayList<Component>();
		cm.addAll(comp);
		for(Component c : cm){
			c.tick(pl);
			if(c.lastBody.getWorldCenter().distance(pl.base.getWorldCenter()) > 10) {
				System.out.println("==================");
				System.out.println("= Critical Error =");
				System.out.println("==================");
				pl.constructShip();
			}
			
			
			GameObject bod = c.lastBody;
//			System.out.println(bod.getWorldCenter() + " " + (bod.getWorldCenter().x == bod.getWorldCenter().x));
			if(bod.getWorldCenter().x != bod.getWorldCenter().x || bod.getWorldCenter().y != bod.getWorldCenter().y || bod.getWorldCenter().x < 0 ||bod.getWorldCenter().x > 60 || bod.getWorldCenter().y < 0 || bod.getWorldCenter().y > 30) {
				System.out.println("==================");
				System.out.println("= Critical Error =");
				System.out.println("==================");
				pl.constructShip();
//				pl.base.setTransform(new Transform());
//				bod.setTransform(new Transform());
				pl.setLocation(new Point2D.Float(30, 10), 0);
    		}
			
		}
	}
	
	public void render(Graphics2D g){
		List<Component> cm = new ArrayList<Component>();
		cm.addAll(comp);
		for(Component c : cm){
			c.render(g);
		}

		AffineTransform oldtrans = g.getTransform();
		AffineTransform trans = new AffineTransform();
		
		trans.concatenate(oldtrans);
		trans.scale(GameObject.SCALE, GameObject.SCALE);
//		trans.rotate(pl.base.getTransform().getRotation(), pl.getLocation().x, pl.getLocation().y);
		
		g.setTransform(trans);
	
	    g.setStroke(new BasicStroke(1f));
	    g.setTransform(oldtrans);
	    
	}
	
	public void renderNoTransform(Graphics2D g){
		List<Component> cm = new ArrayList<Component>();
		cm.addAll(comp);
		for(Component c : cm){
			double ps = c.lastBody.personalScale;
			c.lastBody.personalScale = 100;
			c.render(g);
			c.lastBody.personalScale = ps;
		}

		AffineTransform oldtrans = g.getTransform();
		AffineTransform trans = new AffineTransform();
		
		trans.concatenate(oldtrans);
//		trans.rotate(pl.base.getTransform().getRotation(), pl.getLocation().x, pl.getLocation().y);
		
		g.setTransform(trans);
	
	    g.setStroke(new BasicStroke(1f));
	    g.setTransform(oldtrans);
	    
	}

	public Component getComponent(Point selectedGrid) {
		for(Component c : comp){
			if(c.bounds.contains(selectedGrid)) return c;
		}
		return null;
	}
	
	public Component getComponentOrig(Point selectedGrid) {
		for(Component c : comp){
			if(c.origBounds.x == selectedGrid.x && c.origBounds.y == selectedGrid.y) return c;
		}
		return null;
	}
	
	public boolean removeComponent(Component c){
		
		if(c instanceof ActivatableComponent){
			((ActivatableComponent) c).deactivate();
		}
		
		return comp.remove(c);
	}
	
	public boolean addComponent(Component c){
		if(!overlaps(c.bounds)){
			comp.add(c);
			return true;
		}
		return false;
	}
	
	public boolean overlaps(Rectangle bounds){
		return overlaps(bounds.x, bounds.y, bounds.width, bounds.height);
	}
	
	public boolean overlaps(int gx, int gy, int gw, int gh){
		for(int x = gx; x < gx + gw; x++){
			for(int y = gy; y < gy + gh; y++){
				if(getComponent(new Point(x, y)) != null) return true;
			}
		}
		
		return false;
	}
	
	public boolean save(String fileName) throws IOException {
		File f = FileSystem.getFile("robots/" + fileName + ".rob");

		System.out.println("Saving ship to file: " + f.getAbsolutePath());
		
		if(f.exists()) return false;
		
		f.getParentFile().mkdirs();
		f.createNewFile();
		
		FileOutputStream fo = new FileOutputStream(f);
		fo.write(saveData());
		fo.flush();
		fo.close();
		
		return true;
	}
	
	public String saveDataString() throws IOException{
		StringWriter sw = new StringWriter();
		BufferedWriter bw = new BufferedWriter(sw);
		
		bw.write("info");
		bw.newLine();
		bw.write("size " + gridSize);
		bw.newLine();
		bw.write("key");
		bw.newLine();
		
		List<Class<? extends Component>> usedComponents = new ArrayList<Class<? extends Component>>();
		for(Component c : comp){
			if(!usedComponents.contains(c.getClass())) usedComponents.add(c.getClass());
		}
		
		bw.write("null 0");
		for(int i = 0; i < usedComponents.size(); i++){
			bw.newLine();
			bw.write(usedComponents.get(i).getSimpleName() + " " + (i + 1));
		}
		
		bw.newLine();
		bw.write("data");
		
		List<Component> used = new ArrayList<Component>();
		
		for(int y = 0; y < gridSize; y++){
			for(int x = 0; x < gridSize; x++){
				bw.newLine();
				Component c = getComponentOrig(new Point(x, y));
				if(c == null || used.contains(c)){
					bw.write("0");
				}else{
					bw.write((usedComponents.indexOf(c.getClass()) + 1) + " " + c.rot);
					if(c instanceof ActivatableComponent){
						for(int key : ((ActivatableComponent) c).actKeys){
							bw.write(" " + key);
						}
						bw.write(" /");
						for(int key : ((ActivatableComponent) c).deactKeys){
							bw.write(" " + key);
						}
					}
					used.add(c);
				}
			}
		}
		
		bw.flush();
		bw.close();
		
		System.out.println("============================");
		System.out.println(sw.toString());
		System.out.println("============================");
		
		return sw.toString();
	}
	
	public byte[] saveData() throws IOException{
		ByteArrayOutputStream bo = new ByteArrayOutputStream();
		
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(bo));
		
		bw.write("info");
		bw.newLine();
		bw.write("size " + gridSize);
		bw.newLine();
		bw.write("key");
		bw.newLine();
		
		List<Class<? extends Component>> usedComponents = new ArrayList<Class<? extends Component>>();
		for(Component c : comp){
			if(!usedComponents.contains(c.getClass())) usedComponents.add(c.getClass());
		}
		
		bw.write("null 0");
		for(int i = 0; i < usedComponents.size(); i++){
			bw.newLine();
			bw.write(usedComponents.get(i).getSimpleName() + " " + (i + 1));
		}
		
		bw.newLine();
		bw.write("data");
		
		List<Component> used = new ArrayList<Component>();
		
		for(int y = 0; y < gridSize; y++){
			for(int x = 0; x < gridSize; x++){
				bw.newLine();
				Component c = getComponentOrig(new Point(x, y));
				if(c == null || used.contains(c)){
					bw.write("0");
				}else{
					bw.write((usedComponents.indexOf(c.getClass()) + 1) + " " + c.rot);
					if(c instanceof ActivatableComponent){
						for(int key : ((ActivatableComponent) c).actKeys){
							bw.write(" " + key);
						}
						bw.write(" /");
						for(int key : ((ActivatableComponent) c).deactKeys){
							bw.write(" " + key);
						}
					}
					used.add(c);
				}
			}
		}
		
		bw.flush();
		bw.close();
		
		return bo.toByteArray();
	}
	
	public static Robot load(String fileName, Player pl) throws IOException, ClassNotFoundException, NumberFormatException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{
		return load(FileSystem.getFile("robots/" + fileName + ".rob"), pl);
	}
	
	public static Robot load(File f, Player pl) throws IOException, ClassNotFoundException, NumberFormatException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{
		
		if(f.isDirectory()) {
			System.err.println("Invalid ship: " + f.getAbsolutePath());
		}
		
		System.out.println("Loading ship: " + f.getAbsolutePath());
		Scanner sc = new Scanner(f);
		sc.useDelimiter("\\A");
		String text = sc.next();
		sc.close();
		
		return loadData(text, pl);
	}
	
	public static Robot loadData(String data, Player pl) throws IOException, ClassNotFoundException, NumberFormatException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{

		String utf8 = StandardCharsets.UTF_8.name();
		InputStream stream = new ByteArrayInputStream(data.getBytes(utf8));
		
		BufferedReader br = new BufferedReader(new InputStreamReader(stream));
		
		String state = "info";
		
		int size = 5;
		
		List<Class<? extends Component>> componentKey = new ArrayList<Class<? extends Component>>();
		
		int dataX = 0;
		int dataY = 0;
		
		List<Component> components = new ArrayList<Component>();
		
		String line;
		l: while ((line = br.readLine()) != null) {
			if(line.equals("info")) {
				state = "info";
				continue;
			}
			if(line.equals("key")) {
				state = "key";
				continue;
			}
			if(line.equals("data")) {
				state = "data";
				continue;
			}
			
			String[] str = line.split(" ");
			
			System.out.println(state);
			
			switch(state){
				case "info":
					if(str[0].equals("size")) size = Integer.parseInt(str[1]);
					break;
				case "key":
					if(str[0].equals("null")) continue l;
					String className = "me.pieking.game.robot.component." + str[0];
					@SuppressWarnings("unchecked")
					Class<? extends Component> cl = (Class<? extends Component>) Class.forName(className);
					componentKey.add(cl);
					break;
				case "data":
					
					String classKey = str[0];
					
					if(!classKey.equals("0")){
						Class<? extends Component> cls = componentKey.get(Integer.parseInt(classKey)-1);
						
						Component c = pl.createComponent(cls, dataX, dataY, Integer.parseInt(str[1]));
//						System.out.println(dataX + " " + dataY + " " + c);
						
						if(c instanceof ActivatableComponent){
							ActivatableComponent ac = (ActivatableComponent) c;
							boolean deact = false;
							for(int i = 2; i < str.length; i++){
								if(str[i].equals("/")){
									deact = true;
								}else{
									int key = Integer.parseInt(str[i]);
									(deact ? ac.actKeys : ac.deactKeys).add(key); // yum
								}
							}
						}
						
						components.add(c);
					}
					
					dataX++;
					if(dataX >= size){
						dataX -= size;
						dataY++;
					}
					break;
				
			}
			
		}
		
		br.close();
		
		
		Robot s = new Robot(size, components, pl);
		
		return s;
	}

	public Component getComponent(GameObject go) {
		for(Component c : comp){
			if(c.lastBody == go) return c;
		}
		return null;
	}

	public void translate(double x, double y) {
		for(Component c : comp){
			c.lastBody.translate(x, y);
		}
	}
	
	public void rotate(double angle, double x, double y) {
		for(Component c : comp){
			c.lastBody.rotate(angle, x, y);
		}
	}
	
	public List<Component> getComponents(){
		return comp;
	}
	
	public boolean has(Class<? extends Component> type){
		for(Component c : comp){
			if(type.isAssignableFrom(c.getClass())) return true;
		}
		return false;
	}

	public int numberOf(Class<? extends Component> type){
		int ct = 0;
		for(Component c : comp){
			if(type.isAssignableFrom(c.getClass())) ct++;
		}
		return ct;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
		
		System.out.println("enabled = " + enabled);
		
		setCanMove(enabled);
		
		if(!enabled){
			for(Component c : getComponents()){
				if(c instanceof ActivatableComponent){
					((ActivatableComponent) c).deactivate();
				}
			}
		}
	}
	
	public static void setAllEnabled(boolean enabled){
		for(Player p : Game.getWorld().getPlayers()){
			p.getRobot().setEnabled(enabled);
		}
	}

	public LuaScript getAutonScript() {
		return autonScript;
	}

	public void setAutonScript(LuaScript autonScript) {
		this.autonScript = autonScript;
	}

	public boolean canMove() {
		return canMove ;
	}
	
	public void setCanMove(boolean canMove) {
		this.canMove = canMove;
	}
	
}
