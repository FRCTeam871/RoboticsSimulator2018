package me.pieking.game.menu;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.studiohartman.jamepad.ControllerState;

import me.pieking.game.FileSystem;
import me.pieking.game.Game;
import me.pieking.game.gfx.Fonts;
import me.pieking.game.robot.Robot;
import me.pieking.game.robot.component.ComputerComponent;
import me.pieking.game.scripting.LuaScriptLoader;

public class SelectScriptMenu extends Menu {

protected List<EToggle> toggles = new ArrayList<EToggle>();

	List<String> avail;
	
	String selected;
	Point hover = new Point(0, 0);

	Robot robot;
	
	private boolean wasLeftPressed = true;
	
	public SelectScriptMenu(Robot robot) {
		super(new Color(0, 0, 0, 0));
		avail = getAvailableScripts();
		this.robot = robot;
	}
	
	private List<String> getAvailableScripts() {
		File[] files = FileSystem.getFolder("scripts").listFiles();
		List<String> av = new ArrayList<String>();
		
		for(File f : files){
			if(f.isFile() && f.getName().toLowerCase().endsWith(".lua")){
				av.add(f.getName());
			}
		}
		
		return av;
	}

	@Override
	protected void render(Graphics2D g) {
		
		int numPerColumn = 5;
		
		int gridW = avail.size() / numPerColumn + 1;
		int gridH = numPerColumn;
		
//		System.out.println(gridW + " " + gridH);
		
		int gridSizeW = 300;
		int gridSizeH = 50;
		int gridPadding = 4;
		
		int boxPadding = 10;
		
		int titleHeight = 32;
		
		int width = (gridW * (gridSizeW + gridPadding) - gridPadding) + boxPadding*2;
//		if(width < 660) width = 660;
		int height = (gridH * (gridSizeH + gridPadding) - gridPadding) + boxPadding*2 + titleHeight;
		int mx = Game.getWidth()/2 - width/2; 
		int my = Game.getHeight()/2 - height/2; 
		
		g.setColor(new Color(0.2f, 0.2f, 0.2f, 0.8f));
		g.fillRoundRect(mx, my, width, height, 20, 20);
		g.setStroke(new BasicStroke(2f));
		g.setColor(new Color(0.1f, 0.1f, 0.1f, 0.8f));
		g.drawRoundRect(mx, my, width, height, 20, 20);
		g.setStroke(new BasicStroke(1f));
		
		AffineTransform trans = g.getTransform();
		
		g.translate(mx, my);
		
		int titleYOfs = 24;
		g.setColor(Color.WHITE);
		g.setFont(Fonts.pixelmix.deriveFont(16f));
		String s = "Select a script.";
		g.drawString(s, width/2 - g.getFontMetrics().stringWidth(s)/2, titleYOfs);

		g.drawLine(width/2 - g.getFontMetrics().stringWidth(s)/2 - 4, titleYOfs + 4, width/2 + g.getFontMetrics().stringWidth(s)/2 + 4, titleYOfs + 4);
		
		
		g.translate(boxPadding, boxPadding + titleHeight);
		
		g.setTransform(trans);
		
		selected = null;
		if(!Game.controllerState().isConnected) hover = new Point(-1, -1);
//		System.out.println(hover);
		for(int x = 0; x < gridW; x++){
			for(int y = 0; y < gridH; y++){
				
				int index = y + x*gridH;
				
				Rectangle r = new Rectangle(x * (gridSizeW + gridPadding) + mx + boxPadding, y * (gridSizeH + gridPadding) + my + boxPadding + titleHeight, gridSizeW, gridSizeH);
				
				boolean hover = r.contains(Game.mouseLoc());
				
				if(Game.controllerState().isConnected) {
					hover = this.hover.y == index;
				}
				
				if(index > 0 && index <= avail.size()){
					
					if(robot.getAutonScript() != null && avail.get(index-1).equals(robot.getAutonScript().name)){
						if(hover){
							g.setColor(new Color(0.4f, 0.6f, 0.4f, 0.5f));
						}else{
							g.setColor(new Color(0.3f, 0.5f, 0.3f, 0.5f));
						}
					}else{
						if(hover){
							g.setColor(new Color(0.6f, 0.6f, 0.6f, 0.5f));
						}else{
							g.setColor(new Color(0.4f, 0.4f, 0.4f, 0.5f));
						}
					}
					
    				g.setStroke(new BasicStroke(2f));
    				g.fill3DRect(r.x, r.y, r.width, r.height, true);
    				g.fill3DRect(r.x+1, r.y+1, r.width-2, r.height-2, true);
    				g.setStroke(new BasicStroke(1f));
				}else if(index <= avail.size()){
					
					if(hover){
						g.setColor(new Color(0.6f, 0.4f, 0.4f, 0.5f));
					}else{
						g.setColor(new Color(0.5f, 0.3f, 0.3f, 0.5f));
					}
					
					g.setStroke(new BasicStroke(2f));
    				g.fill3DRect(r.x, r.y, r.width, r.height, true);
    				g.fill3DRect(r.x+1, r.y+1, r.width-2, r.height-2, true);
    				g.setStroke(new BasicStroke(1f));
				}
				
				if(hover) this.hover = new Point(x, y);
				
				if(index < avail.size()+1){
					
					AffineTransform t = g.getTransform();
					g.setFont(Fonts.pixelmix.deriveFont(32f));
					String num = "" + (index > 0 ? avail.get(index-1) : "None");
					
					num = num.replace("numpad", "NUM");
					
					g.setStroke(new BasicStroke(8.0f));
					if(num.length() > 1){
						g.setFont(Fonts.pixelmix.deriveFont(16f));
						g.setStroke(new BasicStroke(4.0f));
					}
					
					GlyphVector gv = g.getFont().createGlyphVector(g.getFontRenderContext(), num);
					Shape shape = gv.getOutline();
					g.setColor(Color.BLACK);
					g.translate(r.x + r.width/2 - g.getFontMetrics().stringWidth(num)/2 + 2, r.y + r.height/2 + g.getFont().getSize()/2);
					g.draw(shape);
					g.setTransform(t);
					
					g.setStroke(new BasicStroke(1.0f));
					
					g.setColor(Color.WHITE);
					g.drawString(num, r.x + r.width/2 - g.getFontMetrics().stringWidth(num)/2 + 2, r.y + r.height/2 + g.getFont().getSize()/2);
					
					if(hover) selected = num;
				}
				
			}
		}
		
//		System.out.println(hover + " " + selected);
		
//		hover = new Point(1, 0);
		
//		int textYOfs = 22;
//		int textSpacing = 24;
//		
//		g.setColor(Color.WHITE);
//		g.setFont(Fonts.pixelmix.deriveFont(16f));
//		String s = comp.getDisplayName();
//		g.drawString(s, width/2 - g.getFontMetrics().stringWidth(s)/2, Game.getHeight() - height + textYOfs);
//
//		g.drawLine(width/2 - g.getFontMetrics().stringWidth(s)/2 - 4, Game.getHeight() - height + textYOfs + 4, width/2 + g.getFontMetrics().stringWidth(s)/2 + 4, Game.getHeight() - height + textYOfs + 4);
//		
//		int prevWidth = 100;
//		
//		Rectangle r = new Rectangle(width/2 - prevWidth/2, Game.getHeight() - height + textYOfs + 14, prevWidth, prevWidth);
//		
//		Shape cl = g.getClip();
//		g.setClip(r);
//		
//		g.setColor(Color.BLACK);
//		g.fill(r);
//		
//		g.setColor(Color.WHITE);
//		g.setFont(Fonts.pixelmix.deriveFont(12f));
//		
//		AffineTransform tr = g.getTransform();
////		g.scale(10, 10);
//		g.translate(r.getCenterX(), r.getCenterY());
////		comp.lastBody.personalScale = 100;
////		comp.lastBody.renderNoTranslate(g, new BasicStroke(1f));
////		comp.lastBody.personalScale = Float.MAX_VALUE;
//		
//		double scale = Math.pow(zoom / 6f, 2) + 15;
//		
//		double xOffset = -comp.lastBody.getWorldCenter().x * scale;
//		double yOffset = -comp.lastBody.getWorldCenter().y * scale;
//		
//		Game.getWorld().render(g, xOffset, yOffset, scale);
//		
//		g.setTransform(tr);
//		
//		g.setClip(cl);
//		
//		g.setColor(Color.WHITE);
//		g.setFont(Fonts.pixelmix.deriveFont(20f));
//		//(int)r.getMaxX() + 4, r.y, 20, 20
//		g.drawString("+", (int)r.getMaxX() + 8, r.y + 18);
//		g.drawString("-", (int)r.getMaxX() + 8, r.y + 44);
//		
//		int infoIndex = 2;
//		
//		g.setColor(Color.WHITE);
//		g.setFont(Fonts.pixelmix.deriveFont(16f));
//		double hp = comp.health / comp.maxHealth;
//		s = "HP: " + Math.round(comp.health) + "/" + Math.round(comp.maxHealth) + " (" + Math.round(hp * 100) + "%)";
//		g.drawString(s, width/2 - g.getFontMetrics().stringWidth(s)/2, Game.getHeight() - height + textYOfs + prevWidth + (textSpacing * (infoIndex++)));
//		
//		if(comp instanceof ThrusterComponent){
//			ThrusterComponent tc = (ThrusterComponent) comp;
//			
//    		s = "Force = " + Math.round(tc.force * 10.) / 10. + " MN";
//    		g.drawString(s, width/2 - g.getFontMetrics().stringWidth(s)/2, Game.getHeight() - height + textYOfs + prevWidth + (textSpacing * (infoIndex++)));
//		}
		
		
//		g.setFont(Fonts.pixelmix.deriveFont(14f));
//		g.setColor(buttons.get(0).getBounds().contains(Game.mouseLoc()) ? Color.YELLOW : Color.WHITE);
//		g.drawString("<-", 16, 32);
		
	}

