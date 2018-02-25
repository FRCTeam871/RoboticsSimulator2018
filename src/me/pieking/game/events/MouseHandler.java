package me.pieking.game.events;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingUtilities;

import me.pieking.game.world.GameObject;

public class MouseHandler implements MouseListener, MouseWheelListener{

	private List<MouseButton> pressed = new ArrayList<MouseButton>();
	
	@Override
	public void mouseClicked(MouseEvent e) {
		
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		
	}

	@Override
	public void mousePressed(MouseEvent e) {
		MouseButton butt = MouseButton.UNKNOWN;
		if(SwingUtilities.isLeftMouseButton(e)) butt = MouseButton.LEFT;
		if(SwingUtilities.isMiddleMouseButton(e)) butt = MouseButton.MIDDLE;
		if(SwingUtilities.isRightMouseButton(e)) butt = MouseButton.RIGHT;
		
		if(!pressed.contains(butt)) pressed.add(butt);
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		MouseButton butt = MouseButton.UNKNOWN;
		if(SwingUtilities.isLeftMouseButton(e)) butt = MouseButton.LEFT;
		if(SwingUtilities.isMiddleMouseButton(e)) butt = MouseButton.MIDDLE;
		if(SwingUtilities.isRightMouseButton(e)) butt = MouseButton.RIGHT;
		
		if(pressed.contains(butt)) pressed.remove(butt);
	}
	
	public boolean isPressed(MouseButton button){
		return pressed.contains(button);
	}
	
	public boolean isLeftPressed(){
		return isPressed(MouseButton.LEFT);
	}
	
	public boolean isMiddlePressed(){
		return isPressed(MouseButton.MIDDLE);
	}
	
	public boolean isRightPressed(){
		return isPressed(MouseButton.RIGHT);
	}
	
	public static enum MouseButton{
		LEFT, MIDDLE, RIGHT, UNKNOWN;
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		double nz = GameObject.ZOOM_SCALE - e.getWheelRotation() * (GameObject.SCALE/10);
		
		GameObject.ZOOM_SCALE = Math.max(-50, Math.min(nz, 64));
	}
	
}
