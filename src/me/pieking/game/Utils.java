package me.pieking.game;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.json.JSONObject;

public class Utils {

	/**
	 * Opens a webpage on the user's browser.
	 * @param uri The URI of the webpage
	 * @throws Exception 
	 */
	public static void openWebpage(URI uri) throws Exception {
	    Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
	    if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
	    	desktop.browse(uri);
	    }
	}
	
	/**
	 * Opens a webpage on the user's browser.
	 * @param url The URL of the webpage
	 * @throws URISyntaxException 
	 */
	public static void openWebpage(URL url) throws Exception {
		openWebpage(url.toURI());
	}
	
	/**
	 * Gets a random element from the array.
	 * 
	 * @param strings List from which an element will be selected
	 */
	public static String randString(String[] strings){
    	int rand = Rand.range(0, strings.length - 1);
    	return strings[rand];
    }
	
	/**
     * Theta is in radians.
     */
    public static Point2D.Float polarToCartesian(float theta, float distance){

    	float x = (float) (Math.cos( theta ) * distance);
    	float y = (float) (Math.sin( theta ) * distance);
    	
    	return new Point2D.Float(x, y);
    }
    
    /**
     * Theta is in radians.
     */
    public static Point2D.Float cartesianToPolar(float x, float y){

    	float radius = (float) Math.sqrt((x * x) + (y * y));
    	float theta  = (float) Math.atan(y / x);
    	
    	return new Point2D.Float(theta, radius);
    }
    
    /**
     * Convert degrees to radians. Same as Math.toRadians()
     * 
     * @see Math#toRadians
     */
    public static double degToRad(double deg){
		return (deg*Math.PI)/180;
	}
	
    /**
     * Convert radians to degrees. Same as Math.toDegrees()
     * 
     * @see Math#toDegrees
     */
	public static double radToDeg(double rad){
		return (rad*180)/Math.PI;
	}
	
	/**
	 * Get a rainbow color from the time. (My favorite method ;P)
	 * 
	 * @param frequency The rate of change
	 * @param timeOffset If you want to offset the time
	 */
	public static Color rainbowColor(float frequency, int timeOffset){
		
		int i = Game.getTime() + timeOffset;
		
		float red   = (float) (Math.sin(frequency*i + 0) * 127 + 128);
		float green = (float) (Math.sin(frequency*i + 2) * 127 + 128);
		float blue  = (float) (Math.sin(frequency*i + 4) * 127 + 128);
		
		return new Color(red / 255f, green / 255f, blue / 255f);
	}
	
	public static Object getInstance(String name, String... params){
		name = "me.pieking.game." + name;
		Class<?> c = null;
		
		Class<?>[] paramTypes = null;
		
		if(params != null){
			paramTypes = new Class<?>[params.length];
			for(int i = 0; i < paramTypes.length; i++){
				paramTypes[i] = String.class;
			}
		}
		
		try {
			c = Class.forName(name);
		} catch (ClassNotFoundException e) {
			Logger.warn("Could not find class: |" + name + "|");
			e.printStackTrace();
		}
		try {
			if(paramTypes == null){
				return c.newInstance();
			}else{
				return c.getDeclaredConstructor(paramTypes).newInstance((Object[])params);
			}
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			Logger.warn("Could not create instance of class: |" + name + "|");
			e.printStackTrace();
		}
		return null;
	}
	
	public static float average(int[] data) {  
	    int sum = 0;

	    for (int d : data) sum += d;
	    
	    float average = 1.0f * sum / data.length;
	
	    return average;
	}
	
	public static float average(float[] data) {  
	    int sum = 0;

	    for (float d : data) sum += d;
	    
	    float average = 1.0f * sum / data.length;
	
	    return average;
	}
	
	/**
	 * Gets the outline of an image.
	 */
	public static Area createArea(BufferedImage image, int maxTransparency) {
        Area area = new Area();
        Rectangle rectangle = new Rectangle();
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                int rgb = image.getRGB(x, y);
                rgb = rgb >>> 24;
                if (rgb >= maxTransparency) {
                    rectangle.setBounds(x, y, 1, 1);
                    area.add(new Area(rectangle));
                }
            }
        }
        return area;
    }
	
	public static Point2D rotateAboutPoint(Point2D toRotate, Point2D center, double angle){
		double[] pt = {toRotate.getX(), toRotate.getY()};
		AffineTransform.getRotateInstance(Math.toRadians(angle), center.getX(), center.getY()).transform(pt, 0, pt, 0, 1); // specifying to use this double[] to hold coords
		double newX = pt[0];
		double newY = pt[1];
		
		return new Point2D.Double(newX, newY);
	}
	
	public static List<String> wrapString(String str, int lineWidth, FontMetrics fm){
		List<String> wrapped = new ArrayList<String>();
		
		String currLine = "";
		for(String s : str.split(" ")){
			if(fm.stringWidth(currLine + s + " ") < lineWidth){
				currLine = currLine + s + " ";
			}else{
				wrapped.add(currLine);
				currLine = "" + s + " ";
			}
		}
		wrapped.add(currLine);
		
		return wrapped;
	}
	
	public static String getStackTrace(final Throwable throwable) {
	     final StringWriter sw = new StringWriter();
	     final PrintWriter pw = new PrintWriter(sw, true);
	     throwable.printStackTrace(pw);
	     return sw.getBuffer().toString();
	}
	
	public static JSONObject getJSON(String path){
		String json = "";
		
		BufferedReader br = new BufferedReader(new InputStreamReader(Utils.class.getClassLoader().getResourceAsStream(path)));
		
		String line;
		try {
			while((line = br.readLine()) != null){
				json = json + line;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		JSONObject jo = new JSONObject(json);
		
		return jo;
	}
	
	public static JSONObject getJSON(URL path){
		String json = "";
		
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(path.openStream()));
		} catch (NullPointerException e1) {
			Logger.warn("json file not found at " + path);
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		String line;
		try {
			while((line = br.readLine()) != null){
				json = json + line;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		JSONObject jo = new JSONObject(json);
		
		return jo;
	}
	
	public static JSONObject tbaAPICall(URL path){
		String json = "";
		
		BufferedReader br = null;
		try {
			HttpURLConnection con = (HttpURLConnection) path.openConnection();
	        con.setRequestMethod("GET");
	        con.addRequestProperty("X-TBA-Auth-Key", "zxkl7V7cuY5U8nJyBU9wYbzID8LL177rFM3f05V8aSCcobi5Y2CGEnSYaDocN9ok");
	        con.addRequestProperty("User-Agent", "TBA-API");
	        
			br = new BufferedReader(new InputStreamReader(con.getInputStream()));
		} catch (NullPointerException e1) {
			Logger.warn("json file not found at " + path);
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		String line;
		try {
			while((line = br.readLine()) != null){
				json = json + line;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		JSONObject jo = new JSONObject(json);
		
		return jo;
	}
	
	public static JSONObject getTeamInfo(int teamNum) throws MalformedURLException {
		return tbaAPICall(new URL("https://www.thebluealliance.com/api/v3/team/frc" + teamNum + "/simple"));
	}
	
	public static JSONObject getJSON(InputStream stream){
		String json = "";
		
		BufferedReader br = null;
		br = new BufferedReader(new InputStreamReader(stream));
		
		String line;
		try {
			while((line = br.readLine()) != null){
				json = json + line;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		JSONObject jo = new JSONObject(json);
		
		return jo;
	}
	
	public static void drawDialogueBox(Graphics g, int x, int y, int w, int h){
		Graphics2D g2d = (Graphics2D) g;
		
		g.setColor(Color.BLACK);
		g.fillRect(x, y, w, h);
		
		g.setColor(Color.WHITE);
		
		g2d.setStroke(new BasicStroke(6));
		
		g.drawRect(x, y, w, h);
		
		g2d.setStroke(new BasicStroke(1));
	}
	
	public static void drawDialogueBox(Graphics g, Rectangle r){
		Graphics2D g2d = (Graphics2D) g;
		
		int x = r.x;
		int y = r.y;
		int w = r.width;
		int h = r.height;
		
		g.setColor(Color.BLACK);
		g.fillRect(x, y, w, h);
		
		g.setColor(Color.WHITE);
		
		g2d.setStroke(new BasicStroke(6));
		
		g.drawRect(x, y, w, h);
		
		g2d.setStroke(new BasicStroke(1));
	}
	
	public static String toTitleCase(String input) {
	    StringBuilder titleCase = new StringBuilder();
	    boolean nextTitleCase = true;

	    for (char c : input.toCharArray()) {
	        if (Character.isSpaceChar(c)) {
	            nextTitleCase = true;
	        } else if (nextTitleCase) {
	            c = Character.toTitleCase(c);
	            nextTitleCase = false;
	        }

	        titleCase.append(c);
	    }

	    return titleCase.toString();
	}
	
//	public static JSONObject getJSON(String path){
//		String json = "";
//		
//		BufferedReader br = new BufferedReader(new InputStreamReader(Utils.class.getClassLoader().getResourceAsStream(path)));
//		
//		String line;
//		try {
//			while((line = br.readLine()) != null){
//				json = json + line;
//			}
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		
//		System.out.println(json);
//		
//		JSONObject jo = new JSONObject(json);
//		
//		return jo;
//	}
//	
//	public static JSONObject getJSON(URL path){
//		String json = "";
//		
//		BufferedReader br = null;
//		try {
//			br = new BufferedReader(new InputStreamReader(path.openStream()));
//		} catch (NullPointerException e1) {
//			Logger.warn("json file not found at " + path);
//			e1.printStackTrace();
//		} catch (IOException e1) {
//			e1.printStackTrace();
//		}
//		
//		String line;
//		try {
//			while((line = br.readLine()) != null){
//				json = json + line;
//			}
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		
//		//System.out.println(json);
//		
//		JSONObject jo = new JSONObject(json);
//		
//		return jo;
//	}
//	
//	public static JSONObject getJSON(InputStream stream){
//		String json = "";
//		
//		BufferedReader br = null;
//		br = new BufferedReader(new InputStreamReader(stream));
//		
//		String line;
//		try {
//			while((line = br.readLine()) != null){
//				json = json + line;
//			}
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		
//		System.out.println(json);
//		
//		JSONObject jo = new JSONObject(json);
//		
//		return jo;
//	}
	
	public static boolean testIntersection(Shape shapeA, Shape shapeB) {
//		Area areaA = new Area(shapeA);
//		areaA.intersect(new Area(shapeB));
//		return !areaA.isEmpty();
		return shapeA.getBounds().intersects(shapeB.getBounds());
	}
	
	/**
	 * Calculates the angle from centerPt to targetPt in degrees.
	 * The return should range from [0,360), rotating CLOCKWISE, 
	 * 0 and 360 degrees represents NORTH,
	 * 90 degrees represents EAST, etc...
	 *
	 * Assumes all points are in the same coordinate space.  If they are not, 
	 * you will need to call SwingUtilities.convertPointToScreen or equivalent 
	 * on all arguments before passing them  to this function.
	 *
	 * @param centerPt   Point we are rotating around.
	 * @param targetPt   Point we want to calcuate the angle to.  
	 * @return angle in degrees.  This is the angle from centerPt to targetPt.
	 */
	public static double calcRotationAngleInDegrees(Point centerPt, Point targetPt) {
		// calculate the angle theta from the deltaY and deltaX values
		// (atan2 returns radians values from [-PI,PI])
		// 0 currently points EAST.
		// NOTE: By preserving Y and X param order to atan2, we are expecting
		// a CLOCKWISE angle direction.
		double theta = Math.atan2(targetPt.y - centerPt.y, targetPt.x - centerPt.x);

		// rotate the theta angle clockwise by 90 degrees
		// (this makes 0 point NORTH)
		// NOTE: adding to an angle rotates it clockwise.
		// subtracting would rotate it counter-clockwise
		theta += Math.PI / 2.0;

		// convert from radians to degrees
		// this will give you an angle from [0->270],[-180,0]
		double angle = Math.toDegrees(theta);

		// convert to positive range [0-360)
		// since we want to prevent negative angles, adjust them now.
		// we can assume that atan2 will not return a negative value
		// greater than one partial rotation
		if (angle < 0) {
			angle += 360;
		}

		return angle;
	}
	
	public static Shape translate(Location newLoc, Shape shape) {
		return AffineTransform.getTranslateInstance(newLoc.x, newLoc.y).createTransformedShape(shape);
	}
	
	public static Shape scale(float factor, Shape shape) {
		return AffineTransform.getScaleInstance(factor, factor).createTransformedShape(shape);
	}
	
//	public static Shape translate(Location newLoc, Shape shape) {
//		return AffineTransform.getTranslateInstance(newLoc.x, newLoc.y).createTransformedShape(shape);
//	}
	
	/** round n down to nearest multiple of m 
	 *  https://gist.github.com/aslakhellesoy/1134482
	 */
	public static int roundDown(int n, int m) {
	    return n >= 0 ? (n / m) * m : ((n - m + 1) / m) * m;
	}

	/** round n up to nearest multiple of m 
	 *  https://gist.github.com/aslakhellesoy/1134482
	 */
	public static int roundUp(int n, int m) {
	    return n >= 0 ? ((n + m - 1) / m) * m : (n / m) * m;
	}
	
	public static String toHex(Color col){
		return String.format("#%02x%02x%02x", col.getRed(), col.getGreen(), col.getBlue());
	}
	
	public static Color toColor(String hex){
		return Color.decode(hex);
	}
	
	public static byte[] compress(String str) throws Exception {
		if (str == null || str.length() == 0) {
			return new byte[]{};
		}
		//System.out.println("String length : " + str.length());
		ByteArrayOutputStream obj = new ByteArrayOutputStream();
		GZIPOutputStream gzip = new GZIPOutputStream(obj);
		gzip.write(str.getBytes("UTF-8"));
		gzip.close();
		return obj.toByteArray();
	}

	public static String decompress(byte[] bytes) throws Exception {
		if (bytes == null || bytes.length == 0) {
			return "";
		}
		GZIPInputStream gis = new GZIPInputStream(new ByteArrayInputStream(bytes));
		Reader reader = new InputStreamReader(gis, "UTF-8");
	    Writer writer = new StringWriter();
	    char[] buffer = new char[10240];
	    for (int length = 0; (length = reader.read(buffer)) > 0;) {
	        writer.write(buffer, 0, length);
	    }
		return writer.toString();
	}
	
	public static String addElipses(String input, int maxLineLength) {
	    StringTokenizer tok = new StringTokenizer(input, ",");
	    StringBuilder output = new StringBuilder(input.length());
	    int lineLen = 0;
	    while (tok.hasMoreTokens()) {
	        String word = tok.nextToken() + ",";

	        if (lineLen + word.length() > maxLineLength) {
	        	if (output.length() > 0) {
	        		output.setLength(output.length() - 1);
        		}
	        	return output.toString() + " ...";
	        }
	        output.append(word);
	        lineLen += word.length();
	    }
	    if (output.length() > 0) {
    		output.setLength(output.length() - 1);
		}
	    return output.toString();
	}
	
	public static Color fade(Color a, Color b, float thru){
		int red = (int)Math.abs((thru * b.getRed()) + ((1 - thru) * a.getRed()));
		int green = (int)Math.abs((thru * b.getGreen()) + ((1 - thru) * a.getGreen()));
		int blue = (int)Math.abs((thru * b.getBlue()) + ((1 - thru) * a.getBlue()));
		int alpha = (int)Math.abs((thru * b.getAlpha()) + ((1 - thru) * a.getAlpha()));
		return new Color(red, green, blue, alpha);
	}
	
}

