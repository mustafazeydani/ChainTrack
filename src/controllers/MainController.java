package controllers;

import controllers.MyWallets.MyWalletsController;
import controllers.Portfolio.PortfolioController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.fxml.Initializable;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.scene.layout.AnchorPane;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.VBox;
import java.io.IOException;
import javafx.scene.Node;
import java.util.ArrayList;
import java.util.Arrays;
import javafx.scene.control.Label;
import javafx.scene.control.ComboBox;

import java.lang.reflect.Method;

public class MainController implements Initializable {
	/* Nav Menu */
	@FXML
    private Button HomeNavBtn;

    @FXML
    private Button MyWalletsNavBtn;

    @FXML
    private Button PortfolioNavBtn;

    @FXML
    private Button TransactionsNavBtn;
    
    @FXML
    private AnchorPane headerAnchorPane;

    @FXML
    private VBox MainVBox;

    @FXML
    private Label mainTitle;
    
    @FXML
    private ComboBox<Network> networksComboBox;
    
    private Map<String, Class<?>> controllerMap = new HashMap<>();

    private ObservableList<Network> networksList = FXCollections.observableArrayList();
    
    
    public void initialize(URL arg0, ResourceBundle arg1) {
    	// Fetch the networks
    	fetchNetworks();
    	
    	// Add the controllers to the controllerMap
    	controllerMap.put("MyWallets", MyWalletsController.class);
    	controllerMap.put("Portfolio", PortfolioController.class);
    	
    	// Load the Home form
        loadForm("Home");

        // Set the navigation buttons
        HomeNavBtn.setOnAction(e -> {
        	switchNavigationButtonFocus(HomeNavBtn);
            loadForm("Home");
        });

        MyWalletsNavBtn.setOnAction(e -> {
        	switchNavigationButtonFocus(MyWalletsNavBtn);
            loadForm("MyWallets");
        });

        PortfolioNavBtn.setOnAction(e -> {
        	switchNavigationButtonFocus(PortfolioNavBtn);
            loadForm("Portfolio");
        });

        TransactionsNavBtn.setOnAction(e -> {
        	switchNavigationButtonFocus(TransactionsNavBtn);
            loadForm("Transactions");
        });
    }
    
	private void fetchNetworks() {
		String query = "SELECT * FROM networks";
		List<Map<String, Object>> resultList = DatabaseManager.getQuery(query);
		try {
			if (resultList.size() == 0) {
				return;
			}
			networksComboBox.getItems().clear();
			for (Map<String, Object> row : resultList) {
				networksList.add(new Network((String) row.get("name"), (String) row.get("subdomain")));
			}
			networksComboBox.setItems(networksList);
			networksComboBox.getSelectionModel().select(1);
			networksComboBox.setConverter(new NetworkStringConvertor());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
    
    private void switchNavigationButtonFocus(Button selectedButton) {
        ArrayList<Button> navButtons = new ArrayList<>(Arrays.asList(HomeNavBtn, MyWalletsNavBtn, PortfolioNavBtn, TransactionsNavBtn));
        for (Button button : navButtons) {
            if (button == selectedButton) {
                button.getStyleClass().add("nav-menu-button-focused");
            } else {
                button.getStyleClass().remove("nav-menu-button-focused");
            }
        }
    }

    private Node currentForm;
    
    private void loadForm(String name) {
        try {
        	// If the current form is the same as the new form, do not reload the form
            if (currentForm != null && currentForm.getId().equals(name + "Form")) {
                return;
            }

            // Load the form
            FXMLLoader walletsComboBoxLoader = new FXMLLoader(getClass().getResource("../FXML/WalletsComboBox.fxml"));
            FXMLLoader loader = new FXMLLoader(getClass().getResource("../FXML/" + name + "/" + name +".fxml"));
            AnchorPane walletsComboBoxForm = walletsComboBoxLoader.load();
            VBox form = loader.load();
            form.setId(name + "Form");
            
            // Set the title
            mainTitle.setText(name);
            
			if (!name.equals("Home")) {
	            try {
	                Method method = controllerMap.get(name).getMethod("setNetworksComboBox", ComboBox.class);
					method.invoke(loader.getController(), networksComboBox);
					
					networksComboBox.setVisible(true);
					
					// Get the walletsComboBox from the walletsComboBoxLoader and set it in the form
					Method getWalletsComboBox = walletsComboBoxLoader.getController().getClass().getMethod("getWalletsComboBox");
					ComboBox<String> walletsComboBox = (ComboBox<String>) getWalletsComboBox.invoke(walletsComboBoxLoader.getController());
					Method setComboBoxMethod = controllerMap.get(name).getMethod("setWalletsComboBox", ComboBox.class);
					setComboBoxMethod.invoke(loader.getController(), walletsComboBox);
	            }
				catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				networksComboBox.setVisible(false);
			}
            
			// Keep the headerAnchorPane and add the form to the MainVBox
            MainVBox.getChildren().remove(1, MainVBox.getChildren().size());
            
            // Add the walletsComboBoxForm and the form to the MainVBox
            if(!name.equals("Home"))
            	MainVBox.getChildren().add(1, walletsComboBoxForm);
            MainVBox.getChildren().add(form);
            
            // Set the currentForm to the new form
            currentForm = form;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
