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
    
    
     public static String processQuery(String query){
        
        //Encrypt content to URL format     
        String searchText = query; 
        searchText = searchText.replaceAll(" ", "%20"); 
        String numResults = "";
 
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
        int c = 0;
        System.out.println("Output from Server .... \n");
        if ((output = br.readLine()) != null) {
        System.out.println("Output is: "+output);
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
        
        return (searchText + " " + numResults);
     }
   
     public static void main(String[] args) {
        
        //Create file reader
        try{
        FileInputStream in = new FileInputStream("./data/input.txt");
        InputStreamReader istream = new InputStreamReader(in,"utf-8");
        BufferedReader bfr = new BufferedReader(istream);
        String line;
        int c = 0;
        
        //Write results to result.txt. Create file if not exists.
        File f = new File("./data/output.txt");
        if(!f.exists()) {    
            f.createNewFile();
        } 
        
        FileOutputStream out = new FileOutputStream(f);
        OutputStreamWriter ostream = new OutputStreamWriter(out,"utf-8");
        BufferedWriter bfw = new BufferedWriter(ostream);      
        
        while((line = bfr.readLine())!= null){
            String result = processQuery(line);
            bfw.write(result);
            bfw.newLine();
            c++;
        }
      
        bfw.close();
        bfr.close();
        
        
        String result = Integer.toString(c);
        System.out.println("A total of "+ result + " queries have been processed");
            
        } catch(IOException e) {
            e.printStackTrace();
        }

     }
     
}

