package Controller;
import javafx.scene.control.TextField;
import org.controlsfx.control.textfield.TextFields;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;


public class APIConnector {

    private final String urlString;
    private String removeNull = "";
    private JSONObject getAPI;
    private JSONObject getAirlinName;
    private JSONObject getFlightNum;
    private JSONArray dataObject;
    private String names[];
    private HashMap<String, String> getAirlineList = new HashMap<>();


    public APIConnector(String urlString) throws MalformedURLException {
        this.urlString = urlString;
    }


    //Parses API and stores data in a Hashmap, and replaces all null values with 0.
    public void getFullApi(){

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
                scanner.close();

                String actualData = informationString.substring(informationString.indexOf("["), informationString.lastIndexOf("]")+1);
                JSONParser parse = new JSONParser();
                dataObject = (JSONArray) parse.parse(actualData);

                for(int i = 0; i < 100; i++){
                    //Loops through the API and stores the flight# and airline name in key/value pairs.
                    getAPI = (JSONObject) dataObject.get(i);
                    getAirlinName = (JSONObject) getAPI.get("airline");
                    getFlightNum = (JSONObject) getAPI.get("flight");
                    getAirlineList.put(getFlightNum.get("number").toString(), getAirlinName.get("name").toString());
                }

            }
        }catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println(getAirlineList);
    }

    //Updates the suggestion field in the Textfields based off inputs within it.
    public void getAirline(TextField airline, TextField flight){
        String flights = flight.getText();
        String namesTest;
        String flightNum;

        ArrayList<String> names = new ArrayList<>();

        if(flights.isEmpty()) {
            for (Object o : getAirlineList.values()) {
                if (!names.contains(o.toString()))
                    names.add(o.toString());
            }
        }else{
            names.clear();
            for(Map.Entry<String, String> entry : getAirlineList.entrySet()){
                namesTest = entry.getValue().toString();
                flightNum = entry.getKey().toString();
                System.out.println(namesTest);
                if(flightNum.equals(flights)){
                    names.clear();
                    names.add(entry.getValue());
                    break;
                }else if(flightNum.contains(flights)){
                    names.add(entry.getValue());
                }
            }
        }
        TextFields.bindAutoCompletion(airline,names);

    }

    public void getFlights(TextField airline, TextField flight){

        String flights = flight.getText();
        String airlines = airline.getText();
        String namesTest;
        String flightNum;

        ArrayList<String> nums = new ArrayList<>();

        if(airlines.isEmpty()) {
            for (Object o : getAirlineList.keySet()) {
                if (!nums.contains(o.toString()))
                    nums.add(o.toString());
            }

        }else{
            nums.clear();
            for(Map.Entry<String, String> entry : getAirlineList.entrySet()){
                namesTest = entry.getValue().toString();
                flightNum = entry.getKey().toString();

                if(namesTest.equals(airlines)){
                    nums.clear();
                    nums.add(entry.getKey());
                    break;
                }else if(namesTest.contains(airlines)){
                    nums.clear();
                    nums.add(entry.getKey());
                }
            }
        }
        TextFields.bindAutoCompletion(flight,nums);
    }



    public JSONObject findData(String flightnum){
            JSONObject data;
            JSONObject searchData = null;


            for(int i = 0; i < 100; i++){
                data = (JSONObject) dataObject.get(i);
                getFlightNum = (JSONObject) data.get("flight");
                String num = getFlightNum.get("number").toString();
                System.out.println(num);
                if(Objects.equals(num, flightnum)){
                    searchData = (JSONObject) dataObject.get(i);
                }
            }
            return searchData;
    }


}