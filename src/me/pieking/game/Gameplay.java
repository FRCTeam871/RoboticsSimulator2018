package me.pieking.game;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.event.KeyEvent;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import me.pieking.game.gfx.Fonts;
import me.pieking.game.gfx.Render;
import me.pieking.game.menu.SelectScriptMenu;
import me.pieking.game.net.ServerStarter;
import me.pieking.game.net.packet.ChoseAutonPacket;
import me.pieking.game.net.packet.PlayerUpdatePacket;
import me.pieking.game.net.packet.SetGameTimePacket;
import me.pieking.game.net.packet.SetStatePacket;
import me.pieking.game.net.packet.SetTeamPacket;
import me.pieking.game.net.packet.UpdateScorePacket;
import me.pieking.game.net.packet.VotePacket;
import me.pieking.game.robot.Robot;
import me.pieking.game.robot.component.ClawGrabberComponent;
import me.pieking.game.robot.component.Component;
import me.pieking.game.scripting.LuaScript;
import me.pieking.game.sound.Sound;
import me.pieking.game.sound.SoundClip;
import me.pieking.game.world.Balance.Team;
import me.pieking.game.world.Player;

public class Gameplay {

	private static SoundClip s_matchEnd = Sound.loadSound("Match End_normalized.wav");
	private static SoundClip s_matchPause = Sound.loadSound("Match Pause_normalized.wav");
	private static SoundClip s_startAuton = Sound.loadSound("Start Auto_normalized.wav");
	private static SoundClip s_startEndgame = Sound.loadSound("Start of End Game_normalized.wav");
	private static SoundClip s_startTeleop = Sound.loadSound("Start Teleop_normalized.wav");
	
	private static final Point2D[] redSpawns;
	static{
		redSpawns = new Point2D[3];
		redSpawns[0] = new Point2D.Double(10.2, 6);
		redSpawns[1] = new Point2D.Double(10.2, 16);
		redSpawns[2] = new Point2D.Double(10.2, 20);
	}
	private static final Point2D[] blueSpawns;
	static{
		blueSpawns = new Point2D[3];
		blueSpawns[0] = new Point2D.Double(54.6, 6);
		blueSpawns[1] = new Point2D.Double(54.6, 11.5);
		blueSpawns[2] = new Point2D.Double(54.6, 20);
	}

	private List<Player> redPlayers = new ArrayList<Player>();
	private List<Player> bluePlayers = new ArrayList<Player>();

	private List<Player> voted = new ArrayList<Player>();
	private List<Player> readyToStart = new ArrayList<Player>();
	private List<Player> crossedAutoLine = new ArrayList<Player>();
	
	private int gameTime = ((2 * 60) + 30) * 60;
	
	private GameState state;
	{
		setState(GameState.WAITING_FOR_PLAYERS);
	}
	
	public void tick() {
		
		switch (state) {
			case WAITING_FOR_PLAYERS:
//				if(gameTime <= 0){
//					setState(GameState.SETUP);
//				}
				
				if(Game.controllerState().aJustPressed) {
					voteToStart(Game.getWorld().getSelfPlayer());
				}
				
				int numVoted = 0;
				List<Player> vote = new ArrayList<Player>();
				vote.addAll(voted);
				for(Player p : vote){
					if(Game.getWorld().getPlayers().contains(p)){
						numVoted++;
					}else{
						voted.remove(p);
					}
				}
				
				if(numVoted / (double)Game.getWorld().getPlayers().size() >= 0.5){
					setState(GameState.SETUP);
				}
				
				break;
			case SETUP:
				if(gameTime <= 0 || Game.GAMEPLAY_DEBUG){
					setState(GameState.AUTON);
				}
				
				if(readyToStart.size() < Game.getWorld().getPlayers().size()) {
					setGameTime(2 * 60);
				}
				
				break;
			case AUTON:
				if(Game.keyHandler().isPressed(KeyEvent.VK_F10)) setState(GameState.TELEOP);
				if(gameTime <= 0){
					setState(GameState.TELEOP);
				}else{
					for(Player p : Game.getWorld().getPlayers()) {
						if(crossedAutoLine.contains(p)) continue;
						if(p.base.isInContact(Game.getWorld().getAutoLine())) {
							crossedAutoLine.add(p);
							Game.getWorld().getProperties(p.team).addScore(5);
						}
					}
				}
				break;
			case TELEOP:
				if(Game.keyHandler().isPressed(KeyEvent.VK_F9)) setState(GameState.MATCH_END);
				if(gameTime <= 0){
//					setState(GameState.MATCH_END);
				}
				
				if(gameTime == 30 * 60) {
					s_startEndgame.stop();
					s_startEndgame.start();
				}
				
				break;
			case MATCH_END:
				if(gameTime <= 0){
					setState(GameState.WAITING_FOR_PLAYERS);
				}
				break;
			default:
				break;
		}
		
		if(Game.isServer() || !Game.isConnected()) {
    		// decrease the remaining game time if its more than 0
    		if(gameTime > 0) gameTime--;
    		
    		if(Game.isServer()) {
        		SetGameTimePacket sgtp = new SetGameTimePacket(gameTime + "");
        		ServerStarter.serverStarter.sendToAll(sgtp);
    		}
		}
	}
	
