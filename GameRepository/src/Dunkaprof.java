//****************************************************************************************
//This is the main class where the program is run from.
//
//class sets up the Jframe and implements runnable which, calls the run method in return 
//starts the game. 
//initializing the parameters required from the set up of the final Jframe
//****************************************************************************************

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class Dunkaprof extends JFrame implements Runnable{
	private int screen_width;
	private int screen_height;
	private JFrame frame = new JFrame("dunk-a-prof");
	
	/**
	 * Create the frame here
	 *
	 */
	public void run(){
		screen_width = 1280;
		screen_height = 720;
		
		frame.setLayout(new BorderLayout());
		frame.setSize(new Dimension(screen_width, screen_height));  //default is supposed to be 1280 x 720
		frame.setVisible(true);
		
		Menu menu = new Menu(frame, screen_width, screen_height);
		frame.getContentPane().add(menu.MenuLayout());
		
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setResizable(false);	
		frame.setTitle("Dunk-A-Prof");
		frame.setVisible(true);
	};
	
	/**
	 * Initiate the frame here
	 */
	public static void main(String[] args){
		(new Thread(new Dunkaprof())).start();
	}
}
