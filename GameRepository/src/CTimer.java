//****************************************************************************************
//										CTimer
//----------------------------------------------------------------------------------------
//CTime class is a countdown timer which initial time is 120 seconds.
//When the time is '0'. It will display Times up.
//
//
//****************************************************************************************
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.Timer;

public class CTimer extends GameObject implements ActionListener{
    final int T_time = 120;
    int counter = T_time;
    Timer RefreshT;
    public CTimer()
    {RefreshT = new Timer(1000,this);
            RefreshT.start();
    }
    
    public void actionPerformed(ActionEvent act){
	if(counter>0)	
        {//repaint();
        counter--;}
    }
    
    public void paint(Graphics g)
    {   
      if(counter!=0)
        g.drawString(Integer.toString(counter), 600, 100);
      else{
    	setGameOver();
        g.drawString("Times up", 520, 100);
      }
    }
    public void reset()
    {counter = T_time;}
}
