package me.pieking.game;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;

import me.pieking.game.events.KeyHandler;
import me.pieking.game.events.MouseHandler;
import me.pieking.game.gfx.Disp;
import me.pieking.game.gfx.Fonts;
import me.pieking.game.gfx.Render;
import me.pieking.game.menu.Menu;
import me.pieking.game.net.ClientStarter;
import me.pieking.game.net.ServerStarter;
import me.pieking.game.net.packet.JoinPacket;
import me.pieking.game.net.packet.LeavePacket;
import me.pieking.game.net.packet.Packet;
import me.pieking.game.net.packet.ShipDataPacket;
import me.pieking.game.robot.Robot;
import me.pieking.game.scripting.LuaScriptLoader;
import me.pieking.game.sound.Sound;
import me.pieking.game.world.Balance.Team;
import me.pieking.game.world.GameObject;
import me.pieking.game.world.GameWorld;
import me.pieking.game.world.Player;
import me.pieking.game.world.PowerCube;

public class Game {

	/** The width of the window content, in pixels. */
	private static final int WIDTH = 800;
	/** The height of the window content, in pixels. */
	private static final int HEIGHT = 600;
	
	/** The name of the game. */
	private static final String NAME = "Robotics Simulator 2018";
	
	/** Follows Semantic Versioning as per <a href="// https://semver.org/">semver.org</a>.*/
	private static final String VERSION = "0.2.0"; 
	
	/** Whether the game is running or not. */
	private static boolean running = false;
	
	/** The current FPS. */
	private static int fps = 0;
	/** The current TPS. */
	private static int tps = 0;
	
	/** 
	 * Increments by 1 every time {@link #tick()} is called.<br>
	 * If the game is running at normal speed, this means {@link #time} will increase once every 1/60 of a second.
	 */
	private static int time = 0;
	
	/** The active frame. */
	private static JFrame frame;
	/** The {@link Disp} inside {@link #frame}. */
	private static Disp disp;
	
	/** The {@link KeyHandler} registered to {@link #disp}.*/
	private static KeyHandler keyHandler;
	/** The {@link MouseHandler} registered to {@link #disp}.*/
	private static MouseHandler mouseHandler;
	
	/** The active world. */
	private static GameWorld gw;
	public static Gameplay gameplay;
	
	/**
	 * Run the game with arguments
	 */
	public static void runGame(String[] args) {
		run();
	}

	/**
	 * Initialize and run the game loop.<br>
	 * This method ensures that as long as the game is running, {@link #tick()} is called 60 times per second, and {@link #render()} is called as often as possible.
	 */
	private static void run(){
		init();
		
		long last = System.nanoTime();
		long now = System.nanoTime();
		
		double delta = 0d;
		
		double nsPerTick = 1e9 / 60d;
		
		long timer = System.currentTimeMillis();
		
		int frames = 0;
		int ticks = 0;
		
		running = true;
		
		while(running){
			now = System.nanoTime();
			
			long diff = now - last;
			
			delta += diff / nsPerTick;
			
			boolean shouldRender = true;
			
			while(delta >= 1){
				delta--;
				tick();
				ticks++;
				shouldRender = true;
			}
			
			try {
				Thread.sleep(2);
			} catch (InterruptedException e) {}
			
			if(shouldRender){
				render();
				frames++;
			}
			
			last = now;
			
			if(System.currentTimeMillis() - timer >= 1000){
				timer = System.currentTimeMillis();
				fps = frames;
				tps = ticks;
				frames = 0;
				ticks = 0;
			}
			
			
		}
		
	}
	
	/**
	 * Initialize the game 
	 */
	private static void init(){
		
//		try {
//			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
//		} catch (Exception e) {}
		
		// Doing this hack with the JPanel makes it so the contents of the frame are actually the right dimensions.
		frame = new JFrame(NAME + " v" + VERSION + " | " + fps + " FPS " + tps + " TPS");
		JPanel jp = new JPanel();
		jp.setPreferredSize(new Dimension(WIDTH, HEIGHT));
		frame.add(jp);
		frame.pack();
		
		
		frame.setIconImage(PowerCube.spr.getImage());
		
		jp.setVisible(false);
		
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frame.addWindowListener(new WindowListener() {
			@Override
			public void windowOpened(WindowEvent e) {}
			@Override
			public void windowIconified(WindowEvent e) {}
			@Override
			public void windowDeiconified(WindowEvent e) {}
			@Override
			public void windowDeactivated(WindowEvent e) {}
			@Override
			public void windowClosed(WindowEvent e) {}
			@Override
			public void windowActivated(WindowEvent e) {}
			
			@Override
			public void windowClosing(WindowEvent e) {
				try{
    				if(!isServer() && ClientStarter.clientStarter.getClient().isConnected()){
        				LeavePacket pack = new LeavePacket(gw.getSelfPlayer().name);
        				sendPacket(pack);
    				}
				}catch(Exception e2){
					e2.printStackTrace();
				}
				
				System.exit(0);
			}
		});
		
		frame.setLocationRelativeTo(null);
		
		disp = new Disp(WIDTH, HEIGHT, WIDTH, HEIGHT);
		
		keyHandler = new KeyHandler();
		disp.addKeyListener(keyHandler);
		
		mouseHandler = new MouseHandler();
		disp.addMouseListener(mouseHandler);
		disp.addMouseWheelListener(mouseHandler);
		
		frame.add(disp);
		
		frame.setVisible(true);
		
		LuaScriptLoader.init();
		Sound.init();
		Fonts.init();
		
		gw = new GameWorld();
		gameplay = new Gameplay();
		
		while(!ClientStarter.hasEnteredIp){
			try {
				Thread.sleep(100);
			}catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		}
		
		if(!isServer()){
			ClientStarter.clientStarter.getClient().connect();
			if (ClientStarter.clientStarter.getClient().isConnected()) {
				System.out.println("Connected to the server.");
				JoinPacket pack = new JoinPacket("Player " + System.currentTimeMillis(), "1", "1");
				Game.doPacket(pack);
				Game.getWorld().setSelfPlayer(pack.getCreated());
				
				Robot s = gw.getSelfPlayer().selectShip();
			    
			    try {
					ShipDataPacket sdp = new ShipDataPacket(Game.getWorld().getSelfPlayer().name, s.saveDataString());
					Game.doPacket(sdp);
				}catch (IOException e1) {
					e1.printStackTrace();
				}
				
			} else {
				gw.setSelfPlayer(new Player("Player 1", 900f / GameObject.SCALE * GameWorld.FIELD_SCALE, 500f / GameObject.SCALE * GameWorld.FIELD_SCALE, Team.RED));

				Robot s = gw.getSelfPlayer().selectShip();
				gw.getSelfPlayer().loadShip(s);
				
				gw.addPlayer(gw.getSelfPlayer());
				
			}
		}
		
	}
	
