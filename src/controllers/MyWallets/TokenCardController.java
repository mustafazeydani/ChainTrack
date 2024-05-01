package controllers.MyWallets;

import java.math.BigInteger;

import javafx.fxml.FXML;
import javafx.scene.image.Image;

import javafx.scene.control.Label;

import javafx.scene.image.ImageView;

public class TokenCardController {
	@FXML
	private ImageView tokenImg;
	@FXML
	private Label tokenName;
	@FXML
	private Label tokenBalance;
    @FXML
    private Label tokenContractAddress;
    
	void setData(String tokenImge, String name, BigInteger balance, String contractAddress) {
		tokenImg.setImage(new Image(tokenImge));
		tokenName.setText(name);
		tokenBalance.setText(balance + "");
		tokenContractAddress.setText(contractAddress.substring(0, 6) + "..." + contractAddress.substring(38));
	}

}
