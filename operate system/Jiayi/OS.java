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
public class OS {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Memory Mem = new Memory();
        Mem.getjob(30);
        Mem.rjob(0, 10);
        System.out.println(Mem.getW() + " " +Mem.startV+" "+Mem.getsize(Mem.startV));
        for(int i = 0; i < 100;i++)
        {System.out.print(Mem.mem[i]+" ");}
    }
    
}