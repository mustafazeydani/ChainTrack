package controllers;

import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.DecimalFormat;
import java.util.ResourceBundle;

import org.json.JSONArray;
import org.json.JSONObject;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.layout.HBox;

public class Home implements Initializable {

    @FXML
    private HBox NFTCardsLayout;

    @Override
    public void initialize(URL arg0, ResourceBundle arg1) {
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.coingecko.com/api/v3/nfts/list?order=h24_volume_usd_desc&per_page=3&page=1"))
                .header("x-cg-demo-api-key", "CG-z8sb92fYPDJyKVXTKxD91oWF")
                .build();

        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenApply(this::fetchNFTs)
                .thenAccept(this::handleNFTsResponse)
                .join();
    }

    private JSONArray fetchNFTs(String responseBody) {
        return new JSONArray(responseBody);
    }

    private void handleNFTsResponse(JSONArray nfts) {
        for (int i = 0; i < nfts.length(); i++) {
            JSONObject nft = nfts.getJSONObject(i);
            String nftId = nft.getString("id");
            HttpClient httpClient = HttpClient.newHttpClient();
            HttpRequest nftRequest = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.coingecko.com/api/v3/nfts/" + nftId))
                    .header("x-cg-demo-api-key", "CG-z8sb92fYPDJyKVXTKxD91oWF")
                    .build();
            httpClient.sendAsync(nftRequest, HttpResponse.BodyHandlers.ofString())
                    .thenApply(HttpResponse::body)
                    .thenAccept(this::handleNFTResponse);
        }
    }

    private void handleNFTResponse(String responseBody) {
        JSONObject nft = new JSONObject(responseBody);
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("../FXML/NFTCard.fxml"));
            HBox box = loader.load();
            NFTCardController controller = loader.getController();
            JSONArray externalLinks = nft.getJSONArray("explorers");
            String link = externalLinks != null && externalLinks.length() > 0 ? externalLinks.getJSONObject(0).optString("link", "") : "";
            controller.setData(
            	nft.getString("name"), 
            	formatInteger(nft.getJSONObject("volume_24h").getInt("usd")), 
            	formatInteger(nft.getJSONObject("floor_price").getInt("usd")), 
            	formatInteger(nft.getInt("number_of_unique_addresses")), 
            	nft.getJSONObject("image").getString("small"),
            	link
            );
            // Update UI on the JavaFX Application Thread
            Platform.runLater(() -> {
                NFTCardsLayout.getChildren().add(box);
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private static String formatInteger(int number) {
        DecimalFormat formatter = new DecimalFormat("#,###");
        return formatter.format(number);
    }
}
