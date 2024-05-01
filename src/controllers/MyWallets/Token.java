package controllers.MyWallets;

import java.math.BigInteger;

public class Token {
	private String name;
	private BigInteger balance;
	private String address;
	private String symbol;
	private int decimals;
	private String logoURI;
	
	public Token(String name, BigInteger balance2, String address, String symbol, int decimals, String logoURI) {
		this.name = name;
		this.balance = balance2;
		this.address = address;
		this.symbol = symbol;
		this.decimals = decimals;
		this.logoURI = logoURI;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public BigInteger getBalance() {
		return balance;
	}
	public void setBalance(BigInteger balance) {
		this.balance = balance;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public String getSymbol() {
		return symbol;
	}
	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}
	public int getDecimals() {
		return decimals;
	}
	public void setDecimals(int decimals) {
		this.decimals = decimals;
	}
	public String getLogoURI() {
		return logoURI;
	}
	public void setLogoURI(String logoURI) {
		this.logoURI = logoURI;
	}
}
