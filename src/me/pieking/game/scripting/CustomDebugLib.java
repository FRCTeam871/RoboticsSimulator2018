package me.pieking.game.scripting;

import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.DebugLib;

public class CustomDebugLib extends DebugLib {
    public boolean interrupted = false;

    @Override
    public void onInstruction(int pc, Varargs v, int top) {
        if (interrupted) {
            throw new ScriptInterruptException();
        }
        
        try {
			Thread.sleep(5);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
        
        super.onInstruction(pc, v, top);
    }

    @SuppressWarnings("serial")
	public static class ScriptInterruptException extends RuntimeException {
    	@Override
    	public String toString() {
    		return "ScriptInterruptException";
    	}
    }
}