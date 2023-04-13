package pt.unl.fct.di.apdc.firstwebapp.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegisterAcc {

	// Atributos obrigatorios
	public String username;
	public String email;
	public String name;
	public String password;
	public String passwordRepeat;
	// Atributos obrigatorios

	// Atributos opcionais
	public Boolean revealPerfil;
	public Long telefone;
	public Long telemovel;
	public String ocupacao;
	public String localTrabalho;
	public String morada1;
	public String morada2;
	public String pfpURL;
	public Long nif;
	// Atributos opcionais

	public RegisterAcc() {

	}

	public RegisterAcc(String username, String email, String name, String password, String passwordRepeat, boolean revealPerfil, Long telefone,
			Long telemovel, String ocupacao, String localTrabalho,String morada1, String morada2, String pfpURL, Long nif) {
		this.username = username;
		this.email = email;
		this.name = name;
		this.password = password;
		this.passwordRepeat = passwordRepeat;
		this.revealPerfil = revealPerfil;
		this.telefone = telefone;
		this.telemovel = telemovel;
		this.ocupacao = ocupacao;
		this.localTrabalho = localTrabalho;
		this.morada1 = morada1;
		this.morada2 = morada2;
		this.pfpURL = pfpURL;
		this.nif = nif;
	}

	public boolean doesPasswordMatch() {
		return password.equals(passwordRepeat);
	}
	
	//Verifica se os inputs nao sao nulls
	public boolean isInputValid() {
		if (username == null && email == null && name == null && password == null)
			return false;
		return true;
	}

	// Verifica se a password contem pelo menos 1 numero
	public boolean isPasswordValid() {
		return password.matches(".*\\d+.*");
	}
	
	// Verifica se o email e valido
	public boolean isEmailValid() {
		String regex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(email);
		return matcher.matches();
	}
}
