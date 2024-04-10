package controllers.Home;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;

public class CryptocurrencyCardController {

    @FXML
    private ImageView image;
    
    @FXML
    private Label name;
    
    @FXML
    private Label price;

    @FXML
    private Label marketCap;

    @FXML
    private Label allTimeHigh;
    
	public void setData(String imageUrl, String name, String price, String marketCap, String allTimeHigh) {
		this.image.setImage(new javafx.scene.image.Image(imageUrl));
		this.name.setText(name);
		this.price.setText("$" + price);
		this.marketCap.setText("$" + marketCap);
		this.allTimeHigh.setText("$" + allTimeHigh);
	}
}
