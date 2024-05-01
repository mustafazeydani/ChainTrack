package controllers;

public class Network {
	String name;
	String url;
	String subdomain;
	
	public Network(String name, String subdomain) {
		super();
		this.name = name;
		this.subdomain = subdomain;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getSubdomain() {
		return subdomain;
	}
	public void setSubdomain(String subdomain) {
		this.subdomain = subdomain;
	}
}
