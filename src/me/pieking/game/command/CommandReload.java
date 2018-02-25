package me.pieking.game.command;

import java.util.List;

import me.pieking.game.gfx.Sprite;
import me.pieking.game.sound.Sound;

public class CommandReload extends Command{

	public CommandReload() {
		super("/reload");
		
		desc  = "Reload parts of the game.";
		usage = "/reload <textures|sounds|commands>";
	}

	@Override
	public boolean runCommand(List<String> args) {
		if(!args.isEmpty()){
			String reload = args.get(0);
			
			if(reload.equalsIgnoreCase("textures")){
				Sprite.reloadAll();
			}else if(reload.equalsIgnoreCase("sounds")){
				Sound.soundSystem.reload();
			}else if(reload.equalsIgnoreCase("commands")){
				Command.registerDefaultCommands();
			}else{
				return false;
			}
			return true;
		}else{
			return false;
		}
	}

}
