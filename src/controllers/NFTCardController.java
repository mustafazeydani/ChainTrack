package controllers;

import javafx.fxml.FXML;
import java.awt.Desktop;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;

import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

public class NFTCardController {
    @FXML
    private HBox NFTCardBox;

    @FXML
    private Label NFTCardVolume;

    @FXML
    private Label NFTCardFloorPrice;

    @FXML
    private Label NFTCardHolders;

    @FXML
    private Label NFTCardTitle;
    
    @FXML
    private ImageView NFTCardImage;
    
    @FXML
    private FontAwesomeIcon NFTCardExternalLink;
	
	public HBox getNFTCardBox() {
		return NFTCardBox;
	}
	
	public void setData(String title, String volume, String floorPrice, String holders, String imageUrl, String externalLink) {
		NFTCardTitle.setText(title);
		NFTCardVolume.setText(volume.toString() + " USD");
		NFTCardFloorPrice.setText(floorPrice.toString() + " USD");
		NFTCardHolders.setText(holders.toString());
		NFTCardImage.setImage(new Image(imageUrl));
		if (externalLink != "") {
			NFTCardExternalLink.setOnMouseClicked(e -> {
				try {
					Desktop.getDesktop().browse(new java.net.URI(externalLink));
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			});
		} else {
			NFTCardExternalLink.setVisible(false);
		}
	}

}
