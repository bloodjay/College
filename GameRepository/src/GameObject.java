//****************************************************************************************
//										Game Object
//----------------------------------------------------------------------------------------
//This class is responsible for the painting and repainting of graphics onto the jpanel.
//when an action is performed objects update should be called and the repaint method called
//
//
//****************************************************************************************


import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.JTextField;



public class GameObject extends Menu implements ActionListener, KeyListener, Runnable{
	private static final boolean True = false;
	Image background;
	ImageIcon icon;
	Slider slider;
	Timer time;
	Scoreboard score;
	Balls balls;
	Dunkees dunkees;
	Target target;
    CTimer Tobject;
    Sound sound;
	int ballsThrown;
	private static boolean can_we_throw;
	private static boolean trusteeDunked;
    static boolean isItAHit;
	private static boolean ballBeingThrown;
	private static boolean gameOver;


	/**
	 * Constructor sets up
	 * our variables.
	 *
	 * Possible TODO:
	 * 	Change constructor to take number of balls as argument
	 */
	GameObject(JFrame frame){
		super(frame, getScreenWidth(), getScreenHeight());
		this.frame = frame;

		icon = new ImageIcon("QuadBuildingBackground.jpg");
    	background = icon.getImage();

		this.addKeyListener(this);
		this.setFocusable(true);
	}

	GameObject(){
		icon = new ImageIcon("QuadBuildingBackground.jpg");
    	background = icon.getImage();

		this.addKeyListener(this);
		this.setFocusable(true);
	}

	public void run(){
		initializeGame();
	}

	/**
	 * Our Constructor is called in
	 * sub classes so we need to set
	 * any values in this class
	 */
	public void initializeGame(){
		ballsThrown = 0;
		gameOver = false;
		can_we_throw = true;
		trusteeDunked = false;
		ballBeingThrown = false;
        Tobject = new CTimer();
		balls = new Balls(6);
		score = new Scoreboard(0);
    	slider = new Slider();
    	dunkees = new Dunkees();		// we should provide a prompt screen to select the number of professors we want to dunk
    	target = new Target();
        sound = new Sound();
	}



	/**
	 * Paint Component is called when we create instance
	 * of Gameobject
	 */
	public void paintComponent(Graphics g){
		Graphics2D g2d = (Graphics2D) g;
		super.paintComponent(g);
	    g2d.drawImage(background, 0, 0, null);
	    score.paint(g);
	    slider.paint(g);
	    dunkees.paint(g);
	    target.paint(g);
	    balls.paint(g);
        Tobject.paint(g);
	    repaint();
	}

	/**
	 * Dynamically change whether we're accepting throws or not
	 * @param value
	 */
	public void setCanWeThrow(boolean value){
		can_we_throw = value;
	}

	/**
	 * Return the boolean value
	 * of can_we_throw so that
	 * other classes can use it
	 * @return
	 */
	public boolean getCanWeThrow(){
		return can_we_throw;
	}

	/**
	 * Dynamically change if the ball motion is happening
	 * @param value
	 */
	public void setBallBeingThrown(boolean value){
		ballBeingThrown = value;
	}

	/**
	 * Return the boolean value of
	 * ballBeingThrown so we know if
	 * the ball should be moving or not
	 * @return
	 */
	public boolean getBallBeingThrown(){
		return ballBeingThrown;
	}

	/**
	 * Dynamically change if the trustees are moving or not
	 * @param value
	 */
	public void setTrusteeDunked(boolean value){
		trusteeDunked = value;
	}

	/**
	 * Return the boolean value of
	 * trusteeDunked so we know if
	 * the trustees are moving or not
	 * @return
	 */
	public boolean getTrusteeDunked(){
		return trusteeDunked;
	}

	/**
	 * Currently relies on timer
	 */
	public void actionPerformed(ActionEvent e){
		repaint();
	}

	public void setIsItAHit(boolean value){
		isItAHit = value;
	}

	public void setGameOver(){
		gameOver = true;
	}

	public boolean getGameOver(){
		return gameOver;
	}

	public void ballThrownInGame(){
		slider.setxVelocity(0); // Stop the Slider
		setCanWeThrow(false);
		setBallBeingThrown(true);

		if (slider.getStartingWidth() < 100){
			System.out.println("Works");
			isItAHit = True;
		}

		dunkees.trusteeDunked();

		setTrusteeDunked(false);			// misuse of terminology, im saying if the ball movement should be updated.
		balls.ballThrown(slider.getHitRanking(), target.getHitYPos());

		/*
		 * What happens if we hit the target?
		 * Current increase the score
		 */
		if(isItAHit){
			score.addScore(10);
		}

		slider.setxVelocity(slider.getLastVelocity());
	}

	/**
	 * Should only be listening to spacebar for time being
	 */
	public void keyPressed(KeyEvent e){
		if (e.getKeyCode() == KeyEvent.VK_SPACE){
			if(getCanWeThrow()){
				ballThrownInGame();
				sound.A_throw();
			}
			//else
			//sound.A_throw();
		}
	}

	/**
	 * Error if all three are not overrided
	 */
	public void keyReleased(KeyEvent e){}

	/**
	 * Error if all three are not overrided
	 */
	public void keyTyped(KeyEvent e){}
}
