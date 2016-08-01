/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package icevaluation;

/**
 *
 * @author Zihan
 */

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.codec.binary.Base64;

/**
 *
 * @author zn8ae
 */
public class BingAPIAccess {
    
    
     public static String processQuery(String query, String key){
        
        //Encrypt content to URL format     
        String searchText = query; 
        searchText = searchText.replaceAll(" ", "%20"); 
        String numResults = "";
        
        
        //Encrypt accountKey for API
        String accountKey = key;
        byte[] accountKeyBytes = Base64.encodeBase64((accountKey + ":" + accountKey).getBytes());
        String accountKeyEnc = new String(accountKeyBytes);
        URL url;
        
        //Connect using HTTpURLConnection and send GET request with key
        try {
            url = new URL(
                "https://api.datamarket.azure.com/Bing/Search/v1/Composite?Sources=%27Web%27&Query=%27" + searchText + "%27&$format=JSON");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", "Basic " + accountKeyEnc);

        //Read input stream
        BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
        StringBuilder sb = new StringBuilder();
        
        //Print server response and append to string sb
        String output;
        int c = 0;
        if ((output = br.readLine()) != null) {  
        sb.append(output);
        c++;
        }
        
        //Close connection. We got what we need :D
        conn.disconnect();
        
        //find the start index of results number, skip 12 for "WebTotal":"    
        int find= sb.indexOf("\"WebTotal\":\"");
        int startindex = find + 12; 
        
        //Find the last index of results number
        int lastindex = sb.indexOf("\",\"WebOffset\""); //find the last index of number

        //Get the String of number we need 
        numResults = sb.substring(startindex,lastindex);
         
        String count = Integer.toString(c);
        System.out.println(count + " query successfully processed");
           
        //These catches are for API connection
        } catch (MalformedURLException e1) {
            e1.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }   
        
        return (numResults);
     }
     
     public static void processFile(String input, String output) throws FileNotFoundException, UnsupportedEncodingException, IOException{
        
        //Initiate key and count
        ArrayList<String[]> dict= new ArrayList<>();
        String[] lineContent;
        FileInputStream kinput = new FileInputStream("./data/keys.txt");
        InputStreamReader kstream = new InputStreamReader(kinput,"utf-8");
        BufferedReader bfk = new BufferedReader(kstream);
        String kentry;
        while((kentry = bfk.readLine())!= null){
            lineContent = kentry.split(" ");    //Input the key and count into local dictionary
            dict.add(lineContent);
        }
        bfk.close();
        //now you have a dict of key - count pairs ready to deploy
        
        //Create file reader to read input queries
        try{
        FileInputStream in = new FileInputStream(input);
        InputStreamReader istream = new InputStreamReader(in,"utf-8");
        BufferedReader bfr = new BufferedReader(istream);
        String line;
        int c = 0;
        
        //Create output file if not exists.
        File f = new File(output);
        if(!f.exists()) {    
            f.createNewFile();
        } 
        
        FileOutputStream out = new FileOutputStream(f);
        OutputStreamWriter ostream = new OutputStreamWriter(out,"utf-8");
        BufferedWriter bfw = new BufferedWriter(ostream);      
        
        //process queries line by line
        while((line = bfr.readLine())!= null){
            //find a key to use
            String key="";
            //get a usable key
            
            int i = 0; //counter to traverse keys dict
            while(i<dict.size()){
                //find an available key for this query
                String[] entry = dict.get(i);
                if(Integer.parseInt(entry[1])<5000){
                    //Use the key 
                    System.out.println(entry[0]+" "+entry[1]+" is in use");
                    key = entry[0];
                    //update the count in dict
                    int newvalue = Integer.parseInt(entry[1]);
                    newvalue --;
                    entry[1] = Integer.toString(newvalue);
                    break; //break out while loop
                } else {
                     i++; //This key is used up. go to next key.
                }
             }          
            //use the key to process the query
            if(key.equals(""))
                System.out.println("no key usable");
            
            //process query and update count for processed queries
            String result = processQuery(line, key);
            bfw.write(result);
            bfw.newLine();
            c++;
        }
      
        bfw.close();
        bfr.close();
        
        
        String result = Integer.toString(c);
        System.out.println("A total of "+ result + " queries have been processed");
        
        //write back keys and counts
        FileOutputStream kout = new FileOutputStream("./data/keys.txt");
        OutputStreamWriter kostream = new OutputStreamWriter(kout,"utf-8");
        BufferedWriter bfko = new BufferedWriter(kostream);
        
        for(String[] entry:dict){
            //Write the updated results to file
            bfko.write(entry[0] + " " + entry[1]);
            bfko.newLine();
        }
        bfko.close();
        
        } catch(IOException e) {
            e.printStackTrace();
        }

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
        File f = new File("./result/ic/Rate.txt");
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
        System.out.println("A total of "+ result + " rates have been processed");
            
        } catch(IOException e) {
            e.printStackTrace();
        }

     }
    }
     
   
     public static void main(String[] args) {
         try {
             processFile("./data/original.txt","./result/ic/oHits.txt");
             processFile("./data/cover.txt","./result/ic/cHits.txt");
             calcRate("./result/ic/oHits.txt","./result/ic/cHits.txt");
         } catch (UnsupportedEncodingException ex) {
             Logger.getLogger(BingAPIAccess.class.getName()).log(Level.SEVERE, null, ex);
         } catch (IOException ex) {
             Logger.getLogger(BingAPIAccess.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
     
}

