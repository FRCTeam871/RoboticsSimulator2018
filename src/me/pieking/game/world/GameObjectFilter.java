package me.pieking.game.world;

import org.dyn4j.collision.Filter;

public class GameObjectFilter implements Filter{

	public FilterType type = FilterType.DEFAULT;
	
	public GameObjectFilter(FilterType type) {
		this.type = type;
	}
	
	@Override
	public boolean isAllowed(Filter arg0) {
		//System.out.println(arg0);
		if(arg0 instanceof GameObjectFilter){
			GameObjectFilter f = (GameObjectFilter) arg0;
			
			if(f instanceof PlayerFilter){
				Player pl = ((PlayerFilter) f).pl;
				if(pl != null && pl.noClip) return false;
			}
			
			if(this.type.collidesWith(f.type)){
				return true;
			}else{
				return false;
			}
			
		}else{
			return true;
		}
	}

	public enum FilterType{
		DEFAULT						(), 
		POWER_CUBE					("SHIP", "POWER_CUBE", "SCALE_PLATFORM", "SWITCH_PLATFORM", "POWER_CUBE_HOLDING_GROUND"),
		POWER_CUBE_HOLDING_GROUND 	("SHIP", "POWER_CUBE", "SCALE_PLATFORM", "SWITCH_PLATFORM", "POWER_CUBE_HOLDING_GROUND"),
		POWER_CUBE_HOLDING_LOW	  	("SHIP", "SCALE_PLATFORM"),
		POWER_CUBE_HOLDING_HIGH	 	("SHIP"),
		SHIP						("SHIP", "POWER_CUBE"),
		PARTICLE					(),
		SCALE_PLATFORM_CENTER 		("SHIP", "POWER_CUBE_HOLDING_GROUND", "POWER_CUBE_HOLDING_LOW"),
		SCALE_PLATFORM 				("SHIP", "POWER_CUBE", "POWER_CUBE_HOLDING_GROUND", "POWER_CUBE_HOLDING_LOW"),
		SWITCH_PLATFORM_CENTER 		("SHIP", "POWER_CUBE_HOLDING_GROUND"),
		SWITCH_PLATFORM 			("SHIP", "POWER_CUBE", "POWER_CUBE_HOLDING_GROUND");
		
		static {
			for(FilterType f : values()){
				f.finish();
			}
		}
		
		FilterType[] coll;
		String[] collS;
		
		private FilterType(String... coll) {
			this.collS = coll;
		}
		
		private void finish(){
			coll = new FilterType[collS.length];
			for(int i = 0; i < collS.length; i++){
				coll[i] = FilterType.valueOf(collS[i]);
				System.out.println(this + " collides with " + coll[i]);
			}
		}
		
		public boolean collidesWith(FilterType f){
			for(FilterType fi : coll){
//				if(f == POWER_CUBE_HOLDING_GROUND) System.out.println(f + " " + fi);
				if(fi == f){
					return true;
				}
			}
			return false;
		}
		
	}
	
}
