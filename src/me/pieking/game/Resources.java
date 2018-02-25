package me.pieking.game;

import java.net.MalformedURLException;
import java.net.URL;

public class Resources {

	public static URL getSound(String name) {
		URL u = null;
		try {
			u = FileSystem.getFile("sound/" + name).toURI().toURL();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
//		URL u = Resources.class.getClassLoader().getResource("sound/" + name);
		System.out.println(name + " " + u);
		return u;
	}

}
