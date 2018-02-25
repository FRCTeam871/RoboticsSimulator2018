package me.pieking.game.gfx;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

import me.pieking.game.Game;
import me.pieking.game.menu.Menu;

public class Render {

	private static List<Menu> menus = new ArrayList<Menu>();
	
	public static void render(Disp d) {
		
		Graphics2D g = d.getRenderGraphics();
		
		g.clearRect(0, 0, d.getWidth(), d.getHeight());
		
		g.setColor(Color.DARK_GRAY);
		g.fillRect(0, 0, d.getWidth(), d.getHeight());
		
		g.setColor(Color.GREEN);
		g.drawRect((int) (100 + Math.sin(Game.getTime()/10f) * 10), (int) (100 + Math.cos(Game.getTime()/10f) * 10), 20, 20);

		try{
			Game.getWorld().render(g);
		}catch(Exception e){
			e.printStackTrace();
		}
		
		for(int i = 0; i < menus.size(); i++){
			Menu m = menus.get(i);
			m.iRender(g);
		}
		
//		int rot = (int)((Game.getTime() / 30f) % 4)*90;
//		Rectangle2D r2d = new Float(0, 0, 2, 1);
//		AffineTransform rotate = AffineTransform.getRotateInstance(Math.toRadians(rot), 0.5, 0.5);
//		
//		Shape s = rotate.createTransformedShape(r2d);
//		
//		Rectangle2D r2d2 = rotate.createTransformedShape(r2d).getBounds2D();
//		g.scale(10, 10);
//		g.translate(10, 10);
//		
//		g.setStroke(new BasicStroke(0.2f));
//		g.setColor(Color.RED);
//		g.draw(r2d);
//		g.setColor(Color.GREEN);
//		g.draw(s);
//		g.setColor(Color.BLUE);
//		g.draw(r2d2);
//		g.setColor(Color.CYAN);
//		g.draw(r2d2.getBounds());
//		
//		System.out.println(r2d2.getBounds());
		
	}

	public static boolean showMenu(Menu m){
		return menus.add(m);
	}
	
	public static boolean hideMenu(Menu m){
		return menus.remove(m);
	}
	
	public static void hideAllMenus(){
		List<Menu> mn = new ArrayList<Menu>();
		mn.addAll(menus);
		
		for(Menu m : mn){
			m.close();
		}
		menus.clear();
	}
	
	public static List<Menu> getMenus(){
		return menus;
	}
	
}
