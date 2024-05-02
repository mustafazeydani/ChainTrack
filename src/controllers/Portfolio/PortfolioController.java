package controllers.Portfolio;

import controllers.Network;
import javafx.fxml.FXML;

import javafx.scene.control.ComboBox;

import javafx.scene.layout.VBox;

public class PortfolioController {
	@FXML
	private VBox Portfolio;
    @FXML
    private ComboBox<String> walletsComboBox;
    @FXML
    private ComboBox<Network> networksComboBox;
	
	public void setWalletsComboBox(ComboBox<String> walletsComboBox) {
		this.walletsComboBox = walletsComboBox;
		
		walletsComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
			System.out.println("Wallet changed to: " + newValue);
		});
	}
	
	public void setNetworksComboBox(ComboBox<Network> networksComboBox) {
		this.networksComboBox = networksComboBox;
		
		networksComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
			System.out.println("Network changed to: " + newValue);
		});
	}

}
