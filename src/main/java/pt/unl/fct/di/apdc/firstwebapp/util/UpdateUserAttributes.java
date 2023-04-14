package pt.unl.fct.di.apdc.firstwebapp.util;

public class UpdateUserAttributes {
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
	
    public UpdateUserAttributes() {
    	
    }
    
	public UpdateUserAttributes(String email, String name, Boolean revealPerfil, Long telefone,
			Long telemovel, String ocupacao, String localTrabalho, String morada1, String morada2, String pfpURL, Long nif, String role, String estado) {
		this.email = email;
		this.name = name;
		this.revealPerfil = revealPerfil;
		this.telefone = telefone;
		this.telemovel = telemovel;
		this.ocupacao = ocupacao;
		this.localTrabalho = localTrabalho;
		this.morada1 = morada1;
		this.morada2 = morada2;
		this.pfpURL = pfpURL;
		this.nif = nif;
		this.role = role;
		this.estado = estado;
	}
	
	public boolean isRoleValid() {
		return (role.equals("USER") || role.equals("GBO") || role.equals("GS") || role.equals("GA") || role.equals("SU"));
	}
}
