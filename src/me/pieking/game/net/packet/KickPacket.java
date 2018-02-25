package me.pieking.game.net.packet;

import javax.swing.JOptionPane;

import me.pieking.game.Game;
import me.pieking.game.Logger.ExitState;

public class KickPacket extends Packet{

	String reason;
	
	public KickPacket(String reason) {
		this.reason = reason;
	}

	@Override
	public String format() {
		return reason;
	}
	
	@Override
	public void doAction() {
		JOptionPane.showMessageDialog(null, "You were disconnected from the server:\n" + reason);
		Game.stop(ExitState.SERVER_DISCONNECT.code);
	}
	
}
