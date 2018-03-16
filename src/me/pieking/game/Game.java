package me.pieking.game;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;

import org.json.JSONObject;

import com.studiohartman.jamepad.ControllerManager;
import com.studiohartman.jamepad.ControllerState;
import com.studiohartman.jamepad.ControllerUnpluggedException;

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
	private static int WIDTH = 800;
	/** The height of the window content, in pixels. */
	private static int HEIGHT = 600;
	
	/** The name of the game. */
	private static final String NAME = "Robotics Simulator 2018";
	
	private static final String VERSION = "1.0.0-beta";
	
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
	
	private static ControllerManager controllerManager;
	private static ControllerState state;
	private static boolean fullScreen;
	private static JPanel jp;
	
	public static final boolean QUICK_CONNECT = false;
	public static final boolean GAMEPLAY_DEBUG = false;
	
	public static List<Packet> packetQueue = new ArrayList<Packet>();
	
	private static Thread gameThread;
	private static long lastUpdate = System.currentTimeMillis();
	private static Thread monitorThread;
	
	private static boolean debugHang = false;
	private static boolean debugLag = false;
	
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
		lastUpdate = System.currentTimeMillis();
		gameThread = new Thread(Game::runGameLoop);
		gameThread.start();
		monitorThread = new Thread(Game::monitorThreadLoop);
		monitorThread.start();
	}

	private static void runGameLoop() {
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
			lastUpdate = System.currentTimeMillis();
			
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
	
	private static void monitorThreadLoop() {
		while(true) {
			
			long now = System.currentTimeMillis();
			
//			System.out.println(now + " " + lastUpdate + " " + (now - lastUpdate));
			
			if(now - lastUpdate > 3000) {
				Logger.warn("Gameloop Thread is hung!");
				lastUpdate = now;
				
				gameThread.interrupt();
				gameThread.stop();
				gameThread = new Thread(Game::runGameLoop);
				gameThread.start();
			}
			
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Initialize the game 
	 */
	private static void init(){
		
		if(controllerManager == null) {
    		controllerManager = new ControllerManager();
    		controllerManager.initSDLGamepad();
    		state = controllerManager.getState(0);
		}
		
		if(isServer()) {
			
			boolean smallServer = false;
			
			if(smallServer) {
    			HEIGHT = 400;
    			GameObject.SCALE = 15.3;
    			GameObject.DESIRED_SCALE = 15.3;
			}else {
				HEIGHT = 600;
				GameObject.SCALE = 23;
				GameObject.DESIRED_SCALE = 23;
			}
			
			WIDTH = (int) (HEIGHT * 2.4625);
			
		}
		
//		try {
//			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
//		} catch (Exception e) {}
		
		// Doing this hack with the JPanel makes it so the contents of the frame are actually the right dimensions.
		frame = new JFrame(NAME + " v" + VERSION + " | " + fps + " FPS " + tps + " TPS");
		jp = new JPanel();
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
		Settings.loadConfig();
		
		gw = new GameWorld();
		gameplay = new Gameplay();
		
		while(!ClientStarter.hasEnteredIp && !isServer()){
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
//				String username = JOptionPane.showInputDialog(frame, "Enter a username:");

				try {
					Thread.sleep(500);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				
				connectToServer();
				
			} else {
				
				int teamNum = Rand.range(1, 8000);
				String username = "Team " + teamNum;
				
				gw.setSelfPlayer(new Player(username, 900f / GameObject.SCALE * GameWorld.FIELD_SCALE, 500f / GameObject.SCALE * GameWorld.FIELD_SCALE, Team.RED));

				Robot s = gw.getSelfPlayer().selectShip();
				gw.getSelfPlayer().loadShip(s);
				
				gw.addPlayer(gw.getSelfPlayer());
				
			}
		}
		
	}

	public static void connectToServer() {
			
		List<Player> pl = new ArrayList<Player>();
		pl.addAll(getWorld().getPlayers());
		
		for(Player p : pl) {
			Game.getWorld().removePlayer(p);
		}
		
		int teamNum = Rand.range(1, 8000);
		String username = "Team " + teamNum;
		
		// data can be polled from https://www.thebluealliance.com/api/v3/team/frc####?X-TBA-Auth-Key=****
		JoinPacket pack = new JoinPacket(username, "1", "1", getVersion(), true);
		Game.doPacket(pack);
		Game.getWorld().setSelfPlayer(pack.getCreated());
		
		try {
			System.out.println("Polling TBA for team " + teamNum + " ...");
			JSONObject json = Utils.getTeamInfo(teamNum);
			System.out.println("Got response:");
			System.out.println(json.toString(2));
			if(!json.has("Errors")) gw.getSelfPlayer().setTeamInfo(json);
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		Robot s = gw.getSelfPlayer().selectShip();
		
		try {
			ShipDataPacket sdp = new ShipDataPacket(Game.getWorld().getSelfPlayer().name, s.saveDataString());
			Game.doPacket(sdp);
		}catch (IOException e1) {
			e1.printStackTrace();
		}
	}
	
	/**
	 * Update everything.<br>
	 * This method expects to be called 60 times per second.
	 */
	private static void tick(){
		
		if(debugHang) {
			debugHang = false;
			while(true) { // purposefully hang the thread
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {}
			}
		}
		
		frame.setTitle(NAME + (isServer() ? " (Server) " : "") + " v" + VERSION + " | " + fps + " FPS " + tps + " TPS");

		List<Packet> pack = new ArrayList<Packet>();
		pack.addAll(packetQueue);
//		System.out.println("packets: " + packetQueue.size());
//		long start = System.currentTimeMillis();
		for(Packet p : pack) {
			if(p != null) p.doAction();
		}
		packetQueue.clear();
//		System.out.println("took " + (System.currentTimeMillis()-start) + "ms");
		
		state = controllerManager.getState(0);
		
		try{
			gameplay.tick();
			gw.tick();
		}catch(Exception e){
			e.printStackTrace();
		}
		
//		if(!isServer()){
			Point p = disp.getMousePositionScaled();
    		if(p != null) keyHandler.lastMousePos = p;
    		
    		List<Menu> menus = Render.getMenus();
    		for(int i = 0; i < menus.size(); i++){
    			Menu m = menus.get(i);
    			m.iTick();
    		}
    		
//		}
		
		time++;
	}
	
	/**
	 * Tells {@link Render} to render to {@link #disp}
	 */
	private static void render(){
		
		if(debugLag) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {}
		}
		
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
		sendPacket(pack);
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
	
	public static ControllerState controllerState() {
		if(controllerManager == null) {
			return null;
		}
		
		return state;
	}
	
	public static void setVibration(float r, float l) {
		try {
			controllerManager.getControllerIndex(0).startVibration(l, r);
		} catch (ControllerUnpluggedException e) {
			e.printStackTrace();
		}
	}

	public static boolean isConnected() {
		if(ClientStarter.clientStarter == null || ClientStarter.clientStarter.getClient() == null) return false;
		return ClientStarter.clientStarter.getClient().isConnected();
	}
	
	public static void toggleFullScreen(){
		
		JFrame newFrame = new JFrame(NAME + " " + VERSION);
		
		if(!fullScreen){
			newFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
			newFrame.setUndecorated(true);
		}else{
			//newFrame.setExtendedState(JFrame.NORMAL);
			newFrame.setUndecorated(false);
		}
		
		jp = new JPanel();
		jp.setPreferredSize(new Dimension(WIDTH - 10, HEIGHT - 10));
		newFrame.getContentPane().add(jp);
		newFrame.getContentPane().setBackground(Color.BLACK);
		newFrame.pack();
		jp.setVisible(false);
		newFrame.setVisible(true);
		newFrame.setLocationRelativeTo(null);
		newFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		newFrame.setTitle(Game.NAME);
		newFrame.add(disp);
		newFrame.setLayout(null);
		newFrame.addWindowListener(new WindowListener() {
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
		newFrame.setResizable(false);
		
		newFrame.setIconImage(PowerCube.spr.getImage());
		
		if(!fullScreen) {
			
			int dheight = (int) (newFrame.getHeight());
			int dwidth = (int) (dheight * ((float)WIDTH / (float)HEIGHT));
			
			//System.out.println(disp.rwidth + " " + dwidth );
			
			disp.setBounds((newFrame.getWidth() / 2) - (dwidth / 2), 0, dwidth, dheight);
			disp.realHeight = dheight + 2;
			disp.realWidth = dwidth + 2;
			
		}else{
			disp.realHeight = HEIGHT + 1;
			disp.realWidth = WIDTH + 1;
			disp.setBounds(0, 0, WIDTH, HEIGHT);
		}
		
		frame.dispose();
		
		frame = newFrame;
		
//		game.setHideCursor(hideCursor);
		
		fullScreen = !fullScreen;
		
		java.awt.Robot r;
		try {
			
			r = new java.awt.Robot();
			
			Timer t = new Timer(200, new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					int x = (int) MouseInfo.getPointerInfo().getLocation().getX();
					int y = (int) MouseInfo.getPointerInfo().getLocation().getY();
					r.mouseMove(Toolkit.getDefaultToolkit().getScreenSize().width/2, Toolkit.getDefaultToolkit().getScreenSize().height/2);
					r.mousePress(InputEvent.BUTTON1_DOWN_MASK);
					r.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
					r.mouseMove(x, y);
				}
			});
			t.setRepeats(false);
			t.start();
			
		} catch (AWTException e1) {}
	}

	public static void queuePacket(Packet p) {
		packetQueue.add(p);
	}
	
	public static void debugHang() {
		debugHang = true;
	}
	
	public static void debugLag() {
		debugLag = !debugLag;
	}
	
}
