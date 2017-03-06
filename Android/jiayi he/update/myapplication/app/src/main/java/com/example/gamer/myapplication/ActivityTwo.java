package com.example.gamer.myapplication;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import static android.view.View.OnTouchListener;
import static com.example.gamer.myapplication.R.id.button3;
import android.R.drawable;

import static com.example.gamer.myapplication.R.id.plus;
import static com.example.gamer.myapplication.R.id.textView2;
import static com.example.gamer.myapplication.R.id.textView3;

/**
 * Created by Gamer on 03/16/2015.
 */
public class ActivityTwo extends Activity implements OnTouchListener {
    public TextView textView,textView3;
    OurView v;
    Bitmap ball;
    //Bitmap ball = BitmapFactory.decodeResource(getResources(),R.drawable.blueball);
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //v = new OurView(this);
        //v.setOnTouchListener(this);
        setContentView(R.layout.activety_two);
        Button btnOne = (Button)findViewById(button3);
        final Button btwTwo = (Button)findViewById(plus);
        btwTwo.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        btwTwo.setX(event.getX());
                        btwTwo.setY(event.getY());
                        break;
                }
                return true;
            }
        });
        btnOne.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                startActivity(intent);
            }
        });
        textView = (TextView) findViewById(R.id.textView);
        textView3 = (TextView) findViewById(R.id.textView3);
        Calc cal = new Calc();
        cal.setArr();
        cal.aAnds();
        textView3.setText(Integer.toString(cal.arr[0])+"   "+cal.opp[0]+"   "+Integer.toString(cal.arr[1])+"   "+cal.opp[1]+"   " +
                Integer.toString(cal.arr[2])+'='
                +Integer.toString(cal.arr[3]));
        new CountDownTimer(60000,1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                textView.setText("Time remain: "+millisUntilFinished/1000);
            }

            @Override
            public void onFinish() {
                textView.setText("Time over");
            }
        }.start();
    }
    public class OurView extends SurfaceView implements Runnable
    {
        Thread t = null;
        SurfaceHolder holder;
        boolean isItOK = false;
        public OurView(Context context)
    {
        super(context);
        holder = getHolder();
    }
        public void run()
        {
            while (isItOK==true)
            {
                if(!holder.getSurface().isValid())
                {continue;}
                Canvas c = holder.lockCanvas();
                c.drawARGB(255,150,150,10);
                holder.unlockCanvasAndPost(c);
            }
        }
        public void pause() {
            isItOK = false;
            while (true) {
                try {
                    t.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }break;
            }
            t = null;
        }
        public void resume()
        {
            isItOK = true;
            t = new Thread(this);
            t.start();
        }
    }
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return false;
    }
}