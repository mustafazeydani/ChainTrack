package controllers.Transactions;

import java.time.LocalDateTime;

public class Transaction {
	String from;
	String to;
	Float value;
	String asset;
	LocalDateTime block_timestamp;
	String network;
	
	public Transaction(String from, String to, Float value, String asset, LocalDateTime block_timestamp, String network) {
		this.from = from;
		this.to = to;
		this.value = value;
		this.asset = asset;
		this.block_timestamp = block_timestamp;
		this.network = network;
	}
	
	public String getFrom() {
		return from;
	}
	public void setFrom(String from) {
		this.from = from;
	}
	public String getTo() {
		return to;
	}
	public void setTo(String to) {
		this.to = to;
	}
	public Float getValue() {
		return value;
	}
	public void setValue(Float value) {
		this.value = value;
	}
	public String getAsset() {
		return asset;
	}
	public void setAsset(String asset) {
		this.asset = asset;
	}
	public LocalDateTime getBlock_timestamp() {
		return block_timestamp;
	}
	public void setBlock_timestamp(LocalDateTime block_timestamp) {
		this.block_timestamp = block_timestamp;
	}
	public String getNetwork() {
		return network;
	}
	public void setNetwork(String network) {
		this.network = network;
	}
}
