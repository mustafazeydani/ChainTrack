package controllers.Transactions;

import controllers.Auth.User;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.net.URI;
import java.util.ResourceBundle;

import org.json.JSONArray;
import org.json.JSONObject;

import controllers.DatabaseManager;
import controllers.Network;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.scene.control.TableColumn;
import javafx.fxml.Initializable;

public class TransactionsController implements Initializable {
  private User loggedInUser;
  
  @FXML
  private VBox Portfolio;

  @FXML
  private ComboBox < String > walletsComboBox;

  @FXML
  private ComboBox < Network > networksComboBox;

  @FXML
  private TableView < Transaction > transactionsTable;

  @FXML
  private TableColumn < Transaction, String > fromCol;

  @FXML
  private TableColumn < Transaction, String > toCol;

  @FXML
  private TableColumn < Transaction, String > valueCol;

  @FXML
  private TableColumn < Transaction, String > assetCol;

  @FXML
  private TableColumn < Transaction, LocalDateTime > blockTimestampCol;

  @FXML
  private TableColumn < Transaction, String > networkCol;

  private JSONArray incomingTransactions = new JSONArray();
  private JSONArray outgoingTransactions = new JSONArray();

  // Combine incoming and outgoing transactions into a list
  ObservableList < Transaction > transactions = FXCollections.observableArrayList();

  private String apiKey = "4xsJgISQeJoIjLMkQfwSRc0sJQT-f0iC";
  
  @Override
  public void initialize(URL arg0, ResourceBundle arg1) {
    // TODO Auto-generated method stub
    setCellValueFactories();
    transactionsTable.setItems(transactions);
  }
  
  public void setLoggedInUser(User loggedInUser) {
	this.loggedInUser = loggedInUser;
  }
  
  public void setCellValueFactories() {
    fromCol.setCellValueFactory(new PropertyValueFactory < Transaction, String > ("from"));
    toCol.setCellValueFactory(new PropertyValueFactory < Transaction, String > ("to"));
    valueCol.setCellValueFactory(new PropertyValueFactory < Transaction, String > ("value"));
    assetCol.setCellValueFactory(new PropertyValueFactory < Transaction, String > ("asset"));
    blockTimestampCol.setCellValueFactory(new PropertyValueFactory < Transaction, LocalDateTime > ("block_timestamp"));
    networkCol.setCellValueFactory(new PropertyValueFactory < Transaction, String > ("network"));
  }

