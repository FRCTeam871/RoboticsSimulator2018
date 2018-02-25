package me.pieking.game.scripting;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

import org.luaj.vm2.Globals;
import org.luaj.vm2.LoadState;
import org.luaj.vm2.LuaString;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaThread;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.compiler.LuaC;
import org.luaj.vm2.lib.Bit32Lib;
import org.luaj.vm2.lib.DebugLib;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.PackageLib;
import org.luaj.vm2.lib.StringLib;
import org.luaj.vm2.lib.TableLib;
import org.luaj.vm2.lib.TwoArgFunction;
import org.luaj.vm2.lib.ZeroArgFunction;
import org.luaj.vm2.lib.jse.JseBaseLib;
import org.luaj.vm2.lib.jse.JseMathLib;

import me.pieking.game.FileSystem;
import me.pieking.game.Logger;

public class LuaScriptLoader {

	// These globals are used by the server to compile scripts.
	static Globals server_globals;

	public static void init() {
		// Create server globals with just enough library support to compile user scripts.
		server_globals = new Globals();
		server_globals.load(new JseBaseLib());
		server_globals.load(new PackageLib());
		server_globals.load(new StringLib());

		// To load scripts, we occasionally need a math library in addition to compiler support.
		// To limit scripts using the debug library, they must be closures, so we only install LuaC.
		server_globals.load(new JseMathLib());
		LoadState.install(server_globals);
		LuaC.install(server_globals);

		// Set up the LuaString metatable to be read-only since it is shared across all scripts.
		LuaString.s_metatable = new ReadOnlyLuaTable(LuaString.s_metatable);

//		try {
//			runScriptInSandbox(FileSystem.getFile("scripts/inputtest.lua"));
//		}
//		catch (FileNotFoundException e) {
//			e.printStackTrace();
//		}

	}

	// Run a script in a lua thread and limit it to a certain number
	// of instructions by setting a hook function.
	// Give each script its own copy of globals, but leave out libraries
	// that contain functions that can be abused.
	static void runScriptInSandbox(File script) throws FileNotFoundException {

		// Each script will have it's own set of globals, which should 
		// prevent leakage between scripts running on the same server.
		Globals user_globals = new Globals();
		user_globals.load(new JseBaseLib());
		user_globals.load(new PackageLib());
		user_globals.load(new Bit32Lib());
		user_globals.load(new TableLib());
		user_globals.load(new StringLib());
		user_globals.load(new JseMathLib());
		applyDefaultLibs(user_globals);

		// This library is dangerous as it gives unfettered access to the
		// entire Java VM, so it's not suitable within this lightweight sandbox. 
		// user_globals.load(new LuajavaLib());

		// Starting coroutines in scripts will result in threads that are 
		// not under the server control, so this libary should probably remain out.
		// user_globals.load(new CoroutineLib());

		// These are probably unwise and unnecessary for scripts on servers,
		// although some date and time functions may be useful.
		// user_globals.load(new JseIoLib());
		// user_globals.load(new JseOsLib());

		// Loading and compiling scripts from within scripts may also be 
		// prohibited, though in theory it should be fairly safe.
		// LoadState.install(user_globals);
		// LuaC.install(user_globals);

		// The debug library must be loaded for hook functions to work, which  
		// allow us to limit scripts to run a certain number of instructions at a time.
		// However we don't wish to expose the library in the user globals,
		// so it is immediately removed from the user globals once created.
		user_globals.load(new DebugLib());
		LuaValue sethook = user_globals.get("debug").get("sethook");
		user_globals.set("debug", LuaValue.NIL);

		// Set up the script to run in its own lua thread, which allows us 
		// to set a hook function that limits the script to a specific number of cycles.
		// Note that the environment is set to the user globals, even though the 
		// compiling is done with the server globals.
		LuaValue chunk = server_globals.load(new FileReader(script), "main", user_globals);
		LuaThread thread = new LuaThread(user_globals, chunk);

		// Set the hook function to immediately throw an Error, which will not be 
		// handled by any Lua code other than the coroutine.
		LuaValue hookfunc = new ZeroArgFunction() {
			public LuaValue call() {
				// A simple lua error may be caught by the script, but a 
				// Java Error will pass through to top and stop the script.
				throw new Error("Script overran resource limits.");
			}
		};
		final int instruction_count = 20;
		sethook.invoke(LuaValue.varargsOf(new LuaValue[] { thread, hookfunc, LuaValue.EMPTYSTRING, LuaValue.valueOf(instruction_count) }));

		// When we resume the thread, it will run up to 'instruction_count' instructions
		// then call the hook function which will error out and stop the script.
		Varargs result = thread.resume(LuaValue.NIL);
		System.out.println("[[" + script + "]] -> " + result);
	}