	/**
	 * Renders the HUD, including game time, scores, active power ups, power cube storage, etc.
	 * @param g - the {@link Graphics2D} to render onto.
	 */
	public void renderHUD(Graphics2D g) {
		
		if(Robot.buildMode){
			g.setFont(Fonts.gamer.deriveFont(40f));
			g.setColor(Color.WHITE);
			g.drawString("BUILD MODE", 10, 44);
			
			g.setColor(new Color(0.2f, 0.2f, 0.2f, 0.7f));
			g.fillRect(Game.getWidth() - 100, Game.getHeight() - 100, 100, 100);
			
			Player p = Game.getWorld().getSelfPlayer();
			if(p != null){
				if(p.buildSelected != null && p.buildPreview != null){
					AffineTransform trans = g.getTransform();
					g.translate(Game.getWidth() - 50, Game.getHeight() - 50);
					p.buildPreview.renderScaled(g);
					g.setTransform(trans);
					
					String num = "" + p.inventory.get(p.buildSelected);
					
					g.setFont(Fonts.pixelmix.deriveFont(20f));
					int x = Game.getWidth() - g.getFontMetrics().stringWidth(num) - 8;
					
					GlyphVector gv = g.getFont().createGlyphVector(g.getFontRenderContext(), num);
					Shape shape = gv.getOutline();
					g.setStroke(new BasicStroke(4.0f));
					g.setColor(Color.BLACK);
					g.translate(x, Game.getHeight() - 10);
					g.draw(shape);
					
					g.setStroke(new BasicStroke(1.0f));
					g.setColor(Color.WHITE);
					g.drawString(num, 0, 0);
					
					g.setTransform(trans);
				}
				
			}
		}
		
		if(state == GameState.WAITING_FOR_PLAYERS){
			g.setColor(new Color(0f, 0f, 0f, 0.7f));
			g.fillRect(0, 0, Game.getWidth(), Game.getHeight());
			g.setColor(Color.RED);
			g.setFont(Fonts.pixelmix.deriveFont(40f));
			String msg = "Waiting for Players...";
			g.drawString(msg, Game.getWidth()/2 - g.getFontMetrics().stringWidth(msg)/2, Game.getHeight()/2);
			g.setFont(Fonts.pixelmix.deriveFont(32f));
			String msg2 = "(" + Game.getWorld().getPlayers().size() + "/6)";
			g.drawString(msg2, Game.getWidth()/2 - g.getFontMetrics().stringWidth(msg2)/2, Game.getHeight()/2 + 40);
		
			int numVoted = 0;
			List<Player> vote = new ArrayList<Player>();
			vote.addAll(voted);
			for(Player p : vote){
				if(Game.getWorld().getPlayers().contains(p)){
					numVoted++;
				}else{
					voted.remove(p);
				}
			}
			
			g.setFont(Fonts.pixelmix.deriveFont(16f));
			g.setColor(new Color(170, 120, 0));
			String msg3 = "Press " + (Game.controllerState().isConnected ? "(A)" : "[V]") + " to vote for force start.";
			g.drawString(msg3, Game.getWidth()/2 - g.getFontMetrics().stringWidth(msg3)/2, Game.getHeight()/2 + 80);
			String msg4 = "" + numVoted + " of " + (int)Math.ceil(Game.getWorld().getPlayers().size() / 2d) + " needed.";
			g.drawString(msg4, Game.getWidth()/2 - g.getFontMetrics().stringWidth(msg4)/2, Game.getHeight()/2 + 100);
		
		}else if(state == GameState.SETUP){
			g.setColor(new Color(0f, 0f, 0f, 0.5f));
			g.fillRect(0, 0, Game.getWidth(), Game.getHeight());
			g.setColor(Color.GREEN);
			g.setFont(Fonts.pixelmix.deriveFont(40f));
			String msg = "Setup (" + (gameTime / 60) + ")";
			g.drawString(msg, Game.getWidth()/2 - g.getFontMetrics().stringWidth(msg)/2, Game.getHeight()/2);
		}else if(state == GameState.MATCH_END){
			g.setColor(new Color(0f, 0f, 0f, 0.5f));
			g.fillRect(0, 0, Game.getWidth(), Game.getHeight());
			
			int timer = (10 * 60) - gameTime;
			
			g.setColor(Color.GREEN);
			g.setFont(Fonts.pixelmix.deriveFont(64f));
			if(timer < 60 * 2){
				String msg = "Match Over!";
				g.drawString(msg, Game.getWidth()/2 - g.getFontMetrics().stringWidth(msg)/2, Game.getHeight()/2);
			}
			
			if(timer > 60 * 4){
				
				float thru = ((timer - (60 * 4f)) / (60 * 1f));
				if(thru > 1f) thru = 1f;
				
				int scoreBoardY = Game.getHeight()/2 + (int)(50 * thru);
				
				g.setFont(Fonts.pixelLCD.deriveFont(150f * thru));
	    		
	    		int colonWidth = g.getFontMetrics().stringWidth(":")/2;
				
				g.setColor(Color.DARK_GRAY);
	    		int redScore = Game.getWorld().getScoreWithPenalites(Team.RED);
	    		int blueScore = Game.getWorld().getScoreWithPenalites(Team.BLUE);
	    		g.drawString("" + redScore, Game.getWidth()/2 - (colonWidth) - g.getFontMetrics().stringWidth("" + redScore) - 3, scoreBoardY - 3);
	    		g.setColor(Team.RED.color);
	    		g.drawString("" + redScore, Game.getWidth()/2 - (colonWidth) - g.getFontMetrics().stringWidth("" + redScore), scoreBoardY);
	    		
	    		g.setColor(Color.DARK_GRAY);
	    		g.drawString(":", Game.getWidth()/2 - g.getFontMetrics().stringWidth(":")/2 - 3, scoreBoardY - 3);
	    		g.setColor(blueScore > redScore ? Team.BLUE.color : Team.RED.color);
	    		g.drawString(":", Game.getWidth()/2 - g.getFontMetrics().stringWidth(":")/2, scoreBoardY);
	    		
	    		g.setColor(Color.DARK_GRAY);
	    		g.drawString("" + blueScore, Game.getWidth()/2 + (colonWidth) - 3, scoreBoardY - 3);
	    		g.setColor(Team.BLUE.color);
	    		g.drawString("" + blueScore, Game.getWidth()/2 + (colonWidth), scoreBoardY);
	    		
			}
		}else if(state == GameState.AUTON || state == GameState.TELEOP){
		
    		g.setFont(Fonts.pixelmix.deriveFont(20f));
    		
    		if(Game.getWorld().getBoost().getUsing() != Team.NONE){
    			g.setColor(Game.getWorld().getBoost().getUsing().color);
    			String s = "boost (" + (Game.getWorld().getBoost().getTimer() / 60) + ")";
    			if(Game.getWorld().getBoost().getQueued() != Team.NONE) s += " (" + Game.getWorld().getBoost().getQueued() + " queued)";
    			g.drawString(s, 10, Game.getHeight() - 20);
    		}
    		
    		if(Game.getWorld().getForce().getUsing() != Team.NONE){
    			g.setColor(Game.getWorld().getForce().getUsing().color);
    			String s = "force (" + (Game.getWorld().getForce().getTimer() / 60) + ")";
    			if(Game.getWorld().getForce().getQueued() != Team.NONE) s += " (" + Game.getWorld().getForce().getQueued() + " queued)";
    			g.drawString(s, 10, Game.getHeight() - 40);
    		}
    
    		
    		int scoreBoardY = 80;
    		
    		g.setColor(new Color(0.4f, 0.4f, 0.4f, 0.7f));
    		g.fillRoundRect(Game.getWidth()/2 - 150, -50, 300, 140, 50, 50);
    		g.setColor(new Color(0.2f, 0.2f, 0.2f, 0.7f));
    		g.setStroke(new BasicStroke(2f));
    		g.drawRoundRect(Game.getWidth()/2 - 150, -50, 300, 140, 50, 50);
    		g.setStroke(new BasicStroke(1f));
    		
    //		g.drawLine(Game.getWidth()/2, 0, Game.getWidth()/2, 100);
    		
    		
    		g.setFont(Fonts.pixelLCD.deriveFont(40f));
    		
    		// game time
    
    		double time = gameTime / 60;
    		
    		int seconds = (int) (time % 60);
    		String secondsStr = (seconds < 10 ? "0" : "") + seconds; 
    		int minutes = (int) (time / 60);
    		String minutesStr = "" + minutes; 
    		
    		String timeS = minutesStr + ":" + secondsStr;
    		g.setColor(Color.DARK_GRAY);
    		g.drawString(timeS, Game.getWidth()/2 - g.getFontMetrics().stringWidth(timeS)/2 - 3, scoreBoardY - 34 - 3);
    		if(Game.isServer()) {
    			g.setColor(Team.RED.color);
    		}else {
    			g.setColor(Game.getWorld().getSelfPlayer().team.color);
    		}
    		g.drawString(timeS, Game.getWidth()/2 - g.getFontMetrics().stringWidth(timeS)/2, scoreBoardY - 34);
    		
    		g.setFont(Fonts.pixelLCD.deriveFont(28f));
    		
    		// scoreboard
    		
    		int colonWidth = g.getFontMetrics().stringWidth(":")/2;
    		
    		g.setColor(Color.DARK_GRAY);
    		int redScore = Game.getWorld().getScoreWithPenalites(Team.RED);
    		int blueScore = Game.getWorld().getScoreWithPenalites(Team.BLUE);
    		g.drawString("" + redScore, Game.getWidth()/2 - (colonWidth) - g.getFontMetrics().stringWidth("" + redScore) - 3, scoreBoardY - 3);
    		g.setColor(Team.RED.color);
    		g.drawString("" + redScore, Game.getWidth()/2 - (colonWidth) - g.getFontMetrics().stringWidth("" + redScore), scoreBoardY);
    		
    		g.setColor(Color.DARK_GRAY);
    		g.drawString(":", Game.getWidth()/2 - g.getFontMetrics().stringWidth(":")/2 - 3, scoreBoardY - 3);
    		g.setColor(blueScore > redScore ? Team.BLUE.color : Team.RED.color);
    		g.drawString(":", Game.getWidth()/2 - g.getFontMetrics().stringWidth(":")/2, scoreBoardY);
    		
    		g.setColor(Color.DARK_GRAY);
    		g.drawString("" + blueScore, Game.getWidth()/2 + (colonWidth) - 3, scoreBoardY - 3);
    		g.setColor(Team.BLUE.color);
    		g.drawString("" + blueScore, Game.getWidth()/2 + (colonWidth), scoreBoardY);
    		
    		// lifter height
    		if(!Game.isServer()) {
    			double roboHeight = 0.0;
        		if(Game.getWorld().getSelfPlayer().isClimbing()) {
        			roboHeight = 1 - Game.getWorld().getSelfPlayer().getHeight();
        		}
        		
        		g.setColor(Color.DARK_GRAY);
        		g.fillRect(Game.getWidth() - 20, Game.getHeight() - 250 -(int)(100 * roboHeight), 10, 110);
        		
        		double clawHeight = Game.getWorld().getSelfPlayer().getHeight();
        		
        		if(roboHeight > 0) clawHeight = 1;
        		
        		g.setColor(Color.LIGHT_GRAY);
        		g.fillRect(Game.getWidth() - 20 - 50, Game.getHeight() - 250 + 100-(int)(100 * clawHeight), 50, 10);
        		
        		g.setColor(Color.LIGHT_GRAY);
        		g.fillRect(Game.getWidth() - 20 - 50 + 10, Game.getHeight() - 250 + 10 + 100-(int)(100 * roboHeight), 50, 50);
    		}
    		
		}
		
		if(Game.gameplay.getState() == GameState.TELEOP && !Game.isServer()) {
			Game.getWorld().getProperties(Game.getWorld().getSelfPlayer().team).getVault().render(g);
		}else if(Game.gameplay.getState() == GameState.TELEOP && Game.isServer()) {
			Vault rVault = Game.getWorld().getProperties(Team.RED).getVault();
			rVault.setLockedOpen(true);
			rVault.setScale(1f);
			rVault.render(g);
			
			AffineTransform tr = g.getTransform();
			
			g.translate(Game.getWidth() - 140, 0);
			Vault bVault = Game.getWorld().getProperties(Team.BLUE).getVault();
			bVault.setLockedOpen(true);
			bVault.setScale(1f);
			bVault.render(g);
			
			g.setTransform(tr);
		}
		
//		g.setFont(Fonts.pixeled.deriveFont(16f));
//		g.setColor(Color.GREEN);
//		g.drawString("" + state, 6, 24);
		
	}
	
