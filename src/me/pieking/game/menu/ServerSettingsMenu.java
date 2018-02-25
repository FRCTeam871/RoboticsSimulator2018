package me.pieking.game.menu;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import me.pieking.game.Game;
import me.pieking.game.Settings;
import me.pieking.game.gfx.Fonts;
import me.pieking.game.net.ServerStarter;
import me.pieking.game.net.packet.UpdateSettingsPacket;
import me.pieking.game.sound.Sound;

public class ServerSettingsMenu extends Menu {

protected List<EToggle> toggles = new ArrayList<EToggle>();
	
	private ESwitch playerUpdateInterval;
	private ESwitch cubeUpdateInterval;
	
	private int baseX;
	private int baseY;
	private int yInc;
	
	public ServerSettingsMenu() {
		super(Color.BLACK);
	}
	
	@Override
	protected void render(Graphics2D g) {
		g.setColor(Color.WHITE);
		g.setFont(Fonts.gamer.deriveFont(55f));
		g.drawString("Settings", Game.getWidth()/2 - g.getFontMetrics().stringWidth("Settings")/2, 35);
		
		g.drawLine(Game.getWidth()/2 - 100, 45, Game.getWidth()/2 + 100, 45);
		
		g.setColor(Color.WHITE);
		g.setFont(Fonts.pixelmix.deriveFont(12f));
		
		int padding = 10;
		
//		String msg = "Day/Night Cycle";
//		g.drawString(msg, baseX - padding - g.getFontMetrics().stringWidth(msg), baseY + (yInc * 0) + 16);
		
//		msg = "(" + (Settings.dayNightCycle ? "On" : "Off") + ")";
//		g.drawString(msg, baseX + 20 + padding, baseY + (yInc * 0) + 16);
//		
//		
		String msg = "Player Update Interval:";
		g.drawString(msg, baseX - padding - g.getFontMetrics().stringWidth(msg), baseY + (yInc * 0) + 16);
		msg = "-";
		g.drawString(msg, baseX - padding + 16, baseY + (yInc * 0) + 16);
		msg = "+";
		g.drawString(msg, baseX - padding + 76, baseY + (yInc * 0) + 16);
		msg = Settings.playerUpdateInterval + "t";
		g.drawString(msg, baseX - padding + 40, baseY + (yInc * 0) + 16);
		
		msg = "Cube Update Interval:";
		g.drawString(msg, baseX - padding - g.getFontMetrics().stringWidth(msg), baseY + (yInc * 1) + 16);
		msg = "-";
		g.drawString(msg, baseX - padding + 16, baseY + (yInc * 1) + 16);
		msg = "+";
		g.drawString(msg, baseX - padding + 76, baseY + (yInc * 1) + 16);
		msg = Settings.cubeUpdateInterval + "t";
		g.drawString(msg, baseX - padding + 40, baseY + (yInc * 1) + 16);
		
//		msg = "(" + Settings.nameTags.display() + ")";
//		g.drawString(msg, baseX + 20 + padding, baseY + (yInc * 1) + 16);
//		
//		
//		msg = "Damage Bar Visibility";
//		g.drawString(msg, baseX - padding - g.getFontMetrics().stringWidth(msg), baseY + (yInc * 2) + 16);
//		
//		msg = "(" + Settings.nameTagDamages.display() + ")";
//		g.drawString(msg, baseX + 20 + padding, baseY + (yInc * 2) + 16);
//		
//		
//		msg = "Player Tag Visibility";
//		g.drawString(msg, baseX - padding - g.getFontMetrics().stringWidth(msg), baseY + (yInc * 3) + 16);
//		
//		msg = "(" + Settings.playerNameTags.display() + ")";
//		g.drawString(msg, baseX + 20 + padding, baseY + (yInc * 3) + 16);
//		
//		msg = "Volume";
//		g.drawString(msg + "  -  +", baseX - 14 - padding - g.getFontMetrics().stringWidth(msg), baseY + (yInc * 4) + 16);
//		
//		msg = "(" + (int)(Sound.soundSystem.getVolume() * 100) + "%)";
//		g.drawString(msg, baseX + 20 + 14 + padding, baseY + (yInc * 4) + 16);
//		
//		msg = "Window Scale";
//		g.drawString(msg, baseX - padding - g.getFontMetrics().stringWidth(msg), baseY + (yInc * 5) + 16);
//		msg = "(" + Game.getScale() + "x)";
//		g.drawString(msg, baseX + 20 + padding, baseY + (yInc * 5) + 16);
		
		g.setFont(Fonts.pixelmix.deriveFont(14f));
		g.setColor(buttons.get(0).getBounds().contains(Game.mouseLoc()) ? Color.YELLOW : Color.WHITE);
		g.drawString("<-", 16, 32);
	}

