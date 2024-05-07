package controllers.MyWallets;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import controllers.DatabaseManager;
import controllers.Network;
import controllers.Auth.User;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.math.BigInteger;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

import javafx.fxml.FXML;
import javafx.event.ActionEvent;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.VBox;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.application.Platform;
import javafx.scene.layout.GridPane;
import javafx.geometry.Insets;

public class MyWalletsController {
	private User loggedInUser;
	
    @FXML
    private VBox MyWallets;
    
    @FXML
    private ComboBox<String> walletsComboBox;
    
    @FXML
    private ComboBox<Network> networksComboBox;
    
    @FXML
    private GridPane tokensGrid;
    
    private String apiKey = "4xsJgISQeJoIjLMkQfwSRc0sJQT-f0iC";
    
	private ObservableList<String> walletsList = FXCollections.observableArrayList();
    private ObservableList<Token> walletTokensList = FXCollections.observableArrayList();
    
	public void setLoggedInUser(User loggedInUser) {
		this.loggedInUser = loggedInUser;
	}
    
    public void setWalletsComboBox(ComboBox<String> walletsComboBox) {
        this.walletsComboBox = walletsComboBox;
        
		fetchWalletTokens(walletsComboBox.getValue(), networksComboBox.getValue());
		
		walletsComboBox.getSelectionModel().selectedItemProperty().addListener((options, oldValue, newValue) -> {
			walletTokensList.clear();
			fetchWalletTokens(newValue, networksComboBox.getValue());
		});
    }

	public void setNetworksComboBox(ComboBox<Network> networksComboBox) {
        this.networksComboBox = networksComboBox;
        
        this.networksComboBox.getSelectionModel().selectedItemProperty().addListener((options, oldValue, newValue) -> {
        	walletTokensList.clear();
        	fetchWalletTokens(walletsComboBox.getValue(), newValue);
        });
    }
	
	public void fetchWalletTokens(String walletAddress, Network network) {
        String jsonBody = "{\"id\":1,\"jsonrpc\":\"2.0\",\"method\":\"alchemy_getTokenBalances\",\"params\":[\""+ walletAddress + "\"]}";
       
        // Define the URL
        String url = "https://"+ network.getSubdomain() +".g.alchemy.com/v2/" + apiKey;

        // Create HttpClient
        HttpClient httpClient = HttpClient.newHttpClient();

        // Create HttpRequest
		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(url)).header("Content-Type", "application/json")
				.POST(HttpRequest.BodyPublishers.ofString(jsonBody))
				.build();
		
		// Send the request	
		httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString()).thenApply(HttpResponse::body)
				.thenApply(response -> new JSONObject(response)).thenAccept(walletTokensResponse -> handleWalletTokensResponse(walletTokensResponse, network)).join();
	}
	
	public void handleWalletTokensResponse(JSONObject jsonResponse, Network network) {
	    // Check if the response contains the 'result' field
	    if (jsonResponse.has("result")) {
	        JSONObject result = jsonResponse.getJSONObject("result");
	        JSONArray tokenBalances = result.getJSONArray("tokenBalances");
	        
	        for (int i = 0; i < tokenBalances.length(); i++) {
	            JSONObject tokenBalance = tokenBalances.getJSONObject(i);    
	            String tokenContractAddress = tokenBalance.getString("contractAddress");
	            String balanceString = tokenBalance.getString("tokenBalance");
	            BigInteger balance = new BigInteger(balanceString.substring(2), 16);
	            String url = "https://"+ network.getSubdomain() +".g.alchemy.com/v2/" + apiKey;
	            
	            HttpClient httpClient = HttpClient.newHttpClient();
	            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url))
	                    .header("Content-Type", "application/json")
	                    .POST(HttpRequest.BodyPublishers.ofString(
	                            "{\"id\":1,\"jsonrpc\":\"2.0\",\"method\":\"alchemy_getTokenMetadata\",\"params\":[\""
	                                    + tokenContractAddress + "\"]}"))
	                    .build();
	            
	            httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString()).thenApply(HttpResponse::body)
	                .thenApply(response -> new JSONObject(response))
	                .thenAccept(metadataResponse -> handleTokenMetadataResponse(metadataResponse, balance, tokenContractAddress)).join();
	        }
	        
			Platform.runLater(() -> {
				int column = 0;
		        int row = 1;
				tokensGrid.getChildren().clear();
				for (int i = 0; i < walletTokensList.size(); i++) {
					Token token = walletTokensList.get(i);
					FXMLLoader loader = new FXMLLoader(getClass().getResource("../../FXML/MyWallets/TokenCard.fxml"));
					try {
						VBox tokenCard = loader.load();
						TokenCardController controller = loader.getController();
						controller.setData(token.getLogoURI(), token.getName(), token.getBalance(), token.getAddress());
						
						if (column == 3) {
							column = 0;
							row++;
						}
						tokensGrid.add(tokenCard, column++, row);
						GridPane.setMargin(tokenCard, new Insets(10));
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			});
	    } else {
	        System.out.println("Error: No result found in response.");
	    }
	}

	public void handleTokenMetadataResponse(JSONObject jsonResponse, BigInteger balance, String address) {
	    if (jsonResponse.has("result")) {
	        JSONObject result = jsonResponse.getJSONObject("result");
	        String name = result.getString("name");
	        String logoURI = result.isNull("logo") ? null : result.getString("logo");
	        String symbol = result.getString("symbol");
	        int decimals = result.getInt("decimals");
	        
	        // check for invalid data
			if (name.equals("") || symbol.equals("") || logoURI == null) return;

			// check if address is already in the list
			for (Token token : walletTokensList) {
				if (token.getAddress().equals(address)) {
					token.setBalance(token.getBalance().add(balance));
					return;
				}
			}
			Token token = new Token(name, balance, address, symbol, decimals, logoURI);
	        walletTokensList.add(token);
	    } else {
	        System.out.println("Error: No result found in response.");
	    }
	}
	
	@FXML
	void handleAddWallet(ActionEvent event) {
	    try {
	        FXMLLoader loader = new FXMLLoader(getClass().getResource("../../FXML/MyWallets/AddWalletPopup.fxml"));
	        Parent root = loader.load();
	        AddWalletPopupController controller = loader.getController();
	        controller.setLoggedInUser(loggedInUser);
	        controller.setWalletsComboBox(walletsComboBox);
	        Stage stage = new Stage();
	        stage.initModality(Modality.APPLICATION_MODAL);
	        stage.setTitle("Add Wallet");
	        stage.setScene(new Scene(root));
	        stage.show();
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	}
}