	// Simple read-only table whose contents are initialized from another table.
	static class ReadOnlyLuaTable extends LuaTable {
		public ReadOnlyLuaTable(LuaValue table) {
			presize(table.length(), 0);
			for (Varargs n = table.next(LuaValue.NIL); !n.arg1().isnil(); n = table.next(n.arg1())) {
				LuaValue key = n.arg1();
				LuaValue value = n.arg(2);
				super.rawset(key, value.istable() ? new ReadOnlyLuaTable(value) : value);
			}
		}

		public LuaValue setmetatable(LuaValue metatable) {
			return error("table is read-only");
		}

		public void set(int key, LuaValue value) {
			error("table is read-only");
		}

		public void rawset(int key, LuaValue value) {
			error("table is read-only");
		}

		public void rawset(LuaValue key, LuaValue value) {
			error("table is read-only");
		}

		public LuaValue remove(int pos) {
			return error("table is read-only");
		}
	}

	public static void applyDefaultLibs(Globals env){
		addLibrary(env, new LuaLibInput());
		addLibrary(env, new LuaLibShip());
		addLibrary(env, new LuaLibField());

		env.set("sleep", new sleep());
	}
	
	public static void addLibrary(Globals env, TwoArgFunction func){
		env.load(func);
		func.call(LuaValue.valueOf(func.getClass().getSimpleName()), env);
	}
	
	static class sleep extends OneArgFunction {
		public LuaValue call(LuaValue ms) {
			try {
				Thread.sleep(ms.checklong());
			}catch (InterruptedException e) {
				e.printStackTrace();
			}
			return LuaValue.NIL;
		}
	}
	
	public static LuaScript runScript(String name){
		try{
    		File f = FileSystem.getFile("scripts/" + name + (name.endsWith(".lua") ? "" : ".lua"));
    		
    		System.out.println(f);
    		
    		Globals user_globals = new Globals();
    		user_globals.load(new JseBaseLib());
    		user_globals.load(new PackageLib());
    		user_globals.load(new Bit32Lib());
    		user_globals.load(new TableLib());
    		user_globals.load(new StringLib());
    		user_globals.load(new JseMathLib());
    		applyDefaultLibs(user_globals);
    
    		LuaValue chunk = server_globals.load(new FileReader(f), name, user_globals);
    		
    		user_globals.load(new DebugLib());
    		LuaValue sethook = user_globals.get("debug").get("sethook");
    		user_globals.set("debug", LuaValue.NIL);

    		// Set up the script to run in its own lua thread, which allows us 
    		// to set a hook function that limits the script to a specific number of cycles.
    		// Note that the environment is set to the user globals, even though the 
    		// compiling is done with the server globals.
    		LuaThread thread = new LuaThread(user_globals, chunk);

    		// Set the hook function to immediately throw an Error, which will not be 
    		// handled by any Lua code other than the coroutine.
    		LuaValue hookfunc = new ZeroArgFunction() {
    			public LuaValue call() {
    				// A simple lua error may be caught by the script, but a 
    				// Java Error will pass through to top and stop the script.
    				throw new Error("Script overran resource limits.");
    			}
    		};
    		final int instruction_count = 20;
    		sethook.invoke(LuaValue.varargsOf(new LuaValue[] { thread, hookfunc, LuaValue.EMPTYSTRING, LuaValue.valueOf(instruction_count) }));
    		
    		CustomDebugLib cdl = new CustomDebugLib();
    		user_globals.load(cdl);
    		
    		return new LuaScript(name, user_globals, chunk, cdl);
		}catch(Exception e){
			Logger.warn("Error loading script \"" + name + "\": " + e.getMessage());
			e.printStackTrace();
		}
		return null;
	}
	
}
