import java.awt.Graphics;
import java.awt.*;
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

public class Highscore extends Menu{
	JFrame frame;
	Image background;
	ImageIcon icon;
	String strLine;
	int y = 150;

	Highscore(JFrame frame) throws IOException{
		super(frame, getScreenWidth(), getScreenHeight());
		this.frame = frame;
		icon = new ImageIcon("Highscore_background.jpg");
		background = icon.getImage();
		openStream();
		this.setFocusable(true);
	}

	public void openStream(){
		try {
	        // Open the file that is the first
	        // command line parameter
	        FileInputStream fstream = new FileInputStream("scores.txt");
	        BufferedReader br = new BufferedReader(new InputStreamReader(fstream));

	    }catch(IOException e){
	    	e.printStackTrace();
	    }
	}

	public void paintComponent(Graphics g){
		g.drawImage(background, 0, 0, null);
		Color color = new Color(51,98,227);
		g.setColor(color);
		Font c = new Font("Courier", Font.BOLD, 50);
		g.setFont(c);

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
		

		try {
	        // Open the file that is the first
	        // command line parameter
	        FileInputStream fstream = new FileInputStream("scores.txt");
	        BufferedReader br = new BufferedReader(new InputStreamReader(fstream));

		        // Read File Line By Line
	        while ((strLine = br.readLine()) != null) {
	            strLine = strLine;
	            System.out.println(strLine);
				g.drawString(strLine, 600, y);
				y=y+50;
			}
	    }catch(IOException e){
	    	e.printStackTrace();
	    }



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