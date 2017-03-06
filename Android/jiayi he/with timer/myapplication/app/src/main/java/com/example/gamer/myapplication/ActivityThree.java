package com.example.gamer.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import static com.example.gamer.myapplication.R.id.button3;
import static com.example.gamer.myapplication.R.id.button4;

/**
 * Created by Gamer on 03/16/2015.
 */
public class ActivityThree extends Activity {
    public TextView textView;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activeity_three);
        Button btnOne = (Button)findViewById(button4);
        btnOne.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                startActivity(intent);
            }
        });
        textView = (TextView) findViewById(R.id.textView2);
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
}