	@Override
	protected void tick() {
		
	}

	@Override
	public void init() {
		buttons.clear();
		
		baseX = Game.getWidth()/2 + 50;
		baseY = 60;
		yInc  = 30;
		
		addButton(new Rectangle(10, 10, 32, 32), new Runnable() {
			public void run() {
				Settings.saveConfig();
				String settings = Settings.save();
				System.out.println("Update Settings: " + settings);
				UpdateSettingsPacket usp = new UpdateSettingsPacket(settings);
				ServerStarter.serverStarter.sendToAll(usp);
				close();
			}
		});
		
		if(Game.debug()){
    		addButton(new Rectangle(0, 0, 5, 5), new Runnable() {
    			public void run() {
    				reload();
    			}
    		});
		}
	
		//===================== settings buttons
		
//		System.out.println(baseX + " " + baseY + " " + yInc);
		
//		dayNight = addToggle(new Rectangle(baseX, baseY, 20, 20), new ToggleRunnable() {
//			@Override
//			public void toggle(int state) {
//				Settings.dayNightCycle = (state == 1);
////				System.out.println(Settings.dayNightCycle);
//			}
//		}, Settings.dayNightCycle);
//		
//		playerUpdateInterval = addSwitch(new Rectangle(baseX, baseY + (yInc * 0), 20, 20), 
//				state -> Settings.playerUpdateInterval = state * 5 + 1
//				, 2, (Settings.playerUpdateInterval - 1) / 5);
		
		addButton(new Rectangle(baseX, baseY + (yInc * 0), 20, 20), () -> {
			int mod = Game.keyHandler().isPressed(KeyEvent.VK_SHIFT) ? 5 : 1;
			Settings.playerUpdateInterval = Math.max(1, Math.min(Settings.playerUpdateInterval - mod, 60));
		});
		
		addButton(new Rectangle(baseX + 60, baseY + (yInc * 0), 20, 20), () -> {
			int mod = Game.keyHandler().isPressed(KeyEvent.VK_SHIFT) ? 5 : 1;
			Settings.playerUpdateInterval = Math.max(1, Math.min(Settings.playerUpdateInterval + mod, 60));
		});
		
		addButton(new Rectangle(baseX, baseY + (yInc * 1), 20, 20), () -> {
			int mod = Game.keyHandler().isPressed(KeyEvent.VK_SHIFT) ? 5 : 1;
			Settings.cubeUpdateInterval = Math.max(1, Math.min(Settings.cubeUpdateInterval - mod, 60));
		});
		
		addButton(new Rectangle(baseX + 60, baseY + (yInc * 1), 20, 20), () -> {
			int mod = Game.keyHandler().isPressed(KeyEvent.VK_SHIFT) ? 5 : 1;
			Settings.cubeUpdateInterval = Math.max(1, Math.min(Settings.cubeUpdateInterval + mod, 60));
		});
		
//		nameTagDamage = addSwitch(new Rectangle(baseX, baseY + (yInc * 2), 20, 20), 
//			state -> Settings.nameTagDamages = NameTagVisibility.values()[state]
//		, 2, Settings.nameTagDamages.getCode());
//		
//		
//		playerNameTags = addSwitch(new Rectangle(baseX, baseY + (yInc * 3), 20, 20), 
//			state -> Settings.playerNameTags = NameTagVisibility.values()[state]
//		, 2, Settings.playerNameTags.getCode());
//		
//		addButton(new Rectangle(baseX - 14, baseY + (yInc * 4), 20, 20), () -> {
//			Sound.soundSystem.setVolume(Sound.soundSystem.getVolume() - 0.1f);
//			Sound.soundTest();
//		});
//		
//		addButton(new Rectangle(baseX + 14, baseY + (yInc * 4), 20, 20), () -> {
//			Sound.soundSystem.setVolume(Sound.soundSystem.getVolume() + 0.1f);
//			Sound.soundTest();
//		});
//		
//		playerNameTags = addSwitch(new Rectangle(baseX, baseY + (yInc * 5), 20, 20), 
//			state -> Game.setScale(state + 1, true)
//		, 1, Game.getScale() - 1);
		
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
	
}
