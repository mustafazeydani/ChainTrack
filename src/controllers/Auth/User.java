package controllers.Auth;

public class User {
	private String uuid;
	private String username;

    public User(String uuid, String username) {
        this.uuid = uuid;
    	this.username = username;
    }
    
	public String getId() {
		return uuid;
	}

    public String getUsername() {
        return username;
    }
}
