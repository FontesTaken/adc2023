package pt.unl.fct.di.apdc.firstwebapp.util;

public class TokenRequest {
	public String username;
	public String tokenID;
	
	public TokenRequest() {
		
	}
	
	public TokenRequest(String username, String tokenID) {
		this.username = username;
		this.tokenID = tokenID;
	}
}
