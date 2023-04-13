package pt.unl.fct.di.apdc.firstwebapp.util;

public class UpdatePasswordAttribute {

    public String oldPassword;
    public String newPassword;
    public String newPasswordConfirmation;

    public UpdatePasswordAttribute() {}

    public UpdatePasswordAttribute(String oldPassword, String newPassword, String newPasswordConfirmation) {
        this.oldPassword = oldPassword;
        this.newPassword = newPassword;
        this.newPasswordConfirmation = newPasswordConfirmation;
    }

    public boolean isInputValid() {
        return oldPassword != null && !oldPassword.isEmpty()
                && newPassword != null && !newPassword.isEmpty()
                && newPasswordConfirmation != null && !newPasswordConfirmation.isEmpty();
    }

    public boolean isNewPasswordValid() {
        return newPassword.equals(newPasswordConfirmation);
    }

}