	/**
	 * @return the time remaining in the game, in ticks.
	 */
	public int getGameTime() {
		return gameTime;
	}

	/**
	 * Sets the remaining game time.
	 * @param gameTime - the remaining game time, in ticks.
	 */
	public void setGameTime(int gameTime) {
		this.gameTime = gameTime;
	}
	
	public void setState(GameState state){
		this.state = state;
		
		if(Game.isServer()) {
			SetStatePacket ssp = new SetStatePacket(state.toString());
			ServerStarter.serverStarter.sendToAll(ssp);
		}
		
		switch (state) {
			case WAITING_FOR_PLAYERS:
				setGameTime(2 * 60);
				Robot.setAllEnabled(false);
				resetField();
				voted.clear();
				Game.getWorld().setCameraCentered(true);
				break;
			case SETUP:
				resetField();
				setGameTime(2 * 60);
				Robot.setAllEnabled(false);
				Game.getWorld().setCameraCentered(false);
				
				redPlayers.clear();
				bluePlayers.clear();
				
				for(Player p : Game.getWorld().getPlayers()){
					if(p.team == Team.RED){
						redPlayers.add(p);
					}else if(p.team == Team.BLUE){
						bluePlayers.add(p);
					}
				}
				
				for(int i = 0; i < Math.min(redPlayers.size(), 3); i++){
					Player p = redPlayers.get(i);
					setLocation(p, redSpawns[i], Math.toRadians(90));
				}
				
				for(int i = 0; i < Math.min(bluePlayers.size(), 3); i++){
					Player p = bluePlayers.get(i);
					setLocation(p, blueSpawns[i], Math.toRadians(-90));
				}
				
				if(!Game.isServer() && !Game.GAMEPLAY_DEBUG) {
					Game.getWorld().getSelfPlayer().getRobot().setAutonScript(null);
    				SelectScriptMenu ssm = new SelectScriptMenu(Game.getWorld().getSelfPlayer().getRobot());
    				Render.showMenu(ssm);
    				new Thread(() -> {
    					while(ssm.isFocused()) {
    						try {
    							Thread.sleep(100);
    						} catch (InterruptedException e) {}
    					}
    					readyToStart(Game.getWorld().getSelfPlayer());
    				}).start();
				}
				
				break;
			case AUTON:
				Game.getWorld().getProperties(Team.RED).setScaleScoreMod(2);
				Game.getWorld().getProperties(Team.RED).setSwitchScoreMod(2);
				Game.getWorld().getProperties(Team.BLUE).setScaleScoreMod(2);
				Game.getWorld().getProperties(Team.BLUE).setSwitchScoreMod(2);
				setGameTime(15 * 60); // 15s
				Robot.setAllEnabled(false);
				
//				for(Player p : Game.getWorld().getPlayers()) {
//					Robot r = p.getRobot();
//					for(Component comp : r.getComponents()) {
//						if(comp instanceof ClawGrabberComponent) {
//							((ClawGrabberComponent) comp).setHasCube(true);
//						}
//					}
//				}
				
				if(!Game.isServer()) {
    				LuaScript ls = Game.getWorld().getSelfPlayer().getRobot().getAutonScript();
    				if(ls != null) ls.run();
				}
				
				s_startAuton.stop();
				s_startAuton.start();
				
				break;
			case TELEOP:
				Game.getWorld().getProperties(Team.RED).setCubeStorage(14);
				Game.getWorld().getProperties(Team.RED).setScaleScoreMod(1);
				Game.getWorld().getProperties(Team.RED).setSwitchScoreMod(1);
				Game.getWorld().getProperties(Team.BLUE).setCubeStorage(14);
				Game.getWorld().getProperties(Team.BLUE).setScaleScoreMod(1);
				Game.getWorld().getProperties(Team.BLUE).setSwitchScoreMod(1);
				setGameTime(135 * 60); // 2m 15s
				Robot.setAllEnabled(true);
				
				if(!Game.isServer()) {
    				LuaScript ls2 = Game.getWorld().getSelfPlayer().getRobot().getAutonScript();
    				if(ls2 != null) ls2.stop();
				}
				
				s_startTeleop.stop();
				s_startTeleop.start();
				break;
			case MATCH_END:
				
				if(Game.isServer()) {
					UpdateScorePacket usp = new UpdateScorePacket(Game.getWorld().getProperties(Team.RED).getScore() + "", Game.getWorld().getProperties(Team.BLUE).getScore() + "");
					ServerStarter.serverStarter.sendToAll(usp);
				}
				
				setGameTime(10 * 60); // 10s
				Robot.setAllEnabled(false);
				
				for(Player p : Game.getWorld().getPlayers()) {
					if(p.isClimbing() && p.getHeight() < 0.5) {
						Game.getWorld().getProperties(p.team).addScore(30);
					}else if(p.base.isInContact(Game.getWorld().getPlatform(p.team))) {
						Game.getWorld().getProperties(p.team).addScore(5);
					}
				}
				
				s_matchEnd.stop();
				s_matchEnd.start();
				break;
			default:
				break;
		}
		
	}
	
	private void setLocation(Player p, Point2D pt, double rot) {
		p.setLocation(pt, rot);
		if(Game.isServer()) {
			PlayerUpdatePacket pup = p.createUpdatePacket();
			ServerStarter.serverStarter.sendToAll(pup);
		}
	}

	private void resetField() {
		Game.getWorld().reset();
	}

	public boolean isEndGame(){
		return state == GameState.TELEOP && gameTime <= 30*60;
	}
	
	public static enum GameState {
		WAITING_FOR_PLAYERS,
		SETUP,
		AUTON,
		TELEOP,
		MATCH_END;
	}

	public GameState getState() {
		return state;
	}

	public void voteToStart(Player player) {
		if(!voted.contains(player)) {
			if(player == Game.getWorld().getSelfPlayer()) {
				VotePacket vp = new VotePacket(player.name);
				Game.sendPacket(vp);
			}
			voted.add(player);
		}
	}

	public void readyToStart(Player player) {
		if(!readyToStart.contains(player)) {
			if(player == Game.getWorld().getSelfPlayer()) {
				ChoseAutonPacket vp = new ChoseAutonPacket(player.name);
				Game.sendPacket(vp);
			}
			readyToStart.add(player);
		}
	}
	
}
