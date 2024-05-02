package controllers.Portfolio;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.util.ArrayList;
import controllers.DatabaseManager;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Map;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.json.JSONArray;
import org.json.JSONObject;

import controllers.Network;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;

import javafx.scene.layout.VBox;

public class PortfolioController {
  @FXML
  private VBox Portfolio;
  @FXML
  private ComboBox < String > walletsComboBox;
  @FXML
  private ComboBox < Network > networksComboBox;

  @FXML
  private LineChart < String, Float > performanceChart;

  private JSONArray incomingTransactions = new JSONArray();
  private JSONArray outgoingTransactions = new JSONArray();

  // Combine incoming and outgoing transactions into a list
  List < JSONObject > transactions = new ArrayList < > ();

  private String apiKey = "4xsJgISQeJoIjLMkQfwSRc0sJQT-f0iC";

  public void setWalletsComboBox(ComboBox < String > walletsComboBox) {
    this.walletsComboBox = walletsComboBox;

    fetchTransactions(walletsComboBox.getValue());

    walletsComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
      fetchTransactions(newValue);
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
    handleLineChart();
  }

  //Method to escape special characters in the string
  private String escapeString(String value) {
    // Replace ' with '' to escape single quotes
    return value.replaceAll("'", "''");
  }

  private void combineTransactions() {
    // Add incoming transactions to the list
    for (int i = 0; i < incomingTransactions.length(); i++) {
      transactions.add(incomingTransactions.getJSONObject(i));
    }

    // Add outgoing transactions to the list
    for (int i = 0; i < outgoingTransactions.length(); i++) {
      transactions.add(outgoingTransactions.getJSONObject(i));
    }
  }

  void handleLineChart() {
    performanceChart.getData().clear();
    XYChart.Series < String, Float > series = new XYChart.Series < > ();
    for (int i = 0; i < transactions.size(); i++) {
      JSONObject transaction = transactions.get(i);
      Float value = transaction.getBigDecimal("value").floatValue();
      // convert i to string
      series.getData().add(new XYChart.Data < > (Integer.toString(i + 1), value));
      series.setName("Last 10 Transactions Performance " + transaction.getString("asset"));
    }
    performanceChart.getData().add(series);
  }

  public void setNetworksComboBox(ComboBox < Network > networksComboBox) {
    this.networksComboBox = networksComboBox;

    networksComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
      fetchTransactions(walletsComboBox.getValue());
    });
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
      transactions.add(incomingTransactions.getJSONObject(i));
    }
    for (int i = 0; i < outgoingTransactions.length(); i++) {
      transactions.add(outgoingTransactions.getJSONObject(i));
    }

    System.out.println(transactions);
    // Sort the list by timestamp in descending order
    transactions = transactions.stream()
      .sorted((a, b) -> {
		String timestampA = a.getJSONObject("metadata").getString("blockTimestamp");
		String timestampB = b.getJSONObject("metadata").getString("blockTimestamp");
    	long timeA = Instant.parse(timestampA).toEpochMilli();
        long timeB = Instant.parse(timestampB).toEpochMilli();
        return Long.compare(timeB, timeA);
      })
      .collect(Collectors.toList());

    // Get only last 10 transactions
    transactions = transactions.stream().limit(10).collect(Collectors.toList());
  }

  void insertTransactionsToDB() {
    // Add transactions to database
    for (JSONObject transaction: transactions) {
      String timestampString = transaction.getJSONObject("metadata").getString("blockTimestamp");
      ZonedDateTime zonedDateTime = ZonedDateTime.parse(timestampString);
      LocalDateTime localDateTime = zonedDateTime.toLocalDateTime();
      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
      String formattedTimestamp = localDateTime.format(formatter);

      String query = "INSERT INTO transactions (`uuid`, `from`, `to`, `value`, `asset`, `block_timestamp`, `network`) VALUES ('" +
	        java.util.UUID.randomUUID().toString() + "', '" +
	        transaction.getString("from") + "', '" + transaction.getString("to") + "', '" +
	        transaction.getBigDecimal("value") + "', '" + transaction.getString("asset") + "', '" +
	        formattedTimestamp + "', '" + networksComboBox.getValue().getSubdomain() + "')";
      DatabaseManager.updateQuery(query);
    }
  }
}