	@Override
	protected void tick() {
		boolean nowLeftPressed = Game.mouseHandler().isLeftPressed();
		
		ControllerState cont = Game.controllerState();
		if(cont.isConnected) {
			if(cont.a) nowLeftPressed = true;
		}
		
		if(nowLeftPressed && !wasLeftPressed){
			if(selected != null){
				System.out.println("selected: " + selected);
				if(selected.equals("None")){
					robot.setAutonScript(null);
				}else{
					robot.setAutonScript(LuaScriptLoader.runScript(selected));
				}
			
				close();
			}
		}
		
		wasLeftPressed = nowLeftPressed;
		
		
		if(cont.isConnected) {
			if(cont.dpadDownJustPressed) {
				if(hover.x == -1) {
					hover = new Point(0, 0);
				}else {
					int newHover = hover.y + 1;
					if(newHover < 0) newHover += avail.size()+1;
					if(newHover > avail.size()) newHover -= avail.size()+1;
					System.out.println(newHover);
					hover = new Point(0, newHover);
				}
			}else if(cont.dpadUpJustPressed) {
				if(hover.x == -1) {
					hover = new Point(0, 0);
				}else {
					int newHover = hover.y - 1;
					if(newHover < 0) newHover += avail.size()+1;
					if(newHover > avail.size()) newHover -= avail.size()+1;
					System.out.println(newHover);
					hover = new Point(0, newHover);
				}
			}
		}
		
	}

