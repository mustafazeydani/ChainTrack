package controllers;
	
import javafx.application.Application;
import controllers.Auth.FormController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.stage.Stage;
import javafx.scene.Scene;


public class Main extends Application {
	private Stage primaryStage;
	@Override
	public void start(Stage primaryStage) {
	    try {
	        this.primaryStage = primaryStage;
	        FXMLLoader loader = new FXMLLoader(getClass().getResource("../FXML/Auth/Form.fxml"));
	        Parent root = loader.load();

	        // Get the controller
	        FormController formController = loader.getController();
	        
	        // Pass the primaryStage to the controller
	        formController.setPrimaryStage(primaryStage);

	        Scene scene = new Scene(root);
	        primaryStage.setScene(scene);
	        primaryStage.setTitle("ChainTrack");
	        primaryStage.centerOnScreen();
	        primaryStage.show();
	    } catch(Exception e) {
	        e.printStackTrace();
	    }
	}

	public static void main(String[] args) {
		launch(args);
	}
}
