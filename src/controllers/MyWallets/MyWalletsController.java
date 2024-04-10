package controllers.MyWallets;

import controllers.DatabaseManager;
import javafx.fxml.Initializable;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.event.ActionEvent;
import javafx.scene.control.Label;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.VBox;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.Scene;

public class MyWalletsController implements Initializable {
    @FXML
    private VBox MyWallets;
    
    @FXML
    private Label noWalletsLabel;
    
    @FXML
    private ComboBox<String> walletsComboBox;
    
    @Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		fetchWallets();
	}

    void fetchWallets() {
        String query = "SELECT * FROM wallets";
        List<Map<String, Object>> resultList = DatabaseManager.getQuery(query);
        try {
            if (resultList.size() == 0) {
                noWalletsLabel.setVisible(true);
                walletsComboBox.setDisable(true);
                return;
            }
            noWalletsLabel.setVisible(false);
            walletsComboBox.setDisable(false);
            walletsComboBox.getItems().clear();
			for (Map<String, Object> row : resultList) {
				walletsComboBox.getItems().add((String) row.get("address"));
			}
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
	@FXML
	void handleAddWallet(ActionEvent event) {
	    try {
	        FXMLLoader loader = new FXMLLoader(getClass().getResource("../../FXML/MyWallets/AddWalletPopup.fxml"));
	        Parent root = loader.load();
	        AddWalletPopupController controller = loader.getController();
	        controller.setWalletsComboBox(walletsComboBox);
	        controller.setNoWalletsLabel(noWalletsLabel);
	        Stage stage = new Stage();
	        stage.initModality(Modality.APPLICATION_MODAL);
	        stage.setTitle("Add Wallet");
	        stage.setScene(new Scene(root));
	        stage.show();
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	}
}
