package controllers.Auth;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;

import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import controllers.DatabaseManager;
import controllers.MainController;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;

import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.scene.control.Label;

import javafx.scene.control.PasswordField;

public class LoginController implements Initializable {
	private Stage primaryStage;
	private FormController formController;
	
	@FXML
	private TextField usernameField;
	@FXML
	private Button loginBtn;
	@FXML
	private PasswordField passwordField;
	@FXML
	private Label signupLink;
	@FXML
	private Label errorMessage;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		signupLink.setOnMouseClicked(e -> {
			formController.SwitchForm("Signup");
		}); 
		
		loginBtn.setOnAction(e -> {
			// Login
			String username = usernameField.getText();
			String password = passwordField.getText();
			if (username.isEmpty() || password.isEmpty()) {
				// Show error message
				errorMessage.setVisible(true);
				errorMessage.setText("Please fill in all fields");
				return;
			}
			// Authenticate user
			if (authenticate(username, password)) {
				// Redirect to dashboard
				return;
			}
			// Show error message
		});
	}
	
	public void setPrimaryStage(Stage primaryStage) {
		this.primaryStage = primaryStage;
	}
	
	void setFormController(FormController formController) {
        this.formController = formController;
    }
	
	private boolean authenticate(String username, String password) {
		// Authenticate user
		String query = "SELECT * FROM users WHERE username = '" + username + "'";
		List<Map<String, Object>> resultList = DatabaseManager.getQuery(query);
		if (resultList.size() == 0) {
			errorMessage.setVisible(true);
			errorMessage.setText("Invalid username or password");
            return false;
		}
		Map<String, Object> user = resultList.get(0);
		String storedHashedPassword = (String) user.get("password");
		String saltAsString = (String) user.get("salt");
	    byte[] salt = Base64.getDecoder().decode(saltAsString);
		if (verifyPassword(password, storedHashedPassword, salt)) {
			User loggedInUser = new User((String) user.get("id"), (String) user.get("username"));
			// Redirect to Main form
			FXMLLoader loader = new FXMLLoader(getClass().getResource("../../FXML/Main.fxml"));
			try {
				Parent root = loader.load();
				MainController mainController = loader.getController();
				mainController.setPrimaryStage(primaryStage);
				mainController.setLoggedInUser(loggedInUser);
				Scene scene = new Scene(root);
				primaryStage.setScene(scene);
				primaryStage.setTitle("ChainTrack");
				primaryStage.setResizable(false);
				primaryStage.centerOnScreen();
				primaryStage.show();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			return true;
		}
		errorMessage.setVisible(true);
		errorMessage.setText("Invalid username or password");
		return false;
	}
	
    // Method to hash a password with a salt using SHA-256 hashing algorithm
    private static String hashPassword(String password, byte[] salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt);
            byte[] hashedBytes = md.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hashedBytes);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }
	
    private static boolean verifyPassword(String enteredPassword, String storedHashedPassword, byte[] salt) {
        String hashedEnteredPassword = hashPassword(enteredPassword, salt);
        return hashedEnteredPassword.equals(storedHashedPassword);
    }
}
