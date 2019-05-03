/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package earthquakemapper;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedWriter;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import static java.lang.System.exit;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Trevor Takawira
 */
public class EarthQuakeMapper {

    EarthQuakeMapper() throws MalformedURLException, FileNotFoundException, IOException{
        HashMap<String, String> quakesList = connect();
        print(quakesList);
    }
    
    public HashMap<String, String> connect() throws MalformedURLException, FileNotFoundException, IOException{
        
       HashMap<String, String> quakes = new HashMap<String, String>();
        
       String url = buildString();
       String sb = null; 
       
       
        URL api = new URL(url);
        try {
            sb = readURL(api);
        }catch(IOException e){
            System.out.println("No Internet Access");
            sb = readCache();
        }
                       
        JsonParser jp = new JsonParser();
        JsonElement root  = jp.parse(sb);            
        
        JsonObject Root = root.getAsJsonObject();
        JsonArray features = Root.getAsJsonArray("features");

        for(JsonElement feature: features){
            JsonObject feat = feature.getAsJsonObject();
            JsonElement prop = feat.get("properties");
            JsonObject properties = prop.getAsJsonObject();

            JsonElement mag = properties.get("mag");
            String magnitude = mag.toString();
            System.out.println(magnitude);
            
            JsonElement placef = properties.get("place");
            String place = placef.toString();
                    
            quakes.put(magnitude, place);
        }
            
        return quakes;    
    }
    
    public void print(HashMap<String, String> quakes){
        for(Map.Entry<String, String> quake : quakes.entrySet()){
            String magnitude = quake.getKey();
            String place = quake.getValue();
            
            System.out.println("An earthquake of magnitude "+ magnitude +" happened at "+place);
        }
        
    }
    
    private String readURL(URL url) throws IOException{
        String sb = "";

            
        HttpURLConnection conn = (HttpURLConnection)url.openConnection();
        InputStream reader = (InputStream) conn.getContent();
        Scanner sc = new Scanner(reader, "UTF-8");
            while(sc.hasNextLine()){
                sb+= sc.nextLine();
            }

        try (
                BufferedWriter writer = new BufferedWriter(new FileWriter("query.json", true))
                ) {
            writer.write(sb);

            while(sc.hasNextLine()){
                writer.append(sc.nextLine());
            }
        } catch (IOException ex) {
            System.err.println("Failed to write to cache file");
        }


        return sb;
    }
    
    public String readCache() {
        System.out.println("Reading From cache");
        String sb = null;
        try (FileInputStream conn = new FileInputStream("query.json")) {
            sb = "";
            Scanner sc = new Scanner(conn, "UTF-8");
            while(sc.hasNextLine()){
                sb+=sc.nextLine();
            }
        }
        catch(FileNotFoundException ex){
            System.err.println("No cache file. Exiting...");
            exit(1);
        } catch (IOException ex) {
            System.err.println("Failed to open and read cache file. Exiting ...");
            exit(1);
        }
        return sb;        
    }
    
    public String buildString(){
        
        String BaseURL = "https://earthquake.usgs.gov/fdsnws/event/1/query?format=geojson&starttime=";

        Date date = new Date();
        String str = String.format("%tF",date);
        String Magnitude = "5";
        return BaseURL + date.toString()+ "\"&minmagnitude" + Magnitude;
    }
    
    public static void main(String[] args) {

        try {
            System.out.println("Starting");
            EarthQuakeMapper earthquake = new EarthQuakeMapper();
        } catch (MalformedURLException ex) {
            System.err.println("Input url is grabage");
        } catch (FileNotFoundException ex) {
            System.err.println("no cache file. Exiting...");
        } catch (IOException ex) {
            System.err.println("Failed to write to file");
        }
        
    }
    
}
