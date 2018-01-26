package me.pieking.game.menu;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import me.pieking.game.Game;
import me.pieking.game.Vars;
import me.pieking.game.gfx.Render;

public abstract class Menu {

	protected List<EButton> buttons = new ArrayList<EButton>();
		
	protected abstract void render(Graphics2D g);
	protected abstract void tick();
	
	protected Color bgCol;
	
	public static Color defaultColor = new Color(0f, 0f, 0f, 0.4f);
	
	public Menu() {
		this(defaultColor);
	}
	
	public Menu(Color bg) {
		bgCol = bg;
		init();
	}
	
	public final void iRender(Graphics2D g){
		g.setColor(bgCol);
		g.fillRect(0, 0, Game.getWidth(), Game.getHeight());
		render(g);
		
//		Vars.showCollision = true;
		
		if(Vars.showCollision && isFocused()){
    		for(EButton b : buttons){
    			if(b instanceof EToggle){
    				EToggle e = (EToggle) b;
    				
    				g.setColor(e.toggled ? Color.GREEN : Color.RED);
    				g.draw(e.getBounds());
    				g.setColor(Color.WHITE);
    				g.drawString(e.toggled ? "I" : "O", e.getBounds().x + 4, e.getBounds().y + 16);
    				
    			}else if(b instanceof ESwitch){
    				ESwitch s = (ESwitch) b;
    				
    				g.setColor(Color.YELLOW);
    				g.draw(s.getBounds());
    				g.setColor(Color.WHITE);
    				g.drawString("" + s.state, s.getBounds().x + 4, s.getBounds().y + 16);
    				
    			}else{
    				g.setColor(Color.RED);
    				g.draw(b.getBounds());
    			}
    		}
		}
		
	}
	
	public final void iTick(){
		tick();

		if(isFocused()){
    		for(int i = 0; i < buttons.size(); i++){
    			EButton b = buttons.get(i);
    			b.tick();
    		}
		}else{
			for(int i = 0; i < buttons.size(); i++){
    			EButton b = buttons.get(i);
    			b.wasClicked = true;
    		}
		}
	}
	
	public boolean open(){
		return Render.showMenu(this);
	}
	
	public boolean close(){
		return Render.hideMenu(this);
	}
	
	public EButton addButton(Rectangle rect, Runnable run){
		EButton b = new EButton(rect);
		b.addListener(run);
		buttons.add(b);
		
		return b;
	}
	
	public EButton addButton(EButton b){
		buttons.add(b);
		return b;
	}
	
	public boolean removeButton(EButton b){
		return buttons.remove(b);
	}
	
	public abstract void init();
	
	public void reload() {
		init();
	}
	
	public boolean isFocused(){
		if(!Render.getMenus().isEmpty()) return this == Render.getMenus().get(Render.getMenus().size()-1);
		return false;
	}
	
}
