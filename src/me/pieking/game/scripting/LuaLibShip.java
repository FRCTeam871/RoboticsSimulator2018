package me.pieking.game.scripting;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import org.dyn4j.dynamics.Force;
import org.dyn4j.dynamics.Torque;
import org.dyn4j.geometry.Vector2;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.TwoArgFunction;
import org.luaj.vm2.lib.VarArgFunction;
import org.luaj.vm2.lib.ZeroArgFunction;

import me.pieking.game.Game;
import me.pieking.game.robot.Robot;
import me.pieking.game.robot.component.ActivatableComponent;
import me.pieking.game.robot.component.Component;
import me.pieking.game.robot.component.ComputerComponent;
import me.pieking.game.world.TeamProperties;
import me.pieking.game.world.Balance.Team;

public class LuaLibShip extends TwoArgFunction {

	public LuaLibShip() {}

	public static Robot getRobot(){
		return Game.getWorld().getSelfPlayer().robot;
	}
	
	public LuaValue call(LuaValue modname, LuaValue env) {
		LuaValue library = tableOf();
		library.set("getComponent", new getComponent());
		library.set("getComponents", new getComponents());
		library.set("getOrientation", new getOrientation());
		library.set("mechDrive", new mechDrive());
		
		env.set("robot", library);
		return library;
	}
	
	public static LuaTable makeComponent(Component comp){
		LuaTable tab = tableOf();
		
		tab.set("tileX", comp.bounds.x);
		tab.set("tileY", comp.bounds.y);
		tab.set("rotation", comp.rot);
		tab.set("tileWidth", comp.bounds.width);
		tab.set("tileHeight", comp.bounds.height);
		tab.set("type", comp.getClass().getSimpleName());
		tab.set("health", comp.health);
		tab.set("maxhealth", comp.maxHealth);
		
		if(comp instanceof ActivatableComponent){
			ActivatableComponent ac = (ActivatableComponent) comp;
			tab.set("activated", ac.activated + "");
			
			List<Integer> act = ac.actKeys;
			LuaValue[] actV = new LuaValue[act.size()];
			for(int i = 0; i < act.size(); i++){
				actV[i] = valueOf(act.get(i));
			}
			
			List<Integer> deact = ac.deactKeys;
			LuaValue[] deactV = new LuaValue[deact.size()];
			for(int i = 0; i < deact.size(); i++){
				deactV[i] = valueOf(deact.get(i));
			}
			
			tab.set("actKeys", listOf(actV));
			tab.set("deactKeys", listOf(deactV));
			tab.set("activate", new activate(ac));
			tab.set("deactivate", new deactivate(ac));
			
		}

		if(comp instanceof ComputerComponent){
			ComputerComponent cc = (ComputerComponent) comp;
			
			tab.set("script", cc.script != null ? cc.script.name : "null");
		}
		
		return tab;
	}

	static class activate extends ZeroArgFunction {
		ActivatableComponent c;
		public activate(ActivatableComponent c) {
			this.c = c;
		}
		
		public LuaValue call() {
			c.activate();
			return LuaValue.NIL;
		}
	}
	
	static class deactivate extends ZeroArgFunction {
		ActivatableComponent c;
		public deactivate(ActivatableComponent c) {
			this.c = c;
		}
		
		public LuaValue call() {
			c.deactivate();
			return LuaValue.NIL;
		}
	}
	
	static class getOrientation extends ZeroArgFunction {
		public LuaValue call() {
			double actual = Math.toDegrees(Game.getWorld().getSelfPlayer().getRotation());
			return valueOf(actual);
		}
	}
	
	static class getComponent extends TwoArgFunction {
		public LuaValue call(LuaValue x, LuaValue y) {
			return makeComponent(getRobot().getComponent(new Point(x.checkint(), y.checkint())));
		}
	}
	
	static class turnRobot extends OneArgFunction {
		@Override
		public LuaValue call(LuaValue rot) {
			double val = rot.checkdouble();
			val = Math.min(Math.max(-1, val), 1d);
			Game.getWorld().getPlayer(getRobot()).queueTorque(new Torque(200 * val));
			return LuaValue.NIL;
		}
	}
	
	static class mechDrive extends VarArgFunction {
		@Override
		public LuaValue invoke(Varargs args) {
			LuaValue x = args.arg(1);
			LuaValue y = args.arg(2);
			LuaValue rot = args.arg(3);
			LuaValue gyro = args.arg(4);
			
			double theta = gyro.checkdouble();
			
			theta += Math.toDegrees(Game.getWorld().getPlayer(getRobot()).getRotation());
			
			double xa = Math.max(-1, Math.min(x.checkdouble(), 1));
			xa *= 300;
			double xcx = xa * Math.sin(Math.toRadians(theta));
			double xcy = -xa * Math.cos(Math.toRadians(theta));
			
			double ya = Math.max(-1, Math.min(y.checkdouble(), 1));
			ya *= 300;
			double ycx = ya * Math.sin(Math.toRadians(90 - theta));
			double ycy = ya * Math.cos(Math.toRadians(90 - theta));
			
			Vector2 vec = new Vector2(xcx + ycx, xcy + ycy);
			
			Game.getWorld().getPlayer(getRobot()).queueForce(new Force(vec));
			
			double rotation = rot.checkdouble();
			rotation = Math.min(Math.max(-1, rotation), 1d);
			Game.getWorld().getPlayer(getRobot()).queueTorque(new Torque(400 * rotation));
			return LuaValue.NIL;
		}
	}
	
	static class getComponents extends OneArgFunction {
		public LuaValue call(LuaValue x) {
			
			List<Component> comp = getRobot().getComponents();
			List<Component> matched = new ArrayList<Component>();
			
			if(x.istable()){
				LuaTable filter = x.checktable();
				LuaValue[] keys = filter.keys();
				for(Component c : comp){
					LuaTable tab = makeComponent(c);
					
					boolean matches = true;
					for(int i = 0; i < keys.length; i++){
						LuaValue v = keys[i];
						
						if(tab.get(v).isnil() || !tab.get(v).raweq(filter.get(v))){
							matches = false;
							break;
						}
					}

					if(matches) matched.add(c);
					
				}
			}else{
				matched.addAll(comp);
			}
			
			LuaValue[] vals = new LuaValue[matched.size()];
			
			for(int i = 0; i < vals.length; i++){
				vals[i] = makeComponent(matched.get(i));
				for(LuaValue v : vals[i].checktable().keys()) {
					System.out.println(v.toString() + " : " + vals[i].checktable().get(v));
				}
			}
			
			return listOf(vals);
		}
	}
}