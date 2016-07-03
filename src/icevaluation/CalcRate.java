package icevaluation;

import static icevaluation.BingAPIAccess.processQuery;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author aceni
 */
public class CalcRate {
    
    public static void main(String[] args) {
        calcRate("./data/input1.txt","./data/input2.txt");
        }
    
    public static void calcRate(String s1, String s2){
        {
        
        //Create file reader
        try{
        FileInputStream in1 = new FileInputStream(s1);
        InputStreamReader istream1 = new InputStreamReader(in1,"utf-8");
        BufferedReader bfr1 = new BufferedReader(istream1);
        
        FileInputStream in2 = new FileInputStream(s2);
        InputStreamReader istream2 = new InputStreamReader(in2,"utf-8");
        BufferedReader bfr2 = new BufferedReader(istream2);
        
        String line1;
        String line2;
        int c = 0;
        
        //Write results to result.txt. Create file if not exists.
        File f = new File("./data/Rate.txt");
        if(!f.exists()) {    
            f.createNewFile();
        } 
        
        FileOutputStream out = new FileOutputStream(f);
        OutputStreamWriter ostream = new OutputStreamWriter(out,"utf-8");
        BufferedWriter bfw = new BufferedWriter(ostream);      
        
        while((line1 = bfr1.readLine())!= null && (line2 = bfr2.readLine())!= null){
            int x = Integer.parseInt(line1);
            int y = Integer.parseInt(line2);
           
            if(x==0 || y ==0) {
                bfw.write(Double.toString((double) 1999999999.9));
                bfw.newLine();
                c++;
            } else {
            double result = Math.abs((double)Math.max(x,y) / Math.min(x,y));
            bfw.write(Double.toString(result));
            bfw.newLine();
            c++;
            }
        }
      
        bfw.close();
        bfr1.close();
        bfr2.close();
        
        
        String result = Integer.toString(c);
        System.out.println("A total of "+ result + " lines have been processed");
            
        } catch(IOException e) {
            e.printStackTrace();
        }

     }
    }
}
