package me.pieking.game.world;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import org.dyn4j.collision.Filter;
import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.AABB;
import org.dyn4j.geometry.Convex;
import org.dyn4j.geometry.Vector2;

import me.pieking.game.gfx.Graphics2DRenderer;
import me.pieking.game.gfx.Sprite;
import me.pieking.game.robot.component.Component;

public class GameObject extends Body {
	
	public static double DEFAULT_SCALE = 50.0;
	public static double SCALE = DEFAULT_SCALE;
	public static double DESIRED_SCALE = DEFAULT_SCALE;
	public static double ZOOM_SCALE = 0.0;
	
	public double personalScale = Float.MAX_VALUE;
	
	/** The color of the object */
	public Color color;
			
	public boolean shouldRender = true;
	
	public float desiredRotation = 0f;
	
	public long creationTime;
	public long destructionTime;
	public boolean pullable = true;
	
	public BodyType type = BodyType.DEFAULT;
	
	/**
	 * Default constructor.
	 */
	public GameObject() {
		creationTime = System.currentTimeMillis();
		destructionTime = -50;
		// randomly generate the color
		this.color = new Color(
				(float)Math.random() * 0.5f + 0.5f,
				(float)Math.random() * 0.5f + 0.5f,
				(float)Math.random() * 0.5f + 0.5f);
		
		super.setLinearDamping(0.5d);
		super.setAngularDamping(0.5d);
	}
	
	public void render(Graphics2D g) {
		render(g, new BasicStroke(1f));
	}
	
	/**
	 * Draws the body.
	 * <p>
	 * Only coded for polygons and circles.
	 * @param g the graphics object to render to
	 */
	public void renderNoTranslate(Graphics2D g, Stroke str) {
		if(!shouldRender) return;
		// save the original transform
		AffineTransform ot = g.getTransform();
		
		// transform the coordinate system from world coordinates to local coordinates
		AffineTransform lt = new AffineTransform();
		double scale = (personalScale != Float.MAX_VALUE ? personalScale : SCALE);
		//System.out.println(scale);
//		lt.translate(this.transform.getTranslationX() * scale, this.transform.getTranslationY() * scale);
		lt.rotate(this.transform.getRotation());
		
		// apply the transform
		g.transform(lt);
		
		Composite c = g.getComposite();
		
		//System.out.println(destructionTime);
		
		if(destructionTime != -50){
			float fadeTime = 1000f;
			long now = System.currentTimeMillis();
			//System.out.println(destructionTime - now);
			if(destructionTime - now <= fadeTime){
				float maxOpacity = color.getAlpha() / 255f;
				float opacity = ((destructionTime - now) / fadeTime) * maxOpacity;
				//System.out.println(opacity);
				
				if(opacity < 0) opacity = 0;
				if(opacity > maxOpacity) opacity = maxOpacity;
				
				g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
				//color = new Color(color.getRed(), color.getGreen(), color.getBlue(), (int)(255 * opacity));
			}
		}
		
		// loop over all the body fixtures for this body
		for (BodyFixture fixture : this.fixtures) {
			// get the shape on the fixture
			Convex convex = fixture.getShape();
			//System.out.println(convex.getClass());
			Stroke st = g.getStroke();
			g.setStroke(str);
			Graphics2DRenderer.render(g, convex, scale, color);
			g.setStroke(st);
		}
		
		g.setComposite(c);
		
		// set the original transform
		g.setTransform(ot);
	}
	
