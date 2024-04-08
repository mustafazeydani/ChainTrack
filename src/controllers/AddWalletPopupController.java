package controllers;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import javafx.fxml.FXML;
import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

public class AddWalletPopupController {
    @FXML
    private TextField address;
    
    @FXML
    private Button add;

    @FXML
    private Button cancel;

    @FXML
    void handleAddWallet(ActionEvent event) {
        	try {
				if (validateAddress()) {
					// Add wallet to database
					
					// Close popup
					Stage stage = (Stage) add.getScene().getWindow();
					stage.close();
				} else {
					Alert alert = new Alert(AlertType.ERROR);
					alert.setTitle("Invalid Address");
					alert.setHeaderText("The address you entered is invalid.");
					alert.setContentText("Please enter a valid address.");
					alert.showAndWait();
				}
        	}
			catch (URISyntaxException | IOException | InterruptedException e) {
				e.printStackTrace();
			}
    }
    
	Boolean validateAddress() throws URISyntaxException, IOException, InterruptedException {
    	String addressText = address.getText();
        String apiUrl = "https://rest.cryptoapis.io/blockchain-tools/binance-smart-chain/testnet/addresses/validate";
        String apiKey = "0c7acd229ecec95e344455a097d1f40015981c66";
        String requestBody = "{\"data\": {\"item\": {\"address\": \"" + addressText + "\"}}}";

        // Create an HttpClient
        HttpClient client = HttpClient.newHttpClient();

        // Create an HttpRequest
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(apiUrl))
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .header("Content-Type", "application/json")
                .header("X-API-Key", apiKey)
                .build();

        // Send the request and retrieve the response
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

		if (response.statusCode() == 200) {
			return true;
		} else {
			return false;
		}
	}

    @FXML
    void handleCancel(ActionEvent event) {
    	Stage stage = (Stage) cancel.getScene().getWindow();
    	stage.close();
    }
    
    @FXML
    void onTextChanged(KeyEvent event) {
    	String addressText = address.getText();
		if (addressText.matches("^0x[0-9a-fA-F]{40}$")) {
			address.setStyle("-fx-border-color: green");
			if(add.isDisabled()) {
				add.setDisable(false);
			}
		} else {
			address.setStyle("-fx-border-color: red");
			if (!add.isDisabled())
				add.setDisable(true);
		}
    }
}
