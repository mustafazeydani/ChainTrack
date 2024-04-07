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
import javafx.scene.layout.GridPane;

public class Home implements Initializable {

    @FXML
    private HBox NFTCardsLayout;
    
    @FXML
    private GridPane cryptocurrenciesGrid;
    
    private int cryptocurrencyCount = 10;

    @Override
    public void initialize(URL arg0, ResourceBundle arg1) {
		fetchNFTs();
		fetchCryptocurrencies();
    }

    private void fetchNFTs() {
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.coingecko.com/api/v3/nfts/list?order=h24_volume_usd_desc&per_page=5&page=1"))
                .header("x-cg-demo-api-key", "CG-z8sb92fYPDJyKVXTKxD91oWF")
                .build();

        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenApply(response -> new JSONArray(response))
                .thenAccept(this::handleNFTsResponse)
                .join();
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
            	formatDouble(nft.getJSONObject("volume_24h").getDouble("usd")), 
            	formatDouble(nft.getJSONObject("floor_price").getDouble("usd")), 
            	formatDouble(nft.getInt("number_of_unique_addresses")), 
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
    
	private void fetchCryptocurrencies() {
		HttpClient httpClient = HttpClient.newHttpClient();
		HttpRequest request = HttpRequest.newBuilder().uri(URI.create(
				"https://api.coingecko.com/api/v3/coins/markets?vs_currency=usd&order=market_cap_desc"))
				.header("x-cg-demo-api-key", "CG-z8sb92fYPDJyKVXTKxD91oWF").build();

		httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString()).thenApply(HttpResponse::body)
				.thenApply(response -> new JSONArray(response)).thenAccept(this::handleCryptocurrenciesResponse).join();
	}
	
	private void handleCryptocurrenciesResponse(JSONArray cryptocurrencies) {
		int column = 0;
	    int row = 1;
	    for (int i = 0; i < cryptocurrencyCount; i++) {
	        JSONObject cryptocurrency = cryptocurrencies.getJSONObject(i);
	        try {
	            FXMLLoader loader = new FXMLLoader(getClass().getResource("../FXML/CryptocurrencyCard.fxml"));
	            HBox box = loader.load();
	            CryptocurrencyCardController controller = loader.getController();
	            controller.setData(
	                    cryptocurrency.getString("image"),
	                    cryptocurrency.getString("name"),
	                    formatDouble(cryptocurrency.getDouble("current_price")),
	                    formatDouble(cryptocurrency.getDouble("market_cap")),
	                    formatDouble(cryptocurrency.getDouble("ath"))
	            );
	            if (column == 2) {
	                column = 0;
	                row++;
	            }
	            
	            cryptocurrenciesGrid.add(box, column++, row);
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	    }
	}
	
    
	private static String formatDouble(double number) {
	    DecimalFormat formatter;
	    if (number < 1) {
	        formatter = new DecimalFormat("#,###.##");
	    } else {
	        formatter = new DecimalFormat("#,###");
	    }
	    return formatter.format(number);
	}
}
