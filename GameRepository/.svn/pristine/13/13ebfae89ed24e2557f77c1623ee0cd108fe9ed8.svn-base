import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.awt.BorderLayout;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;

import java.awt.Color;
import java.io.File;
import java.io.IOException;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class SelectionScreen extends Menu{
	
	JFrame frame;
	Image background;
	ImageIcon icon;
	boolean selected;
	public boolean professor1Selected = false;
	public boolean professor2Selected = false;
	public boolean professor3Selected = false;
	public boolean trustee1Selected = false;
	public boolean trustee2Selected = false;
	public boolean trustee3Selected = false;
	public JButton StartButton;

	//map <String, String> selectedPlayers;
	
	SelectionScreen(JFrame frame){
		super(frame, getScreenWidth(), getScreenHeight());
		selected = false;
		this.frame = frame;
		icon = new ImageIcon("selection_menu2.png");
		background = icon.getImage();
		this.setFocusable(true);
		this.requestFocusInWindow();
	}

	public boolean minimumNumberOfPlayersSelected(){
	    int sum = 0;
	    sum += professor1Selected ? 1 : 0;
		sum += professor2Selected ? 1 : 0;
		sum += professor3Selected ? 1 : 0;
		sum += trustee1Selected ? 1 : 0;
		sum += trustee2Selected ? 1 : 0;
		sum += trustee3Selected ? 1 : 0;
		System.out.println(sum);
		if(sum == 5){
			return true;
		}
		return false;
	}

	public void activateStart(){
	    JButton NewGame = new JButton("Start Game"); 
	    final GameObject go = new GameObject();
		    go.initializeGame(); 
	
		if(minimumNumberOfPlayersSelected()){
			StartButton.setEnabled(true);
			System.out.print("HERE");
		    /**
		     * New Game button
		     * will clear screen
		     * and start the GameObject class
		     */	 
					    	
		}
		
	    NewGame.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent arg0) {
		        if(minimumNumberOfPlayersSelected()){
			        ClearScreenContents();
			        // Add NewGame function
			        NewPane(go);
		        }else{
		            JOptionPane.showMessageDialog(null, "Please select 5 choices!");
		        }
		    }
	    });		
		
		NewGame.setBounds(785, 620, 110, 40);
        NewGame.setBackground(Color.YELLOW);
        add(NewGame); 
		
		repaint();
	}
	
	public void paintComponent(Graphics g){
		Graphics2D g2d = (Graphics2D) g;
		super.paintComponent(g);	    
	    g2d.drawImage(background, 0, 0, null);

	    //border to differenciate between selectend and unselected
	    final Border selectedBorder = new LineBorder(Color.BLUE, 7);
	    final Border deselectedBorder = new LineBorder(Color.RED, 7);


	    
	    //Start button
	    StartButton = new JButton("Start");
	    StartButton.setBounds(982, 638, 200, 54);
		

		//professor 1
		final JButton Professor1 = new JButton();
		Professor1.setOpaque(false);
		Professor1.setBorder(deselectedBorder);
		Professor1.setContentAreaFilled(false);
		Professor1.setBorderPainted(true);
		Professor1.setBounds(220,163,205,183);
		add(Professor1);

		//professor2
		final JButton Professor2 = new JButton();
		Professor2.setOpaque(false);
		Professor2.setBorder(deselectedBorder);
		Professor2.setContentAreaFilled(false);
		Professor2.setBorderPainted(true);
		Professor2.setBounds(545,163,205,183);
		add(Professor2);

		//professor3
		final JButton Professor3 = new JButton();
		Professor3.setOpaque(false);
		Professor3.setBorder(deselectedBorder);
		Professor3.setContentAreaFilled(false);
		Professor3.setBorderPainted(true);
		Professor3.setBounds(855,163,205,183);
		add(Professor3);

		//trustee1
		final JButton Trustee1 = new JButton();
		Trustee1.setOpaque(false);
		Trustee1.setBorder(deselectedBorder);
		Trustee1.setContentAreaFilled(false);
		Trustee1.setBorderPainted(true);
		Trustee1.setBounds(220,393,205,183);
		add(Trustee1);
		
		//trustee2
		final JButton Trustee2 = new JButton();
		Trustee2.setOpaque(false);
		Trustee2.setBorder(deselectedBorder);
		Trustee2.setContentAreaFilled(false);
		Trustee2.setBorderPainted(true);
		Trustee2.setBounds(545,393,205,183);
		add(Trustee2);

		//trustee3
		final JButton Trustee3 = new JButton();
		Trustee3.setOpaque(false);
		Trustee3.setBorder(deselectedBorder);
		Trustee3.setContentAreaFilled(false);
		Trustee3.setBorderPainted(true);
		Trustee3.setBounds(860,395,205,183);
		add(Trustee3);

		
		StartButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				//ClearScreenContents();
				//frame.getContentPane().removeAll();
				//final GameObject go = new GameObject();
				//NewPane(go);
				System.out.println("launch game object");
			}
		});

		Professor1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				//ClearScreenContents();
				professor1Selected = !professor1Selected;
				Professor1.setSelected(professor1Selected);
				if(professor1Selected){
					System.out.println("professor 1 selected");
					Professor1.setBorder(selectedBorder);
					activateStart();
				}else{
					Professor1.setBorder(deselectedBorder);
				}
				//professor1Selected = professor1Selected;

			}
		});

		Professor2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				//ClearScreenContents();
				professor2Selected = !professor2Selected;
				Professor2.setSelected(professor2Selected);
				if(professor2Selected){
					System.out.println("professor 2 selected");
					Professor2.setBorder(selectedBorder);
					activateStart();
				}else{
					Professor2.setBorder(deselectedBorder);
				}
				//professor2Selected = professor2Selected;
			}
		});

		Professor3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				//ClearScreenContents();
				professor3Selected = !professor3Selected;
				Professor3.setSelected(professor3Selected);
				if(professor3Selected){
					System.out.println("professor 3 selected");
					Professor3.setBorder(selectedBorder);
					activateStart();
				}else{
					Professor3.setBorder(deselectedBorder);
				}
				//professor3Selected = professor3Selected;
			}
		});
		
		Trustee1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				//ClearScreenContents();
				trustee1Selected = !trustee1Selected;
				Trustee1.setSelected(trustee1Selected);
				if(trustee1Selected){
					System.out.println("trustee 1 selected");
					Trustee1.setBorder(selectedBorder);
					activateStart();
				}else{
					Trustee1.setBorder(deselectedBorder);
				}
				//trustee1Selected = trustee1Selected;
			}
		});

		Trustee2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				//ClearScreenContents();
				trustee2Selected = !trustee2Selected;
				Trustee2.setSelected(trustee2Selected);
				if(trustee2Selected){
					System.out.println("trustee 2 selected");
					Trustee2.setBorder(selectedBorder);
					activateStart();
				}else{
					Trustee2.setBorder(deselectedBorder);
				}
				//trustee2Selected = trustee2Selected;
			}
		});
		
		Trustee3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				//ClearScreenContents();
				trustee3Selected = !trustee3Selected;
				Trustee3.setSelected(trustee3Selected);
				if(trustee3Selected){
					System.out.println("trustee 3 selected");
					Trustee3.setBorder(selectedBorder);
					activateStart();
				}else{
					Trustee3.setBorder(deselectedBorder);
				}
				//trustee3Selected = trustee3Selected;
			}
		});
	}
}
