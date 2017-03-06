//							Dunkees
//This class controls the movement of the dunkees, and repaints them onto the screen 
//usisng its paint method to repaint its components which is called by super in the GameObject 
//class.
//3 images are imported in from the res folder, and are therefore utilized in the code
//****************************************************************************************
import java.util.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

public class Dunkees extends GameObject implements ActionListener{
	private final int TRUSTEE_WIDTH = 75;
	private final int TRUSTEE_LENGTH = 100;
	private final int DUNKTANK_WIDTH = 100;
	private final int DUNKTANK_LENGTH = 200;
	private int path = 3;
	private int initial_num_of_dunkees;
	private int num_of_dunkees;
	private int max_to_dunk;
	private int dunkeesLeft;
	private int rightmostTrustee;
	private int trustee_movement = 0;
	private Image trusteeLeft;
	private Image trusteeRight;
	private Image trusteeCenter;
	private Image dunktank;
	private ImageIcon icon;
	private boolean lastRenderedLeft = true;
	private Timer time;

	public Dunkees(int initial_num_of_dunkees){
		this.initial_num_of_dunkees = initial_num_of_dunkees;
		max_to_dunk = initial_num_of_dunkees;
		rightmostTrustee = (getScreenWidth()/3) - TRUSTEE_WIDTH;
		//Initiallizing all images
		icon = new ImageIcon("gross_right.png");
		trusteeLeft = icon.getImage();
		trusteeLeft = trusteeLeft.getScaledInstance(TRUSTEE_WIDTH, TRUSTEE_LENGTH, Image.SCALE_SMOOTH);

		icon = new ImageIcon(".gross_left.png");
		trusteeRight = icon.getImage();
		trusteeRight = trusteeRight.getScaledInstance(TRUSTEE_WIDTH, TRUSTEE_LENGTH, Image.SCALE_SMOOTH);

		icon = new ImageIcon("gross_stand.png");
		trusteeCenter = icon.getImage();
		trusteeCenter= trusteeCenter.getScaledInstance(TRUSTEE_WIDTH, TRUSTEE_LENGTH, Image.SCALE_SMOOTH);

		icon = new ImageIcon("Dunk_Tank1.png");
		dunktank = icon.getImage();
		//dunktank = dunktank.getScaledInstance(DUNKTANK_WIDTH, DUNKTANK_LENGTH, Image.SCALE_SMOOTH);

		time = new Timer(50, this);
		time.start();
	}

	public Dunkees(){
		this.initial_num_of_dunkees = 5;
		max_to_dunk = initial_num_of_dunkees;
		rightmostTrustee = (getScreenWidth()/3) - TRUSTEE_WIDTH;
		
		//Initiallizing all images
		icon = new ImageIcon("gross_right.png");
		trusteeLeft = icon.getImage();
		trusteeLeft = trusteeLeft.getScaledInstance(TRUSTEE_WIDTH, TRUSTEE_LENGTH, Image.SCALE_SMOOTH);

		icon = new ImageIcon("gross_left.png");
		trusteeRight = icon.getImage();
		trusteeRight = trusteeRight.getScaledInstance(TRUSTEE_WIDTH, TRUSTEE_LENGTH, Image.SCALE_SMOOTH);

		icon = new ImageIcon("gross_stand.png");
		trusteeCenter = icon.getImage();
		trusteeCenter= trusteeCenter.getScaledInstance(TRUSTEE_WIDTH, TRUSTEE_LENGTH, Image.SCALE_SMOOTH);

		icon = new ImageIcon("Dunk_Tank1.png");
		dunktank = icon.getImage();
		//dunktank = dunktank.getScaledInstance(DUNKTANK_WIDTH, DUNKTANK_LENGTH, Image.SCALE_SMOOTH);

		time = new Timer(50, this);
		time.start();
	}

	public void trusteeDunked(){
		num_of_dunkees++;
		path = 0;
	}

	public void update(){
		if(getTrusteeDunked()){
			trustee_movement = trustee_movement - 4;
			path+=5;
		}
	}

	public void actionPerformed(ActionEvent e){
		if(!getBallBeingThrown()){
			update();
			repaint();
		}
	}

	public void paint(Graphics g){
		Graphics2D g2d = (Graphics2D) g;

		g2d.drawImage(dunktank, 335, 262, null);
		if(!getTrusteeDunked()){
			g2d.drawImage(trusteeCenter, getScreenWidth()/2 - 20, getScreenHeight()/2, null);
		}

		if(getBallBeingThrown()){
			if (!getTrusteeDunked()){ //if true
				//player dunked
				
				for(int i = num_of_dunkees-1; i < max_to_dunk; i++){
					g2d.drawImage(trusteeCenter, rightmostTrustee - (i*TRUSTEE_WIDTH) - trustee_movement, getScreenHeight()/2, null);
				}
			}
		}else{
			if (!getTrusteeDunked()){ //if true
				for(int i = num_of_dunkees; i < max_to_dunk; i++){
					g2d.drawImage(trusteeCenter, rightmostTrustee - (i*TRUSTEE_WIDTH) - trustee_movement, getScreenHeight()/2, null);
				}
			}	
		}
		
		if(!getBallBeingThrown()){
			if (getTrusteeDunked() && isItAHit == true){
				g2d.drawImage(trusteeCenter, getScreenWidth()/2 - 20, getScreenHeight()/2 + path, null);
			}

			if(getTrusteeDunked() && lastRenderedLeft){
				for(int i = num_of_dunkees-1; i < max_to_dunk; i++){
					g2d.drawImage(trusteeRight, rightmostTrustee - (i*TRUSTEE_WIDTH) - trustee_movement, getScreenHeight()/2, null);
				}
				lastRenderedLeft = false;
			}else if (getTrusteeDunked() && !lastRenderedLeft){
				for(int i = num_of_dunkees-1; i < max_to_dunk; i++){
					g2d.drawImage(trusteeLeft, rightmostTrustee - (i*TRUSTEE_WIDTH) - trustee_movement, getScreenHeight()/2, null);
				}
				lastRenderedLeft = true;
			}
		}else{
			if(getTrusteeDunked() && lastRenderedLeft){
				for(int i = num_of_dunkees; i < max_to_dunk; i++){
					g2d.drawImage(trusteeRight, rightmostTrustee - (i*TRUSTEE_WIDTH) - trustee_movement, getScreenHeight()/2, null);
				}
				lastRenderedLeft = false;
			}else if (getTrusteeDunked() && !lastRenderedLeft){
				for(int i = num_of_dunkees; i < max_to_dunk; i++){
					g2d.drawImage(trusteeLeft, rightmostTrustee - (i*TRUSTEE_WIDTH) - trustee_movement, getScreenHeight()/2, null);
				}
				lastRenderedLeft = true;
			}	
		}
	}
}
