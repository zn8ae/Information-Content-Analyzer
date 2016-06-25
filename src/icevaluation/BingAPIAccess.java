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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import org.apache.commons.codec.binary.Base64;

/**
 *
 * @author zn8ae
 */
public class BingAPIAccess {
    
    
     public static void processQuery(String query){
        
        //Encrypt content to URL format     
        String searchText = query; 
        searchText = searchText.replaceAll(" ", "%20");   
 
        //Encrypt accountKey for API
        String accountKey = "dh8dWClCMuTDF++o8urdm359v6xDgkQayQQyHiH2lE0";
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
        System.out.println("Output from Server .... \n");
        
        //write json to string sb
        if ((output = br.readLine()) != null) {
        System.out.println("Output is: "+output);
        sb.append(output);
        }
        
        //Close connection. We got what we need :D
        conn.disconnect();
        
        //find the start index of results number, skip 12 for "WebTotal":"    
        int find= sb.indexOf("\"WebTotal\":\"");
        int startindex = find + 12; 
        
        //Find the last index of results number
        int lastindex = sb.indexOf("\",\"WebOffset\""); //find the last index of number

        //Get the String of number we need 
        String numResults = sb.substring(startindex,lastindex);
        
        //Write results to result.txt. Create file if not exists.
        try{
        File f = new File("./data/output.txt");
        if(!f.exists()) {
            f.createNewFile();
        } 
        FileOutputStream out = new FileOutputStream(f,false);
        OutputStreamWriter ostream = new OutputStreamWriter(out,"utf-8");
        BufferedWriter bf = new BufferedWriter(ostream);
        
        bf.write(searchText + " " + numResults);
        bf.newLine();
        bf.close();
        } catch(IOException e){
            e.printStackTrace();
        }
              
        //These catches are for API connection
        } catch (MalformedURLException e1) {
            e1.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }   

     }
   
     public static void main(String[] args) {
        
        try{
        FileInputStream in = new FileInputStream("./data/input.txt");
        InputStreamReader istream = new InputStreamReader(in,"utf-8");
        BufferedReader bfr = new BufferedReader(istream);
        String line;
        while((line = bfr.readLine())!= null){
            processQuery(line);
        }
        bfr.close();
        } catch(IOException e) {
            e.printStackTrace();
        }

     }
     
}

