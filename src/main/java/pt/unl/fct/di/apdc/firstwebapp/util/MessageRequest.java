package pt.unl.fct.di.apdc.firstwebapp.util;

public class MessageRequest {
	
	public AuthToken token;
	public String nameOfUser;
	public String content;
	
	public MessageRequest() {
		
	}
	
	public MessageRequest(AuthToken token, String nameOfUser, String content) {
		this.token = token;
		this.nameOfUser = nameOfUser;
		this.content = content;
	}

}
