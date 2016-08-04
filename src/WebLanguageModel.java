
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
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;


public class WebLanguageModel {
   
    private static HashMap<String, Double> dict = new HashMap<>();    
       
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
   

     public static void processQueries(String location) throws FileNotFoundException, UnsupportedEncodingException, IOException{
         
        String[] lineContent;
        FileInputStream in = new FileInputStream(location);
        InputStreamReader istream = new InputStreamReader(in,"utf-8");
        BufferedReader bfr = new BufferedReader(istream);
        
        //writer 1 to output original query
        FileOutputStream out1 = new FileOutputStream("./result/ngram/original.txt");
        OutputStreamWriter ostream1 = new OutputStreamWriter(out1,"utf-8");
        BufferedWriter bfw1 = new BufferedWriter(ostream1);      
        
        //writer 2 to output cover query 1
        FileOutputStream out2 = new FileOutputStream("./result/ngram/cover1.txt");
        OutputStreamWriter ostream2 = new OutputStreamWriter(out2,"utf-8");
        BufferedWriter bfw2 = new BufferedWriter(ostream2);
        
        //writer 3 to output cover query 2
        FileOutputStream out3 = new FileOutputStream("./result/ngram/cover2.txt");
        OutputStreamWriter ostream3 = new OutputStreamWriter(out3,"utf-8");
        BufferedWriter bfw3 = new BufferedWriter(ostream3);
        
        
        //Process line, put probability into hashMap, and write results into file
        String line;
        while((line = bfr.readLine())!= null){
            lineContent = line.split(",");      
            //Input the key and count into local dictionary
            for(String s:lineContent) {
            if(!dict.containsKey(s)) {
                System.out.println(s);
                String prob = getJointProbability(s,"e1ab23a3e8394363ac7b1df4e5fb4cac");
                dict.put(s,Double.parseDouble(prob));
                if(s.equals(lineContent[0])) {
                    bfw1.write(s + ", " + prob);
                    bfw1.newLine();
                }
                if(s.equals(lineContent[1])) {
                    bfw2.write(s + ", " + prob);
                    bfw2.newLine();
                }
                if(lineContent.length==2)
                    break;
                if(s.equals(lineContent[2])) {
                    bfw3.write(s + ", " + prob);
                    bfw3.newLine();
                }
            }
            }   
        }
        bfr.close();
        bfw1.close();
        bfw2.close();
        bfw3.close();
        
    }
     
    public static void calc(String location) throws FileNotFoundException, UnsupportedEncodingException, IOException {
        
        //import hashmap from properties
        HashMap<String, Double> dict = new HashMap<String, Double>();
        Properties properties = new Properties();
        properties.load(new FileInputStream("data.properties"));

        for (String key : properties.stringPropertyNames()) {
            dict.put(key, (Double) properties.get(key));
        }
        
        String[] lineContent;
        FileInputStream in = new FileInputStream(location);
        InputStreamReader istream = new InputStreamReader(in,"utf-8");
        BufferedReader bfr = new BufferedReader(istream);
            
        //writerto output cover - original difference
        FileOutputStream out1 = new FileOutputStream("./result/ngram/Result1.txt");
        OutputStreamWriter ostream1 = new OutputStreamWriter(out1,"utf-8");
        BufferedWriter bfw1 = new BufferedWriter(ostream1); 
        
        //writer 2 to output cover query 2
        FileOutputStream out2 = new FileOutputStream("./result/ngram/Result2.txt");
        OutputStreamWriter ostream2 = new OutputStreamWriter(out2,"utf-8");
        BufferedWriter bfw2 = new BufferedWriter(ostream2);
        
        
        //Process line, put probability into hashMap, and write results into file
        String line;
        while((line = bfr.readLine())!= null){
            lineContent = line.split(",");    
            String origin = lineContent[0];
            double valO = dict.get(origin);
            String cover1 = lineContent[1];
            double valC1 = dict.get(cover1);
            double result1 = valC1 - valO;
             
            bfw1.write(Double.toString(result1));  
            bfw1.newLine();
            
            if(lineContent.length==2)
                    break;
            String cover2 = lineContent[2];
            double valC2 = dict.get(cover2);
            double result2 = valC2 - valO;
            
            bfw2.write(Double.toString(result2));  
            bfw2.newLine();
        }
        bfw1.close();
        bfw2.close();    
    }
     
     public static void main(String[] args) throws UnsupportedEncodingException, IOException{
     /*   WebLanguageModel weblm = new WebLanguageModel();
        //process file using the designated apiKey
        processFile("./data/ngramOrigin.txt","./result/ngram/ngramOHits.txt");
        processFile("./data/ngramCover.txt","./result/ngram/ngramCHits.txt");
        calcDiff("./result/ngram/ngramOHits.txt","./result/ngram/ngramCHits.txt");
*/
      processQueries("./data/triQueries.txt");
      //calc("./data/test.txt");
      //calc("./data/triQueries.txt");
    }
    
}
