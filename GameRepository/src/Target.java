import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;


public class Target extends GameObject implements ActionListener{
	private Image target;
	private ImageIcon icon;
	private int targetRadius;
	private int hitXPos;
	private int hitYPos;
	private int hitRadius;
	
	public Target(){
		targetRadius = 140;
		hitXPos = getScreenWidth() - 504;
		hitYPos = getScreenHeight() - 405;
		hitRadius = 50;
		
		// Image for target
		icon = new ImageIcon("target.fw.png");
		target = icon.getImage();
		target = target.getScaledInstance(targetRadius, targetRadius, Image.SCALE_SMOOTH);		
	}
	
	/**
	 * Get x-position of the hittable target
	 * @return 
	 */
	public int getHitXPos(){
		return hitXPos;
	}
	
	/**
	 * Get y-position of the hittable target
	 * @return 
	 */
	public int getHitYPos(){
		return hitYPos;
	}
	
	// Draw green background, white box over that, then the balls over that
	public void paint(Graphics g){		
	    Graphics2D g2d = (Graphics2D) g;
	    
	    // Draw target
	    g2d.drawImage(target, getScreenWidth() - 550, getScreenHeight() - 450, null);
	    
	    // Draw silver slab
	    g.setColor(Color.GRAY);
	    g.fillOval(hitXPos, hitYPos, hitRadius, hitRadius);
	    
		
		// End drawing balls left graphic representation
	}
}
