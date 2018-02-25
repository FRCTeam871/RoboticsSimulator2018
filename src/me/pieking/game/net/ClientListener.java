package me.pieking.game.net;

import javax.swing.JOptionPane;

import com.jmr.wrapper.common.Connection;
import com.jmr.wrapper.common.listener.SocketListener;

import me.pieking.game.Game;
import me.pieking.game.Logger.ExitState;

public class ClientListener implements SocketListener {

	@Override
	public void received(Connection con, Object object) {
//		System.out.println("Received: " + object + " " + (object instanceof String));
		
		if(object instanceof String && !((String)object).equals("TestAlivePing")){
			try {
				ClientStarter.clientStarter.recieve((String)object);
			}catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void connected(Connection con) {
		System.out.println("Connected to the server.");
	}

	@Override
	public void disconnected(Connection con) {
		System.out.println("Disconnected from the server.");
		JOptionPane.showMessageDialog(null, "You were disconnected from the server.");
		Game.stop(ExitState.SERVER_DISCONNECT.code);
	}
	
}