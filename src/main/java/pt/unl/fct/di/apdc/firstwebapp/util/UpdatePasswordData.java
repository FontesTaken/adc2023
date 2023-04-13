package pt.unl.fct.di.apdc.firstwebapp.util;

public class UpdatePasswordData {
    public AuthToken token;
    public UpdatePasswordAttribute data;
    
    public UpdatePasswordData() {
        
    }
    
    public UpdatePasswordData(AuthToken token, UpdatePasswordAttribute updatePasswordAttribute) {
        this.token = token;
        this.data = updatePasswordAttribute;
    }
}
