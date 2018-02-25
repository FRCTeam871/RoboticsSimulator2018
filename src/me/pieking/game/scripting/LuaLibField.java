package me.pieking.game.scripting;

import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.TwoArgFunction;
import org.luaj.vm2.lib.ZeroArgFunction;

import me.pieking.game.Game;
import me.pieking.game.robot.Robot;
import me.pieking.game.world.Balance.Team;
import me.pieking.game.world.TeamProperties;

public class LuaLibField extends TwoArgFunction {

	public LuaLibField() {}

	public static Robot getRobot(){
		return Game.getWorld().getSelfPlayer().robot;
	}
	
	public LuaValue call(LuaValue modname, LuaValue env) {
		LuaValue library = tableOf();
		library.set("getPlateColors", new getPlateColors());
		
		env.set("field", library);
		return library;
	}
	
	static class getPlateColors extends ZeroArgFunction {
		public LuaValue call() {

			String data = "";
			
			Team rTeam = Game.getWorld().getPlayer(getRobot()).team;
			TeamProperties tp = Game.getWorld().getProperties(rTeam);
			TeamProperties tpOther = Game.getWorld().getProperties(rTeam.getOpposite());
			
			Team top = tp.getSwitch().getTopTeam();
			data += rTeam == top ? "L" : "R";
			
			top = Game.getWorld().getScale().getTopTeam();
			data += rTeam == top ? "L" : "R";
			
			top = tpOther.getSwitch().getTopTeam();
			data += rTeam == top ? "L" : "R";
			
			return valueOf(data);
		}
	}
	
}