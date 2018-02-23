package me.pieking.game;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;

import org.dyn4j.geometry.Vector2;

import com.studiohartman.jamepad.ControllerState;

import me.pieking.game.gfx.Fonts;
import me.pieking.game.gfx.Images;
import me.pieking.game.gfx.Sprite;
import me.pieking.game.net.packet.PortalEjectPacket;
import me.pieking.game.net.packet.SetPowerupLevelPacket;
import me.pieking.game.net.packet.UpdateCubeStoragePacket;
import me.pieking.game.net.packet.UsePowerupPacket;
import me.pieking.game.world.Balance.Team;
import me.pieking.game.world.PowerCube;
import me.pieking.game.world.TeamProperties;

public class Vault {

	public static Sprite vault_red = Images.getSprite("vault_red.png");
	public static Sprite vault_blue = Images.getSprite("vault_blue.png");
	public static Sprite exchange_red = Images.getSprite("exchange_red.png");
	public static Sprite exchange_blue = Images.getSprite("exchange_blue.png");
	public static Sprite portal_red = Images.getSprite("portal_red.png");
	public static Sprite portal_blue = Images.getSprite("portal_blue.png");
	
	private boolean draggingCube = false;
	private boolean wasMousePressed = false;
	private int hoverVault = -1;
	
	private int visibilityTimer = 0;
	private boolean controllerCubePlaceToggle = false;
	
	private Team team;
	
	private boolean lockOpen = false;
	private float scale = 2f;
	
	public Vault(Team team) {
		this.team = team;
	}
	
