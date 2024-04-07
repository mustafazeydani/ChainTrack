package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;

public class AddWalletPopupController {
    @FXML
    private TextField address;
    
    @FXML
    void onTextChanged(KeyEvent event) {
    	String addressText = address.getText();
		if (addressText.matches("^0x[0-9a-fA-F]{40}$")) {
			address.setStyle("-fx-border-color: green");
		} else {
			address.setStyle("-fx-border-color: red");
		}
    }
}
