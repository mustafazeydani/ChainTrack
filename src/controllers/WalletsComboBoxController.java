package controllers;

import java.util.List;
import java.util.Map;

import controllers.Auth.User;
import javafx.collections.ObservableList;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;

public class WalletsComboBoxController  {
	private User loggedInUser;
	
	@FXML
	private ComboBox<String> walletsComboBox;
	
	private ObservableList<String> walletsList = FXCollections.observableArrayList();
	
	public ComboBox<String> getWalletsComboBox() {
		return walletsComboBox;
	}
	
	public void setLoggedInUser(User loggedInUser) {
		this.loggedInUser = loggedInUser;
		
		fetchWallets(loggedInUser);
	}

    void fetchWallets(User loggedInUser) {
        String query = "SELECT * FROM wallets WHERE userId = '" + loggedInUser.getId() + "'";
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