	public void tick() {
		boolean nowMousePressed = Game.mouseHandler().isLeftPressed();
		
		int scale = 2;
		
		Sprite vaultSpr = vault_red;
		int w = vaultSpr.getWidth() * scale;
		int h = vaultSpr.getHeight() * scale;
		
		TeamProperties prop = Game.getWorld().getProperties(team);
		
		int numCubes = prop.getCubeStorage();
		
		int padding = 20;
		Rectangle pickupCube = new Rectangle(Game.getWidth() - 80 - padding/2, Game.getHeight() - 80 - padding/2, 60 + padding, 60 + padding);
		
		ControllerState cont = Game.controllerState();
		
		if((nowMousePressed && !wasMousePressed && pickupCube.contains(Game.mouseLoc())) || (cont.startJustPressed) && numCubes > 0) {
			draggingCube = true;
			prop.setCubeStorage(prop.getCubeStorage() - 1);
			UpdateCubeStoragePacket ucsp = new UpdateCubeStoragePacket(Game.getWorld().getProperties(Team.RED).getCubeStorage() + "", Game.getWorld().getProperties(Team.BLUE).getCubeStorage() + "");
			Game.sendPacket(ucsp);
		}else if(nowMousePressed && !wasMousePressed) {
			float timer = visibilityTimer / 30f;
			if(timer > 1f) timer = 1f;
			if(timer < 0f) timer = 0f;
			
			double transY = ((h - 23*scale) * (1f-timer));
			if(transY > (h - 36*scale)) transY = (h - 36*scale);

			int hov = -1;
			for(int i = 0; i < 3; i++) {
				Rectangle r = new Rectangle(10 + 38*scale*i, Game.getHeight() - h - 10 + (int)(transY), 40 * scale, 30 * scale);
				if(r.contains(Game.mouseLoc())) hov = i;
    		}
			
			UsePowerupPacket upp = null;
			switch(hov) {
				case 0:
					upp = new UsePowerupPacket("force", team.toString());
					break;
				case 1:
					upp = new UsePowerupPacket("levitate", team.toString());
					break;
				case 2:
					upp = new UsePowerupPacket("boost", team.toString());
					break;
				default: 
					break;
			}
			
			if(upp != null) {
				Game.doPacket(upp);
			}
			
		}else if((!nowMousePressed && wasMousePressed && draggingCube) || (cont.isConnected && false && !cont.start && draggingCube)) {
			draggingCube = false;
			
			switch(hoverVault) {
				case 0:
					int forcePrev = prop.getForceLevel();
					if(forcePrev < 3) {
						SetPowerupLevelPacket splp = new SetPowerupLevelPacket("force", team.toString(), (forcePrev+1) + "");
						Game.doPacket(splp);
					}
					break;
				case 1:
					int levitatePrev = prop.getLevitateLevel();
					if(levitatePrev < 3) {
						SetPowerupLevelPacket splp = new SetPowerupLevelPacket("levitate", team.toString(), (levitatePrev+1) + "");
						Game.doPacket(splp);
					}
					break;
				case 2:
					int boostPrev = prop.getBoostLevel();
					if(boostPrev < 3) {
						SetPowerupLevelPacket splp = new SetPowerupLevelPacket("boost", team.toString(), (boostPrev+1) + "");
						Game.doPacket(splp);
					}
					break;
				case 3:
					{
						PortalEjectPacket pep = new PortalEjectPacket(team.toString(), 0 + "");
						Game.sendPacket(pep);
						if(!Game.isConnected()) Game.doPacket(pep);
					}
					break;
				case 4:
    				{
    					PortalEjectPacket pep = new PortalEjectPacket(team.toString(), 1 + "");
    					Game.sendPacket(pep);
    					if(!Game.isConnected()) Game.doPacket(pep);
    				}
					break;
				case 5:
    				{
    					PortalEjectPacket pep = new PortalEjectPacket(team.toString(), 2 + "");
    					Game.sendPacket(pep);
    					if(!Game.isConnected()) Game.doPacket(pep);
    				}
					break;
				default: 
					prop.setCubeStorage(prop.getCubeStorage() + 1);
					UpdateCubeStoragePacket ucsp = new UpdateCubeStoragePacket(Game.getWorld().getProperties(Team.RED).getCubeStorage() + "", Game.getWorld().getProperties(Team.BLUE).getCubeStorage() + "");
					Game.sendPacket(ucsp);
					break;
			}
			
			hoverVault = -1;
		}else if(nowMousePressed && draggingCube) {
    		int newHover = -1;
			for(int i = 0; i < 3; i++) {
				Rectangle r = new Rectangle(10 + 38*scale*i, Game.getHeight() - h - 10 + 192 - 36*scale*2, 40 * scale, 40 * scale + (36*scale*2));
				if(r.contains(Game.mouseLoc())) {
					newHover = i;
				}
    		}
			
			Rectangle portal1Rect = new Rectangle(Game.getWidth() - portal_red.getWidth() - 10, 10, portal_red.getWidth(), portal_red.getHeight());
			Rectangle exchangeRect = new Rectangle(Game.getWidth() - portal_red.getWidth() - exchange_red.getWidth() - 10 - 10, 10, exchange_red.getWidth(), exchange_red.getHeight());
			Rectangle portal2Rect = new Rectangle(Game.getWidth() - portal_red.getWidth()*2 - exchange_red.getWidth() - 10 - 10 - 10, 10, portal_red.getWidth(), portal_red.getHeight());
			
			if(portal1Rect.contains(Game.mouseLoc())) newHover = 3;
			if(exchangeRect.contains(Game.mouseLoc())) newHover = 4;
			if(portal2Rect.contains(Game.mouseLoc())) newHover = 5;
			
			System.out.println(newHover);
			hoverVault = newHover;
		}
		
		wasMousePressed = nowMousePressed;
		
		Rectangle vaultRect = new Rectangle(10, Game.getHeight() - (h) - 10, w, h);
		
		if(draggingCube || vaultRect.contains(Game.mouseLoc())) {
			if(visibilityTimer < 60) visibilityTimer++;
		}else {
			if(visibilityTimer > 2) visibilityTimer--;
		}
		
		if(cont.isConnected && false && draggingCube) {
			int newHover = -1;
			
			if(cont.dpadLeft) {
				newHover = 0;
			}else if(cont.dpadUp) {
				newHover = 1;
			}else if(cont.dpadRight) {
				newHover = 2;
			}
			
			if(controllerCubePlaceToggle && newHover != -1) newHover = 5 - newHover;
			
			hoverVault = newHover;
			
			if(cont.dpadDownJustPressed) {
				controllerCubePlaceToggle = !controllerCubePlaceToggle;
			}
		}else if(cont.isConnected && false && cont.back) {
			if(cont.dpadLeft) {
				Game.getWorld().useForce(team);
			}else if(cont.dpadUp) {
				Game.getWorld().useLevitate(team);
			}else if(cont.dpadRight) {
				Game.getWorld().useBoost(team);
			}
		}
		
		
		
	}
	
