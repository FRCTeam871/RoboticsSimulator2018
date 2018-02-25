package me.pieking.game.world;

import me.pieking.game.gfx.LEDStrip;

public class Switch extends Balance {

	public Switch(double x, double y, boolean blueTop, LEDStrip ledStripTop, LEDStrip ledStripBottom) {
		super(x, y, blueTop, ledStripTop, ledStripBottom, false);
		(blueTop ? red : blue).ledYofs = -1;
	}

}