	@Override
	public void init() {
		buttons.clear();
		
		addButton(new Rectangle(10, 10, 10, 10), new Runnable() {
			public void run() {
				close();
			}
		});
		
	}
	
	public EToggle addToggle(Rectangle bounds, ToggleRunnable t){
		EToggle tog = new EToggle(bounds);
		tog.addToggleListener(t);
		addButton(tog);
		return tog;
	}
	
	public EToggle addToggle(Rectangle bounds, ToggleRunnable t, boolean toggled){
		EToggle tog = addToggle(bounds, t);
		tog.state = toggled ? 1 : 0;
		tog.toggled = toggled;
		return tog;
	}
	
	public ESwitch addSwitch(Rectangle bounds, ToggleRunnable t, int maxState){
		ESwitch tog = new ESwitch(bounds, maxState);
		tog.addToggleListener(t);
		addButton(tog);
		return tog;
	}
	
	public ESwitch addSwitch(Rectangle bounds, ToggleRunnable t, int maxState, int currState){
		ESwitch tog = addSwitch(bounds, t, maxState);
		tog.state = currState;
		return tog;
	}
	
	public boolean insideMenu(Point p){
		int height = 400;
		int width = 300;
		
		return new Rectangle(0, Game.getHeight() - height, width, height).contains(p);
	}
	
}
