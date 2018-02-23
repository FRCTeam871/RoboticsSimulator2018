package me.pieking.game.gfx;

import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.font.TextAttribute;
import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;

public class Fonts {

	public static Font gamer = new Font("Arial", 0, 20);
	public static Font beefd = new Font("Arial", 0, 20);
	public static Font pixeled = new Font("Arial", 0, 20);
	public static Font pixelmix = new Font("Arial", 0, 20);
	public static Font pixelLCD = new Font("Arial", 0, 20);
	
	public static void init(){
		
		InputStream is = Fonts.class.getResourceAsStream("/fonts/Gamer.ttf");
		
		try {
			Font font2 = Font.createFont(Font.TRUETYPE_FONT, is);
			gamer = font2.deriveFont(0, 24);
		} catch (FontFormatException | IOException e) {
			e.printStackTrace();
		}
		
		try {
			is.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//===================
		
		is = Fonts.class.getResourceAsStream("/fonts/Beef'd.ttf");
		
		try {
			Font font2 = Font.createFont(Font.TRUETYPE_FONT, is);
			beefd = font2.deriveFont(0, 10);
		} catch (FontFormatException | IOException e) {
			e.printStackTrace();
		}
		
		try {
			is.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//===================
		
		is = Fonts.class.getResourceAsStream("/fonts/Pixeled.ttf");
		
		try {
			Font font2 = Font.createFont(Font.TRUETYPE_FONT, is);
			pixeled = font2.deriveFont(0, 10);
		} catch (FontFormatException | IOException e) {
			e.printStackTrace();
		}
		
		try {
			is.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//===================
		
		is = Fonts.class.getResourceAsStream("/fonts/pixelmix.ttf");
		
		try {
			Hashtable<TextAttribute, Object> map = new Hashtable<TextAttribute, Object>();
		    map.put(TextAttribute.TRACKING, 0.1);
			Font font2 = Font.createFont(Font.TRUETYPE_FONT, is);
			font2 = font2.deriveFont(map);
			pixelmix = font2.deriveFont(0, 12);
		} catch (FontFormatException | IOException e) {
			e.printStackTrace();
		}
		
		try {
			is.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		is = Fonts.class.getResourceAsStream("/fonts/Pixel LCD-7-2.ttf");
		
		try {
			Hashtable<TextAttribute, Object> map = new Hashtable<TextAttribute, Object>();
		    map.put(TextAttribute.TRACKING, 0.1);
			Font font2 = Font.createFont(Font.TRUETYPE_FONT, is);
			font2 = font2.deriveFont(map);
			pixelLCD = font2.deriveFont(0, 12);
		} catch (FontFormatException | IOException e) {
			e.printStackTrace();
		}
		
		try {
			is.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public static String execFormatValue(String str, String f) {

	  // Idk what this means

	  // Gewisse Schriften können die alten Texte nicht anzeigen.
	  // Beispielsweise sollte "A" für WingDings ein Victory Zeichen zeigen.
	  // Dies funktioniert neuerdings nur, wenn man in der Private Use Area nachschaut.
	  // http://www4.carthage.edu/faculty/ewheeler/GrafiX/LessonsAdvanced/wingdings.pdf
	  // http://www.fileformat.info/info/unicode/char/270c/index.htm
	  // http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6176474
		if (str != null && f != null) {
			Font font = new Font(f, Font.PLAIN, 1);
			boolean changed = false;
			char[] chars = str.toCharArray();
			for (int i = 0; i < chars.length; i++) {
				if (!font.canDisplay(chars[i])) {
					if (chars[i] < 0xF000) {
						chars[i] += 0xF000;
						changed = true;
					}
				}
			}
			if (changed)
				str = new String(chars);
		}
		return str;
	}
	
	public static String execFormatValue(String str, Font f) {

		  // Idk what this means

		  // Gewisse Schriften können die alten Texte nicht anzeigen.
		  // Beispielsweise sollte "A" für WingDings ein Victory Zeichen zeigen.
		  // Dies funktioniert neuerdings nur, wenn man in der Private Use Area nachschaut.
		  // http://www4.carthage.edu/faculty/ewheeler/GrafiX/LessonsAdvanced/wingdings.pdf
		  // http://www.fileformat.info/info/unicode/char/270c/index.htm
		  // http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6176474
			if (str != null && f != null) {
				Font font = f;
				boolean changed = false;
				char[] chars = str.toCharArray();
				for (int i = 0; i < chars.length; i++) {
					if (!font.canDisplay(chars[i])) {
						if (chars[i] < 0xF000) {
							chars[i] += 0xF000;
							changed = true;
						}
					}
				}
				if (changed)
					str = new String(chars);
			}
			return str;
		}
	
}
