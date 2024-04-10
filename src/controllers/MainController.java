package controllers;

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
    private ComboBox<String> networksComboBox;
    
    public void initialize(URL arg0, ResourceBundle arg1) {
        loadForm("Home");

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
        
        networksComboBox.getItems().addAll("Ethereum", "Binance Smart Chain", "Polygon");
        networksComboBox.setValue("Ethereum");
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
            if (currentForm != null && currentForm.getId().equals(name + "Form")) {
                return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("../FXML/" + name + "/" + name +".fxml"));
            VBox form = loader.load();
            form.setId(name + "Form");
            mainTitle.setText(name);
			if (name.equals("Home")) {
				networksComboBox.setVisible(false);
			} else {
				networksComboBox.setVisible(true);
			}
            MainVBox.getChildren().remove(1, MainVBox.getChildren().size());
            MainVBox.getChildren().add(form);
            currentForm = form;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
