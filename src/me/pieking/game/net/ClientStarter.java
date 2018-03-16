package me.pieking.game.net;

import java.lang.reflect.InvocationTargetException;

import javax.swing.JOptionPane;

import com.jmr.wrapper.client.Client;

import me.pieking.game.Game;
import me.pieking.game.net.packet.JoinPacket;
import me.pieking.game.net.packet.Packet;
import me.pieking.game.net.packet.ShipDataPacket;

public class ClientStarter {

	public static ClientStarter clientStarter;

	public static boolean hasEnteredIp = false;
	public static String enteredIp = null;
	
	private Client client;
	
	private ClientStarter(String[] args) {
		
//		new Thread(() -> {
//			while (true) {
//				ControllerState state = Game.controllerState();
//				if(state != null) {
//    				if(state.isConnected) {
//    					try {
//    						java.awt.Robot r = new java.awt.Robot();
//    						if(state.leftStickClick) r.mouseMove(MouseInfo.getPointerInfo().getLocation().x + (int)(state.leftStickX * 2), MouseInfo.getPointerInfo().getLocation().y - (int)(state.leftStickY * 5));
//    					} catch (AWTException e) {
//    						e.printStackTrace();
//    					}
//    				}
//    			}
//				
//				try {
//					Thread.sleep(10);
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//				}
//			}
//		}).start();
		
		String rawip = "localhost:" + ServerStarter.DEFAULT_PORT;
		
		if(args.length > 0) {
			rawip = args[0];
		}else {
			if(!Game.QUICK_CONNECT) rawip = JOptionPane.showInputDialog("Enter an IP: ", "localhost:" + ServerStarter.DEFAULT_PORT);
		}
		
		if(rawip == null) Game.stop(-1);
		
		connect(rawip);
	}

	public void connect(String rawip) {
		
		enteredIp = rawip;
		
		String ip = rawip.split(":")[0];
		int port = Integer.parseInt(rawip.split(":")[1]);
		
		System.out.println(ip + " " + port);
		
		MyClient cl = new MyClient(ip, port, port);
		cl.setListener(new ClientListener());
		
		client = cl;
		
		System.out.println("client = " + client + " " + client.getListener());
		
		hasEnteredIp = true;
	}
	
	public static void main(String[] args) {
		new Thread(() -> {
			clientStarter = new ClientStarter(args);
		}).start();
		Game.runGame(args);
	}
	
	public Client getClient(){
		return client;
	}
	
	public void writePacket(Packet p){
		String className = p.getClass().getSimpleName();
//		System.out.println("write " + className + "|" + p.format());
		
		if(client.isConnected()) client.getServerConnection().sendUdp(className + "|" + p.format());
	}
	
	public void recieve(String msg) throws ClassNotFoundException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		String[] args = msg.split("\\|");
		
		
		String className = args[0];
		Class<?> clazz;
		try{
			clazz = Class.forName("me.pieking.game.net.packet." + className);
		}catch(ClassNotFoundException e){
			System.err.println("Class not found: " + className);
			return;
		}
		
		String[] otherArgs = new String[args.length-1];
		for(int i = 1; i < args.length; i++) otherArgs[i-1] = args[i];
		
		Class<?>[] types = new Class<?>[otherArgs.length];
		for(int i = 0; i < types.length; i++) types[i] = otherArgs[i].getClass();

		Packet p = (Packet) clazz.getConstructor(types).newInstance((Object[]) otherArgs);
//		System.out.println(p);
		
		if(p instanceof JoinPacket){
			System.out.println(msg);
		}
		
		if(p instanceof ShipDataPacket){
			System.out.println(msg);
		}
		
//		System.out.println(msg);
		
		Game.queuePacket(p);
		
	}

	public void reconnect() {
		System.out.println("reconnect");
		connect(enteredIp);
		getClient().connect();
		
		System.out.println(client.isConnected());
		if(client.isConnected()) {
			Game.connectToServer();
		}
	}
	
}