	/**
	 * Draws the body.
	 * <p>
	 * Only coded for polygons and circles.
	 * @param g the graphics object to render to
	 */
	public void render(Graphics2D g, Stroke str) {
		if(!shouldRender) return;
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		// save the original transform
		AffineTransform ot = g.getTransform();
		
		// transform the coordinate system from world coordinates to local coordinates
		AffineTransform lt = new AffineTransform();
		double scale = (personalScale != Float.MAX_VALUE ? personalScale : SCALE);
		//System.out.println(scale);
		lt.translate(this.transform.getTranslationX() * scale, this.transform.getTranslationY() * scale);
		lt.rotate(this.transform.getRotation());
		
		// apply the transform
		g.transform(lt);
		
		Composite c = g.getComposite();
		
		//System.out.println(destructionTime);
		
		if(destructionTime != -50){
			float fadeTime = 1000f;
			long now = System.currentTimeMillis();
			//System.out.println(destructionTime - now);
			if(destructionTime - now <= fadeTime){
				float maxOpacity = color.getAlpha() / 255f;
				float opacity = ((destructionTime - now) / fadeTime) * maxOpacity;
				//System.out.println(opacity);
				
				if(opacity < 0) opacity = 0;
				if(opacity > maxOpacity) opacity = maxOpacity;
				
				g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
				//color = new Color(color.getRed(), color.getGreen(), color.getBlue(), (int)(255 * opacity));
			}
		}
		
		// loop over all the body fixtures for this body
		for (BodyFixture fixture : this.fixtures) {
			// get the shape on the fixture
			Convex convex = fixture.getShape();
			//System.out.println(convex.getClass());
			Stroke st = g.getStroke();
			g.setStroke(str);
			Graphics2DRenderer.render(g, convex, scale, color);
			Vector2 cen = convex.getCenter();
			
			AffineTransform tr = g.getTransform();
			g.rotate(-getTransform().getRotation());
			
			g.setColor(Color.BLACK);
			Filter f = fixture.getFilter();
			if(f instanceof GameObjectFilter) {
				g.drawString(((GameObjectFilter) f).type.toString(), (int)cen.x, (int)cen.y);
			}
			g.setTransform(tr);
			
			g.setStroke(st);
		}
		
		g.setComposite(c);
		
		// set the original transform
		g.setTransform(ot);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
	}
	
	/**
	 * Draws the body.
	 * <p>
	 * Only coded for polygons and circles.
	 * @param g the graphics object to render to
	 */
	public void render(Graphics2D g, Stroke str, Sprite spr, int sprW, int sprH, Color col) {
		if(!shouldRender) return;
		
		// save the original transform
		AffineTransform ot = g.getTransform();
		
		// transform the coordinate system from world coordinates to local coordinates
		AffineTransform lt = new AffineTransform();
		double scale = (personalScale != Float.MAX_VALUE ? personalScale : SCALE);
		//System.out.println(scale);
		lt.translate(this.transform.getTranslationX() * scale, this.transform.getTranslationY() * scale);
		lt.rotate(this.transform.getRotation());
		
		// apply the transform
		g.transform(lt);
		
		Composite c = g.getComposite();
		
		//System.out.println(destructionTime);
		
		if(destructionTime != -50){
			float fadeTime = 1000f;
			long now = System.currentTimeMillis();
			//System.out.println(destructionTime - now);
			if(destructionTime - now <= fadeTime){
				float maxOpacity = color.getAlpha() / 255f;
				float opacity = ((destructionTime - now) / fadeTime) * maxOpacity;
				//System.out.println(opacity);
				
				if(opacity < 0) opacity = 0;
				if(opacity > maxOpacity) opacity = maxOpacity;
				
				g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
				//color = new Color(color.getRed(), color.getGreen(), color.getBlue(), (int)(255 * opacity));
			}
		}
		
		// loop over all the body fixtures for this body
		List<BodyFixture> bffs = new ArrayList<>();
		bffs.addAll(fixtures);
			
		Stroke st = g.getStroke();
		g.setStroke(str);
		scale += 2;
		
		int w = (int)(Component.unitSize * scale * sprW);
		int h = (int)(Component.unitSize * scale * sprH);
		
		g.drawImage(spr.getImage(), -(int)(Component.unitSize/2 * scale), -(int)(Component.unitSize/2 * scale), w, h, null);
		Shape s = spr.getShape();
		AffineTransform tra = AffineTransform.getTranslateInstance(-(int)(Component.unitSize/2 * scale), -(int)(Component.unitSize/2 * scale));
		tra.scale(w/(double)spr.getWidth(), h/(double)spr.getHeight());
		s = tra.createTransformedShape(s);
		
		g.setColor(col);
		g.fill(s);
//			Graphics2DRenderer.render(g, convex, scale, color);
		g.setStroke(st);
		
		g.setComposite(c);
		
		// set the original transform
		g.setTransform(ot);
	}
	/**
	 * Draws the body.
	 * <p>
	 * Only coded for polygons and circles.
	 * @param g the graphics object to render to
	 */
	public void render(Graphics2D g, Stroke str, Sprite spr, double sprW, double sprH) {
		if(!shouldRender) return;
		
		// save the original transform
		AffineTransform ot = g.getTransform();
		
		// transform the coordinate system from world coordinates to local coordinates
		AffineTransform lt = new AffineTransform();
		double scale = (personalScale != Float.MAX_VALUE ? personalScale : SCALE);
		//System.out.println(scale);
		lt.translate(this.transform.getTranslationX() * scale, this.transform.getTranslationY() * scale);
		lt.rotate(this.transform.getRotation());
		
		// apply the transform
		g.transform(lt);
		
		Composite c = g.getComposite();
		
		//System.out.println(destructionTime);
		
		if(destructionTime != -50){
			float fadeTime = 1000f;
			long now = System.currentTimeMillis();
			//System.out.println(destructionTime - now);
			if(destructionTime - now <= fadeTime){
				float maxOpacity = color.getAlpha() / 255f;
				float opacity = ((destructionTime - now) / fadeTime) * maxOpacity;
				//System.out.println(opacity);
				
				if(opacity < 0) opacity = 0;
				if(opacity > maxOpacity) opacity = maxOpacity;
				
				g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
				//color = new Color(color.getRed(), color.getGreen(), color.getBlue(), (int)(255 * opacity));
			}
		}
		
		// loop over all the body fixtures for this body
		Stroke st = g.getStroke();
		g.setStroke(str);
		scale += 2;
		g.drawImage(spr.getImage(), -(int)(Component.unitSize/2 * scale), -(int)(Component.unitSize/2 * scale), (int)(Component.unitSize * scale * sprW), (int)(Component.unitSize * scale * sprH), null);
//			Graphics2DRenderer.render(g, convex, scale, color);
		g.setStroke(st);
		
		g.setComposite(c);
		
		// set the original transform
		g.setTransform(ot);
	}
	
