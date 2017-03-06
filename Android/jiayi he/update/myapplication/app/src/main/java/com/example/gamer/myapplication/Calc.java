package com.example.gamer.myapplication;
import java.util.Random;

/**
 * Created by Gamer on 03/24/2015.
 */
public class Calc {
    int[] arr =new int[4];
    char[] opp =new char[2];
    Random random = new Random();

    public void setArr() {
        this.arr[0] = random.nextInt(10);
        this.arr[1] = random.nextInt(10);
        this.arr[2] = random.nextInt(10);
        int op;
        for(int i = 0; i<=1; i++)
        {
        op = random.nextInt(2);
        if(op==0)
           opp[i] = '+';
        else
           opp[i] = '-';}

    }
    public void aAnds()
    {   int temp = this.arr[0];
        for(int i = 0; i<=1;i++)
    {
        if(opp[i]=='+')
            temp = temp+arr[i+1];
        else
            temp = temp-arr[i+1];
    }
       this.arr[3] = temp;
    }


}
