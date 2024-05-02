package controllers;

import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.util.Map;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.fxml.Initializable;
import java.net.URL;
import java.util.ResourceBundle;

public class WalletsComboBoxController implements Initializable {
	@FXML
	private ComboBox<String> walletsComboBox;
	
	private ObservableList<String> walletsList = FXCollections.observableArrayList();
	
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		fetchWallets();
	}
	
	public ComboBox<String> getWalletsComboBox() {
		return walletsComboBox;
	}
	
    void fetchWallets() {
        String query = "SELECT * FROM wallets";
        List<Map<String, Object>> resultList = DatabaseManager.getQuery(query);
        try {
            if (resultList.size() == 0) {
                walletsComboBox.setDisable(true);
                return;
            }
            walletsComboBox.setDisable(false);
            walletsComboBox.getItems().clear();
			for (Map<String, Object> row : resultList) {
				walletsList.add((String) row.get("address"));
			}
			walletsComboBox.setItems(walletsList);
			walletsComboBox.getSelectionModel().selectFirst();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