	public Vector2 convertVector(Vector2 v){
		AffineTransform lt = new AffineTransform();
		double scale = (personalScale != Float.MAX_VALUE ? personalScale : SCALE);
		lt.translate(this.transform.getTranslationX() * scale, this.transform.getTranslationY() * scale);
		lt.rotate(this.transform.getRotation());
		
		return applyTransform(v, lt);
	}
	
	@Override
	public void translate(double x, double y) {
		double scale = (personalScale != Float.MAX_VALUE ? personalScale : 1);
		super.translate(x / scale, y / scale);
	}
	
	public Vector2 applyTransform(Vector2 v, AffineTransform t){
		Point2D p = new Point2D.Double(v.x, v.y);
		
		p = t.transform(p, p);
		
		Vector2 vn = new Vector2(p.getX(), p.getY());
		
		return vn;
	}
	
	public double containsFixture(GameObject bod) {
		List<BodyFixture> fixture = bod.getFixtures();
		for(BodyFixture f : fixture){
			for(BodyFixture fi : fixtures){
				//System.out.println(((Rectangle)fi.getShape()).getWidth() * 45);
				
				float rot = (float) bod.getTransform().getRotation();
				f.getShape().rotate(rot);
				AABB ab1 = f.getShape().createAABB();
				f.getShape().rotate(-rot);
				ab1.translate(bod.getWorldCenter().x, bod.getWorldCenter().y);
				//ab1 = new AABB(ab1.getMinX() * 45, ab1.getMinY() * 45, ab1.getMaxX() * 45, ab1.getMaxY() * 45);
				//System.out.println(ab1.getWidth() + " " + ab1.getHeight());
				
				rot = (float) getTransform().getRotation();
				fi.getShape().rotate(rot);
				AABB ab2 = fi.getShape().createAABB();
				fi.getShape().rotate(-rot);
				//System.out.println(getWorldCenter().x + " " + getWorldCenter().y);
				//ab2.translate(getWorldCenter().x, getWorldCenter().y);
				//ab2 = new AABB(ab2.getMinX() * 45, ab2.getMinY() * 45, ab2.getMaxX() * 45, ab2.getMaxY() * 45);
				
				//System.out.println();
				
				//System.out.println(ab1 + " " + ab2);
				if(ab1.overlaps(ab2)){
					return ab1.getIntersection(ab2).getArea();
				}
			}
		}
		return 0d;
	}
	
//	public GameObjectData getData(){
//		GameObjectData data = new GameObjectData();
//		data.desiredRotation = desiredRotation;
//		
//		return data;
//	}
	
	public static enum BodyType {
		DEFAULT,
		SHIP,
		BULLET,
		PARTICLE, SCALE_PLATFORM;
	}
	
}
