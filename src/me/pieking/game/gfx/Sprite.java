package me.pieking.game.gfx;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Shape;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.LookupOp;
import java.util.ArrayList;
import java.util.List;

import me.pieking.game.Logger;
import me.pieking.game.Utils;

public class Sprite {

	public static List<Sprite> loadedSprites = new ArrayList<Sprite>();
	
	public String path;
	private BufferedImage image;
	private Shape clip;
	
	public boolean error = false;
	public boolean relativeToTexures = true;
	
	public boolean gaveSource = false;
	
	public Sprite(){
		this("nothing.png");
	}
	
	public Sprite(String path){
		this(path, true);
	}
	
	public Sprite(String path, boolean relativeToTexures){
		this.path = path;
		this.relativeToTexures = relativeToTexures;
		reload();
		loadedSprites.add(this);
	}
	
	public Sprite(Image im){
		this.image = toCompatibleImage(Images.toBufferedImage(im));
		gaveSource = true;
	}
	
	public BufferedImage getImage(){
		return image;
	}
	
	/**
	 * This method is useless now since getImage() returns a BufferedImage by default.
	 * 
	 * @deprecated
	 */
	public BufferedImage getBuffered(){
		return getImage();
	}
	
	@SuppressWarnings("deprecation")
	public void reload(){
		if(!gaveSource){
    		if(relativeToTexures){
    			image = toCompatibleImage(Images.toBufferedImage(Images.getImage(path)));
    		}else{
    			image = toCompatibleImage(Images.toBufferedImage(Images.getImagePath(path)));
    		}
		}
	}
	
	public static void reloadAll(){
		for(Sprite s : loadedSprites){
			s.reload();
		}
		Logger.info("Reloaded textures.");
	}
	
	public Shape getShape(){
		if(clip == null){
			clip = Utils.createArea(Images.toBufferedImage(getImage()), 1);
		}
		
		return clip;
	}
	
	public BufferedImage getImageAlpha(float alpha){
		
		if(alpha > 1){
			alpha = 1;
		}else if(alpha < 0){
			alpha = 0;
		}
		
		if(alpha == 1f) return getImage();
		
		BufferedImage orig = Images.toBufferedImage(getImage());
		
		BufferedImage resizedImage = new BufferedImage(orig.getWidth(), orig.getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = resizedImage.createGraphics();
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
		g.drawImage(orig, 0, 0, orig.getWidth(), orig.getHeight(), null);
		g.dispose();
		
		return resizedImage;
	}

	public int getWidth() {
		return getImage().getWidth(null);
	}
	
	public int getHeight() {
		return getImage().getHeight(null);
	}
	
	private static BufferedImage toCompatibleImage(BufferedImage image){
	    // obtain the current system graphical settings
	    GraphicsConfiguration gfx_config = GraphicsEnvironment.
	        getLocalGraphicsEnvironment().getDefaultScreenDevice().
	        getDefaultConfiguration();

	    /*
	     * if image is already compatible and optimized for current system 
	     * settings, simply return it
	     */
	    if (image.getColorModel().equals(gfx_config.getColorModel()))
	        return image;

	    // image is not optimized, so create a new image that is
	    BufferedImage new_image = gfx_config.createCompatibleImage(
	            image.getWidth(), image.getHeight(), image.getTransparency());

	    // get the graphics context of the new image to draw the old image on
	    Graphics2D g2d = (Graphics2D) new_image.getGraphics();

	    // actually draw the image and dispose of context no longer needed
	    g2d.drawImage(image, 0, 0, null);
	    g2d.dispose();

	    // return the new optimized image
	    return new_image; 
	}
	
	public Sprite replace(Color from, Color to){
		BufferedImage out = getImageAlpha(1f);
		if(image != null && out != null){
			BufferedImageOp lookup = new LookupOp(new ColorMapper(from, to), null);
			lookup.filter(image, out);
		}
		return new Sprite(out);
	}
	
	public BufferedImage scaleImage(int w, int h){
		BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
		Graphics2D g2d = out.createGraphics();

		g2d.drawImage(getImage(), 0, 0, w, h, null);
		
		g2d.dispose();
		return out;
	}
	
	public Sprite scale(int w, int h){
		BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
		Graphics2D g2d = out.createGraphics();

		g2d.drawImage(getImage(), 0, 0, w, h, null);
		
		g2d.dispose();
		return new Sprite(out);
	}
	
}
