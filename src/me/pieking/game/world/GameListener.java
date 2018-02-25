package me.pieking.game.world;

import org.dyn4j.collision.manifold.Manifold;
import org.dyn4j.collision.narrowphase.Penetration;
import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.dynamics.CollisionListener;
import org.dyn4j.dynamics.contact.ContactConstraint;

public class GameListener implements CollisionListener{

	@Override
	public boolean collision(ContactConstraint e) {
		//System.out.println(Game.game.gw.ship.getCollision().contains(arg0.getBody2()));
		//System.out.println("col 1");
		return true;
	}

	@Override
	public boolean collision(Body arg0, BodyFixture arg1, Body arg2, BodyFixture arg3) {
//		arg1.setFilter(new GameObjectFilter(FilterType.BULLET));
//		System.out.println("col 2");
		
		return true;
	}

	@Override
	public boolean collision(Body arg0, BodyFixture arg1, Body arg2, BodyFixture arg3, Penetration arg4) {
		
		return true;
	}

	@Override
	public boolean collision(Body arg0, BodyFixture arg1, Body arg2, BodyFixture arg3, Manifold arg4) {
		//System.out.println("col 4");
		return true;
	}

}
