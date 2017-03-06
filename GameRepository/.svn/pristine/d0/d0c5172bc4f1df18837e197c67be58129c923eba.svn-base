/*
 * Sound class
 */
import java.applet.Applet;
import java.applet.AudioClip;
import java.awt.event.ActionListener;
import java.io.*;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.Clip;
import javax.sound.sampled.AudioSystem;
import javax.swing.Timer;
/**
 *
 * @author Gamer
 */
public class Sound extends GameObject implements ActionListener{


    public static final AudioClip BACK = Applet.newAudioClip(Sound.class.getResource("back.wav"));
    public static final AudioClip Hit = Applet.newAudioClip(Sound.class.getResource("hit.wav"));
    public static final AudioClip end = Applet.newAudioClip(Sound.class.getResource("over.wav"));
    public static final AudioClip thr_ball = Applet.newAudioClip(Sound.class.getResource("throw.wav"));
    public Sound()
    {BACK.loop();}
    public void B_throw()
    {
    try{Thread.sleep(2000);}
    catch(Exception e)
    {System.out.print("someting worng");}
    thr_ball.play();
    try{Thread.sleep(2000);}
    catch(Exception e)
    {System.out.print("someting worng");}
    Hit.play();
    }
    public void A_throw()
    {  
        thr_ball.play();}
    public void END()
    {BACK.stop();
    end.play();}
    public void again()
    {end.stop();
    BACK.loop();}
}
