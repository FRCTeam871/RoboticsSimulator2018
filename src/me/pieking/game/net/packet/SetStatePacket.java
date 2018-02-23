package me.pieking.game.net.packet;

import me.pieking.game.Game;
import me.pieking.game.Gameplay.GameState;

public class SetStatePacket extends Packet {

	GameState state;
	
	public SetStatePacket(String gameState) {
		this.state = GameState.valueOf(gameState);
	}

	@Override
	public String format() {
		return state.toString();
	}

	@Override
	public void doAction() {
		Game.gameplay.setState(state);
	}
	
}
