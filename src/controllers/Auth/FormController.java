package controllers.Auth;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class FormController {
	private Stage primaryStage;
	
    @FXML
    private VBox formContainer;
    
	private void InitializeFormController() {
        // Load the form into the container
        FXMLLoader loader = new FXMLLoader(getClass().getResource("../../FXML/Auth/Login.fxml"));
        try {
            formContainer.getChildren().add(loader.load());
            LoginController controller = loader.getController(); 
            controller.setFormController(this); 
            controller.setPrimaryStage(primaryStage);
        } catch (Exception e) { 
            e.printStackTrace();
        }
	}
    
	public void setPrimaryStage(Stage primaryStage) {
		this.primaryStage = primaryStage;
		
		InitializeFormController();
	}
    
    public void SwitchForm(String form) {
        try {
            // Load the FXML file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("../../FXML/Auth/" + form + ".fxml"));
            // Load the form into the container
            AnchorPane newForm = loader.load();

            // Pass the FormController reference to the loaded controller
            if (form.equals("Login")) {
                LoginController controller = loader.getController();
                controller.setFormController(this);
                controller.setPrimaryStage(primaryStage);
            } else if (form.equals("Signup")) {
                SignupController controller = loader.getController();
                controller.setFormController(this);
                controller.setPrimaryStage(primaryStage);
            }

            // Clear existing children and add the new form
            formContainer.getChildren().clear();
            formContainer.getChildren().add(newForm);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
