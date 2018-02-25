package me.pieking.game.menu;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.List;

import me.pieking.game.Game;
import me.pieking.game.Utils;
import me.pieking.game.gfx.Fonts;
import me.pieking.game.gfx.Images;
import me.pieking.game.gfx.Render;
import me.pieking.game.gfx.Sprite;
import me.pieking.game.robot.component.ActivatableComponent;
import me.pieking.game.robot.component.Component;
import me.pieking.game.robot.component.ComputerComponent;

public class ComponentInfoMenu extends Menu {

	public static Sprite keyboardIcon = Images.getSprite("keyboard.png");
	public static Sprite scriptIcon = Images.getSprite("script.png");
	
	protected List<EToggle> toggles = new ArrayList<EToggle>();

	Component comp = null;
	
	public double zoom = 50;
	
	public EButton editActKeys = null;
	public EButton editDeactKeys = null;
	public EButton editScript = null;
	
	public ComponentInfoMenu(Component c) {
		super(new Color(0, 0, 0, 0));
		this.comp = c;
		reload();
	}
	
	@Override
	protected void render(Graphics2D g) {
		
		int height = 400;
		int width = 300;
		
		g.setColor(new Color(0.2f, 0.2f, 0.2f, 0.7f));
		g.fillRect(0, Game.getHeight() - height, width, height);
		
		int textYOfs = 22;
		int textSpacing = 24;
		
		g.setColor(Color.WHITE);
		g.setFont(Fonts.pixelmix.deriveFont(16f));
		String s = comp.getDisplayName();
		g.drawString(s, width/2 - g.getFontMetrics().stringWidth(s)/2, Game.getHeight() - height + textYOfs);

		g.drawLine(width/2 - g.getFontMetrics().stringWidth(s)/2 - 4, Game.getHeight() - height + textYOfs + 4, width/2 + g.getFontMetrics().stringWidth(s)/2 + 4, Game.getHeight() - height + textYOfs + 4);
		
		int prevWidth = 100;
		
		Rectangle r = new Rectangle(width/2 - prevWidth/2, Game.getHeight() - height + textYOfs + 14, prevWidth, prevWidth);
		
		Shape cl = g.getClip();
		g.setClip(r);
		
		g.setColor(Color.BLACK);
		g.fill(r);
		
		g.setColor(Color.WHITE);
		g.setFont(Fonts.pixelmix.deriveFont(12f));
		
		AffineTransform tr = g.getTransform();
//		g.scale(10, 10);
		g.translate(r.getCenterX(), r.getCenterY());
//		comp.lastBody.personalScale = 100;
//		comp.lastBody.renderNoTranslate(g, new BasicStroke(1f));
//		comp.lastBody.personalScale = Float.MAX_VALUE;
		
		double scale = Math.pow(zoom / 6f, 2) + 15;
		
		double xOffset = -comp.lastBody.getWorldCenter().x * scale;
		double yOffset = -comp.lastBody.getWorldCenter().y * scale;
		
		Game.getWorld().render(g, xOffset, yOffset, scale);
		
		g.setTransform(tr);
		
		g.setClip(cl);
		
		g.setColor(Color.WHITE);
		g.setFont(Fonts.pixelmix.deriveFont(20f));
		//(int)r.getMaxX() + 4, r.y, 20, 20
		g.drawString("+", (int)r.getMaxX() + 8, r.y + 18);
		g.drawString("-", (int)r.getMaxX() + 8, r.y + 44);
		
		List<String> info = new ArrayList<String>();
		
		g.setColor(Color.WHITE);
		g.setFont(Fonts.pixelmix.deriveFont(16f));
		double hp = comp.health / comp.maxHealth;
		info.add("HP: " + Math.round(comp.health) + "/" + Math.round(comp.maxHealth) + " (" + Math.round(hp * 100) + "%)");
		
		if(comp instanceof ActivatableComponent){
			ActivatableComponent ac = (ActivatableComponent) comp;
			
			String k = "";
			for(int i = 0; i < ac.actKeys.size(); i++){
				k = k + KeyEvent.getKeyText(ac.actKeys.get(i)).toLowerCase() + ",";
			}
//			System.out.println(k);
			String ch = (k.length() > 0 ? k.substring(0, k.length()-1) : "");
			ch = ch.replace("numpad", "NUM");
			ch = Utils.addElipses(ch, 10);
			if(ac.toggleMode){
				info.add("Toggle: " + ch);
			}else{
    			info.add("Activate: " + ch);
    			
    			k = "";
    			for(int i = 0; i < ac.deactKeys.size(); i++){
    				k = k + KeyEvent.getKeyText(ac.deactKeys.get(i)).toLowerCase() + ",";
    			}
    			ch = (k.length() > 0 ? k.substring(0, k.length()-1) : "");
    			ch = ch.replace("numpad", "NUM");
    			ch = Utils.addElipses(ch, 9);
    			info.add("Deactivate: " + ch);
			}
			
		}
		
		if(comp instanceof ComputerComponent){
			info.add("Script: " + (((ComputerComponent)comp).script != null ? ((ComputerComponent)comp).script.name : "null"));
		}
		
		for(int i = 0; i < info.size(); i++){
			String st = info.get(i);
			
			int xOfs = 0;
		
			float iconAlpha = Game.getTime() % 60 >= 30 ? 0.6f : 1f;
			if(st.startsWith("Toggle:")){
				xOfs = 14;
				int kx = width/2 - g.getFontMetrics().stringWidth(st)/2 - 28 + xOfs;
				int ky = Game.getHeight() - height + textYOfs + prevWidth + (textSpacing * (i+2)) - 13;
				Rectangle b = editActKeys.getBounds();
				b.x = kx - 2;
				b.y = ky - 2;
				editActKeys.setBounds(b);
				g.drawImage(keyboardIcon.getImageAlpha(iconAlpha), kx, ky, null);
			}else if(st.startsWith("Activate:")){
				xOfs = 14;
				int kx = width/2 - g.getFontMetrics().stringWidth(st)/2 - 28 + xOfs;
				int ky = Game.getHeight() - height + textYOfs + prevWidth + (textSpacing * (i+2)) - 13;
				Rectangle b = editActKeys.getBounds();
				b.x = kx - 2;
				b.y = ky - 2;
				editActKeys.setBounds(b);
				g.drawImage(keyboardIcon.getImageAlpha(iconAlpha), kx, ky, null);
			}else if(st.startsWith("Deactivate:")){
				xOfs = 14;
				int kx = width/2 - g.getFontMetrics().stringWidth(st)/2 - 28 + xOfs;
				int ky = Game.getHeight() - height + textYOfs + prevWidth + (textSpacing * (i+2)) - 13;
				Rectangle b = editDeactKeys.getBounds();
				b.x = kx - 2;
				b.y = ky - 2;
				editDeactKeys.setBounds(b);
				g.drawImage(keyboardIcon.getImageAlpha(iconAlpha), kx, ky, null);
			}else if(st.startsWith("Script:")){
				xOfs = 14;
				int kx = width/2 - g.getFontMetrics().stringWidth(st)/2 - 28 + xOfs;
				int ky = Game.getHeight() - height + textYOfs + prevWidth + (textSpacing * (i+2)) - 13;
				Rectangle b = editScript.getBounds();
				b.x = kx - 2;
				b.y = ky - 2;
				editScript.setBounds(b);
				g.drawImage(scriptIcon.getImageAlpha(iconAlpha), kx, ky, null);
			}
		
			g.drawString(st, width/2 - g.getFontMetrics().stringWidth(st)/2 + xOfs, Game.getHeight() - height + textYOfs + prevWidth + (textSpacing * (i+2)));
			
		}
		
//		g.setFont(Fonts.pixelmix.deriveFont(14f));
//		g.setColor(buttons.get(0).getBounds().contains(Game.mouseLoc()) ? Color.YELLOW : Color.WHITE);
//		g.drawString("<-", 16, 32);
		
	}

