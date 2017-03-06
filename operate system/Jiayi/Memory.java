/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package os;

/**
 *
 * @author Gamer
 */
public class Memory {
    int startV = 0;
    int [] mem;
    int index,maxV =0;
    public Memory() {
        mem = new int[101];
        for(int i = 0; i < 100 ; i++)
        {mem[i]=1;}
        mem[100]=2;
    }
    public int worstfit(int size)
    {return size;}
    
    public int getW()
    {   
    for(int i = 0; i<100;i++)
    if(mem[i]==1)
    if(getsize(i)>maxV)
    {maxV = getsize(i);
    startV = i;}
        return startV;
    }
    
    public int getsize(int x)
    {int Msize = 0;
    while(mem[x]==1)
    {x++;
    Msize++;}
    return Msize;}
    
    public void getjob(int size)
    {if(size<getsize(startV))
        for(int i = startV; i<size+startV; i++)
        {mem[i] = 0;}
    }
    
    public void rjob(int start, int size)
    {for(int i = start; i<start+size; i++)
        mem[i] = 1;}
}
