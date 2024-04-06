package controllers;

import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.io.IOException;
import java.net.URI;
import java.util.ResourceBundle;
import org.json.JSONArray;
import org.json.JSONObject;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.layout.HBox;

public class Home implements Initializable {
	
    @FXML
    private HBox NFTCardsLayout;
    
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		// TODO Auto-generated method stub
		HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.coingecko.com/api/v3/nfts/list?order=h24_volume_usd_desc&per_page=3&page=1"))
                .header("x-cg-demo-api-key", "CG-z8sb92fYPDJyKVXTKxD91oWF")
                .build();

        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(this::handleNFTSResponse)
                .join();
	}
	
	private void handleNFTSResponse(String responseBody) {
	    JSONArray nfts = new JSONArray(responseBody);

	    for (int i = 0; i < nfts.length(); i++) {
	        JSONObject nft = nfts.getJSONObject(i);

	        try {
	            FXMLLoader loader = new FXMLLoader(getClass().getResource("../FXML/NFTCard.fxml"));
	            HBox box = loader.load();
	            NFTCardController controller = loader.getController();
	            NFTCardsLayout.getChildren().add(box);
	            controller.setData(nft.getString("name"), "20", "100", "200");
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	    }
	}
}
