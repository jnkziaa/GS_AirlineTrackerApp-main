package Controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.util.Callback;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    @FXML
    public DatePicker dpDate;
    @FXML
    private TextField airlines;
    @FXML
    private TextField flightNumbers;
    private String accessKey = "9cf202df7c52030fcbe351d02d9a1834";
    @FXML
    private Text depAirport;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Callback<DatePicker, DateCell> blockedDates = dp -> new DateCell(){
            @Override
            public void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty); //To change body of generated methods, choose Tools | Templates.
                LocalDate today = LocalDate.now();
                setDisable(empty || item.isAfter(today) || item.isBefore(today));
            }

        };

        dpDate.setDayCellFactory(blockedDates);
    }

    @FXML
    public void getAirlineData(ActionEvent event) throws MalformedURLException, ParseException {
        StringBuilder strBuild = new StringBuilder(airlines.getText());
        String flightNumberData = flightNumbers.getText();
        if(strBuild.toString().contains(" ")){
            int spaceFiller = strBuild.lastIndexOf( " ");
            strBuild.deleteCharAt(spaceFiller);
            strBuild.insert(spaceFiller, "+");
        }

        String newString = String.format("?airline_name=%s&flight_number=%s&access_key=%s", strBuild, flightNumberData, accessKey);

        getCurrentInfo(newString);


    }

    private void getCurrentInfo(String url) throws MalformedURLException, ParseException {
        APIConnector apiConnector = new APIConnector("http://api.aviationstack.com/v1/flights"+url);
        System.out.println("before");
        JSONObject jsonObject = apiConnector.getJSONArray();
        String departureString = jsonObject.get("departure").toString();
        departureField(departureString);
    }

    private void departureField(String departureString) throws ParseException {
        String newDeparture = "[" + departureString + "]";
        JSONParser parse = new JSONParser();
        JSONArray dataObject = (JSONArray) parse.parse(newDeparture);
        JSONObject departureData = (JSONObject) dataObject.get(0);
        depAirport.setText(departureData.get("airport").toString());
    }


}



