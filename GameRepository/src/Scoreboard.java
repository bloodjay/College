//****************************************************************************************
//										Scoreboard
//paints and updates the scores onto the screen calling its paint method from the GameObject 
//class
//****************************************************************************************
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import java.io.*;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import javax.swing.Timer;
import java.awt.event.ActionEvent;

public class Scoreboard extends GameObject implements ActionListener{
	private int score, starting_score;
	private Timer time;
	/*
	private final int MAX_SCORES;
	private String[] scoreList = new String[MAX_SCORES];
	private FileReader fr = new FileReader("scores.txt"); //check and possible update me after the game ends and we have a final score
	String s;
	int i=0;
	*/

	/**
	 * Set score = starting_score
	 * @param starting_score
	 */
	Scoreboard(int starting_score){
		this.starting_score = starting_score;
		score = starting_score;
		time = new Timer(20, this);
/*
		while((s=br.readLine()) != null) {
			if(i<10) scoreList[i] = s;
		}
		fr.close();
*/
	}
	
	/**
	 * Increase the score by desired value
	 * @param increase
	 */
	public void addScore(int increase){
		//if (getCanWeThrow())
			score += increase;
	}
	
	/**
	 * Decrease the score by desired value
	 * @param decrease
	 */
	public void decreaseScore(int decrease){
		score -= decrease;
	}
	
	public void actionPerformed(ActionEvent e){
		repaint();
	}
	/**
	 * Paint scoreboard in top right corner
	 * @param g
	 */
	public void paint(Graphics g){
		g.setColor(Color.WHITE);
		g.fillRect(1100, 50, 150, 130);
		
		g.setColor(Color.BLACK);
		g.fillRect(1110, 85, 130, 90);
		
		/*
		 * Text starts here
		 */
		Graphics2D g2 = (Graphics2D)g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
		    RenderingHints.VALUE_ANTIALIAS_ON);
		Font font = new Font("Impact", Font.PLAIN, 16);
		g2.setFont(font);
	 
		g2.setColor(Color.BLUE);
		g2.drawString("SCORE", 1150, 75);
		
		Font font2 = new Font("Impact", Font.PLAIN, 60);
		g2.setFont(font2);
		g2.setColor(Color.WHITE);
		
		// Position the score itself
		if (score < 10)
			g2.drawString(Integer.toString(score), 1160, 155);
		else if (score < 100)
			g2.drawString(Integer.toString(score), 1142, 155);
		else if (score < 1000)
			g2.drawString(Integer.toString(score), 1128, 155);
		/*
		 * Text ends here
		 */

	}
		
	/**
	 * Paint scoreboard in top right corner
	 * @param g
	 */
/*
	public void paint(Graphics g){
		g.setColor(Color.WHITE);
		g.fillRect(1100, 50, 150, 130);
		
		g.setColor(Color.BLACK);
		g.fillRect(1110, 85, 130, 90);
		
		/*
		 * Text starts here
		 */
/*
		Graphics2D g2 = (Graphics2D)g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
		    RenderingHints.VALUE_ANTIALIAS_ON);
		Font font = new Font("Impact", Font.PLAIN, 16);
		g2.setFont(font);
	 
		g2.setColor(Color.BLUE);
		g2.drawString("SCORE", 1150, 75);
		
		Font font2 = new Font("Impact", Font.PLAIN, 60);
		g2.setFont(font2);
		g2.setColor(Color.WHITE);
		int foo	= 1040;
		for(int i=0;i<MAX_SCORES;i++) {
			g2.drawString(scoreList[i], foo, 155);
			foo+=12;
		}
	}
*/
}
