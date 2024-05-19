package controllers.Auth;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.UUID;

import controllers.DatabaseManager;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import controllers.MainController;
import javafx.scene.Scene;

import javafx.scene.control.Button;

import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.scene.control.Label;

import javafx.scene.control.PasswordField;

public class SignupController implements Initializable {
	private Stage primaryStage;
	private FormController formController;
	@FXML
	private TextField usernameField;
	@FXML
	private Button signupBtn;
	@FXML
	private PasswordField passwordField;
	@FXML
	private Label loginLink;
	@FXML
	private PasswordField confirmPasswordField;
	@FXML
	private Label errorMessage;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		loginLink.setOnMouseClicked(e -> {
			formController.SwitchForm("Login");
		}); 
		
		signupBtn.setOnAction(e -> {
			// Login
			String username = usernameField.getText();
			String password = passwordField.getText();
			String confirmPassword = confirmPasswordField.getText();
			
			if (!validateInput(username, password, confirmPassword)) {
				return;
			}
            User user = signup(username, password);
            if (user != null) {
				// Redirect to Main form
				FXMLLoader loader = new FXMLLoader(getClass().getResource("../../FXML/Main.fxml"));
				try {
					Parent root = loader.load();
					MainController mainController = loader.getController();
					mainController.setPrimaryStage(primaryStage);
					mainController.setLoggedInUser(user);
					Scene scene = new Scene(root);
					primaryStage.setScene(scene);
					primaryStage.setTitle("ChainTrack");
					primaryStage.setResizable(false);
					primaryStage.centerOnScreen();
					primaryStage.show();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				return;
			}
			// Show error message
		});
	}
	
	public boolean validateInput(String username, String password, String confirmPassword) {
		// Validate input
		if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
			// Show error message
			errorMessage.setVisible(true);
			errorMessage.setText("Please fill in all fields.");
			return false;
		} else if (!password.equals(confirmPassword)) {
			// Show error message
			errorMessage.setVisible(true);
			errorMessage.setText("Passwords do not match.");
			return false;
		} else if (password.length() < 8) {
			// Show error message
			errorMessage.setVisible(true);
			errorMessage.setText("Password must be at least 8 characters long.");
			return false;
		}
		return true;
	}
	
	public void setPrimaryStage(Stage primaryStage) {
		this.primaryStage = primaryStage;
	}
	
	void setFormController(FormController formController) {
		this.formController = formController;
	}
	
	private User signup(String username, String password) {
		// Check if user already exists
		String query = "SELECT * FROM users WHERE username = '" + username + "'";
		if (DatabaseManager.getQuery(query).size() > 0) {
			// Show error message
			errorMessage.setVisible(true);
			errorMessage.setText("Username already exists.");
            return null;
		}
		
        byte[] salt = generateSalt();
        String hashedPassword = hashPassword(password, salt);

		String uuid = UUID.randomUUID().toString();
        
		// Insert user
		query = "INSERT INTO users (uuid, username, password, salt) VALUES ('" + uuid + "', '" + username + "', '" + hashedPassword + "', '" + Base64.getEncoder().encodeToString(salt) + "')";
		DatabaseManager.updateQuery(query);
		User user = new User(uuid ,username);
		return user;
	}

    // Method to generate a random salt
    private static byte[] generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16]; // You can adjust the salt size as needed
        random.nextBytes(salt);
        return salt;
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
}
