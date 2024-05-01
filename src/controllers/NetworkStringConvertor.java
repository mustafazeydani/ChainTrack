package controllers;
import javafx.util.StringConverter;

public class NetworkStringConvertor extends StringConverter<Network> {
	@Override
	public String toString(Network network) {
		return network == null ? null : network.getName();
	}

	@Override
	public Network fromString(String string) {
		Network network = null;
		if(string == null) 
			return network;
		
		network = new Network(string, string);
		return network;
	}
}
