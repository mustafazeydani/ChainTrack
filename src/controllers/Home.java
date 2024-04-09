package controllers;

import java.io.IOException;

import java.net.URI;

import java.util.List;
import java.util.Map;
import javafx.event.ActionEvent;
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
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Pagination;
import javafx.scene.layout.GridPane;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;

public class Home implements Initializable {

	/* Nav Menu */
	@FXML
    private Button HomeNavBtn;

    @FXML
    private Button MyWalletsNavBtn;

    @FXML
    private Button PortfolioNavBtn;

    @FXML
    private Button TransactionsNavBtn;
    
    @FXML
    private VBox Home;
    
    @FXML
    void ShowHome(ActionEvent event) {
    	if(!Home.isVisible()) {
    		Home.setVisible(true);
    		HomeNavBtn.getStyleClass().add("nav-menu-button-focused");
    		MyWallets.setVisible(false);
    		MyWalletsNavBtn.getStyleClass().remove("nav-menu-button-focused");
    	}
    }



    @FXML
    void ShowPortfolio(ActionEvent event) {

    }

    @FXML
    void ShowTransactions(ActionEvent event) {

    }
    
	
	/* Home */
    @FXML
    private HBox NFTCardsLayout;
    
    @FXML
    private ScrollPane cryptoCurrenciesLayout;
    
    @FXML
    private TextField searchInput;
    
    @FXML
    private Pagination pagination;
    
    private int pageLimit = 10;
    private JSONArray cryptocurrencies;

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
                HBox.setMargin(box, new javafx.geometry.Insets(10, 10, 10, 10));
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
	    this.cryptocurrencies = cryptocurrencies;
	    int pageCount = (int) Math.ceil(cryptocurrencies.length() / pageLimit);
	    pagination.setPageCount(pageCount);
	    pagination.setPageFactory(pageIndex -> {
	        ScrollPane page = new ScrollPane();
	        Platform.runLater(() -> {
	            page.setContent(createPage(pageIndex, cryptocurrencies));
	        });
	        return page;
	    });
	}
	
    @FXML
    void onSearch(KeyEvent event) {
    	filterCryptocurrencies();
    }
    
    private void filterCryptocurrencies() {
        String query = searchInput.getText().toLowerCase();
        JSONArray filteredCryptocurrencies = new JSONArray();
        for (int i = 0; i < cryptocurrencies.length(); i++) {
            JSONObject cryptocurrency = cryptocurrencies.getJSONObject(i);
            if (cryptocurrency.getString("name").toLowerCase().startsWith(query)) {
                filteredCryptocurrencies.put(cryptocurrency);
            }
        }
        int pageCount = (int) Math.ceil((double) filteredCryptocurrencies.length() / pageLimit); // Correcting the calculation
        pagination.setPageCount(pageCount);
        pagination.setPageFactory(pageIndex -> {
            ScrollPane page = new ScrollPane();
            page.setContent(createPage(pageIndex, filteredCryptocurrencies));
            return page;
        });
    }

	private ScrollPane createPage(int pageIndex, JSONArray cryptocurrencies) {
	    int startIndex = pageIndex * pageLimit;
	    int endIndex = Math.min(startIndex + pageLimit, cryptocurrencies.length());

	    ScrollPane cryptoCurrenciesLayout = new ScrollPane();
	    GridPane cryptoCurrenciesGrid = new GridPane();
	    int column = 0;
	    int row = 1;
	    for (int i = startIndex; i < endIndex; i++) {
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
	            cryptoCurrenciesGrid.add(box, column++, row);
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	    }
	    cryptoCurrenciesLayout.setPrefWidth(1000);
	    cryptoCurrenciesLayout.setContent(cryptoCurrenciesGrid);
	    return cryptoCurrenciesLayout;
	}
	
	
	/* My Wallets */
    @FXML
    private VBox MyWallets;
    
    @FXML
    private Label noWalletsLabel;
    
    @FXML
    private ComboBox<String> walletsComboBox;
	
    @FXML
    void ShowMyWallets(ActionEvent event) {
    	fetchWallets();
		if (!MyWallets.isVisible()) {
			MyWallets.setVisible(true);
			MyWalletsNavBtn.getStyleClass().add("nav-menu-button-focused");
			Home.setVisible(false);
			HomeNavBtn.getStyleClass().remove("nav-menu-button-focused");
		}
    }
    
    void fetchWallets() {
        String query = "SELECT * FROM wallets";
        List<Map<String, Object>> resultList = DatabaseManager.getQuery(query);
        try {
            if (resultList.size() == 0) {
                noWalletsLabel.setVisible(true);
                walletsComboBox.setDisable(true);
                return;
            }
            noWalletsLabel.setVisible(false);
            walletsComboBox.setDisable(false);
            walletsComboBox.getItems().clear();
			for (Map<String, Object> row : resultList) {
				walletsComboBox.getItems().add((String) row.get("address"));
			}
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
	@FXML
	void handleAddWallet(ActionEvent event) {
	    try {
	        FXMLLoader loader = new FXMLLoader(getClass().getResource("../FXML/AddWalletPopup.fxml"));
	        Parent root = loader.load();
	        AddWalletPopupController controller = loader.getController();
	        controller.setWalletsComboBox(walletsComboBox);
	        controller.setNoWalletsLabel(noWalletsLabel);
	        Stage stage = new Stage();
	        stage.initModality(Modality.APPLICATION_MODAL);
	        stage.setTitle("Add Wallet");
	        stage.setScene(new Scene(root));
	        stage.show();
	    } catch (IOException e) {
	        e.printStackTrace();
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
