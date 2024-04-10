package controllers.MyWallets;
import controllers.DatabaseManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javafx.fxml.FXML;
import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
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
    private Label noWalletsLabel;
    
    @FXML
    private ComboBox<String> walletsComboBox;
    
	void setWalletsComboBox(ComboBox<String> walletsComboBox) {
		this.walletsComboBox = walletsComboBox;
	}
	
	void setNoWalletsLabel(Label noWalletsLabel) {
		this.noWalletsLabel = noWalletsLabel;
	}

	@FXML
	void handleAddWallet(ActionEvent event) {
	    try {
	        String addressText = address.getText();
	        String uuid = java.util.UUID.randomUUID().toString();
	        String query = "INSERT INTO wallets (id, address) VALUES (?, ?)";
	        
	        // Use PreparedStatement to prevent SQL injection
	        PreparedStatement preparedStatement = DatabaseManager.getConnection().prepareStatement(query);
	        preparedStatement.setString(1, uuid);
	        preparedStatement.setString(2, addressText);
	        
	        // Execute the update query
	        int rowsAffected = preparedStatement.executeUpdate();
	        
	        if (rowsAffected > 0) {
	            // Insertion successful
	            if (walletsComboBox.isDisabled()) {
	                walletsComboBox.setDisable(false);
	            }
	            if (noWalletsLabel.isVisible()) {
	                noWalletsLabel.setVisible(false);
	            }
	            walletsComboBox.getItems().add(addressText);
	        } else {
	            Alert alert = new Alert(AlertType.ERROR);
	            alert.setTitle("Error");
	            alert.setHeaderText("Failed to add wallet");
	            alert.setContentText("An error occurred while adding the wallet. Please try again.");
	            alert.showAndWait();
	        }
	        
	        // Close the prepared statement
	        preparedStatement.close();
	        
	    } catch (SQLException e) {
	        Alert alert = new Alert(AlertType.ERROR);
	        alert.setTitle("Error");
	        alert.setHeaderText("Failed to add wallet");
	        alert.setContentText(e.getMessage());
	        alert.showAndWait();
	    } finally {
	        // Close the stage
	        Stage stage = (Stage) add.getScene().getWindow();
	        stage.close();
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
