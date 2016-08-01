
/**
 *
 * @author Masud
 */
// // This sample uses the Apache HTTP client from HTTP Components (http://hc.apache.org/httpcomponents-client-ga/)

import static icevaluation.BingAPIAccess.processQuery;
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
import java.net.URI;
import java.util.ArrayList;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;


public class WebLanguageModel {
   
    public static String getJointProbability(String sentence,String apikey) 
    {
        HttpClient httpclient = HttpClients.createDefault();
        String value = "";
        //HttpClientBuilder httpbuild = HttpClientBuilder.create();

        try
        {
            URIBuilder builder = new URIBuilder("https://api.projectoxford.ai/text/weblm/v1.0/calculateJointProbability");

            builder.setParameter("model", "body");
            builder.setParameter("order", "1");

            URI uri = builder.build();
            HttpPost request = new HttpPost(uri);
            request.setHeader("Content-Type", "application/json");
            request.setHeader("Ocp-Apim-Subscription-Key", apikey);


            // Request body
            String query_json = "{\n" +
                        "	\"queries\":\n" +
                        "	[\n" +
                        "		\""+sentence+"\"\n" +
                        "	]\n" +
                        "}";
            StringEntity reqEntity = new StringEntity(query_json);
            request.setEntity(reqEntity);

            HttpResponse response = httpclient.execute(request);
            HttpEntity entity = response.getEntity();

            //Close connection. We got what we need :D
         
            if (entity != null) 
            {
                String sb = EntityUtils.toString(entity);
                
                //find the start index of results number, skip 12 for "WebTotal":"    
                int find= sb.indexOf("\"probability\":");
                int startindex = find + 14; 
        
                //Find the last index of results number
                int lastindex = sb.length()-3; //find the last index of number

                //Get the String of number we need 
                value = sb.substring(startindex,lastindex); 
            }
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
      return value;
    }
    
    public static void processFile(String input, String output) throws FileNotFoundException, UnsupportedEncodingException, IOException{
        
        //Initiate key and count
        ArrayList<String[]> dict= new ArrayList<>();
        String[] lineContent;
        FileInputStream kinput = new FileInputStream("./data/ngramKeys.txt");
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
                if(Integer.parseInt(entry[1])<100000){
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
            String result = getJointProbability(line, key);
            bfw.write(result);
            bfw.newLine();
            c++;
        }
      
        bfw.close();
        bfr.close();
        
        
        String result = Integer.toString(c);
        System.out.println("A total of "+ result + " queries have been processed");
        
        //write back keys and counts
        FileOutputStream kout = new FileOutputStream("./data/ngramKeys.txt");
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
         
     public static void calcDiff(String origin, String cover){
        {
        
        //Create file reader
        try{
        FileInputStream in1 = new FileInputStream(origin);
        InputStreamReader istream1 = new InputStreamReader(in1,"utf-8");
        BufferedReader bfr1 = new BufferedReader(istream1);
        
        FileInputStream in2 = new FileInputStream(cover);
        InputStreamReader istream2 = new InputStreamReader(in2,"utf-8");
        BufferedReader bfr2 = new BufferedReader(istream2);
        
        String line1;
        String line2;
        int c = 0;
        
        //Write results to result.txt. Create file if not exists.
        File f = new File("./result/ngram/Difference.txt");
        if(!f.exists()) {    
            f.createNewFile();
        } 
        
        FileOutputStream out = new FileOutputStream(f);
        OutputStreamWriter ostream = new OutputStreamWriter(out,"utf-8");
        BufferedWriter bfw = new BufferedWriter(ostream);      
        
        while((line1 = bfr1.readLine())!= null && (line2 = bfr2.readLine())!= null){
            double ori = Double.parseDouble(line1);
            double cov = Double.parseDouble(line2);

            double result = cov - ori;
            bfw.write(Double.toString(result));
            bfw.newLine();
            c++;
            
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
     
     public static void main(String[] args) throws UnsupportedEncodingException, IOException{
        WebLanguageModel weblm = new WebLanguageModel();
        //process file using the designated apiKey
        processFile("./data/ngramOrigin.txt","./result/ngram/ngramOHits.txt");
        processFile("./data/ngramCover.txt","./result/ngram/ngramCHits.txt");
        calcDiff("./result/ngram/ngramOHits.txt","./result/ngram/ngramCHits.txt");
        
    }
    
}
