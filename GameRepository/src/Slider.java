//****************************************************************************************
//										Slider
//----------------------------------------------------------------------------------------
//the slider class manages the back and forth movement of the power par then pass a variable 
//once the space bar is pressed. After which the pendant paused for 3 seconds to simulate
//possible outcomes of the throw. this will be changed it the future. interals to signify 
//the success of a shot is still pending. 
//****************************************************************************************

import java.util.*;
import java.awt.*;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import javax.swing.Timer;

public class Slider extends GameObject implements ActionListener {
	
	private int x;
	private int y;
	private final int startingXVelocity;
	private int lastVelocity;
	private int xVelocity;
	private int barSize;
	private int greenValue;
	private Image slider;
	private Image pendant;
	private ImageIcon icon;
	private Timer time;
	
	public Slider(){
		x = 5;
		y = 5;
		startingXVelocity = 10;
		xVelocity = 10;
		barSize = 10;
		
		icon = new ImageIcon("slider.fw.png");
		slider = icon.getImage();
		slider = slider.getScaledInstance(450, 250, Image.SCALE_SMOOTH);

		icon = new ImageIcon("Slider_part_a.fw.png");
		pendant = icon.getImage();
		pendant = pendant.getScaledInstance(20, 40, Image.SCALE_SMOOTH);

		greenValue = 170;
		
		time = new Timer(20, this);
		time.start();

	}

	//once the update is called if the pendulimn reaches either end of the bar 
	//it would swing the other way. 
	//NB: an action listener will be placed in the main class to stop it once the 
	//space bar is pressed.
	public void update(){
		if (getCanWeThrow()){

			x = x + xVelocity;

			if(x > 310){
				xVelocity = -(startingXVelocity);
			}

			if(x < 15){
				xVelocity = startingXVelocity;
			}
		}
	}

	public void actionPerformed(ActionEvent e){
		update();
		repaint();
	}
	
	/*
 	 * The following two classes
 	 * can be used by the throwing
 	 * class to track if the throw
 	 * should be a hit or miss
 	 * Zach
 	 */	 

	public int getStartingWidth(){
		return x;
	}
	
	public int getEndingWidth(){
		return x + barSize;
	}

	public void paint(Graphics g){
		Graphics2D g2d = (Graphics2D) g;
		//g.setColor(Color.BLUE);
		//g.fillRect(x,y, Math.abs(barSize), 50);
		g2d.drawImage(slider, -50, -90, null);
		g2d.drawImage(pendant, x, y, null);
	}

	public int getLastVelocity(){
		return lastVelocity;
	}
	
	
	/**
	 * This function returns a value between 1 and 5 ranking how close the pendant is to the green
	 * @return
	 */
	public int getHitRanking(){
		System.out.println(x);
		if(x >= greenValue - 25 && x <= greenValue + 25)
			return 1;
		else if(x >= greenValue - 100 && x < greenValue - 25 || x > greenValue + 25 && x <= greenValue + 100)
			return 2;
		else if(x >= greenValue - 200 && x < greenValue - 100 || x > greenValue + 100 && x <= greenValue + 200)
			return 3;
		else
			return 4;		
	}

	public void setxVelocity(int vel){
		lastVelocity = xVelocity;
		xVelocity = vel;
	}
}