	/**
	 * Update everything.<br>
	 * This method expects to be called 60 times per second.
	 */
	private static void tick(){
		
		frame.setTitle(NAME + (isServer() ? " (Server) " : "") + " v" + VERSION + " | " + fps + " FPS " + tps + " TPS");
		
		try{
			gameplay.tick();
			gw.tick();
		}catch(Exception e){
			e.printStackTrace();
		}
		
		if(!isServer()){
			Point p = disp.getMousePositionScaled();
    		if(p != null) keyHandler.lastMousePos = p;
    		
    		List<Menu> menus = Render.getMenus();
    		for(int i = 0; i < menus.size(); i++){
    			Menu m = menus.get(i);
    			m.iTick();
    		}
		}
		
		time++;
	}
	
	/**
	 * Tells {@link Render} to render to {@link #disp}
	 */
	private static void render(){
		Render.render(disp);
		disp.paint(disp.getGraphics());
	}
	
	/**
	 * @return the display name of the game.
	 */
	public static String getName(){
		return NAME;
	}

	/**
	 * @return the version of the game.
	 * @see #VERSION
	 */
	public static String getVersion(){
		return VERSION;
	}

	/**
	 * @return the number of ticks since the game was started.
	 * @see #time
	 */
	public static int getTime() {
		return time;
	}
	
	/**
	 * Stop the game by calling {@link System#exit(int)}.
	 * @param status - exit status.
	 * @see System#exit(int)
	 */
	public static void stop(int status){
		System.exit(status);
	}
	
	/**
	 * @return the {@link KeyHandler} registered to the active {@link Disp}.
	 */
	public static KeyHandler keyHandler(){
		return keyHandler;
	}
	
	/**
	 * @return the {@link MouseHandler} registered to the active {@link Disp}.
	 */
	public static MouseHandler mouseHandler(){
		return mouseHandler;
	}
	
	/**
	 * @return the width of the window contents, in pixels.
	 */
	public static int getWidth(){
		return WIDTH;
	}
	
	/**
	 * @return the height of the window contents, in pixels.
	 */
	public static int getHeight(){
		return HEIGHT;
	}
	
	/**
	 * @return the active {@link Disp}.
	 */
	public static Disp getDisp(){
		return disp;
	}

	/**
	 * @return the active {@link GameWorld}.
	 */
	public static GameWorld getWorld() {
		return gw;
	}
	
	/**
	 * @return <code>true</code> if the running {@link Game} is a server.<br>
	 * <code>false</code> otherwise
	 */
	public static boolean isServer(){
		return ServerStarter.isServer;
	}
	
	/**
	 * Sends a packet to the conencted server, and also runs it locally.
	 * @param pack - the {@link Packet} to process
	 */
	public static void doPacket(Packet pack){
		if(!isServer()) ClientStarter.clientStarter.writePacket(pack);
		pack.doAction();
	}
	
	/**
	 * Sends a packet to the conencted server.
	 * @param pack - the {@link Packet} to process
	 */
	public static void sendPacket(Packet pack){
		if(!isServer()) ClientStarter.clientStarter.writePacket(pack);
	}
	
	/**
	 * @return the last mouse position relative to the top left corner of the screen.
	 * Coordinates are in pixels.
	 */
	public static Point mouseLoc() {
		return keyHandler.lastMousePos == null ? new Point(0, 0) : keyHandler.lastMousePos;
	}

	/**
	 * @return <code>true</code> if the game is in debug mode.<br>
	 * <code>false</code> otherwise.
	 */
	public static boolean debug() {
		return false;
	}
	
}
