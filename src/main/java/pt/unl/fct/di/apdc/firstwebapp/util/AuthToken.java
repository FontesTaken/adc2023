package pt.unl.fct.di.apdc.firstwebapp.util;

import java.util.UUID;

import com.google.cloud.datastore.Entity;

public class AuthToken {
	public static final long EXPIRATION_TIME = 1000 * 60 * 60 * 2; // 2h

	public String username;
	public String role;

	public long creationDate;
	public long expirationDate;
	public String tokenID;
	

	public AuthToken() {

	}

	public AuthToken(String username, String role) {
		this.username = username;
		this.role = role;
		this.tokenID = UUID.randomUUID().toString();
		this.creationDate = System.currentTimeMillis();
		this.expirationDate = this.creationDate + AuthToken.EXPIRATION_TIME;
	}
	
	public boolean isMyToken(Entity token) {
		return (username.equals(token.getString("username")) && role.equals(token.getString("role")) && expirationDate == token.getLong("expirationDate"));
	}
}