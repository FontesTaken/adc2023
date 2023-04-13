package pt.unl.fct.di.apdc.firstwebapp.resources;


import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.google.appengine.repackaged.org.apache.commons.codec.digest.DigestUtils;
import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.Transaction;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;


import pt.unl.fct.di.apdc.firstwebapp.util.RegisterAcc;

@Path("/register")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class RegisterResource {
	
	private static final DateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZ");
	private final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
	private static final Logger LOG = Logger.getLogger(LoginResource.class.getName());
	

	public RegisterResource() {
	}
	
	
	@POST
	@Path("/OP1")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
	public Response registerAccount(RegisterAcc data) {
	    LOG.fine("Attempt to register account: " + data.username);
	    if (!data.isInputValid())
	        return Response.status(Status.FORBIDDEN).entity("Missing parameter").build();
	    if (!data.isEmailValid())
	        return Response.status(Status.FORBIDDEN).entity("Email invalid").build();
	    if (!data.isPasswordValid())
	        return Response.status(Status.FORBIDDEN).entity("Password invalid").build();
	    if (!data.doesPasswordMatch())
	        return Response.status(Status.FORBIDDEN).entity("Password doesn't match").build(); 
	    Key userKey = datastore.newKeyFactory().setKind("User").newKey(data.username);
	    Transaction txn = datastore.newTransaction();
	    try {
	        Entity userEntity = txn.get(userKey);
	        if (userEntity != null) {
	            txn.rollback();
	            return Response.status(Status.FORBIDDEN).entity("User already exists.").build();
	        } else {
	            Entity.Builder personBuilder = Entity.newBuilder(userKey)
	                    .set("email", data.email)
	                    .set("name", data.name)
	                    .set("password", DigestUtils.sha512Hex(data.password))
	                    .set("timecreation", fmt.format(new Date()))
	                    .set("role", "USER")
	                    .set("estado", "INATIVO");

	            if (data.revealPerfil != null) personBuilder.set("revealPerfil", data.revealPerfil);
	            if (data.telefone != null) personBuilder.set("telefone", data.telefone);
	            if (data.telemovel != null) personBuilder.set("telemovel", data.telemovel);
	            if (data.ocupacao != null) personBuilder.set("ocupacao", data.ocupacao);
	            if (data.localTrabalho != null) personBuilder.set("localTrabalho", data.localTrabalho);
	            if (data.morada1 != null) personBuilder.set("morada1", data.morada1);
	            if (data.morada2 != null) personBuilder.set("morada2", data.morada2);
	            if (data.pfpURL != null) personBuilder.set("pfpURL", data.pfpURL);
	            if (data.nif != null) personBuilder.set("NIF", data.nif);

	            Entity person = personBuilder.build();

	            txn.add(person);
	            LOG.info("User registered " + data.username);
	            txn.commit();
	            return Response.ok("{}").build();
	        }
	    } finally {
	        if (txn.isActive()) {
	            txn.rollback();
	        }
	    }
	}


	@POST
	@Path("/AdminOP1")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
	public Response registerAdminAccount() {
	    LOG.fine("Attempt to register admin account");
	    Key userKey = datastore.newKeyFactory().setKind("User").newKey("admin");
	    Transaction txn = datastore.newTransaction();
	    try {
	        Entity userEntity = txn.get(userKey);
	        if (userEntity != null) {
	            txn.rollback();
	            return Response.status(Status.FORBIDDEN).entity("User already exists.").build();
	        } else {
	            Entity.Builder personBuilder = Entity.newBuilder(userKey)
	                    .set("email", "adminEmail")
	                    .set("name", "adminName")
	                    .set("password", DigestUtils.sha512Hex("admin"))
	                    .set("timecreation", fmt.format(new Date()))
	                    .set("role", "SU")
	                    .set("estado", "ATIVO");

	            Entity person = personBuilder.build();

	            txn.add(person);
	            LOG.info("Admin registered");
	            txn.commit();
	            return Response.ok("{}").build();
	        }
	    } finally {
	        if (txn.isActive()) {
	            txn.rollback();
	        }
	    }
	}
	
}