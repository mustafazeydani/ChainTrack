package controllers;

import javafx.fxml.FXML;

import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

public class NFTCardController {
    @FXML
    private HBox NFTCardBox;

    @FXML
    private Label NFTCardVolume;

    @FXML
    private Label NFTCardMinPrice;

    @FXML
    private Label NFTCardMaxPrice;

    @FXML
    private Label NFTCardTitle;
	
	public HBox getNFTCardBox() {
		return NFTCardBox;
	}
	
	public void setData(String title, String volume, String minPrice, String maxPrice) {
		NFTCardTitle.setText(title);
		NFTCardVolume.setText(volume);
		NFTCardMinPrice.setText(minPrice);
//		NFTCardMaxPrice.setText(maxPrice);
	}

}
