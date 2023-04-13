package pt.unl.fct.di.apdc.firstwebapp.util;

public class DeleteRequest {
	public AuthToken token;
	public String usernameToDelete;

	public DeleteRequest() {
	}

	public DeleteRequest(AuthToken token, String usernameToDelete) {
		this.token = token;
		this.usernameToDelete = usernameToDelete;
	}

}
