package pt.unl.fct.di.apdc.firstwebapp.util;

public class UpdateAttributesData {
    public AuthToken token;
    public String targetUsername;
    public UpdateUserAttributes data;

    public UpdateAttributesData() {}
    
	public UpdateAttributesData(AuthToken token, String targetUsername, UpdateUserAttributes data) {
		this.token = token;
		this.targetUsername = targetUsername;
		this.data = data;
	}

	public boolean isUsernameValid() {
		if (targetUsername == null || targetUsername.equals("")) return false;
		return true;
	}
	
}
