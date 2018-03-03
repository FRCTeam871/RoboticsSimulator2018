package me.pieking.game;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;

public class Settings {
	
	public static int playerUpdateInterval = 5;
	public static int cubeUpdateInterval = 15;
	public static boolean s_useWorldUpdateThread = false;
	
	public static void load(String settings) {
		Logger.info("Loading settings: " + settings);
		
		StringReader sr = new StringReader(settings.replace("/", System.lineSeparator()));
		
		try {
			loadConfig(new BufferedReader(sr));
		} catch (IOException e) {
			Logger.warn("Error loading settings: " + e.getMessage());
			e.printStackTrace();
		}
		
		sr.close();
	}
	
	public static String save() {
		StringWriter sw = new StringWriter();
		
		try {
			saveConfig(new BufferedWriter(sw));
		} catch (IOException e) {
			Logger.warn("Error saving config: " + e.getMessage());
			e.printStackTrace();
		}
		
		try {
			sw.close();
		} catch (IOException e) {
			Logger.warn("Error saving config: " + e.getMessage());
			e.printStackTrace();
		}
		
		return sw.toString().replace(System.lineSeparator(), "/");
	}
	
	public static void loadConfig(){
		Logger.info("Loading config...");
		
		try{
			//BufferedReader b = new BufferedReader(new FileReader(new File(Save.class.getClassLoader().getResource("saves/" + file + ".txt").toURI())));
			
			File cfgFile = FileSystem.getFile("config.txt");
			
			if(!cfgFile.exists()){
				boolean succ = cfgFile.createNewFile();
				while(succ && !cfgFile.exists()){
					Thread.sleep(100);
				}
				
				BufferedWriter writer = new BufferedWriter(new FileWriter(cfgFile));
				
				writer.write(playerUpdateInterval + "");
				writer.newLine();
				writer.write(cubeUpdateInterval + "");
				writer.newLine();
				writer.write(s_useWorldUpdateThread + "");
				writer.newLine();
			     
			    writer.close();
			}
			
			InputStream is = new FileInputStream(cfgFile);
			
			BufferedReader b = new BufferedReader(new InputStreamReader(is));
			
			loadConfig(b);
			
			b.close();
		}catch(Exception e){
			Logger.warn("Error loading config: " + e.getMessage());
			e.printStackTrace();
		}
		
		Logger.info("Done!");
	}
	
	public static void loadConfig(BufferedReader b) throws IOException {
		
		String s = "";
		int line = 1;
		
		while((s = b.readLine()) != null){
			s = s.trim();
			
			try{
				if(line == 1){
					playerUpdateInterval = Integer.parseInt(s);
				}else if(line == 2){
					cubeUpdateInterval = Integer.parseInt(s);
				}else if(line == 3){
					s_useWorldUpdateThread = Boolean.parseBoolean(s);
				}
			}catch(Exception e){
				Logger.warn("Error loading settings: " + e.getMessage() + " on line " + line + " at \"" + s + "\".");
				e.printStackTrace();
			}
			line++;
		}
	}
	
	public static void saveConfig(BufferedWriter wr) throws IOException {
		for (int line = 1; line <= 2; line++) {
			
			String s = "";
			try{
				if(line == 1){
					s = playerUpdateInterval + "";
				}else if (line == 2) {
					s = cubeUpdateInterval + "";
				}else if (line == 3) {
					s = s_useWorldUpdateThread + "";
				}
			}catch (Exception e) {
				Logger.warn("Error saving config: " + e.getMessage() + " on line " + line + " at \"" + s + "\".");
				e.printStackTrace();
			}
			
			if(line != 1) wr.newLine();
			wr.write(s);
			wr.flush();
		}
	}
	
	public static void saveConfig() {
		Logger.info("Saving config...");
		
		try {
			//BufferedReader b = new BufferedReader(new FileReader(new File(Save.class.getClassLoader().getResource("saves/" + file + ".txt").toURI())));

			File cfgFile = FileSystem.getFile("config.txt");
			
			if(!cfgFile.exists()){
				boolean succ = cfgFile.createNewFile();
				while(succ && !cfgFile.exists()){
					Thread.sleep(100);
				}
			}
			
			BufferedWriter wr = new BufferedWriter(new FileWriter(cfgFile));

			saveConfig(wr);
			
			wr.close();
			
		}catch (Exception e) {
			Logger.warn("Error saving config: " + e.getMessage() + ".");
			e.printStackTrace();
		}
		
		Logger.info("Done!");
	}
}
