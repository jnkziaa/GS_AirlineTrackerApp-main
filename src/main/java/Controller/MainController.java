package Controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.util.Callback;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Date;
import java.util.Locale;
import java.util.ResourceBundle;

import static java.time.format.DateTimeFormatter.ISO_OFFSET_TIME;
import static java.time.format.DateTimeFormatter.ofLocalizedDate;

public class MainController implements Initializable {

    private Boolean ontime = true;
    private final String accessKey = "f90129335c49b254755f388b5503853a"; //access key duhh
    @FXML
    public DatePicker dpDate;
    @FXML
    public Label aitaFlightNum, termDepLabel, termArrLabel, gateDepLabel, gateArrLabel, scheduledArrLabel, estimatedArrLabel, estimatedDepLabel, scheduleDepLabel,
                airlineName, aitaArrLabel, aitaDepLabel, flightStatusLabel, timeLabel;

    @FXML
    private TextField airlines, flightNumbers;
    @FXML
    private Text depAirport, arrAirport;

    @FXML
    private TextArea depDatas;
    @FXML
    private TextArea arrDatas;



    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Callback<DatePicker, DateCell> blockedDates = dp -> new DateCell() {
            @Override
            public void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty); //To change body of generated methods, choose Tools | Templates.
                LocalDate today = LocalDate.now();
                setDisable(empty || item.isAfter(today) || item.isBefore(today)); //block any day that isnt today
            }

        };


        dpDate.setDayCellFactory(blockedDates);
    }

    @FXML
    public void getAirlineData(ActionEvent event) throws MalformedURLException, ParseException {
        StringBuilder strBuild = new StringBuilder(airlines.getText());
        String flightNumberData = flightNumbers.getText();
        if (strBuild.toString().contains(" ")) { //replace "space" with + because thats what API wants us to do
            int spaceFiller = strBuild.lastIndexOf(" ");
            strBuild.replace(spaceFiller, spaceFiller + 1, "+");
        }
        String newString = String.format("?airline_name=%s&flight_number=%s&access_key=%s", strBuild, flightNumberData, accessKey);
        System.out.println(newString);
        getCurrentInfo(newString);


    }

    private void getCurrentInfo(String url) throws MalformedURLException, ParseException {
        APIConnector apiConnector = new APIConnector("http://api.aviationstack.com/v1/flights" + url);
        JSONObject jsonObject = apiConnector.getJSONArray();
        String flightStatus = StringUtils.capitalize(jsonObject.get("flight_status").toString());
        flightStatusLabel.setText(flightStatus);


        String departureString = jsonObject.get("departure").toString();
        String arrivalString = jsonObject.get("arrival").toString();
        String liveDataString = jsonObject.get("live").toString();
        String airlineData = jsonObject.get("airline").toString();
        String flightData = jsonObject.get("flight").toString();
        System.out.println(flightData);
        System.out.println(liveDataString);
        departureField(departureString);
        arrivalField(arrivalString);
        miscField(airlineData, flightData);
        liveData(liveDataString);
    }

    private void miscField(String airlineData, String flightData) throws ParseException {
        String airlineDataString = "[" + airlineData + "]";
        JSONParser parse = new JSONParser();
        JSONArray dataObjectAirline = (JSONArray) parse.parse(airlineDataString);
        JSONObject airlineDataJson = (JSONObject) dataObjectAirline.get(0);
        String flightAirline = airlineDataJson.get("name").toString();
        String flightIata = airlineDataJson.get("iata").toString();
        String combinedDatas = flightAirline + String.format(" (%s)", flightIata);
        airlineName.setText(combinedDatas);

        String flightDataString = "[" + flightData + "]";
        JSONArray dataObjectFlight = (JSONArray) parse.parse(flightDataString);
        JSONObject flightDataJson = (JSONObject) dataObjectFlight.get(0);
        String icao = flightDataJson.get("icao").toString();
        aitaFlightNum.setText(icao);

    }

    private void arrivalField(String arrivalString) throws ParseException {
        String newArrival = "[" + arrivalString + "]";
        JSONParser parse = new JSONParser();
        JSONArray dataObject = (JSONArray) parse.parse(newArrival);
        JSONObject departureData = (JSONObject) dataObject.get(0);
        arrAirport.setText(departureData.get("airport").toString());
        String timezone = departureData.get("timezone").toString();
        String scheduled = departureData.get("scheduled").toString();
        String formattedSchedule = dateFormatter(scheduled);
        scheduledArrLabel.setText(formattedSchedule);

        String estimated = departureData.get("estimated").toString();
        String formattedEstimated = dateFormatter(scheduled);
        estimatedArrLabel.setText(formattedEstimated);
        
        String gate = departureData.get("gate").toString();
        String terminal = departureData.get("terminal").toString();
        String iata = departureData.get("iata").toString();
        aitaArrLabel.setText(iata);
        termArrLabel.setText(terminal);
        gateArrLabel.setText(gate);
        if(!scheduled.equalsIgnoreCase(estimated)){
            timeLabel.setText("Delayed");
        }
        else {
            timeLabel.setText("On Time");
        }


    }


    private void departureField(String departureString) throws ParseException {
        String newDeparture = "[" + departureString + "]";
        JSONParser parse = new JSONParser();
        JSONArray dataObject = (JSONArray) parse.parse(newDeparture);
        JSONObject departureData = (JSONObject) dataObject.get(0);
        depAirport.setText(departureData.get("airport").toString());
        String timezone = departureData.get("timezone").toString();
        String scheduled = departureData.get("scheduled").toString();
        String formattedScheduled = dateFormatter(scheduled);
        scheduleDepLabel.setText(formattedScheduled);

        String estimated = departureData.get("estimated").toString();
        String formattedEstimated = dateFormatter(estimated);
        estimatedDepLabel.setText(formattedEstimated);

        String gate = departureData.get("gate").toString();
        String terminal = departureData.get("terminal").toString();
        String iata = departureData.get("iata").toString();
        String icao = departureData.get("icao").toString();
        aitaDepLabel.setText(iata);
        termDepLabel.setText(terminal);
        gateDepLabel.setText(gate);


    }

    private String dateFormatter(String scheduled) {
        String actualDate = scheduled.substring(0, 10);
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH);
        DateTimeFormatter dtf2 = DateTimeFormatter.ofPattern("MMM dd yy", Locale.ENGLISH);
        LocalDate ld = LocalDate.parse(actualDate, dtf);
        String formattedDate = dtf2.format(ld);
        String formattedTime = null;
        try {
            final SimpleDateFormat sdf = new SimpleDateFormat("H:mm");
            final Date dateObj = sdf.parse(scheduled.substring(11,16));

            formattedTime = new SimpleDateFormat("K:mm a").format(dateObj);
        } catch (final java.text.ParseException e) {
            e.printStackTrace();
        }
        return formattedDate + "\n" + formattedTime;
    }

    private void liveData(String liveData) throws ParseException {
        try {
            String newLiveData = "[" + liveData + "]";
            //System.out.println(newLiveData);
            JSONParser parse = new JSONParser();
            JSONArray dataObject = (JSONArray) parse.parse(newLiveData);
            //System.out.println(dataObject);
            JSONObject liveFlightData = (JSONObject) dataObject.get(0);
            String lat = liveFlightData.get("latitude").toString();
            String longe = liveFlightData.get("longitude").toString();
            String alti = liveFlightData.get("altitude").toString();
            String speed = liveFlightData.get("speed_horizontal").toString();
            try {
                File file = new File("googleMapEmbed.html");
                Writer fileWriter = new FileWriter(file, false);
                fileWriter.write("<!DOCTYPE html>\n" +
                        "<html lang=\"en\">\n" +
                        "<head>\n" +
                        "    <meta charset=\"UTF-8\">\n" +
                        "    <title>HTML Google Map HTML Embed</title>\n" +
                        "</head>\n" +
                        "<body style=\"background-color:black\">\n" +
                        "    <div style=\"position:relative; height=250px; weight:450pxs;\">\n" +
                        "        <iframe\n" +
                        "            width=\"450\"\n" +
                        "            height=\"250\"\n" +
                        "            frameborder=\"0\" style=\"border:0\"\n" +
                        "            referrerpolicy=\"no-referrer-when-downgrade\"\n" +
                        "            src=\"https://www.google.com/maps/embed/v1/view?key=AIzaSyDqkeHwRwVib_iIGz1x8-qBLDe_PHeDvSM&center=" + lat + "," + longe + "zoom=12&maptype=satellite\"\n" +
                        "            allowfullscreen>\n" +
                        "        </iframe>\n" +
                        "        <div style=\"position:absolute; z-index:1; left:225px; margin-right:-50%; transform:translate(-50%,-50%); top:125px;\">\n" +
                        "            <img style=\"height:40px;width:40px;border-style:solid;border-width:2px;border-color:white;border-radius:10px;background:rgba(0,0,0,.5)\" src=\"https://upload.wikimedia.org/wikipedia/commons/1/17/Airplane_silhouette_orange.svg\">\n" +
                        "        </div>\n" +
                        "    </div>\n" +
                        "</body>\n" +
                        "</html>");
                fileWriter.close();
            } catch (IOException e) {
                System.out.println("did not rewrite file");
            }


        } catch (Exception e) {
            System.out.println("no live data");
        }
    }

}