	@Override
	protected void tick() {
		
	}

	@Override
	public void init() {
		buttons.clear();
		
		int height = 400;
		int width = 300;
		
		int textYOfs = 22;
		
		int prevWidth = 100;
		
		Rectangle r = new Rectangle(width/2 - prevWidth/2, Game.getHeight() - height + textYOfs + 14, prevWidth, prevWidth);
		
		
		addButton(new Rectangle((int)r.getMaxX() + 4, r.y, 20, 20), new Runnable() {
			public void run() {
				zoom = Math.min(zoom + 10, 70);
			}
		});
		
		addButton(new Rectangle((int)r.getMaxX() + 4, r.y + 24, 20, 20), new Runnable() {
			public void run() {
				zoom = Math.max(zoom - 10, 10);
			}
		});
		
		if(comp instanceof ActivatableComponent){
			editActKeys = addButton(new Rectangle(0, 0, 24, 16), new Runnable() {
				public void run() {
					EditKeysMenu km = new EditKeysMenu(((ActivatableComponent) comp).actKeys);
					Render.showMenu(km);
				}
			});
			
			editDeactKeys = addButton(new Rectangle(0, 0, 24, 16), new Runnable() {
				public void run() {
					EditKeysMenu km = new EditKeysMenu(((ActivatableComponent) comp).deactKeys);
					Render.showMenu(km);
				}
			});
		}
		
//		
//		if(Game.debug()){
//    		addButton(new Rectangle(0, 0, 5, 5), new Runnable() {
//    			public void run() {
//    				reload();
//    			}
//    		});
//		}
		
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
