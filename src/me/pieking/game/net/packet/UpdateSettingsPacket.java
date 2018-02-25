package me.pieking.game.net.packet;

import me.pieking.game.Settings;

public class UpdateSettingsPacket extends Packet {

	String settings;
	
	public UpdateSettingsPacket(String settings) {
		this.settings = settings;
	}

	@Override
	public String format() {
		return settings;
	}

	@Override
	public void doAction() {
		Settings.load(settings);
	}
	
}
