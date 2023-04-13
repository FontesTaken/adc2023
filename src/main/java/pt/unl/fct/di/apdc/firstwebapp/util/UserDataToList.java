package pt.unl.fct.di.apdc.firstwebapp.util;

import com.google.cloud.datastore.Value;

public class UserDataToList {
	public String username;
	public String email;
	public String name;
	public Boolean revealPerfil;
	public Long telefone;
	public Long telemovel;
	public String ocupacao;
	public String localTrabalho;
	public String morada1;
	public String morada2;
	public String pfpURL;
	public Long nif;
	public String role;
	public String estado;


	public UserDataToList() {
	}

	public UserDataToList(String username, String email, String name) {
		this.username = username;
		this.email = email;
		this.name = name;
	}

	public void addValue(String property, Value<?> value) {
		switch (property) {
		case "estado":
			estado = (String) value.get();
			break;
		case "role":
			role = (String) value.get();
			break;
		case "revealPerfil":
			revealPerfil = (Boolean) value.get();
			break;
		case "telefone":
			telefone = (Long) value.get();
			break;
		case "telemovel":
			telemovel = (Long) value.get();
			break;
		case "ocupacao":
			ocupacao = (String) value.get();
			break;
		case "localTrabalho":
			localTrabalho = (String) value.get();
			break;
		case "morada1":
			morada1 = (String) value.get();
			break;
		case "morada2":
			morada2 = (String) value.get();
			break;
		case "pfpURL":
			pfpURL = (String) value.get();
			break;
		case "NIF":
			nif = (Long) value.get();
			break;
		}
	}

}