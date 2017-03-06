//****************************************************************************************
//										Game Object
//----------------------------------------------------------------------------------------
//this class is responsible for the painting and repainting of graphics onto the jpanel.
//when a new instructions method is called. 
//class only has the ability to display info and go back into the mainmenu.
//****************************************************************************************


import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import java.io.*;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;


public class Instructions extends Menu{
	JFrame frame;
	Image background;
	ImageIcon icon;
	
	Instructions(JFrame frame) throws IOException{
		super(frame, getScreenWidth(), getScreenHeight());
		this.frame = frame;
		icon = new ImageIcon("instruction_unclear.jpg");
		background = icon.getImage();
		
		this.setFocusable(true);
	}
	
	/**
	 * Paint Component is called when we create instance 
	 * of Game object
	 */
	public void paintComponent(Graphics g){
		super.paintComponent(g);
	    g.drawImage(background, 400, 200, null);
	    
	    JButton BackButton = new JButton();
	    try {
		    Image img = ImageIO.read(getClass().getResource("back_button.png"));
		    BackButton.setIcon(new ImageIcon(img));
		} catch (IOException ex) {
			ex.printStackTrace();
		}

		BackButton.setOpaque(false);
		BackButton.setFocusPainted(false);
		BackButton.setBorderPainted(false);
		BackButton.setContentAreaFilled(false);
		BackButton.setBounds(1050, 600, 60,60);
		BackButton.setBorder(BorderFactory.createEmptyBorder(0,0,0,0));
		add(BackButton);
		
		BackButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				ClearScreenContents();
				// Go back to main menu

				Menu mm = new Menu(frame, getScreenWidth(), getScreenHeight());
				NewPane(mm.MenuLayout());
			}
		});
		
	}
}
