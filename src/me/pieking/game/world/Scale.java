package me.pieking.game.world;

import java.awt.Color;

import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.MassType;
import org.dyn4j.geometry.Rectangle;

import me.pieking.game.gfx.LEDStrip;
import me.pieking.game.gfx.LEDStrip.StripMode;
import me.pieking.game.robot.component.Component;

public class Scale extends Balance {

	public Scale(double x, double y, boolean blueTop, LEDStrip ledStripTop, LEDStrip ledStripBottom) {
		super(x, y, blueTop, ledStripTop, ledStripBottom, true);
		red = new ScalePlatform(x, y - 4.75, ledStripTop, true);
		blue = new ScalePlatform(x, y + 4.75, ledStripBottom, true);
		
		red.ledXofs = -1;
		red.ledYofs = -1;
		blue.ledXofs = -1;
		blue.ledYofs = -1;
		
		if(blueTop){
			ScalePlatform temp = red;
			red = blue;
			blue = temp;
		}

		red.strip.setMode(StripMode.SOLID_RED_FULL);
		blue.strip.setMode(StripMode.SOLID_BLUE_FULL);
		
		walls = new GameObject();
//		walls.type = BodyType.DEFAULT;
		walls.color = Color.GREEN;
		
		Rectangle r = new Rectangle(Component.unitSize * (2), Component.unitSize * (red.sizeH*3 + 1));
		r.translate(Component.unitSize * (red.sizeW/2 + 5.4), Component.unitSize * (red.sizeH/2 + 2));
		BodyFixture bf = new BodyFixture(r);
//		bf.setFilter(new GameObjectFilter(FilterType.DEFAULT));
		walls.setMass(MassType.INFINITE);
		walls.addFixture(bf);
		
		walls.setAngularDamping(GameWorld.getAngularDamping());
		walls.setLinearDamping(GameWorld.getLinearDamping());
		walls.translate(x, y);
		
	}

}
