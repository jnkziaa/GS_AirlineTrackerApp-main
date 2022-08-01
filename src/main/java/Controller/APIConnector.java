package Controller;
import com.google.gson.reflect.TypeToken;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import com.google.gson.Gson;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;


public class APIConnector {

    private final String urlString;
    private final String regex = ".*\": null(,)?\\r\\n";
    private String removeNull = "";
    private JSONObject testData;
    private JSONObject testData2;


    public APIConnector(String urlString) throws MalformedURLException {
        this.urlString = urlString;
    }

    public JSONObject getJSONArray(){
        ArrayList<String> getAirlineList = new ArrayList<>();

        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            System.out.println(conn + "?");
            conn.setRequestMethod("GET");
            conn.connect();

            //Check if connect is made
            int responseCode = conn.getResponseCode();
            System.out.println(responseCode + "?");

            if (responseCode != 200) {
                throw new RuntimeException("HttpResponseCode: " + responseCode);
            } else {

                StringBuilder informationString = new StringBuilder();
                Scanner scanner = new Scanner(url.openStream());

                while (scanner.hasNext()) {
                    removeNull = scanner.nextLine();
                    informationString.append(removeNull.replaceAll("null", "0"));

                }
               // System.out.println(informationString);
                scanner.close();

                String actualData = informationString.substring(informationString.indexOf("["), informationString.lastIndexOf("]")+1);
                JSONParser parse = new JSONParser();
                JSONArray dataObject = (JSONArray) parse.parse(actualData);
                JSONObject aviationData = (JSONObject) dataObject.get(0);

                /*for(int i = 0; i < 100; i++){
                     testData = (JSONObject) dataObject.get(i);
                     testData2 = (JSONObject) testData.get("airline");
                     if(!getAirlineList.contains(testData2.get("name").toString()))
                        getAirlineList.add(testData2.get("name").toString());


                }*/
                System.out.println(aviationData);
               return aviationData;

            }
        }catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public JSONObject getJSONObject(String query){
        try {
            URL url = new URL(urlString + query);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.connect();

            //Check if connect is made
            int responseCode = conn.getResponseCode();

            if (responseCode != 200) {
                throw new RuntimeException("HttpResponseCode: " + responseCode);
            } else {

                StringBuilder informationString = new StringBuilder();
                Scanner scanner = new Scanner(url.openStream());

                while (scanner.hasNext()) {
                    informationString.append(scanner.nextLine());
                }
                scanner.close();

                JSONParser parse = new JSONParser();

                return (JSONObject) parse.parse(String.valueOf(informationString));
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


}