  public void setWalletsComboBox(ComboBox < String > walletsComboBox) {
    this.walletsComboBox = walletsComboBox;
    fetchTransactions(walletsComboBox.getValue());

    walletsComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
      fetchTransactions(newValue);
    });
  }

  public void setNetworksComboBox(ComboBox < Network > networksComboBox) {
    this.networksComboBox = networksComboBox;

    networksComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
      fetchTransactions(walletsComboBox.getValue());
    });
  }

  void fetchTransactions(String address) {
    transactions.clear();
    outgoingTransactions = new JSONArray();
    incomingTransactions = new JSONArray();
    String query = "SELECT * FROM transactions WHERE (`from` = '" + escapeString(address.toLowerCase()) + "' OR `to` = '" + escapeString(address.toLowerCase()) + "') AND network = '" + escapeString(networksComboBox.getValue().getSubdomain()) + "' ORDER BY block_timestamp DESC LIMIT 10";
    List < Map < String, Object >> resultList = DatabaseManager.getQuery(query);
    if (resultList.size() == 0) {
      fetchIncomingTransactions(address);
      fetchOutgoingTransactions(address);
      proccessTransactions();
      insertTransactionsToDB();
    } else {
      // Process transactions
      for (Map < String, Object > row: resultList) {
        JSONObject transaction = new JSONObject();
        transaction.put("from", row.get("from"));
        transaction.put("to", row.get("to"));
        transaction.put("value", row.get("value"));
        transaction.put("metadata", new JSONObject().put("blockTimestamp", row.get("block_timestamp")));
        transaction.put("asset", row.get("asset"));
        if (row.get("from").equals(address)) {
          outgoingTransactions.put(transaction);
        } else {
          incomingTransactions.put(transaction);
        }
      }
      combineTransactions();
    }
  }

  //Method to escape special characters in the string
  private String escapeString(String value) {
    // Replace ' with '' to escape single quotes
    return value.replaceAll("'", "''");
  }

  void fetchIncomingTransactions(String address) {
    try {
      HttpClient client = HttpClient.newHttpClient();

      JSONObject paramsObject = new JSONObject();
      paramsObject.put("fromBlock", "0x0");
      paramsObject.put("toBlock", "latest");
      paramsObject.put("toAddress", address);
      paramsObject.put("withMetadata", true);
      paramsObject.put("excludeZeroValue", true);
      paramsObject.put("maxCount", "0x14");
      paramsObject.put("category", new String[] {
        "external"
      });

      JSONObject requestBodyObject = new JSONObject();
      requestBodyObject.put("id", 1);
      requestBodyObject.put("jsonrpc", "2.0");
      requestBodyObject.put("method", "alchemy_getAssetTransfers");
      requestBodyObject.put("params", new JSONObject[] {
        paramsObject
      });

      String requestBody = requestBodyObject.toString();

      HttpRequest request = HttpRequest.newBuilder()
        .uri(new URI("https://" + networksComboBox.getValue().getSubdomain() + ".g.alchemy.com/v2/" + apiKey))
        .POST(HttpRequest.BodyPublishers.ofString(requestBody))
        .header("accept", "application/json")
        .header("content-type", "application/json")
        .build();

      // print the response body
      client.sendAsync(request, java.net.http.HttpResponse.BodyHandlers.ofString())
        .thenApply(java.net.http.HttpResponse::body).thenAccept(response -> handleTransactions(response, address)).join();
    } catch (URISyntaxException e) {
      e.printStackTrace();
    }
  }

  void fetchOutgoingTransactions(String address) {
    try {
      HttpClient client = HttpClient.newHttpClient();

      JSONObject paramsObject = new JSONObject();
      paramsObject.put("fromBlock", "0x0");
      paramsObject.put("toBlock", "latest");
      paramsObject.put("fromAddress", address);
      paramsObject.put("withMetadata", true);
      paramsObject.put("excludeZeroValue", true);
      paramsObject.put("maxCount", "0x14");
      paramsObject.put("category", new String[] {
        "external"
      });

      JSONObject requestBodyObject = new JSONObject();
      requestBodyObject.put("id", 1);
      requestBodyObject.put("jsonrpc", "2.0");
      requestBodyObject.put("method", "alchemy_getAssetTransfers");
      requestBodyObject.put("params", new JSONObject[] {
        paramsObject
      });

      String requestBody = requestBodyObject.toString();

      HttpRequest request = HttpRequest.newBuilder()
        .uri(new URI("https://" + networksComboBox.getValue().getSubdomain() + ".g.alchemy.com/v2/" + apiKey))
        .POST(HttpRequest.BodyPublishers.ofString(requestBody))
        .header("accept", "application/json")
        .header("content-type", "application/json")
        .build();

      client.sendAsync(request, java.net.http.HttpResponse.BodyHandlers.ofString())
        .thenApply(java.net.http.HttpResponse::body).thenAccept(response -> handleTransactions(response, address)).join();
    } catch (URISyntaxException e) {
      e.printStackTrace();
    }
  }

  void handleTransactions(String response, String address) {
    // get result from response
    JSONArray responseArray = new JSONObject(response).getJSONObject("result").getJSONArray("transfers");
    for (int i = 0; i < responseArray.length(); i++) {
      JSONObject transaction = responseArray.getJSONObject(i);
      if (transaction.getString("to").toLowerCase().equals(walletsComboBox.getValue().toLowerCase())) {
        incomingTransactions.put(transaction);
      } else {
        outgoingTransactions.put(transaction);
      }
    }
  }

  void proccessTransactions() {
    for (int i = 0; i < incomingTransactions.length(); i++) {
      JSONObject transaction = incomingTransactions.getJSONObject(i);
      transactions.add(new Transaction(transaction.getString("from"), transaction.getString("to"), transaction.getBigDecimal("value").floatValue(), transaction.getString("asset"), ZonedDateTime.parse(transaction.getJSONObject("metadata").getString("blockTimestamp")).toLocalDateTime(), networksComboBox.getValue().getSubdomain()));
    }
    for (int i = 0; i < outgoingTransactions.length(); i++) {
      JSONObject transaction = outgoingTransactions.getJSONObject(i);
      transactions.add(new Transaction(transaction.getString("from"), transaction.getString("to"),
        transaction.getBigDecimal("value").floatValue(), transaction.getString("asset"), ZonedDateTime
        .parse(transaction.getJSONObject("metadata").getString("blockTimestamp")).toLocalDateTime(),
        networksComboBox.getValue().getSubdomain()));
    }
  }

  void insertTransactionsToDB() {
    // Add transactions to database
    for (int i = 0; i < transactions.size(); i++) {
      Transaction transaction = transactions.get(i);
      String uuid = java.util.UUID.randomUUID().toString();
      String from = transaction.getFrom();
      String to = transaction.getTo();
      Float value = transaction.getValue();
      String asset = transaction.getAsset();
      LocalDateTime blockTimestamp = transaction.getBlock_timestamp();
      String network = transaction.getNetwork();
      String query = "INSERT INTO transactions (uuid, `from`, `to`, value, asset, block_timestamp, network) VALUES ('" + uuid + "', '" + from + "', '" + to + "', " + value + ", '" + asset + "', '" + blockTimestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "', '" + network + "')";
      DatabaseManager.updateQuery(query);
    }
  }

  private void combineTransactions() {
    // Add incoming transactions to the list
    for (int i = 0; i < incomingTransactions.length(); i++) {
      Transaction transaction = new Transaction(incomingTransactions.getJSONObject(i).getString("from"), incomingTransactions.getJSONObject(i).getString("to"), incomingTransactions.getJSONObject(i).getBigDecimal("value").floatValue(), incomingTransactions.getJSONObject(i).getString("asset"),
        (LocalDateTime) incomingTransactions.getJSONObject(i).getJSONObject("metadata").get("blockTimestamp"),
        networksComboBox.getValue().getSubdomain());
      transactions.add(transaction);
    }

    //    // Add outgoing transactions to the list
    for (int i = 0; i < outgoingTransactions.length(); i++) {
      Transaction transaction = new Transaction(outgoingTransactions.getJSONObject(i).getString("from"),
        outgoingTransactions.getJSONObject(i).getString("to"),
        outgoingTransactions.getJSONObject(i).getBigDecimal("value").floatValue(),
        outgoingTransactions.getJSONObject(i).getString("asset"),
        (LocalDateTime) outgoingTransactions.getJSONObject(i).getJSONObject("metadata").get("blockTimestamp"),
        networksComboBox.getValue().getSubdomain());
      transactions.add(transaction);
    }
  }
}