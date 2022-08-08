package Controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
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
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;
import java.util.ResourceBundle;


public class MainController implements Initializable {

    private final String accessKey = "b22650573a3e39cf5f3ee76c113b9d38"; //access key duhh
    private APIConnector apiConnector;
    @FXML
    public DatePicker dpDate;
    @FXML
    public Text aitaFlightNum, termDepLabel, termArrLabel, gateDepLabel, gateArrLabel, scheduledArrLabel, estimatedArrLabel, estimatedDepLabel, scheduleDepLabel,
                airlineName, aitaArrLabel, aitaDepLabel, flightStatusLabel, timeLabel;

    @FXML
    private TextField airlines, flightNumbers;
    @FXML
    private Text depAirport, arrAirport;


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
        String newString = String.format("?airline_name=&flight_number=&access_key=%s", accessKey);

        try {
            apiConnector = new APIConnector("http://api.aviationstack.com/v1/flights"+newString);
            apiConnector.getFullApi();

        } catch (MalformedURLException e) {
            System.out.println("not working in initialize");
        }

        airlines.textProperty().addListener((a,b,c) ->{
            apiConnector.getAirline(airlines, flightNumbers);
        });

        flightNumbers.textProperty().addListener((a,b,c) ->{
            apiConnector.getFlights(airlines, flightNumbers);
        });
    }

    @FXML
    public void getAirlineData(ActionEvent event) throws MalformedURLException, ParseException {
        String flightNumberData = flightNumbers.getText();
        getCurrentInfo(flightNumberData);


    }

   /*Main method in file.  Changes api data to JSONObject and takes
    several keys in the data into different methods in file
    @param flightNumbers
    */
    private void getCurrentInfo(String flightNum) throws MalformedURLException, ParseException {
        JSONObject jsonObject = apiConnector.findData(flightNum);
        System.out.println(jsonObject);
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

        JSONObject airlineDataJson = jsonConverter(airlineData);
        String flightAirline = airlineDataJson.get("name").toString();
        String flightIata = airlineDataJson.get("iata").toString();
        String combinedDatas = flightAirline + String.format(" (%s)", flightIata);
        airlineName.setText(combinedDatas);


        JSONObject flightDataJson = jsonConverter(flightData);
        String icao = flightDataJson.get("icao").toString();
        aitaFlightNum.setText(icao);

    }

    private void arrivalField(String arrivalString) throws ParseException {
        JSONObject departureData = jsonConverter(arrivalString);
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
        JSONObject departureData = jsonConverter(departureString);
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

        String formattedDate = null;
        String formattedTime = null;
        try {
            String actualDate = scheduled.substring(0, 10);
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH);
            DateTimeFormatter dtf2 = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.ENGLISH);
            LocalDate ld = LocalDate.parse(actualDate, dtf);
            formattedDate = dtf2.format(ld);

            final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
            final Date dateObj = sdf.parse(scheduled.substring(11,16));

            formattedTime = new SimpleDateFormat("K:mm a").format(dateObj);
        } catch (final java.text.ParseException e) {
            e.printStackTrace();
        }
        return formattedDate + "\n" + formattedTime;
    }

    private JSONObject jsonConverter(String input) throws ParseException {
        String jsonDataString = String.format("[%s]", input);
        JSONParser parse = new JSONParser();
        JSONArray dataObjectAirline = (JSONArray) parse.parse(jsonDataString);
        JSONObject inputDataJson = (JSONObject) dataObjectAirline.get(0);
        return inputDataJson;
    }

    private void liveData(String liveData) throws ParseException {
        try {
            JSONObject liveFlightData = jsonConverter(liveData);
            String lat = liveFlightData.get("latitude").toString();
            String longe = liveFlightData.get("longitude").toString();
            //String alti = liveFlightData.get("altitude").toString();
            //String speed = liveFlightData.get("speed_horizontal").toString();
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


        }catch (Exception e) {
            try{
                File file = new File("googleMapEmbed.html");
                Writer fileWriter = new FileWriter(file, false);
                fileWriter.write("<!DOCTYPE html>\n" +
                        "<html lang=\"en\">\n" +
                        "<head>\n" +
                        "    <meta charset=\"UTF-8\">\n" +
                        "    <title>HTML Google Map HTML Embed</title>\n" +
                        "</head>\n" +
                        "<body style=\"background-color:black\">\n" +
                        "   <h1 style=\"font-color:white\">NO LIVE DATA<\\h1>" +
                        "</body>\n" +
                        "</html>");
                fileWriter.close();
            } catch (IOException f) {
            }
        }
    }


    public void buttonNext(ActionEvent actionEvent) {
        FXMLLoader fxmlLoader = new FXMLLoader(MainController.class.getResource("/View/liveData.fxml"));
        Parent root;
        try {
            root = fxmlLoader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Scene scene = new Scene(root);
        Stage stage = new Stage();
        stage.initStyle(StageStyle.UTILITY);

        stage.setTitle("Live Data Viewer");
        stage.setScene(scene);
        stage.show();
    }
}



