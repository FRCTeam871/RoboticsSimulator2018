package me.pieking.game;

public class Logger {

	private static String log_all;
	private static String log_err;
	
	public static final int VB_NONE = -1;
	public static final int VB_NORMAL = 0;
	public static final int VB_DEV_ONLY = 100;
	
	private static int verbosity = VB_NORMAL; //higher = more verbose
	
	public static void info(String msg){
		info(msg, 0);
	}
	
	public static void warn(String msg){
		warn(msg, 0);
	}
	
	public static void fatal(String msg, ExitState state){
		plainErr("[FATAL] " + msg);
		Game.stop(state.code);
	}
	
	public static void info(String msg, int verbosity){
		if(Logger.verbosity >= verbosity) plainOut("[INFO] " + msg);
	}
	
	public static void warn(String msg, int verbosity){
		if(Logger.verbosity >= verbosity) plainErr("[WARN] " + msg);
	}
	
	public static void plainOut(String msg){
		log_all = log_all + msg + "\n";
		System.out.println("[" + Game.getName() + "] " + msg);
	}
	
	public static void plainErr(String msg){
		log_all = log_all + msg + "\n";
		log_err = log_err + msg + "\n";
		System.err.println("[" + Game.getName() + "] " + msg);
	}
	
	public static enum ExitState {
		UNKNOWN(-50),
		OK(0),
		SERVER_RUN_ERROR(1), 
		SERVER_DISCONNECT(2);
		
		public int code = 0;
		
		private ExitState(int code) {
			this.code = code;
		}
		
	}
	
}