	public void render(Graphics2D g) {
		g.setColor(Color.CYAN);
		Sprite cube = PowerCube.spr;
		
		int scale = (int)this.scale;
		
		float timer = visibilityTimer / 30f;
		
		if(lockOpen) timer = 1f;
		
		if(timer > 1f) timer = 1f;
		if(timer < 0f) timer = 0f;
		
		Sprite vaultSpr = team == Team.BLUE ? vault_blue : vault_red;
		int w = vaultSpr.getWidth() * scale;
		int h = vaultSpr.getHeight() * scale;
		
		AffineTransform trans = g.getTransform();
		
		Shape clip = g.getClip();
		
		g.setClip(10, Game.getHeight() - (h) - 10, w, h);
		
		double transY = ((h - 23*scale) * (1f-timer));
		if(transY > (h - 36*scale)) transY = (h - 36*scale);
		
		g.translate(0, transY);
		g.drawImage(vaultSpr.getImage(), 10, Game.getHeight() - h - 10, w, h, null);
		
		TeamProperties prop = Game.getWorld().getProperties(team);
		
//		System.out.println(prop.getForceLevel());
		
		int forcePlace = hoverVault == 0 ? 1 : 0;
		for(int i = 0; i < prop.getForceLevel() + forcePlace; i++) {
			if(i >= 3) continue;
			if(forcePlace > 0 && i == prop.getForceLevel() + forcePlace - 1) {
				g.drawImage(cube.getImageAlpha(0.25f), 10, Game.getHeight() - h - 10 + (96 * scale) - 36*scale*i, 40 * scale, 40 * scale, null);
			}else {
				g.drawImage(cube.getImage(), 10, Game.getHeight() - h - 10 + (96 * scale) - 36*scale*i, 40 * scale, 40 * scale, null);
				g.setColor(Color.YELLOW);
				g.fillRect(10 + (7 * scale) + ((6 * scale) * (i+1)), Game.getHeight() - h - 10 + (7 * scale), 2*scale, 2*scale);
			}
		}
		
		int levitatePlace = hoverVault == 1 ? 1 : 0;
		for(int i = 0; i < prop.getLevitateLevel() + levitatePlace; i++) {
			if(i >= 3) continue;
			if(levitatePlace > 0 && i == prop.getLevitateLevel() + levitatePlace - 1) {
				g.drawImage(cube.getImageAlpha(0.25f), 10 + 38*scale, Game.getHeight() - h - 10 + (96 * scale) - 36*scale*i, 40 * scale, 40 * scale, null);
			}else {
				g.drawImage(cube.getImage(), 10 + 38*scale, Game.getHeight() - h - 10 + (96 * scale) - 36*scale*i, 40 * scale, 40 * scale, null);
				g.setColor(Color.YELLOW);
				g.fillRect(10 + (7 * scale) + 38*scale + scale + ((6 * scale) * (i+1)), Game.getHeight() - h - 10 + (7 * scale), 2*scale, 2*scale);
			}
		}
		
		int boostPlace = hoverVault == 2 ? 1 : 0;
		for(int i = 0; i < prop.getBoostLevel() + boostPlace; i++) {
			if(i >= 3) continue;
			if(boostPlace > 0 && i == prop.getBoostLevel() + boostPlace - 1) {
				g.drawImage(cube.getImageAlpha(0.25f), 10 + 38*scale*2, Game.getHeight() - h - 10 + (96 * scale) - 36*scale*i, 40 * scale, 40 * scale, null);
			}else {
				g.drawImage(cube.getImage(), 10 + 38*scale*2, Game.getHeight() - h - 10 + (96 * scale) - 36*scale*i, 40 * scale, 40 * scale, null);
				g.setColor(Color.YELLOW);
				g.fillRect(10 + (7 * scale) + 38*scale*2 + ((6 * scale) * (i+1)), Game.getHeight() - h - 10 + (7 * scale), 2*scale, 2*scale);
			}
		}
		
		Team force = Game.getWorld().getForce().getUsing();
		Team boost = Game.getWorld().getBoost().getUsing();
		Team force_q = Game.getWorld().getForce().getQueued();
		Team boost_q = Game.getWorld().getBoost().getQueued();

		Color blueLedCol = new Color(0.2f, 0.2f, 1f, 1f);
		Color blueLedColPulse = new Color(0.2f, 0.2f, 1f, (float) ((Math.sin(Game.getTime() / 10f) + 1f) / 2f));
		Color redLedCol = new Color(1f, 0.4f, 0.4f, 1f);
		Color redLedColPulse = new Color(1f, 0.4f, 0.4f, (float) ((Math.sin(Game.getTime() / 10f) + 1f) / 2f));
		
		if(force == team) {
			for(int i = 0; i < 5; i++) {
				g.setColor(team == Team.BLUE ? blueLedCol : redLedCol);
				g.fillRect(10 + 14+ (12 * i), Game.getHeight() - h - 10 + 14, 2*scale, 2*scale);
			}
		}else if(force_q == team) {
			for(int i = 0; i < 5; i++) {
				g.setColor(Color.DARK_GRAY);
				g.fillRect(10 + 14+ (12 * i), Game.getHeight() - h - 10 + 14, 2*scale, 2*scale);
				g.setColor(team == Team.BLUE ? blueLedColPulse : redLedColPulse);
				g.fillRect(10 + 14+ (12 * i), Game.getHeight() - h - 10 + 14, 2*scale, 2*scale);
			}
		}
		
		if(prop.getUsedLevitate()) {
			for(int i = 0; i < 5; i++) {
				g.setColor(team == Team.BLUE ? blueLedCol : redLedCol);
				g.fillRect(10 + (7*scale) + 38*scale*1 + scale + ((6*scale) * i), Game.getHeight() - h - 10 + (7*scale), 2*scale, 2*scale);
			}
		}
		
		if(boost == team) {
			for(int i = 0; i < 5; i++) {
				g.setColor(team == Team.BLUE ? blueLedCol : redLedCol);
				g.fillRect(10 + (7*scale) + 38*scale*2 + ((6*scale) * i), Game.getHeight() - h - 10 + (7*scale), 2*scale, 2*scale);
			}
		}else if(boost_q == team) {
			for(int i = 0; i < 5; i++) {
				g.setColor(Color.DARK_GRAY);
				g.fillRect(10 + (7*scale) + 38*scale*2 + ((6*scale) * i), Game.getHeight() - h - 10 + (7*scale), 2*scale, 2*scale);
				g.setColor(team == Team.BLUE ? blueLedColPulse : redLedColPulse);
				g.fillRect(10 + 14 + 38*scale*2 + (12 * i), Game.getHeight() - h - 10 + 14, 2*scale, 2*scale);
			}
		}
		
		g.setTransform(trans);
		
		g.setClip(10, Game.getHeight() - (23 * scale) - 10, w, 23 * scale);
		g.drawImage(vaultSpr.getImage(), 10, Game.getHeight() - h - 10, w, h, null);
		
		g.setClip(clip);
		
		if(!lockOpen) {
    		Sprite portal = team == Team.RED ? portal_red : portal_blue;
    		Sprite exchange  = team == Team.RED ? exchange_red : exchange_blue;
    		
    		int portal1W = portal.getWidth();
    		int portal1H = portal.getHeight();
    		if(hoverVault == 3) {
    			portal1W *= 1.2;
    			portal1H *= 1.2;
    		}
    		g.drawImage(portal.getImage(), Game.getWidth() - portal1W/2 - 40, 40 - portal1H/2, portal1W, portal1H, null);
    		
    		int exchangeW = exchange.getWidth();
    		int exchangeH = exchange.getHeight();
    		if(hoverVault == 4) {
    			exchangeW *= 1.2;
    			exchangeH *= 1.2;
    		}
    		g.drawImage(exchange.getImage(), Game.getWidth() - portal.getWidth() - exchangeW/2 - 45 - 10, 40 - exchangeH/2, exchangeW, exchangeH, null);
    		
    		int portal2W = portal.getWidth();
    		int portal2H = portal.getHeight();
    		if(hoverVault == 5) {
    			portal2W *= 1.2;
    			portal2H *= 1.2;
    		}
    		g.drawImage(portal.getImage(), Game.getWidth() - portal.getWidth() - portal2W/2 - 40 - exchange.getWidth() - 10 - 10 - 10, 40 - portal2H/2, portal2W, portal2H, null);
		}
		
		// power cube storage
		
		if(lockOpen) {
			g.drawImage(PowerCube.spr.getImage(), 20, Game.getHeight() - h - 60, 40, 40, null);
    		int numCubes = Game.getWorld().getProperties(team).getCubeStorage();
    		g.setFont(Fonts.pixeled.deriveFont(14f));
    		g.setColor(Color.DARK_GRAY);
    		g.drawString("x " + numCubes, 65, Game.getHeight() - h - 35 + 2);
    		g.setColor(Color.WHITE);
    		g.drawString("x " + numCubes, 65, Game.getHeight() - h - 35);
		}else {
    		g.drawImage(PowerCube.spr.getImage(), Game.getWidth() - 80, Game.getHeight() - 80, 60, 60, null);
    		int numCubes = Game.getWorld().getProperties(team).getCubeStorage();
    		g.setFont(Fonts.pixeled.deriveFont(14f));
    		g.setColor(Color.DARK_GRAY);
    		g.drawString("" + numCubes, Game.getWidth() - 80 + 53, Game.getHeight() - 80 + 65);
    		g.setColor(Color.WHITE);
    		g.drawString("" + numCubes, Game.getWidth() - 80 + 55, Game.getHeight() - 80 + 67);
		}

		if(draggingCube && Game.mouseHandler().isLeftPressed()) {
			int ww = 60;
			int hh = 60;
			g.drawImage(cube.getImage(), Game.mouseLoc().x - ww/2, Game.mouseLoc().y - hh/2, ww, hh, null);
		}
	}
	
	public boolean isLockedOpen() {
		return lockOpen;
	}

	public void setLockedOpen(boolean lockOpen) {
		this.lockOpen = lockOpen;
	}

	public float getScale() {
		return scale;
	}

	public void setScale(float scale) {
		this.scale = scale;
	}
	
}
